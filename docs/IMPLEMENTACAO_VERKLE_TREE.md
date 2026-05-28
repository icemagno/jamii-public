# 🌳 Implementação Verkle Tree - Jamii Blockchain

## 1. Visão Geral
A Jamii Blockchain migrou seu motor de estado de uma Merkle Tree tradicional para uma **Verkle Tree (Vector Commitment Merkle Tree)** de alto desempenho. Esta implementação utiliza o modelo **Bonsai Turbo**, que otimiza drasticamente o acesso ao disco e a eficiência de memória.

## 2. Como Funciona

### 2.1 Estrutura da Árvore (Arquitetura Esparsa)
- **Aridade 256**: Cada nó interno (`InternalNode`) possui até 256 filhos.
- **Sparse Nodes (Zero-Alloc)**: Em vez de vetores fixos, utilizamos mapas (`map[byte]VerkleNode`) e bitmasks (`populatedMask`). Isso reduz o consumo de memória de ~12KB para menos de 1KB por nó esparso.
- **Lazy Loading**: Os nós não são carregados inteiros para a RAM. O sistema carrega apenas os ramos necessários para a transação atual, utilizando o cache de `ChildCommitments` para reconstruir a raiz sem ler os dados dos filhos.
- **IPA Paralelo (Multi-Core)**: O cálculo de compromissos vetoriais (IPA/Bandersnatch) é executado em paralelo aproveitando todos os núcleos da CPU via goroutines e semáforos, permitindo ultrapassar os 1000 TPS em disco.

### 2.2 Modelo de Persistência (Bonsai Turbo)
Os dados são indexados no banco de dados (`PebbleStore`) usando três prefixos principais:
1.  **`d:` (Data/Flat Storage)**: Armazena os valores brutos (ex: saldo, nonce) indexados pelo hash da conta. A VM lê daqui em **O(1)**.
2.  **`n:` (Nodes)**: Armazena a estrutura da Verkle Tree (os nós internos e seus compromissos). Usado apenas para provar a integridade e calcular a nova raiz.
3.  **`l:` (Logs/Journal)**: Logs de rollback atômicos. Permitem que o sistema desfaça mudanças no Flat Storage (`d:`) caso um bloco seja revertido ou ocorra uma reorg, mantendo a árvore sincronizada.

### 2.3 Bandersnatch Committer (O Motor Criptográfico)
O `BandersnatchCommitter` é o coração matemático da Verkle Tree no Jamii. Ele é responsável por comprimir os dados dos nós em compromissos criptográficos seguros e eficientes.

- **Curva Bandersnatch**: Uma curva elíptica otimizada para operações dentro de circuitos SNARK e compromissos vetoriais.
- **IPA (Inner Product Argument)**: O esquema de compromisso utilizado para gerar a raiz do nó. Suas principais vantagens técnicas incluem:
  *   **Sem Trusted Setup**: Diferente de esquemas como KZG, o IPA não exige uma cerimônia de configuração confiável, garantindo transparência total.
  *   **Provas Logarítmicas**: O tamanho da prova de inclusão cresce de forma logarítmica, permitindo que provas de árvores massivas permaneçam compactas.
  *   **Agregação de Provas**: Permite fundir múltiplas provas de abertura em uma única prova agregada, essencial para a viabilidade de "Stateless Clients".
- **Atualização Homomórfica**: Em vez de recalcular todo o compromisso quando um valor muda, o sistema utiliza a propriedade homomórfica:
  $$C' = C + (new - old) \cdot G_i$$
  Onde $G_i$ é o i-ésimo ponto da base SRS (*Structured Reference String*). Isso reduz drasticamente o custo de CPU em atualizações de estado.
- **Otimização de Memória**: Utiliza um `sync.Pool` de elementos escalares (`fr.Element`) para minimizar a pressão do Garbage Collector durante o processamento de grandes lotes de transações.

### 2.4 Inicialização e Integridade Criptográfica
Para garantir a soberania do consenso e evitar estados corrompidos, a Jamii adota uma política de **fail-fast** na inicialização do motor criptográfico:

- **Remoção do DummyCommitter**: O uso de committer de fallback (Dummy) foi permanentemente removido do código de produção. Isso impede que o nó opere com lógica de hash simplificada que divergiria da rede oficial.
- **Aborto Crítico (Fail-Fast)**: Caso o `BandersnatchCommitter` falhe ao inicializar (por falta de memória, erro na geração da SRS ou incompatibilidade de CPU), o sistema emite um log de nível `CRITICAL` e encerra o processo (`os.Exit(1)`) imediatamente. Esta medida protege a integridade do `StateDB` contra escritas baseadas em compromissos inválidos.

## 3. Gestão de Recursos

### 3.1 Governador de Memória (Memory Governor)
Para garantir a coexistência com outros módulos, a Verkle Tree monitora o uso da Heap:
- **Teto de Higiene**: Quando a alocação ultrapassa 800MB, um ciclo de `Pruning` (poda) é disparado.
- **LRU Cache**: Os shards de cache descartam nós menos acessados para manter o working set dentro do orçamento de RAM definido.

### 3.2 Blindagem de Memória
Durante o desenvolvimento, detectamos que a biblioteca `go-ipa` realizava a reversão de bytes (Little-Endian) **in-place** nos buffers fornecidos. Implementamos uma camada de proteção que utiliza buffers de stack e tipos fixos (`[32]byte`) para evitar corrupção de estado e alocações desnecessárias no heap.

## 4. Pontos de Contato com o Sistema

### 4.1 StateDB (`pkg/core/state`)
O `StateDB` é o principal cliente da Verkle Tree. Quando você chama `st.Commit()`, o seguinte fluxo ocorre:
1. O `StateDB` envia todas as contas modificadas para o `UpdateBatch` da árvore.
2. A árvore atualiza o Flat Storage (`d:`) e marca os nós internos como sujos (`dirtyDisk`).
3. Os compromissos IPA são recalculados de baixo para cima.
4. Os logs de rollback (`l:`) são gerados associados à nova raiz.

### 4.2 Processor (`pkg/core/processor.go`)
O motor de execução de blocos interage com a árvore através da interface do `StateDB`. Ele não precisa saber que a árvore é Verkle, mas se beneficia da velocidade de leitura do Flat Storage para processar transações em paralelo.

### 4.3 Auditoria e Tools (`cmd/inspector`)
Ferramentas de inspeção de banco de dados utilizam o método `IterateLeaves` para percorrer o prefixo `d:` e exportar o estado completo da rede de forma eficiente, sem precisar reconstruir a árvore inteira.

## 5. Manutenção e Rollbacks
Para reverter o estado para uma altura anterior:
- Use `tree.RollbackToRoot(hash)`.
- Isso percorre os logs `l:`, restaura os valores antigos em `d:` e **invalida** o nó raiz em memória (`s.rootNode = nil`).
- Na próxima operação, o sistema recarregará automaticamente a estrutura correta do disco.

### Racional Técnico do Cache Particionado

A decisão de segmentar o cache da Verkle Tree em exatamente dezesseis shards independentes responde a um desafio crítico de escalabilidade em sistemas de alta concorrência: a contenção de locks em ambientes multi-core. Durante as fases iniciais da otimização Turbo, identificamos que o uso de um Mutex global para proteger o acesso aos nós da árvore criava um gargalo severo, onde múltiplos núcleos da CPU permaneciam em estado de espera (lock contention) enquanto uma única goroutine manipulava o cache. Ao fragmentarmos essa estrutura, distribuímos a carga de trabalho de forma que dezesseis operações possam ocorrer de maneira genuinamente paralela, reduzindo drasticamente a probabilidade de colisão e permitindo que o sistema escale de forma linear conforme mais poder de processamento é adicionado.

O número dezesseis não foi uma escolha arbitrária, mas sim o resultado de benchmarks exaustivos que buscaram o equilíbrio ideal entre concorrência e overhead. Descobrimos que aumentar o número de partições além desse ponto introduzia um custo de gerenciamento de memória excessivo, pois cada shard exige sua própria estrutura de controle LRU e mapas de busca. Por outro lado, um número menor de shards falhava em extrair a performance máxima de processadores modernos com alta contagem de threads. Além disso, a escolha de uma potência de dois permite que o motor de distribuição, que utiliza o algoritmo de hash FNV-1a, realize a localização do shard correto através de operações de bitwise extremamente rápidas. Essa abordagem matemática elimina a necessidade de divisões complexas no nível da CPU, garantindo que a descoberta do shard ocorra em poucos ciclos de instrução, o que foi fundamental para elevar o patamar de performance da Jamii de centenas para milhares de transações por segundo.

---
*Documentação gerada automaticamente para a Sprint 6 - Jamii Verkle Migration.*

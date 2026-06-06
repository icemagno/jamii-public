# 🔬 Pesquisa de Futuro: Evolução JamiiVM (Pós-Cancun)

Este documento rastreia as tendências industriais e mudanças de protocolo identificadas no Geth e Besu que devem ser consideradas para as próximas sprints da JamiiVM.

## 1. EVM Object Format (EOF) - EIP-3540 & EIP-3670
O EOF é a maior mudança na EVM desde sua criação, introduzindo um formato de bytecode estruturado com separação entre código e dados.

**Opcodes Identificados no Geth:**
- **Acesso a Dados:** `DATALOAD` (0xD0), `DATALOADN` (0xD1), `DATASIZE` (0xD2), `DATACOPY` (0xD3).
- **Saltos Estáticos:** `RJUMP` (0xE0), `RJUMPI` (0xE1), `RJUMPV` (0xE2).
- **Funções:** `CALLF` (0xE3), `RETF` (0xE4), `JUMPF` (0xE5).
- **Manipulação:** `DUPN` (0xE6), `SWAPN` (0xE7), `EXCHANGE` (0xE8).
- **Criação EOF:** `EOFCREATE` (0xEC), `RETURNCONTRACT` (0xEE).

**Impacto:** Exigirá uma refatoração em como a `EVM` carrega e valida o bytecode (`analysis.go` no Geth).

## 2. Fork Prague / Osaka (Próximas EIPs)
- **`SLOTNUM` (0x4B):** Expõe o número de slots ocupados ou disponíveis (em discussão).
- **`RETURNDATALOAD` (0xF7):** Leitura direta do buffer de retorno.
- **`EXTCALL` Family (0xF8 - 0xFB):** Novos opcodes de chamada otimizados para EOF.

## 3. Otimizações Jamii PQC (Soberania)
- **Instruções de Mirror Bridge:** Pesquisar opcodes nativos para validação de assinaturas MLDSA/Ed25519 em lote (Batch Verification) para reduzir o custo de gás em Rollups Jamii.
- **Precompilados Híbridos:** Implementação de suporte nativo a Kyber/Dilithium via opcodes especializados.

## 4. EIP-7702: Account Abstraction Refinada (Pectra)
Substituindo o EIP-3074, este EIP permite que contas comuns (EOAs) funcionem como Smart Contracts temporariamente durante a execução de um bundle.
- **Impacto na Wallet:** Permite batch transactions, gas sponsorship e permissões granulares sem migração de conta.
- **Implementação:** Exige ajustes no processador de transações (`core/state_processor.go`) para lidar com o novo tipo de transação (Type 4).

## 5. Verkle Trees (Statelessness)
Transição das Merkle Patricia Tries para Verkle Trees utilizando Vector Commitments.
- **Impacto:** Redução drástica no tamanho das provas (witnesses), permitindo que nós validem blocos sem o estado completo.
- **Foco:** Pesquisar integração com as bibliotecas de criptografia PQC já existentes na Jamii para garantir resistência quântica nas provas.

## 6. Execução Paralela Determinística (Besu/Monad Style)
Implementação de execução multi-threaded de transações sem conflitos de estado.
- **Metodologia:** Análise de `accessList` para identificar dependências e paralelizar a execução na JamiiVM.
- **Status Jamii:** A arquitetura atual de isolamento de estado (`pkg/store`) favorece a implementação de um scheduler otimizado.

## 7. Status de Tecnologias Consolidadas
- **[CONCLUÍDO] Armazenamento Estilo Bonsai:** A Jamii já utiliza uma estrutura de armazenamento "flat" (plana) com diferenciais de estado, eliminando o gargalo de amplificação de leitura tradicional das Merkle Patricia Tries clássicas.

---
*Atualizado em 28 de Abril de 2026 após homologação de infraestrutura de armazenamento.*

## 8. Pesquisa Profunda: Hash Agility, ERC-4337 e EIP-7702

Esta seção documenta a pesquisa realizada sobre a flexibilidade de endereçamento, agilidade de hash e a convergência entre contas comuns e contratos inteligentes.

### 8.1. Desafios da Mudança de Endereçamento em Produção
Abandonar a compatibilidade de endereçamento Ethereum (20 bytes derivados do Keccak256) em uma rede já em produção é uma mudança crítica que quebraria a rede imediatamente pelos seguintes motivos:

*   **Quebra da EVM:** A EVM opera assumindo endereços de 20 bytes em opcodes como `ADDRESS`, `CALLER`, `ORIGIN`, etc.
*   **Perda de Acesso a Fundos (Storage):** O mapeamento de saldos (ex: ERC-20) é baseado no hash do endereço. Mudar o endereço muda a chave de busca, resultando em saldo zero para todos.
*   **Corrupção do Estado (StateDB):** A árvore de estado usa o hash do endereço como indexador. Mudar o formato exige uma migração massiva de todo o banco de dados.
*   **Criptografia:** O remetente (`from`) é deduzido da assinatura ECDSA. Mudar a lógica de derivação invalidaria transações antigas e ferramentas externas.

### 8.2. Hash Agility na EVM
Hash Agility é a capacidade de mudar o algoritmo de hash sem redesenhar a arquitetura. Na EVM, isso é restrito porque o hash é a própria identidade (Endereço) e a estrutura de dados (Storage/Trie).

**Caminhos para Agilidade:**
*   **Account Abstraction (ERC-4337):** Permite que o endereço seja um contrato que valida assinaturas de qualquer tipo (Schnorr, BLS, PQC).
*   **Versionamento de Bytecode (EOF):** Permite novas regras de execução para novos contratos enquanto preserva os antigos.
*   **Multi-Hash Support:** Definir via Hard Fork blocos onde novos algoritmos de hash passam a ser usados para a árvore de estado.

### 8.3. ERC-4337: Account Abstraction em Operação
O ERC-4337 já está em vigor (desde março de 2023) e funciona como uma infraestrutura sobreposta:
*   **EntryPoint:** Um contrato central (`0x0000000071727De22E5E9d8BAf0edAc6f37da032`) que coordena a execução.
*   **UserOperations:** Objetos que substituem transações tradicionais em uma mempool alternativa.
*   **Bundlers & Paymasters:** Entidades que agrupam operações e podem patrocinar taxas de gás (gas sponsorship).

### 8.4. EIP-7702: A Convergência (Pectra Fork)
O EIP-7702 permite que contas comuns (EOAs) designem um código de contrato para si mesmas temporariamente durante uma transação.

**Funcionamento:**
1.  O usuário assina uma autorização delegando seu endereço para um código de contrato específico.
2.  O protocolo "instala" virtualmente esse código no endereço da EOA durante a transação.
3.  Isso permite batching, patrocínio de gás e privilégios de smart contract sem mudar o endereço ou migrar ativos.

### 8.5. Por que não a automação total (Enshrined AA)?
A associação automática de todas as contas ao ERC-4337 é evitada por:
*   **Custo de Gás:** EOAs são baratas (21.000 gas), enquanto Smart Accounts são caras (>100.000 gas) devido à execução da EVM.
*   **Segurança:** Um bug no contrato padrão afetaria a rede inteira. O modelo opcional (EIP-7702) isola o risco.
*   **Retrocompatibilidade:** Verificações como `tx.origin == msg.sender` em contratos antigos quebrariam se EOAs virassem contratos permanentemente.
*   **State Bloat:** Adicionar bytecode a centenas de milhões de endereços aumentaria drasticamente o tamanho do banco de dados de estado.

### 8.6. Mecanismo de Escolha do Usuário (EIP-7702)
O usuário escolhe a delegação via um novo tipo de transação contendo uma `Authorization List`:
*   A lista contém o `chain_id`, o `address` do contrato de destino e um `nonce`.
*   O usuário assina essa lista com sua chave privada.
*   A rede (especificamente o `state_processor.go`) aplica o código do contrato ao endereço do usuário apenas durante o contexto de execução daquela transação.

### 8.7. Riscos de Delegação Arbitrária
Embora um usuário possa apontar sua conta para qualquer contrato (ex: um ERC-20), existem proteções naturais:
*   **Isolamento de Storage:** Ao "copiar" o código do USDT, o usuário não ganha o saldo do USDT, pois os dados (storage) permanecem no contrato original.
*   **Contexto de Execução:** Se delegar para um contrato malicioso, o risco é **exclusivamente do usuário**, que pode ter seus fundos drenados pela lógica do código que ele mesmo autorizou a rodar em seu nome.
*   **Inicialização:** Contratos de carteira exigem um `owner` no storage. Como a EOA começa com storage vazio, um contrato delegado mal configurado simplesmente falharia por falta de permissão.

### 8.8. Criptografia de Provas: IPA vs. Recursão (Halo2/Aggregation)
...
Com a evolução para o *Statelessness* total, a recursão de provas (ZK-STARKs ou Halo2) será reavaliada para agregar provas de múltiplos blocos e estados, permitindo verificação instantânea por nós leves (Light Clients).

### 8.9. Soberania Stateless: Arquitetura e Estratégia Pragmática
...
*   Ao atingir um limite crítico, o Peer é **banido e colocado em uma lista negra** (Blacklist) persistente.

### 8.10. O Fracasso da Execução Especulativa e o Pivot para a Matemática
Durante a fase de otimização da Jamii, foi realizada uma tentativa exaustiva de implementar a **Execução Especulativa** (trabalhar no bloco $N+1$ enquanto o bloco $N$ ainda está sendo commitado). Esta abordagem foi declarada um **fracasso técnico definitivo** pelos seguintes motivos:

1.  **Dependência de Estado Atômica:** Em uma rede sequencial e determinística, o bloco $N+1$ depende integralmente do estado final resultante do bloco $N$. Sem a confirmação física (commit) do estado de $N$, a execução de $N+1$ frequentemente operava sobre dados inconsistentes ou "fantasmas", resultando em divergência de StateRoot.
2.  **Inutilidade da Espera:** Se o sistema precisa, invariavelmente, esperar pela confirmação do bloco anterior para garantir a integridade do próximo, a especulação não oferece ganho real de throughput, apenas complexidade de código e risco de corrupção.
3.  **Gargalo Real identificado (CPU vs. I/O):** Testes empíricos na Jamii demonstraram que o tempo gasto entre a finalização do bloco e o commit de disco é dominado pelo **cálculo da prova criptográfica (IPA/Bandersnatch)**, e não pela escrita física no SSD (PebbleDB). O sistema gasta mais tempo "pensando" na matemática da árvore do que "escrevendo" os bytes no disco.

**Deliberação Arquitetural:**
A Jamii descarta oficialmente a execução especulativa em favor da **Otimização de Prova (MSM/Pippenger)** e do **Nó Stateless**. Em vez de tentar "adivinhar" o futuro estado, a rede focará em:
*   **Hierarquia Híbrida:** Proposers Stateful (máquinas de HPC com aceleração para geração de provas) servindo Validadores Stateless (que verificam a matemática de forma ultra-rápida).
*   **Root Lagging (Pesquisa):** Avaliar a viabilidade de a `StateRoot` do bloco $N$ ser transportada no cabeçalho do bloco $N+1$, desacoplando o cálculo da prova do caminho crítico do round de consenso.

### 8.11. Pré-Montagem de Bloco pelo Sucessor (Optimistic Slot Lead)
Uma pesquisa estratégica para reduzir o *gap* de ociosidade entre blocos é permitir que o sucessor imediato (próximo Proposer) inicie seu trabalho de montagem antes da finalização física do bloco atual.

**A Estratégia:**
1.  **Identificação Preditiva:** Utilizando a função `getNextProposer()` e monitorando a saúde dos pares via DTS, um nó identifica antecipadamente que será o próximo a propor (seja por fluxo normal ou *early round change*).
2.  **Mempool Isolation:** O nó remove da sua MemPool as transações já selecionadas pelo Proposer atual e inicia a seleção das transações subsequentes (ex: TXs 6, 7, 8...).
3.  **Execução Virtual (RAM-Only):** O nó executa o seu bloco futuro em uma cópia efêmera do estado na RAM enquanto o disco e a CPU estão ocupados processando o commit/prova IPA do bloco anterior.

**Bloqueios Técnicos a Resolver:**
*   **Dependência Atômica:** O `ParentHash` e a `StateRoot` só são conhecidos após o commit do bloco anterior. 
*   **Late Stamping:** Pesquisar técnica de "Carimbo Tardio", onde o bloco é montado "sem cabeça" e o cabeçalho é preenchido no microssegundo em que o estado anterior é confirmado.
*   **Invalidade por Efeito Colateral:** Resolver casos onde uma TX do bloco anterior invalida o saldo ou nonce de uma TX pré-selecionada para o bloco atual.

### 8.12. Witness-Aided Quorum Acceleration (Arquitetura de Verificação Assimétrica)
Esta estratégia visa eliminar o gargalo de CPU detectado na Jamii (cálculo redundante de provas IPA por todos os validadores) através de um modelo de confiança matemática assimétrica.

**Implementação e Resultados (Fase 9):**
1.  **Validadores (Stateless-Momentary):** Implementado o método `ApplyWitness` no `StateDB`, que injeta os saldos e nonces na RAM antes da execução. O `VerifyPayload` foi otimizado para realizar a validação em **Sandbox de RAM**, pulando a matemática pesada da Verkle Tree (IPA) durante a fase de votação.
2.  **Aceleração de Quórum:** Testes com 1.000 TXs mostraram que o tempo para atingir o quórum (votos de rede) caiu para a casa dos **milissegundos**, independentemente da escrita em disco.
3.  **Segurança (O Protocolo "Bala na Agulha"):**
    *   **Post-Quorum Commit:** Confirmada a necessidade de manter o commit de disco sequencial e posterior ao quórum. 
    *   **Fracasso do Pipeline Assíncrono:** Uma tentativa de executar o bloco em background enquanto o próximo iniciava falhou devido ao **Double Signing de Nonces** na RAM. A atomicidade do estado exige que o `ApplyWitness` e a execução do bloco $N$ terminem na RAM antes do bloco $N+1$ começar.

**Status Atual:**
*   **Proposer:** Gera a Witness automaticamente (via `accessList` corrigida).
*   **Validator:** Vota instantaneamente via Witness.
*   **Rede:** Estabilizada em modo de verificação acelerada com persistência síncrona pós-quórum.

**Próximos Desafios (Sprint 9.3):**
*   **Storage Witness:** Expandir a `accessList` para rastrear não apenas contas, mas também os slots de storage acessados por contratos inteligentes, permitindo que DApps complexos também rodem em RAM pura.
*   **Serialização Binária (PPQ):** Migrar o payload da Witness de JSON para um formato binário compacto (inspirado em SSZ) para reduzir o overhead de banda e o tempo de parse.

---

### 9. Conclusões Estratégicas e Priorização

1.  **Verkle Trees (A mais estratégica agora):**
    *   **Por que:** Momento perfeito para mudar para Verkle Trees se a `pkg/trie/smt.go` for reconstruída.
    *   **Vantagem Jamii:** O Bonsai Turbo favorece essa mudança, pois a árvore serve apenas para Root e Provas.
    *   **Facilidade:** Interface SparseMerkleTree pode ser mantida.

2.  **EOF (Obrigatória para Conformidade):**
    *   **Por que:** Garante compatibilidade com a próxima geração de contratos Solidity.
    *   **Impacto:** Afeta apenas a execução (`pkg/vm`), sem quebrar consenso de estado histórico.

3.  **EIP-7702 (Aceleração de AA):**
    *   **Por que:** Torna-se trivial com endereços unificados. Exige apenas o novo tipo de transação e carregamento temporário de bytecode.

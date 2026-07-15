# Jamii: The Sovereign Post-Quantum Blockchain

<img width="2816" height="1536" alt="jamii" src="https://github.com/user-attachments/assets/717f708c-02a7-4b8f-87f8-1844188e2183" />


Jamii é uma blockchain de alta performance projetada para a era pós-quântica. Ela combina algoritmos tradicionais (Secp256k1) com assinaturas de rede baseadas em reticulados (**ML-DSA-65/Dilithium L3**) para garantir soberania digital e resistência contra computadores quânticos.


⚠️ Licença: Este sistema está protegido sob a PolyForm Noncommercial License 1.0.0. O uso e a modificação são livres para fins não comerciais, desde que mantidos os créditos ao autor original. Uso comercial é estritamente proibido.


## 🚀 Status do Projeto: Sprint 9.4 Concluída (Junho/2026)

O núcleo fundamental do Jamii atingiu maturidade industrial, quebrando recordes de throughput e estabilidade através de processamento paralelo e especulativo, com foco recente em resiliência de rede e otimização do consenso sob sincronismo.

*   **`Consenso Resiliente (Watchdog de Liderança)`:** Redução do tempo de troca de propositor inativo de 10s para **2 segundos** se o propositor designado for detectado como "not ready" (syncing) ou desconectado.
*   **`CPU-Safe Sync (Observer Mode)`:** Filtros estritos que evitam a exaustão de CPU e estouro de memória sob inundações de rede (floods), descartando transações e selos do consenso (`MT_VALIDATOR_SEAL`) enquanto o nó estiver em modo Observer.
*   **`BitTorrent Sync Resiliente`:** Correção de concorrência e carregamento de metadados no boot, garantindo sincronização robusta e rápida do histórico em menos de 2 segundos para gaps grandes de blocos.
*   **`Archiver Sync Atômico`:** Sincronismo contínuo e callbacks DTS integrados no nó de arquivo desacoplado.
*   **`Chained State Oracle` (Consistência):** Montagem síncrona pós-pacing com vazão de **94.8 TPS** sustentada sob flood em ambiente multi-servidor.
*   **`pkg/trie` (v1.5):** Arquitetura *Bonsai Turbo* com **Verkle Tree Homomórfica (O(1))**.

## 🛠️ Diferenciais Tecnológicos (The Jamii Edge)

*   **Identidade Unificada Shadowless (Zero Migration UX):** O ecossistema Jamii elimina o maior atrito da transição pós-quântica. Um único payload de chaves tradicionais (Secp256k1) e quânticas (ML-DSA-65) mapeia para a mesma conta no World State, servindo de forma unificada tanto a endereços clássicos Ethereum (`0x...`) quanto soberanos (`jamii1...`), sem necessidade de migrações de saldos.
*   **Bonsai Turbo (O(1) I/O nativo em Go):** Esqueça a descida de árvore tradicional de outros clientes ($O(\log N)$ acessos a disco). A Jamii implementa o Bonsai Turbo em PebbleDB, garantindo que o acesso a saldos e contas ocorra em tempo constante $O(1)$, com escrita de árvore em segundo plano.
*   **Trie Criptográfica Paralela (Multi-core SMT & Verkle):** Seja usando SMT ou a inovadora Verkle Tree (IPA/Bandersnatch), a Jamii paraleliza a computação de hashes de subárvores e compromissos polinomiais complexos usando goroutines e semáforos, aproveitando 100% dos núcleos de CPU.
*   **Chained State Oracle (Synchronous Pacing):** O Jamii utiliza a cadência física de blocos (Block Period) para garantir a consistência do estado pai. A montagem do bloco ocorre de forma estritamente síncrona e segura no início do turno de liderança (pós-pacing), sobre uma base do PebbleDB totalmente consolidada e livre de escritas concorrentes, unindo robustez a tempos de execução de milissegundos.
*   **Witness-Aided Quorum Acceleration:** Diferente de outras blockchains, a Jamii separa a prova de estado do quórum de votação. Validadores usam o "Witness" para aprovar o bloco instantaneamente em RAM, enquanto a prova IPA pesada é processada em paralelo, eliminando o gargalo de latência do IBFT tradicional.
*   **Ultra-Compact Skeleton (Bi-Polar):** Redução de **81%** no tráfego de rede. Blocos com 3.000 TXs trafegam quase sem overhead, usando identificadores de 6 bytes (3 iniciais + 3 finais), permitindo que o dado chegue antes do sinal de consenso.
*   **DTS (Distributed Transaction Store):** Motor P2P de canal duplo (Express/Bulk). Sinais de consenso viajam por "vias rápidas" sem serem bloqueados por downloads de blocos pesados.

## ⚡ Desempenho em Rede Distribuída (Julho/2026)

Abaixo estão os resultados reais medidos sob flood massivo simultâneo (90.000 transações divididas em 3 fluxos de 30.000 TXs) em rede física distribuída em 3 servidores de produção (5 validadores + Archiver):

| Métrica de Eficiência | Vazão em Produção | Destaque Técnico |
| :--- | :--- | :--- |
| **Throughput Médio** | **~94.8 TPS** | Vazão real sustentada sob flood de transações Dilithium (PQC Nível 3). |
| **Pico de Vazão** | **107.0 TPS** | Máxima velocidade de escoamento e comits registrada sob flood contínuo. |
| **Tempo de Bloco** | **3.0s a 3.1s** | **Block Pacing:** Cadência perfeitamente homogênea com `blockperiod = 3s` configurado. |
| **Tempo de Consenso** | **< 100ms** | A votação física (Propose/Prepare/Commit) no DTS leva apenas ~100ms, restando ~2.9s de ociosidade/pacing. |
| **Gravação PebbleDB** | **O(1) Efficiency** | **Bonsai Turbo:** Persistência determinística em tempo de milissegundos. |
| **Reconstrução Bi-Polar** | **~95% Economia** | Reconstrução de blocos cheios localmente a partir de Short IDs (compact block skeleton). |

### Avaliação do Comportamento da Rede:
* **Ampla Folga Operacional:** A rede opera com extrema estabilidade. De um tempo de bloco de 3.0s, cerca de 2.9s representam tempo de pacing ocioso (o nó aguarda a expiração do timestamp mínimo do bloco anterior). O processamento de Consensus, EVM e I/O físico em disco (PebbleDB) consome apenas cerca de 100ms, demonstrando que a rede está longe do limite de saturação física.
* **Auto-Recuperação e Resiliência (Watchdog Keep-Alive):** O Watchdog de quórum do nó transmite ativamente atualizações de status a cada 5 segundos. Em caso de oscilações de rede ou atraso de sincronização, as tabelas locais dos validadores se autoregulam em tempo real, destravando o consenso de forma imediata e transparente.


## 🛡️ Documentação e Auditoria

O Jamii é regido por uma arquitetura rigorosa e documentada. Consulte os pilares da nossa documentação:

*   📜 **[Documentação Técnica (Arquitetura)](docs/DOCUMENTACAO_TECNICA.md):** A autoridade sobre o funcionamento interno.
*   🛠️ **[Manual de Desenvolvimento (Diretrizes)](docs/MANUAL_DESENVOLVIMENTO.md):** Regras de código e performance.
*   🧠 **[Memória Técnica Consolidada](MEMORIA_TECNICA.md):** Resumo executivo da arquitetura e decisões de design.
*   🏁 **[Plano Geral do Projeto](docs/PLANO_GERAL_PROJETO.md):** Roadmap e marcos de evolução.
*   ⚡ **[Otimizações Jamii Turbo](docs/jamii_turbo_optimization.md):** Detalhes sobre a arquitetura Zero-Alloc/Zero-Copy.

### Resultados da Auditoria (Abril/2026):
*   ✅ **Consenso de Estado (Verkle/SMT):** Estado convergente garantido via Trie Factory e indexação determinística.
*   ✅ **Criptografia L3:** Sincronização total com ML-DSA-65 (Nível 3).
*   ✅ **Imutabilidade de Memória:** Prevenção de Data Races no cálculo de hashes de transação.
*   ✅ **Resiliência DoS:** Operações aritméticas seguras com tratamento de erros.

## 🛠️ Começando

### Configuração Inicial (Setup)
Antes de iniciar um nó pela primeira vez, você pode gerar sua identidade soberana (chave privada e manifesto de endereços) usando o comando de setup:

```bash
# Gera a identidade no diretório padrão (./data)
jamii.exe setup generate-key

# Ou especificando um diretório customizado
jamii.exe setup generate-key --datadir ./meu_node_data
```

Este comando criará o arquivo `nodekey` (chave privada híbrida PQC) e o `node_address.json` contendo seu **Sovereign Address**, necessário para inclusão no arquivo `genesis.json` caso você deseje ser um validador.

### Iniciando o Nó
```bash
docker run \
  --name jamii-node \
  -v ./config.yaml:/config.yaml:ro \
  -v ./genesis.json:/genesis.json:ro \
  -v ./peers.json:/peers.json:ro \
  -v ./datadir:/datadir \
  -p 8545:8545 \
  -p 30303:30303 \
  -p 42000:42000 \
  -d magnoabreu/jamii-node:0.1.0-alpha 
```

### Iniciando o Archiver
```bash
docker run \
  --name jamii-archiver \
  -v ./config.yaml:/config.yaml:ro \
  -v ./genesis.json:/genesis.json:ro \
  -v ./peers.json:/peers.json:ro \
  -v ./datadir:/datadir \
  -p 8556:8556 \
  -p 30310:30310 \
  -p 42020:42020 \
  -d magnoabreu/jamii-archiver:0.1.0-alpha 
```  
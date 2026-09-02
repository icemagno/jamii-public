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

## ⚡ Desempenho em Rede Distribuída (Agosto/2026)

Abaixo estão os resultados reais medidos sob teste de estresse sustentado (*Soak Test*) submetido por 5 instâncias concorrentes do gerador de tráfego pós-quântico (`cmd/traffic`), mantendo 10.000 carteiras efêmeras ativas disparando transações simultâneas via JSON-RPC em rede física de produção:

| Métrica de Eficiência | Vazão e Latência Apuradas | Destaque Técnico / Metodologia |
| :--- | :--- | :--- |
| **Throughput Sustentado** | **65,6 a 82,3 TPS** | Média contínua de transações pós-quânticas seladas e finalizadas sob soak test prolongado. |
| **Pico de Vazão (Grafana)** | **140,0 TPS** | Máxima velocidade de escoamento e comits em janelas móveis de tempo real sob surtos de bloco. |
| **Carga de Entrada Submetida** | **500 TPS** | Pressão de entrada gerada por 10.000 carteiras PQC efêmeras operadas por 5 instâncias paralelas. |
| **Taxa de Confirmação** | **99,985%** | Mais de 20.000 transações confirmadas com apenas 3 descartes isolados sob concorrência extrema. |
| **Estabilidade da MemPool** | **~500 TXs** | Ponto de equilíbrio perfeito em estado estacionário (vazão de drenagem emparelhada com RPC). |
| **Verificação Criptográfica PQC** | **500 µs a 1,0 ms** | Latência de validação de assinaturas pós-quânticas ML-DSA-65 em sub-milissegundos. |
| **Execução na VM (EVM)** | **573 µs a 1,0 ms** | Latência de processamento de contratos e transferências na EVM em sub-milissegundos. |
| **Commit StateDB (PebbleDB)** | **20 ms a 50 ms** | Persistência determinística em tempo de milissegundos (Arquitetura Bonsai Turbo). |
| **Socorro Expresso (DTS EXPRESS)**| **5 ms a 18 ms** | Resgate reativo de transações faltantes em blocos compactos com injeção forçada (`AddForced`). |
| **Estabilidade de Consenso IBFT** | **100% em Round 0 (`R:0`)** | Zero estouros de timeout de rodada ao longo de centenas de blocos consecutivos. |

<img width="1866" height="957" alt="jamii" src="https://github.com/user-attachments/assets/7b2381f5-d94c-41a5-b809-7832d17558a2" />


### Avaliação do Comportamento da Rede:
* **Resiliência e Recuperação Reativa no Canal EXPRESS:** Em casos pontuais onde transações fofocadas no canal BULK sofrem atrasos micro-temporais, o motor de socorro expresso (`MT_EXPRESS_TX_REQ`) resgata as transações faltantes junto ao proponente em apenas 5 a 18 milissegundos. O mecanismo de injeção forçada (`AddForced`) garante a reconstrução instantânea do bloco compacto sem provocar estouros de timeout no consenso IBFT.
* **Estabilidade de Estágio Estacionário:** A MemPool oscila de forma saudável na faixa de 500 transações pendentes, provando que a taxa de drenagem do consenso emparelhou perfeitamente com o ritmo do RPC sem vazamento de memória RAM.

### 🔐 Baseline Criptográfico PQC e Tráfego de Rede DTS (ML-DSA-65 vs Falcon Target)

Para fundamentar o plano futuro de **Rotação Dinâmica de Algoritmos PQC** e benchmark comparativo com o **Falcon**, foi estabelecido o baseline oficial de tráfego de rede P2P (motor DTS) e custos de payload da criptografia nativa **ML-DSA-65** (Dilithium Nível 3 NIST):

| Parâmetro PQC / Rede | Valor Baseline (ML-DSA-65) | Projeção / Alvo para Rotação com **Falcon-512** | Impacto Técnico no Motor DTS |
| :--- | :--- | :--- | :--- |
| **Tamanho da Chave Pública** | **1.952 bytes** | **897 bytes** (Redução de 54%) | Menor footprint de armazenamento no cadastro de contas/StateDB. |
| **Tamanho da Assinatura** | **3.309 bytes** | **666 bytes** (Redução de 80%) | Drástica redução de overhead por transação e bloco compacto. |
| **Tamanho Médio da Transação** | **~5,2 KB / TX** | **~1,6 KB / TX** (Redução de ~70%) | Pacotes HTTP RPC e P2P significativamente mais leves. |
| **Payload do Witness de Bloco**| **65 KB a 82 KB / bloco** | **~20 KB a 25 KB / bloco** (250 TXs) | Bloco compacto (*Skeleton Witness*) 3.2x mais rápido de transmitir. |
| **Tráfego DTS - Canal BULK** | **150 a 350 KB/s** (Picos 800 KB/s) | **~30 a 70 KB/s** (Est. mesma TPS) | Alívio maciço na saturação de banda dos validadores P2P. |
| **Tráfego DTS - Canal EXPRESS** | **10 KB/s a 25 KB/s** | **~3 KB/s a 8 KB/s** | Vias rápidas de consenso e socorro ainda mais instantâneas. |
| **Latência de Verificação** | **500 µs a 662 µs** (0,5 ms) | *A medir na implementação* | Desempenho de validação de assinaturas em CPU multicore. |

> **Nota Arquitetural para Rotação Futura:** Como a assinatura do **Falcon-512 (666 bytes)** é **80% menor** que a do ML-DSA-65 (3.309 bytes), o tráfego do motor DTS no canal BULK deve cair de 150-350 KB/s para apenas ~30-70 KB/s para manter a mesma taxa de TPS (~80 TPS). Este baseline de rede medido sob estresse servirá de métrica de controle oficial para a transição.

## 🛡️ Documentação e Auditoria

O Jamii é regido por uma arquitetura rigorosa e documentada. Consulte os pilares da nossa documentação:

*   📜 **[Documentação Técnica (Arquitetura)](docs/DOCUMENTACAO_TECNICA.md):** A autoridade sobre o funcionamento interno.
*   🛠️ **[Manual de Desenvolvimento (Diretrizes)](docs/MANUAL_DESENVOLVIMENTO.md):** Regras de código e performance.
*   🧠 **[Memória Técnica Consolidada](MEMORIA_TECNICA.md):** Resumo executivo da arquitetura e decisões de design.
*   🏁 **[Plano Geral do Projeto](docs/PLANO_GERAL_PROJETO.md):** Roadmap e marcos de evolução.
*   ⚡ **[Otimizações Jamii Turbo](docs/jamii_turbo_optimization.md):** Detalhes sobre a arquitetura Zero-Alloc/Zero-Copy.
*   ⚙️ **[Configuração](CONFIG.md):** Parâmetros do arquivo config.yaml e peers.json.
*   ⚙️ **[Arquivo de Gênesis](GENESIS.md):** Configuração do Gênesis.

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

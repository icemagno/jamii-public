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


## ⚡ Desempenho em Rede Distribuída & Benchmarks PQC

### 🚀 Benchmark de Super Carga Massiva: Falcon-512 & Assinaturas Híbridas (Setembro/2026)

Abaixo estão os resultados apurados durante o teste de carga massiva na rede física de produção composta por 3 nós validadores (`ALBERNAZ`, `BONAPARTE` e `CHARLIE`). O teste avaliou o desempenho real com transações e assinaturas de contas usando **Falcon-512**, enquanto os validadores operaram o consenso com assinaturas híbridas (**ML-DSA-65** + Clássica). Os dados foram extraídos dos logs de auditoria de bloco ([super-carga-albernaz.txt](file:///c:/Magno/Projetos/jamii/metrics/super-carga-albernaz.txt)) e da telemetria oficial do Grafana ([jamii-super-carga-falcon.png](file:///c:/Magno/Projetos/jamii/metrics/jamii-super-carga-falcon.png) e [jamii-super-carga-falcon2.png](file:///c:/Magno/Projetos/jamii/metrics/jamii-super-carga-falcon2.png)):

| Métrica de Eficiência | Valor Medido em Produção (Falcon-512 & ML-DSA-65) | Destaque Técnico / Metodologia |
| :--- | :--- | :--- |
| **Throughput Sustentado** | **120,9 TPS** | Média contínua ao longo de 57 blocos consecutivos (37.166 TXs finalizadas em 5min 7s em log auditado). |
| **Pico de Vazão Efetiva (Grafana)** | **351,0 req/s** | Picos máximos de velocidade de processamento no nó ALBERNAZ (250 a 351 req/s nos 3 validadores). |
| **Total Confirmativo Acumulado** | **67.000 TXs (67,0 K)** | Transações pós-quânticas processadas e persistidas com 100% de determinismo no StateDB. |
| **Taxa de Confirmação & Consenso** | **100% em Round 0/1 (`R:0`)** | Zero estouros de timeout de rodada; quórum de COMMIT atingido na 1ª tentativa em todos os blocos (#639 ao #695). |
| **Estabilidade da MemPool** | **Picos de 1,19 K a 2,07 K TXs** | Alta capacidade de absorção e drenagem contínua sob surtos de injeção massiva. |
| **Verificação Criptográfica PQC** | **285 µs a 480 µs (Max 0,48 ms)** | Validação de assinaturas **Falcon-512** e **ML-DSA-65** ultra-rápida em sub-milissegundos (< 0,5 ms). |
| **Execução na VM (EVM)** | **40 µs a 86 µs** | Latência base de processamento de estado na VM em microssegundos (picos de 1,08ms a 14,3ms em lotes). |
| **Commit StateDB (PebbleDB)** | **78,8 ms a 196 ms** | Persistência determinística da árvore de estado para lotes de até 1.300 TXs por bloco. |
| **Intervalo Adaptativo entre Blocos**| **4,2 s a 5,0 s** | Cadência adaptativa do consenso IBFT alinhada ao empacotamento adaptativo de transações. |
| **Tráfego DTS - Canal BULK** | **100 a 241 KB/s (Max 539 KB/s OUT)** | Economia expressiva de banda de rede decorrente da assinatura compacta Falcon-512 (666 bytes). |
| **Tráfego DTS - Canal EXPRESS** | **35 a 45 KB/s (Max 64 KB/s OUT)** | Tráfego ultra-enxuto para mensagens de sinalização e votos do consenso dos validadores via ML-DSA-65. |

---

### 📊 Benchmark Histórico de Referência (Agosto/2026 - Baseline ML-DSA-65 Puro)

Abaixo mantêm-se os resultados do teste de estresse sustentado (*Soak Test*) prévio com 5 instâncias paralelas do gerador de tráfego pós-quântico (`cmd/traffic`) operando estritamente com **ML-DSA-65**:

| Métrica de Eficiência | Vazão e Latência Apuradas (ML-DSA-65 Baseline) | Destaque Técnico / Metodologia |
| :--- | :--- | :--- |
| **Throughput Sustentado** | **65,6 a 82,3 TPS** | Média contínua de transações ML-DSA-65 seladas e finalizadas sob soak test prolongado. |
| **Pico de Vazão (Grafana)** | **140,0 TPS** | Máxima velocidade de escoamento em janelas móveis sob surtos de bloco. |
| **Carga de Entrada Submetida** | **500 TPS** | Pressão de entrada gerada por 10.000 carteiras PQC efêmeras operadas por 5 instâncias paralelas. |
| **Taxa de Confirmação** | **99,985%** | Mais de 20.000 transações confirmadas com apenas 3 descartes isolados sob concorrência extrema. |
| **Estabilidade da MemPool** | **~500 TXs** | Ponto de equilíbrio em estado estacionário (vazão de drenagem emparelhada com RPC). |
| **Verificação Criptográfica PQC** | **500 µs a 1,0 ms** | Latência de validação de assinaturas pós-quânticas ML-DSA-65 em sub-milissegundos. |

---

### 🔐 Matriz Comparativa Criptográfica PQC Homologada (ML-DSA-65 vs Falcon-512)

Com a homologação e o teste de carga massiva do **Falcon-512**, os resultados práticos comprovam os ganhos expressivos de footprint e rede previstos na arquitetura:

| Parâmetro PQC / Rede | ML-DSA-65 (NIST L3) | Falcon-512 (NIST L1) | Ganho / Impacto Técnico no Motor DTS |
| :--- | :--- | :--- | :--- |
| **Tamanho da Chave Pública** | **1.952 bytes** | **897 bytes** | **Redução de 54%** no footprint de armazenamento do StateDB. |
| **Tamanho da Assinatura** | **3.309 bytes** | **666 bytes** | **Redução de 80%** no overhead por transação e bloco compacto. |
| **Tamanho Médio da Transação** | **~5,2 KB / TX** | **~1,6 KB / TX** | **Redução de ~70%** no payload HTTP RPC e tráfego P2P. |
| **Payload do Witness de Bloco**| **65 KB a 82 KB** (250 TXs) | **45 KB (103 TXs) a 320 KB (1.300 TXs)** | Transmissão de blocos massivos com até 1.300 TXs mantendo payload compacto. |
| **Tráfego DTS - Canal BULK** | **150 a 350 KB/s** (@ ~80 TPS) | **100 a 241 KB/s** (@ **121 a 351 TPS**) | **Rendimento 3x superior** de TPS consumindo a mesma banda de rede. |
| **Tráfego DTS - Canal EXPRESS** | **10 KB/s a 25 KB/s** | **35 KB/s a 45 KB/s** | Sinalização de consenso mantida em vias rápidas dedicadas. |
| **Latência de Verificação PQC** | **500 µs a 1,0 ms** | **285 µs a 480 µs (< 0,48 ms)** | Verificação computacional **2x mais rápida em CPU**. |

> **Nota de Desempenho Homologada:** A implementação do **Falcon-512** permitiu elevar a capacidade máxima da rede Jamii para **351 TPS em pico** e **120,9 TPS sustentados**, reduzindo o tamanho da assinatura por transação para apenas **666 bytes** e mantendo o tempo de verificação criptográfica abaixo de **0,5 milissegundos**.

<img width="1866" height="957" alt="jamii" src="https://github.com/user-attachments/assets/7b2381f5-d94c-41a5-b809-7832d17558a2" />


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

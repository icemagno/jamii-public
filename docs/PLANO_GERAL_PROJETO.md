# Jamii Blockchain: Master Plan & Roadmap Industrial

Este documento define o planejamento estratégico de longo prazo para a construção da Jamii Blockchain, consolidando a inspiração técnica em protocolos estabelecidos (Besu/Geth) com a inovação soberana em criptografia híbrida e PQC.

---

## 🏗️ Fase 1: Motores e Fundações (CONCLUÍDA)
*Objetivo:* Construir os componentes atômicos, performáticos e seguros.
- [x] **Módulo CRYPTO:** Implementação soberana de assinaturas híbridas (Secp256k1 + ML-DSA) com Strong Binding.
- [x] **Módulo ENCODING:** Serialização SSZ-based otimizada para payloads extensos.
- [x] **Módulo STORE:** Camada de persistência resiliente baseada no PebbleDB (100% Go).
- [x] **Módulo TRIE:** 
    - [x] Sparse Merkle Tree (SMT) de alta performance.
    - [x] **Verkle Tree Industrial** (256-vias com IPA).
    - [x] **Trie Factory** (Abstração total entre motores).
- [x] **Módulo STATE (Unified Identity):** Identidade Mirror (0x) e Sovereign compartilhando o mesmo estado nativo.
- [x] **Módulo CONSENSUS:** Máquina de estados IBFT 2.0 adaptada para o ecossistema Jamii.

---

## 🏗️ Fase 2: Saneamento e Orquestração (CONCLUÍDA)
*Objetivo:* Transformar módulos isolados em um nó funcional e garantir a integridade da transição de estado.

### 🧹 Saneamento Arquitetural (Dívida Bloqueante)
- [x] **Integridade da Transação (pkg/encoding):** Adição do campo `GasPrice` e suporte SSZ.
- [x] **Integridade do Cabeçalho (pkg/core/types):** Adição do campo `ReceiptsRoot`.
- [x] **Fluxo de Gas (Buy Gas):** Débito preventivo de `GasLimit * GasPrice` funcional no `StateProcessor`.
- [x] **Fluxo de Gas (Refund Gas):** Estorno de saldo não utilizado (`GasLimit - GasUsed`) ao remetente após execução. [CONCLUÍDO - 04/05/2026]
- [x] **Cálculo de ReceiptsRoot:** Implementar agregação Merkle de recibos no bloco e validação no `Processor`. [CONCLUÍDO - 04/05/2026]
- [x] **Ghost Root Killer (pkg/core/processor):** Implementação de snapshots de transação e garantia de StateRoot determinístico em blocos vazios. [CONCLUÍDO - 11/05/2026]
- [x] **Validação de Preço Mínimo em Bloco (Anti-Proposer Malicious):** Implementar trava no `StateProcessor` que rejeita transações com `GasPrice` inferior ao mínimo da rede. [CONCLUÍDO - 28/05/2026]

### 🏗️ Orquestração de Execução
- [x] **Standard Log (Log4J Style):** Unificação da telemetria com motor assíncrono (FIFO).
- [x] **Módulo NODE (Orquestrador):** Gerenciamento do ciclo de vida dos motores (EVM, Consenso, Store).
- [x] **Integração VM/Processor:** O `StateProcessor` agora orquestra a execução de bytecode via `EVM.Run`.
- [x] **Fase P3 da VM (Jump Table):** Migração do loop de execução para despacho via tabela (Besu Compliance). [CONCLUÍDO - 28/05/2026]
- [x] **Ativação de Contract Creation (Top-Level):**
    - [x] Habilitar fluxo `tx.To == nil` no `StateProcessor`.
    - [x] Implementar regra de derivação de endereço baseada no nonce do sender para transações externas.
    - [x] Adicionar campo `ContractAddress` no objeto `Receipt` para conformidade industrial. [CONCLUÍDO - 28/05/2026]
- [x] **Main Loop & Lifecycle:** Implementação do laço de trabalho que orquestra a transição de alturas e quórum sincronizado. [CONCLUÍDO - 11/05/2026]
- [x] **CLI Framework:** Implementação de comandos e flags via cobra (cmd/jamii). [CONCLUÍDO - 11/05/2026]
- [x] **Configuração Dinâmica:** Suporte a YAML/JSON para Gênese.
- [x] **Identidade de Rede Imutável:** Persistência de parâmetros pétreos no DB (Besu Compliance). [CONCLUÍDO - 28/05/2026]
- [x] **Mecanismo LRU (pkg/util/cache):** Cache industrial para o StateDB (MaxWarmTries). [CONCLUÍDO - 21/05/2026]

---

## 🌐 Fase 3: Rede Soberana (P2P Networking) (CONCLUÍDA)
*Objetivo:* Comunicação descentralizada, robusta e imune a ataques de eclipse.
- [x] **Discovery Service:** Implementação de lista estática e transição para Kademlia (DHT).
- [x] **Gossip Protocol (Turbine-style):** Disseminação não-linear e fragmentada de propostas via DTS (Block Sharding) para mitigar o peso das assinaturas PQC.
- [x] **Recuperação Reativa de TXs:** Mecanismo sob demanda para reconstrução de compact blocks. [CONCLUÍDO 08/05/2026]
- [x] **Timeout Configurável (BlockPeriod):** Implementação da configuração de tempo base do consenso via `genesis.json`, permitindo o ajuste fino da rede sem alteração de código. [CONCLUÍDO 08/05/2026]
- [x] **Sync Engine Resilience:** Implementação de drenagem de canais, ignorância de blocos atrasados e verificação recursiva "Catch the Bus" no boot. [CONCLUÍDO - 11/05/2026]
- [x] **Cartório Local (pkg/crypto/signer):** Persistência de chaves públicas PQC em `identities.json` para resiliência de reinício e validação histórica. [CONCLUÍDO - 11/05/2026]
- [x] **Sincronismo de Segurança (Sync-to-Consensus):** Blindagem do motor para atuar como Observador durante o sync, evitando forks por votos prematuros. [CONCLUÍDO - 28/05/2026]
- [ ] **Snap Sync:** Mecanismo de sincronização rápida (State Sync).

---

## 🏛️ Fase 4: Maturidade Industrial & Conformidade (Benchmarking Besu)
Baseado na análise de logs de produção do Besu, os seguintes itens devem ser integrados para alcançar o nível industrial:

#### 🏛️ Outros itens de Maturidade:
- [x] **Gestão de Metadados de Versão:** Persistir a versão do protocolo e do layout do banco de dados para evitar conflitos de identidade e facilitar migrações. [CONCLUÍDO - 28/05/2026]
- [ ] **Formal Verification:** Auditoria matemática dos caminhos críticos.

#### 🆕 Sprint 4.2: Ativação de Smart Contracts (Roadmap de Deploy)
Para transformar a Jamii em uma rede de estado programável:
- [x] **Fluxo de Criação (Contract Creation):**
    - [x] Habilitar detecção de `tx.To == nil` no `StateProcessor`.
    - [x] Implementar regra de derivação de endereço `Create(sender, nonce)`.
    - [x] Persistência de Bytecode e Storage na Verkle Tree. [CONCLUÍDO - 28/05/2026]
- [x] **Motor de Chamada (Contract Call):**
    - [x] Implementar o roteamento de transações para o interpretador da JamiiVM. [CONCLUÍDO - 03/06/2026]
    - [x] Adicionar campo `ContractAddress` nos Recibos (`Receipt`) para rastreabilidade de deploy. [CONCLUÍDO - 28/05/2026]
- [ ] **Integração de Pré-compilados:**
    - [ ] **Identity Bridge (Sovereign Address Bridge):** Conversão Nativa ultra-rápida ETH <-> Jamii Address (Bech32). Alocado no final do espaço de endereçamento (ex: `0x...FF`) para evitar conflitos com o Geth.
    - [ ] Validador PQC On-Chain: Verificação de assinaturas ML-DSA dentro da EVM.

---

### 🏗️ Fase 5: Saneamento Arquitetural & Desacoplamento (CONCLUÍDA)
Para resolver o alto grau de acoplamento detectado e eliminar os gargalos de CPU na finalização de blocos:
- [x] **Cálculo Incremental Homomórfico (Verkle Optimization):** Migrar da re-computação total O(N) para atualizações O(1) utilizando a propriedade homomórfica dos compromissos IPA (C' = C + delta * G_i). [CONCLUÍDO - 28/05/2026]
    - *Objetivo:* Reduzir o tempo de finalização de blocos de segundos para milissegundos, eliminando o gargalo de CPU na curva Bandersnatch.
- [x] **Configuração por Contexto:** Substituir a passagem de variáveis primitivas por objetos de contexto (`ChainConfig`). [CONCLUÍDO - 28/05/2026]
- [x] **Dependency Inversion (DI):** Refatorar os construtores de `Controller`, `Round` e `HeightManager` para dependerem de abstrações de configuração. [CONCLUÍDO - 28/05/2026]
- [x] **Redução de Boilerplate:** Unificar a orquestração de boot. [CONCLUÍDO - 28/05/2026]

### 🛠️ Fase 6: Ecossistema & Kit de Ferramentas (SDK) (EM CURSO)
Para facilitar a adoção e integração com a camada de Identidade Soberana:
- [x] **Jamii Java SDK (Core):** Implementação industrial completa em Java/Spring Boot. [CONCLUÍDO - 28/05/2026]
    - [x] Suporte total a Identidade Soberana Híbrida (Secp256k1 + ML-DSA-65).
    - [x] Motor de assinatura com *Strong Binding* compatível com o nó Go.
    - [x] Gestão de Keystores industriais (Criptografia AES-GCM com derivação SCrypt).
    - [x] Cliente JSON-RPC tipado para integração com dApps e sistemas legados.
- [x] **Identity Bridge (Sovereign Precompiled):** Implementação do contrato pré-compilado em Go no endereço `0xFF...FF` para conversão Bech32. [CONCLUÍDO - 05/06/2026]
- [x] **Sovereign Bridge (Reverse Conversion):** Implementação da decodificação Bech32 no endereço `0xFF...FE` para retorno ao Mirror (0x). [CONCLUÍDO - 05/06/2026]
- [x] **VM Gas Fix:** Correção da contabilidade de gás para chamadas nativas (Refund System). [CONCLUÍDO - 05/06/2026]
- [ ] **Jamii Dev Kit (Solidity):** Criar biblioteca de ferramentas oficiais (`Identity.sol`) para abstrair chamadas de baixo nível (`staticcalls`) aos pré-compilados da Jamii, garantindo segurança e tipagem forte para desenvolvedores de dApps.
- [ ] **Compiler Integration:** Integrar o Jamii Dev Kit ao compilador soberano para resolução automática de imports de sistema.
- [ ] **Jamii Go SDK:** Unificar as ferramentas de carteira e cliente RPC em um pacote SDK Go reutilizável.

---

## 🍃 Fase 7: Soberania Stateless (Nós Sem Disco) (PLANEJADA)
*Objetivo:* Permitir que dispositivos de baixa capacidade (Android, IoT) participem da validação sem armazenar a cadeia completa.

### 🧬 Verkle Witnesses & Stateless Execution
- [ ] **Protocolo de Testemunha (Execution Witness):** Implementar a geração de provas Verkle compactas durante a proposta de bloco.
- [ ] **Mensagem DTS Híbrida:** Criar o tipo `MSG_BLOCK_WITH_WITNESS` para propagação de blocos auto-contidos criptograficamente.
- [ ] **Validação Stateless:** Habilitar o `StateProcessor` a validar blocos usando apenas a `StateRoot` e a Witness, sem consultar o `StateDB` local.

### 🛡️ Guardian Nodes (Nós Guardiões)
- [ ] **Perfil de Hardware Android:** Otimizar o consumo de recursos para permitir que o nó rode em background em smartphones.
- [ ] **Consenso de Vigilância:** Implementar o papel de "Nó Guardião", que vigia a rede e emite alertas de fraude sem precisar de minerar ou armazenar TBs de dados.
- [ ] **SDK Java Native Integration:** Expandir o SDK Spring Boot para suportar a validação leve de cabeçalhos e provas de estado.

### ⏳ State Expiry (Expiração de Estado)
- [ ] **Gestão de Buffer Circular de Estado:** Implementar lógica para "esquecer" contas inativas e delegar a prova de histórico para nós de arquivo.
- [ ] **Incentivo de Arquivo:** Definir modelo econômico para nós que mantêm o histórico completo (Full Archive Nodes).

---

## ⚡ Fase 8: Sincronismo Híbrido e Throughput (PLANEJADA)
*Objetivo:* Escalar a rede para volumes massivos de dados sem comprometer a latência do consenso.

### 🆕 Sprint 8.1: Chaves Públicas de Validadores no Genesis (Resolução de Boot Offline)
- [ ] **Genesis PQC Keys**: Alterar a struct `Genesis` em `pkg/node/config.go` e a especificação do `genesis.json` para permitir a inclusão opcional das chaves públicas completas (ML-DSA) dos validadores do bloco inicial.
- [ ] **Bootstrap Key Seeding**: Atualizar o orquestrador do nó para pré-carregar e registrar essas chaves em memória no `IdentityRegistry` durante a inicialização (boot), permitindo a validação síncrona de blocos históricos sem dependência de handshakes DTS dinâmicos prévios.

### 🆕 Sprint 8.2: Política de Seeding Torrent Restrita (Archiver-Only Seeding)
- [ ] **Restricted Seed Mode**: Adicionar suporte a um modo puramente consumidor no `pkg/torrent/engine.go` (definindo `cfg.Seed = false` e silenciando loops de seeding se o nó for um validador/nó comum).
- [ ] **Validator Offloading**: Configurar nós validadores e comuns para atuarem apenas como baixadores no BitTorrent Swarm, limitando a atividade de *seeding* (geração de arquivos virtuais/reais e upload) estritamente aos nós de arquivo (*Archiver*), conservando CPU e largura de banda de I/O de disco nos nós de consenso.

### 🦴 Ultra-Compactação de Skeleton Blocks (Bi-Polar Short IDs)
Para minimizar a largura de banda durante o consenso sem perder a precisão:
- [x] **Bi-Polar Short IDs:** Em vez de transmitir hashes de 32 bytes das transações, o Proposer enviará um identificador de **6 bytes** composto pelos **3 primeiros e 3 últimos bytes** do hash original (ex: `[0:3] + [29:32]`). [CONCLUÍDO - 03/06/2026]
- [x] **Mitigação de Colisão:** O espaçamento bi-polar aumenta a entropia em relação a um truncamento sequencial. Caso (raro) ocorra colisão na MemPool do nó receptor, o nó utilizará o `TxRoot` do cabeçalho como prova matemática para rejeitar a montagem incorreta e solicitar a transação específica via P2P. [CONCLUÍDO - 03/06/2026]
- [ ] **Impacto:** Redução de 48 KB para **~9 KB** por bloco (para 1.500 TXs), melhorando a resiliência em redes de alta latência e reduzindo picos de I/O na placa de rede.

### 🛡️ Peer Scoring & Network Protection (Sybil Mitigation)
- [ ] **Peer Scoring System:** Implementar um motor de reputação que avalia a qualidade dos dados enviados por cada par (DTS/P2P).
- [ ] **Optimistic Punishment:** Nós (especialmente os Stateless) punem severamente vizinhos que propagam transações inválidas ou esqueletos de blocos malformados.
- [ ] **Persistent Blacklisting:** Criação de uma lista negra persistente no disco para evitar a reconexão de nós detectados como maliciosos ou spammers.
- [ ] **Impacto:** Proteção vital para a viabilidade de nós Stateless e para a saúde da MemPool global.

### ⏱️ Otimizações de Latência e Pipeline (High-Speed Throughput)
- [x] **Optimistic Block Pre-Assembly:** Pesquisar e implementar a capacidade de o próximo Proposer montar e pré-executar seu bloco em RAM enquanto o bloco atual finaliza seu commit IPA (Chained State Oracle). [CONCLUÍDO - 10/06/2026]
- [x] **Late-Stamping Header:** Mecanismo para injeção imediata de `ParentHash` e `StateRoot` em blocos pré-montados para reduzir o gap entre rodadas de consenso (Chained State Oracle). [CONCLUÍDO - 10/06/2026]
- [ ] **Debounce de Sync (Catch-up Delay):** Adicionar uma tolerância temporal (ex: 200ms) antes do disparo de catch-up pelo SyncManager, evitando concorrência redundante com o processo de finalização e commit de blocos pelo consenso local.
- [ ] **Impacto:** Redução de tráfego DTS espúrio e eliminação de conflitos de gravação no banco de dados entre threads de Sync e Consenso. Redução drástica do tempo de ociosidade da rede.

### 🏛️ Arquitetura de Plano Duplo (Dual-Plane Transport)
Após estudo técnico, a Jamii adotará uma estratégia híbrida para o transporte de dados:

1.  **Plano de Controle (DTS Custom):**
    *   **Foco:** Baixa latência.
    *   **Responsabilidade:** Votos de consenso (IBFT2), sinalização de rounds, compact blocks e transações individuais.
2.  **Plano de Dados (BitTorrent via `anacrolix/torrent`):** [CONCLUÍDO - 04/06/2026]
    *   **Foco:** Alto rendimento (Throughput).
    *   **Responsabilidade:** Download massivo de blocos históricos (State Sync), propagação de blocos cheios (Full Blocks) e distribuição de snapshots de estado.
    *   **Identificação Soberana (Peer ID):** A identificação de peers na rede torrent deve utilizar obrigatoriamente o **Endereço Soberano (Bech32, ex: `jamii1...`)** em vez do Mirror Address (Hex). Isso garante a consistência da identidade através de todos os planos de rede (Controle e Dados).
    *   **Arquitetura Soberana (Trusted Peers):** Diferente de clientes BitTorrent convencionais, a Jamii injeta automaticamente a topologia de validadores como `Trusted Peers` em cada torrent. Isso elimina a dependência de DHTs públicos ou Trackers, garantindo conectividade P2P direta, imediata e segura entre os nós da rede.
    *   **Estratégia Zero-Copy (Virtual Storage):** O motor BitTorrent não duplica dados no disco. Ele acessa o banco de dados (StateDB) através de um wrapper de "Arquivo Virtual", serializando pedaços da chain sob demanda. Isso garante que o uso de disco não dobre, mantendo a eficiência industrial.
    *   **Isolamento de Consenso:** O tráfego pesado de dados flui em paralelo ao DTS, garantindo que o download de blocos passados não gere latência (jitter) nas mensagens de voto em tempo real.
    *   **Benefício:** Utiliza o poder do *Swarm* (enxame) para baixar dados de múltiplos vizinhos simultaneamente, otimizando o uso de banda em redes distribuídas.

#### 🧪 Experiência de Validação de Malha (Mesh Test) [CONCLUÍDO - 04/06/2026]
Para validar a robustez da infraestrutura BitTorrent, foi implementado um laboratório isolado:
- **Payload:** Cada nó gera um arquivo de 100MB (`ID[:5].bin`).
- **Descoberta:** Anunciador HTTP em porta dedicada (`TorrentPort + 10000`).
- **Swarm Logic:** Logs em nível DEBUG comprovam a recepção de chunks de múltiplas fontes simultâneas.
- **Isolamento:** A rede de dados opera de forma totalmente independente do consenso IBFT.

### 🏗️ Integração e Ecossistema (JSON-RPC) (CONCLUÍDA)
*Objetivo:* Expor a inteligência da blockchain para o mundo exterior.
- [x] **Engine API:** Interface Consenso <-> Execução.
- [x] **JSON-RPC 2.0 (Basic):** Suporte a métodos de leitura (Saldo, Nonce, ChainID).
- [x] **JSON-RPC 2.0 (Transaction):** Suporte a `eth_sendRawTransaction`. [CONCLUÍDO - 28/05/2026]
- [ ] **Pub/Sub:** Notificações via WebSockets para eventos de contratos.

---

### 🚀 Fase 5: Estabilização e Mainnet (EM CURSO)
*Objetivo:* Auditorias, stress tests em larga escala e lançamento oficial.

#### ✅ Sprint 5.1: Mempool Industrial & Resource-Aware (Resiliência Anti-Flood) - [CONCLUÍDO - 03/06/2026]
Para garantir que a Jamii suporte fluxos massivos de transações PQC sem degradação de performance ou risco de OOM (Out of Memory):
- [x] **Parametrização via Genesis:** Adicionados os campos `MaxMempoolSlotSize` (10k) e `MaxMempoolMemorySize` (512MB) no `ChainConfig`.
- [x] **Lógica de Barreira Híbrida:** Implementada verificação de transbordamento baseada em slots e memória real ocupada em RAM.
- [x] **Check-First Validation:** Otimizado pipeline de entrada para rejeitar transações gigantes antes de realizar a verificação pesada de assinaturas quânticas.

#### 🏛️ Outros itens de Estabilização:
- [ ] **Formal Verification:** Auditoria matemática dos caminhos críticos.
- [ ] **Penetration Testing:** Simulação de ataques bizantinos.
- [ ] **Mainnet Genesis:** Materialização do bloco zero oficial.

---

## ⚡ Fase 9: Aceleração de Quórum por Witness (PRÓXIMA SPRINT)
*Objetivo:* Eliminar o gargalo de CPU/IPA na rede através da verificação assimétrica.

### 🚀 Sprint 9.1: Otimização do StateProcessor
- [ ] **Modo de Validação Stateless:** Adaptar o `pkg/core/processor.go` para aceitar uma `Witness` opcional durante a execução de blocos recebidos via P2P.
- [ ] **RAM-First Execution:** Se uma Witness estiver presente, o processador deve priorizar os dados da testemunha em vez de consultar o PebbleDB local, economizando ciclos de I/O.

### 🛡️ Sprint 9.2: Protocolo "Bala na Agulha" (Safety)
- [ ] **Deferred Commit:** Refatorar o `consensus/ibft/controller.go` para segurar o `Commit()` de disco até a finalização do quórum total de mensagens `COMMIT`.
- [ ] **Witness Injection:** Alterar o Proposer para gerar e anexar a prova Verkle/IPA (Witness) no momento do `PRE-PREPARE`.

### 🧪 Sprint 9.3: Witness para Storage e Otimização Binária
- [ ] **Storage Slot Tracking:** Atualizar o `StateDB` para incluir slots de storage na Witness gerada pelo Proposer.
- [ ] **Binary Serialization:** Substituir o formato JSON por uma codificação binária estrita (PPQ/SSZ) para minimizar o peso do Skeleton Block.
- [ ] **Suporte a Contratos Complexos:** Validar a aceleração de quórum em blocos que realizam múltiplas chamadas `SSTORE/SLOAD`.

### 🛡️ Sprint 9.4: Resiliência de Sincronismo e Watchdog do Consenso (CONCLUÍDA)
- [x] **Watchdog de Proposer Não-Pronto:** Redução da janela de timeout de 10s para 2s caso o proposer designado esteja offline ou não esteja pronto (`IsProposerReady() == false`).
- [x] **Filtros Estritos do Observer Mode:** Descarte imediato de votos de consenso (`MT_VALIDATOR_SEAL`) e novas transações de mempool (`MT_TRANSACTION`) durante a sincronização para economizar CPU e evitar saturação da mempool.
- [x] **Simetria de Rede do Archiver:** Implementação de callbacks de conexão e anúncio inicial de status no boot e reconexões do Archiver.
- [x] **Correção de Metadados do Torrent:** Impedir falhas por ponteiro nulo aguardando a resolução de metadados (`<-t.GotInfo()`) no download de chunks.
- [x] **Visualização de Rede Amigável:** Logs de sincronismo agora exibem nomes lógicos amigáveis (ex: `NODE_A`) em vez de IDs Bech32.

---

## 🛡️ Fase 10: Mitigação de Riscos de Consenso e Otimização de Estado (Futuras Sprints)
*Objetivo:* Endereçar as limitações e vulnerabilidades de segurança expostas pela análise crítica do modelo de especulação, do overhead de PQC e do gargalo de execução linear da EVM.

### 🆕 Sprint 10.1: Resiliência de Especulação em Round Changes (Anti-Nonce Drift)
- [ ] **Descarte Imediato de Estado Especulativo:** Implementar escuta reativa para eventos de `RoundChange` no `controller.go` para cancelar e limpar o cache de `speculativeBlock` no exato milissegundo de uma mudança de rodada.
- [ ] **Prevenção de Proposta Inválida:** Garantir que o Proposer nunca envie uma proposta baseada em estado especulativo defasado por mudança de round, forçando regeneração imediata em cima do estado estável do round atual.
- *Razão:* Evita propostas inválidas em cascata causadas por nonces/saldos inconsistentes quando a rede perde sincronia temporária.

### 🆕 Sprint 10.2: Mitigação de Storage Bloat e Sincronismo Stateless PQC
- [ ] **Soberania Stateless (Pruning de Assinaturas):** Pesquisar e implementar descarte de assinaturas PQC pesadas (~3.3KB por TX) de blocos antigos em nós validadores comuns após a finalização e checkpoint do estado.
- [ ] **Isolamento de Histórico:** Manter a guarda do histórico completo de assinaturas brutas exclusivamente nos nós Archiver (armazenadores de torrent), reduzindo o custo de I/O e espaço de disco de nós normais.
- *Razão:* Evita o crescimento insustentável do banco de dados (storage bloat) devido ao tamanho das assinaturas pós-quânticas.

### 🆕 Sprint 10.3: Blindagem Matemática de Witness (Anti-State Drift)
- [ ] **Equivalência Estrita de Sandbox:** Garantir que a validação otimista baseada em Proposer Witness na RAM execute sob regras matemáticas 100% idênticas ao motor de persistência em disco.
- [ ] **Tratamento de Divergência Falsa:** Substituir a pânico imediata do nó por uma rejeição graciosa e isolamento do proposer se a consolidação física divergir do resultado da Witness otimista.
- *Razão:* Protege a rede contra ataques DoS onde proposers maliciosos enviam witnesses falsas que passam na RAM mas travam/brickam todos os validadores honestos no commit de disco.

### 🆕 Sprint 10.4: Execução Paralela Concorrente na EVM (Multi-threaded VM)
- [ ] **Módulo de Paralelismo de Transações (Block-STM):** Desenvolver um classificador que identifique transações não conflitantes no bloco (que tocam em contas e armazenamento distintos).
- [ ] **Execução Concorrente na VM:** Executar transações sem conflitos em threads paralelas de EVM, removendo o gargalo de thread única antes de enviar o batch para a Trie (SMT/Verkle).
- *Razão:* Garante que o paralelismo de computação da Trie da Jamii traga ganho real de TPS mesmo para blocos com transações complexas de smart contracts.

---

## 🛠️ Checklist de Transição para Produção (Tuning & Safety)
Esta seção detalha os ajustes obrigatórios ao sair do ambiente de testes (2 nós) para a rede real:

1.  **Quórum e Segurança BFT ($3f + 1$):**
    *   **Mandato:** Mínimo de 4 validadores para tolerar 1 nó bizantino/falho.
    *   **Configuração:** Reversão de qualquer "flexibilização" de quórum feita em testes. A fórmula `(2n + 2) / 3` deve ser absoluta.

2.  **Calibração de Timeouts (`BaseTimeout`):**
    *   **Ajuste:** Avaliar a latência geográfica da rede. Recomenda-se aumentar `BaseTimeout` de 2s para 5s-10s em redes globais para reduzir `Round Changes` espúrios.

3.  **Governança de Limites de Bloco:**
    *   **Mandato:** O limite de transações por bloco é configurado estritamente via `MaxTxsPerBlock` na Gênese. Esta abordagem garante previsibilidade total do throughput e simplifica a orquestração do DTS.

4.  **Pruning e Disponibilidade de Dados:**
    *   **Retenção:** Aumentar a janela de `Prune` do consenso para manter as últimas 100~500 alturas, facilitando o sincronismo de nós com micro-quedas.

5.  **Política Anti-Spam da MemPool:**
    *   **MinPrice:** Calibrar o `MinPrice` e o `PriceBump` (RBF) conforme o valor econômico da rede para evitar ataques de inundação.

6.  **Gestão de Validadores (Cartório Dinâmico):**
    *   **Evolução:** Mover o Cartório de Identidades para dentro do **StateDB (On-Chain Identity)**. Chaves públicas PQC devem ser registradas na Verkle Tree para permitir a recuperação determinística do histórico sem dependência de cache local (`identities.json`).
    *   **Governança:** Migrar da lista estática no Gênesis para um mecanismo de votação On-Chain ou contrato de Staking.

---

## 🧭 Diretrizes de Evolução (Mandatos)

---

## 🔴 Production Blocking Debt (Pendências Críticas)
Estas tarefas são consideradas bloqueantes para o lançamento da Mainnet. Sem elas, a rede opera apenas em modo de "Federação Fechada".

1.  **On-Chain Validator Registry (Sovereign Governance):**
    *   **Status:** Pendente.
    *   **Definição:** Mover as chaves públicas PQC de validadores do arquivo local (`identities.json`) para a **Verkle Tree**.
    *   **Impacto:** Permite a governança descentralizada (votação para novos validadores) e rotatividade de chaves sem necessidade de Hard Forks.

2.  **Torrent Snap Sync (High-Speed Onboarding):**
    *   **Status:** Motor validado, Integração pendente.
    *   **Definição:** Utilizar o motor BitTorrent (Fase 8) para distribuir snapshots binários da Verkle Tree.
    *   **Impacto:** Permite que novos nós entrem na rede instantaneamente, baixando apenas o estado atual em vez de reprocessar milhões de blocos históricos.

3.  **Real-Time Event Streaming (WebSocket RPC):**
    *   **Status:** Pendente.
    *   **Definição:** Implementar suporte a WebSockets no módulo `pkg/rpc` para o método `eth_subscribe`.
    *   **Impacto:** Indispensável para carteiras (MetaMask) e dApps receberem notificações de eventos de contratos e confirmações de TX em tempo real.

4.  **Verkle Gas Repricing (EIP-4762 Compliance):**
    *   **Status:** Pendente.
    *   **Definição:** Ajustar a tabela de custos de gás (`GasCalculator`) para refletir o custo computacional real das provas Verkle/IPA, conforme a proposta oficial da Ethereum Foundation.
    *   **Impacto:** Protege a rede contra ataques de exaustão de CPU (DoS) que exploram a complexidade matemática da curva Bandersnatch em comparação aos hashes tradicionais.

5.  **Formal Verification (Core Audit):**

---

## 🧭 Diretrizes de Evolução (Mandatos)

1. **Besu/Geth como Norte:** Sempre consultar a lógica dessas implementações antes de propor mudanças arquiteturais.
2. **Desempenho PQC:** O custo computacional do ML-DSA não deve ser subestimado. O paralelismo é a regra.
3. **Imutabilidade de Finalidade:** O IBFT 2.0 garante finalidade imediata. O código deve impedir qualquer tentativa de reorg em nível de motor.
5. **Soberania de Logs:** A saúde do nó deve ser visível sem necessidade de ferramentas externas; o log industrial é a primeira linha de defesa.
6. **Manifesto Pétreo (Stony Config):** Parâmetros de rede definidos na gênese (ChainID, FreeGas, etc.) são imutáveis e protegidos pelo banco de dados; a tentativa de rodar um node com configurações divergentes do banco deve abortar a execução imediatamente.
7. **Sincronismo de Segurança (Sync-to-Consensus):** Um nó validador nunca deve enviar votos de consenso (Prepare/Commit) enquanto sua altura local estiver atrasada em relação à rede. O nó deve atuar como "Observador" até que o sincronismo total seja atingido, com exceção do nó de Bootstrap ou em modo Solo.

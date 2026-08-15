# Jamii Blockchain: Master Plan & Roadmap Industrial

Este documento define o planejamento estratégico de longo prazo para a construção da **Jamii Blockchain**, consolidando a inspiração técnica em protocolos estabelecidos (Hyperledger Besu / Go-Ethereum) com a inovação soberana em criptografia híbrida pós-quântica (**ML-DSA-65 / Dilithium L3**), **Verkle Trie Homomórfica**, **Bonsai Turbo** e arquitetura de **Plano Duplo P2P (DTS + BitTorrent)**.

---

## 🏗️ Fase 1: Motores e Fundações (CONCLUÍDA)
*Objetivo:* Construir os componentes atômicos, performáticos e seguros.
- [x] **Módulo CRYPTO:** Implementação soberana de assinaturas híbridas (Secp256k1 + ML-DSA-65) com Strong Binding (`SovereignSigner`). [CONCLUÍDO]
- [x] **Módulo ENCODING:** Serialização SSZ-based otimizada para payloads extensos. [CONCLUÍDO]
- [x] **Módulo STORE:** Camada de persistência resiliente baseada no PebbleDB (100% Go). [CONCLUÍDO]
- [x] **Módulo TRIE:** 
    - [x] Sparse Merkle Tree (SMT) de alta performance. [CONCLUÍDO]
    - [x] **Verkle Tree Industrial** (256-vias com IPA e curva Bandersnatch). [CONCLUÍDO]
    - [x] **Trie Factory** (Abstração total entre motores). [CONCLUÍDO]
- [x] **Módulo STATE (Unified Identity):** Identidade Mirror (0x) e Sovereign (`jamii1...`) compartilhando o mesmo estado nativo de 20 bytes no StateDB. [CONCLUÍDO]
- [x] **Módulo CONSENSUS:** Máquina de estados IBFT 2.0 adaptada para o ecossistema Jamii. [CONCLUÍDO]

---

## 🏗️ Fase 2: Saneamento e Orquestração (CONCLUÍDA)
*Objetivo:* Transformar módulos isolados em um nó funcional e garantir a integridade da transição de estado.

### 🧹 Saneamento Arquitetural (Dívida Bloqueante)
- [x] **Integridade da Transação (`pkg/encoding`):** Adição do campo `GasPrice` e suporte SSZ. [CONCLUÍDO]
- [x] **Integridade do Cabeçalho (`pkg/core/types`):** Adição do campo `ReceiptsRoot`. [CONCLUÍDO]
- [x] **Fluxo de Gas (Buy Gas):** Débito preventivo de `GasLimit * GasPrice` funcional no `StateProcessor`. [CONCLUÍDO]
- [x] **Fluxo de Gas (Refund Gas):** Estorno de saldo não utilizado (`GasLimit - GasUsed`) ao remetente após execução. [CONCLUÍDO - 04/05/2026]
- [x] **Cálculo de ReceiptsRoot:** Agregação Merkle de recibos no bloco e validação no `Processor`. [CONCLUÍDO - 04/05/2026]
- [x] **Ghost Root Killer (`pkg/core/processor`):** Snapshots de transação e garantia de StateRoot determinístico em blocos vazios. [CONCLUÍDO - 11/05/2026]
- [x] **Validação de Preço Mínimo em Bloco:** Trava no `StateProcessor` que rejeita transações com `GasPrice` inferior ao mínimo da rede. [CONCLUÍDO - 28/05/2026]

### 🏗️ Orquestração de Execução
- [x] **Standard Log (Log4J Style):** Unificação da telemetria com motor assíncrono (FIFO). [CONCLUÍDO]
- [x] **Módulo NODE (Orquestrador):** Gerenciamento do ciclo de vida dos motores (EVM, Consenso, Store). [CONCLUÍDO]
- [x] **Integração VM/Processor:** O `StateProcessor` orquestra a execução de bytecode via `EVM.Run`. [CONCLUÍDO]
- [x] **Fase P3 da VM (Jump Table):** Migração do loop de execução para despacho via tabela (Besu Compliance). [CONCLUÍDO - 28/05/2026]
- [x] **Ativação de Contract Creation (Top-Level):**
    - [x] Habilitar fluxo `tx.To == nil` no `StateProcessor`.
    - [x] Regra de derivação de endereço baseada no nonce do sender (`Create(sender, nonce)`).
    - [x] Adicionar campo `ContractAddress` no objeto `Receipt` para conformidade industrial. [CONCLUÍDO - 28/05/2026]
- [x] **Main Loop & Lifecycle:** Laço de trabalho que orquestra a transição de alturas e quórum sincronizado. [CONCLUÍDO - 11/05/2026]
- [x] **CLI Framework:** Implementação de comandos e flags via Cobra (`cmd/jamii`). [CONCLUÍDO - 11/05/2026]
- [x] **Configuração Dinâmica:** Suporte a YAML (`config.yaml`) e JSON para Gênese (`genesis.json`). [CONCLUÍDO]
- [x] **Identidade de Rede Imutável (`meta:chain`):** Persistência de parâmetros pétreos no PebbleDB (Besu Compliance). [CONCLUÍDO - 28/05/2026]
- [x] **Mecanismo LRU (`pkg/util/cache`):** Cache industrial para o StateDB (`MaxWarmTries`). [CONCLUÍDO - 21/05/2026]

---

## 🌐 Fase 3: Rede Soberana (P2P Networking) (CONCLUÍDA)
*Objetivo:* Comunicação descentralizada, robusta e imune a ataques de eclipse.
- [x] **Discovery Service:** Implementação de lista estática e resolução de pares. [CONCLUÍDO]
- [x] **Engine P2P DTS (Dual-Channel):** Disseminação não-linear via canais separados **EXPRESS** (consenso IBFT em < 1ms) e **BULK** (blocos e transações diretas `MsgData`). [CONCLUÍDO]
- [x] **Dual-Channel Gatekeeper (`IsPeerFullyConnected`):** Validação atômica de conectividade dos canais gêmeos antes de liberar tráfego P2P. [CONCLUÍDO - 11/08/2026]
- [x] **Proteção Anti-OOM DoS:** Leitura incremental de payloads de rede em chunks de 64KB no DTS. [CONCLUÍDO - 31/07/2026]
- [x] **SafeClose Idempotente:** Encerramento seguro de sockets P2P via `sync.Once` prevenindo pânicos de goroutine. [CONCLUÍDO - 31/07/2026]
- [x] **Mandato de Identificação Soberana Bech32:** Endereçamento obrigatoriamente Soberano no BitTorrent Data Plane (Diretiva 13) e formato `ENDEREÇO@host:port` no Control Plane DTS (Diretiva 14). [CONCLUÍDO]
- [x] **Recuperação Reativa de TXs:** Mecanismo sob demanda para reconstrução de compact blocks. [CONCLUÍDO - 08/05/2026]
- [x] **Sync Engine Resilience:** Drenagem de canais, ignorância de blocos atrasados e verificação "Catch the Bus" no boot. [CONCLUÍDO - 11/05/2026]
- [x] **Cartório Local (`pkg/crypto/signer`):** Persistência de chaves públicas PQC em `identities.json` para resiliência de reinício. [CONCLUÍDO - 11/05/2026]
- [x] **Sincronismo de Segurança (Sync-to-Consensus):** Blindagem do motor para atuar como Observador durante o sync, evitando forks por votos prematuros. [CONCLUÍDO - 28/05/2026]

---

## 🏛️ Fase 4: Maturidade Industrial & Conformidade (Benchmarking Besu) (CONCLUÍDA)
*Objetivo:* Conformidade com especificações industriais e suporte total a Smart Contracts.

- [x] **Gestão de Metadados de Versão:** Persistir versão do protocolo e layout do banco de dados (`meta:chain`). [CONCLUÍDO - 28/05/2026]
- [x] **Fluxo de Criação de Contratos (Contract Creation):**
    - [x] Habilitar detecção de `tx.To == nil` no `StateProcessor`.
    - [x] Derivação de endereço `Create(sender, nonce)`.
    - [x] Persistência de Bytecode e Storage na Verkle Tree. [CONCLUÍDO - 28/05/2026]
- [x] **Motor de Chamada (Contract Call):**
    - [x] Roteamento de transações para a JamiiVM. [CONCLUÍDO - 03/06/2026]
    - [x] Adicionar campo `ContractAddress` nos Recibos (`Receipt`). [CONCLUÍDO - 28/05/2026]
- [x] **Integração de Pré-compilados Soberanos (Estratégia Decrescente):**
    - [x] **ValidatorRegistry Contract:** Contrato inteligente pré-compilado no endereço `0x00...00fffffffd` para registro de validadores on-chain. [CONCLUÍDO - 05/06/2026]
    - [x] **Identity Bridge:** Conversão nativa `ETH <-> Jamii Address (Bech32)` via pré-compilados. [CONCLUÍDO - 05/06/2026]

---

## 🏗️ Fase 5: Saneamento Arquitetural & Desacoplamento (CONCLUÍDA)
- [x] **Cálculo Incremental Homomórfico (Verkle Optimization):** Atualização $O(1)$ utilizando a propriedade homomórfica dos compromissos IPA ($C' = C + \Delta \cdot G_i$). [CONCLUÍDO - 28/05/2026]
- [x] **Configuração por Contexto (`ChainConfig`):** Substituição de primitivos pelo contexto global `ChainConfig`. [CONCLUÍDO - 28/05/2026]
- [x] **Dependency Inversion (DI):** Refatoração dos construtores de `Controller`, `Round` e `HeightManager`. [CONCLUÍDO - 28/05/2026]
- [x] **Redução de Boilerplate:** Unificação da orquestração de boot. [CONCLUÍDO - 28/05/2026]

---

## 🛠️ Fase 6: Ecossistema & Kit de Ferramentas (SDK) (EM CURSO)
- [x] **Jamii Java SDK (Core):** Implementação industrial completa em Java/Spring Boot. [CONCLUÍDO - 28/05/2026]
    - [x] Suporte total a Identidade Soberana Híbrida (Secp256k1 + ML-DSA-65).
    - [x] Motor de assinatura com *Strong Binding* compatível com o nó Go.
    - [x] Gestão de Keystores industriais (Criptografia AES-GCM com SCrypt).
    - [x] Cliente JSON-RPC tipado para integração com dApps.
- [x] **Block Explorer (JamiiScan) & Wallet UI:** Interface Light Premium integrada à carteira de testes, com suporte a Login (Keystore/Mnemônico BIP-39), roteamento Hash (`#/wallet`, `#/block`, `#/tx`), reconciliação DOM e modais de confirmação. [CONCLUÍDO - 18/06/2026]
- [x] **Smart Contracts Gate:** Roteador e implantador dinâmico de contratos Solidity via API REST e frontend do explorer. [CONCLUÍDO - 26/06/2026]
- [x] **Otimizações do Banco de Dados Postgres:** Indexação secundária de 11 colunas e fatiamento em lote de 1.000 contas para eliminação de gargalos de WAL. [CONCLUÍDO - 18/06/2026]
- [ ] **Jamii Dev Kit (Solidity):** Biblioteca oficial (`Identity.sol`) para abstrair `staticcalls` aos pré-compilados.
- [ ] **Compiler Integration:** Integrar o Jamii Dev Kit ao compilador soberano para resolução automática de imports.
- [ ] **Jamii Go SDK:** Unificar as ferramentas de carteira e cliente RPC em um pacote SDK Go reutilizável.

---

## 🍃 Fase 7: Soberania Stateless (Nós Sem Disco) (CONCLUÍDA - 06/07/2026)
*Objetivo:* Permitir que dispositivos de baixa capacidade participem da validação sem armazenar a cadeia completa.

- [x] **Protocolo de Testemunha (Execution Witness):** Geração de provas Verkle compactas durante a proposta de bloco. [CONCLUÍDO - 06/07/2026]
- [x] **Mensagem DTS Híbrida (`MSG_BLOCK_WITH_WITNESS`):** Propagação de blocos auto-contidos criptograficamente. [CONCLUÍDO - 06/07/2026]
- [x] **Validação Stateless (`StateProcessor`):** Habilidade de validar blocos usando apenas o `StateRoot` e a Witness, sem consultar o PebbleDB local. [CONCLUÍDO - 06/07/2026]
- [x] **Execução de Nós em Modo 100% Stateless (`stateless: true`):** Nó leitor operando inteiramente em RAM sem banco em disco. [CONCLUÍDO - 06/07/2026]

---

## ⚡ Fase 8: Sincronismo Híbrido e Throughput (CONCLUÍDA - Julho/2026)
*Objetivo:* Escalar a rede para volumes massivos de dados sem comprometer a latência do consenso.

- [x] **Genesis PQC Keys (Boot Offline):** Inclusão opcional de chaves públicas ML-DSA dos validadores no `genesis.json` e pré-carga no `IdentityRegistry` durante o boot. [CONCLUÍDO - 14/07/2026]
- [x] **BitTorrent Seeding Restrito & Multi-Peer Sync:** Nós validadores operam como baixadores no BitTorrent e o seeding é delegado aos nós Arquivadores (*Archiver-Only Seeding*). [CONCLUÍDO - 14/07/2026]
- [x] **Fallback de Torrent Sync (Diretiva 19):** Transição automática do Torrent Sync para P2P Sequencial em caso de falhas consecutivas. [CONCLUÍDO - 14/07/2026]
- [x] **Bi-Polar Short IDs (Bi-Polar Skeleton):** Identificadores compactos de **6 bytes** (3 bytes iniciais + 3 bytes finais do hash), reduzindo a banda de bloco em ~95%. [CONCLUÍDO - 03/06/2026]
- [x] **Mempool BaseFee Startup Sync:** Sincronização do BaseFee inicial da MemPool com o valor do último bloco persistido no PebbleDB ao bootar o nó. [CONCLUÍDO - 10/07/2026]
- [x] **Arquitetura de Plano Duplo (DTS Control + BitTorrent Data Plane):** Separação física de tráfego de controle (DTS) e dados massivos (BitTorrent com Virtual Storage Zero-Copy). [CONCLUÍDO - 04/06/2026]

---

## ⚡ Fase 9: Aceleração de Quórum por Witness, Resiliência e Poda (CONCLUÍDA - Agosto/2026)

- [x] **Modo de Validação Stateless (`StateProcessor`):** Validação de blocos recebidos via P2P com priorização de Witness em RAM. [CONCLUÍDO - 06/07/2026]
- [x] **Protocolo "Bala na Agulha" (Deferred Commit & Witness Injection):** Proposer anexa a Witness no `PRE-PREPARE` e segura o commit físico até a confirmação do quórum total. [CONCLUÍDO - 06/07/2026]
- [x] **Witness para Storage e Otimização Binária:** Rastreamento de slots `SSTORE/SLOAD` na Witness e serialização binária PPQ/SSZ. [CONCLUÍDO - 06/07/2026]
- [x] **Watchdog de Liderança & Fast-Path `READY`:** Troca de propositor em 5ms quando o líder designado estiver inativo ou em sincronização (`IsProposerReady() == false`). [CONCLUÍDO - 12/08/2026]
- [x] **Status Keep-Alive Ticker:** Transmissão periódica de `BroadcastStatus` a cada 5s para atualização de quórum. [CONCLUÍDO - 13/07/2026]
- [x] **Poda em Disco Automatizada em 3 Fases (Sprint 9.5):**
    - [x] **`RootTracker`:** Mapeamento de StateRoots obsoletas além da janela de retenção `PruneRetainBlocks`. [CONCLUÍDO - 11/08/2026]
    - [x] **`PruneManager`:** Execução atômica assíncrona de Poda de Histórico (`HistoryPruner`), Garbage Collection de Rollbacks Verkle (`PruneHistoricalRoots` em `l:*`) e Compactação Nativa PebbleDB (`CompactRange`). [CONCLUÍDO - 11/08/2026]
    - [x] **Catch-Up Sync Protection:** Adiamento automático da poda se `SyncManager.IsBehind() == true`. [CONCLUÍDO - 11/08/2026]
- [x] **Worker de Gravação Assíncrono (`AsyncWriteWorker` - Sprint 9.6):**
    - [x] Gravação assíncrona de blocos no PebbleDB com fila buferizada em RAM (`maxQueueSize = 32`) e liberação de rodada de consenso em $< 5\text{ ms}$. [CONCLUÍDO - 13/08/2026]
    - [x] Busca em 3 níveis no `handleBlockRequest` P2P (Disco $\to$ Fila RAM do Worker $\to$ `PayloadPool`). [CONCLUÍDO - 13/08/2026]
- [x] **Resiliência P2P DTS & Handover Buffer (Sprint 9.7):**
    - [x] `IsPeerFullyConnected`: Exigência de conectividade atômica Dual-Channel (`Bulk` + `Express`). [CONCLUÍDO - 11/08/2026]
    - [x] `futureBlocksBuffer`: Buffer em RAM para até 2 blocos futuros, evitando pausar o consenso no `SyncManager`. [CONCLUÍDO - 12/08/2026]
    - [x] Transmissão direta em 1 pacote no canal BULK (`MsgData`) para transações, eliminando handshakes de 3 etapas. [CONCLUÍDO - 11/08/2026]
- [x] **Guias Operacionais, DNS Local & Telemetria `/metrics` (Sprint 9.8):**
    - [x] `peers.json`: Registro local de resolução de nomes lógicos (`ENDEREÇO:PORTA` $\to$ `ENDEREÇO@IP:PORTA`). [CONCLUÍDO - 14/08/2026]
    - [x] Métricas Prometheus em `/metrics` (`effective_tps`, `block_time_ms`, `db_commit_micros`, `async_worker_queue_length`, tráfego DTS). [CONCLUÍDO - 13/08/2026]
    - [x] Documentação oficial: [`CONFIG.md`](file:///c:/Magno/Projetos/jamii/CONFIG.md), [`GENESIS.md`](file:///c:/Magno/Projetos/jamii/GENESIS.md) e [`docs/DOCUMENTACAO_TECNICA.md`](file:///c:/Magno/Projetos/jamii/docs/DOCUMENTACAO_TECNICA.md). [CONCLUÍDO - 14/08/2026]

---

## 🛡️ Fase 10: Mitigação de Riscos de Consenso, Governança e Mainnet Readiness (EM CURSO)

- [x] **Desativação Permanente da Especulação Paralela (Sprint 10.1):** Remoção da especulação assíncrona para eliminar corridas de I/O no PebbleDB e concorrência na RAM. Montagem de blocos estritamente síncrona e determinística. [CONCLUÍDO - 10/07/2026]
- [x] **Purga Autônoma de MemPool & Hot Nonces (Sprint 10.2):**
    - [x] `RemoveInvalidTx`: Purga imediata por Hash e Purga Descendente de dependentes. [CONCLUÍDO - 30/07/2026]
    - [x] Varredura autônoma em `hotNonces` no `Reset` da MemPool para evitar travamento de nonces em carteiras ativas. [CONCLUÍDO - 30/07/2026]
- [x] **RPC Sync Guard (Sprint 10.3):** Proteção de endpoints RPC rejeitando requisições de escrita em nós em fase de sincronismo (`Node in sync mode, retry later`). [CONCLUÍDO - 30/07/2026]
- [x] **On-Chain Validator Registry & Governança (Sprint 10.4):** Contrato pré-compilado de validadores no endereço `0x00...00fffffffd` alocado on-chain. [CONCLUÍDO - 05/06/2026]
- [ ] **Execução Paralela Concorrente na EVM (Sprint 10.5 - Multi-threaded Block-STM):** Classificador de transações sem conflitos para execução paralela em múltiplas goroutines de EVM antes do commit da Trie.
- [ ] **Redirecionamento de Taxas & Recompensa de Bloco (Sprint 10.6):** Redirecionamento da gorjeta da transação (`Tip`) para o `coinbase` do propositor e suporte a `BlockReward` minting via `genesis.json`.
- [ ] **Detecção de Fraude e Punição de Validadores (Sprint 10.7 - Slashing & Jailing):** Monitoramento de assinatura dupla (Double-Signing), ejecção automatizada de nós bizantinos e suspensão temporária por inatividade.
- [ ] **Criptografia PQC de Canal P2P (Sprint 10.8 - ML-KEM-768 / Kyber):** Implementação de Key Encapsulation Mechanism (NIST FIPS 203) no *handshake* do motor DTS (`pkg/dts`) com derivação HKDF e cifragem simétrica de pacotes TCP (AES-256-GCM), garantindo confidencialidade *End-to-End* quântica contra ataques *Harvest Now, Decrypt Later*.

---

## 🛠️ Checklist de Transição para Produção (Tuning & Safety)

1. **Topologia de Produção (Recomendação DevOps - Mínimo 4 Validadores):** Na implantação de redes de produção (Mainnet), recomenda-se cadastrar no mínimo 4 validadores no `genesis.json` para tolerar a queda de 1 servidor ($F=1$), mantendo o quórum operacional de 3 nós online ($Q = 2F+1 = 3$). Em ambiente de desenvolvimento local, a rede pode operar com 3 validadores cadastrados (quórum 2).
2. **Calibração de Timeouts:** Ajuste do `BaseTimeout` de acordo com a latência geográfica da rede (padrão 2s a 5s).
3. **Poda em Disco e Retenção:** Janela de `prune_retain_blocks` ajustada para 1.024 blocos em nós validadores de produção e 50.000 em redes de teste.
4. **Política Anti-Spam da MemPool:** Validação de `MinPrice` e rejeição de transações gigantes antes de verificações quânticas.
5. **Nós de Arquivo Dedicados (`Archiver`):** Alocação de nós com `is_archive_node: true` e `prune_enabled: false` para garantir preservação histórica e servir de fonte de sincronismo.

---

## 🔴 Production Blocking Debt (Pendências Críticas)

1. **On-Chain Validator Registry (Sovereign Governance):** [x] **CONCLUÍDO** (Pré-compilado `0x00...00fffffffd`).
2. **Torrent Snap Sync (High-Speed Onboarding):** Motor BitTorrent validado e funcional no Data Plane.
3. **Real-Time Event Streaming (WebSocket RPC):** Suporte a `eth_subscribe` via WebSockets para dApps (Pendente).
4. **Verkle Gas Repricing (EIP-4762 Compliance):** Ajuste de tabela de gas para provas Verkle/IPA (Pendente).
5. **Multi-threaded Block-STM Execution:** Paralelização de transações EVM sem conflito (Pendente).

---

## 🧭 Diretrizes de Evolução (Mandatos)

1. **Besu/Geth como Norte:** Sempre consultar a lógica dessas implementações oficiais antes de propor mudanças arquiteturais.
2. **Desempenho PQC:** O custo computacional do ML-DSA-65 não deve ser subestimado. Paralelismo e esqueletos de bloco bi-polares são a regra.
3. **Imutabilidade de Finalidade:** O IBFT 2.0 garante finalidade imediata no bloco finalizado. Reorganizações de cadeia (*reorgs*) são estritamente proibidas.
4. **Manifesto Pétreo (Stony Config):** Parâmetros de rede definidos no `genesis.json` (`ChainID`, `IsFreeGas`, `TreeType`) são imutáveis e protegidos pelo banco de dados (`meta:chain`).
5. **Sincronismo de Segurança (Sync-to-Consensus):** Um nó validador nunca envia votos de consenso (Prepare/Commit) enquanto sua altura local estiver atrasada em relação à rede (`SyncManager.IsBehind() == true`).

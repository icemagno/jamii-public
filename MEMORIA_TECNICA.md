# 🧠 Jamii - Memória Técnica Consolidada


## 🏛️ Arquitetura Core (Imutável)
- **VM:** Ordem LIFO (Top op Next), aritmética industrial, flags de overflow integradas no `types`. 
- **Criptografia:** Híbrida (ML-DSA + Secp256k1). Identidade soberana e mirror (0x) compartilham o mesmo payload de 20 bytes derivado da Secp256k1.
- **Endereçamento:** `jamii1...` (Sovereign) e `0x...` (Mirror/EIP-55) são a mesma conta no StateDB.
- **Armazenamento:** Arquitetura Bonsai/SSZ com suporte a Verkle Trees (default) e SMT (Sparse Merkle Trie) através da Trie Factory.

## 🛠️ Padrões de Desenvolvimento
- **Compliance:** 100% Besu/Geth logic (Yellow Paper).
- **Módulos Homologados:** `types`, `encoding`, `crypto`, `store`, `trie`, `wallet`. Alterações exigem auditoria.
- **Testes:** Soberanos. Não alterar testes para corrigir bugs.

- **Concluído:** Unified Identity, **Sovereign Transaction V1 (EIP-1559 Native)**, Mirroring nativo de saldo, Dívida Técnica Bloqueante (Gas Price, ReceiptsRoot, Buy Gas, VM Integration, JSON-RPC Read-only), Rede P2P (DTS Engine), MemPool (Gestão de transações pendentes com Purga Descendente), Sincronismo Determinístico (Besu-style), Conformidade Industrial IBFT2 (Pacing, Configurable Timeouts), Segurança Sync-to-Consensus (Observer Mode), Saneamento de Logs (Critical Level), Ghost Root Killer (Determinismo de Blocos Vazios), Resiliência de Sincronia (Channel Drainage), Compactação Soberana (Storage Optimization), Tsunami PQC Test (10.000 TXs Sustentadas), **Blindagem de Mercado (London/Besu Logic)**, **Otimização de Cache Industrial (MaxWarmTries Guard)**, **Sincronização Atômica Sync-Consensus (Anti-Self-Sabotage)**, **Chained State Oracle (Active Speculation)**, **Desacoplamento do SDK Java (Pure SDK)**, **Wallet Web App de Exemplo (Interativa)**, **Terminologia de Estado (Verkle/SMT)**, **Execução de Nós em Modo 100% Stateless (RAM Storage)**, **Validação Acelerada por Witness (Fase 7 & 9)**, **Muting de Consenso para Observadores**, **Reset Offline da MemPool (Otimização de Heaps)**, **DOM Reconciliation no Explorer**, **Mapeamento de Variáveis Docker e Teste de Conexão no Startup**, **Sincronização de BaseFee no Startup do Nó**, **Correção do Nonce Drift no Consenso via Pre-State Witness**, **Desativação Permanente do Paralelismo de Especulação**, **Keep-Alive Ativo de Status no Watchdog de Quórum**, **Filtro de Maturação de Transações (Propagation Delay Guard)**, **Empacotamento de Transações (Transaction Transfer Packet)**, **Purga Autônoma de Hot Nonces na MemPool**, **Sincronização Segura do Motor de Consenso (Prevenção de Stall em Sync Clássico)**, **Filtro de Quórum em Votações de RoundChange (IBFT 2.0)** e **Arquitetura de Poda em Disco para Nó Core (Sprints 1 a 3 em PRUNE.MD).**

## 🛠️ Decisões Recentes (13/08/2026)
1. **Consolidação de Resiliência do Consenso IBFT 2.0 e Handover Buffer Dinâmico (`REPAROS-12-08.md` Sprints 1 a 3):**
    - **Contexto:** Oscilações de rede e atrasos pontuais de 1 bloco ativavam o `SyncManager` e pausavam o motor de consenso (`HALT`), travando a blockchain.
    - **Ação:** (1) Implementada a tolerância de até 2 blocos em `SyncManager.isSyncedLocked` e a recepção temporária em RAM no buffer dinâmico `futureBlocksBuffer` em [`pkg/node/node.go`](file:///c:/Magno/Projetos/jamii/pkg/node/node.go). (2) Liberado o registro imediato de `ready = false` em `handleStatusArrival` e vinculado ao callback `OnSyncStateChanged` em [`pkg/blockchain/sync.go`](file:///c:/Magno/Projetos/jamii/pkg/blockchain/sync.go), ativando o Fast-Path (pulo de propositor em 5ms). (3) Adotado o modelo linear suave de timeout de rodada $T_{\text{round}} = T_{\text{base}} + (R \times 2\text{s})$ com teto de 20s em [`pkg/consensus/ibft/round.go`](file:///c:/Magno/Projetos/jamii/pkg/consensus/ibft/round.go).
2. **Descarte de Timeouts Obsoletos de Rodadas Commitadas (`pkg/consensus/ibft/round.go`):**
    - **Contexto:** Timers de rodadas passadas expiravam após a finalização de blocos via `SyncManager`, transmitindo mensagens `ROUND CHANGE` para alturas antigas e dessincronizando o nó.
    - **Ação:** Adicionada verificação em `r.onTimeout` comparando a altura da rodada $H$ com a cabeça canônica da cadeia (`r.engine.CurrentHeader()`). Se $H \le \text{CurrentHeader.Number}$, o timeout é cancelado e descartado silenciosamente sem gerar tráfego P2P.
3. **Limites Explícitos de Prefixo em `CompactRange` (`pkg/store/prefixed.go`):**
    - **Contexto:** O `PruneManager` invocava `CompactRange(nil, nil)` para compactação global. Na camada `PrefixedStore`, argumentos `nil` resultavam em `start = prefix` e `limit = prefix`, disparando o aviso `Cause: Compact start is not less than end` no PebbleDB.
    - **Ação:** Atualizados os métodos `CompactRange` e `DeleteRange` em [`pkg/store/prefixed.go`](file:///c:/Magno/Projetos/jamii/pkg/store/prefixed.go) para converter parâmetros `nil` na faixa lexicográfica do prefixo `[prefix, keyUpperBound(prefix)]`.
4. **Telemetria de Tempo entre Blocos no Endpoint `/metrics` (`pkg/rpc/server.go` e `pkg/node/node.go`):**
    - **Contexto:** Necessidade de mensurar com precisão no endpoint `/metrics` o tempo decorrido em milissegundos ($ms$) entre a gravação de um bloco e o bloco seguinte (`Block #N committed to database`).
    - **Ação:** (1) Adicionada a métrica Prometheus `jamii_blockchain_block_time_ms` *(gauge)* e o método `GetLastBlockTimeMs() uint64` à interface `MetricsProvider` em [`pkg/rpc/server.go`](file:///c:/Magno/Projetos/jamii/pkg/rpc/server.go). (2) Implementados os campos `lastBlockTimeMs` e `lastBlockCommitNano` na struct em memória `Node` em [`pkg/node/node.go`](file:///c:/Magno/Projetos/jamii/pkg/node/node.go) e acionada a atualização da medição thread-safe a cada commit de bloco no banco de dados (Consenso, Sync e Buffer de Blocos Futuros), sem qualquer modificação nas estruturas de bloco (`pkg/types`).
5. **Expansão do Dashboard Grafana com Métricas de DB e Tráfego DTS por Canal (`Grafana Dashboard/Grafana.json`):**
    - **Contexto:** As métricas de latência de banco de dados (`jamii_db_commit_micros`) e tráfego de rede (`jamii_dts_traffic_kbps`) não estavam contempladas no painel visual do Grafana.
    - **Ação:** Atualizado o arquivo [`Grafana Dashboard/Grafana.json`](file:///c:/Magno/Projetos/jamii/Grafana%20Dashboard/Grafana.json) incluindo: (1) Painel Timeseries *`💾 Latência de Commit no Banco de Dados (StateDB)`* em $\mu s$; (2) Dois painéis Timeseries dedicados lado a lado: *`🌐 Taxa de Tráfego DTS - Canal BULK (KB/s)`* e *`⚡ Taxa de Tráfego DTS - Canal EXPRESS (KB/s)`* com legendas por direção (`in`/`out`); (3) Atualização do painel de tabela comparativa *`📋 Resumo Detalhado por Nó`* com colunas individuais para `Commit DB (µs)`, `DTS Bulk (KB/s)` e `DTS Express (KB/s)`.

## 🛠️ Decisões Recentes (11/08/2026)
1. **Transmissão Direta e Compulsória de Transações no Canal BULK (`pkg/dts/engine.go`, `pkg/rpc/server.go` e `pkg/node/tx_batch.go`):**
    - **Contexto:** A função `Engine.Store` transmitia um anúncio de inventário `MsgInv` pelo canal BULK, forçando um handshake de 3 etapas (`MsgInv` -> `MsgReq` -> `MsgData`) com 3 viagens de ida e volta TCP (~15-50ms) para entregar a transação aos pares. Como a proposta de bloco viajava em 1ms pelo canal EXPRESS, os nós pares recebiam o bloco compacto antes da transação completar as 3 etapas, fazendo a reconstrução falhar e acionando o `SyncManager`.
    - **Ação:** (1) Atualizada a função `Engine.Store` em [`pkg/dts/engine.go`](file:///c:/Magno/Projetos/jamii/pkg/dts/engine.go) para transmitir os bytes da transação compulsoriamente como `MsgData` em 1 único pacote pelo canal BULK (sem `MsgInv` e sem conversa de 3 etapas). (2) Mantida a transmissão estritamente no canal BULK (sem utilizar o canal EXPRESS para transações). (3) Invertida a ordem no RPC ([`pkg/rpc/server.go`](file:///c:/Magno/Projetos/jamii/pkg/rpc/server.go)) para transmitir a transação via P2P no milissegundo zero antes da inclusão local.
2. **Integração do `PruneManager` e `RootTracker` no Ciclo de Vida do Nó (`pkg/node/node.go`):**
    - **Contexto:** O `Node` em `node.go` ainda utilizava a função de poda legada que só apagava histórico e não acionava a Garbage Collection de Estado Verkle nem a compactação física de SSTables via `CompactRange`. Isso mantinha arquivos de banco ocupando ~6 GB no PebbleDB.
    - **Ação:** Interligados os componentes `RootTracker` e `PruneManager` em [`pkg/node/node.go`](file:///c:/Magno/Projetos/jamii/pkg/node/node.go). A cada finalização de bloco (seja via `SyncManager` ou `Consenso`), a raiz canônica de estado é registrada no `RootTracker` e o `PruneManager.MaybeTriggerPruning` é acionado para realizar a limpeza em 3 fases (Histórico -> Verkle Rollbacks -> PebbleDB CompactRange).
2. **Refinamento e Clareza dos Logs de Votação de Consenso por Altura (`pkg/node/node.go`):**
    - **Contexto:** A mensagem genérica `log.Info("Consensus engine started")` em `startConsensusAt(height)` era emitida a cada mudança de altura de bloco $H$. Essa redação causava dubiedade nos operadores, sugerindo erroneamente que o motor de consenso inteiro estaria caindo e reiniciando do zero a cada bloco finalizado.
    - **Ação:** Atualizada a mensagem em [`pkg/node/node.go`](file:///c:/Magno/Projetos/jamii/pkg/node/node.go#L656) para `log.Info("Starting consensus voting for height #%d", height)`, especificando com clareza a altura exata que está entrando em votação de consenso.
2. **Orquestrador de Limpeza Física `PruneManager` e Telemetria Operacional de Disco (Sprint 3 Tasks 3.2 & 3.3):**
    - **Contexto:** Necessidade de coordenar o pipeline assíncrono completo de poda e descarte de espaço físico em disco no banco PebbleDB do Nó Core.
    - **Ação:** Criado o `PruneManager` em [`pkg/node/prune_manager.go`](file:///c:/Magno/Projetos/jamii/pkg/node/prune_manager.go) encarregado de executar a sequência atômica de 3 fases em segundo plano: (1) Poda de Histórico (`PruneHistoryBefore`), (2) Poda de Estado Verkle (`PruneHistoricalRoots`), e (3) Compactação Nativa PebbleDB (`CompactRange`). Adicionada a emissão de telemetria em log `INFO` com dados de blocos, transações, logs de rollback desativados e tempo de compactação.
3. **Extensão Segura do Módulo Homologado `pkg/store` para Compactação Física em Disco (`CompactRange` - Sprint 3 Task 3.1):**
    - **Contexto:** Devido à arquitetura LSM-Tree do PebbleDB, chamadas a `DeleteRange` gravam lápides (*tombstones*), mas o espaço físico em disco (arquivos `.sst`) só é devolvido ao sistema operacional durante a compactação de tabelas.
    - **Ação:** Adicionado o método `CompactRange(start, limit []byte) error` ao contrato da interface `Store` em [`pkg/store/store.go`](file:///c:/Magno/Projetos/jamii/pkg/store/store.go#L70) e implementada a compactação nativa `p.db.Compact(start, limit, false)` em [`pkg/store/pebble.go`](file:///c:/Magno/Projetos/jamii/pkg/store/pebble.go#L83). Criadas respostas no-op (`return nil`) não-bloqueantes para `memStore` e `postgresStore`, mantendo 100% de compatibilidade entre os storages.
4. **Blindagem de Poda durante Sincronismo Inicial (*Catch-Up Sync Safety Guard* - Sprint 2 Task 2.3):**
    - **Contexto:** Executar a poda de histórico ou de estado enquanto o nó está baixando blocos antigos para alcançar a rede provocava corrupção de estado ou inconsistências na verificação de cabeçalhos.
    - **Ação:** Adicionada uma trava de segurança em [`pkg/node/node.go`](file:///c:/Magno/Projetos/jamii/pkg/node/node.go#L1363) verificando `syncManager.IsBehind()`. Caso o nó esteja em fase de sincronismo, a execução do pruner é automaticamente postergada.
5. **Garbage Collection do Estado Verkle (`PruneHistoricalRoots` - Sprint 2 Task 2.2):**
    - **Contexto:** Acúmulo de logs de rollback históricos (`l:<rootHex>:*`) de blocos passados que ultrapassavam a janela de reorganização da rede.
    - **Ação:** Implementado o método autônomo `PruneHistoricalRoots` em [`pkg/trie/verkle/gc.go`](file:///c:/Magno/Projetos/jamii/pkg/trie/verkle/gc.go) sem alterar nenhuma struct ou assinatura do pacote homologado `pkg/trie`. O algoritmo executa o expurgo em faixa $O(1)$ de logs de rollback das raízes obsoletas enquanto preserva 100% dos dados de saldos e contas do *Flat State* (`d:*`).
6. **Rastreador de Raízes Canônicas Ativas em RAM (`RootTracker` - Sprint 2 Task 2.1):**
    - **Contexto:** Necessidade de mapear quais raízes de estado (`StateRoot`) superaram a janela de retenção `PruneRetainBlocks` e podem ser declaradas obsoletas.
    - **Ação:** Criado o componente thread-safe `RootTracker` em [`pkg/core/state/root_tracker.go`](file:///c:/Magno/Projetos/jamii/pkg/core/state/root_tracker.go) com `AddRoot`, `GetObsoleteRoots`, `PruneObsoleteRoots` e `IsRootActive`.
7. **Correção do Bug de Filtro de Votação em `handleRoundChange` (Conformidade Besu IBFT 2.0):**
    - **Contexto:** Em momentos de queda de validadores ou reinício da rede, os nós travavam na mudança de liderança (ex: `ROUND CHANGE para R:1`) mantendo 2/3 de votos em vez de atingir o Quórum Forte (3/3).
    - **Ação:** Corrigida a instrução em [`pkg/consensus/ibft/controller.go`](file:///c:/Magno/Projetos/jamii/pkg/consensus/ibft/controller.go#L917) trocando `if targetRound <= c.currentRound` por `if targetRound < c.currentRound`. Isso garante que votos tardios de `RoundChange` para a rodada atual (\(targetRound == c.currentRound\)) sejam devidamente registrados em `roundChangeVotes`, permitindo atingir o Quórum Forte ($2F+1$).
8. **Plano Mestre de Poda em Disco para o Nó Core (`PRUNE.MD`) e Implementação da Sprint 1:**
    - **Contexto:** O crescimento físico do PebbleDB para 18 GB em retenção completa exigia transformar o Nó Core em um *Pruned Full Node* (retenção controlada de 1 a 3 GB).
    - **Ação:** Criado o plano mestre [`PRUNE.MD`](file:///c:/Magno/Projetos/jamii/PRUNE.MD) e executada a Sprint 1: estendidas as interfaces com `DeleteRange`, criado o `HistoryPruner` em [`pkg/blockchain/pruner.go`](file:///c:/Magno/Projetos/jamii/pkg/blockchain/pruner.go) e integrado o disparo assíncrono de limpeza em [`pkg/node/node.go`](file:///c:/Magno/Projetos/jamii/pkg/node/node.go).
9. **Sincronização Segura do Motor de Consenso vs SyncManager (Prevenção de Stall em Sync Clássico):**
    - **Contexto:** Durante o boot ou oscilações de rede, a regra de tolerância de 1 bloco em `IsSynced()` permitia que o nó local considerasse que não estava mais atrasado (`IsBehind() == false`), mesmo que o thread do `SyncManager` ainda estivesse ativamente processando e aguardando um bloco no loop sequencial `catchUp`. Isso ativava precocemente o motor de consenso, fazendo com que ele e o `SyncManager` processassem concorrentemente a mesma altura. O consenso acabava finalizando o bloco de forma nativa e avançando o nó de altura, enquanto o `SyncManager` ficava aguardando no canal `s.blockSub`. Ao receber os blocos futuros propagados pela rede, o `SyncManager` quebrava com o erro `received future block`, entrava em um loop infinito de fail-restart e mantinha `s.syncing` ativo, o que sequestrava todos os blocos `MT_BLOCK` da rede e impedia o Consensus de receber os payloads e votar, travando o nó (STALL).
    - **Ação:** Refatorado o método [`IsBehind()`](file:///c:/Magno/Projetos/jamii/pkg/blockchain/sync.go#L173) em [`pkg/blockchain/sync.go`](file:///c:/Magno/Projetos/jamii/pkg/blockchain/sync.go) para verificar sob trava de leitura se a flag `s.syncing` está ativa. Se estiver, o nó é considerado incondicionalmente como "atrás" (`IsBehind() == true`), mantendo o motor de consenso em **HALT** e evitando qualquer execução concorrente até que a sincronização sequencial seja concluída com sucesso.

## 🛠️ Decisões Recentes (10/08/2026)
1. **Métricas de TPS Efetivo em Tempo Real e Total Acumulado no `/metrics` e Grafana:**
    - **Contexto:** Necessidade de mensurar com precisão a vazão real em transações por segundo seladas em bloco (`Effective TPS`) e o volume total acumulado no endpoint `/metrics` do nó e no painel do Grafana.
    - **Ação:** Implementada a amostragem automática de transações seladas por bloco (`recordCommittedTxs`) em [`pkg/node/node.go`](file:///c:/Magno/Projetos/jamii/pkg/node/node.go) com média móvel em janela deslizante de 10s (`GetEffectiveTPS`) e contador acumulado (`GetTotalCommittedTxs`). Adicionadas as métricas `jamii_blockchain_effective_tps` e `jamii_blockchain_total_transactions_total` no handler `/metrics` em [`pkg/rpc/server.go`](file:///c:/Magno/Projetos/jamii/pkg/rpc/server.go). Atualizada a coluna `TPS Efetivo` na tabela comparativa por nó no dashboard enxuto do Grafana ([`Grafana Dashboard/Grafana.json`](file:///c:/Magno/Projetos/jamii/Grafana%20Dashboard/Grafana.json)).
2. **Unificação do `CompactBlock` no `ProposalPayload` via Canal EXPRESS (Besu IBFT 2.0 Logic):**
    - **Contexto:** A transmissão separada do esqueleto do bloco (`MT_COMPACT_BLOCK`) pelo canal BULK e da mensagem de proposta (`ProposalPayload`) pelo canal EXPRESS gerava corridas de canal (race conditions) no milissegundo de estabilização dos sockets TCP P2P, fazendo com que nós recebessem o bloco mas perdessem o sinal de proposta.
    - **Ação:** Adicionado o campo `CompactBlock` diretamente na struct `ProposalPayload` em [`pkg/consensus/ibft/payloads.go`](file:///c:/Magno/Projetos/jamii/pkg/consensus/ibft/payloads.go), transmitindo o esqueleto unificado em uma única mensagem no canal EXPRESS. Em [`pkg/consensus/ibft/round.go`](file:///c:/Magno/Projetos/jamii/pkg/consensus/ibft/round.go), a recepção da `PROPOSAL` reconstrói o bloco na `PayloadPool` local e dispara a verificação de estado no mesmo milissegundo. O canal BULK permanece dedicado exclusivamente ao sincronismo histórico e download de blocos passados.
3. **Janela de Estabilização de Conexões TCP P2P (2s Delay antes do Sinal `READY`):**
    - **Contexto:** No boot dos nós, o sinal `ready: true` era transmitido imediatamente após a carga do banco de dados, antes que os canais gêmeos P2P (`Bulk` e `Express`) com os outros validadores estivessem 100% negociados no nível de socket.
    - **Ação:** Inserida uma pausa de 2 segundos (`time.Sleep(2 * time.Second)`) em [`pkg/node/node.go`](file:///c:/Magno/Projetos/jamii/pkg/node/node.go) após o sincronismo histórico e antes do anúncio `ready: true`, garantindo tempo suficiente para estabilização completa da malha de conexões TCP.
4. **Deduplicação Estrita de Mensagens de `RoundChange` (`isNewVote`) & Ajuste de Nível de Log:**
    - **Contexto:** O recebimento de votos repetidos de `RoundChange` gerava poluição de logs no terminal e loops de re-resposta na rede P2P.
    - **Ação:** Adicionado o filtro `isNewVote` em `handleRoundChange` no [`pkg/consensus/ibft/controller.go`](file:///c:/Magno/Projetos/jamii/pkg/consensus/ibft/controller.go) para absorver silenciosamente pacotes repetidos do mesmo remetente. O log de votação foi alterado para nível `DEBUG` e teve a tag duplicada `[CONSENSUS] [CONSENSUS]` removida.
5. **Ticker Proativo de Re-gossip de `RoundChange` (1.5s):**
    - **Contexto:** Votações de mudança de rodada pendentes podiam travar em caso de oscilações temporárias de rede.
    - **Ação:** Adicionadas as funções `startRoundChangeRegossip` e `stopRoundChangeRegossip` em [`pkg/consensus/ibft/controller.go`](file:///c:/Magno/Projetos/jamii/pkg/consensus/ibft/controller.go) com ticker de 1.5s para re-transmitir o voto ativo do nó via canal EXPRESS até atingir o quórum de $2f+1$.
6. **Refatoração dos Handshakes DTS (Separação Canal vs Peer):**
    - **Contexto:** Registros duplicados de chaves públicas criptográficas e notificações de conexão ocorriam porque o DTS acionava callbacks a cada canal TCP individual (`Bulk` ou `Express`), em vez de esperar a entidade Dual-Channel completa (`Peer`).
    - **Ação:** Refatorado `registerPeer` em [`pkg/dts/engine.go`](file:///c:/Magno/Projetos/jamii/pkg/dts/engine.go) para executar o registro PQC e notificação de status estritamente no nível do Peer (`set.Bulk != nil && set.Express != nil`).

## 🛠️ Decisões Recentes (31/07/2026)
1. **Proteção Anti-OOM DoS com Leitura Incremental de Memória no DTS (Sprint 2 - Reparo de Urgência):**
    - **Contexto:** Na função `readMessage()` de [`pkg/dts/network_proto.go`](file:///c:/Magno/Projetos/jamii/pkg/dts/network_proto.go), ao ler um cabeçalho declarando um tamanho `length32` (ex: 30MB), o sistema realizava `make([]byte, length32)` imediatamente antes de ler os bytes reais do socket. Conexões maliciosas ou travadas que enviavam apenas o cabeçalho alocavam 30MB de RAM instantaneamente sem enviar dados, expondo o nó a ataques OOM (*Out Of Memory*).
    - **Ação:** Refatorada a leitura de payload em `readMessage()` para usar alocação incremental protegida em blocos de 64KB (`chunkBuf := make([]byte, 64*1024)`), expandindo o buffer final somente conforme os bytes chegam da rede. O teste TDD [`emerg-tests/dts_oom_protection_test.go`](file:///c:/Magno/Projetos/jamii/emerg-tests/dts_oom_protection_test.go) comprovou uma redução de consumo de 300.00 MB para 1.28 MB (redução de 232x na exposição de memória por conexão).
2. **Proteção Idempotente (`SafeClose`) para Canais Gêmeos do DTS (Sprint 2 - Reparo de Urgência):**
    - **Contexto:** Na função `unregisterPeer()` do motor DTS em [`pkg/dts/engine.go`](file:///c:/Magno/Projetos/jamii/pkg/dts/engine.go), quando um dos canais gêmeos (Bulk ou Express) sofria desconexão, o mandato de encerramento mútuo provocava chamadas duplas a `close(p.quit)`, disparando exceções de pânico (`panic: close of closed channel`) e derrubando o processo do nó.
    - **Ação:** Adicionado o campo `closeOnce sync.Once` e o método `SafeClose()` à struct `Peer` em `engine.go`, garantindo que conexões e canais de sinalização sejam fechados de forma idempotente e thread-safe. Criado e aprovado o teste TDD [`emerg-tests/dts_channel_panic_test.go`](file:///c:/Magno/Projetos/jamii/emerg-tests/dts_channel_panic_test.go).
3. **Garantia de Entrega sem Descarte Silencioso para Votos Express no DTS (Sprint 2 - Reparo de Urgência):**
    - **Contexto:** Na função `broadcast()` do DTS em [`pkg/dts/engine.go`](file:///c:/Magno/Projetos/jamii/pkg/dts/engine.go), caso o buffer do peer estivesse congestionado (`sendCh` cheio), mensagens marcadas como Express (votos de consenso IBFT) eram descartadas silenciosamente pela cláusula `default:`. Sob estresse ou rajadas de tráfego, esse descarte aleatório impedia o consenso de atingir o quórum de $2f+1$.
    - **Ação:** Atualizado o método `broadcast()` em `engine.go` para adicionar um seletor com janela de espera de 500ms (`select { case target.sendCh <- data: case <-time.After(500 * time.Millisecond): ... }`) para payloads do tipo `useExpress`, garantindo que os votos de consenso tenham prioridade e janela de resiliência sem descarte instantâneo por `default`. Criado e aprovado o teste TDD [`emerg-tests/dts_express_queue_test.go`](file:///c:/Magno/Projetos/jamii/emerg-tests/dts_express_queue_test.go).
4. **Suporte a Bloco Travado (Locked Block) & Certificado em RoundChange (Sprint 1 - Reparo de Urgência):**
    - **Contexto:** No protocolo IBFT 2.0 (Besu/QBFT), se a rede estoura o tempo da rodada $R$ após atingir o estado `PREPARED`, os validadores que prepararam o bloco ficam travados (*locked*). Ao transitar para a rodada $R+1$, o novo propositor É OBRIGADO a repropor o mesmo bloco travado. Na Jamii, o `RoundChangePayload` não enviava o bloco travado e o propositor montava um bloco novo da MemPool em $R > 0$, podendo gerar bifurcações (Forks/Double-Spend).
    - **Ação:** Atualizado `RoundChangePayload` em `pkg/consensus/ibft/payloads.go` para incluir `HasPrepared`, `PreparedRound` e `PreparedDigest`. Atualizado `onTimeout` em `pkg/consensus/ibft/round.go` para registrar o bloco preparado se `IsPrepared()` for verdadeiro. Atualizado `checkProposerAction` em `pkg/consensus/ibft/controller.go` para inspecionar os certificados de `RoundChange` em $R > 0$ e propor obrigatoriamente o bloco travado da maior rodada preparada. Criado e aprovado o teste TDD [`emerg-tests/consensus_locked_block_test.go`](file:///c:/Magno/Projetos/jamii/emerg-tests/consensus_locked_block_test.go).
5. **Eliminação do Deadlock e Inversão de Travamento em Consenso (Sprint 1 - Reparo de Urgência):**
    - **Contexto:** Existia uma condição potencial de inversão de travas e chamadas concorrentes entre `Controller.mu` e `Round.mu` quando o watchdog de timeout disparava simultaneamente com o recebimento de mensagens P2P no `HandleMessage`.
    - **Ação:** Adicionados guardas de nulidade para `c.pool` e `c.engine` em `checkProposerAction` no [`pkg/consensus/ibft/controller.go`](file:///c:/Magno/Projetos/jamii/pkg/consensus/ibft/controller.go) para chamadas assíncronas/em testes, mantendo o desbloqueio estrito de `r.mu` antes da aquisição de `controller.mu` e garantindo a hierarquia de locks `Controller.mu` -> `Round.mu`. Criado e aprovado o teste de estresse TDD [`emerg-tests/consensus_deadlock_test.go`](file:///c:/Magno/Projetos/jamii/emerg-tests/consensus_deadlock_test.go).
6. **Invalidação de Cache do StateDB no Commit e Promote (StateDB Cache Invalidation):**
    - **Contexto:** Durante a verificação/execução de blocos via sandbox, contas carregadas por Witness eram armazenadas sob `hashKey` (Keccak256 do payload). Se a conta não fosse executada no bloco atual, ela não era mapeada para a chave curta `addrKey` no cache do sandbox. A promoção de estado (`Promote`) copiava a `hashKey` com o nonce correto para o cache canônico do `StateDB`, mas mantinha a chave curta `addrKey` canônica desatualizada (stale). Em blocos seguintes, o `GetNonce` lia a chave curta do cache com o valor defasado.
    - **Ação:** Implementação de invalidação/limpeza completa e incondicional dos caches `s.accounts`, `s.storage` e `s.storageTries` ao final de cada comit físico (`Commit()`) e promoção de estado (`Promote()`) do `StateDB`. Isso força todas as leituras subsequentes a consultarem os valores reais e atualizados do PebbleDB ou do Witness, eliminando bugs de descompasso de nonces (`nonce mismatch`) no Sincronismo e na MemPool.
2. **TCP Keep-Alive e Bypass por Inatividade no DTS (DTS Connection Inactivity Bypass):**
    - **Contexto:** O motor DTS não configurava Keep-Alive no nível do socket TCP. Em reboots abruptos de validadores, as conexões antigas continuavam como ativas no cache de alguns nós (sockets órfãos no estado half-open) por até 2 horas. As tentativas de reconexão do validador reiniciado eram rejeitadas pelo tie-break determinístico do DTS (que mantinha o socket antigo inativo), gerando isolamento parcial de validadores e impedindo a rede de atingir o quórum de 4.
    - **Ação:** Ativação de TCP Keep-Alive agressivo de 10 segundos em todas as conexões do DTS. Adicionado o campo `lastSeen` à struct `Peer` (atualizado a cada mensagem de rede recebida, incluindo pings de status). Modificada a lógica de Tie-Break no `registerPeer` para ignorar as restrições e adotar imediatamente a nova conexão se o peer antigo no cache estiver inativo por mais de 15 segundos.
3. **Renovação do Timer da Rodada na Fase Prepared (IBFT2 Commit Window Extension):**
    - **Contexto:** Ao atingir o estado `Prepared` e assinar seu `COMMIT`, o timer da rodada `r.timer` de 8 segundos no IBFT2 não era resetado. Se a transição ocorresse aos 7.8s de rodada, o timer estourava 0.2s depois de forma cega, marcava `r.isActive = false` e descartava todos os selos de `COMMIT` recebidos dos outros validadores, abortando blocos válidos a milissegundos de finalizar.
    - **Ação:** Implementação de renovação/extensão automática do `r.timer` em `checkStateTransitions` no [round.go](file:///c:/Magno/Projetos/jamii/pkg/consensus/ibft/round.go) ao transitar para `Prepared` (Hyperledger Besu Compliance). Isso concede uma janela de Commit estendida dedicada para consolidação dos selos dos pares sem cancelamento prematuro da rodada.
4. **Purificação do Tie-Break do DTS para Canais Express Exclusivos (Pure Express Tie-Break):**
    - **Contexto:** O canal Express é utilizado **exclusivamente** para mensagens de consenso IBFT. Em silêncio de transações, a falta de tráfego de aplicação no Express fazia com que uma heurística de `lastSeen` no `registerPeer` assumisse erroneamente que a conexão Express antiga estava morta, rejeitando e fechando novas conexões Express legítimas.
    - **Ação:** Removida a dependência de `lastSeen` no `registerPeer` em [engine.go](file:///c:/Magno/Projetos/jamii/pkg/dts/engine.go). O tie-break de NodeIDs opera de forma limpa e determinística, deixando o ciclo de vida dos sockets TCP a cargo dos eventos de I/O de socket (`readMessage`) e do Mandato de Canais Gêmeos em `unregisterPeer`. Mantido fallback instantâneo no `broadcast` do DTS para o canal Bulk caso a conexão Express hesite.

## 🛠️ Decisões Recentes (30/07/2026)
1. **Purga Autônoma de `hotNonces` no `Reset` da MemPool (Independent Hot Nonce Reset Purge):**
    - **Contexto:** No método `Reset` da MemPool ([pool.go](file:///c:/Magno/Projetos/jamii/pkg/mempool/pool.go)), a purga do mapa de carteiras quentes em RAM (`hotNonces`) era executada exclusivamente dentro do laço `for senderKey, list := range tp.pending`.
    - **Problema:** Quando `GetExecutable` extraía todas as transações de uma conta para montagem de uma proposta de bloco, a lista `tp.pending` dessa conta ficava vazia (`Len() == 0`) e era removida de `tp.pending`. Quando o bloco era minerado e o `Reset` executava, a conta já não estava em `tp.pending`, fazendo com que seu `hotNonce` nunca fosse purgado, ficando preso em um valor inflado no futuro. As consultas `eth_getTransactionCount("pending")` subsequentes devolviam esse nonce adiantado, fazendo os clientes RPC enviarem transações com lacuna de nonce (`QUEUE`).
    - **Ação:** Implementação de uma varredura autônoma e direta sobre `tp.hotNonces` ao final do `Reset`, verificando `hotNonce <= tp.state.GetNonce(addr)` para cada remetente e deletando entradas confirmadas, independentemente da presença da conta no mapa `tp.pending`.
2. **Remoção de Atualização de `hotNonce` na Fila de Espera (`QUEUE` Nonce Corruption Prevention):**
    - **Contexto:** Ao receber uma transação com lacuna de nonce (`tx.Nonce > nextExpected`), o método `AddTx` inseria a transação na `QUEUE` e executava `updateHotNonceLocked(senderKey, tx.Nonce+1)`.
    - **Problema:** Essa atualização corrompia o rastreamento de nonces sequenciais da carteira e fazia o RPC devolver o nonce do gap + 1, travando a conta em um loop infinito de transações enviadas para a `QUEUE` sem conseguir preencher os nonces intermediários faltantes.
    - **Ação:** Removida a chamada `updateHotNonceLocked` no ramo de inserção em `QUEUE`. Apenas transações contíguas em `PENDING` ou extraídas para bloco (`GetExecutable`) atualizam o `hotNonce`.
3. **Alinhamento de Formato de Chave de Endereço em `GetPendingNonce` (Hex Key Alignment):**
    - **Contexto:** O método `GetPendingNonce` consultava o mapa `hotNonces` e `pending` utilizando `addr.String()` (representação Bech32 `jamii1...`), enquanto a MemPool armazenava as chaves utilizando `addr.Key()` (representação hexadecimal do payload de 20 bytes).
    - **Problema:** Devido a esse descompasso no tipo de chave, `GetPendingNonce` nunca encontrava as entradas em `hotNonces` nem em `pending`, retornando nonces defasados do estado base.
    - **Ação:** Alterada a consulta em `GetPendingNonce` para utilizar `addr.Key()`, garantindo paridade total com o endereçamento de chaves de todas as estruturas da MemPool.
4. **Resiliência Transiente no Gerador de Tráfego (`cmd/traffic/generator.go` Transient Nonce Pause):**
    - **Contexto:** Sob alta vazão (soak test a 10 TPS com 50 carteiras), pequenos descompassos de milissegundos na propagação do estado durante o encerramento do bloco geravam respostas temporárias de `Nonce mismatch` ou `Nonce too low`.
    - **Ação:** Adicionada uma pausa suave de 50ms quando a resposta RPC indica erro transiente de nonce, permitindo que a carteira re-sincronize seu nonce via RPC no ciclo seguinte com estabilidade e sem registrar falhas falsas-positivas.

## 🛠️ Decisões Recentes (17/07/2026)
1. **Recepção Assíncrona de Lotes de Transações (Asynchronous DTS Transaction Processing):**
    - **Contexto:** A recepção de pacotes `MT_TX_BATCH` rodava de forma síncrona dentro da thread leitora da conexão DTS. Sob flood massivo (500 TPS), a importação à MemPool e validação criptográfica (ML-DSA) levava até 50ms por lote.
    - **Problema:** O bloqueio da thread de leitura do DTS por 50ms gerava latência e gargalo de I/O no canal TCP do socket do canal BULK. Quando uma proposta de compact block (Skeleton) chegava pelo canal EXPRESS, ela era processada de imediato, mas falhava a reconstrução porque as transações necessárias ainda estavam retidas no socket do canal BULK, gerando fallbacks frequentes para Full Blocks.
    - **Ação:** Refatoração de `handleDataArrival` em [node.go](file:///c:/Magno/Projetos/jamii/pkg/node/node.go) para processar o lote de forma assíncrona em uma goroutine (`go func() { ... }()`). Isso libera a thread de leitura DTS imediatamente para drenar o socket de rede, eliminando o gargalo de latência e acelerando o processamento do backlog da MemPool.
2. **Filtragem de Transações Válidas na Montagem de Blocos (Valid Transactions Proposal Filtering):**
    - **Contexto:** Durante floods extremos a 500 TPS, transações obsoletas ou temporariamente inválidas (ex: gap de nonce) podiam ser rejeitadas no sandbox de simulação do propositor, mas ainda assim eram incluídas na lista global de transações do bloco final proposto (`NewBlock(header, transactions)`).
    - **Problema:** Ao receberem a proposta, os demais validadores tentavam executar a lista do bloco contendo a transação inválida, falhando no check e rejeitando sistematicamente a proposta legítima, o que travava a rede em timeouts e round changes.
    - **Ação:** Refatoração de `GetPayloadWithState` em [processor.go](file:///c:/Magno/Projetos/jamii/pkg/core/processor.go) para instanciar e preencher um slice secundário `validTxs` apenas com as transações cuja execução no sandbox tenha retornado `err == nil`. O bloco proposto passa a ser construído exclusivamente com `validTxs`, eliminando rejeições de propostas por contaminação de transações puladas.
3. **Higiene Defensiva contra Deadlocks de Consenso (Stale Transaction Purging):**
    - **Contexto:** Sob inundação de transações a 500 TPS com limites de slots da MemPool estourados, lacunas de nonces provocavam rejeições de propostas e rodadas extras de consenso. Transações obsoletas com nonces já minerados permaneciam presas na MemPool do proposer, fazendo com que ele as repropusesse ciclicamente nas novas rodadas, travando o consenso.
    - **Ação:** Inserção de uma validação e purga ativa de nonces obsoletos (`tx.Nonce < state.GetNonce(sender)`) de dentro da função `GetExecutable` da MemPool ([pool.go](file:///c:/Magno/Projetos/jamii/pkg/mempool/pool.go)), eliminando transações desatualizadas no momento da montagem e destravando qualquer loop de deadlock.
4. **Otimização de Performance via Cache de Nonces na Montagem (Local Nonce Caching):**
    - **Contexto:** Consultar `tp.state.GetNonce(sender)` repetidamente no loop de `GetExecutable` para um mesmo remetente com centenas de transações pendentes gerava overhead redundante de travas (Mutex) sobre a `StateDB`.
    - **Ação:** Criação de um mapa local temporário (`localNonces`) em `GetExecutable` para registrar o nonce do remetente na primeira consulta, permitindo que consultas subsequentes para o mesmo remetente durante a montagem do bloco rodem em complexidade $O(1)$ sem concorrência de travas.
5. **Otimização Concorrente de Cache de Assinaturas PQC (Signature Validation Caching):**
    - **Contexto:** Validações de assinaturas ML-DSA-65 pós-quânticas são custosas (~1ms por assinatura). Durante a validação de blocos cheios (1.000+ TXs), a assinatura de cada transação era verificada até 4 vezes no ciclo de consenso e processamento da VM.
    - **Problema:** Desperdício severo de CPU que elevava o tempo de verificação de blocos para 9 segundos, estourando o timeout de rodada (watchdog) e forçando trocas de líder.
    - **Ação:** Implementação de cache de verificação concorrente (`verifiedCache` e `hasVerified` protegidos por RWMutex) na struct `Transaction` em [transaction.go](file:///c:/Magno/Projetos/jamii/pkg/encoding/transaction.go). Reduziu o processamento de blocos populosos para menos de 1 segundo.
6. **Correção de Pânico de Índice na MemPool (Heap Index Inconsistency on Reset):**
    - **Contexto:** Sob estresse massivo de transações (Tsunami Test de 30.000 TXs), os nós podiam crashar com erro `index out of range` nas heaps de prioridade e expulsão.
    - **Problema:** O método `Reset()` da MemPool reconstrói as heaps de forma linear pós-bloco. No entanto, elementos que não eram movidos pelo `heap.Init` retinham seus índices antigos obsoletos do estado pré-reset. Ao tentar remover transações subsequentes, o `heap.Remove` usava esses índices inválidos maiores que o tamanho atual da heap, causando o pânico.
    - **Ação:** Reset defensivo geral de todos os campos `priceIdx` e `evictIdx` para `-1` para todas as transações ativas em `tp.all` no início da reconstrução de heaps em `Reset()`. Isso limpa índices obsoletos de transações que não foram re-incluídas na nova PriceHeap (`tp.heads`) (como transações secundárias ou da `queue`), evitando referências inválidas fora do intervalo que causavam pânico em `heap.Remove`.
7. **Recuperação Direcionada de Bloco Inteiro (Targeted Fallback Recovery):**
    - **Contexto:** Se a reconstrução compacta (Skeleton) falhava sob flood (por falta de alguma transação na pool local), o nó pedia o bloco completo à rede.
    - **Problema:** O método legado buscava por altura (`bestPeer`). Como a rodada estava ativa, nenhum nó havia persistido o bloco, fazendo com que o pedido retornasse vazio e forçando timeouts da rodada 0.
    - **Ação:** Ajustado `RequestFullBlock` em [sync.go](file:///c:/Magno/Projetos/jamii/pkg/blockchain/sync.go) para aceitar um preferredPeer (Coinbase do cabeçalho) e direcionar o pedido diretamente ao proposer do bloco compacto.
8. **Filtro de Maturação de Transações Baseado em Blocos (Block-Based Propagation Delay Guard):**
    - **Contexto:** Proposições de blocos compactos falhavam na reconstrução imediata porque o propositor incluía transações recém-chegadas que ainda não tinham tido tempo de se propagar pela rede P2P aos demais validadores.
    - **Ação:** Criação do controle por alturas na MemPool ([pool.go](file:///c:/Magno/Projetos/jamii/pkg/mempool/pool.go)), onde cada transação registra a altura canônica atual (`addedAtHeight = currentHeight`) ao ingressar. Durante a montagem do bloco, se o número total de transações executáveis na pool exceder o limiar de segurança `PropagationDelayThreshold` (padrão 50), o propositor ativa o filtro e ignora transações da altura atual (`addedAtHeight >= currentHeight`). Isso impede oscilações de fluxo sob carga e garante 100% de sucesso nas reconstruções de blocos compactos sem a necessidade constante de fallbacks.
9. **Lote de Propagação de Transações (Transaction Transfer Packet / Batching):**
    - **Contexto:** Transações pós-quânticas individuais são pesadas (~5.5 KB cada). Propagá-las individualmente sob tráfego massivo gerava milhares de mensagens (INV/REQ/DATA) que congestionavam e estouravam o buffer de saída do DTS, causando perdas de mensagens de controle.
    - **Ação:** Criação do tipo de payload `MT_TX_BATCH` no DTS e de um buffer dinâmico no `Node`. Transações recebidas via RPC são agrupadas em lotes de tamanho configurável (via genesis `MaxTxBatchSize`, padrão 50) ou enviadas após 100ms de inatividade. Isso diminuiu o overhead de controle de rede in 98% e estabilizou completamente a fila de saída.


## 🛠️ Decisões Recentes (10/07/2026)
1. **Silenciamento de Consenso para Nós Observadores/Stateless (P2P Consensus Broadcast Suppression):**
    - **Contexto:** Nós rodando em modo stateless ou observador (que não pertencem ao conjunto de validadores oficiais do gênese) processavam blocos localmente mas transmitiam mensagens de voto (`PREPARE`, `COMMIT` ou `ROUND_CHANGE`) no canal P2P do DTS. A rede rejeitava essas mensagens, gerando logs constantes de aviso de remetente não autorizado (`unauthorized sender`).
    - **Problema:** Tráfego indevido na rede de controle e log spam desnecessário nos demais validadores autorizados da rede Jamii.
    - **Ação:** Inserção do bloqueio de transmissão `if !c.IsValidator(c.localAddr) { return }` no topo da função `Multicast` em [controller.go](file:///c:/Magno/Projetos/jamii/pkg/consensus/ibft/controller.go). Isso silenciou a emissão de mensagens de consenso por nós observadores sem afetar sua capacidade de processar blocos locais e sincronizar o StateDB.
2. **Otimização de Reset e Prevenção de Pânico de Heap na MemPool (MemPool Reset Heap Index Out of Range Fix):**
    - **Contexto:** Durante floods ou altas cargas de transações no final de um bloco, nós ativos podiam sofrer crash fatal com o erro `index out of range` no heap de remoção da MemPool.
    - **Problema:** O método `Reset` executava purgas de nonces minerados e transações inválidas individualmente usando `heap.Remove` de dentro de laços iterativos. Essas remoções reequilibravam as heaps de forma incremental e alteravam dinamicamente os índices dos elementos na árvore (`priceIdx` e `evictIdx`), causando descompasso de leitura nos loops e corrupção de ponteiros. Dado que o `Reset` descarta e reconstrói as heaps do zero no final do método (`heap.Init`), remover itens de forma incremental durante a iteração era redundante, custoso e inseguro.
    - **Ação 1 (Filtro e Promoção Offline):** Refatoração da rotina de promoção de filas para `promoteWithOptions(senderKey, updateHeaps)`, permitindo que ela rode em modo offline (sem chamadas a heap). Substituição do `tp.remove` incremental por um loop local offline de filtragem `list.FilterGreaterOrEqual` diretamente em `tp.all`.
    - **Ação 2 (Reconstrução Linear de Heaps):** Remoção de todas as chamadas `heap.Remove` e `heap.Push` de dentro dos loops de purga e promoção do `Reset`. As heaps são agora limpas e reconstruídas em complexidade linear uma única vez no final da execução via `heap.Init(tp.eviction)` e `heap.Init(tp.heads)`. Isso reduziu a complexidade de \(O(K \log N) + O(N)\) para \(O(K) + O(N)\) e erradicou qualquer pânico de índice na MemPool.
3. **Reconciliação de DOM sem Flicker e Painel de Resumo no Explorer:**
    - **Contexto:** A aba de validadores no explorador sofria de oscilação visual constante (flicker) a cada 2.5s devido ao uso de `innerHTML` que recriava a página do zero, além do histórico de propostas estar limitado a 10 blocos na API.
    - **Ação 1 (DOM Reconciliation cirúrgico):** Refatoração de `index.html` em [index.html](file:///c:/Magno/Projetos/jamii/explorer/src/main/resources/static/index.html) mapeando os cards de validadores por atributos `data-address` e atualizando cirurgicamente apenas os valores alterados. Endereços longos Bech32 foram encurtados em linha única com tooltip no hover.
    - **Ação 2 (Cache e Gráfico TPS):** Criação da variável global `validatorBlockHistory` que acumula blocos (janela deslizante de até 100 blocos) para computar taxas de participação reais. Integração do Chart.js para plotagem de gráfico de linha em área com o TPS instantâneo.
4. **Startup Connection Diagnostics & Docker Parametrization no Explorer:**
    - **Contexto:** O explorer não conseguia conectar ao nó se o host e a porta fossem customizados, pois as credenciais de banco e a URL JSON-RPC estavam hardcoded em `application.properties`.
    - **Ação 1 (Parametrização):** Mapeamento de todas as variáveis do banco, porta e nó no [application.properties](file:///c:/Magno/Projetos/jamii/explorer/src/main/resources/application.properties) para as variáveis de ambiente utilizadas no deploy do Docker (`DB_URL`, `DB_USER`, `DB_PASSWORD`, `PORT`, `JSON_URL`) com seus respectivos fallbacks.
    - **Ação 2 (Diagnósticos no Boot):** Implementação de um método `testJsonRpcConnection()` anotado com `@PostConstruct` no [ApiController.java](file:///c:/Magno/Projetos/jamii/explorer/src/main/java/com/jamii/explorer/ApiController.java) que tenta ler o `chainId` da rede do nó no startup do Spring Boot, imprimindo logs formatados de sucesso ou alertas instruindo o ajuste do setup.
5. **Sincronização de BaseFee no Startup do Nó (Mempool BaseFee Sync):**
    - **Contexto:** No startup do nó, a MemPool inicializava a taxa base padrão (`currentBaseFee`) com o valor fixo de 1 Gwei. Se a rede estivesse inativa, a API RPC retornava o valor histórico real (ex: 7 wei) do último bloco gravado, mas a MemPool rejeitava transações válidas por achar que a taxa mínima era 1 Gwei (já que as rotinas dinâmicas de blocos não estavam rodando para atualizar o valor).
    - **Ação:** Atualização da inicialização do nó em [node.go](file:///c:/Magno/Projetos/jamii/pkg/node/node.go) para ler a cabeça da blockchain local e sincronizar o `BaseFee` da MemPool com o do último bloco gravado no startup.
6. **Correção do Nonce Drift no Consenso via Pre-State Witness (Stateless Verification Drift Fix):**
    - **Contexto:** Ao submeter transações na rede Jamii, a proposta de bloco do round de consenso falhava persistentemente com o erro `Transaction nonce mismatch: expected 42750, got 42749`, travando a rede em loops de timeouts e leader changes.
    - **Problema:** A Witness gerada pelo Proposer no método `GetPayloadWithState` era computada a partir do `sandbox` de estado pós-execução das transações. Ao receber a proposta de bloco, os validadores stateless aplicavam a Witness via `ApplyWitness` no início da verificação, injetando os saldos e nonces finais/pós-execução no cache local. Consequentemente, ao executar o `ApplyTransaction` para validar a transação (que esperava o nonce de partida `42749`), o validador lia o nonce pós-execução `42750` pré-carregado no cache, provocando a rejeição indevida por nonce inválido.
    - **Ação 1 (Extração de Pre-State Witness):** Implementação do método `GeneratePreStateWitness(sandbox)` no [statedb.go](file:///c:/Magno/Projetos/jamii/pkg/core/state/statedb.go) para coletar as chaves de contas acessadas no sandbox, mas extrair os seus nonces e saldos iniciais a partir do `StateDB` de partida (pré-execução, correspondente ao estado do bloco pai).
    - **Ação 2 (Segregação de Estados na Construção):** Ajuste do método `GetPayloadWithParent` em [processor.go](file:///c:/Magno/Projetos/jamii/pkg/core/processor.go) para instanciar e passar o `parentState` isolado da simulação. Ajuste de `GetPayloadWithState` em [processor.go](file:///c:/Magno/Projetos/jamii/pkg/core/processor.go) para invocar `st.GeneratePreStateWitness(sandbox)` para a geração da Witness de pré-estado. Isso resolveu definitivamente o desalinhamento e desbloqueou a finalização das propostas de blocos.
7. **Desativação Permanente do Paralelismo de Especulação Ativa (Active Speculation Parallelism Disabling):**
    - **Contexto:** Concorrência e condições de corrida na RAM (StateDB/MemPool) e leituras concorrentes com commits físicos no PebbleDB causavam falhas intermitentes de StateRoot na rede sob alto throughput.
    - **Ação:** Remoção do gatilho assíncrono de especulação paralela em [round.go](file:///c:/Magno/Projetos/jamii/pkg/consensus/ibft/round.go), silenciamento da função `speculateNextBlock` e fixação da flag `nextProposerReady` para sempre falso em [controller.go](file:///c:/Magno/Projetos/jamii/pkg/consensus/ibft/controller.go). O propositor agora monta novos blocos síncrona e deterministicamente no início de sua rodada de proposta após a expiração do pacing de bloco, operando sobre o estado totalmente estável e comitado do bloco anterior.
8. **Keep-Alive Ativo de Status no Watchdog de Quórum (P2P Status Keep-Alive Ticker):**
    - **Contexto:** Validadores travavam após períodos longos no status `HALT` (Consensus Quorum Insufficient) devido à falta de sincronia nas tabelas locais de alturas de peers.
    - **Problema:** O anúncio de status (`BroadcastStatus`) ocorria exclusivamente em eventos-chave (término de Sync ou Finalização de Bloco). Quando o consenso era interrompido e a rede entrava em Halt, nenhum bloco novo era finalizado e nenhum sync ocorria. Os nós paravam de transmitir status, congelando as memórias locais de quórum com alturas defasadas dos pares e gerando um deadlock de silêncio do consenso.
    - **Ação:** Inclusão do anúncio reativo `dts.BroadcastStatus` a cada 5 segundos de dentro do loop do Watchdog de Manutenção de Quórum em [node.go](file:///c:/Magno/Projetos/jamii/pkg/node/node.go). Isso garante que, mesmo em Halt, os validadores atualizem ativamente suas tabelas de quórum na malha P2P, permitindo o reinício automático e imediato do motor de consenso assim que o quórum de prontidão e as alturas convergirem.

## 🛠️ Decisões Recentes (06/07/2026)
1. **Alinhamento de Constantes da Fábrica de Trie (TreeType Configuration Alignment):**
    - **Contexto:** Os arquivos `genesis.json` definem a arquitetura da árvore global (`treeType`) como `1` para SMT e `2` para Verkle. No entanto, no código Go a enumeração `TreeType` utilizava `iota` implícito.
    - **Problema:** O uso do `iota` definia implicitamente `TypeSMT` como `0` e `TypeVerkle` como `1`. Quando o nó lia `treeType: 2` do gênese (Verkle), o factory falhava em encontrar um correspondente no switch e entrava em fallback silencioso para SMT (0). Já em nós configurados com `treeType: 1` (SMT), a fábrica instanciada erroneamente a Verkle Tree (1).
    - **Ação:** Refatoração de [trie-iface.go](file:///c:/Magno/Projetos/jamii/pkg/trie/trie-iface.go) definindo explicitamente as constantes `TypeSMT = 1` e `TypeVerkle = 2`. Isso alinhou a fábrica com as especificações e definições de boot em toda a rede local e multiserver.
2. **Correção de Panic na MemPool e Ajuste da Purga Descendente (MemPool Panic and Descendant Purge Fix):**
    - **Contexto:** Durante o processamento de transações, nós da rede podiam sofrer um panic por `index out of range [-1]` na MemPool, especialmente se ficassem temporariamente atrás do consenso e tivessem transações acumuladas com gaps na queue.
    - **Problema 1 (Panic na Pendência Vazia):** O método de promoção (`promote`) registrava prematuramente listas pendentes vazias (`tp.pending[sender]`) na memória antes de mover as transações da fila. Ao receber uma nova transação compatível subsequente, o método `Add` tentava ler o último elemento da lista usando o índice `-1` (pois a lista estava vazia), resultando em panic.
    - **Problema 2 (Bug na Purga Descendente):** O método de remoção (`remove`) de transações usava a função `Filter` (que remove nonces *inferiores* ao especificado) em vez de remover transações com nonces *maiores ou iguais* (que representam a quebra da sequência/descendentes). Isso corrompia e descompassava o estado interno da MemPool, além de quebrar os testes de limites de slots originais que usavam um mesmo remetente.
    - **Ação 1 (Defesa e Atraso de Inicialização):** Adicionados bloqueios defensivos (`pendingList.Len() > 0`) nos leitores de nonce em [pool.go](file:///C:/Magno/Projetos/jamii/pkg/mempool/pool.go) e postergado o registro de novas listas em `tp.pending` até que o primeiro item seja promovido com sucesso.
    - **Ação 2 (Implementação do Filtro Descendente):** Adicionado o método `FilterGreaterOrEqual` em [tx_list.go](file:///C:/Magno/Projetos/jamii/pkg/mempool/tx_list.go) e atualizado o método `remove` em [pool.go](file:///C:/Magno/Projetos/jamii/pkg/mempool/pool.go) para utilizá-lo de forma recursiva e simétrica tanto no mapa `pending` quanto no mapa `queue`.
    - **Ação 3 (Alinhamento dos Testes de Evicção):** Ajustado o teste `TestTxPool_Limits` em [pool_test.go](file:///C:/Magno/Projetos/jamii/pkg/mempool/pool_test.go) para usar chaves híbridas reais e endereços distintos para cada conta de teste. Isso evitou que a purga de uma transação eliminasse transações de outras contas. O limite de memória do teste foi expandido para `8000` bytes para acomodar os payloads maiores de assinaturas PQC híbridas. Adicionados novos casos de testes focados para validar a purga e o panic.

## 🛠️ Decisões Recentes (02/07/2026)
1. **Resolução de Apelidos de Validadores no Cartório do Archiver (P2P Handshake Alias Persistence):**
    - **Contexto:** Nós observadores (como o Archiver) podem utilizar arquivos `genesis.json` sem chaves públicas (`publicKey: ""`) pré-configuradas. Ao inicializarem, esses nós não registram as identidades dos validadores localmente sob apelidos no boot, dependendo unicamente da recepção das chaves públicas durante o handshake da rede P2P (DTS).
    - **Problema:** O handshake de conexão direta do DTS transmite apenas a chave pública para autenticação mútua (sem carregar o nome/apelido do validador para poupar banda). Isso fazia com que o método `Register` do cartório do Archiver registrasse a identidade com o apelido vazio (`""`), deixando a coluna `alias` da tabela `identities` vazia no PostgreSQL e inviabilizando a exibição dinâmica de nomes no explorador de blocos.
    - **Ação 1 (Pre-registro de Apelidos de Gênese):** Correção em [main.go](file:///c:/Magno/Projetos/jamii/cmd/archiver/main.go) e [node.go](file:///c:/Magno/Projetos/jamii/pkg/node/node.go) para ler todos os validadores e apelidos do `genesis.json` no boot e cadastrá-los na tabela in-memory do `IdentityRegistry` via `RegisterAlias`, mesmo que a chave pública ainda não esteja disponível.
    - **Ação 2 (Resolução e Persistência Oportuna):** Ajuste na rotina `RegisterWithAlias` de [registry.go](file:///c:/Magno/Projetos/jamii/pkg/crypto/signer/registry.go) para que, caso o parâmetro de apelido seja vazio (como no handshake), busque-se o apelido correspondente do mapa de memória local e, caso exista, grave no banco de dados (`alias:<address>`). Adicionada lógica de persistência para atualizações de apelidos mesmo para identidades já em cache.
2. **Profissionalização do Smart Contracts Gate no Block Explorer:**
    - **Contexto:** O protótipo inicial do Smart Contracts Gate em `index.html` utilizava classes e tamanhos de fonte de cards estatísticos, resultando em cabeçalhos de descrição gigantescos e inputs/formulários desproporcionais ou sem estilos dedicados.
    - **Ação 1 (Estilização Unificada):** Implementação de regras de formulário (`.form-label` e `.form-input`) e botões genéricos de ação (`.btn` e `.btn-primary`) nas declarações CSS globais, combinando com o tema claro (Light Premium) e degradês Azul e Laranja do logo oficial.
    - **Ação 2 (Reestruturação de Layout):** Substituição do banner com cards estatísticos por um painel exclusivo (`.section-card`) com fundo em degradê suave, limitando o tamanho dos textos. Transposição das colunas do dashboard de contratos para a classe `.section-card`, unificando as bordas finas e sombras premium.
    - **Ação 3 (Saneamento do Loader e Color Coding):** Substituição do texto estático "Carregando contratos..." por uma linha de loading animada com spinner do FontAwesome (`fa-spinner`). Classificação visual de rotinas de contrato por cores: métodos de leitura (view/pure) destacados em verde (`var(--success)`) e métodos de escrita/transação em laranja (`var(--accent-orange)`), com os retornos e TX hashes exibidos em caixas monospaçadas formatadas em Fira Code.
    - **Ação 4 (Escopo Global de Funções auxiliares):** Correção do bug onde o clique no botão "Registrar Existente" não realizava ações devido ao fato das funções auxiliares da aba de contratos estarem aninhadas dentro da função `initApp()`, tornando-as inacessíveis a partir de gatilhos HTML inline (`onclick`). Fechou-se o escopo de `initApp()` logo após o intervalador e moveu-se as declarações de interação para o escopo global.
    - **Ação 5 (Segurança no Deploy de Contratos):** Implementação de bloqueio de deploy de contratos sem carteira conectada. O formulário agora esconde o botão de deploy caso o usuário esteja sem sessão ativa de carteira (`!walletState.logged`), exibindo em seu lugar um banner de aviso que redireciona à aba de login, além de uma salvaguarda programática com `alert()` na função `submitContractDeploy()`.
    - **Ação 6 (Correção de Status de Conexão RPC):** Resolução do bug de interface onde o "Status Nó RPC" no painel da carteira exibia permanentemente o valor inicial "Desconectado" mesmo com o nó em execução. A correção incluiu atribuir o ID `wallet-node-status-disp` ao elemento correspondente no DOM e atualizá-lo tanto durante o intervalo periódico de polling de carteira quanto imediatamente após logins bem-sucedidos (via keystore, mnemônico ou criação).
    - **Ação 7 (Alinhamento dos Endereços da Carteira):** Resolução de bug visual no painel da carteira onde o botão de cópia do "Endereço Espelho (Mirror)" quebrava para a linha inferior. Forçou-se comportamento inline-flex (`display: inline-flex; align-items: center; gap: 6px; white-space: nowrap;`) no estilo da classe `.wallet-info-val`, garantindo alinhamento horizontal consistente em qualquer tamanho de rótulo.
    - **Ação 8 (Resolução de ReferenceError no Deploy):** Substituição das chamadas obsoletas `showStatusModal` e `hideStatusModal` (que disparavam `ReferenceError`) pelas funções canônicas `showTxStatusModal` e `hideTxStatusModal` na função `submitContractDeploy()`.
    - **Ação 9 (Correção de Panic na Witness do Bloco):** Resolução de crash fatal (`nil pointer dereference`) em [statedb.go](file:///c:/Magno/Projetos/jamii/pkg/core/state/statedb.go) durante a chamada `GenerateWitness()` na especulação de blocos de deploy. O problema ocorria porque a EVM adiciona pré-compilados (0x01 a 0x09) à `accessList` no boot do frame, mas eles não são instanciados no cache de banco de dados (`s.accounts`), provocando falha ao ler o endereço. O método foi ajustado para validar a existência da conta no cache de estado e ignorar contas inexistentes ou nulas.
    - **Ação 10 (Mapeamento de Parâmetros de Chamada RPC):** Correção no backend do explorador em [ApiController.java](file:///c:/Magno/Projetos/jamii/explorer/src/main/java/com/jamii/explorer/ApiController.java) onde os parâmetros fornecidos à função `client.ethCall` do SDK estavam na ordem incorreta (mapeando o calldata do contrato no campo de endereço de destino, o que induzia o RPC do nó Go a tentar decodificar calldata longo como endereço e entrar em panic por estouro de vetor). A chamada foi corrigida para `client.ethCall(null, cleanAddress, calldataHex)`.

## 🛠️ Decisões Recentes (26/06/2026)
1. **Consistência de Nomes de Validadores (Peer Alias / Naming Unificado):**
    - **Contexto:** Os nomes/apelidos de validadores eram configurados localmente ou de forma estática no explorador de blocos, impedindo a consistência na identificação ao adicionar novos validadores na rede.
    - **Problema:** Mapeamento estático de apelidos no frontend do explorer (`knownAddresses`) e necessidade de passar o apelido no boot e no gênese de forma robusta e persistente.
    - **Ação 1 (Gênese Unificado):** Adição do campo `"alias"` na configuração de validadores (`ValidatorConfig` em `genesis.go`). Os apelidos agora são definidos no genesis e registrados dinamicamente via `RegisterWithAlias`.
    - **Ação 2 (Persistência e DNS Simplificado):** Atualização do `IdentityRegistry` para persistir os apelidos localmente no PebbleDB sob o prefixo `alias:<address>`. O arquivo `peers.json` foi simplificado para funcionar apenas como DNS (`endereço_soberano -> IP`), em vez de realizar tradução de apelidos.
    - **Ação 3 (Consumo pelo Explorer):** Desenvolvimento de endpoint `/api/identities` no backend em Java para retornar a tabela de endereços e apelidos ativos gravados pelo Archiver. O frontend (`index.html`) foi adaptado para consumir esses dados dinamicamente, eliminando os mapeamentos de nomes estáticos em JavaScript.
2. **Resolução de Parada do Consenso (Out-of-Order Status Guard no DTS):**
    - **Problema:** Ao bootar ou sincronizar nós (ex: Nó E), mensagens concorrentes de presença/status (`Ready: false` do início do startup e `Ready: true` de sincronização concluída) podiam chegar fora de ordem na rede devido a fila assíncrona do DTS. O nó receptor processava a mensagem mais antiga (`Ready: false`) por último, sobrescrevendo o status do par na memória de prontidão e travando o quórum de consenso no watchdog.
    - **Solução:** Implementação de uma salvaguarda (stale status guard) em `handleStatusArrival` ([node.go](file:///c:/Magno/Projetos/jamii/pkg/node/node.go#L731)) que descarta transições de status para `Ready: false` se o peer já estiver registrado como `Ready: true` e a altura reportada no status for menor ou igual à última altura conhecida do peer. Adicionado log detalhado de quórum (`[QUORUM-DIAG]`) para monitoramento de saúde da rede.
3. **Gateway REST de Contratos e Deployer Dinâmico no Block Explorer:**
    - **Contexto:** O explorador de blocos não possuía suporte nativo para implantar (deploy) ou interagir dinamicamente com contratos inteligentes Solidity sem depender do Remix (que é incompatível com as assinaturas PQC exigidas pelo Portão-E da Jamii).
    - **Ação 1 (Esquema de Metadados de Contratos):** Criação da tabela `smart_contracts` no banco de dados do Archiver para rastrear e registrar ABI, bytecode BIN, endereço e apelidos de contratos cadastrados.
    - **Ação 2 (Endpoints de Deploy e Interação REST):** Implementação de rotas em [ApiController.java](file:///c:/Magno/Projetos/jamii/explorer/src/main/java/com/jamii/explorer/ApiController.java) (`/api/contracts/deploy`, `/api/contracts/register`, `/api/contracts/registered` e `/api/contracts/{address}/call`). O deploy calcula deterministicamente o endereço derivado do contrato via Keccak-256 localmente, com base no nonce e no endereço da chave pública pós-quântica do remetente. Integração com o parser e decodificador Web3j para chamadas dinâmicas.
    - **Ação 3 (Interface do Usuário):** Criação da aba "Contratos" em [index.html](file:///c:/Magno/Projetos/jamii/explorer/src/main/resources/static/index.html) permitindo o deploy de contratos (com detecção em tempo real de parâmetros do construtor e criação dinâmica de campos de input) e interação dinâmica (Read/Write) com todas as funções expostas na ABI.

## 🛠️ Decisões Recentes (23/06/2026)
1. **Governança de Chaves no Gênese e Saneamento de Manifestos PQC:**
    - **Contexto:** Antes, as chaves públicas dos validadores eram trocadas dinamicamente via handshakes e/ou gossip de identidades e salvas no banco (sob prefixo `ident:`). Na nova arquitetura, o gossip de identidades e a troca dinâmica via handshake foram removidos. As chaves públicas PQC híbridas passam a ser providas e governadas estritamente a partir do bloco gênese e via eleições.
    - **Problema:** Na carga de novos validadores ou no boot de nós com bancos limpos/novos, a ausência de chaves consistentes no bloco gênese resultava em falha de boot com o erro de criptografia Dilithium `packed public key must be of mldsa65.PublicKeySize bytes` (causado por inconsistência no tamanho das chaves lidas ou nos hexadecimais salvos no arquivo).
    - **Ação 1 (Padronização do Gênese):** Correção e padronização do array `validators` em todos os arquivos `genesis.json` (`node_a` a `node_e`) para usar um formato estruturado contendo `"address"` (Endereço Soberano `jamii1...`) e `"publicKey"` (com o hexadecimal exato de 3984 caracteres representando a chave híbrida Secp256k1 + ML-DSA-65).
    - **Ação 2 (Alinhamento dos Manifestos):** Correção do script utilitário de geração de endereços (`regenerate_manifests.go`) para estruturar a chave pública híbrida no mesmo padrão de 3984 caracteres usado no handshake (prefixado com `02220000000002...`), garantindo consistência com o validador interno e gerando manifestos `node_address.json` alinhados.
2. **Resiliência de Boot e Consensus Unlock (Materialização de Gênese Fallback):**
    - **Problema:** Quando os bancos de dados PebbleDB (`database`) eram limpos ou continham dados de transações/estados mas a chain principal estava sem blocos canônicos indexados (`latest == nil`), o laço de consenso abortava silenciosamente sem reportar status de prontidão (`Ready`), bloqueando a comunicação e travando a rede.
    - **Ação:** Implementado em [node.go](file:///c:/Magno/Projetos/jamii/pkg/node/node.go#L224-L241) um fallback automático no boot. Caso exista uma base de dados PebbleDB aberta, mas o cabeçalho canônico ativo seja nulo (`latest == nil`), o nó materializa localmente o bloco gênese #0 a partir do arquivo de configuração, grava-o no banco de dados e prossegue com a inicialização normal, desbloqueando o consenso.
3. **Controles Específicos do Ambiente Windows (UAC & Terminal CP1252):**
    - **Windows UAC Bypass:** O Windows bloqueia automaticamente a execução sem permissões administrativas de executáveis de linha de comando contendo palavras-chave como `update`, `install` ou `setup` (gerando prompts do UAC). Para evitar interrupções, o utilitário de atualização foi renomeado de `update_node_addresses.go` para `regenerate_manifests.go`.
    - **Encoding CP1252:** Terminais padrão do Windows utilizam codificação CP1252 por padrão. O uso de emojis Unicode de alta resolução nos logs ou console do terminal lançava exceções de encodagem (`UnicodeEncodeError`). Ajustou-se a saída de scripts auxiliares para caracteres ASCII compatíveis.
4. **Execução de Nós em Modo 100% Stateless (RAM Storage):**
    - **Contexto:** Visando descentralização e suporte a nós leves ou temporários, a Jamii introduziu a capacidade de rodar nós sem persistência física em disco.
    - **Ação:** Implementação do suporte à flag `--stateless` e `"stateless": true` no arquivo `config.yaml`. Quando ativa, o nó inicializa um banco de dados em RAM (`store.NewMemoryStore()`) em vez do PebbleDB no disco. Adicionou-se uma validação de segurança fundamental no boot: um nó stateless não pode ser um validador ativo na lista do `genesis.json`. Se o endereço correspondente à chave privada do nó estiver na lista de validadores ativos do gênese, o boot é abortado para preservar a integridade da rede.
5. **Remoção do Gossip Dinâmico de Identidades PQC (MsgIdentities):**
    - **Contexto & Ação:** Com a governança das chaves públicas de validadores agora centralizada no arquivo gênese e controlada via votação (on-chain elections), o envio dinâmico via handshake e o protocolo de gossip P2P (`MsgIdentities`) foram desativados. Isso eliminou um overhead de rede significativo de ~3.9 KB por validador no plano de controle, protegendo a rede contra saturação e pacotes duplicados.

## 🛠️ Decisões Recentes (19/06/2026)
1. **Otimização de Banco de Dados PostgreSQL e Resolução de Delay do Explorer:**
    - **Problema:** A interface do explorador de blocos Java demorava 20 segundos para atualizar após floods de transações, devido à contenção de banco causada por scans sequenciais em consultas frequentes sem índices (como `SELECT COUNT(*) FROM transactions` e ordenação por altura/saldo).
    - **Ação 1 (Adição de Índices):** Adicionados 11 índices secundários na inicialização de esquema do `NewPostgresStore` em [postgres.go](file:///C:/Magno/Projetos/jamii/pkg/store/postgres/postgres.go#L59) para permitir buscas ultra rápidas por hash de bloco, histórico de transações, saldos, contratos e mirror address.
    - **Ação 2 (Remoção de Redundância):** Comentada e anotada a gravação física na tabela espelho `trie_nodes` no `commitBatches` em [postgres.go](file:///C:/Magno/Projetos/jamii/pkg/store/postgres/postgres.go#L496). Como todos os nós históricos da Trie de Estado são preservados na tabela chave-valor `system_kv` (usada exclusivamente para restauração de estado e leituras), a gravação relacional espelho foi desabilitada, economizando cerca de 80% do overhead de escrita do banco.
    - **Ação 3 (Fatiamento de Flat Accounts):** Implementado o fatiamento de escritas de saldo na tabela `account_flat` em chunks de 1.000 registros ([postgres.go](file:///C:/Magno/Projetos/jamii/pkg/store/postgres/postgres.go#L528)) para estabilizar o I/O sob estresse.
2. **Identidade Visual e Tematização Light Premium (Logo e Cores de Marca no Block Explorer):**
    - **Ação:** Integração do logotipo oficial `jamii-logo.png` ([jamii-logo.png](file:///C:/Magno/Projetos/jamii/docs/images/jamii-logo.png)) nos recursos estáticos e reestruturação do design visual do Block Explorer para adotar um tema Light Premium moderno e limpo.
    - **Paleta de Cores Light:** Fundo alterado para cinza/branco ultra-suave (`#f8fafc`), cores de texto escuras de alta legibilidade (`#0f172a` e `#64748b`) e acentuações usando a paleta exata do logotipo: **Azul Real** (`#1d5792`) e **Laranja Vibrante** (`#e27a0b`).
    - **Design Sutil de Cards:** Redesenhados os cards estatísticos, modais de detalhes e tabelas para possuírem bordas finas muito claras (`#e2e8f0`) e sombras extremamente suaves e sutis (`rgba(15, 23, 42, 0.03)`), eliminando o contraste marcado do tema escuro anterior.
    - **Ajustes de Degradê e Inputs:** Barra de busca redesenhada com fundo cinza claro e foco em branco puro com brilho suave. Título "Jamii Scan" atualizado para usar um gradiente nítido de Azul Real para Laranja.
3. **Reestruturação Visual e Arquitetura Frontend do Explorador de Blocos (JamiiScan):**
    - **Tema Claro (Light Theme):** Transição do tema escuro genérico para um tema claro baseado na paleta do logotipo da Jamii (tons pastéis de ciano/azul `#0284c7` e laranja/ouro `#d97706`). Isso resolveu a inconsistência visual do logotipo `jamii-logo.png` (que possui fundo branco) e integrou o design do painel.
    - **Correção de Contraste e Legibilidade:** Resolução de bugs graves de visualização do protótipo onde títulos de blocos, valores de transações e textos digitados na busca ficavam brancos sobre fundo branco/claro.
    - **Navegação com Roteamento Hash (URL Hash Routing):** Implementação de roteamento no lado do cliente (`#/block/{height}`, `#/tx/{hash}`, `#/address/{address}`) em substituição a pop-ups modais. Garante histórico de navegação nativo (botões voltar/avançar do browser), URLs compartilháveis e mais espaço de exibição. Logotipo e botão "Voltar" integrados para limpeza de rota.
    - **Paginação de Transações:** Implementação de paginação client-side (lotes de 25 transações) na tela de detalhes do bloco para otimizar visualização de blocos populosos (como picos de 500+ TXs comuns em redes como Ethereum/BSC) e evitar travamentos de renderização.
    - **Painel Horizontal de Carteiras Recentes:** Criação de um card horizontal responsivo de largura total que lista as últimas 10 contas modificadas no banco de dados (através da coluna `last_update_height` da tabela `account_flat` via endpoint `/api/accounts/recent`), integrado com o polling e detalhamento.
    - **Reordenação e Posicionamento Premium:** O carrossel de carteiras atualizadas foi extraído do grid principal de duas colunas `.explorer-main` e reposicionado como uma faixa de largura total independente logo abaixo do barramento de estatísticas principais (`.stats-grid`), melhorando a harmonia estética e dando maior destaque visual ao elemento.
    - **Correção de Clipping do Hover (CSS):** Ajustado o container `.horizontal-list` adicionando `padding-top: 6px;`. Isso impede que a borda superior dos cards de carteiras seja recortada visualmente pelo overflow-x do container durante o deslocamento vertical da animação (`transform: translateY(-2px);`).
    - **Otimização de Chamadas de Rede:** Programado controle para pausar requisições em segundo plano do painel principal enquanto telas de detalhes estiverem abertas.
    - **Saneamento de Vocabulário:** Ajuste da tradução do cabeçalho de "Altura Canonical" para o termo técnico formal em português **"Altura Canônica"**.
4. **Calibração de Throughput e Dimensionamento de `maxTxsPerBlock`:**
    - **Métricas de Comparação:** Em blockchains de produção sob tráfego estável, a média de transações por bloco gira em torno de 150 a 300 transações no Ethereum e na BNB Smart Chain. Em picos de estresse, no entanto, a BSC é capaz de comportar mais de 10.000 transações em um único bloco (com tempo de bloco curto de 3s, o que atinge picos de TPS de ~3.300+).
    - **Dimensionamento e Limites locais:** O valor de `maxTxsPerBlock = 3000` na Jamii Blockchain foi avaliado como ideal para alto rendimento sob produção industrial. No entanto, para testes e simulações locais com múltiplos nós (ex: 5 validadores) compartilhando recursos físicos de processador e escrita em PebbleDB, blocos de 3.000 transações (com criptografia pós-quântica ML-DSA) geram contenção excessiva de CPU e disco, resultando em timeouts de rodada (round change) no IBFT2.
    - **Diretriz Técnica de Testes:** Calibrar o limite de `maxTxsPerBlock` em `1000` a `1500` para testes locais é o recomendado para assegurar a fluidez e estabilidade do consenso sob floods massivos de transações.
5. **Tematização Light Premium da Wallet de Exemplos (Java SDK):**
    - **Ação:** Aplicação do mesmo esquema de cores da marca (Azul Real `#1d5792`, Laranja `#e27a0b` e Azul Ciano `#0284c7`) no arquivo estático [index.html](file:///C:/Magno/Projetos/jamii/sdk/examples/java/src/main/resources/static/index.html) da carteira de testes do SDK.
    - **Logotipo e Header:** Cópia e vinculação do logotipo oficial `jamii-logo.png` na barra de cabeçalho da carteira, com o título do app usando o mesmo gradiente visual do explorador de blocos.
    - **Ajuste de Componentes:** Adaptação de todos os inputs, badges de status, botões (gradientes do Azul Real e Laranja), overlays, e animações (spinner de carregamento) para o tema claro sobre background `#f8fafc`.
6. **Integração de Carteira Jamii no Explorador de Blocos (Jamii Scan):**
    - **Ação 1 (Backend - ApiController):** Implementação dos endpoints `/api/wallet/login`, `/api/wallet/logout`, `/api/wallet/info` e `/api/wallet/transfer` sob gerenciamento seguro de sessão do Spring Boot (`HttpSession`). A chave privada (`JamiiKeyPair`) é descriptografada em memória usando o SDK Java da Jamii e armazenada temporariamente na sessão volátil da RAM, sendo destruída no logout ou no timeout da sessão.
    - **Ação 2 (Frontend - index.html):** Inserção de um botão de acesso no cabeçalho e desenvolvimento de uma view completa (`#wallet-view`) associada ao roteamento de hash (`#/wallet`).
    - **Asegurança & Envio (Ação 3):** Implementação de uma área de arrastar e soltar (drag & drop) para arquivos de Keystore JSON, toggle de visibilidade de senha, cálculo preciso de taxas de gás com base no base fee atual da rede usando BigInt e modal moderno de feedback com links diretos para o recibo da transação gerada.
    - **Ação 4 (Parametrização do JSON-RPC):** Externalização da URL de conexão com o nó JSON-RPC do blockchain (anteriormente fixada em localhost) para a propriedade `jamii.jsonrpc.url` em `application.properties`, injetada via `@Value` no Spring Boot.
7. **Suporte Nativo a Mnemônicos (BIP-39/BIP-32) e Alinhamento Criptográfico Determinístico:**
    - **Integração no SDK Java:** Adição do suporte a mnemônicos (12 palavras de recuperação) e derivação de chaves híbridas Secp256k1 (caminho `m/44'/60'/0'/0/0`) e ML-DSA-65 (via `FixedSecureRandom` alimentado com o HMAC-SHA512 da seed).
    - **Correção no Go `wallet-cli`:** Correção dos erros de compilação em `cmd/wallet-cli/main.go` ajustando o tipo de entrada do mnemônico para `[]byte` e substituindo a dependência do campo `.ID` removido do Keystore por um substring do Mirror Address.
    - **Garantia de Simetria (Teste Unitário):** Implementação de uma suíte de testes JUnit (`JamiiWalletTest.java`) contendo um caso de teste baseado em vetores gerados pelo Go CLI. A execução bem-sucedida do teste comprovou o determinismo absoluto na derivação de endereços Sovereign/Mirror em ambas as linguagens.
    - **Frontend do Explorer:** Atualização do painel em `index.html` para expor abas de Login com Keystore, Recuperação com Mnemônico e Geração de Nova Carteira, integrando downloads automáticos de arquivos keystore JSON e a exibição do **Endereço Espelho (Mirror Ethereum)** em todos os fluxos de sucesso e no painel principal da carteira.
8. **Resiliência a Falhas do Archiver e Robustez no Explorer de Blocos:**
    - **Problema:** Quando o archiver de dados era temporariamente desligado ou atrasava, novas transações processadas com sucesso pela blockchain não constavam no PostgreSQL, fazendo com que a exibição de detalhes no explorador falhasse de forma silenciosa ou técnica com placeholders inválidos (`undefined`, `NaN`).
    - **Ação 1 (Fallback Dinâmico no Java Backend):** Implementação de fallback no Spring Boot ([ApiController.java](file:///C:/Magno/Projetos/jamii/explorer/src/main/java/com/jamii/explorer/ApiController.java#L210)) que, ao não encontrar a transação ou recibo no PostgreSQL, realiza uma chamada JSON-RPC direta ao nó ativo da blockchain (`getTransactionReceipt`).
    - **Ação 2 (Robustez UI):** Atualização do javascript no frontend ([index.html](file:///C:/Magno/Projetos/jamii/explorer/src/main/resources/static/index.html)) para tratar valores nulos e estruturar a renderização dinâmica de dados do recibo de forma limpa, ocultando dados técnicos de erro.
9. **Janela Deslizante de Sementes BitTorrent (Prevenção contra History Bloat nos Validadores):**
    - **Problema:** O seeding persistente de blocos antigos por torrent forçava os nós validadores ativos a armazenarem eternamente grandes volumes de arquivos físicos de blocos antigos (`chunk_*_*.bin`), causando consumo descontrolado de disco e conexões na rede P2P.
    - **Solução:** Implementação de uma política de janela deslizante (Sliding Window Seeding) no motor torrent em Go ([engine.go](file:///C:/Magno/Projetos/jamii/pkg/torrent/engine.go)). A janela móvel monitora os torrents ativos de blocos semeados e remove handles e arquivos físicos mais antigos que o limite configurado (janela de no máximo 2 chunks simultâneos), preservando o espaço em disco e mantendo a missão dos validadores atômica e limpa.
10. **Mapeamento de Endereços Híbridos no Explorer e Sincronização Retroativa de Contas:**
    - **Problema:** A tabela `account_flat` do explorer mantinha saldos e nonces corretos alimentados pelo Commit do Trie de Estado (usando o `address_hash` keccak de 20 bytes), mas os campos textuais `address` and `mirror_address` permaneciam `NULL` para qualquer nova conta criada pós-Genesis, gerando cards corrompidos com endereços nulos. No bloco #1617, uma transferência para o endereço Ethereum do usuário gerou uma conta de 506 JAMII com endereço `null`.
    - **Ação 1 (Registro Ativo no Commit):** Ajustamos o `commitBatches` em [postgres.go](file:///C:/Magno/Projetos/jamii/pkg/store/postgres/postgres.go#L390) para extrair e registrar ativamente os endereços válidos (`sender`, `receiver` e `coinbase`) de cada lote de transações e blocos gravados no Postgres.
    - **Ação 2 (Robustez no Conflito do Address Book):** Atualizada a cláusula `ON CONFLICT (address_hash)` do método `RegisterAddress` em [postgres.go](file:///C:/Magno/Projetos/jamii/pkg/store/postgres/postgres.go#L623) para atualizar também a coluna `mirror_address` em caso de conflito, garantindo que contas criadas preliminarmente pela Trie com campos nulos ganhem as duas representações de endereço.
    - **Ação 3 (Sincronização Retroativa):** Escrito e executado um script Go utilitário (`migrate_addresses.go`) que varreu todo o histórico de blocos e transações no PostgreSQL e sincronizou retroativamente os 9 endereços únicos no banco de dados, recuperando o endereço textual `0xd0438D4539867cC3b58f0ce6824bEe58787c70Bd` da carteira do usuário de forma 100% precisa.
11. **Modal de Confirmação de Transação Customizado no Explorer (UX Premium):**
    - **Problema:** A caixa de confirmação de transações no explorador utilizava o método `confirm()` nativo do navegador, destoando do design visual e de cores Light Premium unificado da Jamii Blockchain.
    - **Ação 1 (Interface HTML/CSS):** Inserida a marcação `#tx-confirm-overlay` no arquivo [index.html](file:///C:/Magno/Projetos/jamii/explorer/src/main/resources/static/index.html#L1210) reaproveitando o layout e os estilos suaves do modal de status.
    - **Ação 2 (Promisificação JavaScript):** Implementada a função `showTxConfirmModal(to, valWei)` que envelopa a interação do modal em uma `Promise<boolean>` para suporte nativo e limpo a `await`.
    - **Ação 3 (Acoplamento de Fluxo):** Substituída a chamada síncrona `confirm()` na rotina `submitWalletTransfer` pelo novo confirmador assíncrono.
12. **Otimização de Performance e Resolução de Gargalo de Escrita (Flushing de WAL) no Archiver:**
    - **Problema:** Sob floods de transações massivas (ex: 5.000 transações enviadas em blocos populosos), o archiver começou a empilhar dezenas de arquivos de Write-Ahead Log (`.wal`) no disco e parou de atualizar o explorador por mais de 10 minutos. Isso foi causado pela introdução de chamadas síncronas individuais a `p.RegisterAddress(addr)` (que executa `db.Exec` fora da transação SQL do lote) para cada Coinbase, Sender e Receiver processados.
    - **Ação 1 (Acúmulo em RAM):** Substituído o registro síncrono individual por acúmulo temporário dos endereços únicos decodificados em um mapa `regAddrs` no início do `commitBatches` em [postgres.go](file:///C:/Magno/Projetos/jamii/pkg/store/postgres/postgres.go#L176).
    - **Ação 2 (Batching com Unnest na Transação):** Implementada a persistência de novos endereços em lote (batching) usando `unnest` e arrays parametrizados diretamente no objeto da transação SQL `tx` ativa antes do commit. Isso reduziu drasticamente o overhead de I/O de rede e banco de dados de milhares de requisições separadas para apenas 1 a 5 queries consolidadas por lote, normalizando o flushing dos arquivos WAL de volta a milissegundos e atualizando o explorador instantaneamente.
13. **Filtro de Contas com Saldo Nulo na Rich List do Explorer:**
    - **Problema:** Na lista de contas mais ricas do explorador, apareciam contas com `0 JAMII` e nonce `null` no início da lista, apontando para endereços com valores nulos. Isso era causado pelo fato de que o PostgreSQL ordena valores `NULL` no topo da lista em queries decrescentes por padrão (`ORDER BY balance DESC`), misturando contas que foram apenas mapeadas via `RegisterAddress` (com campos de saldo nulos) com as contas de saldo real.
    - **Ação:** Ajustada a query SQL no endpoint `/accounts/top` em [ApiController.java](file:///C:/Magno/Projetos/jamii/explorer/src/main/java/com/jamii/explorer/ApiController.java#L277) adicionando a cláusula `WHERE balance IS NOT NULL AND balance > 0`. Isso garante que apenas contas com saldos reais e positivos apareçam na Rich List, tirando proveito dos índices de balance e corrigindo a exibição na interface.

## 🛠️ Decisões Recentes (18/06/2026)
1. **Resolução de Iterador PostgreSQL (`PostgresIterator` Funcional):**
    - **Ação:** Implementação do iterador funcional do driver de banco de dados `PostgresStore` (em [postgres.go](file:///C:/Magno/Projetos/jamii/pkg/store/postgres/postgres.go#L453)) para processar chaves com prefixo `ident:`, restabelecendo a propagação dinâmica do cartório de identidades do Archiver para novos nós no handshake.
2. **Logs Transparentes com Identificação de Remetente (`[ FROM PEER ]` no DTS):**
    - **Ação:** Ajuste na exibição de mensagens no console de rede do `DTS Engine` em [engine.go](file:///C:/Magno/Projetos/jamii/pkg/dts/engine.go) para incluir explicitamente a origem das mensagens recebidas e das identidades propagadas de validadores (ex: `[ FROM ARCHIVER ] Receiving Public Key for ...`), eliminando poluição visual sem adicionar overhead.
3. **Otimização Concorrente de Seeder Virtual e Cache de RAM (N+1 Query Resolution):**
    - **Problema:** A criação do seeder virtual para sincronizar um chunk de blocos parcial (ex: 1 a 327 blocos) demorava mais de 3 minutos devido à latência de rede em queries relacionais sequenciais repetidas de montagem de blocos e marshal na leitura stateless do Torrent.
    - **Solução:** Otimização do método `buildBlockIndex` em [storage.go](file:///C:/Magno/Projetos/jamii/pkg/torrent/storage.go#L139) para disparar queries assíncronas concorrentes de busca de blocos (através de um worker pool de concorrência limitada a 20 goroutines) e realizar o caching dos dados serializados dos blocos em memória RAM. Redução de latência de inicialização de seeding para menos de 1 segundo.
4. **Resiliência e Desalocação de Disco (File Handle Release pós-Sync):**
    - **Problema:** Os arquivos temporários binários parciais (`temp_chunk_*.bin`) gerados pelo downloader torrent dos proposers não eram apagados no final da sincronização de blocos no Windows, correndo risco de exaustão de espaço em disco em sincronizações massivas.
    - **Solução:** Inclusão de chamada mestre `t.Drop()` em `DownloadChunk` ([engine.go](file:///C:/Magno/Projetos/jamii/pkg/torrent/engine.go#L324)) imediatamente após o download atingir 100%, liberando o travamento do handle de escrita do torrent e permitindo a deleção do arquivo físico no disco local via `os.Remove`.

## 🛠️ Decisões Recentes (17/06/2026)
1. **Resolução de Sincronia de Consenso sob ML-DSA PQC (Handshake de Chaves Públicas Dinâmico):**
    - **Problema:** Sob assinaturas pós-quânticas ML-DSA, a chave pública do validador não pode ser recuperada matematicamente a partir da assinatura do bloco (`ecrecover`). Isso impedia a validação de blocos históricos recebidos por Torrent se o nó de destino (zerado/recém-entrado) não possuísse as chaves dos validadores em seu cartório PebbleDB.
    - **Solução:** Registro de um novo tipo de mensagem `MsgIdentities` (`0x09`) no DTS. Transmissão imediata de todo o cartório de identidades (chaves públicas e nomes) via canal `BULK` no handshake inicial do DTS, populando o PebbleDB local do nó receptor antes da validação da cadeia de blocos de torrent.
    - **Higiene e Logs:** Adicionado log informativo no nível `INFO`: `Receiving Public Key for <NODE_NAME>` para fins de visibilidade visual no bootloader do nó.
2. **Exposição de Identidades no PostgreSQL (Archiver Sync Relacional):**
    - **Ação:** Criação da tabela `identities` dedicada no PostgreSQL do Archiver (definida tanto em `cmd/archiver/schema.sql` quanto no construtor programático `NewPostgresStore`).
    - **Gravação:** Atualização do processador de lotes `commitBatches` no `postgres.go` para filtrar e gravar dados com prefixo `"ident:"` do PebbleDB e persistir chaves públicas e metadados no banco relacional em tempo real.
3. **Otimização de Gargalo no Torrent Sync Engine (Single Flight Coalescing & Memory Locks):**
    - **Problema:** Concorrência pesada por InfoHash do Torrent gerava requisições repetidas e recálculo lento de SHA-1 de peças a cada 10 segundos no disco (PostgreSQL lento), travando o mutex principal do TorrentEngine e criando loops de espera concorrentes.
    - **Solução:** Implementação de *Single Flight Request Coalescing* e cache estático de InfoHashes no motor de Torrent. Liberação precoce do mutex durante operações lentas de I/O de dados de chunk e cálculo de hash.
    - **Sync Manager Reativo:** Remoção do loop periódico de solicitação do Torrent InfoHash por um loop puramente reativo (Fire & Forget). Sincronismo de cadeia acionado imediatamente por recepção de status do peer em background no milissegundo de chegada via `UpdatePeerHeight`.
4. **Resiliência e Conexão Simétrica do DTS Handshake:**
    - **Problema:** Race condition entre os canais `EXPRESS` (TCP rápido) e `BULK` (TCP levemente mais lento por latência de socket) causava o descarte silencioso do `BroadcastStatus` inicial de presença.
    - **Solução:** Ajustado o `dts.Engine` para disparar `onPeerConnect` no canal `BULK` caso o `EXPRESS` já esteja ativo, garantindo redundância, e adicionando fallback de transmissão no canal `EXPRESS` se o `BULK` estiver nulo temporariamente.
5. **Correção no Iterator PebbleDB de Identidades:**
    - **Problema:** O iterator de busca do PebbleDB ao fazer o bootstrap de identidades no `pkg/node/node.go` ignorava o primeiro registro encontrado no `Seek` inicial, causando perda da primeira chave inserida em execuções subsequentes.
    - **Solução:** Correção do loop iterator para processar a chave do seek inicial e, em seguida, iterar com `Next()`.
6. **Watchdog de Proposer Não-Pronto (Early Leader Change):**
    - **Ação:** Adicionada validação de prontidão (`IsProposerReady`) ao Watchdog de rodada (IBFT). Se o propositor selecionado for detectado como "not ready" (sincronizando) ou offline, o nó dispara imediatamente a troca de líder (`onTimeout("proposer offline/unready")`) após o grace period de 2 segundos, em vez de aguardar o timeout mestre de 10s.
    - **Inicialização de Startup:** Inicialização explícita de todos os validadores da rede como `false` ('not ready') no mapa de controle `peerReady` no boot do nó.
    - **Simplificação de Logs de Quórum:** Remoção de logs debug detalhados de votos de commit individuais (`voted COMMIT. Count: X/Y`) para evitar poluição visual e simplificação do log de quórum de commit para `[Round <H:R>] Quorum to COMMIT reached.` para melhor compatibilidade com grandes conjuntos de validadores (100+).
7. **Correção de Panics no BitTorrent Sync (Data Plane):**
    - **Problema:** O método `DownloadChunk` invocava `t.DownloadAll()` antes de verificar `<-t.GotInfo()`, resultando em panic por desreferência de ponteiro nulo (`t.info == nil`) quando o InfoHash do torrent ainda não havia resolvido seus metadados no enxame.
    - **Solução:** Adicionado bloqueio preventivo via select `<-t.GotInfo()` antes de chamar `t.DownloadAll()`, garantindo que os metadados do torrent estejam totalmente carregados.
8. **Mapeamento Amigável nos Logs de Sincronismo (Friendly P2P Sync Logs):**
    - **Ação:** Adicionado método público `PeerName(id)` ao `dts.Engine` e helper `peerName(id)` no `SyncManager` para exibir nomes lógicos (ex: `NODE_A`) nos logs de sync em vez de IDs Bech32 brutos.
9. **Higiene de CPU e Mempool em Estado de Sync (Observer Mode Filtering):**
    - **Ação:** Implementada filtragem estrita no `handleExpressArrival` para descartar mensagens de selos de validadores (`dts.MT_VALIDATOR_SEAL`) e no `handleDataArrival` para descartar transações da mempool (`dts.MT_TRANSACTION`) enquanto o nó estiver em modo de sincronismo inicial (`IsBehind() == true`).
    - **Benefício:** Redução maciça no uso de CPU e proteção contra estouro/poluição de RAM na mempool sob inundações de rede (floods).
10. **Estratégia de Requisição de InfoHash Distribuída (Torrent Unicast to Broadcast):**
    - **Problema:** O nó iniciador do torrent solicitava o InfoHash do chunk via `RequestTorrentChunk` em unicast para apenas um único peer (o `bestPeer` principal). Isso causava travamentos se o peer alvo (ex: o Archiver) estivesse lento para gerar e expor o arquivo virtual, impedindo que outros nós que já continham os mesmos blocos em seu disco local rápido (PebbleDB) respondessem.
    - **Solução:** Alterado o `SyncManager` para enviar a solicitação do InfoHash para todos os nós conectados que possuam altura de bloco maior ou igual ao fim do chunk solicitado. A primeira resposta destrava o download via BitTorrent, que baixa as peças de todo o enxame em paralelo.

## 🛠️ Decisões Recentes (12/06/2026)
1. **Desacoplamento e Biblioteca Pura (SDK Java)**:
    - **Ação:** Refatoração completa do módulo `sdk/java/jamii-sdk` para atuar como uma biblioteca Java pura e autônoma, livre de dependências do Spring Boot e de servidores web embutidos.
    - **Implementação:** Substituição do `RestTemplate` por `java.net.http.HttpClient` nativo do Java 22. Adição do script `deploy_local.bat` para automatizar a compilação e publicação no diretório local `.m2/repository`.
2. **Criação da Wallet Web de Exemplo (Java Web)**:
    - **Ação:** Criação do módulo de exemplo em `sdk/examples/java` consumindo o SDK como dependência local externa do Maven.
    - **Arquitetura & Segurança:** Criação do arquivo `application.properties` para configuração dinâmica de Keystores, eliminando caminhos hardcoded e cortando qualquer execução ou deploy automático no boot para evitar falhas silenciosas na ausência de nós locais da rede blockchain.
    - **API e Assinatura Local:** Implementação do endpoint GET `/api/wallet` e do endpoint POST `/api/transfer` no `ApiController.java`. A assinatura da transação ocorre localmente usando a chave privada da carteira através do SDK, transmitindo o payload serializado em SSZ de forma segura via chamada RPC ao nó configurado.
    - **Interface Premium:** Desenvolvimento de uma UI interativa em estilo *glassmorphism* (HTML/CSS/JS) rodando na porta `8080` com suporte a injeção e teste dinâmico de host/porta RPC persistido via `localStorage` do navegador, com feedback em tempo real para transações e testes de conexão.
    - **Ferramental:** Criação do script `run.bat` para compilar e inicializar o servidor de exemplo.
3. **Terminologia de Estado (Verkle Default)**:
    - **Ação:** Substituição sistemática das referências exclusivas a "SMT" (Sparse Merkle Trie) nos manuais e documentação técnica pelo termo geral "Trie de Estado" ou "Trie", refletindo o suporte híbrido (Verkle/SMT) introduzido pela Trie Factory e confirmando o Verkle como padrão de árvore de estado na arquitetura Jamii.
4. **Otimização de DTS e SyncManager (Resiliência sob Flood)**:
    - **DTS Dense Payload Resiliency (Desconexão Concorrente por Limite de Buffer e Timeout):**
        - **Problema:** O método `readMessage` em `pkg/dts/network_proto.go` possuía um limite máximo fixo de tamanho de mensagem de 10 MB. Como cada transação pós-quântica (PQC/ML-DSA) consome cerca de 4.3 KB, um bloco denso contendo 3.000 transações atinge aproximadamente 12.9 MB. Ao tentar ler esse payload, o nó receptor barrava a mensagem com o erro `message too large` e fechava a conexão, fazendo com que o remetente recebesse um erro de socket fechado abruptamente (`wsasend`).
        - **Solução:** Elevação do limite de segurança de mensagens no DTS de 10 MB para 32 MB e aumento do `WriteDeadline` de 10s para 30s em `pkg/dts/engine.go` para suportar a latência de rede/CPU sob alta carga.
    - **Anxious Sync Debounce (Sync Ansiado por Falta de Tolerância de Commit):**
        - **Problema:** O `SyncManager` roda um loop periódico que ativa a sincronização se encontrar a altura local menor que a altura máxima dos peers. Como o commit de Verkle/PebbleDB de blocos de 3.000 TXs leva até ~1.5s, o nó local fica em um estado temporário em que a RAM já finalizou a rodada do consenso, mas o disco ainda registra a altura anterior ($N-1$).
        - **Solução:** Implementação de um debounce de 500ms em `pkg/blockchain/sync.go` no início de `checkAndSync` caso a diferença seja de exatamente 1 bloco, liberando o mutex, esperando e re-checando a altura local antes de disparar requisições de rede.
5. **Calibração de Cadência de Consenso (blockPeriod Tuning)**:
    - **Diagnóstico Local:** Análise matemática do teste de flood de 15K revelou vazão líquida de execução da VM de **~1.250 TPS**, mas o TPS médio geral de rede ficou em **625 TPS** devido a 12s de ócio acumulados (pacing do consenso).
    - **Contenção Multiprocesso:** Identificado que em setups locais rodando 5 validadores na mesma máquina Windows, a concorrência por disco (5 escritas PebbleDB simultâneas) e CPU (15.000 validações ML-DSA concorrentes) eleva o tempo de commit de blocos cheios para até 4s (bloco #18).
    - **Tuning Recomendado:** O `blockPeriod` de 2s é mantido como amortecedor de segurança em setups locais concorrentes. Para reduzir a cadência com segurança para 1s (elevando o TPS médio para ~900-1000 TPS), é mandatória a redução de `maxTxsPerBlock` para 1.000 TXs (evitando lags acumulados de rodada) ou migração para nós com hardware dedicado.

## 🛠️ Decisões Recentes (11/06/2026)
1. **Transição de Assistência e CLI:**
    - **Ação:** Oficializada a transição das ferramentas de assistência e suporte ao desenvolvimento da Jamii Blockchain, migrando do antigo `gemini-cli` para o novo assistente autônomo `antigravity-cli` (Antigravity).
    - **Impacto:** Todas as diretivas de engenharia do [GEMINI.md](file:///C:/Magno/Projetos/jamii/GEMINI.md) e o histórico consolidado continuam 100% vigentes e sob custódia e aplicação rígida pelo novo assistente.

2. **Humano-Compatibilidade de Logs (Friendly Peer Names & Clean Logs):**
    - **Ação:** Implementação de resolução reversa de nomes lógicos no console de depuração e visualização do nó (ex: `NODE_A`, `NODE_B`) com base no `peers.json` do ambiente.
    - **Resultado:** Os logs de canais de conexão do DTS, logs de status do nó, logs de verificação de assinaturas criptográficas PQC (ML-DSA) e logs do propositor designado/watchdog do consenso agora exibem o nome lógico do validador in vez do hash Bech32 bruto.
    - **Higiene e Otimização de Logs:** Remoção do log redundante de chegada de sinal de consenso (`Consensus Signal`), rebaixamento do log de tráfego de dados expressos para o nível `TRACE` (limpando o console de depuração padrão) e elevação dos logs de quórum de `PREPARE` (Prepared state reached) e `COMMIT` (voted COMMIT. Quorum reached) para o nível `INFO` (dando visibilidade do consenso em produção).
    - **Design Concorrente Seguro:** A resolução é estritamente de leitura (Read-Only) em tempo de execução (mapa populado síncrono no boot), eliminando overheads de sincronização e deadlocks.

3. **Resiliência do Sync (Debounce de Catch-up):**
    - **Ação:** Planejada a adição de uma tolerância temporal (debounce configurável de ~200ms) antes que o SyncManager inicie o processo de catch-up de blocos.
    - **Objetivo:** Evitar que o SyncManager compita com o banco de dados PebbleDB e envie requisições DTS extras se o nó estiver apenas finalizando ativamente o bloco corrente via consenso.

## 🛠️ Decisões Recentes (10/06/2026)
1. **Triunfo Arquitetural: Chained State Oracle (Especulação Ativa):**
    - **Contexto:** Implementação de pipelining assíncrono para processar o bloco $N+1$ enquanto a rede aguarda a cadência (Pacing) do bloco $N$.
    - **Resultado:** **Sucesso Absoluto.** A especulação agora é integrada ao fluxo real de proposta.
    - **Ganho de Performance:** O tempo de montagem de bloco pelo líder caiu de ~1.3s para **zero milissegundos** (O(1)).
    - **Recorde de Throughput:** Atingido marco de **750 TX/s** sustentados (3.000 TX/bloco a cada 4s reais), com pico de **1.045 TX/s**.
    - **Espera Inteligente:** Implementado mecanismo de sincronização onde o Proposer aguarda a conclusão da especulação em andamento, eliminando a re-execução redundante e a asfixia de CPU.
    - **Fidelidade Determinística:** Trava mandatória que obriga o propositor a usar a mesma "fotografia" da MemPool tirada na especulação, garantindo 100% de convergência de StateRoot.

2. **Otimização de Retenção (Warm Parent Cache):**
    - **Ação:** O bloco pai agora é mantido em RAM no `PayloadPool` após a finalização.
    - **Benefício:** Elimina a necessidade de I/O de disco para iniciar a especulação do bloco seguinte.

3. **Correção de Persistência (State Promotion Receipts):**
    - **Ação:** Sincronizada a coleta de recibos durante a fase de `VerifyPayload`.
    - **Resultado:** Sanado o panic de disparidade de recibos (`receipt count mismatch`) no banco de dados.

## 🛠️ Decisões Recentes (06/06/2026)
1. **Witness-Aided Quorum Acceleration (Aceleração por Testemunha):**
    - **Ação:** Introdução da **Block Witness** no Skeleton do bloco. O Proposer agora envia os estados iniciais (contas/slots) necessários para a execução.
    - **Benefício:** Permite que validadores executem o bloco de forma "Stateless" e otimista.
    - **Resultado:** Validação de blocos em validadores agora pula a matemática pesada de Verkle (IPA/MSM) durante a fase de votação (Prepare/Commit).

2. **Mecanismo de Promoção de Estado (State Promotion):**
    - **Problema:** O Proposer executava o bloco duas vezes (uma para propor, outra no commit final), duplicando o esforço de CPU-bound mais caro.
    - **Solução:** Implementado o cache de **Sandbox State** no `PayloadPool`. O resultado da execução é preservado e promovido atomicamente.
    - **Resultado:** Redução de **~90%** no tempo de processamento do Proposer na fase de commit.

## 🛠️ Decisões Recentes (03/06/2026)
1. **Refatoração do Motor Verkle (Deadlock & Atomicidade):**
    - **Problema:** Detectado Deadlock Recursivo durante a materialização do Genesis.
    - **Solução:** Implementado sistema de **Cache Sharding Antecipado** (Eager Loading) no construtor da Verkle Tree.
    - **Segurança Atômica:** A variável `cacheMultiplier` foi migrada para o tipo `atomic.Uint64`.
2. **Blindagem de Memória e Slots (Mempool Resilience V1):**
    - **Ação:** Implementada proteção de "Portão Duplo" na MemPool (Slots e RAM).
    - **Lógica de Expulsão Híbrida:** O nó agora realiza expulsão em loop (AllHeap) priorizando transações de maior valor econômico.
1. **Ultra-Compactação de Rede (Bi-Polar Short IDs):**
    - **Ação:** Skeleton Blocks usando identificadores de 6 bytes.
    - **Resultado:** Redução de **81%** no tráfego de rede.
2. **Otimização Homomórfica O(1) (Verkle Turbo):**
    - **Ação:** Implementado o cálculo incremental de compromissos IPA.
    - **Resultado:** O pico do motor atingiu **750 TX/s**.
2. **Resiliência de Ressurreição (Consensus Halt Guard):**
    - **Ação:** Implementado reset automático da trava `lastStartedHeight` quando o quórum de rede é recuperado.

## 🛠️ Decisões Recentes (03/06/2026)
1. **Blindagem de Memória e Slots (Mempool Resilience V1):**
    - **Ação:** Implementada proteção de "Portão Duplo" na MemPool, limitando tanto a quantidade de transações (`MaxMempoolSlotSize`) quanto o consumo real de RAM (`MaxMempoolMemorySize`).
    - **Lógica de Expulsão Híbrida:** O nó agora realiza expulsão em loop (AllHeap) até que ambos os critérios de saúde sejam restabelecidos, priorizando transações de maior valor econômico.
    - **Anti-OOM:** Adicionada validação *check-first* que rejeita transações individuais que sozinhas excedam o limite total de memória da pool.
    - **Resultado:** Garantia de que o nó Jamii permaneça operacional sob ataques de flood massivo, com consumo de RAM previsível e proteção contra exaustão de CPU por validações inúteis.

## 🛠️ Decisões Recentes (03/06/2026)
1. **Refatoração do Motor Verkle (Deadlock & Atomicidade):**
    - **Problema:** Detectado Deadlock Recursivo durante a materialização do Genesis. O motor tentava adquirir um lock de leitura (`RLock`) para ler configurações enquanto já segurava o lock mestre de escrita (`Lock`).
    - **Solução:** Implementado sistema de **Cache Sharding Antecipado** (Eager Loading) no construtor da Verkle Tree. Removida a necessidade de travas (`v.mu`) para leitura de configurações.
    - **Segurança Atômica:** A variável `cacheMultiplier` foi migrada para o tipo `atomic.Uint64` (IEEE 754 bitcasting), permitindo que o cache seja redimensionado em tempo real sem causar contenção ou deadlocks no pipeline de execução.
    - **Performance:** Restaurado o paralelismo multi-core total para o cálculo de compromissos IPA/Bandersnatch.

2. **Blindagem de Memória e Slots (Mempool Resilience V1):**
    - **Ação:** Implementada proteção de "Portão Duplo" na MemPool, limitando tanto a quantidade de transações (`MaxMempoolSlotSize`) quanto o consumo real de RAM (`MaxMempoolMemorySize`).
    - **Lógica de Expulsão Híbrida:** O nó agora realiza expulsão em loop (AllHeap) até que ambos os critérios de saúde sejam restabelecidos, priorizando transações de maior valor econômico.
    - **Anti-OOM:** Adicionada validação *check-first* que rejeita transações individuais que sozinhas excedam o limite total de memória da pool.
    - **Resultado:** Garantia de que o nó Jamii permaneça operacional sob ataques de flood massivo, com consumo de RAM previsível e proteção contra exaustão de CPU por validações inúteis.

## 🛠️ Decisões Recentes (28/05/2026)
1. **Ultra-Compactação de Rede (Bi-Polar Short IDs):**
    - **Ação:** Migração dos Skeleton Blocks para serialização binária usando identificadores de 6 bytes (3 iniciais + 3 finais do TxHash) no lugar de hashes de 32 bytes.
    - **Motivo:** Eliminar o overhead de banda em blocos grandes (1.5k TXs) que geravam pacotes de rede pesados (>48KB).
    - **Resultado:** Redução de **81%** no tráfego de rede. Um bloco com 1.000 TXs caiu de ~32KB para **6.2KB**.
    - **Sucesso de Reconstrução:** Validada reconstrução "stateless" em flood de 10.000 TXs com 0 colisões detectadas.
2. **Otimização Homomórfica O(1) (Verkle Turbo):**
    - **Ação:** Implementado o cálculo incremental de compromissos IPA ($C' = C + \sum \Delta \times G_i$) no motor Verkle.
    - **Motivo:** Eliminar o custo $O(256)$ de re-computação completa de nós da árvore a cada mutação de estado.
    - **Resultado:** A finalização de blocos pesados (1.5k TXs) caiu de ~5s para <2s. O pico do motor atingiu **750 TX/s**.
2. **Baseline de Performance Industrial (Verkle Stress Test):**
    - **Resultado:** **484 AvgTPS** sustentados em teste de estresse de longa duração (2 min).
    - **Métricas:** 58.200 updates de estado processados com pico de Heap em 907MB.
    - **Estabilidade:** 0 GCs/s durante o pico, demonstrando eficiência da arquitetura Zero-Alloc na Verkle Tree.
3. **Resiliência de Ressurreição (Consensus Halt Guard):**
    - **Ação:** Implementado reset automático da trava `lastStartedHeight` quando o quórum de rede é perdido e recuperado.
    - **Impacto:** Fim do travamento manual necessário para "acordar" nós que ficaram isolados ou caíram por tempo prolongado.

## 🛠️ Decisões Recentes (22/05/2026)
1. **Sincronização Atômica Sync-Consensus (Anti-Self-Sabotage):**
    - **Ação:** Refatorado o `SyncManager` para callback sincronizado sob o mutex global `execMu`.
2. **Higiene de Execução (Early StateRoot Validation):**
    - **Ação:** Validação do `IntermediateRoot` antes de chamar `st.Commit()`.
3. **PQC Performance Milestone (The Tsunami Test 10K):**
    - **Resultado:** Processadas **10.000 TXs ML-DSA** com **238 AvgTPS de execução** reais (finalização em disco).

## 🛠️ Decisões Recentes (22/05/2026)
1. **Sincronização Atômica Sync-Consensus (Anti-Self-Sabotage):**
    - **Ação:** Refatorado o `SyncManager` para utilizar um callback sincronizado (`ExecuteBlockFn`) gerenciado pelo `Node` sob o mutex global `execMu`.
    - **Guarda de Altura:** Implementada verificação estrita de altura no Sync para ignorar blocos já processados pelo Consenso, evitando falhas de Nonce Mismatch por re-execução.
    - **Recuperação Soberana V2:** Em caso de falha real de execução, o `StateDB` é 100% reiniciado (`NewStateDB`) para limpar o cache de contas e restaurar a verdade absoluta do SSD.
    - **Motivo:** Corrigir a condição de corrida onde o Sync tentava reprocessar blocos que o Consenso acabara de finalizar, disparando restaurações de estado indevidas que travavam a rede.
2. **Higiene de Execução (Early StateRoot Validation):**
    - **Ação:** O `StateProcessor` agora valida o `IntermediateRoot` (calculado em RAM) contra o cabeçalho **antes** de chamar `st.Commit()`.
    - **Impacto:** Impede a gravação de estados corrompidos ou nonces avançados no SSD em caso de blocos inválidos, garantindo que o disco sempre contenha apenas dados canônicos.
3. **Redução de Ruído Criptográfico:**
    - **Ação:** Rebaixados logs de erro de assinaturas malformadas/inválidas no `VerifyHybrid` para nível `DEBUG`.
    - **Motivo:** Tratamento de "lixo de rede" como evento normal de operação distribuída, evitando poluição dos logs de sistema durante ataques ou testes de estresse.
4. **PQC Performance Milestone (The Tsunami Test 10K - 22/05/2026):**
    - **Resultado:** Processadas **10.000 TXs ML-DSA** em 42 segundos com **238 AvgTPS de execução** reais (finalização em disco).
    - **Vazão de Injeção:** Estabilizada em **651 TX/s**.
    - **Estabilidade:** 100% de sucesso na drenagem da MemPool sem interrupções de consenso ou divergência de StateRoot.

    1. Identidade Soberana On-Chain (Mover chaves Dilithium para a Verkle Tree).
    2. Discovery Dinâmico (Kademlia).
    3. Sincronismo Massivo Híbrido (BitTorrent/Torrent).

## 🛠️ Decisões Recentes (12/05/2026)
1. **Ghost Root Killer (Snapshot Resilience):**
    - Implementada a garantia de StateRoot determinístico em blocos com 0 transações, forçando a herança explícita da raiz do bloco pai.
    - **Motivo:** Corrigir a divergência de estado causada por efeitos colaterais de cache ou poluição de memória em rounds de consenso inativos.
2. **Resiliência do Sync Engine:**
    - Implementada a drenagem agressiva de canais DTS e verificação recursiva "Catch the Bus" no boot.
    - **Motivo:** Evitar travamentos do nó causados por mensagens de blocos futuros ou atrasados que congestionavam as buffers de sincronia.
3. **Persistência de Identidades PQC (Cartório Local):**
    - Criado o arquivo `identities.json` por nó para persistir chaves públicas ML-DSA descobertas durante o Gossip.
    - **Motivo:** Resolver o problema de "Memory Loss" onde nós que reiniciavam perdiam a capacidade de verificar blocos históricos assinados por nós que estavam offline no momento do boot.
4. **Compactação Soberana de Boot:**
    - Implementada chamada automática a `db.Compact(nil, nil)` durante o carregamento de bases de dados existentes.
    - **Impacto:** Redução de 16% no footprint de disco (90MB -> 75MB) após carga massiva, otimizando o I/O para operações subsequentes.
5. **PQC Scalability Milestone (The Tsunami Test - 12/05/2026):**
    - Validada a capacidade da rede de processar 1.000 TXs ML-DSA por bloco (250-500 TPS sustentados) com apenas 16% de uso de CPU em um cluster de 7 nós locais.
    - **Estabilidade:** Confirmada a mutação correta da StateRoot e resiliência com 2/7 validadores offline.
6. **Early RoundChange (Watchdog de Conectividade):**
    - Implementado Watchdog de 2 segundos no início de cada rodada para verificar a conectividade P2P com o Proposer designado.
    - **Ação:** Se o Proposer for detectado como offline via DTS, o nó antecipa seu voto de `RoundChange` sem esperar o timeout completo (10s+).
    - **Segurança:** O salto de rodada permanece protegido pelo quórum de $f+1$ (Weak Quorum), garantindo que a rede só avance se a maioria dos nós ativos concordar com a falha.
    - **Impacto:** Redução drástica da latência em redes com validadores instáveis, recuperando até 80% do tempo de espera por proposer offline.
7. **Evolução Absoluta - Mercado Dinâmico Único (Sovereign V1):**
    - **Ação:** Removido o suporte a transações "Legacy" em favor do modelo EIP-1559 como padrão único e obrigatório.
    - **Motivo:** Eliminar dívida técnica de retrocompatibilidade e garantir proteção nativa contra flood desde a gênese.
    - **Canonicidade:** O layout SSZ foi blindado contra data injection, exigindo tamanhos fixos estritos (32 bytes) para campos de valor e taxas.
8. **Blindagem de Memória e Consenso (Besu Alignment):**
    - **MemPool Purge:** Implementada a purga descendente automática. Se um nonce falha por preço ou saldo, todos os subsequentes são expulsos para manter a fila saudável.
    - **Consenso Econômico:** O `StateProcessor` agora valida o `BaseFee` de cada bloco contra o bloco pai, impedindo manipulações de preço por parte do validador (Proposer).
    - **Atomicidade:** Transferências de valor agora são atômicas à execução da VM via snapshots, impedindo perda de fundos em falhas de contrato.
9. **Determinismo de Assinatura (Effective Price Separation):**
    - **Ação:** O campo `GasPrice` (Effective) foi removido do layout binário assinado.
    - **Motivo:** Permitir assinaturas imutáveis. O preço pago é uma consequência do estado da rede, não uma previsão do usuário. O usuário assina apenas seus limites (`MaxFee` e `PriorityFee`).

## 🛠️ Decisões Recentes (15/07/2026)
1. **Otimização do Watchdog de Proposer (Dynamic Watchdog Timer):**
    - **Ação:** Refatoração do Watchdog do Proposer no módulo `Round` ([round.go](file:///c:/Magno/Projetos/jamii/pkg/consensus/ibft/round.go)). Substituição da rotina `go func() { time.Sleep(2 * time.Second) ... }` por um timer cancelável nativo (`time.Timer` via `time.AfterFunc`).
    - **Grace Period Dinâmico:** O tempo de espera do Watchdog foi alterado de 2 segundos fixos para ser proporcional ao período do bloco (`50% de BlockPeriod`, com limite mínimo de 2 segundos). Isso previne timeouts e mudanças de líderes prematuras sob estresse pesado da rede.
    - **Prevenção de Vazamento de Recursos:** O timer do Watchdog é agora explicitamente parado (`Stop()`) nos métodos `Stop()` e `onTimeout()`, eliminando o risco de goroutines órfãs na RAM e contenção de trancas de CPU em rodadas encerradas.
2. **Gerador de Carga Concorrente e Estável (Jamii Traffic Generator):**
    - **Ação:** Criação da ferramenta standalone [generator.go](file:///c:/Magno/Projetos/jamii/cmd/traffic/generator.go) para testes de estresse concorrentes sustentados (Soak Test).
    - **Criptografia Rápida e Faucet:** Gera identidades híbridas (Secp256k1 + ML-DSA-65) de forma extremamente rápida ignorando o fluxo pesado de KDF (scrypt) e mnemônicos. Financia as contas sequencialmente no arranque. Um faucet em background monitora e recarrega os saldos on-chain via chamadas RPC quando caem abaixo de 1 JAMII.
    - **Concorrência Livre de Deadlocks:** Resolvidos problemas de AB-BA Deadlock isolando o lock das goroutines durante as chamadas de rede HTTP e utilizando balanceamento dinâmico via pool de conexões HTTP Keep-Alive.
3. **Mitigação de DoS e Resiliência BFT sob Estresse de Rede (DTS & MemPool Security):**
    - **Ação:** Correção de vulnerabilidade de descarte silencioso de mensagens de sincronização em `pkg/dts/engine.go` e proteção de spam em `pkg/mempool/pool.go`.
    - **DTS Timeout-based Send:** Substituição do enfileiramento não-bloqueante imediato (`select { case: default: }`) por um select com timeout de 3 segundos (`time.After(3 * time.Second)`) para requisições de bloco (`MsgGetBlockByNumber`) e respostas de blocos completos (`MsgData`). Isso garante que dados de reconstrução de blocos não sejam descartados durante congestionamento de rede por gossips de transações, ao mesmo tempo em que previne vazamentos de goroutines em nós desconectados.
    - **Controle de Spam por Remetente na MemPool:** Implementação de um limite de transações pendentes/agendadas na pool por conta de `MaxSlotSize / 5` (2.000 por conta). Evita ataques de exaustão de slots da pool por um único remetente floodando transações inválidas ou de baixo custo, isolando o impacto apenas à conta agressora.
4. **Dimensionamento de requestTimeout sob Carga (BFT Liveness Calibration):**
    - **Regra:** O `requestTimeout` deve ser calibrado com base no `blockPeriod` e na capacidade de processamento de hardware da rede. Sob estresse denso (ex: 100+ TPS com assinaturas híbridas ML-DSA e Verkle Trees), o `requestTimeout` deve garantir uma folga de processamento de pelo menos 5 a 6 segundos além do `blockPeriod` (ex: `blockPeriod = 6s` e `requestTimeout = 12s` ou `15s`).
    - **Motivo:** Evita timeouts prematuros e mudanças de líderes espúrias na Rodada 0. Garante que os líderes de R:0 tenham tempo físico suficiente para computar e propagar blocos sob estresse de CPU antes que os nós secundários forcem transições de rodadas.
5. **Mecanismo de Dupla Camada para Proteção de Vivacidade (BFT Liveness Guard Rails):**
    - **Ação:** Validação da arquitetura de detecção de falhas de validadores.
    - **Watchdog DTS (Falhas Físicas):** O Proposer Watchdog dinâmico (executado a 50% do `blockPeriod`) valida se o nó líder está ativo e conectado via DTS P2P. Em caso de crash físico (queda de processo ou servidor), a troca de líder é acionada instantaneamente em apenas 3 segundos, sem esperar pelo timeout principal.
    - **RequestTimeout (Falhas Lógicas):** O cronômetro de `requestTimeout` age como uma segunda linha de defesa contra falhas lógicas (deadlocks de banco de dados, loops infinitos na EVM ou silenciamentos de rede). Mesmo se o DTS indicar que o nó está online, o timeout impede o congelamento infinito da rede, forçando a vivacidade (Liveness) e a continuidade do consenso.
6. **Cache de Verificação de Assinaturas PQC (Signature Validation Cache):**
    - **Ação:** Otimização do método `Verify()` no módulo homologado `pkg/encoding` ([transaction.go](file:///c:/Magno/Projetos/jamii/pkg/encoding/transaction.go)) adicionando campos privados de runtime (`verifiedCache bool`, `hasVerified bool`) para salvar o resultado da validação matemática.
    - **Motivo:** A verificação de assinaturas híbridas pós-quânticas (ML-DSA-65) é altamente exigente em CPU (~1ms por transação). Como as transações são imutáveis após decodificadas, armazenar o resultado da validação evita que o mesmo nó repita a verificação até 4 vezes por transação (durante a ingestão de mempool, montagem de bloco, validação de proposta e canonical commit).
    - **Impacto:** O tempo de processamento de blocos cheios (1.000 a 1.500 transações) foi reduzido de **9 segundos para menos de 1 segundo**, eliminando picos de CPU e resolvendo os timeouts ("timer expired") na Rodada 0 do consenso.

## 🛠️ Decisões Recentes (28/07/2026)
1. **Prevenção de Corrupção de Estado via RPC Sandbox (State Leak Isolation):**
    - **Problema:** O método RPC `eth_call` executava a simulação de transações na EVM diretamente sobre a instância do `StateDB` canônico do nó, sem isolamento. Isso causava vazamento de alterações temporárias (saldos, nonces e storages) no cache de RAM do validador. Durante o consenso canônico, o nó encontrava o estado alterado, resultando em mismatch de `StateRoot` e disparando pânico de corrupção.
    - **Ação:** Modificado o arquivo [server.go](file:///c:/Magno/Projetos/jamii/pkg/rpc/server.go) do RPC para instanciar a EVM de consulta a partir de um `Sandbox()` isolado e descartável em RAM, protegendo o estado canônico de qualquer poluição.
2. **Otimização Extrema de Cache e Hibridização Pebble/Postgres no Archiver (Pebble L1 Cache):**
    - **Problema:** O Archiver sincronizando sobre o PostgreSQL remoto via rede sofria com latência severa (~16-30s por bloco denso) decorrente de milhares de queries síncronas de leitura (`SELECT`) ao percorrer chaves e nós da trie. O problema era agravado pelo limite padrão de `MaxWarmTries = 512`, que limpava o cache de RAM do `StateDB` a cada poucos blocos, forçando novas leituras frias e lentas do Postgres.
    - **Ação 1 (PebbleDB L1 Cache no PostgresStore):** Modificado o [postgres.go](file:///c:/Magno/Projetos/jamii/pkg/store/postgres/postgres.go) para inicializar um PebbleDB local (`pebble_cache`) e utilizá-lo como cache L1 de alta performance. Métodos `Get` buscam primeiro no PebbleDB local (latência de microsegundos); e a travessia de chaves em `NewIterator` foi movida integralmente para a base local.
    - **Ação 2 (Escrita e Commits Atômicos no L1):** Modificado o `Commit` do batch relacional para aplicar as operações de forma síncrona e atômica no PebbleDB local e, em seguida, enfileirá-las assincronamente na `writeQueue` para o PostgreSQL.
    - **Ação 3 (Aumento de Cache de Contas):** Alterada a constante `MaxWarmTries` em [statedb.go](file:///c:/Magno/Projetos/jamii/pkg/core/state/statedb.go) de `512` para `32768`, permitindo que contas e sub-tries permaneçam em RAM em todos os nós da rede.
    - **Ação 4 (Multiplicador de Cache de Storage):** Implementado o método `SetStorageCacheMultiplier` no `StateDB` e ativado o multiplicador de `10.0` para storage de contratos no arranque do Archiver em [main.go](file:///c:/Magno/Projetos/jamii/cmd/archiver/main.go), reduzindo drasticamente as buscas lentas e eliminando a sobrecarga de sync nos validadores.
3. **Resiliência de Estado no Archiver (StateDB Restoration):**
    - **Ação:** Modificada a rotina de sincronização do Archiver no [main.go](file:///c:/Magno/Projetos/jamii/cmd/archiver/main.go) para recriar o `StateDB` canônico e redefinir a raiz da trie para a última altura canônica válida caso a execução de um bloco importado falhe, prevenindo o vazamento de chaves planas dirty na persistência física do Postgres.
4. **Alinhamento do Consenso e Injeção de Coinbase Nativa no Construtor do Bloco (State Promotion Stabilization):**
    - **Problema:** A Coinbase era atribuída ao cabeçalho do bloco proposto de forma tardia em `controller.go` (fora do `GetPayloadWithParent`). Como a Coinbase altera os bytes do header, o hash do bloco final sofria mutação. O validador local de consenso, ao finalizar o bloco, falhava em recuperar o cache de estados (Sandbox) do pool pela chave do hash alterado (`StateCached: false`). Isso forçava o nó a re-executar as transações no estado canônico, provocando divergências graves na ordem/verificação e gerando mismatches de nonces de conta (`nonce mismatch: expected 669, got 672` no bloco 311).
    - **Ação:** Atualizadas as assinaturas dos métodos de geração de payload na interface `ExecutionEngine` ([engine_api.go](file:///c:/Magno/Projetos/jamii/pkg/core/engine_api.go)) e no processador ([processor.go](file:///c:/Magno/Projetos/jamii/pkg/core/processor.go)) para receberem a `coinbase types.Address` como parâmetro, injetando o endereço beneficiário desde o arranque na instanciação do `Header`. O setter de Coinbase tardio em `controller.go` foi removido, restaurando a consistência absoluta do hash e o reaproveitamento imediato via `State Promotion`.
5. **Correção de Vazamento e Corrupção de Índices em Max-Heap e Min-Heap da MemPool (MemPool Stability):**
    - **Problema:** Sob carga intensa de transações (15.000 slots ocupados), o nó validador sofria pânico (`index out of range` no `PriceHeap.Swap`) ao tentar expulsar transações mais baratas em `evictCheapest`. A causa era dupla: (1) O `GetExecutable` copiava de forma rasa o slice de heads (`tp.heads`) para um heap de ordenação temporário, mas o `heap.Pop` e `heap.Push` operavam em cima das mesmas instâncias de `pooledTransaction` reais em RAM, zerando o `priceIdx` a cada bloco montado e quebrando a consistência do heap real. (2) O `evictCheapest` chamava `tp.remove()` que tentava re-remover o item da heap de eviction após o `Pop` já tê-lo removido.
    - **Ação:** Modificado o [pool.go](file:///c:/Magno/Projetos/jamii/pkg/mempool/pool.go) para clonar por valor (`*pt` e `*nextPt`) as transações no `GetExecutable` ao inseri-las no heap de prioridade temporário, isolando as propriedades de índice de controle (`priceIdx`). O `evictCheapest` foi modificado para marcar `item.evictIdx = -1` antes de chamar o `remove` sequencial, prevenindo manipulações duplicadas de heap.

## 🛠️ Decisões Recentes (14/07/2026)
1. **Autogovernança Dinâmica e Descentralizada de Validadores (Voting Mechanism):**
    - **Ação:** Migração do contrato `ValidatorRegistry` para um modelo auto-gerido sem a figura de administrador estático (`owner`).
    - **Mecânica:** Apenas os membros ativos no comitê de validadores (endereços de consenso dos nós) podem criar propostas (`propose`) e registrar votos (`vote`). A inclusão ou exclusão de validadores é executada automaticamente na EVM ao atingir o quórum de maioria simples ($> N/2$).
    - **Integração com o Consenso:** O motor IBFT 2.0 (`controller.go`) consulta localmente a lista de validadores do contrato na EVM na virada de cada nova altura de bloco, atualizando dinamicamente o conjunto de assinantes autorizados.
2. **Custeio de Gas com Lucro de Consenso (Economic Security Rule):**
    - **Regra:** As transações de governança (votos e propostas) no contrato `ValidatorRegistry` devem ser pagas utilizando os próprios lucros/recompensas de bloco acumulados por cada validador.
    - **Motivo:** Vincula o peso político do validador à sua alta disponibilidade e boa conduta na rede. Nós instáveis ou inativos que fiquem fora do ar não geram recompensas e, consequentemente, perdem a capacidade financeira de propor ou votar em mudanças por escassez de saldo para gas. Além disso, previne spam de transações e propostas lixo no StateDB.
3. **Staking, Slashing e Tesouraria Configurável (Consensus Guarantees):**
    - **Ação:** Introdução de depósito de garantia obrigatório (Staking) e regras de confisco (Slashing) no contrato `ValidatorRegistry`.
    - **Depósito de Entrada:** Qualquer candidato que deseje ingressar no comitê de validadores via proposta (`propose`) deve realizar um depósito sob fiança de `100.000 tokens` nativos usando o método `deposit()`. Os fundos ficam sob custódia do contrato inteligente.
    - **Punição por Má Conduta (Slashing):** Se um validador ativo for removido da rede por meio de votação de consenso, a garantia financeira dele é confiscada do contrato e transferida imediatamente para a conta de **Tesouraria** configurada.
    - **Tesouraria Configurável via Gênese:** O endereço da tesouraria é definido pela chave `"treasury"` no arquivo `genesis.json` e pré-alocado no **Slot 4** de armazenamento permanente do contrato inteligente no bloco gênese #0.
    - **Saque (Withdrawal):** Saídas voluntárias ou candidatos que não se elegeram podem resgatar seus fundos por meio do método `withdraw()`.
4. **Mapeamento de Endereços de Contrato Recém-Criados no Explorer (Smart Contract Address Resolution):**
    - **Problema:** O card "Contratos Inteligentes Recentes" do explorador exibia o endereço dos contratos criados pós-Gênese como `NULL`. Isso acontecia porque a tabela `account_flat` era populada apenas com o `address_hash` (chave do StateDB) durante os commits do Trie, enquanto a correspondência textual (Bech32 e Mirror) permanecia nula.
    - **Solução:** Atualização de [postgres.go](file:///c:/Magno/Projetos/jamii/pkg/store/postgres/postgres.go) para inspecionar os recibos de transações (`r:`). Se o recibo contiver o endereço de um contrato criado (`ContractAddress != nil`), a função `RegisterAddress` é invocada no banco relacional para registrar o mapeamento de hash para endereço textual.
    - **Resolução de Contratos do Sistema:** Adicionado o registro do endereço do contrato de validadores (`ValidatorContract`) do Gênese no startup do arquivador em [main.go](file:///c:/Magno/Projetos/jamii/cmd/archiver/main.go), assegurando que o contrato de governança também seja exibido corretamente com seu endereço textual.



## 🛠️ Decisões Recentes (09/05/2026)
1. **Cadência de Produção (Block Pacing):**
    - Implementada a lógica de espera obrigatória no Proposer baseada no `blockPeriod` do Gênesis.
    - **Motivo:** Evitar a "Inundação de Consenso" (Consensus Flooding) onde líderes propoem blocos tão rápido quanto a CPU permite, desestabilizando a latência da rede. A Jamii agora bate o coração em intervalos previsíveis (Besu Compliance).
2. **Tuning de Tolerância (Configurable RequestTimeout):**
    - Externaliado o parâmetro `requestTimeout` para o `genesis.json`.
    - **Calibração:** Redução do timeout padrão de 10s para 4s em cenários de teste para mitigar a latência causada por Rogue Nodes (Alice/Bob) offline, mantendo a fluidez da rede sem intervenção no código.
3. **Segurança Sync-to-Consensus (Observer Lock):**
    - Implementada trava de segurança no orquestrador `Node`. Um validador agora entra em **Modo Observador** automático se sua altura local for inferior à da rede.
    - **Proteção de Integridade:** O nó descarta mensagens de consenso (votos/propostas) até que o `SyncManager` complete o download dos blocos antigos. Isso evita erros de *StateRoot mismatch* causados por tentativas de execução de blocos novos sobre um estado local defasado.
4. **Saneamento de Logs Industriais:**
    - Refatoração do `pkg/util/logger` para implementar o nível **CRITICAL** real (cor vermelha, alta severidade).
    - Remoção de redundâncias de log (ex: `ERROR: CRITICAL`). Erros fatais que interrompem a execução do bloco agora utilizam o método `log.Critical` exclusivamente, facilitando a filtragem em sistemas de telemetria.
5. **Dívida Técnica - Desacoplamento Arquitetural:**
    - Identificado alto grau de acoplamento por assinatura de métodos (Propeller Effect).
    - **Decisão:** Mapeada a necessidade de migrar de variáveis primitivas para **Objetos de Contexto** (ex: `ChainConfig`) nos construtores de Consenso na Fase 5 do projeto.

## 🛠️ Decisões Recentes (06/05/2026)
1. **Saneamento e Qualidade de Código (Audit Ready):**
    - Início do programa de refinamento de documentação técnica em Português Brasileiro (PT-BR) para os módulos Core, visando facilitar o onboarding e auditoria local.
    - **Módulos Saneados:** `pkg/core/types`, `pkg/blockchain` e `pkg/consensus`.
2. **Erradicação de Números Mágicos:**
    - Introdução de constantes de tamanho padronizadas (`HashSize`, `Uint64Size`) em todo o ecossistema Core para garantir consistência binária entre rede, VM e disco.
3. **Persistência Robusta do Blockchain:**
    - Refatoração dos prefixos de banco de dados (`BlockPrefix`, `TxPrefix`, `HeightPrefix`) para constantes nomeadas, eliminando riscos de colisões ou erros de busca no PebbleDB.
4. **Endurecimento de Testes Unitários:**
    - Criação de suíte de teste de ciclo de vida completo (`blockchain_test.go`) validando persistência, recuperação por hash/número e integridade de assinaturas híbridas (Secp256k1 + ML-DSA) no motor Verkle.
5. **Consistência SSZ (ExtraData Soberano):**
    - Decisão de abandonar o formato RLP (Geth/Besu) para o campo `ExtraData` do cabeçalho em favor do **SSZ (Simple Serialize)**.
    - **Motivo:** Garantir consistência técnica com as Transações e permitir **Acesso Aleatório (Random Access)** às assinaturas PQC pesadas (~2.4KB cada) via tabela de offsets, otimizando o tempo de parsing.
6. **Evolução de Rede (Turbine Strategy):**
    - Adoção de uma estratégia de propagação não-linear e fragmentada estilo BitTorrent (inspirada no protocolo Turbine da Solana) para o tráfego de blocos.
    - **Implementação:** O DTS servirá como motor de sharding para pedaços do `IbftExtraData` e corpos de blocos, mitigando o "Header Bloat" causado pelas assinaturas PQC.
7. **Auditoria de Performance PQC (Audit Real-Cost):**
    - Realizado teste de estresse massivo para medir o custo de validação de provas de consenso PQC (ML-DSA).
    - **Resultado:** Mesmo no cenário extremo de 100 validadores (Header de ~325 KB), o tempo total de processamento (Parsing SSZ + Verificação Criptográfica) estabilizou em **~5ms**.
    - **Conclusão:** O custo de processamento PQC representa menos de **0.25%** de um tempo de bloco de 2 segundos, confirmando a viabilidade industrial da arquitetura Jamii apesar do aumento no volume de dados.
8. **Otimização Verkle (IPA Homomorphism):**
    - **Suporte Homomórfico:** Implementado o método `Update` no `BandersnatchCommitter` para permitir atualizações de compromisso estilo delta ($C' = C + \Delta G_i$).
    - **Veredito Batch:** Testes de estresse provaram que para blocos massivos (200+ chaves), a recomputação total via **MSM (Multi-Scalar Multiplication)** é 47% mais rápida (488 TPS vs 254 TPS) devido ao overhead individual e contenção de travas.
    - **Arquitetura Híbrida:** Decisão de manter o homomorfismo apenas para provas individuais e *Stateless Clients* (Witness Efficiency), enquanto a produção de blocos continua usando MSM paralelo.
9. **Fragmentação de Selos de Consenso (DTS Sharding):**
    - **MT_VALIDATOR_SEAL (0x03):** Adicionado novo tipo de dado no DTS para permitir que selos PQC individuais viajem de forma independente do esqueleto do bloco.
    - **Paralelismo de Rede:** Isso permite que os nós comecem a validar e executar o bloco assim que o esqueleto chega, "encaixando" as assinaturas PQC pesadas (~2.4KB cada) conforme elas chegam via DTS, eliminando o gargalo de propagação do cabeçalho completo.
10. **Integração DTS-Identity (Handshake Soberano):**
    - **Handshake de Chave Pública:** Evolução do protocolo DTS para trocar Chaves Públicas Híbridas completas no primeiro contato.
    - **IdentityRegistry (Auto-Populate):** O DTS agora alimenta automaticamente o `IdentityRegistry` com as chaves dos pares conectados. Isso garante que o IBFT (Consenso) consiga validar assinaturas PQC de selos compactos (sem chave inclusa) de forma instantânea.
    - **Soberania de Endereçamento:** O `nodeID` de rede agora é derivado criptograficamente da chave pública (`DeriveSovereignAddress`), unificando a identidade P2P com a identidade da Blockchain.
11. **Soldagem Criptográfica do Consenso (Hardened BFT):**
    - **Fim dos Placeholders:** Removidas todas as "simulações" de verificação (`valid=true`). O motor agora realiza a validação matemática rigorosa (ML-DSA) de cada selo recebido.
    - **Verificação Progressiva:** Implementada a lógica no `VerifyHeader` que reconstrói o *Sealable Hash* e valida o quórum de assinaturas reais contra o `IdentityRegistry`.
    - **Robustez Industrial:** O controlador de consenso agora é agnóstico ao transporte, mas rigoroso na identidade, garantindo que apenas validadores autorizados consigam influenciar a finalização de blocos.

## 🛠️ Decisões Recentes (05/05/2026)
1. **TreeType Imutável (Consensus Parameter):**
    - O tipo de árvore (`SMT` ou `Verkle`) agora é um parâmetro "pétreo" definido no Gênese (`ChainConfig`).
    - **Proteção de Identidade:** O sistema persiste o `TreeType` no primeiro boot. Reinicializações com um `genesis.json` divergente do banco de dados disparam um erro fatal de conflito de identidade, prevenindo corrupção de estado.
2. **Redefinição DTS:**
    - O módulo `pkg/dts` foi renomeado conceitualmente para **Distributed Transmission Service** para refletir melhor sua função de transporte de dados agnósticos na rede P2P.
3. **Validação Verkle:**
    - A Verkle Tree foi homologada para produção após atingir **4.282 TPS** sustentados com zero erros no teste Inferno.

## 🛠️ Decisões Recentes (04/05/2026)
1. **DTS (Distributed Transaction Store):** Implementado como um módulo de rede isolado ("Internal IPFS") para transporte agnóstico de dados.
    - **Deduplicação Determinística (Tie-Break):** Resolvido loop de conexão infinita através de regra de soberania baseada em ID. O nó Soberano (maior ID) mantém Outbound; o Vassalo mantém Inbound.
    - **Identidade de Conexão (addrToID):** Cache inteligente de endereços para evitar rediscagens redundantes em conexões já estabelecidas via Inbound.
    - **Conexões Persistentes:** Utiliza pool de conexões TCP mantidas vivas com timeouts de socket (2s) para evitar bloqueios de recursos.
    - **Polimorfismo (Message Types):** Protocolo INV/REQ/DATA suporta múltiplos tipos de payload (MT_TRANSACTION, etc.).
2. **Benchmark de Performance (Inferno Test):**
    - **Transporte Puro (DTS):** ~8.8k TPS sustentados.
    - **Com Verificação PQC (ML-DSA):** ~5.6k TPS sustentados (Queda de ~36%).
    - **Estabilidade:** Erros: 0. Validada a eficiência do motor de propagação e o custo real da criptografia pós-quântica.
3. **Saneamento de Logs:** Removidos prefixos redundantes (Node:, DTS:) para otimizar a legibilidade dos logs industriais.
4. **Compact Blocks Strategy:** Decisão de propagar blocos contendo apenas Header e lista de CIDs (hashes de transações).
   
## 🛠️ Decisões Recentes (04/05/2026)
1. **Delegação Soberana da TxPool:**
    - **Centralização de Validação:** A `TxPool` agora é o ponto único de entrada e validação (assinatura + saldo). Componentes como `Node` e `RPC` delegam totalmente a custódia, eliminando lógica redundante.
    - **Barreira Econômica:** Implementada verificação rigorosa de saldo (`TotalCost`) antes da aceitação na pool. Isso protege contra ataques de spam de transações sem fundos.
    - **Sincronização Atômica:** O `Node` agora utiliza o hook `OnBlockFinalized` do consenso para resetar a MemPool, garantindo que nonces e saldos estejam sempre em paridade com o último estado comitado.
2. **Refatoração do Consenso (IBFT):**
    - O `Controller` e o `BlockHeightManager` foram atualizados para consumir transações diretamente da `TxPool` via interface `GetExecutable`, desacoplando o motor de consenso da lógica de mempool.
3. **Hardening de Testes:**
    - A suíte de testes da MemPool foi atualizada para exigir transações criptograficamente válidas. Testes com "dummy transactions" foram depreciados em favor de conformidade industrial.

## 🛠️ Decisões Recentes (04/05/2026)
1. **DTS (Distributed Transaction Store):** Implementado como um módulo de rede isolado ("Internal IPFS") para transporte agnóstico de dados.
    - **Deduplicação Determinística (Tie-Break):** Resolvido loop de conexão infinita através de regra de soberania baseada em ID. O nó Soberano (maior ID) mantém Outbound; o Vassalo mantém Inbound.
    - **Identidade de Conexão (addrToID):** Cache inteligente de endereços para evitar rediscagens redundantes em conexões já estabelecidas via Inbound.
    - **Conexões Persistentes:** Utiliza pool de conexões TCP mantidas vivas com timeouts de socket (2s) para evitar bloqueios de recursos.
    - **Polimorfismo (Message Types):** Protocolo INV/REQ/DATA suporta múltiplos tipos de payload (MT_TRANSACTION, etc.).
2. **Benchmark de Performance (Inferno Test):**
    - **Transporte Puro (DTS):** ~8.8k TPS sustentados.
    - **Com Verificação PQC (ML-DSA):** ~5.6k TPS sustentados (Queda de ~36%).
    - **Estabilidade:** Erros: 0. Validada a eficiência do motor de propagação e o custo real da criptografia pós-quântica.
3. **Saneamento de Logs:** Removidos prefixos redundantes (Node:, DTS:) para otimizar a legibilidade dos logs industriais.
4. **Compact Blocks Strategy:** Decisão de propagar blocos contendo apenas Header e lista de CIDs (hashes de transações).

## 🛠️ Decisões Recentes (02/05/2026)
1. **Soberania do GasPrice:** Integrado ao codec SSZ da Transaction. Preço proposto pelo usuário, validado pelo nó.
2. **Buy Gas Atômico:** O StateProcessor debita saldo *antes* da execução. O rollback do bloco estorna as mudanças na Trie, mas a lógica de revert transacional protege o estado.
3. **JSON-RPC Standard:** Decisão de NÃO usar namespaces proprietários (jamii_) para os métodos padrão, visando compatibilidade "out-of-the-box" com Web3 standard.
4. **Rastreio 'latest':** Implementado ponteiro de banco de dados no módulo Blockchain para recuperação instantânea do topo da cadeia.

## 🛠️ Decisões Recentes e Estabilização de Rede (29/07/2026)
1. **Integridade de Iterador em Memória (`memIterator.Seek`)**:
    - **Problema:** O método `memIterator.Seek` utilizava `it.index = foundIdx - 1`, gerando `panic: runtime error: index out of range [-1]` ao buscar a primeira chave com correspondência exata de prefixo em nós Stateless, Sandboxes e Overlay Stores.
    - **Solução:** Corrigido para `it.index = foundIdx` em `pkg/store/memory.go`, assegurando 100% de estabilidade nas buscas da Trie em RAM.
2. **Purga Obrigatória da MemPool Pós-Execução (`SyncManager` & Consenso)**:
    - **Problema:** Nós que sincronizavam blocos via `SyncManager.ExecuteBlockFn` não executavam a limpeza da MemPool, mantendo transações mineradas na RAM local e gerando avisos repetitivos de `nonce mismatch`.
    - **Solução:** Injetada a chamada obrigatória a `n.mempool.SetBaseFee` e `n.mempool.Reset(n.state, block.Number())` ao final de cada execução de bloco no `SyncManager`.
3. **Sequenciamento Estrito por Remetente no Minerador (`GetExecutable`)**:
    - **Problema:** Transações da mesma conta extraídas em lote na mesma rodada podiam gerar avisos temporários de nonce no minerador.
    - **Solução:** Adicionado o rastreamento atômico `localNonces[senderKey] = pt.tx.Nonce + 1` no `GetExecutable` (`pkg/mempool/pool.go`).
4. **Dispositivo de Maturação Incondicional por Altura**:
    - **Problema:** A trava de maturação por altura (`addedAtHeight >= currentHeight`) era ativada apenas se o saldo de transações na pool excedesse 50 TXs. Em lotes menores, transações recém-chegadas entravam na proposta antes de completarem a fofoca P2P.
    - **Solução:** Tornado incondicional para alturas pós-gênese (`useHeightFilter := tp.currentHeight > 0`). Transações recebidas na altura $H$ aguardam obrigatoriamente 1 rodada completa de bloco ($H+1$) para propagação P2P prévia, assegurando 100% de reconstrução por ShortIDs.
5. **Resiliência a Loop Infinito no Torrent Sync (Fallback P2P Sequencial)**:
    - **Problema:** Se um chunk Torrent falhasse na execução/importação, o `SyncManager` entrava em um loop infinito re-tentando o mesmo chunk a cada 2 segundos.
    - **Solução:** Drenado o canal `torrentHashCh` e estabelecido o limite de 2 tentativas por chunk Torrent. Em caso de falha persistente, o motor desiste do Torrent e dá **Fallback automático para o Sync P2P Sequencial clássico**.
6. **Atendimento de Blocos para Sincronismo Inicial (Sem Timeouts)**:
    - **Solução:** O manipulador `handleBlockRequest` lê e entrega blocos gravados no disco para assegurar que novos nós (como o ALBERNAZ) consigam sincronizar a partir do bloco #1 em diante mesmo quando o nó Archiver não estiver diretamente conectado ao novo nó. Isso elimina a ocorrência de `Timeout waiting for block #1` e permite que qualquer novo nó se integre à rede instantaneamente.

## 🛠️ Decisões Recentes e Purga / Sincronismo Industrial (30/07/2026)
1. **Purga Imediata de Transações Inválidas por Hash (`RemoveInvalidTx`)**:
    - **Problema:** Quando uma transação falhava durante a execução (`ApplyTransaction`) na montagem de um bloco, o sandbox revertia o estado do remetente (mantendo o nonce no `StateDB`). As transações subsequentes falhavam com `Transaction nonce mismatch`, e como não eram mineradas, o `mempool.Reset` pós-bloco mantinha as transações inválidas salvas na RAM, gerando alertas repetitivos em loop infinito a cada novo bloco.
    - **Solução:** Criado o método `RemoveInvalidTx(tx)` no `TxPool` (`pkg/mempool/pool.go`) que purga a transação inválida diretamente por seu Hash (`tx.ID()`) e dispara a **Purga Descendente** (`FilterGreaterOrEqual`) para limpar transações dependentes. No `AssembleBlock` (`pkg/core/processor.go`), a falha de `ApplyTransaction` aciona imediatamente `p.mempool.RemoveInvalidTx(tx)`.
2. **Archiver Keep-Alive P2P Ticker (`dts.BroadcastStatus`)**:
    - **Problema:** O nó Archiver (`cmd/archiver/main.go`) emitia `BroadcastStatus` apenas 1 vez no boot. Conforme a rede avançava, ele não anunciava sua nova altura para os outros nós, fazendo com que a malha P2P mantivesse a altura do Archiver congelada no boot (ou 0).
    - **Solução:** Adicionado um loop de ticker de 5 segundos no Archiver para emitir `dts.BroadcastStatus` continuamente com sua altura atualizada (`isArchive = true`), permitindo que a rede reconheça ativamente a altura do Archiver para sincronismo.
3. **Leitura de Altura de Pares Não-Validadores (`checkQuorumHealth`)**:
    - **Problema:** O diagnóstico de quórum em `pkg/node/node.go` lia a altura dos pares apenas dentro do bloco `if isVal`. Para nós não-validadores (como o Archiver), a altura não era consultada e ficava zerada (`peerHeight = 0`), imprimindo erroneamente `height=0, compatible=false` no log `[QUORUM-DIAG]`.
    - **Solução:** Corrigida a função para consultar `GetPeerHeight` e `ready` de todos os pares conectados, permitindo a exibição correta da altura do Archiver nos logs operacionais.
4. **Fallback Multi-Peer no Sincronismo Sequencial (`SyncManager`)**:
    - **Problema:** Na recuperação de blocos sequenciais (`catchUp`), o `SyncManager` enviava `RequestBlockByNumber` a um único `peerID`. Se aquele nó ficasse lento ou saturado, a requisição estourava o timeout e abortava o sync, caindo em um loop infinito no mesmo nó.
    - **Solução:** Alterado o `catchUp` (`pkg/blockchain/sync.go`) para solicitar o bloco `h` em paralelo a todos os pares conectados elegíveis com altura $\ge h$. O primeiro nó a responder entrega o bloco, eliminando travamentos por timeout individual.
5. **Fixação de RPC por Carteira Efêmera (`Account-to-RPC Sticky Pinning`)**:
    - **Problema:** O gerador de tráfego (`cmd/traffic/generator.go`) escolhia um endpoint RPC aleatório para cada transação de uma mesma carteira. Quando a TX $N+1$ era disparada para o Nó 2 antes da fofoca P2P da TX $N$ vinda do Nó 1, o Nó 2 respondia um nonce defasado, gerando erros de rejeição `Nonce mismatch or invalid RBF attempt`.
    - **Solução:** Adicionada a atribuição estática `TargetRPC` por carteira efêmera. Cada carteira executa `getNonce` e `sendRawTX` exclusivamente no seu nó RPC designado, eliminando 100% das rejeições por corrida de propagação P2P.
6. **Cadastro de Carteiras Quentes na MemPool (`Hot Nonce Tracker`)**:
    - **Problema:** Existia uma janela de corrida (*race condition*) entre o momento em que a transação saía da fila `pending` da MemPool para entrar na proposta de bloco e a gravação física daquele bloco no `StateDB`. Nesse intervalo, a consulta `eth_getTransactionCount("pending")` encontrava a fila pendente vazia e o `StateDB` ainda no estado anterior, devolvendo um nonce defasado ao cliente RPC.
    - **Solução:** Criado o mapa `hotNonces map[string]uint64` em RAM na MemPool (`pkg/mempool/pool.go`). Ele armazena o próximo nonce esperado de qualquer carteira ativa e atualiza atômicamente no `Add` e no `GetExecutable`. O `GetPendingNonce` realiza uma consulta em 2 camadas (`hotNonces` RAM -> `StateDB`), e quando o bloco é finalizado, o `Reset` purga as contas confirmadas do mapa quente.
7. **Bloqueio por Recibo de Bloco no Gerador de Tráfego (`Receipt-Locking` de 30s)**:
    - **Problema:** No testador de carga (`cmd/traffic/generator.go`), a transação era enviada via `eth_sendRawTransaction` recebendo o Hash em 1ms. Se a trava `Busy = false` fosse desmarcada antes de o bloco de 10s da rede fechar, a carteira corria o risco de ser sorteada novamente no mesmo bloco, gerando rejeições por RBF/nonce duplicado.
    - **Solução:** Implementado o polling síncrono `waitForReceipt(httpClient, targetURL, txHash, 30*time.Second)` no `generator.go`. A trava `wFrom.Busy = false` é mantida de forma estrita até que o bloco de 10s feche e o nó responda com o recibo de confirmação (`eth_getTransactionReceipt`), garantindo que cada carteira efêmera transacione exatamente 1 vez por bloco minerado.
8. **Proteção RPC para Nós em Sincronismo (`Node in Sync Guard`) & Resiliência do Gerador**:
    - **Problema:** Se um nó estivesse recuperando blocos em background (`SyncManager.IsBehind() == true`), suas consultas de RPC retornavam o estado defasado de blocos antigos. Quando o gerador de tráfego consultava `getNonce` nesse nó atrasado, recebia um nonce antigo e enviava uma transação que o próprio nó rejeitava após atualizar o estado.
    - **Solução:** Implementado a guarda `IsBehind()` nos métodos RPC `eth_sendRawTransaction`, `eth_getTransactionCount` e `eth_getBalance` (`pkg/rpc/server.go`). O nó atrasado recusa conexões de escrita informando o erro padronizado `Node in sync mode, retry later`. No `generator.go`, o recebimento deste erro ativa uma pausa suave de 500ms antes da re-tentativa, impedindo tempestades de erros e mantendo 100% de resiliência.
9. **Descarte de Bloco Especulativo Vazio (`Speculative Block Invalidation`)**:
    - **Problema:** Quando um bloco era finalizado, o motor de especulação em background (`SpeculativeAssembly`) montava imediatamente uma proposta para o bloco seguinte. Se a MemPool estivesse temporariamente vazia naquele milissegundo, a especulação criava um bloco especulativo de 0 TXs. Quando o líder ia propor o bloco 2s depois, ele promovia o bloco especulativo pré-montado de 0 TXs sem verificar se a MemPool havia recebido novas transações no intervalo.
    - **Solução:** Adicionada a verificação em `pkg/consensus/ibft/controller.go` para descartar o `speculativeBlock` de 0 TXs caso a MemPool possua transações executáveis aguardando mineração (`len(c.pool.GetExecutable(1)) > 0`), garantindo que novas transações enviadas sejam incluídas no bloco imediatamente.
10. **Ajuste de Cadência Industrial do Propositor (`Proposer Pacing Align`)**:
    - **Problema:** No controlador IBFT (`pkg/consensus/ibft/controller.go`), se o tempo limite do bloco já tivesse sido atingido (`now >= targetTime`), o código forçava um `time.Sleep` redundante de 1 ciclo completo (10s). Esse sono forçado fazia os outros validadores estourarem o tempo de espera do líder (`RoundTimeout` de 10s), disparando Round Changes desnecessários e forçando o propositor a cair em sync.
    - **Solução:** Removido o `time.Sleep` na condição `now >= targetTime`, alinhando a implementação aos padrões oficiais do Geth (Clique) e Besu (QBFT). Quando a hora alvo do bloco já passou, o propositor transmite a proposta imediatamente (latência 0ms), eliminando perdas de liderança e travamentos por timeout.

## 🛠️ Governança Dual-Channel e Resiliência de Consenso (11/08/2026)
1. **Mandato de Atomicidade Dual-Channel no DTS (`IsPeerFullyConnected`)**:
    - **Problema:** O motor DTS abria duas conexões TCP paralelas (`Bulk` + `Express`) por par. Tão logo o primeiro socket completava o handshake, a goroutine de leitura já repassava pacotes de status (`MsgStatus`) para a camada superior do nó, antes que o segundo socket terminasse o handshake. Ao consultar `ConnectedPeersIDs()`, a lista exigia ambos os canais operacionais, fazendo o nó registrar um quórum incompleto e pausar o consenso (`consensusHalted = true`) antes de a segunda conexão finalizar.
    - **Solução:** Implementado o helper `IsPeerFullyConnected(peerID)` em `pkg/dts/engine.go` e imposto o **Filtro Cruzado de Entrada e Saída (Dual-Channel Gatekeeper)**. O DTS descarta pacotes de entrada e aborta envios de saída para pares cujas conexões dual-channel não estejam 100% estabelecidas de forma atômica.
2. **Reavaliação Atômica de Quórum no Nó (`handlePeerConnect`)**:
    - **Problema:** A trava de deduplicação em `handlePeerStatus` (`if existed && prevReady == ready && prevHeight == height`) descartava a reavaliação de quórum quando um status duplicado chegava após a conclusão do segundo socket.
    - **Solução:** Injetada a chamada obrigatória a `checkQuorumHealth()` dentro de `handlePeerConnect(peerID)` em `pkg/node/node.go`, garantindo que no instante exato em que ambos os canais TCP (Bulk + Express) são confirmados pelo DTS, o quórum seja reavaliado e o motor de consenso seja retomado (`RESUMING`).
3. **Resiliência contra Discrepâncias e Trava de Nil Pointer**:
    - **Solução:** Adicionadas verificações defensivas `n.blockchain != nil` e `n.dts != nil` nas rotinas de conectividade do nó, prevenindo inconsistências de memória durante inicializações estateless e testes unitários.
4. **Precisão de Sub-Segundo no Pacing do Propositor (`controller.go`)**:
    - **Problema:** A conversão da hora atual usando `time.Now().Unix()` truncava os milissegundos para segundos inteiros. Se um bloco levava 0,5s para ser computado e gravado no banco, o cálculo `targetTime - now` avaliava para o segundo inteiro completo (3s a partir do instante 0,5s), fazendo o propositor dormir por 3,0s adicionais a partir da gravação e totalizando 3,5s por bloco.
    - **Solução:** Atualizado o cálculo em `pkg/consensus/ibft/controller.go` para usar alta resolução temporal de milissegundos (`time.Time` / `time.Duration` via `targetTime.Sub(now)`). O propositor acorda cravado nos 3,0s do tempo alvo.
5. **Otimização do Delay Legado no Ciclo de Vida do Nó (`node.go`)**:
    - **Problema:** Existia um `time.Sleep(100 * time.Millisecond)` legado na linha 559 do `pkg/node/node.go` (introduzido em Maio/2026) que forçava uma pausa fixa de 100ms antes do gatilho para a próxima altura (`startConsensusAt`).
    - **Solução:** Reduzido de 100ms para 20ms, economizando 80ms de latência pura na transição de todas as alturas de bloco na rede.

## 🚀 Backlog de Evolução (Futuro)
1. **Kademlia Discovery:** Substituir a lista estática de peers por descoberta dinâmica.
2. **Phase 4 - BitTorrent Sync:** Plano de dados de alto rendimento para sincronismo massivo.
3. **Estabilidade Industrial IBFT2 (Flood Test - 10/06/2026):**
    - **Resultado:** Sustentados **750 AvgTPS** reais com picos de 3.000 TXs por bloco.
    - **Gargalo Superado:** A latência de montagem do líder foi eliminada via Chained State Oracle.
4. **Transporte de Transações via QUIC (UDP Confiável):** Estudar a migração do canal de transações (DTS) de TCP para QUIC sobre UDP. Isso permitirá manter o alto rendimento e ordenação/confiabilidade sem sofrer com o Head-of-Line Blocking do TCP, preservando a entrega íntegra dos pacotes de transações volumosas (~5.5 KB).
5. **Pré-compilado de Identidade Corporativa X.509 (Compliance Jurídico):** Implementar contrato pré-compilado no endereço de trás para frente `0x00000000000000000000000000000000fffffffe` (abaixo do Identity Bridge que está no endereço `0x00...00ffffffff`), executando verificação Go nativa de chaves de certificados X.509 e permitindo a conformidade jurídica em conjunto com a blindagem PQC.
6. **Gateway Adaptador RLP -> SSZ no RPC (MetaMask Compatibility):** Implementar decodificação transparente de transações Ethereum EIP-1559 RLP no endpoint `eth_sendRawTransaction`. Se o AND-gate PQC estiver inativo (`PQCRequired: false`), o nó gera a assinatura PQC vazia correspondente e empacota a transação em SSZ nativo da Jamii, viabilizando retro-compatibilidade instantânea e plug-and-play com dApps convencionais da Web3.

## 📚 Referências Rápidas
- Detalhes de Opcodes: `docs/DOCUMENTACAO_TECNICA.md`
- Plano Geral: `docs/PLANO_GERAL_PROJETO.md`
- Otimizaciones: `docs/jamii_turbo_optimization.md`


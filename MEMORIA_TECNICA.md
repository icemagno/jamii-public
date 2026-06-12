# 🧠 Jamii - Memória Técnica Consolidada

Este arquivo serve como o "cérebro" de longo prazo para o desenvolvimento, permitindo que a IA mantenha um contexto enxuto e rápido.

## 🏛️ Arquitetura Core (Imutável)
- **VM:** Ordem LIFO (Top op Next), aritmética industrial, flags de overflow integradas no `types`. 
- **Criptografia:** Híbrida (ML-DSA + Secp256k1). Identidade soberana e mirror (0x) compartilham o mesmo payload de 20 bytes derivado da Secp256k1.
- **Endereçamento:** `jamii1...` (Sovereign) e `0x...` (Mirror/EIP-55) são a mesma conta no StateDB.
- **Armazenamento:** Arquitetura Bonsai/SSZ com suporte a Verkle Trees (default) e SMT (Sparse Merkle Trie) através da Trie Factory.

## 🛠️ Padrões de Desenvolvimento
- **Compliance:** 100% Besu/Geth logic (Yellow Paper).
- **Módulos Homologados:** `types`, `encoding`, `crypto`, `store`, `trie`, `wallet`. Alterações exigem auditoria.
- **Testes:** Soberanos. Não alterar testes para corrigir bugs.

- **Concluído:** Unified Identity, **Sovereign Transaction V1 (EIP-1559 Native)**, Mirroring nativo de saldo, Dívida Técnica Bloqueante (Gas Price, ReceiptsRoot, Buy Gas, VM Integration, JSON-RPC Read-only), Rede P2P (DTS Engine), MemPool (Gestão de transações pendentes com Purga Descendente), Sincronismo Determinístico (Besu-style), Conformidade Industrial IBFT2 (Pacing, Configurable Timeouts), Segurança Sync-to-Consensus (Observer Mode), Saneamento de Logs (Critical Level), Ghost Root Killer (Determinismo de Blocos Vazios), Resiliência de Sincronia (Channel Drainage), Compactação Soberana (Storage Optimization), Tsunami PQC Test (10.000 TXs Sustentadas), **Blindagem de Mercado (London/Besu Logic)**, **Otimização de Cache Industrial (MaxWarmTries Guard)**, **Sincronização Atômica Sync-Consensus (Anti-Self-Sabotage)**, **Chained State Oracle (Active Speculation)**, **Desacoplamento do SDK Java (Pure SDK)**, **Wallet Web App de Exemplo (Interativa)** e **Terminologia de Estado (Verkle/SMT).**

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
    - **Resultado:** Os logs de canais de conexão do DTS, logs de status do nó, logs de verificação de assinaturas criptográficas PQC (ML-DSA) e logs do propositor designado/watchdog do consenso agora exibem o nome lógico do validador em vez do hash Bech32 bruto.
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
    - **Métricas:** 58.200 atualizações de estado processadas com pico de Heap em 907MB.
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



## 🚀 Backlog de Evolução (Futuro)
1. **Kademlia Discovery:** Substituir a lista estática de peers por descoberta dinâmica.
2. **Phase 4 - BitTorrent Sync:** Plano de dados de alto rendimento para sincronismo massivo.
3. **Estabilidade Industrial IBFT2 (Flood Test - 10/06/2026):**
    - **Resultado:** Sustentados **750 AvgTPS** reais com picos de 3.000 TXs por bloco.
    - **Gargalo Superado:** A latência de montagem do líder foi eliminada via Chained State Oracle.

## 📚 Referências Rápidas
- Detalhes de Opcodes: `docs/DOCUMENTACAO_TECNICA.md`
- Plano Geral: `docs/PLANO_GERAL_PROJETO.md`
- Otimizações: `docs/jamii_turbo_optimization.md`

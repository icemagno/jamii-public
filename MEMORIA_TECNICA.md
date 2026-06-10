# 🧠 Jamii - Memória Técnica Consolidada

Este arquivo serve como o "cérebro" de longo prazo para o desenvolvimento, permitindo que a IA mantenha um contexto enxuto e rápido.

## 🏛️ Arquitetura Core (Imutável)
- **VM:** Ordem LIFO (Top op Next), aritmética industrial, flags de overflow integradas no `types`. 
- **Criptografia:** Híbrida (ML-DSA + Secp256k1). Identidade soberana e mirror (0x) compartilham o mesmo payload de 20 bytes derivado da Secp256k1.
- **Endereçamento:** `jamii1...` (Sovereign) e `0x...` (Mirror/EIP-55) são a mesma conta no StateDB.
- **Armazenamento:** SMT (Sparse Merkle Trie) com arquitetura Bonsai/SSZ.

## 🛠️ Padrões de Desenvolvimento
- **Compliance:** 100% Besu/Geth logic (Yellow Paper).
- **Módulos Homologados:** `types`, `encoding`, `crypto`, `store`, `trie`, `wallet`. Alterações exigem auditoria.
- **Testes:** Soberanos. Não alterar testes para corrigir bugs.

- **Concluído:** Unified Identity, **Sovereign Transaction V1 (EIP-1559 Native)**, Mirroring nativo de saldo, Dívida Técnica Bloqueante (Gas Price, ReceiptsRoot, Buy Gas, VM Integration, JSON-RPC Read-only), Rede P2P (DTS Engine), MemPool (Gestão de transações pendentes com Purga Descendente), Sincronismo Determinístico (Besu-style), Conformidade Industrial IBFT2 (Pacing, Configurable Timeouts), Segurança Sync-to-Consensus (Observer Mode), Saneamento de Logs (Critical Level), Ghost Root Killer (Determinismo de Blocos Vazios), Resiliência de Sincronia (Channel Drainage), Compactação Soberana (Storage Optimization), Tsunami PQC Test (10.000 TXs Sustentadas), **Blindagem de Mercado (London/Besu Logic)**, **Otimização de Cache Industrial (MaxWarmTries Guard)** e **Sincronização Atômica Sync-Consensus (Anti-Self-Sabotage).**

## 🛠️ Decisões Recentes (05/06/2026)
1. **Pivot Arquitetural: Descarte da Execução Especulativa:**
    - **Contexto:** Tentativa de paralelizar a execução do bloco $N+1$ durante o commit do bloco $N$.
    - **Conclusão:** **Fracasso definitivo.** A dependência sequencial de estado impede a especulação segura; a execução sobre estados "fantasmas" gera divergência de StateRoot.
    - **Descoberta de Gargalo:** O limite de performance não é o disco (SSD), mas o custo de CPU para **geração de provas IPA** (CPU-bound). O sistema gasta mais tempo "calculando a árvore" do que "gravando no banco".
    - **Mandato:** Focar em otimizações matemáticas (MSM/Pippenger) e na hierarquia de nós Stateless em vez de paralelismo especulativo.

2. **Estratégia de Rede Híbrida (Stateless Hierarchy):**
    - **Definição:** Proposers são nós Stateful de alta performance (HPC) que geram a **Block Witness**.
    - **Nós de Borda:** Implementação de nós Stateless que utilizam a Witness do Skeleton para verificação ultra-rápida, eliminando a necessidade de armazenamento local massivo.
    - **MemPool Otimista:** Nós Stateless não aceitam TXs via RPC, confiando na filtragem prévia dos nós Stateful (P2P-only ingress) para mitigar ataques de DoS.
    - **Peer Scoring:** Implementação obrigatória de motor de reputação e lista negra para isolar vizinhos maliciosos.

3. **Saneamento de Configuração e Setup:**
    - **Ação:** Remoção completa do campo `network_mode` (redundante).
    - **Novo Utilitário:** Implementado `jamii setup generate-key` para criação autônoma de identidades híbridas PQC.

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
2. **Resiliência de Ressurreição (Consensus Halt Guard):**
    - **Ação:** Implementado reset automático da trava `lastStartedHeight` quando o quórum de rede é perdido e recuperado.
    - **Impacto:** Fim do travamento manual necessário para "acordar" nós que ficaram isolados ou caíram por tempo prolongado.

## 🛠️ Decisões Recentes (06/06/2026)
1. **Witness-Aided Quorum Acceleration (Aceleração por Testemunha):**
    - **Ação:** Introdução da **Block Witness** no Skeleton do bloco. O Proposer agora envia os estados iniciais (contas/slots) necessários para a execução.
    - **Benefício:** Permite que validadores executem o bloco de forma "Stateless" e otimista.
    - **Mitigação de Banda:** O aumento no tamanho do Skeleton devido à Witness foi neutralizado pela implementação prévia de **Bi-Polar Short IDs**, mantendo a eficiência de rede.
    - **Resultado:** Validação de blocos em validadores agora pula a matemática pesada de Verkle (IPA/MSM) durante a fase de votação (Prepare/Commit).

2. **Mecanismo de Promoção de Estado (State Promotion):**
    - **Problema:** O Proposer executava o bloco duas vezes (uma para propor, outra no commit final), duplicando o esforço de CPU-bound mais caro: a prova de StateRoot.
    - **Solução:** Implementado o cache de **Sandbox State** no `PayloadPool`. O resultado da execução (nós da Verkle Tree e IPA commitments em RAM) é preservado.
    - **Ação:** No momento da finalização, o nó realiza a **Promoção Atômica**: move as mutações do sandbox diretamente para o estado canônico, saltando a re-execução da VM e o re-cálculo da árvore.
    - **Resultado:** Redução de **~90%** no tempo de processamento do Proposer na fase de commit. O esforço pesado é feito apenas uma vez por bloco.

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

## 🛠️ Decisões Recentes (21/05/2026)
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
11. **Otimização de Cache Industrial (Sprint 4.1 - 21/05/2026):**
    - **Ação:** Implementado e validado o uso da constante `MaxWarmTries` para gerenciar o cache de contas e contratos no `StateDB`.
    - **Descoberta:** Testes de estresse provaram que manter árvores Verkle na RAM por longos períodos degrada a performance devido ao custo recursivo do `Prune()` e pressão no Garbage Collector.
    - **Decisão Final:** Adotada a política de **Limpeza Agressiva de Cache**. O cache agora é mantido entre blocos (quente), mas é 100% resetado assim que atinge o limite de 512 entradas em contas ou contratos.
    - **Resultado:** Estabilização do throughput em **300 AvgTPS** com consumo de RAM previsível (~890MB) sob carga de flood industrial.

13. **Persistência de Recibos e TxIndex (Sprint 4.2 - 28/05/2026):**
    - **Ação:** Implementada a gravação física de recibos de execução no PebbleDB e o índice de transações (TxHash -> Localização).
    - **Eliminação de Mocks:** Os recibos agora refletem o status real (`Status`), gás acumulado (`CumulativeGasUsed`) e o hash original da transação, salvos sob o prefixo `r:`.
    - **Busca em O(1):** A API RPC `eth_getTransactionReceipt` foi refatorada para utilizar o prefixo `txi:`, eliminando a busca linear custosa nos blocos recentes.
    - **Resultado:** Consultas de recibos pelo SDK Java tornaram-se instantâneas e fidedignas, garantindo a integridade do ciclo de vida de contratos inteligentes.

15. **Invocação Read-Only e Ciclo de Vida Completo (Sprint 4.2):**
    - **Ação:** Implementado o método JSON-RPC `eth_call` no servidor Go e suporte correspondente no SDK Java.
    - **Execução Virtual:** O `eth_call` instancia uma EVM com o estado atual (StateRoot) e executa o código de forma isolada, permitindo consultas de estado de contratos sem gerar transações ou custos de gás on-chain.
    - **Validação Final:** Validado o fluxo unificado (Atomic Deploy + Dynamic Polling + Smart Call) no SDK Java, confirmando a leitura correta de saldos e estado global de dentro da EVM.

14. **Veto à Paralelização Externa de Trie (Sprint 4.1):**
    - **Experimento:** Tentativa de paralelizar o `updateDirtyStorage` do `StateDB` (processar múltiplos contratos simultaneamente).
    - **Resultado:** **Falha por regressão**. O overhead de gestão de goroutines, disputa de locks no banco e o pico de memória (subiu para 2.1GB) derrubaram o TPS de 300 para 218.
    - **Conclusão Técnica:** O paralelismo em Verkle Trees deve ser focado **dentro** da estrutura da árvore (internal node branching) e não na orquestração externa de múltiplas árvores pequenas, especialmente em cenários de alta expansão de endereços (flood).

## 🚀 Backlog de Evolução (Futuro)
1. **Kademlia Discovery:** Substituir a lista estática de peers por um sistema de descoberta dinâmica baseada na distância de hashes.
2. **Phase 4 - BitTorrent Sync:** Implementar o plano de dados de alto rendimento para sincronismo massivo de estado.
3. **Estabilidade Industrial IBFT2 (Flood Test - 21/05/2026):**
    - **Resultado:** Sustentados **300 AvgTPS** reais com persistência em disco (PebbleDB).
    - **Gargalo Superado:** O "Cold Start" de contas entre blocos foi mitigado pelo cache quente monitorado, recuperando 10% de performance em relação ao estado anterior.
    - **Eficiência Verkle:** Confirmada como o componente mais sensível à latência. A estratégia de limpeza total de cache mostrou-se superior ao gerenciamento de estado persistente em memória para a versão atual da árvore.

## 📚 Referências Rápidas
- Detalhes de Opcodes: `docs/DOCUMENTACAO_TECNICA.md`
- Plano Geral: `docs/PLANO_GERAL_PROJETO.md`
- Otimizações: `docs/jamii_turbo_optimization.md`

## Processo de consenso
 - Teste de 10.000 TXs (ML-DSA) rodou com sucesso em blocos de 1.000 TXs cada.
 - Avaliação
  1. Performance e Throughput
   * Carga Sustentada: A rede processou 10 blocos de 1.000 TXs cada sem interrupções.
   * Eficiência de CPU: O pico de uso foi de 16%, demonstrando que a verificação de assinaturas Dilithium não é um gargalo para o hardware atual.
   * Throughput: Estabilizado entre 250 a 500 TPS reais (incluindo tempo de rede e disco).

  2. Estabilidade do Consenso sob Carga
   * Round Changes: Observados alguns timeouts e mudanças de round durante a propagação de blocos cheios.
   * Motivo: O tempo de montagem e propagação de 1.000 TXs (com assinaturas Dilithium grandes) excedeu o limite do Round 0.
   * Recuperação: O IBFT2 contornou os atrasos saltando de round, garantindo a finalização segura dos blocos pesados.

  3. Diferenciais Técnicos Observados
   * Segurança Pós-Quântica (PQC): Uso intensivo de assinaturas Dilithium validado em massa.
   * Compact Blocks: Eficiência de 100% na reconstrução de blocos via MemPool durante o flood.
   * Compactação PebbleDB: Redução real de espaço em disco pós-teste confirmada (75MB final).

  4. Conclusão do Estado Atual
  A Jamii Blockchain atingiu o nível de **Escalabilidade PQC Homologada**. A arquitetura prova-se capaz de sustentar volumes industriais de transações com segurança criptográfica de próxima geração.

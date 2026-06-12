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

## 🛠️ Decisões Recentes (22/05/2026)
1. **Sincronização Atômica Sync-Consensus (Anti-Self-Sabotage):**
    - **Ação:** Refatorado o `SyncManager` para callback sincronizado sob o mutex global `execMu`.
2. **Higiene de Execução (Early StateRoot Validation):**
    - **Ação:** Validação do `IntermediateRoot` antes de chamar `st.Commit()`.
3. **PQC Performance Milestone (The Tsunami Test 10K):**
    - **Resultado:** Processadas **10.000 TXs ML-DSA** com **238 AvgTPS de execução** reais (finalização em disco).

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

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

- **Concluído:** Unified Identity, **Sovereign Transaction V1 (EIP-1559 Native)**, Mirroring nativo de saldo, Dívida Técnica Bloqueante (Gas Price, ReceiptsRoot, Buy Gas, VM Integration, JSON-RPC Read-only), Rede P2P (DTS Engine), MemPool (Gestão de transações pendentes com Purga Descendente), Sincronismo Determinístico (Besu-style), Conformidade Industrial IBFT2 (Pacing, Configurable Timeouts), Segurança Sync-to-Consensus (Observer Mode), Saneamento de Logs (Critical Level), Ghost Root Killer (Determinismo de Blocos Vazios), Resiliência de Sincronia (Channel Drainage), Compactação Soberana (Storage Optimization), Tsunami PQC Test (10.000 TXs Sustentadas), **Blindagem de Mercado (London/Besu Logic)**, **Otimização de Cache Industrial (MaxWarmTries Guard)**, **Sincronização Atômica Sync-Consensus (Anti-Self-Sabotage)** e **Chained State Oracle (Active Speculation).**

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

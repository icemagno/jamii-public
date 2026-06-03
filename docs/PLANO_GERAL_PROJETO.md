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
- [ ] **Motor de Chamada (Contract Call):**
    - [ ] Implementar o roteamento de transações para o interpretador da JamiiVM.
    - [ ] Adicionar campo `ContractAddress` nos Recibos (`Receipt`) para rastreabilidade de deploy.
- [ ] **Integração de Pré-compilados:**
    - [ ] Contrato `0x100`: Conversão Nativa ultra-rápida ETH <-> Jamii Address (Bech32).
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
- [ ] **Sovereign Identity Precompiled:** Implementar contrato pré-compilado em Go (ex: endereço `0x100`) para conversão ultra-rápida de `address` (ETH) para `string` (Jamii1/Bech32).
- [ ] **Jamii Dev Kit (Solidity):** Criar biblioteca de ferramentas oficiais para abstrair chamadas de baixo nível (`staticcalls`) aos pré-compilados da Jamii, garantindo segurança e tipagem forte para desenvolvedores de dApps.
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

### 🦴 Ultra-Compactação de Skeleton Blocks (Bi-Polar Short IDs)
Para minimizar a largura de banda durante o consenso sem perder a precisão:
- [ ] **Bi-Polar Short IDs:** Em vez de transmitir hashes de 32 bytes das transações, o Proposer enviará um identificador de **6 bytes** composto pelos **3 primeiros e 3 últimos bytes** do hash original (ex: `[0:3] + [29:32]`).
- [ ] **Mitigação de Colisão:** O espaçamento bi-polar aumenta a entropia em relação a um truncamento sequencial. Caso (raro) ocorra colisão na MemPool do nó receptor, o nó utilizará o `TxRoot` do cabeçalho como prova matemática para rejeitar a montagem incorreta e solicitar a transação específica via P2P.
- [ ] **Impacto:** Redução de 48 KB para **~9 KB** por bloco (para 1.500 TXs), melhorando a resiliência em redes de alta latência e reduzindo picos de I/O na placa de rede.

### 🏛️ Arquitetura de Plano Duplo (Dual-Plane Transport)
Após estudo técnico, a Jamii adotará uma estratégia híbrida para o transporte de dados:

1.  **Plano de Controle (DTS Custom):**
    *   **Foco:** Baixa latência.
    *   **Responsabilidade:** Votos de consenso (IBFT2), sinalização de rounds, compact blocks e transações individuais.
2.  **Plano de Dados (BitTorrent via `anacrolix/torrent`):**
    *   **Foco:** Alto rendimento (Throughput).
    *   **Responsabilidade:** Download massivo de blocos históricos (State Sync), propagação de blocos cheios (Full Blocks) e distribuição de snapshots de estado.
    *   **Benefício:** Utiliza o poder do *Swarm* (enxame) para baixar dados de múltiplos vizinhos simultaneamente, eliminando o gargalo de propagação em blocos de grande porte.

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

1. **Besu/Geth como Norte:** Sempre consultar a lógica dessas implementações antes de propor mudanças arquiteturais.
2. **Desempenho PQC:** O custo computacional do ML-DSA não deve ser subestimado. O paralelismo é a regra.
3. **Imutabilidade de Finalidade:** O IBFT 2.0 garante finalidade imediata. O código deve impedir qualquer tentativa de reorg em nível de motor.
5. **Soberania de Logs:** A saúde do nó deve ser visível sem necessidade de ferramentas externas; o log industrial é a primeira linha de defesa.
6. **Manifesto Pétreo (Stony Config):** Parâmetros de rede definidos na gênese (ChainID, FreeGas, etc.) são imutáveis e protegidos pelo banco de dados; a tentativa de rodar um node com configurações divergentes do banco deve abortar a execução imediatamente.
7. **Sincronismo de Segurança (Sync-to-Consensus):** Um nó validador nunca deve enviar votos de consenso (Prepare/Commit) enquanto sua altura local estiver atrasada em relação à rede. O nó deve atuar como "Observador" até que o sincronismo total seja atingido, com exceção do nó de Bootstrap ou em modo Solo.

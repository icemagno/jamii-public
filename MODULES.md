===================================================================================
                   JAMII BLOCKCHAIN: ARQUITETURA MODULAR (v2.0)
===================================================================================

[ NÍVEL 4: CAMADA DE APLICAÇÃO E SERVIÇOS ]
 
   +-------------------------+                 +-------------------------+
   |       pkg/wallet        |                 |         pkg/rpc         |
   | (Gestor de Ativos/ID)   |                 | (Portão de Integração)  |
   +-------------------------+                 +-------------------------+
       |                 |                          |                 |
       v                 v                          v                 v
 (Usa pkg/crypto)   (Envia p/ Node)           (Consulta Blockchain/Core)

...................................................................................
[ NÍVEL 3: CAMADA DE ORQUESTRAÇÃO E CONSENSO ]

   +-------------------------+                 +-------------------------+
   |      pkg/mempool        | <=============> |      pkg/consensus      |
   | (Gestão de Pendentes)   |                 | (Motor IBFT 2.0 / PQC)  |
   +-------------------------+                 +-------------------------+
       |                                            |
       v                                            v
   +-------------------------+                 +-------------------------+
   |         pkg/vm          | <=============> |        pkg/core         |
   |      (Motor EVM)        |  (Interface de  | (Orquestrador Soberano) |
   +-------------------------+   Estado/Core)  +-------------------------+
                                                    |     |     |     |
                               +--------------------+     |     |     +---------+
                               |                          |     |               |
                               v                          v     v               v
                        (Valida Signatures) (Estrutura Blocos) (Gere StateDB) (Executa Contratos)

...................................................................................
[ NÍVEL 2: CAMADA DE REDE E ESTADO ]

   +-------------------------+                 +-------------------------+
   |         pkg/dts         |                 |        pkg/trie         |
   | (Gossip/Block Sharding) |                 | (Autenticador de Estado)|
   +-------------------------+                 +-------------------------+
       |                 |                          |                 |
       v                 v                          v                 v
 (Comunica Peers)  (Propaga Blocos)          (Gera Hashes)     (Bonsai Turbo)

...................................................................................
[ NÍVEL 1: CAMADA DE INFRAESTRUTURA E PERSISTÊNCIA ]

   +-------------------------+                 +-------------------------+
   |       pkg/crypto        |                 |        pkg/store        |
   |   (Guardião Quântico)   |                 |  (PebbleDB Persistence) |
   +-------------------------+                 +-------------------------+
               |                                            |
               v                                            v
         (Gera Digests)                           (Atomic Commit SSD)

...................................................................................
[ NÍVEL 0: CAMADA FUNDACIONAL ]

   +-------------------------+                 +-------------------------+
   |       pkg/encoding      |                 |        pkg/types        |
   |  (SSZ / Sovereign V1)   |                 | (Gramática da Fundação) |
   +-------------------------+                 +-------------------------+
===================================================================================

# 🏗️ Arquitetura Modular Jamii Blockchain: Tratado de Interdependências

A Jamii Blockchain é fundamentada em uma arquitetura de camadas estritamente desacopladas e soberanas. O design atual (v2.0) reflete a transição para a **Era Sovereign V1**, onde o mercado de taxas dinâmico e a segurança híbrida são nativos e obrigatórios em todos os níveis do sistema.

## 🧱 Módulo: pkg/types (A Gramática da Fundação e Identidade Unificada)

O módulo pkg/types constitui a gramática binária fundamental. Sua principal atribuição atual é garantir a **Identidade Unificada (Sprint 3.6)**: 
*   Ambos os endereços Mirror (v0) e Soberano (v2) compartilham o **mesmo payload de 20 bytes** derivado da Secp256k1. 
*   Isso garante que ambos os formatos de endereço apontem para a mesma folha na SMT, permitindo o espelhamento automático de saldo.
*   Ele fornece as primitivas `Address`, `Hash` e `Uint256` (32 bytes) com rigidez binária absoluta.

## 📑 Módulo: pkg/encoding (O Tradutor SSZ e Mercado EIP-1559)

O módulo pkg/encoding evoluiu para o modelo **Sovereign V1**. Ele implementa a serialização SSZ (Simple Serialize) obrigatória para transações baseadas no mercado de taxas dinâmico (Londres/Besu).
*   **Decisão Industrial:** O campo `GasPrice` foi removido da assinatura binária para garantir a imutabilidade do hash do usuário perante a volatilidade da rede.
*   **Validação Zero-Gap:** Impõe tamanhos fixos estritos para todos os campos de valor (32 bytes), protegendo contra ataques de injeção de dados.

## 🛡️ Módulo: pkg/crypto (O Guardião PQC e Strong Binding)

O módulo pkg/crypto provê a **Segurança Híbrida Soberana**. Ele exige a validação simultânea `Verify(Secp256k1) AND Verify(ML-DSA-65)`.
*   **Strong Binding:** Vincula a mensagem original às duas chaves públicas através de um digest único, impedindo o reuso de provas em identidades diferentes.
*   **Higiene Forense:** Implementa o `Memory Wiping` físico em todas as chaves privadas para impedir a extração de segredos via dumps de memória RAM.

## 💾 Módulo: pkg/store (Persistência Atômica PebbleDB)

O módulo pkg/store gerencia a interface com o PebbleDB. Ele é o responsável por garantir que o **Commit Atômico** no SSD ocorra sem corrupção, mesmo em quedas de energia. Ele organiza os dados em partições (`n:` para nós da trie, `d:` para dados planos, `m:` para metadados), permitindo a recuperação rápida do estado mundial.

## 🌳 Módulo: pkg/trie (Trie Factory e Bonsai Turbo)

O módulo pkg/trie é o coração do World State. Ele utiliza a **Trie Factory** para alternar entre SMT e Verkle Trees. 
*   **Bonsai Turbo:** Implementa a separação entre a árvore de autenticação e o armazenamento plano, acelerando a execução da VM em até 400%.
*   **Verkle Native:** Suporta provas compactas e aridade 256 para permitir a execução stateless futuramente.

## 📡 Módulo: pkg/dts (Gossip Protocol e Block Sharding)

O módulo **pkg/dts** (Distributed Transport Service) é a camada de comunicação P2P.
*   **Gossip Resiliente:** Utiliza propagação não-linear para disseminar propostas de bloco pesadas (assinaturas PQC).
*   **Block Sharding:** Fragmenta blocos grandes para otimizar o uso de banda em redes globais.
*   **Deduplicação:** Filtra hashes de transações e blocos já conhecidos para mitigar ataques de flood na camada de rede.

## 🗄️ Módulo: pkg/mempool (Gestão de Pendentes e Purga Descendente)

O módulo **pkg/mempool** gerencia as transações aguardando inclusão em blocos.
*   **Barreira Ativa:** Rejeita transações com taxas inferiores ao `BaseFee` atual do cabeçalho da cadeia.
*   **Purga Descendente:** Implementa a lógica de expulsão em cascata: se uma transação falha (por nonce ou saldo), todos os seus sucessores são removidos para manter o cache saudável e evitar "ataques fantasmagóricos".

## 🧠 Módulo: pkg/core (O Orquestrador Soberano e StateProcessor)

O módulo pkg/core é o sistema nervoso central. Ele orquestra o **StateProcessor**, que segue o rigor industrial do Besu:
*   **Atomicidade de Execução:** Utiliza Journaling para garantir que falhas na VM revertam todas as transferências de valor da transação.
*   **Ghost Root Killer:** Garante que blocos vazios herdem deterministicamente a raiz do pai.
*   **Buy/Refund Gas:** Gerencia o fluxo financeiro de taxas, protegendo o saldo do remetente durante a transição de estado.

## 🤝 Módulo: pkg/consensus (IBFT 2.0 e Crash-Resilience)

O módulo pkg/consensus implementa o **IBFT 2.0**.
*   **Finalidade Imediata:** Garante que um bloco confirmado nunca sofra reorg.
*   **Validação Ativa:** Um validador só emite votos de `Commit` após a execução total e bem-sucedida do bloco via `pkg/core`.
*   **Persistent Seals:** Salva assinaturas de consenso no disco para permitir que o nó reinicie sem perder a integridade da rodada atual.

## 🔑 Módulo: pkg/wallet (Gestor Soberano e Anti-Forensics)

O módulo pkg/wallet gerencia a semente mestre e as chaves híbridas.
*   **Derivação BIP-Jamii:** Gera chaves clássicas e quânticas independentes a partir das mesmas 12 palavras.
*   **Blindagem Keystore:** Armazena segredos usando AES-256-GCM e Scrypt com alta iteração para impedir ataques de força bruta.
*   **Memory Destruction:** Destrói mnemônicos e segredos da RAM imediatamente após a geração do blob de assinatura.

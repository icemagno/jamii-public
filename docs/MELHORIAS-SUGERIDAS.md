# Melhorias Sugeridas (Baseadas no go-ethereum - Geth)

Este documento lista otimizações e padrões industriais encontrados no projeto `go-ethereum` que podem ser aplicados aos módulos equivalentes da blockchain Jamii para elevar sua robustez e performance.

## 1. Módulo Types (Equivalente: `common/types.go`)
* **Uso de Arrays Fixos para Hash/Address:** O Geth utiliza `type Hash [32]byte` e `type Address [20]byte`. No Jamii, usamos fatias (`[]byte`) para *Hash Agility*. 
    * *Sugestão:* Criar tipos especializados para os tamanhos mais comuns (ex: `Hash256`, `Hash512`) que sejam arrays fixos para evitar alocações no heap, mantendo a interface dinâmica apenas onde necessário.
* **Checksum de Endereço (EIP-55):** O Geth utiliza capitalização mista (ex: `0x5aAeb...`) para incluir um checksum na string hexadecimal do endereço.
    * *Sugestão:* Implementar suporte ao EIP-55 no método `Mirror()` para prevenir erros de digitação em ferramentas compatíveis com EVM.

## 2. Módulo Core/State (Equivalente: `core/state/`)
* **Mecanismo de Journaling:** O Geth utiliza um `journal` para rastrear cada mudança atômica no estado (saldos, nonces, etc.). Isso permite reverter o estado de forma granular e segura.
    * *Sugestão:* Substituir o snapshot de "cópia total" do Jamii por um sistema de journal para garantir atomicidade real e resolver definitivamente as falhas de `Snapshot/Revert`.
* **Deep Clone de Contas:** O Geth trata o `Account` como um objeto complexo que exige cópia profunda de todos os campos.
    * *Sugestão:* Refatorar o `Snapshot` do Jamii para garantir que o ponteiro `Balance (*Uint256)` seja fisicamente duplicado, eliminando o risco de corrupção de memória.

## 3. Módulo Encoding (Equivalente: `rlp/`)
* **Buffer Recycling (Encoder):** O RLP do Geth utiliza `sync.Pool` de forma extremamente agressiva e centralizada para gerenciar buffers de codificação de todos os tamanhos.
    * *Sugestão:* Padronizar o uso do `util.SigPool` em todas as operações de `Encode`, garantindo que o ciclo de vida (Get/Put) ocorra sempre dentro da mesma função para evitar vazamentos.

## 4. Módulo Wallet/Keystore (Equivalente: `accounts/keystore/`)
* **Parâmetros Scrypt Dinâmicos:** O Geth define `StandardScryptN = 1 << 18` para segurança máxima e `LightScryptN = 1 << 12` para testes ou dispositivos leves.
    * *Sugestão:* O Jamii já adotou `1 << 17`. Poderíamos oferecer perfis de segurança (Standard/Light) no Keystore para flexibilidade do usuário.
* **Validação de Integridade Pós-Escrita:** Ao criar um Keystore, o Geth tenta descriptografá-lo imediatamente para garantir que o arquivo não foi corrompido e que a senha funciona antes de confirmar ao usuário.
    * *Sugestão:* Implementar este "double-check" no `wallet.Create`.

## 5. Módulo SMT/Trie (Equivalente: `trie/`)
* **Node Batching:** O Geth agrupa a escrita de múltiplos nós da árvore em lotes otimizados para o banco de dados subjacente (LevelDB/PebbleDB).
    * *Sugestão:* O Jamii já usa batches, mas poderíamos implementar a técnica de `Commit` que decide quais nós valem a pena persistir e quais podem ser mantidos apenas no cache de RAM para blocos efêmeros.

## 6. Módulo Crypto (Equivalente: `crypto/`)
* **Reset Explícito de Hashers:** O Geth garante que cada hasher retirado de um pool seja resetado *antes* e *depois* do uso.
    * *Sugestão:* O Jamii já implementou o Reset duplo, mas poderíamos estender isso para todas as bibliotecas externas via wrappers mais rígidos.

## 7. Módulo Core/State (Aprofundamento: Journaling)
* **Estado de Objetos Vivos (`stateObject`):** O Geth separa a representação da conta no banco (`Account`) do objeto manipulado em memória (`stateObject`). Isso permite rastrear mudanças sujas de forma muito mais granular.
    * *Sugestão:* Criar uma struct `stateObject` no Jamii para encapsular contas carregadas, facilitando o rastreamento de mutações e cache.
* **Journal de Reversão:** Em vez de copiar o mapa inteiro de contas (Snapshot), o Geth registra cada pequena alteração (ex: "Saldo da conta X mudou de 10 para 5") em uma lista sequencial (Journal).
    * *Sugestão:* Implementar o padrão `journalEntry` para operações de saldo, nonce e código. O `Revert` deve apenas percorrer o journal de trás para frente desfazendo as ações. Isso resolve 100% da falha de atomicidade detectada.

## 8. Módulo VM (Equivalente: `core/vm/` e `core/state_transition.go`)
* **Camada de Transição de Estado:** O Geth possui um arquivo dedicado (`state_transition.go`) que orquestra a execução de uma transação ANTES de entrar na VM (verificação de assinatura, saldo para gás, nonce).
    * *Sugestão:* Isolar a lógica de "pré-voo" da transação em um objeto `StateTransition` para manter o `Executor` da VM focado apenas na execução lógica.
* **Gas Pooling e Refund:** O Geth utiliza um `GasPool` para gerenciar o gás disponível no bloco e um mecanismo de reembolso para operações que limpam o estado.
    * *Sugestão:* Evoluir o sistema de gás do Jamii para suportar reembolsos e limites de bloco mais rigorosos.
* **Snapshot de Snapshot (Nested):** A VM do Geth permite criar snapshots dentro de snapshots (chamadas de contratos aninhadas).
    * *Sugestão:* O sistema de Journal sugerido acima permite nativamente snapshots aninhados apenas guardando o índice atual do journal.

## 9. Concorrência e Performance (Core)
* **Double-Check Locking Real:** O Geth utiliza o padrão `sync.Once` ou verificações duplas rigorosas após locks de escrita para evitar reinicialização de objetos de estado.
    * *Sugestão:* Aplicar a verificação de existência obrigatória dentro do `Lock` de escrita no `GetAccount` do Jamii.
* **Trie Prefetcher:** O Geth lança goroutines para buscar nós da árvore em disco ANTES da VM precisar deles, baseando-se nos endereços acessados pela transação.
    * *Sugestão:* Implementar um prefetcher básico para carregar contas da SMT em paralelo com a validação de assinatura.

---
## 10. Módulo Store/Trie (Dilema: Forest vs. Bonsai)
* **Modelo Forest (Histórico e Análise):** No modelo tradicional (utilizado pelo Geth original), cada nó da árvore é indexado pelo seu Hash. 
    * *Vantagem:* Permite manter múltiplos estados históricos simultaneamente (Archive Nodes). 
    * *Desvantagem:* Leituras lentas (O(log N)), exigindo ~10 acessos ao disco para um único saldo.
* **Modelo Bonsai (Performance e Presente):** Inspirado no Besu, este modelo armazena os dados em uma tabela "flat" (`Endereço -> Saldo`).
    * *Vantagem:* Leitura instantânea (O(1)). Redução massiva do tamanho do banco de dados.
    * *Desvantagem:* Reorgs (reorganizações de cadeia) são mais complexos, exigindo "Trie Logs" para desfazer mudanças.
* **Sugestão Estratégica (Bonsai-First):** O Jamii deve priorizar o modelo **Bonsai**. Como uma blockchain focada em assinaturas quânticas pesadas, a latência de disco deve ser minimizada ao extremo. 
    * *Ação:* Implementar a tabela `accounts_flat` como a "Fonte da Verdade" para a VM e manter a SMT apenas para geração de raízes de estado e provas de Merkle.

## 11. Módulo Store (Abstração de Estratégia)
* **Flexibilidade de Motor:** O Besu permite alternar entre Forest e Bonsai via configuração.
    * *Sugestão:* Evoluir a interface `store.Store` para suportar `GetFlat` e `GetTrieNode`, permitindo que o nó Jamii possa ser configurado como "Full Node" (Bonsai) ou "Archive Node" (Forest) conforme a necessidade do operador.

## 11. Módulo VM/Executor (Inspiração: Besu EVM)
* **UInt256 Nativo Otimizado:** O Besu utiliza uma implementação de `UInt256` extremamente otimizada com operações de bits puras (bit-shifting e masking) para evitar o overhead do `BigInteger` do Java (equivalente ao nosso `math/big`).
    * *Sugestão:* Conforme discutido anteriormente, migrar para uma biblioteca de `uint256` nativa no módulo da VM para garantir que a aritmética de 256 bits não seja o gargalo do TPS.
* **Message Frame Architecture:** O Besu organiza a execução de contratos em "Frames" (`MessageFrame`), facilitando o rastreamento de chamadas aninhadas e reversões de estado parciais.
    * *Sugestão:* Adotar a estrutura de `Frame` na `vm.Executor` para suportar chamadas entre contratos de forma limpa e isolada.

## 12. Extensibilidade (Besu Plugin-API)
* **Arquitetura de Plugins:** O Besu é famoso por sua API de plugins que permite estender quase qualquer parte do nó.
    * *Sugestão:* Definir interfaces claras no Jamii para que novos esquemas de assinatura ou regras de consenso possam ser injetados sem alterar o código principal (`core`).

---

## 13. Estratégia de Escalabilidade PQC (Pós-Quântica)
*   **Insight de Homologação (Abril 2026):** Os testes de estresse revelaram uma disparidade de 10x entre a capacidade do codec SSZ (**~250k TPS**) e a velocidade de verificação de assinaturas híbridas ML-DSA (**~23k TPS**).
    *   *Ação 1: Signature Verifier Pool:* Implementar um Worker Pool paralelo para validação de assinaturas na camada de Mempool/Ingestão.
    *   *Ação 2: State Prefetching:* Utilizar o tempo de validação de assinatura para disparar leituras assíncronas do `StateDB` (PebbleDB), movendo contas para o cache RAM antes da execução da VM.
    *   *Ação 3: Execução Concorrente:* Desenhar a **JamiiVM** para executar transações não-conflitantes (que não compartilham endereços) em threads paralelas, utilizando o `StateDB` blindado como fonte da verdade.

---

## 14. Módulo Node/Identity (Otimização de Setup Inicial)
* **Pre-generation of Node Identity:** Seguindo o padrão Besu/Geth, a identidade do nó (chave privada e endereço Bech32) deve ser gerável antes do primeiro boot.
    * *Motivo:* Evitar o "auto-bootstrap" dinâmico que causa divergência de StateRoot entre nós. O endereço do nó deve ser conhecido para ser incluído no `genesis.json` (como validador ou conta alocada) de forma estática.
    * *Sugestão:* Criar um utilitário (ex: `jamii public-key export`) que gere o arquivo `node.key` e exiba o endereço `jamii1...` correspondente, permitindo um setup de rede 100% determinístico e industrial.

---
**Conclusão Final:** 
 A combinação do **Journaling do Geth** (para atomicidade) com o **Bonsai Storage do Besu** (para leitura rápida) é a receita para uma blockchain de altíssima performance. O Jamii deve priorizar a implementação da tabela "flat" de contas para maximizar o TPS real em disco.

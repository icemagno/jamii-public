# 🔬 Pesquisa de Futuro: Evolução JamiiVM (Pós-Cancun)

Este documento rastreia as tendências industriais e mudanças de protocolo identificadas no Geth e Besu que devem ser consideradas para as próximas sprints da JamiiVM.

## 1. EVM Object Format (EOF) - EIP-3540 & EIP-3670
O EOF é a maior mudança na EVM desde sua criação, introduzindo um formato de bytecode estruturado com separação entre código e dados.

**Opcodes Identificados no Geth:**
- **Acesso a Dados:** `DATALOAD` (0xD0), `DATALOADN` (0xD1), `DATASIZE` (0xD2), `DATACOPY` (0xD3).
- **Saltos Estáticos:** `RJUMP` (0xE0), `RJUMPI` (0xE1), `RJUMPV` (0xE2).
- **Funções:** `CALLF` (0xE3), `RETF` (0xE4), `JUMPF` (0xE5).
- **Manipulação:** `DUPN` (0xE6), `SWAPN` (0xE7), `EXCHANGE` (0xE8).
- **Criação EOF:** `EOFCREATE` (0xEC), `RETURNCONTRACT` (0xEE).

**Impacto:** Exigirá uma refatoração em como a `EVM` carrega e valida o bytecode (`analysis.go` no Geth).

## 2. Fork Prague / Osaka (Próximas EIPs)
- **`SLOTNUM` (0x4B):** Expõe o número de slots ocupados ou disponíveis (em discussão).
- **`RETURNDATALOAD` (0xF7):** Leitura direta do buffer de retorno.
- **`EXTCALL` Family (0xF8 - 0xFB):** Novos opcodes de chamada otimizados para EOF.

## 3. Otimizações Jamii PQC (Soberania)
- **Instruções de Mirror Bridge:** Pesquisar opcodes nativos para validação de assinaturas MLDSA/Ed25519 em lote (Batch Verification) para reduzir o custo de gás em Rollups Jamii.
- **Precompilados Híbridos:** Implementação de suporte nativo a Kyber/Dilithium via opcodes especializados.

---
*Atualizado em 28 de Abril de 2026 após homologação de infraestrutura de armazenamento.*

## 4. EIP-7702: Account Abstraction Refinada (Pectra)
Substituindo o EIP-3074, este EIP permite que contas comuns (EOAs) funcionem como Smart Contracts temporariamente durante a execução de um bundle.
- **Impacto na Wallet:** Permite batch transactions, gas sponsorship e permissões granulares sem migração de conta.
- **Implementação:** Exige ajustes no processador de transações (`core/state_processor.go`) para lidar com o novo tipo de transação (Type 4).

## 5. Verkle Trees (Statelessness)
Transição das Merkle Patricia Tries para Verkle Trees utilizando Vector Commitments.
- **Impacto:** Redução drástica no tamanho das provas (witnesses), permitindo que nós validem blocos sem o estado completo.
- **Foco:** Pesquisar integração com as bibliotecas de criptografia PQC já existentes na Jamii para garantir resistência quântica nas provas.

## 6. Execução Paralela Determinística (Besu/Monad Style)
Implementação de execução multi-threaded de transações sem conflitos de estado.
- **Metodologia:** Análise de `accessList` para identificar dependências e paralelizar a execução na JamiiVM.
- **Status Jamii:** A arquitetura atual de isolamento de estado (`pkg/store`) favorece a implementação de um scheduler otimizado.

## 7. Status de Tecnologias Consolidadas
- **[CONCLUÍDO] Armazenamento Estilo Bonsai:** A Jamii já utiliza uma estrutura de armazenamento "flat" (plana) com diferenciais de estado, eliminando o gargalo de amplificação de leitura tradicional das Merkle Patricia Tries clássicas.



  1. Verkle Trees (A mais estratégica agora)
   * Por que: Se você vai reconstruir a pkg/trie/smt.go, este é o momento perfeito para mudar para Verkle Trees.
   * Vantagem na Jamii: Como você já tem o Bonsai Turbo (dados planos em d:), a árvore (seja SMT ou Verkle) serve apenas para gerar a Root e as Provas.
   * Facilidade: Como não há migração de dados, você pode manter a interface SparseMerkleTree mas mudar o algoritmo interno para usar compromissos vetoriais
     (Vector Commitments). Isso daria à Jamii uma prova de estado muito menor e abriria caminho para o Statelessness desde o dia zero.
   * Risco: Exige integração de bibliotecas de curva elíptica para compromissos IPA ou KZG (que o Ethereum está adotando).

  2. EOF (A "obrigatória" para conformidade)
   * Por que: O EOF não mexe no armazenamento (pkg/store), mas sim na execução (pkg/vm). É a mudança que garante que a Jamii seja compatível com a próxima
     geração de contratos Solidity.
   * Facilidade: É independente da SMT. Você pode fazer a qualquer momento sem quebrar o consenso do estado histórico. É basicamente uma refatoração do
     analysis.go.

  3. EIP-7702 (A mais "cosmética")
   * Por que: Como os endereços já estão unificados, o EIP-7702 torna-se uma trivialidade técnica. É apenas adicionar um campo na transação e, durante a
     execução do ApplyTransaction, carregar um bytecode temporário para aquela conta.
   * Conclusão: É a mais fácil, mas talvez a menos impactante arquiteturalmente comparada à chance de reconstruir a SMT como Verkle.
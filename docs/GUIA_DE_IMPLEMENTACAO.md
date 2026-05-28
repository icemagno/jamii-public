# Guia de Implementação: Módulo Blockchain (pkg/blockchain)

Este documento descreve o plano detalhado para a implementação da camada de cadeia de blocos da Jamii, baseando-se nos padrões industriais do `go-ethereum` (Geth), mas preservando a soberania criptográfica e a agilidade de hash do projeto.

## 1. Estrutura de Dados: O Bloco e o Cabeçalho
Diferente do Geth, o Jamii deve suportar hashes de tamanhos variáveis. Portanto, campos como `ParentHash` e `StateRoot` devem usar o tipo `types.Hash`.

### Cabeçalho do Bloco (Header)
**Origem Geth:** `core/types/block.go` (struct `Header`)

```go
type Header struct {
    ParentHash  types.Hash // Hash do bloco anterior
    Number      *big.Int   // Altura do bloco na cadeia
    StateRoot   types.Hash // Raiz da SMT após execução do bloco
    TxRoot      types.Hash // Raiz Merkle das transações (opcional, pode ser Hash do lote)
    ReceiptRoot types.Hash // Raiz Merkle dos recibos
    Coinbase    types.Hash // Endereço do validador que selou o bloco
    GasLimit    uint64     // Limite de gás do bloco
    GasUsed     uint64     // Gás total consumido
    Timestamp   uint64     // Hora da criação do bloco
    Extra       []byte     // Dados arbitrários (max 32 bytes)
}
```

### O Bloco (Block)
**Origem Geth:** `core/types/block.go` (struct `Block`)

```go
type Block struct {
    header       *Header
    transactions []*encoding.Transaction
    // Cache de hash para evitar re-calculo
    hash atomic.Value 
}
```

## 2. Persistência da Cadeia (BlockChain)
O `BlockChain` é o orquestrador que utiliza o `pkg/store` para salvar blocos e o `pkg/core` para gerenciar o estado.

**Origem Geth:** `core/blockchain.go`

### Atribuições:
1. **Manter a Cadeia Canônica:** Identificar qual é o "Head" da cadeia.
2. **Inserção de Blocos:** `WriteBlock(block *Block)` deve salvar o bloco no PebbleDB usando prefixos:
    * `h + num + hash` -> Header
    * `b + num + hash` -> Body (Transactions)
    * `n + num` -> Hash do bloco na altura N (Cadeia Canônica)

## 3. Motor de Armazenamento: Estratégia Bonsai-First
Para atingir performance de nível industrial, o Jamii não deve depender exclusivamente da SMT para leitura. Implementaremos o modelo de **Flat Storage** (inspirado no Besu Bonsai Tries).

### Tabela Plana de Contas (`accounts_flat`)
Diferente do modelo tradicional onde cada leitura exige percorrer a árvore, a VM lerá diretamente de uma tabela plana no PebbleDB.

**Prefixos de Armazenamento:**
* `f + address` -> Dados da Conta (Flat) - **Fonte da Verdade para a VM**
* `n + level + hash` -> Nós da SMT (Para provas e raízes)
* `h + num + hash` -> Header do Bloco
* `b + num + hash` -> Body do Bloco

## 4. Validação de Bloco (Validator & Processor)
**Origem Geth/Besu:** `core/block_validator.go` e `BonsaiWorldState.java`

A função `ProcessBlock(block *Block)` deve seguir este fluxo otimizado:
1. **Leitura Rápida:** Carregar contas da tabela plana (`f:`) em vez da SMT.
2. **Execução VM:** Processar transações e gerar novos saldos/nonces.
3. **Double-Write Atômico (Commit):**
    * Escrever novos dados na tabela plana (`f:`).
    * Atualizar a SMT em background (ou ao final do lote) para gerar a nova `StateRoot`.
4. **Verificação de Raiz:** Comparar a `StateRoot` gerada pela SMT com a informada no `Header`.

## 5. Recibos de Transação (Receipts)
**Origem Geth:** `core/types/receipt.go`

Para cada transação executada, um recibo deve ser gerado e armazenado.
```go
type Receipt struct {
    Status      uint8      // 1 = Sucesso, 0 = Falha
    CumulativeGas uint64   // Gás usado até esta transação no bloco
    Logs        []*Log     // Eventos gerados pela VM
    TxHash      types.Hash // ID da transação
}
```

## 6. Roteiro de Desenvolvimento (Sprint 5)

### Passo 1: Definição de Tipos (`pkg/blockchain/types.go`)
* Implementar `Header` e `Block` com suporte a **Hash Agility**.
* Implementar o método `Hash()` para o Header (Keccak-256 do SSZ).

### Passo 2: Implementação do Flat Storage (`pkg/core/state_flat.go`)
* Modificar o `StateDB` para ler prioritariamente do prefixo `f:`.
* Implementar a sincronização: toda alteração em `f:` deve agendar uma atualização na SMT.

### Passo 3: Gerenciador de Cadeia (`pkg/blockchain/chain.go`)
* Implementar a struct `BlockChain`.
* Métodos: `GetBlockByNumber`, `GetBlockByHash`, `CurrentBlock`.
* Integração com `store.Batch` para escritas atômicas (Bloco + Tabela Plana + SMT).

### Passo 4: O "Gênesis"
* Função `WriteGenesisBlock()`: Inicializa a tabela plana e a árvore Merkle com os saldos iniciais.

## 7. O Diferencial Jamii: Hash Agility no Header
Os offsets no SSZ do Header devem ser dinâmicos para aceitar hashes de 32 ou 64 bytes sem quebrar o parser.

---
**Diretriz de Performance:** A leitura de saldo deve ser O(1). Nunca permita que a VM percorra a SMT durante a execução de transações normais. A árvore Merkle serve para **Provas**, a tabela plana serve para **Estado**.

---
**Dica para o Programador:** Use o padrão de "Double-Check Locking" para o cache de hash do bloco, conforme visto no módulo de `encoding`. Não esqueça de fechar os iteradores do PebbleDB ao buscar blocos por intervalo.

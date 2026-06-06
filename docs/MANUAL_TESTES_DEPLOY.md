# 📘 Guia de Engenharia: Testes de Integração e Deploy na JamiiVM

Este manual serve como diretriz fundamental para a criação de novos testes de integração envolvendo contratos Solidity e deploys reais na JamiiVM. **Consulte este arquivo sempre que um novo teste de contrato for solicitado.**

## 1. Princípios Fundamentais (Diretivas)
*   **Atomicidade:** Cada etapa (Deploy vs Execução) deve ser tratada como uma transação distinta.
*   **Persistência Real:** Sempre utilize o `PebbleStore` em testes de integração para garantir que o estado (bytecodes e storage) sobreviva entre o deploy e a chamada.
*   **Verificação Tripla:** A lógica de validação deve sempre comparar os resultados contra o Geth, Besu ou Yellow Paper.

## 2. Dificuldades Encontradas e Soluções
| Problema | Causa | Solução |
| :--- | :--- | :--- |
| **Deploy retornando 0 bytes** | Address/Caller iguais no MessageFrame. | Separar `caller` (EOA) de `contract` (Endereço Destino) no `NewMessageFrame`. |
| **Storage vazio após deploy** | Falta de consolidação do estado. | Chamar obrigatoriamente `st.Commit()` após o deploy e antes da próxima execução. |
| **Erro "File not found" no solc** | Divergência de diretório de trabalho (.bat vs .go). | Usar caminhos relativos simples no `.go` e garantir que o `.bat` use `pushd` para a pasta dos fontes. |
| **Retorno do contrato não capturado** | ReturnData não propagado globalmente. | Garantir que o `EVM.Run` propague o `frame.ReturnData` para o `evm.ReturnData` (Sincronização Homologada). |

## 3. Padrão de Codificação de Chamadas (Seletores e ABI)
Nunca gere hashes de seletores manualmente ou via strings fixas sem validação.
1.  **Geração do Seletor:** Use sempre o comando do compilador:
    `go run cmd/compiler/main.go -hash "funcao(tipo1,tipo2)" -c dummy.code`
2.  **Encoding Manual:** Siga o padrão industrial (BigEndian):
    *   `Address`: 32 bytes (20 bytes de payload com padding à esquerda de 12 zeros).
    *   `Uint256/Int256`: 32 bytes fixos.
    *   `Strings/Bytes`: Requerem Offset, Length e Content (alinhados em 32 bytes).

## 4. Estrutura Padrão do Script de Teste (.go)
Siga o modelo industrial estabelecido no `run_factory_integration.go`:
```go
// 1. Inicializar PebbleStore e StateDB
db, _ := store.NewPebbleStore(dbPath)
st := state.NewStateDB(trie.NewSparseMerkleTree(db, 32, 256))

// 2. Fase de Deploy
bytecode := compileSolidity("contrato.sol")
st.SetCode(contractAddr, bytecode)
st.Commit() // CRÍTICO

// 3. Fase de Execução
inputData := append(selector, arguments...)
engine := vm.NewEVM(st, gc)
frame := vm.NewMessageFrame(st, caller, contractAddr, value, st.GetCode(contractAddr), inputData, gas, false)
engine.Run(frame)
st.Commit()

// 4. Verificação Assertiva
val := st.GetState(contractAddr, slot)
if val != expected { log.Fatalf(...) }
```

## 5. Exemplos de Referência (Ouro)
Utilize estes arquivos como gabarito para novos desenvolvimentos:
*   **Aritmética e Loops:** `cmd/compiler/run_fibonacci_tests.go`
*   **Manipulação de Bits:** `cmd/compiler/run_bitwise_tests.go`
*   **Interoperabilidade (CALL):** `cmd/compiler/run_call_integration.go`
*   **Criação Dinâmica (CREATE):** `cmd/compiler/run_factory_integration.go`
*   **Storage Layout (SSTORE/SLOAD):** `cmd/compiler/run_data_types_audit.go`

---
*Manual gerado e homologado em 20 de Abril de 2026.*
*Atualizado em 05 de Junho de 2026 com instruções de Setup de Identidade.*

## 6. Setup de Identidade do Nó (Nova Versão)
A partir da Sprint 8.2, a Jamii introduziu um comando utilitário para facilitar a criação de identidades sem a necessidade de iniciar o nó completo.

### Gerando Identidade Híbrida (PQC)
Para gerar uma nova chave privada e obter seu endereço soberano:
```bash
jamii setup generate-key --datadir ./data
```
**Artefatos Gerados:**
*   `nodekey`: A chave privada binária (proteja este arquivo!).
*   `node_address.json`: Manifesto público contendo o `sovereign_address`, `mirror_address` (Ethereum compatible) e a `public_key`.

Este comando deve ser o primeiro passo para qualquer novo validador que deseje coletar seu endereço para inclusão em um arquivo `genesis.json`.

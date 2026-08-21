# 🏗️ Arquitetura Interna da Jamii Blockchain

Este documento tem como objetivo registrar a arquitetura interna da Jamii Blockchain, detalhando seus módulos, abstrações, padrões de projeto e implementações de baixo nível.

---

## 📑 Sumário de Módulos

1. [Módulo de Consenso (`pkg/consensus`)](#1-módulo-de-consenso-pkgconsensus)

---

## 1. Módulo de Consenso (`pkg/consensus`)

O módulo de consenso da Jamii Blockchain é responsável por garantir a concordância entre os nós validadores sobre a ordem e a validade dos blocos adicionados à cadeia. Ele segue o padrão de projeto **Strategy**, estabelecendo uma separação clara entre a interface abstrata do motor de consenso e sua implementação concreta.

---

### 1.1. Interface Genérica (`pkg/consensus/interface.go`)

O arquivo [`interface.go`](file:///C:/Magno/Projetos/jamii/pkg/consensus/interface.go) estabelece o contrato unificado para qualquer motor de consenso suportado pela Jamii através da interface `Engine`:

```go
type Engine interface {
	// Start inicia o processo de consenso e workers associados.
	Start() error

	// Stop encerra o processo de consenso de forma graciosa.
	Stop() error

	// VerifyHeader valida se o cabeçalho do bloco satisfaz todas as regras do protocolo de consenso.
	VerifyHeader(header *coreTypes.Header) error

	// Author extrai e retorna o endereço do validador/minerador que gerou o bloco.
	Author(header *coreTypes.Header) (types.Address, error)

	// Seal aplica a prova de consenso final ao bloco (ex: selos IBFT ou Nonce PoW).
	Seal(block *coreTypes.Block) error
}
```

#### Princípios Arquiteturais da Interface:
* **Desacoplamento de Núcleo:** O núcleo da blockchain (`pkg/core`, processador de transações e motor de execução da VM) interage unicamente com a interface `Engine`.
* **Agnosticismo de Algoritmo:** Permite substituir ou estender o algoritmo de consenso (ex: IBFT 2.0, PoW, PoS) sem necessidade de alterar o código de execução do nó ou da máquina de estado.

---

### 1.2. Implementação Concreta: IBFT 2.0 (`pkg/consensus/ibft`)

A pasta [`pkg/consensus/ibft`](file:///C:/Magno/Projetos/jamii/pkg/consensus/ibft) fornece a implementação oficial baseada no protocolo **Istanbul Byzantine Fault Tolerant (IBFT 2.0)**, portado e adaptado das referências de engenharia do *Hyperledger Besu* e *Go-Ethereum (Geth)*.

#### Características Principais do IBFT 2.0 na Jamii:
* **Finalidade Imediata (Instant Finality):** Blocos selados são irreversíveis. Não há reorganização de cadeia (*reorgs*) ou bifurcações (*forks*).
* **Tolerância a Falhas Bizantinas:** Suporta até $f$ nós maliciosos ou inoperantes em uma rede com $N \ge 3f + 1$ validadores, exigindo um quórum de supermaioria de $2f + 1$ votos válidos.

---

### 1.3. Componentes Internos do IBFT 2.0

| Componente | Arquivo | Responsabilidade |
| :--- | :--- | :--- |
| **Orquestrador Central** | [`controller.go`](file:///C:/Magno/Projetos/jamii/pkg/consensus/ibft/controller.go) | Implementa `consensus.Engine`. Gerencia a inicialização, paradas, validação de cabeçalhos (`VerifyHeader`), selagem de blocos (`Seal`), recepção de mensagens P2P e timeouts de rodada. |
| **Máquina de Estados de Rodada** | [`round.go`](file:///C:/Magno/Projetos/jamii/pkg/consensus/ibft/round.go) / [`round_state.go`](file:///C:/Magno/Projetos/jamii/pkg/consensus/ibft/round_state.go) | Controla o fluxo de fases de uma rodada ($R$) em uma altura ($H$): `PROPOSAL` $\to$ `PREPARE` $\to$ `COMMIT` $\to$ `ROUND_CHANGE`. Acumula votos de validadores e valida quórum de $2f+1$. |
| **Seleção de Líder** | [`proposer.go`](file:///C:/Magno/Projetos/jamii/pkg/consensus/ibft/proposer.go) | Define a lógica determinística de rotação de propositores (*Round-Robin*) para determinar qual validador é o responsável por propor o bloco na rodada atual. |
| **Gerenciador de Alturas** | [`height_manager.go`](file:///C:/Magno/Projetos/jamii/pkg/consensus/ibft/height_manager.go) | Gerencia a progressão e transição entre alturas consecutivas de bloco ($H \to H + 1$) após a finalização de um bloco. |
| **Mensagens e Payloads** | [`types.go`](file:///C:/Magno/Projetos/jamii/pkg/consensus/ibft/types.go) / [`payloads.go`](file:///C:/Magno/Projetos/jamii/pkg/consensus/ibft/payloads.go) | Definem as estruturas de dados, identificadores de rodada (`RoundIdentifier`), enums de tipos de mensagem e rotinas de serialização/assinatura para o transporte P2P via DTS. |

---

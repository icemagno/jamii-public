# ⚡ Async Write Worker — Plano Mestre de Implantação

## 📌 Visão Geral e Arquitetura

O **Async Write Worker** (Worker de Gravação Assíncrono) tem como objetivo desacoplar a gravação síncrona de blocos no disco (PebbleDB) do ciclo crítico de votação e avanço do consenso IBFT 2.0.

Inspirado na arquitetura `BonsaiWorldStatePersister` do **Hyperledger Besu** e nas *Snapshot Diff Layers* do **Go-Ethereum (Geth)**, o Async Write Worker aproveita que a Jamii já executa a verificação em RAM via **Bonsai Turbo** e **Witness-Aided Acceleration** para responder instantaneamente ao consenso, enquanto a persistência física no disco ocorre em segundo plano.

---

## 🎯 Objetivos de Desempenho

1. **Redução do Tempo entre Blocos:** Eliminar o solavanco de latência de commit no disco ($\sim 500\text{ms} - 763\text{ms}$ sob alta carga), permitindo transições de altura em milissegundos ($< 5\text{ms}$).
2. **Estabilidade de Pacing:** Garantir tempo de slot IBFT 2.0 cravado nos $3{,}0\text{s}$ nominais sem flutuações.
3. **Preservação de Integridade:** Manter 100% de consistência de estado, suporte a consultas RPC em RAM, servimento P2P de blocos em fila e recuperação graciosa em casos de queda de energia.

---

## 📋 Sprints de Implantação

---

### 🏃 Sprint 1: Motor Core do Async Write Worker e Fila de RAM

**Objetivo:** Criar o componente isolado `AsyncWriteWorker` responsável por gerenciar a fila de tarefas de gravação em memória, worker goroutine e mecanismo de *backpressure*.

#### 📄 Tarefas

- [ ] **Tarefa 1.1: Estruturas de Dados e Worker Routine (`pkg/node/async_write_worker.go`)**
  - Criar a struct `WriteTask` contendo `Block`, `Receipts`, `StateRoot`, `Sequence` e `Timestamp`.
  - Criar o componente `AsyncWriteWorker` com canal Go buferizado (`chan *WriteTask`), trava Mutex, sinalizadores de estado e métricas.
  - Implementar o loop de fundo `workerLoop()` que consome tarefas da fila e executa `blockchain.AddBlockWithReceipts(task.Block, task.Receipts)` de forma sequencial.

- [ ] **Tarefa 1.2: Mecanismo de Backpressure e Limites de RAM**
  - Implementar a configuração `MaxQueueSize` (padrão: 32 blocos).
  - Implementar o método `Enqueue(task)` com controle de congestionamento: se a fila atingir o limite `MaxQueueSize`, o envio aplica uma pausa suave (*backpressure*) até que o worker libere espaço, prevenindo estouros de memória (OOM).

- [ ] **Tarefa 1.3: Encerramento Gracioso (*Graceful Shutdown*)**
  - Implementar o método `Stop()` / `Close()` que aguarda o esvaziamento completo da fila de RAM antes de finalizar o processo do nó, garantindo zero perda de dados em shutdowns normais.

- [ ] **Tarefa 1.4: Suíte de Testes Unitários TDD (`pkg/node/async_write_worker_test.go`)**
  - Teste 1: Validação de sequenciamento estrito de gravação em lote.
  - Teste 2: Validação de ativação de Backpressure quando a fila atinge a capacidade máxima.
  - Teste 3: Validação de shutdown gracioso garantindo persistência de 100% dos blocos pendentes na RAM.

---

### 🏃 Sprint 2: Integração no Nó e Pipelining do Consenso

**Objetivo:** Integrar o `AsyncWriteWorker` no ciclo de vida do `Node`, refatorar o `OnBlockFinalized` e ajustar o servimento P2P/RPC para ler da RAM.

#### 📄 Tarefas

- [ ] **Tarefa 2.1: Desacoplamento do Consenso em `pkg/node/node.go`**
  - Instanciar o `AsyncWriteWorker` na inicialização do `Node`.
  - Refatorar o callback `n.consensus.OnBlockFinalized` em [`pkg/node/node.go`](file:///c:/Magno/Projetos/jamii/pkg/node/node.go):
    - Executar o bloco no estado em RAM via `ExecutePayload(n.state, block)`.
    - Enfileirar a tarefa de disco no `AsyncWriteWorker.Enqueue()`.
    - Atualizar o mercado da MemPool (`mempool.Reset`).
    - Disparar **imediatamente** o consenso da próxima altura (`n.startConsensusAt(H+1)`) sem aguardar a conclusão do disco.

- [ ] **Tarefa 2.2: Servimento de Blocos da RAM para Rede P2P e RPC**
  - Implementar a consulta em duas camadas no `AsyncWriteWorker`: (1º Fila de RAM -> 2º PebbleDB no Disco).
  - Atualizar os manipuladores de requisição de blocos P2P (`handleBlockRequest`) e métodos RPC (`eth_getBlockByNumber`) para buscar blocos pendentes no buffer de RAM do `AsyncWriteWorker` caso ainda não estejam salvos no PebbleDB.

- [ ] **Tarefa 2.3: Telemetria e Métricas Prometheus (`pkg/rpc/server.go`)**
  - Adicionar a métrica `jamii_async_worker_queue_length` no handler `/metrics` para monitorar a quantidade de blocos pendentes na RAM em tempo real no Grafana.

---

### 🏃 Sprint 3: Blindagem contra Crashes, Guardas de Sync e Validação Integrada

**Objetivo:** Garantir resiliência contra desligamento abrupto (queda de energia), proteger o sincronismo inicial e validar sob estresse de carga.

#### 📄 Tarefas

- [ ] **Tarefa 3.1: Recuperação contra Desligamento Abrupto (*Crash Recovery*)**
  - No boot do nó em `node.go`, comparar a altura do cabeçalho canônico em disco (`PebbleDB`) com a altura da MemPool/Trie.
  - Caso haja uma lacuna gerada por crash antes do flush do disco, o nó ativa o `SyncManager` para re-obter os blocos faltantes via rede P2P antes de liberar a validação do consenso.

- [ ] **Tarefa 3.2: Guarda de Modo Sync (*Catch-Up Protection*)**
  - Adicionar a trava `if syncManager.IsBehind()`: Durante a fase de sincronização histórica (*Catch-Up Sync*), o `AsyncWriteWorker` opera em **Modo Pass-Through Síncrono** para garantir que a gravação em disco acompanhe o download em lote de blocos antigos.

- [ ] **Tarefa 3.3: Teste de Carga e Validação de Latência**
  - Executar o gerador de tráfego a 1.000 TXs/bloco.
  - Confirmar no Grafana que a latência percebida de transição de altura caiu para $< 5\text{ms}$ e que a métrica de tempo de bloco se mantém estável sem travamentos do consenso.

---

## 🛡️ Mandatos de Segurança e Conformidade

1. **Proteção de Módulos Homologados:** Nenhuma interface homologada de `pkg/store`, `pkg/trie`, `pkg/types` ou `pkg/crypto` será alterada. O `AsyncWriteWorker` residirá no pacote `pkg/node`.
2. **Preservação de Testes:** Todos os testes unitários existentes do consenso, sincronismo e mempool devem continuar passando sem modificação.
3. **Padrão Besu/Geth:** A lógica de fallback em RAM para requisições P2P segue rigorosamente o padrão `BonsaiWorldStatePersister` do Hyperledger Besu.

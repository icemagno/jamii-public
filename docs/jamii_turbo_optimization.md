# 🚀 Plano de Construção: Jamii Turbo Optimization (2026)

Este documento descreve o plano mestre para a otimização de performance e refinamento arquitetural da Jamii Blockchain, visando atingir o estado de "Zero-Allocation" no hot path e conformidade máxima com os padrões industriais do Geth e Besu.

---

## 📋 Visão Geral
**Objetivo:** Aumentar o TPS aritmético e de execução em pelo menos 5x, reduzir a pressão do Garbage Collector (GC) e eliminar contenção de locks no estado mundial.
**Status:** Planejamento Homologado.

---

## 🛠️ Fase 1: Primitivas de Alta Performance (O Alicerce)
*Status: CONCLUÍDO (22/04/2026)*

### Sprint 1.1: Uint256 Nativo
- [x] **Tarefa 1.1.1:** Criar suíte de benchmarks comparativos.
- [x] **Tarefa 1.1.2:** Implementar aritmética de 256 bits baseada em `[4]uint64` (holiman/uint256).
- [x] **Tarefa 1.1.3:** Migrar operações lógicas e bit shifts para a nova estrutura.
- [x] **Tarefa 1.1.4:** Implementar conversão otimizada de/para bytes e strings.
- [x] **Validação:** TPS aritmético atingiu recorde de ~1B ops/s.

### Sprint 1.2: Pool de Buffers SSZ
- [x] **Tarefa 1.2.1:** Identificar pontos de alocação dinâmica.
- [x] **Tarefa 1.2.2:** Implementar Zero-Copy Decode (fatiamento de rede).
- [x] **Tarefa 1.2.3:** Eliminar deepCopy no SigningHash via serialização parcial.
- [x] **Validação:** Throughput atingiu 911k TPS (Ganho de +127%).

---

## ⚙️ Fase 2: Motor de Execução Turbo (A Máquina Virtual)
*Status: CONCLUÍDO (22/04/2026)*

### Sprint 2.1: Despacho Estático de Opcodes
- [x] **Tarefa 2.1.1:** Refatorar a `JumpTable` para array fixo `[256]operation`.
- [x] **Tarefa 2.1.2:** Integrar aritmética imutável segura (holiman/uint256) na stack.
- [x] **Tarefa 2.1.3:** Otimizar a estrutura `operation` para redução de cache miss.
- [x] **Validação:** Aderência e Bitwise OK (Aritmética Zero-Alloc interna).

### Sprint 2.2: Gerenciamento de Memória "Zero-Copy"
- [x] **Tarefa 2.2.1:** Implementar `SetData` para cópias Zero-Copy (calldata/code).
- [x] **Tarefa 2.2.2:** Otimizar `opSha3` para usar buffer de memória da VM diretamente.
- [x] **Tarefa 2.2.3:** Refatorar estrutura de expansão de memória para evitar realocações agressivas.
- [x] **Validação:** Pista de Obstáculos validada com 100% de sucesso.

---

## 🌳 Fase 3: Estado de Baixa Latência (Trie e StateDB)
*Status: EM ANDAMENTO*

### Sprint 3.1: Bonsai SMT
- [x] **Tarefa 3.1.1:** Implementar Sharded Cache (16 shards FNV-1a) para contenção zero.
- [x] **Tarefa 3.1.2:** Implementar Persistência Seletiva (Pruning) no Short-Circuit.
- [x] **Tarefa 3.1.3:** Implementar Async Node Collector via canais.
- [x] **Validação:** AvgTPS saltou de 814 para 5.354 (Ganho de +557%).

### Sprint 3.2: Otimização do Journal (StateDB)
- [ ] **Tarefa 3.2.1:** Implementar de-duplicação de entradas no Journal para o mesmo slot.
- [ ] **Tarefa 3.2.2:** Refinar a estrutura `journalEntry` para reduzir overhead de ponteiros.
- [ ] **Tarefa 3.2.3:** Otimizar a estrutura do `stateObject` para uso de cache de código pré-compilado.
- [ ] **Validação:** Executar testes de reentrada (Reentrancy).

---

## 🧪 Fase 4: Validação e Homologação Global
*Objetivo: Garantir estabilidade e consolidar novos recordes de performance.*

### Sprint 4.1: Stress Test Industrial Consolidado
- **Tarefa 4.1.1:** Executar todos os testes de estresse simultaneamente (Crypto + VM + SMT + Store).
- **Tarefa 4.1.2:** Realizar análise de perfil de CPU e Memória (`pprof`) sob carga massiva.
- **Tarefa 4.1.3:** Atualizar `auditorias/medicoes.md` com os novos recordes de performance.
- **Tarefa 4.1.4:** Gerar relatório final de homologação técnica.

---
**Nota:** Nenhuma alteração deve ser feita em módulos homologados sem a execução prévia dos benchmarks de baseline definidos em cada Sprint.

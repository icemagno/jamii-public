# 🧠 Memória de Decisão: Mempool Determinística & Sincronismo de Dados

Este documento registra as deliberações técnicas e a arquitetura final decidida para a Mempool da Jamii Blockchain, focando na resiliência contra flood PQC e na eficiência da reconstrução de blocos (Skeleton Blocks).

---

## 1. O Problema: Assimetria de Cache e Skeleton Blocks
A Jamii utiliza **Skeleton Blocks** (Compact Blocks) para propagação rápida. O Proposer envia apenas os hashes das transações, esperando que os validadores as tenham em seus caches (Mempool).
*   **Risco Identificado:** Se os nós tivessem tamanhos de Mempool diferentes (ex: um com 2k slots e outro com 50k), o nó maior poderia propor transações que o nó menor já ejetou.
*   **Consequência:** Falha na reconstrução local, forçando downloads massivos de dados PQC brutos (assinaturas ML-DSA de ~3KB), o que causaria picos de rede e paralisia do consenso (Round Changes).

## 2. Decisão: Mandato de Slots no Genesis
Diferente de redes como Geth/Besu, onde a Mempool é tratada como recurso puramente local, na Jamii ela passa a ser um **Contrato de Disponibilidade de Dados**.
*   **MaxMempoolSlotSize (Genesis):** Definido no arquivo de configuração da rede. Todos os validadores devem se comprometer a manter exatamente a mesma quantidade de transações de elite.
*   **Objetivo:** Garantir que o Proposer e os Validadores tenham "espelhos" quase idênticos de transações, tornando a reconstrução do Skeleton Block infalível e instantânea.

## 3. O "Tie-Breaker" Universal: Timestamp de Despacho
Para que o espelhamento seja perfeito, todos os nós devem ejetar **exatamente a mesma transação** quando o limite for atingido.
*   **Critério Primário:** Rentabilidade (Gas Price/Lucro).
*   **Critério de Desempate:** **Timestamp de Despacho**.
*   **Por que Despacho?** Diferente do timestamp de chegada (que é relativo a cada nó), o timestamp de despacho é gravado e assinado pelo remetente. É um fato universal e imutável.
*   **Lógica de Ejeção:** Entre duas transações com o mesmo preço, a que foi despachada por último é ejetada primeiro (Punição aos retardatários).

## 4. Gestão Híbrida de Recursos (Slots vs. Memória)
As transações PQC são "gordas" (4.5KB a 5KB em média).
*   **Slots (Consenso):** O limite por quantidade (slots) garante a convergência da rede para os Skeleton Blocks.
*   **Memória (Local):** O limite por Bytes/MB (ex: `MaxMempoolMemorySize`) atua como um **disjuntor de emergência local**. Se uma transação maliciosa de 1MB for enviada, o nó a ejetará por estouro de memória local antes de comprometer a RAM, mesmo que ainda haja slots disponíveis.

## 5. Performance e Manutenção
A preocupação com o custo de ordenação de 10.000 itens foi resolvida através de:
*   **Heaps (Max/Min):** Manutenção em $O(\log N)$. Apenas ~14 comparações por inserção/remoção.
*   **sync.RWMutex:** Permite leitura concorrente massiva, travando o acesso por apenas nanossegundos durante a reordenação do Heap.
*   **PQC Overhead:** O custo das verificações de limite (O(1)) é desprezível comparado ao custo da verificação de assinatura ML-DSA (milissegundos).

## 6. Segurança e Leniência de Sincronismo (Refinamento de Boot)
Durante o desenvolvimento do "Rogue Node" (Maio/2026), identificamos a necessidade de equilibrar a segurança absoluta com a clareza operacional durante a fase de sincronização.

*   **And-Gate Barrier (Assinatura Obrigatória):** Foi removido qualquer "atalho" ou leniência na verificação de assinaturas. A MemPool agora exige `tx.Verify()` rigoroso em 100% dos casos, protegendo o nó contra spam de assinaturas falsas mesmo durante o boot.
*   **Leniência de Log (Sync-Aware):** Para evitar "ruído vermelho" (erros alarmistas) no terminal, o nó foi ajustado para diferenciar rejeições:
    *   **Durante o Sync:** Se o nó estiver atrasado (`IsStrictlyBehind`), rejeições por `BaseFee` ou `Saldo` (causadas por estado defasado) são registradas apenas em `DEBUG`.
    *   **Em Operação:** Se o nó estiver sincronizado, as mesmas rejeições são registradas como `ERROR`, indicando problemas reais de execução.

---

## 🚀 Resumo da Arquitetura Sovereign V1 (Mempool)

| Atributo | Localização | Função |
| :--- | :--- | :--- |
| **MaxMempoolSlotSize** | Genesis | SLA de Sincronia de Dados entre nós. |
| **MaxMempoolMemorySize** | CLI / Local | Proteção contra OOM (Out of Memory). |
| **Dispatch Timestamp** | Tx Struct | Desempate universal para ejeção determinística. |
| **Eviction Policy** | `pkg/mempool` | Price-first, Timestamp-tie-breaker. |
| **Storage** | RAM Only | Disco é proibido para evitar latência no Consensus. |

---
**Data da Decisão:** 15 de Maio de 2026  
**Status:** Implementação Iniciada (Sprint 5.1)

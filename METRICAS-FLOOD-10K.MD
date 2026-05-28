/*
 * JAMII BLOCKCHAIN - PERFORMANCE BENCHMARKS & MILESTONES
 * Last Updated: 2026-05-28
 */

## 🏆 Benchmark Final Sprint 5.0 (Otimização Homomórfica)
A Jamii Blockchain eliminou seu último grande gargalo de CPU ao implementar a **Verkle Tree Homomórfica**. O motor agora processa árvores de 256 vias com a mesma velocidade de hashes Merkle simples, mantendo prontidão para clientes Stateless e PQC.

### 📊 Tabela de Evolução de Performance (Flood 10K)

| Métrica | Sprint 4.1 | Sprint 4.2 | **Sprint 5.0 (Homomorphic)** | Ganho Final |
| :--- | :--- | :--- | :--- | :--- |
| **Pico do Motor (EVM+Verkle)** | ~180 TX/s | ~300 TX/s | **~750 TX/s** | **+316% de ganho bruto.** |
| **Tempo de Finalização (1.5k TXs)** | ~10s | ~5s | **~2 segundos** | **-80% tempo de CPU.** |
| **Throughput Efetivo (TPS)** | ~141 TX/s | ~272 TX/s | **~250-270 TX/s*** | **Estabilidade Industrial.** |
| **Determinismo de Hash** | Sim (Parcial) | SIM (100%) | **SIM (100%)** | **Consistência Global.** |
| **Higiene de Logs** | Melhorado | LIMPO | **ELITE** | **Silêncio Operacional.** |

*\*Nota: O TPS efetivo é limitado pelo BlockPeriod de 2s; a capacidade latente do motor agora suportaria 700+ TPS se o período fosse reduzido para 1s.*

---

### 🛠️ Memória Técnica das Blindagens (Sprint 5.0)

1. **Cálculo Incremental Homomórfico (Verkle Turbo)**
   - **Lógica:** Implementação do motor `BatchUpdate` ($C' = C + \sum \Delta \times G_i$).
   - **Impacto:** Fim da re-computação $O(256)$ de compromissos IPA. A finalização de blocos gigantes caiu de 5s para 2s.

2. **Resiliência de Ressurreição (Halt Guard Fix)**
   - **Lógica:** Reset automático da trava `lastStartedHeight` em caso de perda de quórum.
   - **Impacto:** Nós que caem e retornam agora reativam o consenso live instantaneamente assim que o quórum (5/5) é restabelecido.


---

### 🛠️ Memória Técnica das Blindagens (Sprint 4.2)

1. **Determinismo de Identidade (Hash Blindado)**
   - **Lógica:** `Header.Hash()` agora ignora o campo `ExtraData`.
   - **Impacto:** O ID do bloco é derivado apenas do conteúdo de execução. Todos os nós concordam no mesmo Hash, independente de quais selos PQC coletaram.

2. **Temporalidade Pétrea (Timestamp Formulaico)**
   - **Lógica:** Proposer utiliza `Parent.Timestamp + BlockPeriod`.
   - **Impacto:** Eliminação total de divergências de milissegundos entre nós. Essencial para que assinaturas PQC sejam verificáveis em toda a malha.

3. **Trava de Altura (lastStartedHeight)**
   - **Lógica:** Mutex no `startConsensusAt` impedindo disparos duplicados para a mesma altura.
   - **Impacto:** Fim das race conditions entre Consenso e SyncManager. Redução do desperdício de CPU e rede.

4. **Cadência de Produção (Anti-Storm Pacing)**
   - **Lógica:** Sleep obrigatório (Catch-up mode) se a blockchain cair atrás do tempo real.
   - **Impacto:** A rede mantém o ritmo físico mesmo após períodos de inatividade ou sincronia pesada.

5. **Validação Quórum-First**
   - **Lógica:** O sistema só reporta erro se o quórum (4/5) falhar.
   - **Impacto:** Silenciamento de selos redundantes ou inválidos por latência de handshake, limpando o console para produção.

---

### 🏁 Parâmetros de Alta Performance Consolidados
Configuração validada para 300 TPS de pico e 270 TPS sustentados:
- **MaxTxs:** 1500 transações/bloco.
- **BlockPeriod:** 2 segundos.
- **RequestTimeout:** 11 segundos.

**Veredito:** O motor Jamii está otimizado para cenários de alta demanda, mantendo a segurança Post-Quantum (ML-DSA) com latência de confirmação abaixo de 6 segundos.

---
*(Histórico de versões anteriores preservado abaixo para auditoria)*

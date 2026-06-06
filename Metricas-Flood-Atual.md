# 📊 Métricas de Performance - Jamii Blockchain
# Data: 28/05/2026 (Sprint 5.0)

## 🚀 Tsunami Test V2 (10.000 TXs - Otimização O(1))
Teste de carga massiva com transações híbridas (Secp256k1 + ML-DSA) em cluster local. Foco na nova Verkle Tree Homomórfica.

### 📈 Resultados de Vazão (Throughput)
- **Total de Transações:** 10.000
- **TPS de Pico (Motor):** **750 TX/s** (Ganho de +215% vs Sprint 4.2)
- **Tempo de Execução Real (Consenso -> Disco):** 37s (Melhoria de 12% no tempo total)
- **TPS Médio de Execução (Finalização):** **270.27 TX/s**
- **Throughput Latente:** Suportaria > 700 TPS se o `BlockPeriod` fosse reduzido para 1s.

### ⏱️ Latência e Cadência
- **Intervalo de Bloco (Carga):** 2.0s (Cravado via formulaic timestamp)
- **Tempo de Montagem (Proposer):** < 200ms (Otimização O(1) eliminou lag de hashing)
- **Commit no Banco (SSD):** < 1s após finalização do consenso.

### 🛡️ Estabilidade e Resiliência
- **Divergências de StateRoot:** 0
- **Uso de RAM (Pico):** 907MB (Estabilidade industrial com MaxWarmTries)
- **GCs durante o Flood:** Redução drástica devido à arquitetura Zero-Alloc.

---
# Data: 22/05/2026 (Arquivo Histórico)


## 🚀 Tsunami Test (10.000 TXs)
Teste de carga massiva com transações híbridas (Secp256k1 + ML-DSA) em cluster local de 5 nós.

### 📈 Resultados de Vazão (Throughput)
- **Total de Transações:** 10.000
- **Tempo de Injeção (Flooder):** 15.35s
- **TPS de Injeção:** 651.24 TX/s
- **Tempo de Execução Real (Consenso -> Disco):** 42s (Bloco #17 ao #27)
- **TPS Médio de Execução (Finalização):** **238.10 TX/s**

### ⏱️ Latência e Cadência
- **Intervalo de Bloco (Carga):** ~4.2s (média de 1000 TXs/bloco)
- **Tempo de Montagem (Proposer):** < 500ms
- **Propagação (Compact Blocks):** Eficiência de 98% na reconstrução local.
- **Commit no Banco (SSD):** ~1-2s após finalização do consenso.

### 🛡️ Estabilidade e Resiliência
- **Divergências de StateRoot:** 0
- **Nonce Mismatches:** 0 (após fix de sincronização atômica)
- **Recuperação de Round:** 3 RoundChanges detectados e processados com sucesso.
- **Uso de CPU (Média):** 18%
- **Uso de RAM (Pico):** 920MB

### 🏁 Conclusão
A Jamii Blockchain demonstrou estabilidade de classe industrial, sustentando > 230 TPS reais com criptografia pós-quântica ativa. A arquitetura de sincronização atômica e validação antecipada de StateRoot eliminou completamente os gargalos de concorrência observados em versões anteriores.

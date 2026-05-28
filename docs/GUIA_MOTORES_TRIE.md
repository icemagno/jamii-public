# Guia de Seleção de Motor de Estado: SMT vs Verkle Tree

Este documento serve como um manual de decisão técnica para operadores de rede e parceiros corporativos da Jamii Blockchain. Ele detalha as vantagens, desvantagens e perfis de performance de cada motor de árvore de estado (Trie).

---

## 📊 Comparativo Técnico (Benchmark Sprint 4.2)
*Baseado em testes de flood com 1.500 transações por bloco em ambiente de rede privada.*

| Característica | **Sparse Merkle Tree (SMT)** | **Verkle Tree (IPA/Bandersnatch)** |
| :--- | :--- | :--- |
| **Complexidade CPU** | Baixa (Keccak-256 Nativo) | Alta (Criptografia de Curvas Elípticas) |
| **Tempo de Execução (1.500 TXs)** | ~2 segundos | ~5 segundos |
| **Throughput (Motor EVM)** | ~750 TX/s | ~300 TX/s |
| **Consumo de Memória** | Médio | Alto (Cache de Compromissos) |
| **Escalabilidade (Stateless)** | Não suportada | **Nativa (Execution Witnesses)** |
| **Ajuste de Timeout** | Permite timeouts agressivos (5s-8s) | Requer timeouts conservadores (10s-15s) |

---

## 🏆 Quando escolher cada motor?

### 1. Sparse Merkle Tree (SMT): O Motor de Alta Performance
Recomendado para redes privadas, consórcios corporativos e ambientes onde a **velocidade de transação** é o requisito primordial.

*   **Vantagens:**
    *   **Latência Mínima:** O processamento de estado é quase instantâneo, permitindo que o `blockPeriod` seja reduzido para 1s ou 2s.
    *   **Hardware Comum:** Não exige CPUs de última geração para lidar com a criptografia pesada de IPA.
    *   **Estabilidade Industrial:** Baseada no padrão clássico de hashes (Merkle), com menos variáveis matemáticas.
*   **Ideal para:** Sistemas de pagamento, registros de alta frequência e auditoria interna onde todos os nós possuem disco (SSD).

### 2. Verkle Tree: O Motor de Soberania Futura
Recomendado para redes que visam **descentralização extrema** e suporte a dispositivos de baixa capacidade (Statelessness).

*   **Vantagens:**
    *   **Provas Compactas:** Permite a geração de "Testemunhas de Execução" (Witnesses) muito pequenas, permitindo que nós validadores rodem sem armazenar o banco de dados completo.
    *   **Futuro da Indústria:** Alinhado com o roadmap do Ethereum (Era Verge), garantindo que a Jamii esteja na fronteira da tecnologia blockchain.
*   **Considerações:** Exige um hardware mais potente para compensar o custo de CPU da curva Bandersnatch e necessita de `requestTimeout` maior para evitar Round Changes indesejados.
*   **Ideal para:** Redes públicas, governança descentralizada e ecossistemas que incluam dispositivos móveis (Android/IoT).

---

## 🛠️ Guia de Configuração (Ajuste Fino)

### Configuração de Alto Giro (SMT)
Para atingir o pico de performance com SMT, recomendamos os seguintes parâmetros no `genesis.json`:
```json
{
  "tree": 0,
  "period": 2,
  "timeout": 8,
  "max_txs": 1500
}
```
*Impacto:* Finalização de blocos em ~4 segundos com alta tolerância a falhas.

### Configuração de Soberania (Verkle)
Para operar com Verkle Tree mantendo a estabilidade da malha:
```json
{
  "tree": 1,
  "period": 4,
  "timeout": 15,
  "max_txs": 1500
}
```
*Impacto:* Finalização de blocos em ~8 segundos com segurança Post-Quantum e preparação Stateless.

---

**Veredito da Engenharia:**
A Jamii Blockchain oferece a **Trie Factory**, permitindo que o operador escolha o motor conforme sua necessidade de negócio. Parathroughput máximo hoje, use **SMT**. Para inovação e suporte a nós sem disco no futuro, use **Verkle**.

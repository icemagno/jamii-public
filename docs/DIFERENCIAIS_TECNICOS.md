# 💎 Jamii Blockchain: Diferenciais Técnicos e Industriais

Este documento cataloga os pilares tecnológicos, inovações arquiteturais e métricas de performance que posicionam a Jamii Blockchain como uma infraestrutura de próxima geração, distinguindo-a das redes DLT atuais.

---

## 🛡️ 1. Soberania Pós-Quântica (PQC) Híbrida
Enquanto a maioria das redes atuais (Ethereum, Solana, etc.) é vulnerável ao **Algoritmo de Shor**, o Jamii introduz a **Imunidade ao Tempo**.

*   **And-Gate Security (Portão-E):** Exigência mandatória de duas provas simultâneas para autorizar transações: `Verify(Secp256k1) AND Verify(ML-DSA-65)`.
*   **Strong Binding:** Fusão matemática entre a mensagem da transação e o par de chaves (Clássica + Quântica), impedindo ataques de "Mix-and-Match".
*   **ML-DSA-65 (Dilithium):** Implementação do padrão NIST de assinaturas baseadas em reticulados (Lattices).

## 🆔 2. Identidade Unificada (Architecture Shadowless)
O Jamii elimina a necessidade de "Contas de Sombra" ou migrações complexas de saldo ao evoluir a segurança.

*   **20-byte Payload Anchor:** Endereços **V0 (Mirror/0x)**, **V1 (Legacy)** e **V2 (Sovereign/jamii1)** compartilham o mesmo payload derivado da Secp256k1.
*   **Unificação de Estado:** Diferentes representações (Bech32 e Hex) apontam para a **mesma folha** na Sparse Merkle Tree (SMT).
*   **Compatibilidade Nativa:** O saldo recebido em um endereço Ethereum (0x) é imediatamente acessível e protegido pela identidade quântica (V2).

## ⚡ 3. Performance Industrial Homologada
Métricas obtidas em testes de estresse reais (Tsunami Test - Maio/2026):

| Componente | Operação | Performance |
| :--- | :--- | :--- |
| **Crypto Core** | Verificações Híbridas (PQC) | **102.904 op/s** |
| **SSZ Encoding** | Serialização Soberana | **393.507 tx/s** |
| **Throughput Rede** | Transações PQC Sustentadas | **250 - 500 TPS** |
| **JamiiVM** | Engine Dispatch | **32.7 Mop/s** |
| **Eficiência CPU** | Carga de 1.000 TXs/bloco | **16% de uso** |

## ⚙️ 4. Estabilidade e Higiene de Consenso
Arquitetura focada em resiliência e determinismo absoluto.

*   **Ghost Root Killer:** Garantia de StateRoot determinístico em blocos vazios, herdando a raiz do pai e evitando divergências por poluição de cache.
*   **MemPool Purge Descendente:** Limpeza inteligente da fila de transações. Se um nonce falha, todos os subsequentes são expulsos, prevenindo congestionamentos.
*   **Atomic Journaling:** Snapshots atômicos por transação garantem que falhas na VM nunca deixem o saldo do StateDB em estado inconsistente.

## 📦 5. Stack Tecnológica de Ponta
*   **PebbleDB (100% Go):** Camada de persistência de alto desempenho para o estado mundial.
*   **Sparse Merkle Tree (SMT) / Verkle Tree:** Motores de trie avançados para validação stateless e provas compactas.
*   **IBFT 2.0 (Besu-style):** Consenso com finalidade imediata e tolerância a falhas bizantinas.
*   **DTS (Block Sharding):** Propagação eficiente de propostas através de fragmentação, mitigando o peso de assinaturas PQC na rede.

---
**Documento gerado pelo núcleo de engenharia da Jamii Blockchain - Maio/2026.**

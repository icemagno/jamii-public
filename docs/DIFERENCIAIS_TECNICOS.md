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

## 🚀 6. Natureza da Rede: Do Corporativo ao Público
O rótulo "corporativo" da Besu (e consequentemente a escolha do IBFT2 para a Jamii) muitas vezes gera confusão sobre a aplicabilidade da rede. No Jamii, desmistificamos essa visão através de três pilares fundamentais:

### 6.1. Besu: Corporativa vs. Pública
A stack Besu **não é limitada** a redes privadas. Ela é um dos poucos clientes "Mainnet-compatible", o que significa que ela roda na rede pública do Ethereum hoje, processando milhões de transações. O termo "corporativo" refere-se à disponibilidade de ferramentas que o Geth (padrão do mercado) não prioriza, como:
*   **Privacidade:** Capacidade de transações restritas a partes autorizadas.
*   **Permissionamento:** Controle granular de leitura e escrita.
*   **Conformidade:** Facilidade de auditoria e integração com sistemas legados.

### 6.2. O Papel do Consenso (IBFT 2.0)
O **IBFT 2.0** define o perfil de operação da rede, equilibrando vazão e finalidade:
*   **Finalidade Imediata:** Diferente de consensos probabilísticos (PoW/PoS tradicional), no IBFT 2.0 um bloco selado é definitivo. Isso elimina riscos de bifurcação (reorgs), sendo vital para aplicações financeiras de alto valor.
*   **Escalabilidade de Usuários vs. Validadores:** O IBFT 2.0 suporta **grandes cargas de usuários** (TPS elevado), otimizando a rede para um conjunto selecionado de validadores (20 a 100), onde a confirmação ocorre em segundos em vez de minutos.

### 6.3. A Jamii como "Autoestrada Digital" Pública
O modelo da Jamii, ao herdar a lógica da Besu e o consenso IBFT 2.0, posiciona-se como uma **Rede Pública Soberana**:
*   **Acesso Público:** Qualquer usuário pode transacionar e interagir com Smart Contracts de forma irrestrita.
*   **Governança Profissional:** Os validadores são entidades conhecidas ou selecionadas, garantindo que a rede privilegie a eficiência e a segurança da finalidade sobre a descentralização extrema de mineradores anônimos.

**Veredito:** A Jamii não é uma rede "privada", mas uma blockchain de **alta performance e baixa latência**, ideal para uso público massivo, funcionando como uma infraestrutura pública de alta confiabilidade.

---
**Documento gerado pelo núcleo de engenharia da Jamii Blockchain - Maio/2026.**

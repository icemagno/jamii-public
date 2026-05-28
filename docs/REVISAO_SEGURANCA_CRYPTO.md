# Revisão de Segurança do Módulo Crypto (Sprint 5+)

**Status:** Blindado (Certificação Industrial)
**Data da Revisão:** 13/04/2026

---

## 🔒 Diretrizes de Segurança Industriais Implementadas

### 1. Prevenção de Negação de Serviço (Anti-DoS)
*   O módulo de hashing (Keccak/SHAKE) é **Resiliente a Pânico**. 
*   **Ação:** O método `Sum` trunca buffers que excedem o `MaxShakeOutput` (4KB) silenciosamente, protegendo o nó contra interrupções de processo causadas por dados malformados ou ataques de exaustão de memória.

### 2. Higiene de Memória e Wiping de Segredos
*   O método `Zero()` em todas as implementações de `PrivateKey` (Secp256k1, ML-DSA, Hybrid) realiza **wiping físico** (limpeza profunda) da RAM.
*   **Material Blindado:** Caches de serialização e structs internas são zeradas via loop manual.
*   **Anti-Otimização:** Uso mandatório de `runtime.KeepAlive` para impedir que o compilador ignore as operações de limpeza por otimização.

### 3. Blindagem contra Maleabilidade e Mix-and-Match
*   **Strong Binding:** A assinatura híbrida vincula a mensagem original diretamente à representação serializada de ambas as chaves públicas (Tradicional e Quântica).
*   **Zero-Allocation:** O vínculo é calculado na pilha (stack) para evitar alocações e garantir performance constante sob carga massiva.

### 4. Signature Agility (Padrão Into)
*   As interfaces de hashing foram refatoradas para o padrão `Into` (preenchimento de buffers fornecidos pelo chamador), mantendo o sistema pronto para migrações futuras de tamanhos de hash sem quebra de performance.

---

## 📈 Métricas Definidas de Aceitação
O módulo deve manter uma estabilidade de **23.200 a 23.500 TPS** e uma frequência de **~100 GCs/s** sob carga massiva em hardware de referência para ser considerado operacional.

# Jamii: The Sovereign Post-Quantum Blockchain

<img width="2816" height="1536" alt="jamii" src="https://github.com/user-attachments/assets/717f708c-02a7-4b8f-87f8-1844188e2183" />


Jamii é uma blockchain de alta performance projetada para a era pós-quântica. Ela combina algoritmos tradicionais (Secp256k1) com assinaturas de rede baseadas em reticulados (**ML-DSA-65/Dilithium L3**) para garantir soberania digital e resistência contra computadores quânticos.


⚠️ Licença: Este sistema está protegido sob a PolyForm Noncommercial License 1.0.0. O uso e a modificação são livres para fins não comerciais, desde que mantidos os créditos ao autor original. Uso comercial é estritamente proibido.


## 🚀 Status do Projeto: Sprint 5.0 Concluída (Maio/2026)

O núcleo fundamental do Jamii atingiu maturidade industrial e passou por um processo de saneamento de código e documentação (Audit-Ready). Os seguintes módulos estão homologados e documentados em PT-BR:

*   **`pkg/core/types` (v1.1):** Definições soberanas de Header, Block e Receipt com constantes de tamanho padronizadas.
*   **`pkg/blockchain` (v1.1):** Gerenciamento de persistência com prefixos de DB nomeados e sincronismo determinístico.
*   **`pkg/consensus` (v1.0):** Motor IBFT 2.0 com *Validação Ativa* e *Crash-Resilience*.
*   **`pkg/trie` (v1.5):** Arquitetura *Bonsai Turbo* com **Verkle Tree Homomórfica (O(1))** validada para produção (~750 TPS pico).
*   **`pkg/crypto` (v1.0):** Implementação híbrida (Secp256k1 + ML-DSA-65) com *Strong Binding*.

## 🛠️ Diferenciais Técnicos

*   **Arquitetura Bonsai Turbo:** Separação estrita entre dados planos e árvore Merkle, permitindo acesso O(1) e suporte total a reorganizações de rede (Reorgs).
*   **Verkle Turbo (O(1)):** Implementação de cálculo incremental homomórfico ($C' = C + \sum \Delta \times G_i$), eliminando a re-computação $O(256)$ de compromissos IPA.
*   **Trie Agility & Stability:** Capacidade de alternar entre SMT e Verkle Trees. A Verkle Tree foi exaustivamente testada e estabilizada para operações em lote com assinaturas reais.
*   **Audit-Ready Documentation:** Documentação técnica rica em Português Brasileiro integrada diretamente ao código fonte dos módulos Core.
*   **Segurança PQC Híbrida:** Cada transação é protegida por dois portões criptográficos, com validação obrigatória de assinaturas Secp256k1 e ML-DSA.


## ⚡ Desempenho Soberano (Certificação Industrial 2026)

O Jamii atingiu maturidade de escala em seus dois motores de estado. Abaixo estão os resultados reais obtidos em testes de estresse agressivos (500 Tx/bloco, 1000 slots/contrato) em disco PebbleDB:

| Motor de Estado | Capacidade Real | Destaque Técnico |
| :--- | :--- | :--- |
| **SMT (Sparse Merkle)** | **~1.400 TPS** | **Alta Performance:** Foco em throughput bruto e baixa latência (170MB Heap). |
| **Verkle Trees** | **~750 TPS** | **Eficiência Homomórfica:** Otimização O(1) com finalização ultra-rápida (Sprint 5.0). |
| **Encoding SSZ** | **~390k TPS** | Serialização endurecida contra ataques de maleabilidade. |
| **Criptografia PQC** | **~100k TPS** | Verificação dual (Tradicional + Quântica) com memória blindada. |
| **Tipos Soberanos** | **~615k TPS** | Operações base (Address/Uint256) validadas em auditoria. |

> **Nota:** O TPS efetivo em rede é atualmente limitado pelo `BlockPeriod` de 2s (~270 TPS sustentados); a capacidade bruta do motor suporta picos de 750+ TPS.

## 🛡️ Documentação e Auditoria

O Jamii é regido por uma arquitetura rigorosa e documentada. Consulte os pilares da nossa documentação:

*   📜 **[Documentação Técnica (Arquitetura)](docs/DOCUMENTACAO_TECNICA.md):** A autoridade sobre o funcionamento interno.
*   🛠️ **[Manual de Desenvolvimento (Diretrizes)](docs/MANUAL_DESENVOLVIMENTO.md):** Regras de código e performance.
*   🧠 **[Memória Técnica Consolidada](MEMORIA_TECNICA.md):** Resumo executivo da arquitetura e decisões de design.
*   🏁 **[Plano Geral do Projeto](docs/PLANO_GERAL_PROJETO.md):** Roadmap e marcos de evolução.
*   ⚡ **[Otimizações Jamii Turbo](docs/jamii_turbo_optimization.md):** Detalhes sobre a arquitetura Zero-Alloc/Zero-Copy.

### Resultados da Auditoria (Abril/2026):
*   ✅ **Consenso SMT:** Estado convergente garantido via indexação por prefixo de bits.
*   ✅ **Criptografia L3:** Sincronização total com ML-DSA-65 (Nível 3).
*   ✅ **Imutabilidade de Memória:** Prevenção de Data Races no cálculo de hashes de transação.
*   ✅ **Resiliência DoS:** Operações aritméticas seguras com tratamento de erros.

## 🛠️ Começando

### Requisitos
*   Go 1.22+
*   GCC (para Keccak otimizado, se necessário)

### Rodando os Testes
Para garantir a integridade da sua instalação, execute a suíte de testes completa:
```bash
go test -v ./pkg/...
```
**Author:** Carlos Magno O. Abreu (magno.mabreu@gmail.com)
**License:** Sovereign Jamii License

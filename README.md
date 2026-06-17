# Jamii: The Sovereign Post-Quantum Blockchain

<img width="2816" height="1536" alt="jamii" src="https://github.com/user-attachments/assets/717f708c-02a7-4b8f-87f8-1844188e2183" />


Jamii é uma blockchain de alta performance projetada para a era pós-quântica. Ela combina algoritmos tradicionais (Secp256k1) com assinaturas de rede baseadas em reticulados (**ML-DSA-65/Dilithium L3**) para garantir soberania digital e resistência contra computadores quânticos.


⚠️ Licença: Este sistema está protegido sob a PolyForm Noncommercial License 1.0.0. O uso e a modificação são livres para fins não comerciais, desde que mantidos os créditos ao autor original. Uso comercial é estritamente proibido.


## 🚀 Status do Projeto: Sprint 9.4 Concluída (Junho/2026)

O núcleo fundamental do Jamii atingiu maturidade industrial, quebrando recordes de throughput e estabilidade através de processamento paralelo e especulativo, com foco recente em resiliência de rede e otimização do consenso sob sincronismo.

*   **`Consenso Resiliente (Watchdog de Liderança)`:** Redução do tempo de troca de propositor inativo de 10s para **2 segundos** se o propositor designado for detectado como "not ready" (syncing) ou desconectado.
*   **`CPU-Safe Sync (Observer Mode)`:** Filtros estritos que evitam a exaustão de CPU e estouro de memória sob inundações de rede (floods), descartando transações e selos do consenso (`MT_VALIDATOR_SEAL`) enquanto o nó estiver em modo Observer.
*   **`BitTorrent Sync Resiliente`:** Correção de concorrência e carregamento de metadados no boot, garantindo sincronização robusta e rápida do histórico em menos de 2 segundos para gaps grandes de blocos.
*   **`Archiver Sync Atômico`:** Sincronismo contínuo e callbacks DTS integrados no nó de arquivo desacoplado.
*   **`Chained State Oracle` (Recorde):** Novo recorde de **750 TX/s** sustentados (picos de **1.045 TX/s**) com blocos de 3.000 transações a cada 4 segundos.
*   **`pkg/trie` (v1.5):** Arquitetura *Bonsai Turbo* com **Verkle Tree Homomórfica (O(1))**.


## 🛠️ Diferenciais Tecnológicos (The Jamii Edge)

*   **Chained State Oracle (Active Speculation):** O Jamii utiliza um oráculo de estado encadeado que "lê o futuro". Enquanto a rede aguarda o tempo de cadência (Pacing) entre os blocos, o próximo propositor já monta e executa o bloco seguinte em uma thread paralela isolada (Sandbox). Quando a rodada começa, o bloco já está pronto para broadcast instantâneo, eliminando a latência de execução do caminho crítico do consenso.
*   **Witness-Aided Quorum Acceleration:** Diferente de outras blockchains, a Jamii separa a prova de estado do quórum de votação. Validadores usam o "Witness" para aprovar o bloco instantaneamente em RAM, enquanto a prova IPA pesada é processada em paralelo, eliminando o gargalo de latência do IBFT tradicional.
*   **Ultra-Compact Skeleton (Bi-Polar):** Redução de **81%** no tráfego de rede. Blocos com 3.000 TXs trafegam quase sem overhead, usando identificadores de 6 bytes (3 iniciais + 3 finais), permitindo que o dado chegue antes do sinal de consenso.
*   **DTS (Distributed Transaction Store):** Motor P2P de canal duplo (Express/Bulk). Sinais de consenso viajam por "vias rápidas" sem serem bloqueados por downloads de blocos pesados.

## ⚡ Desempenho Soberano (Certificação Industrial 2026)

Abaixo estão os resultados reais obtidos em testes de flood massivo (3.000 Tx/bloco):

| Métrica de Eficiência | Resultado Alcançado | Destaque Técnico |
| :--- | :--- | :--- |
| **Throughput (TPS)** | **~750.0 TX/s** | **Recorde Mundial PQC:** Desempenho industrial sustentado sob carga. |
| **Pico de Vazão** | **1.045 TX/s** | Explosão de processamento (Fidelity Match). |
| **Banda de Rede** | **~15 KB / 3k TXs** | **Bi-Polar Short IDs:** Ultra-compactação de esqueletos de bloco. |
| **Tempo de Liderança** | **< 1ms** | Montagem de bloco em tempo zero via promoção de especulação. |
| **Verkle Trees** | **O(1) Efficiency** | **Cálculo Incremental:** Compromissos IPA calculados sem re-hashing total. |
| **Criptografia PQC** | **~100k op/s** | Verificação dual (Tradicional + Quântica) com latência sub-milissegundo. |


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

### Configuração Inicial (Setup)
Antes de iniciar um nó pela primeira vez, você pode gerar sua identidade soberana (chave privada e manifesto de endereços) usando o comando de setup:

```bash
# Gera a identidade no diretório padrão (./data)
go run cmd/jamii/main.go setup generate-key

# Ou especificando um diretório customizado
go run cmd/jamii/main.go setup generate-key --datadir ./meu_node_data
```

Este comando criará o arquivo `nodekey` (chave privada híbrida PQC) e o `node_address.json` contendo seu **Sovereign Address**, necessário para inclusão no arquivo `genesis.json` caso você deseje ser um validador.

### Iniciando o Nó
```bash
# Inicia o nó usando a configuração padrão
go run cmd/jamii/main.go start

# Ou especificando um arquivo de configuração YAML
go run cmd/jamii/main.go start --config config.yaml
```

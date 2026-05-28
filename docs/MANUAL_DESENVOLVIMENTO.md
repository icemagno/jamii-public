# Manual de Desenvolvimento: Jamii Engine

Este manual define as regras de engenharia, padrões de codificação e protocolos de validação para desenvolvedores do Core do Jamii.

---

## 1. Organização de Pacotes (Standard Go Layout)

Seguimos a estrutura modular para garantir isolamento e testabilidade:
*   **`/cmd`**: Pontos de entrada da aplicação.
*   **`/pkg`**: Bibliotecas públicas exportáveis (Cripto, Encoding, Trie, Types).
*   **`/internal`**: Lógica privada do nó (P2P, Consenso, Store).

---

## 2. Princípios de Codificação (Idiomatic Go)

*   **Dependency Injection (DI):** Utilize interfaces para definir dependências. Mocks são obrigatórios para testes unitários.
*   **Isolamento de Estado:** Componentes não devem depender de variáveis globais. O estado deve ser passado explicitamente.
*   **Erro como Valor:** Trate erros explicitamente. Evite o uso de `panic` em código de produção.
*   **Concorrência Estruturada:** Utilize `context.Context` para gerenciar o ciclo de vida de goroutines e cancelamentos.

---

## 3. Gestão de Memória e Performance

Devido ao peso das assinaturas PQC (37x maiores que ECDSA), a performance é crítica:
*   **Buffer Pooling:** O uso de **`sync.Pool`** (utilizado no `SigPool`) é mandatório para reutilização de buffers de assinatura e serialização, minimizando a pressão sobre o GC.
*   **Zero-Allocation:** Priorize métodos que aceitem buffers de destino (`EncodeInto`, `SignTo`) para evitar alocações desnecessárias no heap.
*   **PebbleDB:** Utilizamos o Pebble (100% Go) para evitar os custos de JNI do RocksDB (C++).

---

## 4. Protocolo de Testes e Validação

Nenhuma funcionalidade é considerada concluída sem passar por este protocolo:

### 4.1 Testes Unitários e Adversários
*   **Cobertura:** Cada pacote em `/pkg` deve buscar 100% de cobertura nos caminhos críticos.
*   **Testes Adversários:** Além do "caminho feliz", implemente testes que tentem quebrar o sistema (Maleabilidade, Overflows, Out-of-bounds).

### 4.2 Fuzzing (Chaos Engineering)
Submeta decodificadores e motores de estado a mutações aleatórias de dados para garantir que o sistema não caia diante de ruídos de rede ou ataques propositais.

### 4.3 Race Detector
Sempre execute testes com a flag `-race` para identificar condições de corrida em operações concorrentes.

### 4.4 Filosofia de Testes Dual-Layer (Memória vs. Disco)
O Jamii adota uma estratégia rigorosa de validação em dois níveis para todos os módulos de estado e persistência:

1.  **Nível 1: Validação Lógica (MemoryStore)**
    *   **Objetivo:** Isolar a matemática e a lógica do protocolo de interferências externas.
    *   **Vantagem:** Garante o **Determinismo Puro**. Se uma raiz Merkle divergir em memória, o erro é algorítmico. A velocidade da RAM permite ciclos de feedback instantâneos para o desenvolvedor.
2.  **Nível 2: Homologação Industrial (PebbleStore/Disco)**
    *   **Objetivo:** Validar a durabilidade e a performance sob condições físicas reais.
    *   **Vantagem:** Expõe o custo real de I/O, latência de disco e eficiência de cache. Somente o teste em disco confirma se o estado sobrevive a reinicializações e se a SMT é resiliente à fragmentação de dados em larga escala.

**Regra de Ouro:** Um módulo de persistência ou árvore de estado só é considerado **Homologado** após passar com sucesso em ambos os níveis. Testamos em memória para garantir que somos **infalíveis**; testamos em disco para garantir que somos **eternos**.

---

## 5. Telemetria e Observabilidade (Industrial Logging)

Todo componente deve utilizar o logger centralizado em `pkg/util/logger`. Prints diretos ao console (`fmt.Print`) são estritamente proibidos em código de produção.

### 5.1 Padrão Log4J (Canonical Format)
As mensagens de log devem seguir o formato:
`[YYYY-MM-DD HH:MM:SS] [LEVEL] [MODULE] Message`

*   **Timestamp:** Data e hora precisas para auditoria.
*   **Level:** DEBUG, INFO, WARN, ERROR.
*   **Module:** Identificador em caixa alta (ex: CONSENSUS, CRYPTO, CORE).
*   **Language:** Todas as mensagens de log devem ser escritas em **Inglês Técnico**.

### 5.2 Níveis de Severidade
*   **DEBUG:** Informações de rastro para depuração (ex: processamento de opcodes individuais).
*   **INFO:** Eventos significativos e marcos de sucesso (ex: "Block #100 processed").
*   **WARN:** Anomalias que não impedem a operação, mas exigem atenção (ex: "Consensus network congested").
*   **ERROR:** Falhas críticas que exigem intervenção ou indicam corrupção (ex: "Database corruption detected").

---

## 6. Orquestração e Configuração (Módulo NODE)

A evolução do projeto exige a unificação dos módulos em um orquestrador central.

*   **CLI First:** Utilizamos `spf13/cobra` para gerenciar comandos.
*   **Miner Control:** A flag `--miner-enabled` deve controlar se o nó participa ativamente da produção de blocos ou atua como um observador passivo.
*   **Identity Management:** O nó deve ler sua identidade soberana de um keystore seguro, utilizando as funções do módulo `CRYPTO`.

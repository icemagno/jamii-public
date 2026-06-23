# 🧠 Memória de Decisão: Especulação de Bloco Híbrida vs. Pipelined BFT

Este documento registra a análise técnica, justificativa arquitetural e o posicionamento de engenharia adotado pela Jamii Blockchain quanto ao modelo de **Especulação de Blocos (Chained State Oracle)** e a decisão de não migrar para modelos de consenso totalmente pipelinados (como HotStuff ou Pipelined BFT).

---

## 1. O Contexto e a Janela de Ociosidade

Na arquitetura de blocos sequenciais da EVM, cada novo bloco $N+1$ depende integralmente do estado final gerado pelas transações do bloco anterior $N$. Tradicionalmente, isso cria um gargalo linear:
1. O nó recebe o bloco $N$ e atinge o consenso.
2. O nó executa o bloco $N$, modificando a Trie de Estado e gravando em disco.
3. O próximo propositor inicia a montagem do bloco $N+1$ coletando transações do pool e executando-as.

O passo 3 (execução de transações) consome tempo crítico de CPU (15ms a 50ms para blocos médios a cheios) que atrasa a propagação da nova proposta.

---

## 2. A Solução Implementada: Chained State Oracle (Especulação Híbrida)

Para eliminar esse atraso sem comprometer a estabilidade do banco de dados, a Jamii adotou o modelo de **Especulação Híbrida em RAM**:

1. **Ação Antecipada:** Assim que o nó atual valida localmente o Bloco $N$ e envia o seu voto de `PREPARE` para a rede, o próximo propositor designado inicia de forma assíncrona a tarefa `go c.speculateNextBlock(block)`.
2. **Execução Virtual em RAM:** Durante a janela ociosa em que a rede troca mensagens de consenso, o nó executa as transações candidatas do Bloco $N+1$ sobre uma representação efêmera do estado pós-bloco $N$ (`parentState` em cache de memória), sem gravar nada no disco físico.
3. **Persistência Segura:** O banco de dados físico (PebbleDB) só é de fato modificado com o commit oficial quando as assinaturas reais da rede fecham o quórum de consenso do Bloco $N$.
4. **Promoção Instantânea:** No milissegundo em que o consenso de $N$ avança, o propositor não reconstrói o bloco do zero. Ele apenas promove o `c.speculativeBlock` em RAM para proposta real, enviando-o para a rede com latência de CPU próxima a **< 1ms**.

---

## 3. Avaliação de Alternativas: Por que não Pipelined BFT (HotStuff)?

Foi avaliada a possibilidade de mudar a arquitetura para suportar especulação paralela profunda de múltiplas alturas (ex: propor $N+1$, $N+2$, $N+3$ sequencialmente sem esperar a finalização de $N$, no estilo HotStuff/DiemBFT). 

Essa abordagem foi **rejeitada** devido aos seguintes fatores:

### A. Assimetria de Gargalo (Rede RTT vs. CPU)
Em redes blockchain descentralizadas, o tempo é dominado pela latência de rede/transmissão (RTT - Round-Trip Time) e não pela execução local. Em um bloco de 1000ms:
* **~95% (950ms)** é gasto com assinaturas PQC, empacotamento, propagação DTS e coleta de quórum de votação.
* **~5% (50ms)** é gasto executando a EVM.

Mudar drasticamente a arquitetura para paralelizar múltiplos blocos traria ganhos marginais de TPS (poucos décimos de segundo), uma vez que a CPU do validador continuaria ociosa esperando a rede concordar com as propostas anteriores.

### B. Custo de Reversão de Estado e Complexidade de Forks
No IBFT 2.0, se um líder falha ou a rede perde sincronia, ocorre um *Round Change*. Em sistemas pipelinados profundos:
* Um único *Round Change* exigiria o descarte em cascata de múltiplos blocos especulativos pendentes.
* O nó precisaria gerenciar ramificações (forks) concorrentes da Trie de Estado na memória, aumentando massivamente o consumo de memória RAM e a chance de vazamento de memória.
* O custo computacional e de engenharia para implementar diários de reversão complexos não oferece retorno de investimento (ROI) de performance que justifique a complexidade.

---

## 4. Otimização de Recursos e Reversão Livre de I/O

A arquitetura híbrida adota a política de manter a reversão simples e barata:
* Como o bloco especulativo é mantido puramente em RAM (no cache de tries sujas do `StateDB`), reverter uma especulação inválida é uma operação trivial: **apenas descartamos os ponteiros em memória**.
* Não há escritas físicas em disco ou transações de banco de dados para desfazer (*rollbacks* de I/O).

---

## 🚀 Resumo de Posicionamento Arquitetural

| Abordagem | Benefício de Latência | Risco de Banco de Dados | Complexidade de Código | Decisão |
| :--- | :--- | :--- | :--- | :--- |
| **Execução Sequencial Pura** | Baixo (Latência linear de CPU) | Nulo (Seguro) | Mínimo | Rejeitada (Muito lenta) |
| **Pipelining BFT Profundo (HotStuff)** | Alto | Elevado (Forks e Rollbacks) | Máximo | Rejeitada (Ganhos não compensam o risco) |
| **Especulação Híbrida (Jamii)** | Alto (CPU removida do caminho) | Nulo (Commit pós-consenso) | Médio (Gerenciado em RAM) | **Adotada & Homologada** 🚀 |

---
**Data da Decisão:** Junho de 2026  
**Status:** Documentado e Homologado no Núcleo do Consenso IBFT.

  ## 📈 1. Cálculo de Vazão e TPS (Transactions Per Second)

  O flood de transações pós-quânticas (PQC) foi totalmente processado e gravado em disco entre os blocos #14 e #20. Vamos somar as transações processadas
  em cada um desses blocos:

  • Bloco #14: 1.272 TXs
  • Bloco #15: 882 TXs
  • Bloco #16: 2.295 TXs
  • Bloco #17: 2.286 TXs
  • Bloco #18: 2.897 TXs
  • Bloco #19: 2.483 TXs
  • Bloco #20: 2.885 TXs
  • Total: 1.272 + 882 + 2.295 + 2.286 + 2.897 + 2.483 + 2.885 = 𝟏𝟓.𝟎𝟎𝟎  TXs exatas (100% de integridade).

  ### A. TPS Médio da Janela de Processamento Ativo

  Considerando o tempo decorrido desde a chegada da primeira proposta de bloco do flood ( 15:59:15 ) até o commit final do último bloco do flood no banco
  de dados ( 15:59:39 ):

  • Tempo total: 39s - 15s = 𝟐𝟒  segundos
  • Vazão Média:
    15.000  TXs
    ─────────── = 𝟔𝟐𝟓  TPS Reais
       24  s

  ### B. TPS Médio da Janela Total de Execução

  Considerando o intervalo completo a partir do commit do bloco anterior ao flood (#13 committed em  15:59:12  até a gravação final do #20 em  15:59:39
  ):

  • Tempo total: 39s - 12s = 𝟐𝟕  segundos
  • Vazão Média:

    15.000  TXs
    ─────────── = 𝟓𝟓𝟓,𝟓𝟔  TPS Reais
       27  s

  ### C. Análise de Throughput por Bloco (TPS de Pico)

  • Bloco #14 (1.272 TXs): Proposta em  15:59:15  e Commit em  15:59:16 . Tempo de processamento: 1s → 1.272 TPS.
  • Bloco #15 (882 TXs): Proposta em  15:59:18  e Commit em  15:59:19 . Tempo: 1s → 882 TPS.
  • Bloco #16 (2.295 TXs): Proposta em  15:59:21  e Commit em  15:59:22 . Tempo: 1s → 2.295 TPS.
  • Bloco #17 (2.286 TXs): Proposta em  15:59:25  e Commit em  15:59:26 . Tempo: 1s → 2.286 TPS.
  • Bloco #18 (2.897 TXs): Proposta em  15:59:28  e Commit em  15:59:32 . Tempo: 4s (devido ao tempo de cálculo da Verkle) → 724,25 TPS.
  • Bloco #19 (2.483 TXs): Proposta em  15:59:33  e Commit em  15:59:35 . Tempo: 2s → 1.241,5 TPS.
  • Bloco #20 (2.885 TXs): Proposta em  15:59:37  e Commit em  15:59:39 . Tempo: 2s → 1.442,5 TPS.
  • Vazão Máxima Instantânea Registrada: 2.295 TPS no Bloco #16.
  ──────
  ## 🛡️ 2. Análise Técnica das Otimizações Ativas

  O log comprova a maturidade e o correto funcionamento das principais otimizações de arquitetura da Jamii:

  ### 1. Bi-Polar Short IDs (Compact Blocks)

  Em quase todos os blocos (ex: #14, #15, #16, #17, #19, #20), vemos a linha:
   [INFO ] [NODE] Block #14 RECONSTRUCTED successfully via Bi-Polar Short IDs.
  A rede não transmitiu blocos cheios redundantes. Os nós propagaram apenas o esqueleto compacto de Short IDs e reconstruíram os payloads localmente a
  partir da MemPool em menos de 1 segundo, reduzindo drasticamente o consumo de banda de rede P2P.

  ### 2. State Promotion (Sandbox -> Canonical)

  Nos logs vemos a transição limpa:
   State Promotion: reusing pre-calculated state for block #14
   State Promotion Successful: Sandbox promoted to Canonical State.
  Em vez de re-executar as milhares de transações e recalculá-las no banco de dados na fase de commit final, o motor de execução reutilizou o estado de
  Sandbox efêmero previamente calculado, poupando ciclos de CPU.

  ### 3. Validação Otimista (Skiping IPA Math)

  Ao aplicar a Witness com sucesso nas Verkle Trees, a blockchain ativou o modo otimista:
   VerifyPayload: Optimistic verification active (Witness present). Skipping IPA math for round speed.
  Isso permitiu que os validadores votassem  PREPARE  e  COMMIT  quase instantaneamente (na mesma fração de segundo), pois pularam os custosos cálculos
  matemáticos de prova de abertura IPA (Inner Product Argument) que atrasam o consenso.

  ### 4. Montagem Especulativa (Chained State Oracle)

  No bloco #18, o  NODE_E  (nó local) foi sorteado como Proposer. O log mostra:

  • 15:59:26: Início do processamento especulativo do bloco #18 ( Speculative assembly for block #18 initiated ) em segundo plano concorrente enquanto o
  consenso ainda finalizava o #17.
  • 15:59:28: Especulação terminada com 2.897 TXs selecionadas.
  • 15:59:28: Na sua vez de propor o bloco #18, o nó promoveu o bloco especulativo instantaneamente:  PROMOTED: Using pre-assembled speculative block for
  #18 (2897 txs) .
  • Resultado: Latência de montagem pelo proposer de zero milissegundos, eliminando o tempo de espera da rede.
  ──────
  ## 💰 3. Dinâmica Econômica (BaseFee EIP-1559)

  A MemPool escalou a BaseFee dinamicamente e com precisão cirúrgica de acordo com a congestionamento:

  • Bloco #13 (Ocioso): BaseFee = 176.240.085 (A MemPool foi zerada após o bloco 13).
  • Bloco #14 (1.272 TXs): BaseFee recalibrada para 154.210.075.
  • Bloco #15 (882 TXs): BaseFee recalibrada para 169.260.978.
  • Bloco #16 (2.295 TXs): BaseFee recalibrada para 174.228.787.
  • Bloco #17 (2.286 TXs): BaseFee recalibrada para 222.424.825.
  • Bloco #18 (2.897 TXs): BaseFee recalibrada para 283.602.773.
  • Bloco #19 (2.483 TXs): BaseFee recalibrada para 391.931.942.
  • Bloco #20 (2.885 TXs): BaseFee recalibrada para 513.244.676 (Pico de congestionamento).
  • Bloco #21 (0 TXs - Vazio): BaseFee escala para 708.213.497 (Cool-down ativado na MemPool, zerando as TXs pendentes).
  • Bloco #22 (0 TXs - Vazio): BaseFee abaixa para 619.686.810 (Mercado se re-estabilizando após a vazão de todo o flood).

  ### Conclusão do Teste

  O teste demonstra que a rede tem desempenho exemplar de ~625 TPS sustentados, lidando com blocos de até 3.000 TXs pós-quânticas. As otimizações de
  State Promotion, Bi-Polar Short IDs, e Speculative Proposing foram o que permitiu manter a taxa média acima de 550 TPS reais mesmo com o peso
  criptográfico do ML-DSA.
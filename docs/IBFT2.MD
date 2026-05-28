  📑 Tratado Técnico: Consenso IBFT 2.0 na Jamii Blockchain

  1. Configuração Inicial e Cálculo de Quórum
  O IBFT 2.0 utiliza a fórmula de tolerância a falhas bizantinas $n \geq 3f + 1$. Para uma rede com 5 nós ($n=5$):

   * Capacidade de Falha ($f$): A rede suporta até 1 nó arbitrariamente malicioso ou offline ($f=1$).
   * Quórum ($Q$): Para atingir a finalidade, utilizamos a fórmula $\lceil \frac{2n + 1}{3} \rceil$. Para 5 nós, $Q = 4$.
   * Validadores: {N1, N2, N3, N4, N5}.
   * Estado Atual: Bloco #0 (Gênese) persistido. Altura Alvo: #1.

  ---

  2. A Mecânica do Propositor e Mudança de Rodada (Round Change)
  Aqui detalhamos o que acontece tecnicamente quando o nó escolhido pela fórmula cai ou sai da rede.

  2.1. O Propositor é "Presumido"
  Cada nó (N1 a N5) calcula localmente: Propositor = (Altura + Rodada) % n.
   * Exemplo: Se estamos no Bloco #3, Rodada 0, todos "sabem" que o líder é o N3. Todos ficam em silêncio, aguardando a proposta dele.

  2.2. O Mecanismo do Timer (A Sentinela)
  Ao iniciar a altura #3, todos os nós ligam um cronômetro (ex: 2 segundos).
   * Se o N3 caiu, ele nunca enviará a proposta.
   * Os outros nós (N1, N2, N4, N5) não fazem nada enquanto o tempo não acaba. Eles apenas esperam.

  2.3. A Votação de Mudança de Rodada
  Quando o cronômetro chega a zero em N1, N2, N4 e N5, eles concluem: "O líder N3 falhou". Ocorre então a votação de coordenação:
   1. Cada nó envia uma mensagem: "Eu voto para pular para a Rodada 1" (RoundChangePayload).
   2. Eles precisam atingir o Quórum de 4 votos para aceitar a mudança.
   3. Assim que N1 recebe os votos de N2, N4 e N5 (mais o dele mesmo), ele tem prova de que a rede concorda que o N3 está fora.

  2.4. A Fórmula Recalculada
  Agora a rodada é 1. Novo Propositor = (3 + 1) % 5 = N4. O N4 assume a liderança imediatamente e tenta propor o bloco que o N3 não conseguiu.

  ---

  3. Quedas durante a Votação e "Locking"
  Este é o cenário mais complexo que o código trata:

   * Cenário: N3 envia a proposta, N1 e N2 já votaram Prepare, mas o N3 cai antes de enviar o seu próprio voto ou antes que N4 e N5 recebam a proposta.
   * Resultado: Ninguém atinge o quórum de 4 votos de Prepare. O timer estoura.
   * Recuperação de Bloqueio (Locking): Se um nó já tinha atingido o estado Prepared na Rodada 0, ele carrega esse bloco para a Rodada 1 e diz ao novo
     líder: "Eu já estava preparado para este bloco, não tente propor outro". Isso garante que o consenso não se divida.

  Por que a fórmula é melhor que votar no líder?
   1. Imunidade a DoS: Como todos sabem quem é o líder, a rede não perde tempo negociando.
   2. Previsibilidade: O sistema é justo; todos os validadores terão sua chance de propor blocos e receber recompensas.
   3. Resiliência: Se o líder for "mentiroso" (Bizantino) e enviar propostas diferentes, ele nunca conseguirá o quórum de 4 assinaturas para o mesmo bloco,
      o timer estourará e ele será removido da liderança.

  ---

  4. Autoridade e Ordenação Lexicográfica
  Transformamos endereços complexos de 32 bytes em números simples de 0 a 4 através de regras rígidas:

   1. A Lista Canônica: Usamos a lista de validadores definida no Bloco Gênese ou atualizada via governança.
   2. Ordenação Lexicográfica: Para evitar que a ordem mude entre os nós, o protocolo IBFT exige que a lista seja ordenada alfabeticamente pelos bytes do
      endereço.
       * Exemplo: N_Beta (0x111...) vira índice [0], N_Delta (0x222...) vira [1], e assim por diante.
   3. Resolução do Índice: O sistema acessa o índice calculado no array ordenado:

   1     index := (rid.Sequence + uint64(rid.Round)) % uint64(len(s.validators))
   2     address := s.validators[index]
   4. Self-Awareness ("Quem sou eu?"): Cada nó deriva seu endereço da sua chave privada e compara com o índice calculado. Se bater, ele assume a liderança;
      se não, aguarda em silêncio.
   * Nota Crítica: Se as listas forem diferentes entre os nós, a rede entra em ciclo infinito de Round Change. No Jamii, a StateRoot garante que todos vejam
     a mesma lista.

  ---

  5. Gestão de Membros (Admissão e Transição)
   1. Contrato Social: A lista de validadores e o valor de $n$ são imutáveis para uma determinada altura. Se o Bloco #3 começou com 5 nós, ele terminará com
      5 nós.
   2. Admissão (N6): Ocorre via Votação de Governança (transações incluídas no Bloco #10).
   3. Transição: O nó N6 só passa a contar na fórmula na Altura #11. Se ele votar na altura #10, seus votos são descartados silenciosamente (ou logados como
      Warn).
   4. O Problema da Reordenação: A entrada de N6 pode mudar o "número" de todos os outros (ex: se N6 for 0x000, ele vira o novo Node 0). Isso não quebra a
      rede porque a mudança é síncrona para todos na virada do bloco.

  ---

  6. Persistência Industrial no PebbleDB (SSD)
  Guardamos os votos da rodada atual no SSD por três razões fundamentais:

   1. Proteção contra Equivocação (Voto Duplo): Evita que um nó, após um crash e reinício, vote em um bloco diferente para a mesma altura por
      "esquecimento". O disco é a âncora de integridade.
   2. Recuperação Atômica: Se o nó trava após receber o quórum de Commit mas antes de gravar o bloco, ao voltar ele mesmo conclui: "Eu já tinha as 4
      assinaturas, vou apenas terminar de gravar". Ele não depende dos vizinhos para se recuperar.
   3. Consenso vs Sincronismo: Os votos em disco servem para a segurança de milissegundos. Se o nó ficar fora por dias, ele entra em modo "Download de
      Blocos" (Sync), ignorando os votos temporários. O Pruning deleta mensagens de alturas passadas para manter o banco limpo.

  ---

  7. Walkthroughs de Ciclo de Bloco

  7.1. Bloco #1 (O Caminho Feliz)
   1. NEW ROUND (R:0): Líder N1 inicia o timer.
   2. PRE-PREPARE (Proposta): N1 monta o bloco, assina e faz o Multicast.
   3. PREPARE: N2-N5 validam a proposta (assinatura de N1, se ele é o líder, integridade das transações) e enviam seus votos.
   4. ESTADO PREPARED: Quando um nó recebe Proposta + 3 votos de Prepare, ele bloqueia esse bloco.
   5. COMMIT: Ao estar Prepared, o nó envia um CommitPayload.
   6. COMMITTED (Finalização): Ao receber 4 votos de Commit, o bloco é gravado e a rede salta para a Altura #2.

  7.2. Bloco #3 (Troca de Líder por Timeout)
   1. N3 (Líder) sofre queda de energia.
   2. Timer de 2s expira em N1, N2, N4 e N5.
   3. Round Change: Quórum de 4 nós vota para saltar para Rodada 1.
   4. NEW ROUND (R:1): Novo líder N4 assume, timer dobra para 4s (Exponential Backoff) e o bloco é finalizado.

  ---

  8. Mecânicas de Baixo Nível
   * Layout Binário: c:[Height][Round][Type][Sender].
   * Strong Binding (PQC): O nó vincula o Hash do Bloco + Chave Pública Tradicional + Chave Pública Quântica. Impede a substituição de chaves em trânsito.
   * O Selo (Seal) no ExtraData: As assinaturas de Commit são inseridas no ExtraData do header. Isso permite que novos nós validem o bloco no futuro apenas
     olhando o cabeçalho, sem re-executar o consenso.

  ---

  9. Validadores vs. Observadores (Full Nodes)
   1. Validadores: VIPs que executam o algoritmo completo e decidem o bloco ($O(n^2)$ de mensagens).
   2. Observadores: Escutam os blocos, verificam as assinaturas no ExtraData (o Selo) e, se o quórum estiver correto, processam o bloco. Eles permitem
      escalar a rede para milhares de nós sem sobrecarregar o consenso.
   3. A Flag --miner-enabled=false:
       * Recursos: Desliga timers e lógica de proposição desnecessária para quem não é validador.
       * Segurança: O observador não precisa carregar chaves privadas "quentes" na memória.
       * Rede: Evita ruído de mensagens de consenso no P2P.

  ---

  10. O Consenso como Maestro (Main Loop)
  O IBFT dita a cadência:
   1. StartHeight: Inicia a pulsação.
   2. GetPayload: Aciona o StateProcessor e VM para montar o bloco.
   3. Multicast: Orquestra a rede P2P.
   4. OnBlockFinalized: Aciona a persistência física (Blockchain.AddBlock e StateDB.Commit).

  O Papel do Nó: O pkg/node/node.go deve garantir o fechamento do ciclo: assim que a altura $H$ termina, limpa caches e inicia $H+1$. Sem esse laço, o
  sistema para após o primeiro bloco.

  ---

  11. Estados de Transição do Validador: Sync vs. Consenso
  Um validador industrial não deve começar a votar assim que liga. Ele segue um ciclo de vida de segurança:

  1. Modo Sincronismo (Sync):
     * O nó detecta que sua altura local é menor que a altura da rede.
     * Ele atua estritamente como "Observador".
     * Não envia mensagens de consenso (Prepare/Commit), evitando poluir a rede com votos inválidos.
     * Foca 100% da CPU em baixar blocos, executar transações e reconstruir o StateDB.

  2. Ponto de Virada (Transition):
     * Quando AlturaLocal == AlturaRede, o nó sinaliza prontidão.
     * Ele "vira a chave" de Observador para Validador Ativo.

  3. Modo Consenso (Participante):
     * O nó agora participa da rodada atual.
     * Se for o líder, propõe blocos; se não, vota nas propostas recebidas.

  ---

  12. Livelock e Recuperação de Quorum (Regra f+1)
  Um desafio crítico no IBFT 2.0 ocorre quando múltiplos nós caem e retornam em rounds diferentes, ou quando novos nós entram em uma rede que já avançou muitos rounds por timeouts.

  12.1. O Problema do Descompasso
  Se os Nodes A e C caem na altura #23 e retornam minutos depois, eles iniciam no Round 0. Entretanto, os sobreviventes (B e D) podem estar no Round 100 devido a timeouts sucessivos. Como A e C esperam propostas do líder do Round 0, e B e D esperam do líder do Round 100, a rede entra em Livelock.

  12.2. A Solução: Weak Quorum (f+1)
  Implementamos a regra de catch-up industrial:
   1. Validação de Sequência: Mensagens de Round Change agora são validadas contra a `currentHeight` local. Mensagens de alturas passadas são descartadas.
   2. Salto por Quorum Fraco: Se um nó recebe $f+1$ mensagens de Round Change para um round $R_{target} > R_{local}$, ele conclui que a rede já avançou.
   3. Sincronização Automática: O nó local abandona seu round atual, "salta" para $R_{target}$, emite seu próprio voto de Round Change e reinicia seus timers. Isso permite que nós reiniciados alcancem o quorum da rede em milissegundos.

  12.3. Ajuda Proativa (Helping)
  Para acelerar a convergência, se um nó recebe um pedido de Round Change para o round em que ele já se encontra, ele re-transmite seu voto original para ajudar o par atrasado a atingir o quorum mais rapidamente.

  1. O Papel do StateProcessor como Mediador
  No arquivo pkg/core/processor.go, o método ProcessBlock é o elo principal. Ele orquestra o ciclo de vida do estado durante a proposta ou validação de um
  bloco pelo IBFT2:

   * Snapshotting em RAM: Antes de aplicar as transações, ele chama st.Snapshot(). Isso permite um rollback rápido na memória se uma transação falhar
     durante a execução da VM, sem tocar no banco de dados.
   * Finalização Atômica (st.Commit()): Ao final do bloco, o processador chama o Commit(). É aqui que a mágica do Bonsai Turbo acontece:
       1. Os saldos são atualizados no prefixo d:.
       2. Os diferenciais (logs) são gerados no prefixo l:.
       3. A raiz Merkle é calculada e persistida no prefixo n:.
   * Validação de Consenso: O stateRoot resultante é comparado com o header.StateRoot. No IBFT2, se um validador detecta uma divergência aqui durante a fase
     de PREPARE, ele não enviará o voto de COMMIT, impedindo que um bloco inválido seja finalizado.

  2. A Camada de Consenso (Interface Engine)
  O arquivo pkg/consensus/interface.go define os métodos VerifyHeader e Seal. No contexto do IBFT2:
   * VerifyHeader: Deve verificar as assinaturas dos validadores no campo extraData (conforme especificação do IBFT2).
   * Seal: No IBFT2, este método é responsável por coletar as assinaturas dos validadores (2/3 + 1) e inseri-las no bloco antes de propagá-lo.

  3. O que falta para a integração completa (Trabalho em Progresso)
  Com base no que vi, os próximos passos para o consenso interagir perfeitamente com os "Trie Logs" (Logs de Rollback) do Bonsai são:

   1. Lógica de Reorg no Consensus: O IBFT2 raramente sofre reorgs (finalidade imediata), mas se houver uma falha de rede ou bifurcação temporária antes da
      finalidade, o motor de consenso precisará chamar o RollbackToRoot (que já existe na SparseMerkleTree) usando os logs l:.
   2. Verificação de Provas de Estado: Durante o VerifyHeader, o consenso poderia usar o Get do modelo plano para verificar rapidamente se o autor do bloco
      é realmente um validador autorizado no estado atual.

  Resumo da Eficiência
  A arquitetura garante que o IBFT2 nunca precise lidar com a complexidade da árvore Merkle diretamente. Ele apenas entrega um bloco para o StateProcessor,
  que utiliza a eficiência do armazenamento plano (Bonsai) para executar e devolver a raiz de estado. Se o consenso decidir que aquele bloco não é mais a
  cabeça da cadeia, ele tem o mecanismo de logs l: pronto para "desfazer" o disco de forma atômica.

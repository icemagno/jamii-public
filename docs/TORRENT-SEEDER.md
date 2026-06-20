A implementação da Jamii Blockchain foi projetada especificamente com técnicas de Virtualização Zero-Copy e Fatiamento Dinâmico para anular o risco de esgotamento de memória (RAM) e espaço em disco nos nós semeadores (seeders).

  Abaixo está o detalhamento técnico de como esse fluxo funciona e como as suas dúvidas se resolvem no design atual do projeto:
  ──────
  ### 1. Resolução Virtual dos Chunks (Estratégia Zero-Copy)

  Quando a cadeia está em uma altura elevada e um novo nó entra pedindo o histórico, os nós não criam um arquivo físico temporário com todos esses blocos, nem sobem gigabytes de dados na RAM.

  No módulo storage.go (e especificamente no storage.go), a Jamii implementa um sistema de Seeding Virtual:

  • Fatiamento em Lotes (Chunks): A sincronização via Torrent não solicita "tudo de uma vez" em um arquivo gigantesco. Ela divide o intervalo de sincronização em blocos lógicos de 1.000 blocos por vez (ex:  chunk_1_1000.bin ,  chunk_1001_2000.bin ), conforme definido no gerenciador de sincronia (sync.go).
  • Indexação Leve: O nó semeador cria apenas um índice leve na RAM ( VirtualTorrentSeeder ) contendo a lista de metadados dos blocos (altura, tamanho físico em bytes serializados e offset lógico que eles teriam dentro de um arquivo contíguo imaginário).
  • Leitura Lazy (Sob Demanda): A biblioteca de Torrent divide esse arquivo virtual de 1.000 blocos em peças (Pieces) pequenas (ex: de 256 KB). Quando um
  peer remoto solicita uma peça específica, o método  ReadAt  (storage.go) é acionado:
      1. O sistema calcula a quais blocos reais aquele pedaço de bytes pertence.
      2. Ele busca apenas esses blocos de forma pontual no banco de dados local (banco KV).
      3. Realiza o marshal (serialização) deles na hora, preenche o buffer da rede e descarta a memória imediatamente.


  │ [!TIP]
  │ Conclusão sobre RAM: Não há perigo de esgotar a memória RAM porque em nenhum momento o arquivo torrent inteiro é materializado nela. O consumo de RAM
  │ nos seeders limita-se apenas às peças de 256 KB ativamente transmitidas pela placa de rede naquele milissegundo.
  ──────
  ### 2. O Fluxo de Download no Nó Novato (Downloader)

  No lado do nó que está ingressando na rede:

  1. Ele baixa o pedaço do arquivo virtual temporário e o escreve fisicamente em disco de forma particionada (utilizando o  FileTorrentDownloader ).
  2. Assim que o download do chunk de 1.000 blocos é finalizado, o nó importa os blocos para o seu StateDB local.
  3. Imediatamente após a importação e validação bem-sucedida, o nó exclui o arquivo temporário do disco ( os.Remove(filePath)  no sync.go).
  4. O processo é repetido sequencialmente para o próximo lote de 1.000 blocos até atingir a altura atual da rede.
  ──────
  ### 3. Nó Comum (Full Node / Validador) vs. Archiver Node

  A separação de responsabilidades entre um nó validador comum e um Archiver é fundamental para a saúde da blockchain no longo prazo:

   Característica       | Nó Comum / Validador (node.go)                                | Archiver Node Decoupled (main.go)
  ----------------------|---------------------------------------------------------------|----------------------------------------------------------------
   Consenso             | Sim. Valida blocos ativamente e participa do quórum de        | Não. Apenas observa a rede de fora sem influenciar na criação
                        | assinaturas PQC.                                              | de blocos.
   Banco de Dados       | Banco de dados Key-Value focado em desempenho bruto de        | Banco de dados relacional tradicional (PostgreSQL) para
                        | leitura/escrita da árvore de estado (PebbleDB/LevelDB).       | armazenamento massivo indexado.
   Poda (Pruning)       | Sim. Pode deletar recibos de transações e estados antigos que | Não. Mantém 100% de todo o histórico da blockchain e estado de
                        | já passaram do período de reversibilidade.                    | cada conta desde a gênese.
   Finalidade principal | Manter a segurança e o processamento de novas transações na   | Servir dados de histórico estruturados para Block Explorers,
                        | MemPool.                                                      | APIs JSON-RPC públicas e consultas analíticas pesadas.

  Em termos práticos, um nó comum apenas precisa semear e responder por blocos históricos que ele ainda possui de acordo com suas políticas locais de
  espaço. Já o Archiver atua como uma grande biblioteca centralizada: ele armazena permanentemente tudo e possui recursos de cache aumentados para
  suportar a carga de leitura síncrona exigida pelo streaming histórico.
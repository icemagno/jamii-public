# 🧠 Jamii - Memória Técnica Consolidada

Este arquivo serve como o "cérebro" de longo prazo para o desenvolvimento, permitindo que a IA mantenha um contexto enxuto e rápido.

## 🏛️ Arquitetura Core (Imutável)
- **VM:** Ordem LIFO (Top op Next), aritmética industrial, flags de overflow integradas no `types`. 
- **Criptografia:** Híbrida (ML-DSA + Secp256k1). Identidade soberana e mirror (0x) compartilham o mesmo payload de 20 bytes derivado da Secp256k1.
- **Endereçamento:** `jamii1...` (Sovereign) e `0x...` (Mirror/EIP-55) são a mesma conta no StateDB.
- **Armazenamento:** Arquitetura Bonsai/SSZ com suporte a Verkle Trees (default) e SMT (Sparse Merkle Trie) através da Trie Factory.

## 🛠️ Padrões de Desenvolvimento
- **Compliance:** 100% Besu/Geth logic (Yellow Paper).
- **Módulos Homologados:** `types`, `encoding`, `crypto`, `store`, `trie`, `wallet`. Alterações exigem auditoria.
- **Testes:** Soberanos. Não alterar testes para corrigir bugs.

- **Concluído:** Unified Identity, **Sovereign Transaction V1 (EIP-1559 Native)**, Mirroring nativo de saldo, Dívida Técnica Bloqueante (Gas Price, ReceiptsRoot, Buy Gas, VM Integration, JSON-RPC Read-only), Rede P2P (DTS Engine), MemPool (Gestão de transações pendentes com Purga Descendente), Sincronismo Determinístico (Besu-style), Conformidade Industrial IBFT2 (Pacing, Configurable Timeouts), Segurança Sync-to-Consensus (Observer Mode), Saneamento de Logs (Critical Level), Ghost Root Killer (Determinismo de Blocos Vazios), Resiliência de Sincronia (Channel Drainage), Compactação Soberana (Storage Optimization), Tsunami PQC Test (10.000 TXs Sustentadas), **Blindagem de Mercado (London/Besu Logic)**, **Otimização de Cache Industrial (MaxWarmTries Guard)**, **Sincronização Atômica Sync-Consensus (Anti-Self-Sabotage)**, **Chained State Oracle (Active Speculation)**, **Desacoplamento do SDK Java (Pure SDK)**, **Wallet Web App de Exemplo (Interativa)** e **Terminologia de Estado (Verkle/SMT).**

## 🛠️ Decisões Recentes (02/07/2026)
1. **Resolução de Apelidos de Validadores no Cartório do Archiver (P2P Handshake Alias Persistence):**
    - **Contexto:** Nós observadores (como o Archiver) podem utilizar arquivos `genesis.json` sem chaves públicas (`publicKey: ""`) pré-configuradas. Ao inicializarem, esses nós não registram as identidades dos validadores localmente sob apelidos no boot, dependendo unicamente da recepção das chaves públicas durante o handshake da rede P2P (DTS).
    - **Problema:** O handshake de conexão direta do DTS transmite apenas a chave pública para autenticação mútua (sem carregar o nome/apelido do validador para poupar banda). Isso fazia com que o método `Register` do cartório do Archiver registrasse a identidade com o apelido vazio (`""`), deixando a coluna `alias` da tabela `identities` vazia no PostgreSQL e inviabilizando a exibição dinâmica de nomes no explorador de blocos.
    - **Ação 1 (Pre-registro de Apelidos de Gênese):** Correção em [main.go](file:///c:/Magno/Projetos/jamii/cmd/archiver/main.go) e [node.go](file:///c:/Magno/Projetos/jamii/pkg/node/node.go) para ler todos os validadores e apelidos do `genesis.json` no boot e cadastrá-los na tabela in-memory do `IdentityRegistry` via `RegisterAlias`, mesmo que a chave pública ainda não esteja disponível.
    - **Ação 2 (Resolução e Persistência Oportuna):** Ajuste na rotina `RegisterWithAlias` de [registry.go](file:///c:/Magno/Projetos/jamii/pkg/crypto/signer/registry.go) para que, caso o parâmetro de apelido seja vazio (como no handshake), busque-se o apelido correspondente do mapa de memória local e, caso exista, grave no banco de dados (`alias:<address>`). Adicionada lógica de persistência para atualizações de apelidos mesmo para identidades já em cache.
2. **Profissionalização do Smart Contracts Gate no Block Explorer:**
    - **Contexto:** O protótipo inicial do Smart Contracts Gate em `index.html` utilizava classes e tamanhos de fonte de cards estatísticos, resultando em cabeçalhos de descrição gigantescos e inputs/formulários desproporcionais ou sem estilos dedicados.
    - **Ação 1 (Estilização Unificada):** Implementação de regras de formulário (`.form-label` e `.form-input`) e botões genéricos de ação (`.btn` e `.btn-primary`) nas declarações CSS globais, combinando com o tema claro (Light Premium) e degradês Azul e Laranja do logo oficial.
    - **Ação 2 (Reestruturação de Layout):** Substituição do banner com cards estatísticos por um painel exclusivo (`.section-card`) com fundo em degradê suave, limitando o tamanho dos textos. Transposição das colunas do dashboard de contratos para a classe `.section-card`, unificando as bordas finas e sombras premium.
    - **Ação 3 (Saneamento do Loader e Color Coding):** Substituição do texto estático "Carregando contratos..." por uma linha de loading animada com spinner do FontAwesome (`fa-spinner`). Classificação visual de rotinas de contrato por cores: métodos de leitura (view/pure) destacados em verde (`var(--success)`) e métodos de escrita/transação em laranja (`var(--accent-orange)`), com os retornos e TX hashes exibidos em caixas monospaçadas formatadas em Fira Code.
    - **Ação 4 (Escopo Global de Funções auxiliares):** Correção do bug onde o clique no botão "Registrar Existente" não realizava ações devido ao fato das funções auxiliares da aba de contratos estarem aninhadas dentro da função `initApp()`, tornando-as inacessíveis a partir de gatilhos HTML inline (`onclick`). Fechou-se o escopo de `initApp()` logo após o intervalador e moveu-se as declarações de interação para o escopo global.
    - **Ação 5 (Segurança no Deploy de Contratos):** Implementação de bloqueio de deploy de contratos sem carteira conectada. O formulário agora esconde o botão de deploy caso o usuário esteja sem sessão ativa de carteira (`!walletState.logged`), exibindo em seu lugar um banner de aviso que redireciona à aba de login, além de uma salvaguarda programática com `alert()` na função `submitContractDeploy()`.
    - **Ação 6 (Correção de Status de Conexão RPC):** Resolução do bug de interface onde o "Status Nó RPC" no painel da carteira exibia permanentemente o valor inicial "Desconectado" mesmo com o nó em execução. A correção incluiu atribuir o ID `wallet-node-status-disp` ao elemento correspondente no DOM e atualizá-lo tanto durante o intervalo periódico de polling de carteira quanto imediatamente após logins bem-sucedidos (via keystore, mnemônico ou criação).
    - **Ação 7 (Alinhamento dos Endereços da Carteira):** Resolução de bug visual no painel da carteira onde o botão de cópia do "Endereço Espelho (Mirror)" quebrava para a linha inferior. Forçou-se comportamento inline-flex (`display: inline-flex; align-items: center; gap: 6px; white-space: nowrap;`) no estilo da classe `.wallet-info-val`, garantindo alinhamento horizontal consistente em qualquer tamanho de rótulo.
    - **Ação 8 (Resolução de ReferenceError no Deploy):** Substituição das chamadas obsoletas `showStatusModal` e `hideStatusModal` (que disparavam `ReferenceError`) pelas funções canônicas `showTxStatusModal` e `hideTxStatusModal` na função `submitContractDeploy()`.
    - **Ação 9 (Correção de Panic na Witness do Bloco):** Resolução de crash fatal (`nil pointer dereference`) em [statedb.go](file:///c:/Magno/Projetos/jamii/pkg/core/state/statedb.go) durante a chamada `GenerateWitness()` na especulação de blocos de deploy. O problema ocorria porque a EVM adiciona pré-compilados (0x01 a 0x09) à `accessList` no boot do frame, mas eles não são instanciados no cache de banco de dados (`s.accounts`), provocando falha ao ler o endereço. O método foi ajustado para validar a existência da conta no cache de estado e ignorar contas inexistentes ou nulas.
    - **Ação 10 (Mapeamento de Parâmetros de Chamada RPC):** Correção no backend do explorador em [ApiController.java](file:///c:/Magno/Projetos/jamii/explorer/src/main/java/com/jamii/explorer/ApiController.java) onde os parâmetros fornecidos à função `client.ethCall` do SDK estavam na ordem incorreta (mapeando o calldata do contrato no campo de endereço de destino, o que induzia o RPC do nó Go a tentar decodificar calldata longo como endereço e entrar em panic por estouro de vetor). A chamada foi corrigida para `client.ethCall(null, cleanAddress, calldataHex)`.

## 🛠️ Decisões Recentes (26/06/2026)
1. **Consistência de Nomes de Validadores (Peer Alias / Naming Unificado):**
    - **Contexto:** Os nomes/apelidos de validadores eram configurados localmente ou de forma estática no explorador de blocos, impedindo a consistência na identificação ao adicionar novos validadores na rede.
    - **Problema:** Mapeamento estático de apelidos no frontend do explorer (`knownAddresses`) e necessidade de passar o apelido no boot e no gênese de forma robusta e persistente.
    - **Ação 1 (Gênese Unificado):** Adição do campo `"alias"` na configuração de validadores (`ValidatorConfig` em `genesis.go`). Os apelidos agora são definidos no genesis e registrados dinamicamente via `RegisterWithAlias`.
    - **Ação 2 (Persistência e DNS Simplificado):** Atualização do `IdentityRegistry` para persistir os apelidos localmente no PebbleDB sob o prefixo `alias:<address>`. O arquivo `peers.json` foi simplificado para funcionar apenas como DNS (`endereço_soberano -> IP`), em vez de realizar tradução de apelidos.
    - **Ação 3 (Consumo pelo Explorer):** Desenvolvimento de endpoint `/api/identities` no backend em Java para retornar a tabela de endereços e apelidos ativos gravados pelo Archiver. O frontend (`index.html`) foi adaptado para consumir esses dados dinamicamente, eliminando os mapeamentos de nomes estáticos em JavaScript.
2. **Resolução de Parada do Consenso (Out-of-Order Status Guard no DTS):**
    - **Problema:** Ao bootar ou sincronizar nós (ex: Nó E), mensagens concorrentes de presença/status (`Ready: false` do início do startup e `Ready: true` de sincronização concluída) podiam chegar fora de ordem na rede devido a fila assíncrona do DTS. O nó receptor processava a mensagem mais antiga (`Ready: false`) por último, sobrescrevendo o status do par na memória de prontidão e travando o quórum de consenso no watchdog.
    - **Solução:** Implementação de uma salvaguarda (stale status guard) em `handleStatusArrival` ([node.go](file:///c:/Magno/Projetos/jamii/pkg/node/node.go#L731)) que descarta transições de status para `Ready: false` se o peer já estiver registrado como `Ready: true` e a altura reportada no status for menor ou igual à última altura conhecida do peer. Adicionado log detalhado de quórum (`[QUORUM-DIAG]`) para monitoramento de saúde da rede.
3. **Gateway REST de Contratos e Deployer Dinâmico no Block Explorer:**
    - **Contexto:** O explorador de blocos não possuía suporte nativo para implantar (deploy) ou interagir dinamicamente com contratos inteligentes Solidity sem depender do Remix (que é incompatível com as assinaturas PQC exigidas pelo Portão-E da Jamii).
    - **Ação 1 (Esquema de Metadados de Contratos):** Criação da tabela `smart_contracts` no banco de dados do Archiver para rastrear e registrar ABI, bytecode BIN, endereço e apelidos de contratos cadastrados.
    - **Ação 2 (Endpoints de Deploy e Interação REST):** Implementação de rotas em [ApiController.java](file:///c:/Magno/Projetos/jamii/explorer/src/main/java/com/jamii/explorer/ApiController.java) (`/api/contracts/deploy`, `/api/contracts/register`, `/api/contracts/registered` e `/api/contracts/{address}/call`). O deploy calcula deterministicamente o endereço derivado do contrato via Keccak-256 localmente, com base no nonce e no endereço da chave pública pós-quântica do remetente. Integração com o parser e decodificador Web3j para chamadas dinâmicas.
    - **Ação 3 (Interface do Usuário):** Criação da aba "Contratos" em [index.html](file:///c:/Magno/Projetos/jamii/explorer/src/main/resources/static/index.html) permitindo o deploy de contratos (com detecção em tempo real de parâmetros do construtor e criação dinâmica de campos de input) e interação dinâmica (Read/Write) com todas as funções expostas na ABI.

## 🛠️ Decisões Recentes (23/06/2026)
1. **Governança de Chaves no Gênese e Saneamento de Manifestos PQC:**
    - **Contexto:** Antes, as chaves públicas dos validadores eram trocadas dinamicamente via handshakes e/ou gossip de identidades e salvas no banco (sob prefixo `ident:`). Na nova arquitetura, o gossip de identidades e a troca dinâmica via handshake foram removidos. As chaves públicas PQC híbridas passam a ser providas e governadas estritamente a partir do bloco gênese e via eleições.
    - **Problema:** Na carga de novos validadores ou no boot de nós com bancos limpos/novos, a ausência de chaves consistentes no bloco gênese resultava em falha de boot com o erro de criptografia Dilithium `packed public key must be of mldsa65.PublicKeySize bytes` (causado por inconsistência no tamanho das chaves lidas ou nos hexadecimais salvos no arquivo).
    - **Ação 1 (Padronização do Gênese):** Correção e padronização do array `validators` em todos os arquivos `genesis.json` (`node_a` a `node_e`) para usar um formato estruturado contendo `"address"` (Endereço Soberano `jamii1...`) e `"publicKey"` (com o hexadecimal exato de 3984 caracteres representando a chave híbrida Secp256k1 + ML-DSA-65).
    - **Ação 2 (Alinhamento dos Manifestos):** Correção do script utilitário de geração de endereços (`regenerate_manifests.go`) para estruturar a chave pública híbrida no mesmo padrão de 3984 caracteres usado no handshake (prefixado com `02220000000002...`), garantindo consistência com o validador interno e gerando manifestos `node_address.json` alinhados.
2. **Resiliência de Boot e Consensus Unlock (Materialização de Gênese Fallback):**
    - **Problema:** Quando os bancos de dados PebbleDB (`database`) eram limpos ou continham dados de transações/estados mas a chain principal estava sem blocos canônicos indexados (`latest == nil`), o laço de consenso abortava silenciosamente sem reportar status de prontidão (`Ready`), bloqueando a comunicação e travando a rede.
    - **Ação:** Implementado em [node.go](file:///c:/Magno/Projetos/jamii/pkg/node/node.go#L224-L241) um fallback automático no boot. Caso exista uma base de dados PebbleDB aberta, mas o cabeçalho canônico ativo seja nulo (`latest == nil`), o nó materializa localmente o bloco gênese #0 a partir do arquivo de configuração, grava-o no banco de dados e prossegue com a inicialização normal, desbloqueando o consenso.
3. **Controles Específicos do Ambiente Windows (UAC & Terminal CP1252):**
    - **Windows UAC Bypass:** O Windows bloqueia automaticamente a execução sem permissões administrativas de executáveis de linha de comando contendo palavras-chave como `update`, `install` ou `setup` (gerando prompts do UAC). Para evitar interrupções, o utilitário de atualização foi renomeado de `update_node_addresses.go` para `regenerate_manifests.go`.
    - **Encoding CP1252:** Terminais padrão do Windows utilizam codificação CP1252 por padrão. O uso de emojis Unicode de alta resolução nos logs ou console do terminal lançava exceções de encodagem (`UnicodeEncodeError`). Ajustou-se a saída de scripts auxiliares para caracteres ASCII compatíveis.
4. **Execução de Nós em Modo 100% Stateless (RAM Storage):**
    - **Contexto:** Visando descentralização e suporte a nós leves ou temporários, a Jamii introduziu a capacidade de rodar nós sem persistência física em disco.
    - **Ação:** Implementação do suporte à flag `--stateless` e `"stateless": true` no arquivo `config.yaml`. Quando ativa, o nó inicializa um banco de dados em RAM (`store.NewMemoryStore()`) em vez do PebbleDB no disco. Adicionou-se uma validação de segurança fundamental no boot: um nó stateless não pode ser um validador ativo na lista do `genesis.json`. Se o endereço correspondente à chave privada do nó estiver na lista de validadores ativos do gênese, o boot é abortado para preservar a integridade da rede.
5. **Remoção do Gossip Dinâmico de Identidades PQC (MsgIdentities):**
    - **Contexto & Ação:** Com a governança das chaves públicas de validadores agora centralizada no arquivo gênese e controlada via votação (on-chain elections), o envio dinâmico via handshake e o protocolo de gossip P2P (`MsgIdentities`) foram desativados. Isso eliminou um overhead de rede significativo de ~3.9 KB por validador no plano de controle, protegendo a rede contra saturação e pacotes duplicados.

## 🛠️ Decisões Recentes (19/06/2026)
1. **Otimização de Banco de Dados PostgreSQL e Resolução de Delay do Explorer:**
    - **Problema:** A interface do explorador de blocos Java demorava 20 segundos para atualizar após floods de transações, devido à contenção de banco causada por scans sequenciais em consultas frequentes sem índices (como `SELECT COUNT(*) FROM transactions` e ordenação por altura/saldo).
    - **Ação 1 (Adição de Índices):** Adicionados 11 índices secundários na inicialização de esquema do `NewPostgresStore` em [postgres.go](file:///C:/Magno/Projetos/jamii/pkg/store/postgres/postgres.go#L59) para permitir buscas ultra rápidas por hash de bloco, histórico de transações, saldos, contratos e mirror address.
    - **Ação 2 (Remoção de Redundância):** Comentada e anotada a gravação física na tabela espelho `trie_nodes` no `commitBatches` em [postgres.go](file:///C:/Magno/Projetos/jamii/pkg/store/postgres/postgres.go#L496). Como todos os nós históricos da Trie de Estado são preservados na tabela chave-valor `system_kv` (usada exclusivamente para restauração de estado e leituras), a gravação relacional espelho foi desabilitada, economizando cerca de 80% do overhead de escrita do banco.
    - **Ação 3 (Fatiamento de Flat Accounts):** Implementado o fatiamento de escritas de saldo na tabela `account_flat` em chunks de 1.000 registros ([postgres.go](file:///C:/Magno/Projetos/jamii/pkg/store/postgres/postgres.go#L528)) para estabilizar o I/O sob estresse.
2. **Identidade Visual e Tematização Light Premium (Logo e Cores de Marca no Block Explorer):**
    - **Ação:** Integração do logotipo oficial `jamii-logo.png` ([jamii-logo.png](file:///C:/Magno/Projetos/jamii/docs/images/jamii-logo.png)) nos recursos estáticos e reestruturação do design visual do Block Explorer para adotar um tema Light Premium moderno e limpo.
    - **Paleta de Cores Light:** Fundo alterado para cinza/branco ultra-suave (`#f8fafc`), cores de texto escuras de alta legibilidade (`#0f172a` e `#64748b`) e acentuações usando a paleta exata do logotipo: **Azul Real** (`#1d5792`) e **Laranja Vibrante** (`#e27a0b`).
    - **Design Sutil de Cards:** Redesenhados os cards estatísticos, modais de detalhes e tabelas para possuírem bordas finas muito claras (`#e2e8f0`) e sombras extremamente suaves e sutis (`rgba(15, 23, 42, 0.03)`), eliminando o contraste marcado do tema escuro anterior.
    - **Ajustes de Degradê e Inputs:** Barra de busca redesenhada com fundo cinza claro e foco em branco puro com brilho suave. Título "Jamii Scan" atualizado para usar um gradiente nítido de Azul Real para Laranja.
3. **Reestruturação Visual e Arquitetura Frontend do Explorador de Blocos (JamiiScan):**
    - **Tema Claro (Light Theme):** Transição do tema escuro genérico para um tema claro baseado na paleta do logotipo da Jamii (tons pastéis de ciano/azul `#0284c7` e laranja/ouro `#d97706`). Isso resolveu a inconsistência visual do logotipo `jamii-logo.png` (que possui fundo branco) e integrou o design do painel.
    - **Correção de Contraste e Legibilidade:** Resolução de bugs graves de visualização do protótipo onde títulos de blocos, valores de transações e textos digitados na busca ficavam brancos sobre fundo branco/claro.
    - **Navegação com Roteamento Hash (URL Hash Routing):** Implementação de roteamento no lado do cliente (`#/block/{height}`, `#/tx/{hash}`, `#/address/{address}`) em substituição a pop-ups modais. Garante histórico de navegação nativo (botões voltar/avançar do browser), URLs compartilháveis e mais espaço de exibição. Logotipo e botão "Voltar" integrados para limpeza de rota.
    - **Paginação de Transações:** Implementação de paginação client-side (lotes de 25 transações) na tela de detalhes do bloco para otimizar visualização de blocos populosos (como picos de 500+ TXs comuns em redes como Ethereum/BSC) e evitar travamentos de renderização.
    - **Painel Horizontal de Carteiras Recentes:** Criação de um card horizontal responsivo de largura total que lista as últimas 10 contas modificadas no banco de dados (através da coluna `last_update_height` da tabela `account_flat` via endpoint `/api/accounts/recent`), integrado com o polling e detalhamento.
    - **Reordenação e Posicionamento Premium:** O carrossel de carteiras atualizadas foi extraído do grid principal de duas colunas `.explorer-main` e reposicionado como uma faixa de largura total independente logo abaixo do barramento de estatísticas principais (`.stats-grid`), melhorando a harmonia estética e dando maior destaque visual ao elemento.
    - **Correção de Clipping do Hover (CSS):** Ajustado o container `.horizontal-list` adicionando `padding-top: 6px;`. Isso impede que a borda superior dos cards de carteiras seja recortada visualmente pelo overflow-x do container durante o deslocamento vertical da animação (`transform: translateY(-2px);`).
    - **Otimização de Chamadas de Rede:** Programado controle para pausar requisições em segundo plano do painel principal enquanto telas de detalhes estiverem abertas.
    - **Saneamento de Vocabulário:** Ajuste da tradução do cabeçalho de "Altura Canonical" para o termo técnico formal em português **"Altura Canônica"**.
4. **Calibração de Throughput e Dimensionamento de `maxTxsPerBlock`:**
    - **Métricas de Comparação:** Em blockchains de produção sob tráfego estável, a média de transações por bloco gira em torno de 150 a 300 transações no Ethereum e na BNB Smart Chain. Em picos de estresse, no entanto, a BSC é capaz de comportar mais de 10.000 transações em um único bloco (com tempo de bloco curto de 3s, o que atinge picos de TPS de ~3.300+).
    - **Dimensionamento e Limites locais:** O valor de `maxTxsPerBlock = 3000` na Jamii Blockchain foi avaliado como ideal para alto rendimento sob produção industrial. No entanto, para testes e simulações locais com múltiplos nós (ex: 5 validadores) compartilhando recursos físicos de processador e escrita em PebbleDB, blocos de 3.000 transações (com criptografia pós-quântica ML-DSA) geram contenção excessiva de CPU e disco, resultando em timeouts de rodada (round change) no IBFT2.
    - **Diretriz Técnica de Testes:** Calibrar o limite de `maxTxsPerBlock` em `1000` a `1500` para testes locais é o recomendado para assegurar a fluidez e estabilidade do consenso sob floods massivos de transações.
5. **Tematização Light Premium da Wallet de Exemplos (Java SDK):**
    - **Ação:** Aplicação do mesmo esquema de cores da marca (Azul Real `#1d5792`, Laranja `#e27a0b` e Azul Ciano `#0284c7`) no arquivo estático [index.html](file:///C:/Magno/Projetos/jamii/sdk/examples/java/src/main/resources/static/index.html) da carteira de testes do SDK.
    - **Logotipo e Header:** Cópia e vinculação do logotipo oficial `jamii-logo.png` na barra de cabeçalho da carteira, com o título do app usando o mesmo gradiente visual do explorador de blocos.
    - **Ajuste de Componentes:** Adaptação de todos os inputs, badges de status, botões (gradientes do Azul Real e Laranja), overlays, e animações (spinner de carregamento) para o tema claro sobre background `#f8fafc`.
6. **Integração de Carteira Jamii no Explorador de Blocos (Jamii Scan):**
    - **Ação 1 (Backend - ApiController):** Implementação dos endpoints `/api/wallet/login`, `/api/wallet/logout`, `/api/wallet/info` e `/api/wallet/transfer` sob gerenciamento seguro de sessão do Spring Boot (`HttpSession`). A chave privada (`JamiiKeyPair`) é descriptografada em memória usando o SDK Java da Jamii e armazenada temporariamente na sessão volátil da RAM, sendo destruída no logout ou no timeout da sessão.
    - **Ação 2 (Frontend - index.html):** Inserção de um botão de acesso no cabeçalho e desenvolvimento de uma view completa (`#wallet-view`) associada ao roteamento de hash (`#/wallet`).
    - **Asegurança & Envio (Ação 3):** Implementação de uma área de arrastar e soltar (drag & drop) para arquivos de Keystore JSON, toggle de visibilidade de senha, cálculo preciso de taxas de gás com base no base fee atual da rede usando BigInt e modal moderno de feedback com links diretos para o recibo da transação gerada.
    - **Ação 4 (Parametrização do JSON-RPC):** Externalização da URL de conexão com o nó JSON-RPC do blockchain (anteriormente fixada em localhost) para a propriedade `jamii.jsonrpc.url` em `application.properties`, injetada via `@Value` no Spring Boot.
7. **Suporte Nativo a Mnemônicos (BIP-39/BIP-32) e Alinhamento Criptográfico Determinístico:**
    - **Integração no SDK Java:** Adição do suporte a mnemônicos (12 palavras de recuperação) e derivação de chaves híbridas Secp256k1 (caminho `m/44'/60'/0'/0/0`) e ML-DSA-65 (via `FixedSecureRandom` alimentado com o HMAC-SHA512 da seed).
    - **Correção no Go `wallet-cli`:** Correção dos erros de compilação em `cmd/wallet-cli/main.go` ajustando o tipo de entrada do mnemônico para `[]byte` e substituindo a dependência do campo `.ID` removido do Keystore por um substring do Mirror Address.
    - **Garantia de Simetria (Teste Unitário):** Implementação de uma suíte de testes JUnit (`JamiiWalletTest.java`) contendo um caso de teste baseado em vetores gerados pelo Go CLI. A execução bem-sucedida do teste comprovou o determinismo absoluto na derivação de endereços Sovereign/Mirror em ambas as linguagens.
    - **Frontend do Explorer:** Atualização do painel em `index.html` para expor abas de Login com Keystore, Recuperação com Mnemônico e Geração de Nova Carteira, integrando downloads automáticos de arquivos keystore JSON e a exibição do **Endereço Espelho (Mirror Ethereum)** em todos os fluxos de sucesso e no painel principal da carteira.
8. **Resiliência a Falhas do Archiver e Robustez no Explorer de Blocos:**
    - **Problema:** Quando o archiver de dados era temporariamente desligado ou atrasava, novas transações processadas com sucesso pela blockchain não constavam no PostgreSQL, fazendo com que a exibição de detalhes no explorador falhasse de forma silenciosa ou técnica com placeholders inválidos (`undefined`, `NaN`).
    - **Ação 1 (Fallback Dinâmico no Java Backend):** Implementação de fallback no Spring Boot ([ApiController.java](file:///C:/Magno/Projetos/jamii/explorer/src/main/java/com/jamii/explorer/ApiController.java#L210)) que, ao não encontrar a transação ou recibo no PostgreSQL, realiza uma chamada JSON-RPC direta ao nó ativo da blockchain (`getTransactionReceipt`).
    - **Ação 2 (Robustez UI):** Atualização do javascript no frontend ([index.html](file:///C:/Magno/Projetos/jamii/explorer/src/main/resources/static/index.html)) para tratar valores nulos e estruturar a renderização dinâmica de dados do recibo de forma limpa, ocultando dados técnicos de erro.
9. **Janela Deslizante de Sementes BitTorrent (Prevenção contra History Bloat nos Validadores):**
    - **Problema:** O seeding persistente de blocos antigos por torrent forçava os nós validadores ativos a armazenarem eternamente grandes volumes de arquivos físicos de blocos antigos (`chunk_*_*.bin`), causando consumo descontrolado de disco e conexões na rede P2P.
    - **Solução:** Implementação de uma política de janela deslizante (Sliding Window Seeding) no motor torrent em Go ([engine.go](file:///C:/Magno/Projetos/jamii/pkg/torrent/engine.go)). A janela móvel monitora os torrents ativos de blocos semeados e remove handles e arquivos físicos mais antigos que o limite configurado (janela de no máximo 2 chunks simultâneos), preservando o espaço em disco e mantendo a missão dos validadores atômica e limpa.
10. **Mapeamento de Endereços Híbridos no Explorer e Sincronização Retroativa de Contas:**
    - **Problema:** A tabela `account_flat` do explorer mantinha saldos e nonces corretos alimentados pelo Commit do Trie de Estado (usando o `address_hash` keccak de 20 bytes), mas os campos textuais `address` and `mirror_address` permaneciam `NULL` para qualquer nova conta criada pós-Genesis, gerando cards corrompidos com endereços nulos. No bloco #1617, uma transferência para o endereço Ethereum do usuário gerou uma conta de 506 JAMII com endereço `null`.
    - **Ação 1 (Registro Ativo no Commit):** Ajustamos o `commitBatches` em [postgres.go](file:///C:/Magno/Projetos/jamii/pkg/store/postgres/postgres.go#L390) para extrair e registrar ativamente os endereços válidos (`sender`, `receiver` e `coinbase`) de cada lote de transações e blocos gravados no Postgres.
    - **Ação 2 (Robustez no Conflito do Address Book):** Atualizada a cláusula `ON CONFLICT (address_hash)` do método `RegisterAddress` em [postgres.go](file:///C:/Magno/Projetos/jamii/pkg/store/postgres/postgres.go#L623) para atualizar também a coluna `mirror_address` em caso de conflito, garantindo que contas criadas preliminarmente pela Trie com campos nulos ganhem as duas representações de endereço.
    - **Ação 3 (Sincronização Retroativa):** Escrito e executado um script Go utilitário (`migrate_addresses.go`) que varreu todo o histórico de blocos e transações no PostgreSQL e sincronizou retroativamente os 9 endereços únicos no banco de dados, recuperando o endereço textual `0xd0438D4539867cC3b58f0ce6824bEe58787c70Bd` da carteira do usuário de forma 100% precisa.
11. **Modal de Confirmação de Transação Customizado no Explorer (UX Premium):**
    - **Problema:** A caixa de confirmação de transações no explorador utilizava o método `confirm()` nativo do navegador, destoando do design visual e de cores Light Premium unificado da Jamii Blockchain.
    - **Ação 1 (Interface HTML/CSS):** Inserida a marcação `#tx-confirm-overlay` no arquivo [index.html](file:///C:/Magno/Projetos/jamii/explorer/src/main/resources/static/index.html#L1210) reaproveitando o layout e os estilos suaves do modal de status.
    - **Ação 2 (Promisificação JavaScript):** Implementada a função `showTxConfirmModal(to, valWei)` que envelopa a interação do modal em uma `Promise<boolean>` para suporte nativo e limpo a `await`.
    - **Ação 3 (Acoplamento de Fluxo):** Substituída a chamada síncrona `confirm()` na rotina `submitWalletTransfer` pelo novo confirmador assíncrono.
12. **Otimização de Performance e Resolução de Gargalo de Escrita (Flushing de WAL) no Archiver:**
    - **Problema:** Sob floods de transações massivas (ex: 5.000 transações enviadas em blocos populosos), o archiver começou a empilhar dezenas de arquivos de Write-Ahead Log (`.wal`) no disco e parou de atualizar o explorador por mais de 10 minutos. Isso foi causado pela introdução de chamadas síncronas individuais a `p.RegisterAddress(addr)` (que executa `db.Exec` fora da transação SQL do lote) para cada Coinbase, Sender e Receiver processados.
    - **Ação 1 (Acúmulo em RAM):** Substituído o registro síncrono individual por acúmulo temporário dos endereços únicos decodificados em um mapa `regAddrs` no início do `commitBatches` em [postgres.go](file:///C:/Magno/Projetos/jamii/pkg/store/postgres/postgres.go#L176).
    - **Ação 2 (Batching com Unnest na Transação):** Implementada a persistência de novos endereços em lote (batching) usando `unnest` e arrays parametrizados diretamente no objeto da transação SQL `tx` ativa antes do commit. Isso reduziu drasticamente o overhead de I/O de rede e banco de dados de milhares de requisições separadas para apenas 1 a 5 queries consolidadas por lote, normalizando o flushing dos arquivos WAL de volta a milissegundos e atualizando o explorador instantaneamente.
13. **Filtro de Contas com Saldo Nulo na Rich List do Explorer:**
    - **Problema:** Na lista de contas mais ricas do explorador, apareciam contas com `0 JAMII` e nonce `null` no início da lista, apontando para endereços com valores nulos. Isso era causado pelo fato de que o PostgreSQL ordena valores `NULL` no topo da lista em queries decrescentes por padrão (`ORDER BY balance DESC`), misturando contas que foram apenas mapeadas via `RegisterAddress` (com campos de saldo nulos) com as contas de saldo real.
    - **Ação:** Ajustada a query SQL no endpoint `/accounts/top` em [ApiController.java](file:///C:/Magno/Projetos/jamii/explorer/src/main/java/com/jamii/explorer/ApiController.java#L277) adicionando a cláusula `WHERE balance IS NOT NULL AND balance > 0`. Isso garante que apenas contas com saldos reais e positivos apareçam na Rich List, tirando proveito dos índices de balance e corrigindo a exibição na interface.

## 🛠️ Decisões Recentes (18/06/2026)
1. **Resolução de Iterador PostgreSQL (`PostgresIterator` Funcional):**
    - **Ação:** Implementação do iterador funcional do driver de banco de dados `PostgresStore` (em [postgres.go](file:///C:/Magno/Projetos/jamii/pkg/store/postgres/postgres.go#L453)) para processar chaves com prefixo `ident:`, restabelecendo a propagação dinâmica do cartório de identidades do Archiver para novos nós no handshake.
2. **Logs Transparentes com Identificação de Remetente (`[ FROM PEER ]` no DTS):**
    - **Ação:** Ajuste na exibição de mensagens no console de rede do `DTS Engine` em [engine.go](file:///C:/Magno/Projetos/jamii/pkg/dts/engine.go) para incluir explicitamente a origem das mensagens recebidas e das identidades propagadas de validadores (ex: `[ FROM ARCHIVER ] Receiving Public Key for ...`), eliminando poluição visual sem adicionar overhead.
3. **Otimização Concorrente de Seeder Virtual e Cache de RAM (N+1 Query Resolution):**
    - **Problema:** A criação do seeder virtual para sincronizar um chunk de blocos parcial (ex: 1 a 327 blocos) demorava mais de 3 minutos devido à latência de rede em queries relacionais sequenciais repetidas de montagem de blocos e marshal na leitura stateless do Torrent.
    - **Solução:** Otimização do método `buildBlockIndex` em [storage.go](file:///C:/Magno/Projetos/jamii/pkg/torrent/storage.go#L139) para disparar queries assíncronas concorrentes de busca de blocos (através de um worker pool de concorrência limitada a 20 goroutines) e realizar o caching dos dados serializados dos blocos em memória RAM. Redução de latência de inicialização de seeding para menos de 1 segundo.
4. **Resiliência e Desalocação de Disco (File Handle Release pós-Sync):**
    - **Problema:** Os arquivos temporários binários parciais (`temp_chunk_*.bin`) gerados pelo downloader torrent dos proposers não eram apagados no final da sincronização de blocos no Windows, correndo risco de exaustão de espaço em disco em sincronizações massivas.
    - **Solução:** Inclusão de chamada mestre `t.Drop()` em `DownloadChunk` ([engine.go](file:///C:/Magno/Projetos/jamii/pkg/torrent/engine.go#L324)) imediatamente após o download atingir 100%, liberando o travamento do handle de escrita do torrent e permitindo a deleção do arquivo físico no disco local via `os.Remove`.

## 🛠️ Decisões Recentes (17/06/2026)
1. **Resolução de Sincronia de Consenso sob ML-DSA PQC (Handshake de Chaves Públicas Dinâmico):**
    - **Problema:** Sob assinaturas pós-quânticas ML-DSA, a chave pública do validador não pode ser recuperada matematicamente a partir da assinatura do bloco (`ecrecover`). Isso impedia a validação de blocos históricos recebidos por Torrent se o nó de destino (zerado/recém-entrado) não possuísse as chaves dos validadores em seu cartório PebbleDB.
    - **Solução:** Registro de um novo tipo de mensagem `MsgIdentities` (`0x09`) no DTS. Transmissão imediata de todo o cartório de identidades (chaves públicas e nomes) via canal `BULK` no handshake inicial do DTS, populando o PebbleDB local do nó receptor antes da validação da cadeia de blocos de torrent.
    - **Higiene e Logs:** Adicionado log informativo no nível `INFO`: `Receiving Public Key for <NODE_NAME>` para fins de visibilidade visual no bootloader do nó.
2. **Exposição de Identidades no PostgreSQL (Archiver Sync Relacional):**
    - **Ação:** Criação da tabela `identities` dedicada no PostgreSQL do Archiver (definida tanto em `cmd/archiver/schema.sql` quanto no construtor programático `NewPostgresStore`).
    - **Gravação:** Atualização do processador de lotes `commitBatches` no `postgres.go` para filtrar e gravar dados com prefixo `"ident:"` do PebbleDB e persistir chaves públicas e metadados no banco relacional em tempo real.
3. **Otimização de Gargalo no Torrent Sync Engine (Single Flight Coalescing & Memory Locks):**
    - **Problema:** Concorrência pesada por InfoHash do Torrent gerava requisições repetidas e recálculo lento de SHA-1 de peças a cada 10 segundos no disco (PostgreSQL lento), travando o mutex principal do TorrentEngine e criando loops de espera concorrentes.
    - **Solução:** Implementação de *Single Flight Request Coalescing* e cache estático de InfoHashes no motor de Torrent. Liberação precoce do mutex durante operações lentas de I/O de dados de chunk e cálculo de hash.
    - **Sync Manager Reativo:** Remoção do loop periódico de solicitação do Torrent InfoHash por um loop puramente reativo (Fire & Forget). Sincronismo de cadeia acionado imediatamente por recepção de status do peer em background no milissegundo de chegada via `UpdatePeerHeight`.
4. **Resiliência e Conexão Simétrica do DTS Handshake:**
    - **Problema:** Race condition entre os canais `EXPRESS` (TCP rápido) e `BULK` (TCP levemente mais lento por latência de socket) causava o descarte silencioso do `BroadcastStatus` inicial de presença.
    - **Solução:** Ajustado o `dts.Engine` para disparar `onPeerConnect` no canal `BULK` caso o `EXPRESS` já esteja ativo, garantindo redundância, e adicionando fallback de transmissão no canal `EXPRESS` se o `BULK` estiver nulo temporariamente.
5. **Correção no Iterator PebbleDB de Identidades:**
    - **Problema:** O iterator de busca do PebbleDB ao fazer o bootstrap de identidades no `pkg/node/node.go` ignorava o primeiro registro encontrado no `Seek` inicial, causando perda da primeira chave inserida em execuções subsequentes.
    - **Solução:** Correção do loop iterator para processar a chave do seek inicial e, em seguida, iterar com `Next()`.
6. **Watchdog de Proposer Não-Pronto (Early Leader Change):**
    - **Ação:** Adicionada validação de prontidão (`IsProposerReady`) ao Watchdog de rodada (IBFT). Se o propositor selecionado for detectado como "not ready" (sincronizando) ou offline, o nó dispara imediatamente a troca de líder (`onTimeout("proposer offline/unready")`) após o grace period de 2 segundos, em vez de aguardar o timeout mestre de 10s.
    - **Inicialização de Startup:** Inicialização explícita de todos os validadores da rede como `false` ('not ready') no mapa de controle `peerReady` no boot do nó.
    - **Simplificação de Logs de Quórum:** Remoção de logs debug detalhados de votos de commit individuais (`voted COMMIT. Count: X/Y`) para evitar poluição visual e simplificação do log de quórum de commit para `[Round <H:R>] Quorum to COMMIT reached.` para melhor compatibilidade com grandes conjuntos de validadores (100+).
7. **Correção de Panics no BitTorrent Sync (Data Plane):**
    - **Problema:** O método `DownloadChunk` invocava `t.DownloadAll()` antes de verificar `<-t.GotInfo()`, resultando em panic por desreferência de ponteiro nulo (`t.info == nil`) quando o InfoHash do torrent ainda não havia resolvido seus metadados no enxame.
    - **Solução:** Adicionado bloqueio preventivo via select `<-t.GotInfo()` antes de chamar `t.DownloadAll()`, garantindo que os metadados do torrent estejam totalmente carregados.
8. **Mapeamento Amigável nos Logs de Sincronismo (Friendly P2P Sync Logs):**
    - **Ação:** Adicionado método público `PeerName(id)` ao `dts.Engine` e helper `peerName(id)` no `SyncManager` para exibir nomes lógicos (ex: `NODE_A`) nos logs de sync em vez de IDs Bech32 brutos.
9. **Higiene de CPU e Mempool em Estado de Sync (Observer Mode Filtering):**
    - **Ação:** Implementada filtragem estrita no `handleExpressArrival` para descartar mensagens de selos de validadores (`dts.MT_VALIDATOR_SEAL`) e no `handleDataArrival` para descartar transações da mempool (`dts.MT_TRANSACTION`) enquanto o nó estiver em modo de sincronismo inicial (`IsBehind() == true`).
    - **Benefício:** Redução maciça no uso de CPU e proteção contra estouro/poluição de RAM na mempool sob inundações de rede (floods).
10. **Estratégia de Requisição de InfoHash Distribuída (Torrent Unicast to Broadcast):**
    - **Problema:** O nó iniciador do torrent solicitava o InfoHash do chunk via `RequestTorrentChunk` em unicast para apenas um único peer (o `bestPeer` principal). Isso causava travamentos se o peer alvo (ex: o Archiver) estivesse lento para gerar e expor o arquivo virtual, impedindo que outros nós que já continham os mesmos blocos em seu disco local rápido (PebbleDB) respondessem.
    - **Solução:** Alterado o `SyncManager` para enviar a solicitação do InfoHash para todos os nós conectados que possuam altura de bloco maior ou igual ao fim do chunk solicitado. A primeira resposta destrava o download via BitTorrent, que baixa as peças de todo o enxame em paralelo.

## 🛠️ Decisões Recentes (12/06/2026)
1. **Desacoplamento e Biblioteca Pura (SDK Java)**:
    - **Ação:** Refatoração completa do módulo `sdk/java/jamii-sdk` para atuar como uma biblioteca Java pura e autônoma, livre de dependências do Spring Boot e de servidores web embutidos.
    - **Implementação:** Substituição do `RestTemplate` por `java.net.http.HttpClient` nativo do Java 22. Adição do script `deploy_local.bat` para automatizar a compilação e publicação no diretório local `.m2/repository`.
2. **Criação da Wallet Web de Exemplo (Java Web)**:
    - **Ação:** Criação do módulo de exemplo em `sdk/examples/java` consumindo o SDK como dependência local externa do Maven.
    - **Arquitetura & Segurança:** Criação do arquivo `application.properties` para configuração dinâmica de Keystores, eliminando caminhos hardcoded e cortando qualquer execução ou deploy automático no boot para evitar falhas silenciosas na ausência de nós locais da rede blockchain.
    - **API e Assinatura Local:** Implementação do endpoint GET `/api/wallet` e do endpoint POST `/api/transfer` no `ApiController.java`. A assinatura da transação ocorre localmente usando a chave privada da carteira através do SDK, transmitindo o payload serializado em SSZ de forma segura via chamada RPC ao nó configurado.
    - **Interface Premium:** Desenvolvimento de uma UI interativa em estilo *glassmorphism* (HTML/CSS/JS) rodando na porta `8080` com suporte a injeção e teste dinâmico de host/porta RPC persistido via `localStorage` do navegador, com feedback em tempo real para transações e testes de conexão.
    - **Ferramental:** Criação do script `run.bat` para compilar e inicializar o servidor de exemplo.
3. **Terminologia de Estado (Verkle Default)**:
    - **Ação:** Substituição sistemática das referências exclusivas a "SMT" (Sparse Merkle Trie) nos manuais e documentação técnica pelo termo geral "Trie de Estado" ou "Trie", refletindo o suporte híbrido (Verkle/SMT) introduzido pela Trie Factory e confirmando o Verkle como padrão de árvore de estado na arquitetura Jamii.
4. **Otimização de DTS e SyncManager (Resiliência sob Flood)**:
    - **DTS Dense Payload Resiliency (Desconexão Concorrente por Limite de Buffer e Timeout):**
        - **Problema:** O método `readMessage` em `pkg/dts/network_proto.go` possuía um limite máximo fixo de tamanho de mensagem de 10 MB. Como cada transação pós-quântica (PQC/ML-DSA) consome cerca de 4.3 KB, um bloco denso contendo 3.000 transações atinge aproximadamente 12.9 MB. Ao tentar ler esse payload, o nó receptor barrava a mensagem com o erro `message too large` e fechava a conexão, fazendo com que o remetente recebesse um erro de socket fechado abruptamente (`wsasend`).
        - **Solução:** Elevação do limite de segurança de mensagens no DTS de 10 MB para 32 MB e aumento do `WriteDeadline` de 10s para 30s em `pkg/dts/engine.go` para suportar a latência de rede/CPU sob alta carga.
    - **Anxious Sync Debounce (Sync Ansiado por Falta de Tolerância de Commit):**
        - **Problema:** O `SyncManager` roda um loop periódico que ativa a sincronização se encontrar a altura local menor que a altura máxima dos peers. Como o commit de Verkle/PebbleDB de blocos de 3.000 TXs leva até ~1.5s, o nó local fica em um estado temporário em que a RAM já finalizou a rodada do consenso, mas o disco ainda registra a altura anterior ($N-1$).
        - **Solução:** Implementação de um debounce de 500ms em `pkg/blockchain/sync.go` no início de `checkAndSync` caso a diferença seja de exatamente 1 bloco, liberando o mutex, esperando e re-checando a altura local antes de disparar requisições de rede.
5. **Calibração de Cadência de Consenso (blockPeriod Tuning)**:
    - **Diagnóstico Local:** Análise matemática do teste de flood de 15K revelou vazão líquida de execução da VM de **~1.250 TPS**, mas o TPS médio geral de rede ficou em **625 TPS** devido a 12s de ócio acumulados (pacing do consenso).
    - **Contenção Multiprocesso:** Identificado que em setups locais rodando 5 validadores na mesma máquina Windows, a concorrência por disco (5 escritas PebbleDB simultâneas) e CPU (15.000 validações ML-DSA concorrentes) eleva o tempo de commit de blocos cheios para até 4s (bloco #18).
    - **Tuning Recomendado:** O `blockPeriod` de 2s é mantido como amortecedor de segurança em setups locais concorrentes. Para reduzir a cadência com segurança para 1s (elevando o TPS médio para ~900-1000 TPS), é mandatória a redução de `maxTxsPerBlock` para 1.000 TXs (evitando lags acumulados de rodada) ou migração para nós com hardware dedicado.

## 🛠️ Decisões Recentes (11/06/2026)
1. **Transição de Assistência e CLI:**
    - **Ação:** Oficializada a transição das ferramentas de assistência e suporte ao desenvolvimento da Jamii Blockchain, migrando do antigo `gemini-cli` para o novo assistente autônomo `antigravity-cli` (Antigravity).
    - **Impacto:** Todas as diretivas de engenharia do [GEMINI.md](file:///C:/Magno/Projetos/jamii/GEMINI.md) e o histórico consolidado continuam 100% vigentes e sob custódia e aplicação rígida pelo novo assistente.

2. **Humano-Compatibilidade de Logs (Friendly Peer Names & Clean Logs):**
    - **Ação:** Implementação de resolução reversa de nomes lógicos no console de depuração e visualização do nó (ex: `NODE_A`, `NODE_B`) com base no `peers.json` do ambiente.
    - **Resultado:** Os logs de canais de conexão do DTS, logs de status do nó, logs de verificação de assinaturas criptográficas PQC (ML-DSA) e logs do propositor designado/watchdog do consenso agora exibem o nome lógico do validador in vez do hash Bech32 bruto.
    - **Higiene e Otimização de Logs:** Remoção do log redundante de chegada de sinal de consenso (`Consensus Signal`), rebaixamento do log de tráfego de dados expressos para o nível `TRACE` (limpando o console de depuração padrão) e elevação dos logs de quórum de `PREPARE` (Prepared state reached) e `COMMIT` (voted COMMIT. Quorum reached) para o nível `INFO` (dando visibilidade do consenso em produção).
    - **Design Concorrente Seguro:** A resolução é estritamente de leitura (Read-Only) em tempo de execução (mapa populado síncrono no boot), eliminando overheads de sincronização e deadlocks.

3. **Resiliência do Sync (Debounce de Catch-up):**
    - **Ação:** Planejada a adição de uma tolerância temporal (debounce configurável de ~200ms) antes que o SyncManager inicie o processo de catch-up de blocos.
    - **Objetivo:** Evitar que o SyncManager compita com o banco de dados PebbleDB e envie requisições DTS extras se o nó estiver apenas finalizando ativamente o bloco corrente via consenso.

## 🛠️ Decisões Recentes (10/06/2026)
1. **Triunfo Arquitetural: Chained State Oracle (Especulação Ativa):**
    - **Contexto:** Implementação de pipelining assíncrono para processar o bloco $N+1$ enquanto a rede aguarda a cadência (Pacing) do bloco $N$.
    - **Resultado:** **Sucesso Absoluto.** A especulação agora é integrada ao fluxo real de proposta.
    - **Ganho de Performance:** O tempo de montagem de bloco pelo líder caiu de ~1.3s para **zero milissegundos** (O(1)).
    - **Recorde de Throughput:** Atingido marco de **750 TX/s** sustentados (3.000 TX/bloco a cada 4s reais), com pico de **1.045 TX/s**.
    - **Espera Inteligente:** Implementado mecanismo de sincronização onde o Proposer aguarda a conclusão da especulação em andamento, eliminando a re-execução redundante e a asfixia de CPU.
    - **Fidelidade Determinística:** Trava mandatória que obriga o propositor a usar a mesma "fotografia" da MemPool tirada na especulação, garantindo 100% de convergência de StateRoot.

2. **Otimização de Retenção (Warm Parent Cache):**
    - **Ação:** O bloco pai agora é mantido em RAM no `PayloadPool` após a finalização.
    - **Benefício:** Elimina a necessidade de I/O de disco para iniciar a especulação do bloco seguinte.

3. **Correção de Persistência (State Promotion Receipts):**
    - **Ação:** Sincronizada a coleta de recibos durante a fase de `VerifyPayload`.
    - **Resultado:** Sanado o panic de disparidade de recibos (`receipt count mismatch`) no banco de dados.

## 🛠️ Decisões Recentes (06/06/2026)
1. **Witness-Aided Quorum Acceleration (Aceleração por Testemunha):**
    - **Ação:** Introdução da **Block Witness** no Skeleton do bloco. O Proposer agora envia os estados iniciais (contas/slots) necessários para a execução.
    - **Benefício:** Permite que validadores executem o bloco de forma "Stateless" e otimista.
    - **Resultado:** Validação de blocos em validadores agora pula a matemática pesada de Verkle (IPA/MSM) durante a fase de votação (Prepare/Commit).

2. **Mecanismo de Promoção de Estado (State Promotion):**
    - **Problema:** O Proposer executava o bloco duas vezes (uma para propor, outra no commit final), duplicando o esforço de CPU-bound mais caro.
    - **Solução:** Implementado o cache de **Sandbox State** no `PayloadPool`. O resultado da execução é preservado e promovido atomicamente.
    - **Resultado:** Redução de **~90%** no tempo de processamento do Proposer na fase de commit.

## 🛠️ Decisões Recentes (03/06/2026)
1. **Refatoração do Motor Verkle (Deadlock & Atomicidade):**
    - **Problema:** Detectado Deadlock Recursivo durante a materialização do Genesis.
    - **Solução:** Implementado sistema de **Cache Sharding Antecipado** (Eager Loading) no construtor da Verkle Tree.
    - **Segurança Atômica:** A variável `cacheMultiplier` foi migrada para o tipo `atomic.Uint64`.
2. **Blindagem de Memória e Slots (Mempool Resilience V1):**
    - **Ação:** Implementada proteção de "Portão Duplo" na MemPool (Slots e RAM).
    - **Lógica de Expulsão Híbrida:** O nó agora realiza expulsão em loop (AllHeap) priorizando transações de maior valor econômico.
1. **Ultra-Compactação de Rede (Bi-Polar Short IDs):**
    - **Ação:** Skeleton Blocks usando identificadores de 6 bytes.
    - **Resultado:** Redução de **81%** no tráfego de rede.
2. **Otimização Homomórfica O(1) (Verkle Turbo):**
    - **Ação:** Implementado o cálculo incremental de compromissos IPA.
    - **Resultado:** O pico do motor atingiu **750 TX/s**.
2. **Resiliência de Ressurreição (Consensus Halt Guard):**
    - **Ação:** Implementado reset automático da trava `lastStartedHeight` quando o quórum de rede é recuperado.

## 🛠️ Decisões Recentes (03/06/2026)
1. **Blindagem de Memória e Slots (Mempool Resilience V1):**
    - **Ação:** Implementada proteção de "Portão Duplo" na MemPool, limitando tanto a quantidade de transações (`MaxMempoolSlotSize`) quanto o consumo real de RAM (`MaxMempoolMemorySize`).
    - **Lógica de Expulsão Híbrida:** O nó agora realiza expulsão em loop (AllHeap) até que ambos os critérios de saúde sejam restabelecidos, priorizando transações de maior valor econômico.
    - **Anti-OOM:** Adicionada validação *check-first* que rejeita transações individuais que sozinhas excedam o limite total de memória da pool.
    - **Resultado:** Garantia de que o nó Jamii permaneça operacional sob ataques de flood massivo, com consumo de RAM previsível e proteção contra exaustão de CPU por validações inúteis.

## 🛠️ Decisões Recentes (03/06/2026)
1. **Refatoração do Motor Verkle (Deadlock & Atomicidade):**
    - **Problema:** Detectado Deadlock Recursivo durante a materialização do Genesis. O motor tentava adquirir um lock de leitura (`RLock`) para ler configurações enquanto já segurava o lock mestre de escrita (`Lock`).
    - **Solução:** Implementado sistema de **Cache Sharding Antecipado** (Eager Loading) no construtor da Verkle Tree. Removida a necessidade de travas (`v.mu`) para leitura de configurações.
    - **Segurança Atômica:** A variável `cacheMultiplier` foi migrada para o tipo `atomic.Uint64` (IEEE 754 bitcasting), permitindo que o cache seja redimensionado em tempo real sem causar contenção ou deadlocks no pipeline de execução.
    - **Performance:** Restaurado o paralelismo multi-core total para o cálculo de compromissos IPA/Bandersnatch.

2. **Blindagem de Memória e Slots (Mempool Resilience V1):**
    - **Ação:** Implementada proteção de "Portão Duplo" na MemPool, limitando tanto a quantidade de transações (`MaxMempoolSlotSize`) quanto o consumo real de RAM (`MaxMempoolMemorySize`).
    - **Lógica de Expulsão Híbrida:** O nó agora realiza expulsão em loop (AllHeap) até que ambos os critérios de saúde sejam restabelecidos, priorizando transações de maior valor econômico.
    - **Anti-OOM:** Adicionada validação *check-first* que rejeita transações individuais que sozinhas excedam o limite total de memória da pool.
    - **Resultado:** Garantia de que o nó Jamii permaneça operacional sob ataques de flood massivo, com consumo de RAM previsível e proteção contra exaustão de CPU por validações inúteis.

## 🛠️ Decisões Recentes (28/05/2026)
1. **Ultra-Compactação de Rede (Bi-Polar Short IDs):**
    - **Ação:** Migração dos Skeleton Blocks para serialização binária usando identificadores de 6 bytes (3 iniciais + 3 finais do TxHash) no lugar de hashes de 32 bytes.
    - **Motivo:** Eliminar o overhead de banda em blocos grandes (1.5k TXs) que geravam pacotes de rede pesados (>48KB).
    - **Resultado:** Redução de **81%** no tráfego de rede. Um bloco com 1.000 TXs caiu de ~32KB para **6.2KB**.
    - **Sucesso de Reconstrução:** Validada reconstrução "stateless" em flood de 10.000 TXs com 0 colisões detectadas.
2. **Otimização Homomórfica O(1) (Verkle Turbo):**
    - **Ação:** Implementado o cálculo incremental de compromissos IPA ($C' = C + \sum \Delta \times G_i$) no motor Verkle.
    - **Motivo:** Eliminar o custo $O(256)$ de re-computação completa de nós da árvore a cada mutação de estado.
    - **Resultado:** A finalização de blocos pesados (1.5k TXs) caiu de ~5s para <2s. O pico do motor atingiu **750 TX/s**.
2. **Baseline de Performance Industrial (Verkle Stress Test):**
    - **Resultado:** **484 AvgTPS** sustentados em teste de estresse de longa duração (2 min).
    - **Métricas:** 58.200 updates de estado processados com pico de Heap em 907MB.
    - **Estabilidade:** 0 GCs/s durante o pico, demonstrando eficiência da arquitetura Zero-Alloc na Verkle Tree.
3. **Resiliência de Ressurreição (Consensus Halt Guard):**
    - **Ação:** Implementado reset automático da trava `lastStartedHeight` quando o quórum de rede é perdido e recuperado.
    - **Impacto:** Fim do travamento manual necessário para "acordar" nós que ficaram isolados ou caíram por tempo prolongado.

## 🛠️ Decisões Recentes (22/05/2026)
1. **Sincronização Atômica Sync-Consensus (Anti-Self-Sabotage):**
    - **Ação:** Refatorado o `SyncManager` para callback sincronizado sob o mutex global `execMu`.
2. **Higiene de Execução (Early StateRoot Validation):**
    - **Ação:** Validação do `IntermediateRoot` antes de chamar `st.Commit()`.
3. **PQC Performance Milestone (The Tsunami Test 10K):**
    - **Resultado:** Processadas **10.000 TXs ML-DSA** com **238 AvgTPS de execução** reais (finalização em disco).

## 🛠️ Decisões Recentes (22/05/2026)
1. **Sincronização Atômica Sync-Consensus (Anti-Self-Sabotage):**
    - **Ação:** Refatorado o `SyncManager` para utilizar um callback sincronizado (`ExecuteBlockFn`) gerenciado pelo `Node` sob o mutex global `execMu`.
    - **Guarda de Altura:** Implementada verificação estrita de altura no Sync para ignorar blocos já processados pelo Consenso, evitando falhas de Nonce Mismatch por re-execução.
    - **Recuperação Soberana V2:** Em caso de falha real de execução, o `StateDB` é 100% reiniciado (`NewStateDB`) para limpar o cache de contas e restaurar a verdade absoluta do SSD.
    - **Motivo:** Corrigir a condição de corrida onde o Sync tentava reprocessar blocos que o Consenso acabara de finalizar, disparando restaurações de estado indevidas que travavam a rede.
2. **Higiene de Execução (Early StateRoot Validation):**
    - **Ação:** O `StateProcessor` agora valida o `IntermediateRoot` (calculado em RAM) contra o cabeçalho **antes** de chamar `st.Commit()`.
    - **Impacto:** Impede a gravação de estados corrompidos ou nonces avançados no SSD em caso de blocos inválidos, garantindo que o disco sempre contenha apenas dados canônicos.
3. **Redução de Ruído Criptográfico:**
    - **Ação:** Rebaixados logs de erro de assinaturas malformadas/inválidas no `VerifyHybrid` para nível `DEBUG`.
    - **Motivo:** Tratamento de "lixo de rede" como evento normal de operação distribuída, evitando poluição dos logs de sistema durante ataques ou testes de estresse.
4. **PQC Performance Milestone (The Tsunami Test 10K - 22/05/2026):**
    - **Resultado:** Processadas **10.000 TXs ML-DSA** em 42 segundos com **238 AvgTPS de execução** reais (finalização em disco).
    - **Vazão de Injeção:** Estabilizada em **651 TX/s**.
    - **Estabilidade:** 100% de sucesso na drenagem da MemPool sem interrupções de consenso ou divergência de StateRoot.

    1. Identidade Soberana On-Chain (Mover chaves Dilithium para a Verkle Tree).
    2. Discovery Dinâmico (Kademlia).
    3. Sincronismo Massivo Híbrido (BitTorrent/Torrent).

## 🛠️ Decisões Recentes (12/05/2026)
1. **Ghost Root Killer (Snapshot Resilience):**
    - Implementada a garantia de StateRoot determinístico em blocos com 0 transações, forçando a herança explícita da raiz do bloco pai.
    - **Motivo:** Corrigir a divergência de estado causada por efeitos colaterais de cache ou poluição de memória em rounds de consenso inativos.
2. **Resiliência do Sync Engine:**
    - Implementada a drenagem agressiva de canais DTS e verificação recursiva "Catch the Bus" no boot.
    - **Motivo:** Evitar travamentos do nó causados por mensagens de blocos futuros ou atrasados que congestionavam as buffers de sincronia.
3. **Persistência de Identidades PQC (Cartório Local):**
    - Criado o arquivo `identities.json` por nó para persistir chaves públicas ML-DSA descobertas durante o Gossip.
    - **Motivo:** Resolver o problema de "Memory Loss" onde nós que reiniciavam perdiam a capacidade de verificar blocos históricos assinados por nós que estavam offline no momento do boot.
4. **Compactação Soberana de Boot:**
    - Implementada chamada automática a `db.Compact(nil, nil)` durante o carregamento de bases de dados existentes.
    - **Impacto:** Redução de 16% no footprint de disco (90MB -> 75MB) após carga massiva, otimizando o I/O para operações subsequentes.
5. **PQC Scalability Milestone (The Tsunami Test - 12/05/2026):**
    - Validada a capacidade da rede de processar 1.000 TXs ML-DSA por bloco (250-500 TPS sustentados) com apenas 16% de uso de CPU em um cluster de 7 nós locais.
    - **Estabilidade:** Confirmada a mutação correta da StateRoot e resiliência com 2/7 validadores offline.
6. **Early RoundChange (Watchdog de Conectividade):**
    - Implementado Watchdog de 2 segundos no início de cada rodada para verificar a conectividade P2P com o Proposer designado.
    - **Ação:** Se o Proposer for detectado como offline via DTS, o nó antecipa seu voto de `RoundChange` sem esperar o timeout completo (10s+).
    - **Segurança:** O salto de rodada permanece protegido pelo quórum de $f+1$ (Weak Quorum), garantindo que a rede só avance se a maioria dos nós ativos concordar com a falha.
    - **Impacto:** Redução drástica da latência em redes com validadores instáveis, recuperando até 80% do tempo de espera por proposer offline.
7. **Evolução Absoluta - Mercado Dinâmico Único (Sovereign V1):**
    - **Ação:** Removido o suporte a transações "Legacy" em favor do modelo EIP-1559 como padrão único e obrigatório.
    - **Motivo:** Eliminar dívida técnica de retrocompatibilidade e garantir proteção nativa contra flood desde a gênese.
    - **Canonicidade:** O layout SSZ foi blindado contra data injection, exigindo tamanhos fixos estritos (32 bytes) para campos de valor e taxas.
8. **Blindagem de Memória e Consenso (Besu Alignment):**
    - **MemPool Purge:** Implementada a purga descendente automática. Se um nonce falha por preço ou saldo, todos os subsequentes são expulsos para manter a fila saudável.
    - **Consenso Econômico:** O `StateProcessor` agora valida o `BaseFee` de cada bloco contra o bloco pai, impedindo manipulações de preço por parte do validador (Proposer).
    - **Atomicidade:** Transferências de valor agora são atômicas à execução da VM via snapshots, impedindo perda de fundos em falhas de contrato.
9. **Determinismo de Assinatura (Effective Price Separation):**
    - **Ação:** O campo `GasPrice` (Effective) foi removido do layout binário assinado.
    - **Motivo:** Permitir assinaturas imutáveis. O preço pago é uma consequência do estado da rede, não uma previsão do usuário. O usuário assina apenas seus limites (`MaxFee` e `PriorityFee`).

## 🛠️ Decisões Recentes (09/05/2026)
1. **Cadência de Produção (Block Pacing):**
    - Implementada a lógica de espera obrigatória no Proposer baseada no `blockPeriod` do Gênesis.
    - **Motivo:** Evitar a "Inundação de Consenso" (Consensus Flooding) onde líderes propoem blocos tão rápido quanto a CPU permite, desestabilizando a latência da rede. A Jamii agora bate o coração em intervalos previsíveis (Besu Compliance).
2. **Tuning de Tolerância (Configurable RequestTimeout):**
    - Externaliado o parâmetro `requestTimeout` para o `genesis.json`.
    - **Calibração:** Redução do timeout padrão de 10s para 4s em cenários de teste para mitigar a latência causada por Rogue Nodes (Alice/Bob) offline, mantendo a fluidez da rede sem intervenção no código.
3. **Segurança Sync-to-Consensus (Observer Lock):**
    - Implementada trava de segurança no orquestrador `Node`. Um validador agora entra em **Modo Observador** automático se sua altura local for inferior à da rede.
    - **Proteção de Integridade:** O nó descarta mensagens de consenso (votos/propostas) até que o `SyncManager` complete o download dos blocos antigos. Isso evita erros de *StateRoot mismatch* causados por tentativas de execução de blocos novos sobre um estado local defasado.
4. **Saneamento de Logs Industriais:**
    - Refatoração do `pkg/util/logger` para implementar o nível **CRITICAL** real (cor vermelha, alta severidade).
    - Remoção de redundâncias de log (ex: `ERROR: CRITICAL`). Erros fatais que interrompem a execução do bloco agora utilizam o método `log.Critical` exclusivamente, facilitando a filtragem em sistemas de telemetria.
5. **Dívida Técnica - Desacoplamento Arquitetural:**
    - Identificado alto grau de acoplamento por assinatura de métodos (Propeller Effect).
    - **Decisão:** Mapeada a necessidade de migrar de variáveis primitivas para **Objetos de Contexto** (ex: `ChainConfig`) nos construtores de Consenso na Fase 5 do projeto.

## 🛠️ Decisões Recentes (06/05/2026)
1. **Saneamento e Qualidade de Código (Audit Ready):**
    - Início do programa de refinamento de documentação técnica em Português Brasileiro (PT-BR) para os módulos Core, visando facilitar o onboarding e auditoria local.
    - **Módulos Saneados:** `pkg/core/types`, `pkg/blockchain` e `pkg/consensus`.
2. **Erradicação de Números Mágicos:**
    - Introdução de constantes de tamanho padronizadas (`HashSize`, `Uint64Size`) em todo o ecossistema Core para garantir consistência binária entre rede, VM e disco.
3. **Persistência Robusta do Blockchain:**
    - Refatoração dos prefixos de banco de dados (`BlockPrefix`, `TxPrefix`, `HeightPrefix`) para constantes nomeadas, eliminando riscos de colisões ou erros de busca no PebbleDB.
4. **Endurecimento de Testes Unitários:**
    - Criação de suíte de teste de ciclo de vida completo (`blockchain_test.go`) validando persistência, recuperação por hash/número e integridade de assinaturas híbridas (Secp256k1 + ML-DSA) no motor Verkle.
5. **Consistência SSZ (ExtraData Soberano):**
    - Decisão de abandonar o formato RLP (Geth/Besu) para o campo `ExtraData` do cabeçalho em favor do **SSZ (Simple Serialize)**.
    - **Motivo:** Garantir consistência técnica com as Transações e permitir **Acesso Aleatório (Random Access)** às assinaturas PQC pesadas (~2.4KB cada) via tabela de offsets, otimizando o tempo de parsing.
6. **Evolução de Rede (Turbine Strategy):**
    - Adoção de uma estratégia de propagação não-linear e fragmentada estilo BitTorrent (inspirada no protocolo Turbine da Solana) para o tráfego de blocos.
    - **Implementação:** O DTS servirá como motor de sharding para pedaços do `IbftExtraData` e corpos de blocos, mitigando o "Header Bloat" causado pelas assinaturas PQC.
7. **Auditoria de Performance PQC (Audit Real-Cost):**
    - Realizado teste de estresse massivo para medir o custo de validação de provas de consenso PQC (ML-DSA).
    - **Resultado:** Mesmo no cenário extremo de 100 validadores (Header de ~325 KB), o tempo total de processamento (Parsing SSZ + Verificação Criptográfica) estabilizou em **~5ms**.
    - **Conclusão:** O custo de processamento PQC representa menos de **0.25%** de um tempo de bloco de 2 segundos, confirmando a viabilidade industrial da arquitetura Jamii apesar do aumento no volume de dados.
8. **Otimização Verkle (IPA Homomorphism):**
    - **Suporte Homomórfico:** Implementado o método `Update` no `BandersnatchCommitter` para permitir atualizações de compromisso estilo delta ($C' = C + \Delta G_i$).
    - **Veredito Batch:** Testes de estresse provaram que para blocos massivos (200+ chaves), a recomputação total via **MSM (Multi-Scalar Multiplication)** é 47% mais rápida (488 TPS vs 254 TPS) devido ao overhead individual e contenção de travas.
    - **Arquitetura Híbrida:** Decisão de manter o homomorfismo apenas para provas individuais e *Stateless Clients* (Witness Efficiency), enquanto a produção de blocos continua usando MSM paralelo.
9. **Fragmentação de Selos de Consenso (DTS Sharding):**
    - **MT_VALIDATOR_SEAL (0x03):** Adicionado novo tipo de dado no DTS para permitir que selos PQC individuais viajem de forma independente do esqueleto do bloco.
    - **Paralelismo de Rede:** Isso permite que os nós comecem a validar e executar o bloco assim que o esqueleto chega, "encaixando" as assinaturas PQC pesadas (~2.4KB cada) conforme elas chegam via DTS, eliminando o gargalo de propagação do cabeçalho completo.
10. **Integração DTS-Identity (Handshake Soberano):**
    - **Handshake de Chave Pública:** Evolução do protocolo DTS para trocar Chaves Públicas Híbridas completas no primeiro contato.
    - **IdentityRegistry (Auto-Populate):** O DTS agora alimenta automaticamente o `IdentityRegistry` com as chaves dos pares conectados. Isso garante que o IBFT (Consenso) consiga validar assinaturas PQC de selos compactos (sem chave inclusa) de forma instantânea.
    - **Soberania de Endereçamento:** O `nodeID` de rede agora é derivado criptograficamente da chave pública (`DeriveSovereignAddress`), unificando a identidade P2P com a identidade da Blockchain.
11. **Soldagem Criptográfica do Consenso (Hardened BFT):**
    - **Fim dos Placeholders:** Removidas todas as "simulações" de verificação (`valid=true`). O motor agora realiza a validação matemática rigorosa (ML-DSA) de cada selo recebido.
    - **Verificação Progressiva:** Implementada a lógica no `VerifyHeader` que reconstrói o *Sealable Hash* e valida o quórum de assinaturas reais contra o `IdentityRegistry`.
    - **Robustez Industrial:** O controlador de consenso agora é agnóstico ao transporte, mas rigoroso na identidade, garantindo que apenas validadores autorizados consigam influenciar a finalização de blocos.

## 🛠️ Decisões Recentes (05/05/2026)
1. **TreeType Imutável (Consensus Parameter):**
    - O tipo de árvore (`SMT` ou `Verkle`) agora é um parâmetro "pétreo" definido no Gênese (`ChainConfig`).
    - **Proteção de Identidade:** O sistema persiste o `TreeType` no primeiro boot. Reinicializações com um `genesis.json` divergente do banco de dados disparam um erro fatal de conflito de identidade, prevenindo corrupção de estado.
2. **Redefinição DTS:**
    - O módulo `pkg/dts` foi renomeado conceitualmente para **Distributed Transmission Service** para refletir melhor sua função de transporte de dados agnósticos na rede P2P.
3. **Validação Verkle:**
    - A Verkle Tree foi homologada para produção após atingir **4.282 TPS** sustentados com zero erros no teste Inferno.

## 🛠️ Decisões Recentes (04/05/2026)
1. **DTS (Distributed Transaction Store):** Implementado como um módulo de rede isolado ("Internal IPFS") para transporte agnóstico de dados.
    - **Deduplicação Determinística (Tie-Break):** Resolvido loop de conexão infinita através de regra de soberania baseada em ID. O nó Soberano (maior ID) mantém Outbound; o Vassalo mantém Inbound.
    - **Identidade de Conexão (addrToID):** Cache inteligente de endereços para evitar rediscagens redundantes em conexões já estabelecidas via Inbound.
    - **Conexões Persistentes:** Utiliza pool de conexões TCP mantidas vivas com timeouts de socket (2s) para evitar bloqueios de recursos.
    - **Polimorfismo (Message Types):** Protocolo INV/REQ/DATA suporta múltiplos tipos de payload (MT_TRANSACTION, etc.).
2. **Benchmark de Performance (Inferno Test):**
    - **Transporte Puro (DTS):** ~8.8k TPS sustentados.
    - **Com Verificação PQC (ML-DSA):** ~5.6k TPS sustentados (Queda de ~36%).
    - **Estabilidade:** Erros: 0. Validada a eficiência do motor de propagação e o custo real da criptografia pós-quântica.
3. **Saneamento de Logs:** Removidos prefixos redundantes (Node:, DTS:) para otimizar a legibilidade dos logs industriais.
4. **Compact Blocks Strategy:** Decisão de propagar blocos contendo apenas Header e lista de CIDs (hashes de transações).
   
## 🛠️ Decisões Recentes (04/05/2026)
1. **Delegação Soberana da TxPool:**
    - **Centralização de Validação:** A `TxPool` agora é o ponto único de entrada e validação (assinatura + saldo). Componentes como `Node` e `RPC` delegam totalmente a custódia, eliminando lógica redundante.
    - **Barreira Econômica:** Implementada verificação rigorosa de saldo (`TotalCost`) antes da aceitação na pool. Isso protege contra ataques de spam de transações sem fundos.
    - **Sincronização Atômica:** O `Node` agora utiliza o hook `OnBlockFinalized` do consenso para resetar a MemPool, garantindo que nonces e saldos estejam sempre em paridade com o último estado comitado.
2. **Refatoração do Consenso (IBFT):**
    - O `Controller` e o `BlockHeightManager` foram atualizados para consumir transações diretamente da `TxPool` via interface `GetExecutable`, desacoplando o motor de consenso da lógica de mempool.
3. **Hardening de Testes:**
    - A suíte de testes da MemPool foi atualizada para exigir transações criptograficamente válidas. Testes com "dummy transactions" foram depreciados em favor de conformidade industrial.

## 🛠️ Decisões Recentes (04/05/2026)
1. **DTS (Distributed Transaction Store):** Implementado como um módulo de rede isolado ("Internal IPFS") para transporte agnóstico de dados.
    - **Deduplicação Determinística (Tie-Break):** Resolvido loop de conexão infinita através de regra de soberania baseada em ID. O nó Soberano (maior ID) mantém Outbound; o Vassalo mantém Inbound.
    - **Identidade de Conexão (addrToID):** Cache inteligente de endereços para evitar rediscagens redundantes em conexões já estabelecidas via Inbound.
    - **Conexões Persistentes:** Utiliza pool de conexões TCP mantidas vivas com timeouts de socket (2s) para evitar bloqueios de recursos.
    - **Polimorfismo (Message Types):** Protocolo INV/REQ/DATA suporta múltiplos tipos de payload (MT_TRANSACTION, etc.).
2. **Benchmark de Performance (Inferno Test):**
    - **Transporte Puro (DTS):** ~8.8k TPS sustentados.
    - **Com Verificação PQC (ML-DSA):** ~5.6k TPS sustentados (Queda de ~36%).
    - **Estabilidade:** Erros: 0. Validada a eficiência do motor de propagação e o custo real da criptografia pós-quântica.
3. **Saneamento de Logs:** Removidos prefixos redundantes (Node:, DTS:) para otimizar a legibilidade dos logs industriais.
4. **Compact Blocks Strategy:** Decisão de propagar blocos contendo apenas Header e lista de CIDs (hashes de transações).

## 🛠️ Decisões Recentes (02/05/2026)
1. **Soberania do GasPrice:** Integrado ao codec SSZ da Transaction. Preço proposto pelo usuário, validado pelo nó.
2. **Buy Gas Atômico:** O StateProcessor debita saldo *antes* da execução. O rollback do bloco estorna as mudanças na Trie, mas a lógica de revert transacional protege o estado.
3. **JSON-RPC Standard:** Decisão de NÃO usar namespaces proprietários (jamii_) para os métodos padrão, visando compatibilidade "out-of-the-box" com Web3 standard.
4. **Rastreio 'latest':** Implementado ponteiro de banco de dados no módulo Blockchain para recuperação instantânea do topo da cadeia.

## 🚀 Backlog de Evolução (Futuro)
1. **Kademlia Discovery:** Substituir a lista estática de peers por descoberta dinâmica.
2. **Phase 4 - BitTorrent Sync:** Plano de dados de alto rendimento para sincronismo massivo.
3. **Estabilidade Industrial IBFT2 (Flood Test - 10/06/2026):**
    - **Resultado:** Sustentados **750 AvgTPS** reais com picos de 3.000 TXs por bloco.
    - **Gargalo Superado:** A latência de montagem do líder foi eliminada via Chained State Oracle.

## 📚 Referências Rápidas
- Detalhes de Opcodes: `docs/DOCUMENTACAO_TECNICA.md`
- Plano Geral: `docs/PLANO_GERAL_PROJETO.md`
- Otimizações: `docs/jamii_turbo_optimization.md`

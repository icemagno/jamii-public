# Tratado de Engenharia e Arquitetura: Fundamentos e Implementação do Jamii Blockchain Core

Este documento é a obra de referência definitiva para o núcleo (Core) da Jamii Blockchain. Diferente de documentações tradicionais que simplificam processos, este tratado foi concebido para ser **extremamente técnico e, simultaneamente, profundamente didático**. Nosso objetivo é capturar a inteligência industrial do projeto, detalhando como o Jamii foi construído para suportar a era pós-quântica mantendo a compatibilidade absoluta com a Ethereum Virtual Machine (EVM).

**Versão:** 1.4 (Consolidação Sprint 9.2 - Recorde de TPS e Estabilidade de Rede)
**Status:** ESPECIFICAÇÃO MESTRA (Homologado pela Auditoria Gemini CLI em 06/06/2026)
**Autor:** Carlos Magno O. Abreu (magno.mabreu@gmail.com) / Jamii Engineering Core

Este documento é a autoridade técnica definitiva sobre o funcionamento interno da Jamii Blockchain. Ele consolida todas as memórias de decisão, relatórios de reparo industrial e especificações de conformidade.

---

## PARTE 1: A Gênese e Fundamentos Teóricos

Antes de dissecarmos a arquitetura, precisamos estabelecer a base teórica sobre a qual este sistema opera. Uma blockchain não é apenas um banco de dados; ela é uma máquina de consenso distribuído que substitui a necessidade de uma autoridade central por provas matemáticas. No Jamii, essa prova é elevada ao nível máximo de segurança conhecido.

### 1.1. O Problema do Consenso e a Imutabilidade
O desafio fundamental de qualquer blockchain é garantir que milhões de computadores independentes (nós) cheguem exatamente ao mesmo resultado final ao processar uma transação. Se Alice envia 10 tokens para Bob, todos os nós devem validar essa operação e atualizar o saldo de forma idêntica. A imutabilidade é garantida por funções de hash criptográfico: se um único bit de uma transação for alterado, o hash muda completamente, invalidando o bloco inteiro. O Jamii utiliza essa imutabilidade para criar o que chamamos de "Cofre Digital".

### 1.2. A Ameaça Quântica e o Algoritmo de Shor
A segurança do Bitcoin e do Ethereum baseia-se em problemas matemáticos que computadores clássicos levam bilhões de anos para resolver (como o logaritmo discreto em curvas elípticas). Contudo, em 1994, Peter Shor provou que um computador quântico de grande escala pode resolver esses problemas quase instantaneamente. Quando essa máquina surgir, as assinaturas digitais atuais tornar-se-ão inúteis. O Jamii resolve este paradoxo através da Segurança Híbrida, unindo a criptografia Secp256k1 (clássica/Ethereum) com o ML-DSA-65 (pós-quântica baseada em Lattices), garantindo que o patrimônio digital sobreviva à transição de eras da computação.

---

## PARTE 2: A Filosofia da Imunidade ao Tempo (O Coração da Blockchain)

O Jamii nasceu de uma constatação inquietante: a fundação criptográfica de quase todas as blockchains existentes hoje (como Bitcoin e Ethereum) tem data de validade. O avanço da computação quântica e algoritmos como o de Shor tornam as curvas elípticas e o ECDSA vulneráveis. Se a rede não puder evoluir sua identidade sem quebrar sua compatibilidade, ela estará condenada ao esquecimento ou ao roubo em massa.

O Jamii resolve este paradoxo através de uma arquitetura que chamamos de "Cofre Imune ao Tempo". Esta solução não é uma simples camada de segurança adicional, mas uma reconstrução total da forma como o estado mundial entende quem é o dono de cada fração de valor. Este documento detalha os três pilares desta soberania: a Escada de Identidade, o Portão-E e o Protocolo de Reconciliação de Mirrors.

### 2.1. A Escada de Identidade: O Caminho da Soberania
Diferente de sistemas tradicionais onde um endereço é apenas um hash fixo de uma chave pública, no Jamii a identidade é uma construção evolutiva em camadas. Esta "escada" permite que o usuário carregue uma armadura quântica pesada enquanto caminha levemente pelas estradas da máquina virtual (EVM).

#### A Raiz Híbrida (The Root of Trust)
No fundamento de tudo, reside a Chave Pública Híbrida. Para garantir que o usuário nunca seja pego de surpresa por avanços na matemática ou no hardware, fundimos dois mundos em um único segredo. A chave híbrida contém uma parte tradicional, baseada no algoritmo Secp256k1, que oferece a velocidade e a compatibilidade exigidas pelos dispositivos móveis atuais. Paralelamente, ela contém uma parte pós-quântica, baseada no padrão ML-DSA-65 (conhecido como Dilithium), que utiliza a matemática de redes esparsas (Lattices) para resistir a ataques quânticos.

Esta chave híbrida completa tem aproximadamente 2KB de tamanho. Se tentássemos usar este objeto diretamente na pilha de 32 bytes de uma máquina virtual, o sistema colapsaria sob o peso dos dados. Por isso, a chave de 2KB nunca circula na rede para transações comuns; ela permanece guardada na carteira do usuário como sua "âncora de soberania" definitiva.

#### O Endereço Soberano V2 (The Sovereign Identity)
Para tornar o tráfego de rede eficiente, a chave híbrida é ancorada através de um payload de 20 bytes derivado da sua parte clássica. O resultado é um identificador binário de 21 bytes (1 byte de versão `0x02` + 20 bytes de payload).

Este degrau da escada é representado para o mundo através do formato Bech32 (ex: `jamii1...`). Escolhemos este formato não por estética, mas por segurança industrial. O Bech32 inclui um checksum polinomial poderoso que detecta erros de digitação humanos. Si você trocar um único caractere ao enviar moedas, a carteira detectará o erro matematicamente e impedirá o envio. No Jamii, o erro de digitação não resulta em perda de fundos, mas em um alerta de segurança.

#### O Mirror Address (A Sombra Ethereum)
O degrau final é o Mirror Address. Ele existe para resolver o problema da compatibilidade com o ecossistema Solidity. Como o mundo Ethereum só entende endereços de 20 bytes e o Jamii opera nativamente com identidades Bech32, criamos uma projeção.

Este endereço Mirror (Versão `0x00`) utiliza exatamente o mesmo payload de 20 bytes da conta soberana, mas formatado como um endereço hexadecimal (`0x...`) com checksum EIP-55. O gênio desta arquitetura é que o Mirror é matematicamente vinculado ao Soberano, sendo ambos a mesma entidade no estado da rede.

### 2.2. O Portão-E (The And-Gate): A Lógica do Cofre Inexpugnável
O Portão-E é a lógica de validação que protege a rede Jamii contra a obsolescência tecnológica. Ele funciona como uma porta física que exige duas chaves diferentes para ser aberta simultaneamente.

Para que qualquer transação de saída seja autorizada pela rede, o sistema aplica a regra: `Verificação = Verify(Secp256k1) AND Verify(ML-DSA-65)`. Se apenas uma das assinaturas for válida, a transação é descartada como lixo criptográfico.

**Por que o And-Gate é vital?**
Imagine que, em dez anos, surja um computador quântico capaz de quebrar a criptografia tradicional. Um atacante que possua sua chave privada clássica tentaria roubar seu dinheiro. Ao apresentar a assinatura clássica na rede Jamii, ele bateria no Portão-E. O sistema diria: *"A chave clássica está correta, mas onde está a prova quântica?"*. Como o atacante não tem a parte quântica da chave (que é imune ao seu computador), o dinheiro permanece imóvel. 

Esta lógica cria uma "Imunidade ao Tempo". Seu saldo pode ficar parado por décadas; enquanto você possuir a chave híbrida, seu patrimônio estará protegido contra qualquer evolução de hardware que venha a ocorrer.

### 2.3. A Identidade Unificada: Arquitetura Shadowless
Uma das maiores inovações do Jamii é a eliminação total do conceito de "Shadow Accounts" (Contas de Sombra) através da **Unificação por Payload**.

#### O Elo Matemático (20-byte Anchor)
Diferente de sistemas que exigem uma migração ou resgate de fundos, no Jamii os endereços Soberanos (`v1`) e os endereços Mirror (`v0`) compartilham o **mesmo payload de 20 bytes**. Este payload é derivado da parte clássica (Secp256k1) da chave pública híbrida.
*   **Consequência:** Ambos os endereços apontam para a **mesma folha** no World State (seja na Trie Verkle ou SMT).
*   **Mão Dupla Nativa:** Se um usuário recebe tokens em seu endereço `0x...`, o saldo é imediatamente visível e acessível através de seu endereço `jamii1...`. Não há "gavetas" separadas ou necessidade de mover fundos entre versões.

#### O Reveal de Segurança
Embora o saldo seja unificado, a segurança da conta evolui organicamente:
1.  **Estado Inicial:** Se a rede apenas conhece o endereço (hash), ela protege o saldo com base no compromisso criptográfico.
2.  **Primeiro Gasto (Reveal):** No momento da primeira transação assinada, o usuário revela sua Chave Pública Híbrida completa. A rede valida o Portão-E e vincula permanentemente a identidade PQC ao payload de 20 bytes.
3.  **Proteção Retroativa:** A partir deste momento, qualquer tentativa de gastar fundos associados àquele payload — mesmo que originada por uma ferramenta que use apenas o formato Mirror — exigirá obrigatoriamente a assinatura híbrida (And-Gate).

### 2.4. Evolução e Hash Agility: Preparação para o Futuro
O Jamii foi projetado sob o princípio da **Agilidade Criptográfica (Hash Agility)**. Esta flexibilidade permite que a rede aceite novas versões de endereços e algoritmos de hash sem necessidade de hard-forks disruptivos.

A capacidade de suportar múltiplos formatos (v0, v1, v2) simultaneamente na mesma estrutura de banco de dados (`StateDB`) garante que a Jamii Blockchain possa adotar o próximo padrão de segurança da indústria (ex: Versão 3 com algoritmos pós-quânticos de 4ª geração) apenas adicionando um novo identificador de versão, mantendo a integridade de todo o histórico anterior.

---

## PARTE 3: O Guia Prático e Analógico do Jamii

Se você nunca ouviu falar de "hashes" ou "criptografia quântica", não se preocupe. Este guia explica o que construímos até agora e como o **Jamii** funciona na prática, usando exemplos do dia a dia.

### 3.1. O Exemplo do Fluxo: Da Carteira ao Bloco
Imagine que o **Jamii** é um sistema de correios ultra-seguro.

**Passo 1: O Envio (Identidade e Endereço)**
Você quer enviar 10 tokens para um amigo. Para isso, você usa a sua chave.
*   **O que construímos:** O `pkg/types` e `pkg/crypto`. 
*   **Como funciona:** O sistema cria um endereço para você começar com `jamii1...`. É como o seu CPF. O fato de ser `jamii1...` garante que se você digitar uma letra errada, o sistema percebe e avisa: "Ei, esse endereço não existe!", protegendo seu dinheiro.

**Passo 2: O Envelope (Transação e Encoding)**
O seu desejo de enviar tokens é colocado em um "envelope digital" chamado **Transação**.
*   **O que construímos:** O `pkg/encoding`.
*   **Como funciona:** Como o envelope do Jamii precisa carregar assinaturas "pesadas" (para se proteger de computadores quânticos), nós criamos um envelope inteligente. Ele tem um **Cabeçalho** (onde diz quem envia e quanto) e um **Corpo** (onde estão os dados pesados). Isso faz com que o carteiro (a rede) consiga ler o essencial rápido sem precisar abrir o envelope todo.

**Passo 3: A Verificação (O Guarda e o "And-Gate")**
Antes do envelope ser aceito, um guarda verifica se a assinatura é sua mesmo usando uma técnica chamada **And-Gate** (Porta "E").
*   **O que construímos:** O `pkg/crypto/signer` (Híbrido).
*   **Como funciona:** O Jamii exige **dois cadeados obrigatórios** para abrir a porta. Um tradicional (que todos usam hoje) e um especial (Pós-Quântico). É uma porta do tipo "E": você precisa da chave A **E** da chave B. 
    *   Se alguém tentar usar apenas a chave comum, a porta não abre. 
    *   Mesmo que no futuro alguém invente uma super-máquina para quebrar o cadeado comum, o segundo cadeado (Pós-Quântico) continuará segurando a porta sozinho.

**Passo 4: O Grande Livro de Contabilidade (Trie de Estado - Verkle / SMT)**
Depois de verificado, o sistema precisa anotar: "Você agora tem 10 a menos, e seu amigo tem 10 a mais".
*   **O que construímos:** O `pkg/trie` (Trie Factory com Verkle/SMT).
*   **Como funciona:** Imagine um armário gigante com bilhões de gavetas. A nossa **Trie de Estado** é o sistema que sabe exatamente em qual gaveta está o seu saldo. Ela utiliza o padrão **Verkle Trees** como motor padrão para gerar provas ultra-compactas (ou opcionalmente **SMT**). Ela só ocupa espaço físico na memória com as gavetas que realmente contêm dados.

### 3.2. Tirando as Suas Dúvidas

**"O que é o tal Hash de tamanho variável?"**
Um **Hash** é como uma "impressão digital" of um dado. No Ethereum antigo, essa impressão digital tem sempre o mesmo tamanho (32 caracteres).
*   **O Problema:** E se no futuro as impressões digitais de 32 ficarem fáceis de falsificar?
*   **A Nossa Solução (Hash Agility):** O Jamii é como um sistema que aceita impressões digitais de 32, 48 ou 64 caracteres. Hoje usamos 32 para sermos rápidos, mas se o perigo aumentar, a rede "muda a lente" e passa a exigir impressões digitais maiores **sem precisar trocar todo o sistema**.

**"Como os endereços podem ter tamanhos diferentes?"**
Imagine o número de telefone. Antigamente eram 8 dígitos, depois viraram 9. O sistema de telefonia aceita os dois.
*   No Jamii, um endereço `jamii1...` de um usuário novo pode ser maior e mais seguro do que um endereço de 5 anos atrás. 
*   **A rede entende os dois.** O sistema olha o primeiro pedaço do endereço (a Versão) e já sabe: "Ah, esse aqui é dos grandes e modernos" ou "Esse aqui é dos antigos".

**"Vou usar jamii1... ou 0x...?"**
*   **Você (Humano):** Sempre usará `jamii1...`. É o seu nome oficial. É mais seguro e legível.
*   **A Rede (Máquina):** Para conversar com os contratos inteligentes (Solidity), a rede usa um "apelido" curto, que começa com `0x...`.
*   **Os dois ao mesmo tempo?** Sim! É como se você tivesse um nome completo (Carlos Magno) e um crachá com um número. Você usa o nome, mas o sistema de ponto da empresa usa o número. O Jamii faz a tradução automática para você.

**"Existe risco nesse 'apelido' curto (0x...)?"**
Sim, existe um risco matemático minúsculo chamado **Colisão de Mirror Address**.
*   **O que é:** Como o "crachá" soberano tem 32 bytes e o "apelido" tem apenas 20, nós cortamos um pedaço da informação. Teoricamente, dois usuários diferentes poderiam ter o mesmo apelido `0x...`.
*   **Qual a chance?** É de 1 em 1.000.000.000.000.000.000... (um seguido de 48 zeros). É a mesma chance de alguém adivinhar a sua chave privada do Ethereum hoje.
*   **Nossa Decisão:** Aceitamos esse risco "impossível" para que você possa usar o Jamii com ferramentas que já existem, como a MetaMask. Se no futuro isso se tornar um problema, o Jamii já nasceu pronto para aumentar o tamanho desse apelido sem quebrar nada (é a nossa famosa **Hash Agility**).

**"Onde o dinheiro fica guardado de verdade?" (Memória vs. Disco)**
Imagine que o Jamii tem uma **Mesa de Trabalho** (Memória RAM) e um **Arquivo de Aço** (Disco/SSD).
*   **A Mesa:** É onde o sistema faz as contas rápidas. Se o sistema desligar, a mesa é limpa.
*   **O Arquivo de Aço:** É o banco de dados real (**PebbleDB**). Tudo o que é confirmado vai para gavetas trancadas no disco. No Jamii, usamos a mesa apenas para sermos rápidos, mas a verdade final está sempre no aço.

**"Por que alguns nomes começam com jamii1 e outros com 0x?"**
*   **jamii1... (O Crachá):** É como o seu nome. Serve para as pessoas saberem para quem estão enviando dinheiro. É bonito e seguro contra erros.
*   **0x... (A Impressão Digital):** É um código técnico. Serve para o computador conferir se o armário de saldos está trancado e se ninguém mexeu em nada. Você verá muito isso nos registros técnicos, mas no dia a dia, usará apenas o seu crachá `jamii1`.

**"Como minha carteira é gerada?"**
A carteira do Jamii não é um simples arquivo; ela é uma fortaleza digital construída em três camadas:
1.  **As 12 Palavras (Mnemônico):** Tudo começa com 12 palavras simples (BIP39). Elas são a sua "Semente Mestre". Se você perder seu computador, essas palavras são a única forma de recuperar seu dinheiro.
2.  **A Derivação Soberana (BIP-Jamii):** Como o Jamii usa chaves duplas (uma para hoje e outra para o futuro quântico), nós usamos uma técnica avançada de derivação (HMAC-SHA512). Isso garante que suas duas chaves sejam matematicamente independentes e seguras. 
    *   *Nota:* Por ser uma tecnologia de ponta para chaves híbridas, as 12 palavras do Jamii só funcionam no nosso ecossistema. Isso é um requisito para garantir a sua proteção pós-quântica total.
3.  **O Cofre (Keystore):** Quando você salva sua carteira no computador, ela é protegida por uma senha forte. Usamos o **Scrypt** (com 131.072 iterações) para impedir que invasores tentem "adivinhar" sua senha, e o **AES-256-GCM** (o mesmo padrão usado por governos) para embaralhar seus dados.

> 💡 **Higiene de Memória:** O Jamii é projetado para ser "anti-forense". Assim que você termina de criar ou abrir sua carteira, o sistema destrói fisicamente o mnemônico e os segredos da memória RAM, para que nenhum vírus ou invasor consiga extraí-los.

### 3.3. O "RG" da sua Conta: Por que o endereço é curto?
Você deve ter notado que nossas chaves são gigantes por causa da segurança quântica. Mas não se preocupe, você não precisará copiar códigos enormes para enviar moedas.

*   **A Chave Pública (O Passaporte):** É um documento técnico grande que fica guardado na sua carteira.
*   **O Endereço (O seu RG):** É um resumo matemático (Hash) dessa chave. O Jamii pega aquela chave gigante e a resume em um código curto e elegante que começa com `jamii1...`.
*   **Resultado:** Você tem a segurança de um cofre de banco, mas a facilidade de usar um número de conta comum.

---

## PARTE 4: A Máquina Virtual Soberana (JEVM) e Processamento de Estado

O motor de execução da Jamii Blockchain, conhecido como **JEVM (Jamii Ethereum Virtual Machine)**, é o coração programável da rede. Ele foi projetado para garantir a compatibilidade absoluta com o padrão Ethereum, permitindo que contratos escritos em Solidity ou Vyper funcionem nativamente com performance industrial.

### 4.1. A Arquitetura da JEVM (`pkg/vm`)
Diferente de implementações experimentais, a JEVM segue o modelo de **Despacho via Tabela de Salto (Jump Table)**, o mesmo padrão de alta performance utilizado pelo Hyperledger Besu.

*   **Orquestração Central (`evm.go`):** Gerencia o ciclo de vida das mensagens e o contexto de execução (Block Number, Timestamp, Gas Limit, etc.).
*   **Isolamento Industrial:** Cada execução ocorre em um `MessageFrame` isolado, garantindo que falhas em contratos não corrompam o estado global da rede.
*   **Compliance Cancun:** Suporte nativo a opcodes modernos como `PUSH0`, `TLOAD/TSTORE` (Transient Storage) e `MCOPY`.

**JamiiVM (EVM Compliance & Cancun-Ready)**
A VM opera em registradores de 256 bits e seguindo o rigor industrial do Besu. Atualmente, a JamiiVM atingiu o marco de **95% de conformidade** com o conjunto de instruções do Ethereum Cancun.

*   **Verificação Tripla (The Gold Standard):** Cada instrução crítica foi validada comparando as implementações do Geth, Besu e as especificações do Yellow Paper.
*   **Opcodes de Nova Geração (Cancun Milestone):** 
    | Opcode | Hex | Descrição | Status |
    | :--- | :--- | :--- | :--- |
    | **PUSH0** | `0x5F` | Empilha o valor zero com custo mínimo de gás. | ✅ Ativo e Testado |
    | **TLOAD** | `0x5C` | Carregamento de Transient Storage (EIP-1153). | ✅ Ativo e Testado |
    | **TSTORE** | `0x5D` | Escrita em Transient Storage (EIP-1153). | ✅ Ativo e Testado |
    | **MCOPY** | `0x5E` | Cópia eficiente de memória (EIP-5656). | ✅ Ativo e Testado |
    | **BLOBHASH** | `0x49` | Acesso a hashes de blobs (EIP-4844). | ✅ Ativo e Testado |

*   **Transient Storage (EIP-1153):** Implementação completa do armazenamento temporário que zera ao final de cada transação, permitindo padrões de reentrância e comunicação entre contratos com economia massiva de gás.
*   **BASEFEE Context:** O opcode `BASEFEE` (0x48) é alimentado pelo valor real da rede, permitindo que contratos inteligentes tomem decisões econômicas baseadas no congestionamento.
*   **Atomicidade de Valor:** Transferências de `msg.value` são protegidas por snapshots de estado, garantindo que o saldo só mude se a execução for concluída sem erros fatais.
*   **Mirror Resolution:** A VM opera sobre endereços de 20 bytes (Mirrors) para compatibilidade Solidity, mas resolve esses mirrors em identidades soberanas de 33 bytes no momento do acesso ao disco.

### 4.2. O Orquestrador `StateProcessor` (`pkg/core/processor.go`)
O `StateProcessor` é o cérebro que une a Máquina Virtual ao Banco de Dados de Estado (StateDB). Sua principal responsabilidade é garantir a **Atomicidade da Transição de Estado**.

*   **Sandboxing de Execução:** Antes de persistir qualquer dado no disco (SSD), o processador executa as transações em uma "Sandbox" na RAM. Se uma transação falha ou o bloco é inválido, a Sandbox é descartada, protegendo o banco contra corrupção.
*   **Garantia de Determinismo:** O processador assegura que, dadas as mesmas transações e o mesmo estado inicial, todos os nós da rede Jamii cheguem exatamente à mesma raiz de estado (`StateRoot`).

**Sistema de Journaling (Diário de Bordo)**
O Jamii utiliza um **Journal** para rastrear mudanças atômicas:
*   **Snapshot/Revert:** O `Snapshot()` retorna um índice no Journal; o `RevertToSnapshot()` desfaz apenas as entradas posteriores a esse índice, permitindo reversões rápidas em caso de erro na VM.

### 4.3. O Ciclo de Vida do Gas (Economia EIP-1559)
A Jamii implementa o mercado de taxas dinâmicas inspirado na atualização Londres do Ethereum.

1.  **Buy Gas (Dedução Preventiva):** O sistema retira do saldo do remetente o valor `GasLimit * EffectiveGasPrice` antes da execução.
2.  **Execução e Consumo:** Opcodes consomem gás baseados em complexidade computacional.
3.  **Refund Gas (Estorno):** Gás não utilizado é devolvido: `(GasLimit - GasUsed) * EffectiveGasPrice`.
4.  **Fee Distribution:** O `BaseFee` é "queimado" e a `PriorityFee` é destinada aos validadores.

#### 4.4. Ciclo de Vida de Smart Contracts
*   **Deploy de Contrato (`tx.To == nil`):** Endereço gerado via `Create(sender, nonce)`.
*   **Chamada de Contrato:** Execução de bytecode contra o estado atual.
*   **Recibos:** Gerados com `Status`, `GasUsed` e `ContractAddress`.

### 4.5. Política de Pré-compilados Soberanos (Anti-Conflict Strategy)
Diferente do ecossistema Ethereum que utiliza numeração crescente para novos pré-compilados (0x01, 0x02, 0x03...), a Jamii Blockchain adota uma **Estratégia de Numeração Decrescente**.

*   **Regra de Ouro:** Todos os serviços soberanos da Jamii devem ser alocados a partir do final do espaço de endereçamento de 20 bytes.
*   **Normalização:** A VM utiliza internamente chaves hexadecimais minúsculas sem o prefixo `0x` para busca ultra-rápida no mapa de pré-compilados.

#### Catálogo de Serviços Soberanos:
1.  **Identity Bridge (`0xFF...FF`):** Converte endereços Mirror (0x) para strings Bech32 (jamii1). Custo: 50 gas.
2.  **Sovereign Bridge (`0xFF...FE`):** Converte strings Bech32 para o payload Mirror (0x) original. Custo: 50 gas.

#### Correção de Contabilidade de Gás (Refundo Industrial)
Durante a implementação dos bridges, foi identificada e corrigida uma falha no motor de execução (`executeCall`). Agora, a JamiiVM garante a devolução do `gasLimit` reservado após a execução bem-sucedida de um pré-compilado, cobrando apenas o custo nativo da função. Isso permite chamadas encadeadas de bridges no mesmo bloco sem erros de `Out of Gas`.

---

## PARTE 5: Especificação Técnica e Motor de Estado (A Bíblia Técnica)

### 5.1. Módulo de Tipos e Primitivas (`pkg/types`)
O módulo `types` é o fundamento de imutabilidade da rede. Ele foi reconstruído para garantir rigidez binária e suporte à evolução criptográfica sem quebra de consenso.

**Hash Agility (Nativo)**
Diferente de redes tradicionais com tamanhos fixos, o Jamii suporta hashes de 256, 384 e 512 bits. 
*   **Implementação:** Cada hash carrega um prefixo implícito que determina seu algoritmo (Keccak-256 por padrão).
*   **Blindagem:** `HashFromBytes` exige validação estrita de tamanho, disparando `CodeDataCorruption` em caso de buffers malformados.

**Endereçamento Soberano e Unificado**
A identidade no Jamii utiliza o formato **Bech32** (conforme BIP-0173) para endereços soberanos e **Hex EIP-55** para espelhos.

*   **Prefixo HRP:** `jamii`
*   **Versões Suportadas:**
    *   `0x00` (Mirror): Compatibilidade Ethereum (0x...).
    *   `0x01` (V1 Legacy): Secp256k1 puro em Bech32.
    *   `0x02` (V2 Sovereign): Híbrido PQC em Bech32.
*   **Payload Unificado:** 20 bytes (Hash Keccak-256 da chave pública Secp256k1 uncompressed, extraindo os últimos 20 bytes).
*   **Tamanho Binário:** 21 bytes (1 byte Versão + 20 bytes Payload).

**Mirroring Seguro (Compatibilidade Ethereum)**
Para interoperabilidade com a EVM, o Jamii gera um **Mirror Address (0x...)**.
*   **Lógica Industrial:** `Mirror = EIP55(V2_Payload)`.
*   **Identidade Única:** Como o Mirror e o Soberano compartilham o mesmo payload, eles apontam para a mesma folha na Trie de estado (Verkle/SMT), garantindo unificação de saldo.

### 5.2. Criptografia e Identidade (`pkg/crypto`)
O Jamii opera em um modelo de **Segurança Pós-Quântica Híbrida**.

**O Portão-E (And-Gate Security)**
Nenhuma transação é válida com apenas uma prova. A validação exige:
`Result = Verify(Secp256k1) AND Verify(ML-DSA-65)`
*   **Secp256k1:** Garante compatibilidade com o ecossistema Ethereum e hardware atual.
*   **ML-DSA-65 (Dilithium):** Garante resistência contra o Algoritmo de Shor (Ataque Quântico).

**Strong Binding (Vínculo Inquebrável)**
Para impedir ataques de *Mix-and-Match* (reuso de assinatura quântica em outra transação clássica), implementamos o vínculo forte:
`BindingHash = Keccak256(Message || TradPubKey || QuantPubKey)`
A assinatura é realizada sobre o `BindingHash`, tornando as duas identidades indissociáveis.

**Higiene de Memória (Wiping Real)**
*   **Método `Zero()`:** Todas as chaves privadas implementam limpeza física da RAM.
*   **Anti-Otimização:** Uso de `runtime.KeepAlive` para garantir que o compilador não ignore o wiping.
*   **Performance:** Verificação híbrida estabilizada em **102.904 TPS**.

### 5.3. Codificação e Serialização (`pkg/encoding`)
O Jamii utiliza **SSZ (Simple Serialize)** como padrão nativo, unificado sob o modelo **Sovereign V1 (EIP-1559 Native)**.

**Unificação Londres (Besu Alignment)**
A Jamii removeu o suporte a transações "Legacy" (GasPrice fixo). Todas as transações operam sob o mercado dinâmico de taxas Londres (EIP-1559 Native).

#### Layout SSZ Soberano (V1)
| Campo | Tipo | Descrição |
| :--- | :--- | :--- |
| **Version** | uint32 | Identificador `0x01` |
| **Nonce** | uint64 | Contador de transações do remetente |
| **GasLimit** | uint64 | Unidades máximas de gás |
| **ChainID** | uint64 | Proteção contra Replay |
| **MaxFeePerGas** | Uint256 | Preço máximo total (incluindo BaseFee) |
| **MaxPriorityFeePerGas** | Uint256 | Gorjeta (Tip) para o validador |
| **Value** | Uint256 | Valor da transferência |
| **To** | Address? | Destino (Opcional para criação de contrato) |
| **Data** | []byte | Payload para contratos inteligentes |
| **PubKey** | []byte | Chave Pública Soberana (Híbrida) |
| **Signature** | []byte | Assinatura Híbrida (Strong Bound) |

**Determinismo de Assinatura:** O campo `GasPrice` (Preço Efetivo) é calculado em runtime (`min(maxFee, baseFee + priorityFee)`) e **não faz parte do hash assinado**, garantindo que a assinatura permaneça válida mesmo com a volatilidade do BaseFee.

#### Motor de Cálculo (Besu-Aligned)
O cálculo do `BaseFee` segue a lógica canônica de Londres para garantir previsibilidade econômica:
1.  **Elasticity Multiplier:** 2 (O GasLimit do bloco é o dobro do TargetGas).
2.  **Max Change Denominator:** 8 (A variação máxima do BaseFee é de 12.5% por bloco).
3.  **Piso Industrial:** O `BaseFee` nunca desce abaixo de 1 wei.

#### Mitigação de Flood (Defesa em Profundidade)
A Jamii implementa três níveis de proteção contra ataques de negação de serviço:
*   **Nível 1 (DTS/Network):** Deduplicação de hashes e propagação inteligente (Broadcast-Wait) para evitar redundância de dados PQC pesados.
*   **Nível 2 (MemPool/Memory):** Barreira ativa (rejeição de TXs com taxa abaixo do BaseFee atual) e **Purga Descendente** (se um nonce falha, todos os sucessores do mesmo remetente são expulsos).
*   **Nível 3 (Execution/State):** Validação rigorosa do BaseFee no cabeçalho contra o cálculo do bloco pai e atomicidade de execução via Journaling.

**Inline Canonicality**
O decodificador SSZ não realiza re-codificação para validar a integridade.
*   **Técnica Zero-Gap:** Validação estrita de offsets e tamanhos fixos durante o parse inicial.
*   **Performance:** Recorde de **911.088 transações/segundo**.

**Lifecycle da Transação**
As transações são imutáveis após a criação.
*   **Deep Copy:** O método `DeepCopy()` garante que o cálculo de hashes de assinatura seja imune a corrupções de memória em ambientes multi-thread.
*   **Sender Recovery:** O remetente é recuperado de forma "honesta" (PubKey Recovery) apenas uma vez e cacheado no objeto da transação.

### 5.4. Motor de Estado e Persistência (`pkg/trie` & `pkg/store`)
O motor de estado do Jamii evoluiu para a arquitetura **Verkle Turbo (O(1))**, combinando a eficiência de prova dos compromissos IPA (Inner Product Argument) com a velocidade de busca de bancos de dados planos.

#### Verkle Tree Homomórfica (O(1) Optimization)
A grande inovação da Sprint 5.0 foi a eliminação do gargalo de re-computação $O(256)$ dos nós internos da árvore.
*   **Cálculo Incremental:** O Jamii utiliza a propriedade homomórfica dos compromissos de Pedersen. Ao alterar um valor na folha, o novo compromisso do nó pai é calculado como:
    $$C' = C + (v_{new} - v_{old}) \times G_i$$
    Onde $G_i$ é o ponto gerador pré-computado para a posição $i$. Isso reduz a complexidade de atualização de $O(256)$ para $O(1)$ operações de curva elíptica por nível.

#### Fundamentação do Motor IPA (Inner Product Argument)
A escolha do esquema de compromisso polinomial (PCS) é o que define a soberania e a escalabilidade da Jamii. Optamos pelo **IPA** em detrimento de alternativas como KZG ou FRI pelos seguintes pilares técnicos:

1.  **Segurança Transparente (No Trusted Setup):** Ao contrário do KZG, que exige uma cerimônia de "Trusted Setup" (risco de governança e segredos tóxicos), o IPA é 100% transparente. Ele utiliza apenas pontos geradores aleatórios na curva elíptica, garantindo que a Jamii nasça sem dependências de confiança em terceiros.
2.  **Viabilidade Stateless (Logarithmic Proofs):** O IPA oferece provas de tamanho logarítmico $O(\log d)$. Embora o KZG ofereça provas constantes $O(1)$, o IPA é consideravelmente mais eficiente que o FRI (base do STARK) para inclusão em blocos, permitindo que as "Testemunhas de Bloco" (Witnesses) permanecem pequenas o suficiente para viabilizar nós sem disco (Stateless Nodes).
3.  **A Sinergia com a Curva Bandersnatch (The Performance Fix):** Para mitigar o custo computacional do IPA, o Jamii utiliza a curva **Bandersnatch** (uma curva de Twisted Edwards otimizada). Esta escolha é estratégica por três motivos:
    *   **Eficiência GLV:** Suporte nativo a endomorfismos acelerados, permitindo multiplicações escalares (base dos compromissos) em tempo recorde.
    *   **Circuit-Friendliness:** Por ser construída sobre o campo escalar da BLS12-381, a Bandersnatch permite que provas de estado sejam verificadas dentro de circuitos ZK sem o custo proibitivo de aritmética não nativa.
    *   **Padrão Industrial:** Alinhamento com a pesquisa de ponta global (Ethereum Statelessness), garantindo que a Jamii utilize uma matemática testada para o problema exato de árvores de estado de alta performance.

*Nota sobre Performance:* Reconhecemos o trade-off do custo de verificação linear $O(d)$ do IPA. Para mitigar esse gargalo em futuras fases de escala massiva, o roadmap da Jamii prevê a exploração de técnicas de recursão (como Halo2) para achatar o custo de verificação na camada de finalização.

#### Teoria Homomórfica: Performance vs. Privacidade
É fundamental distinguir a aplicação do Jamii em relação ao conceito genérico de *Criptografia Homomórfica* (HE).

*   **Criptografia Homomórfica Geral (Privacidade):** Focada em processar dados sem descriptografá-los (ex: FHE - Fully Homomorphic Encryption). O objetivo é o sigilo absoluto perante o processador (ex: nuvem).
*   **Homomorfia no Jamii (Performance):** O Jamii utiliza **PHE (Criptografia Parcialmente Homomórfica)** baseada em *Compromissos de Pedersen*. O objetivo não é o sigilo dos dados (que são públicos no estado da blockchain), mas a **verificabilidade instantânea**.

Ao utilizar a propriedade aditiva homomórfica, o motor Verkle do Jamii consegue "somar" uma alteração de saldo diretamente na raiz da árvore ($C' = C + \Delta \times G_i$) sem precisar ler ou reprocessar os 256 ramos vizinhos. Esta é a "mágica" matemática que permite ao sistema manter a segurança industrial com performance de milissegundos.

#### Estrutura e Persistência (PebbleDB)
*   **Branching Factor:** 256-vias (Aridade de 256), otimizada para provas de estado compactas para *Stateless Clients*.
*   **Batch Commits:** As mutações de estado são acumuladas em memória e gravadas no PebbleDB em um único batch atômico ao final de cada bloco.
*   **PPQ (Pre-Proof Queue):** Sistema de fila que prepara os compromissos em paralelo enquanto a JamiiVM ainda executa as transações, ocultando a latência de I/O.

#### Performance de Estado
*   **Finalização:** Processamento de blocos de 1.500 TXs estabilizado em **~2 segundos**.
*   **Throughput de Pico:** **750 TX/s** sustentados em ambiente de estresse industrial.
*   **Eficiência de Cache:** Uso do `MaxWarmTries` para manter os nós mais acessados em RAM, reduzindo a pressão sobre o motor de disco.

### 5.5. Higiene Protocolar e Resiliência de Memória (Higiene Sincronizada)
O Jamii implementa um rigoroso protocolo de limpeza automática para evitar vazamentos de memória (OOM) e ataques de poluição de estado em cenários de instabilidade de rede ou consenso.

**Gestão de Memória da MemPool (O Custo PQC)**
A arquitetura de assinaturas pós-quânticas (ML-DSA) altera drasticamente o perfil de memória do nó. Enquanto em redes baseadas unicamente em Secp256k1 uma transação ocupa ~200 bytes, no Jamii uma transação híbrida ocupa em média **4.5 KB a 5 KB** devido ao tamanho das chaves públicas (2KB) e assinaturas Dilithium (~3KB).
*   **Perfil sob Estresse:** Uma Mempool operando em sua capacidade padrão de 10.000 transações consome **~150 MB** de RAM (incluindo caches, mapas de indexação e overhead de estruturas Go).
*   **Riscos de Inundação (Flood):** Como o limite de tamanho por transação (`MaxTransactionSize`) é de 1 MB, um atacante explorando o limite numérico (10k TXs) poderia forçar a alocação de até 10 GB de RAM, causando um *Out of Memory* (OOM).
*   **Defesa Planejada:** A transição de um limite puramente numérico para um limite volumétrico (*Memory-Based Capacity*) e a implementação de restrições por conta (*AccountCap*) formam a base da Sprint 5.1 para resiliência industrial.

**Pruning de Payloads Órfãos (PayloadPool)**
Blocos propostos que não atingem o quórum (blocos que "morrem na praia") são automaticamente removidos do cache de memória.
*   **Mecanismo:** O `PayloadPool` executa `PruneByHeight(minHeight)` a cada avanço de altura (`StartHeight`).
*   **Garantia:** Somente blocos com altura igual ou superior à altura atual do consenso permanecem na RAM.

**Saneamento de Esqueletos (Node Pending Blocks)**
Fragmentos de blocos (Compact Blocks) que aguardam transações via P2P são monitorados e descartados se ficarem obsoletos.
*   **Ação:** O método `prunePendingCompactBlocks` remove automaticamente esqueletos de blocos de alturas passadas que nunca foram reconstruídos com sucesso.

**Higiene de Rodadas (Consensus Rounds)**
O controlador de consenso (IBFT) realiza a purga de votos (`roundChangeVotes`) e propostas futuras (`futureProposals`) sempre que uma nova rodada é efetivamente iniciada, garantindo que o estado interno do nó permaneça limpo após saltos de rodada (Round Jumps).

### 5.6. Sincronização e Resiliência P2P (`SyncManager`)
O processo de sincronização da Jamii Blockchain é projetado para ser **direcionado e eficiente**, protegendo a largura de banda da rede e a CPU dos validadores ativos.

#### Estratégia "Best Peer" (Seleção Inteligente)
Diferente de protocolos que inundam a rede com pedidos, o Jamii utiliza o mapeamento de alturas para identificar o **Best Peer** (o par com a maior altura confirmada).
*   **Decisão:** O `SyncManager` foca o download sequencial especificamente contra este par, reduzindo a fragmentação e garantindo que os blocos sejam recebidos em ordem cronológica.

#### Download Sequencial e Validado
A sincronia ocorre através de requisições P2P (`RequestBlockByNumber`) via motor DTS ou em lote via BitTorrent.
1.  **Request-Reply:** O nó solicita o bloco $H$, processa e só então solicita o bloco $H+1$.
2.  **Validação de Consenso (Selo PQC):** Cada bloco recebido via sync passa obrigatoriamente pela `VerifyHeader` do motor de consenso. Se o selo quântico do validador não for autêntico, o sync é abortado para proteger o `StateDB` de corrupção por dados maliciosos.
3.  **Drenagem de Canal:** Para evitar processar blocos obsoletos ou atrasados que chegam devido a latência de rede, o `SyncManager` realiza a drenagem completa do buffer de blocos antes de emitir uma nova solicitação.
4.  **Higiene do Observer Mode (CPU/Mempool Protection):** Para economizar recursos de CPU e evitar saturação da mempool com transações antigas ou inválidas, novas transações (`MT_TRANSACTION`) e selos de consenso fofocados (`MT_VALIDATOR_SEAL`) são descartados silenciosamente pelo nó enquanto o `SyncManager` indicar que o nó está atrás da rede (`IsBehind() == true`).
5.  **Mapeamento Amigável de Logs:** O `SyncManager` resolve a identidade Bech32 bruta para nomes amigáveis baseados na tabela populada no boot (ex: `NODE_A`), facilitando a leitura de rede.

#### Prioridade e Handover
*   **Consensus Priority:** As requisições de sincronia são tratadas em goroutines de transporte (DTS), garantindo que o nó "Server" nunca sacrifique a CPU do Consenso Live para servir dados históricos.
*   **Live Handover:** Quando o nó atinge a altura do Best Peer (ou fica a apenas 1 bloco de distância), ele transita suavemente para o **Consenso Live**, onde passa a receber blocos via broadcast em tempo real, tornando-se um par apto ao quórum.

#### Roadmap: Multi-Peer Sync (BitTorrent-style)
Para cenários de sincronismo massivo (milhões de blocos), o Jamii evoluirá para a **Fase 4: BitTorrent Sync**.
*   **Otimização:** O nó dividirá a cadeia em "chunks" de blocos, solicitando diferentes fatias de múltiplos pares simultaneamente.
*   **Performance:** Esta abordagem transformará o sincronismo em uma operação de **Alto Rendimento**, eliminando o I/O de disco como gargalo único de um só par.

### 5.7. O Plano de Dados (Data Plane): Arquitetura BitTorrent Soberana
A Jamii adota uma arquitetura de rede de "Plano Duplo", onde o **BitTorrent** atua como o músculo de transporte para dados pesados, operando em total isolamento da sinalização crítica de consenso.

#### 1. Separação de Responsabilidades (Control vs. Data)
*   **DTS (Plano de Controle):** Sistema de baixa latência focado na "janela crítica" do consenso. Transporta votos, propostas e esqueletos de blocos (Compact Blocks). É o sistema nervoso que mantém a rede viva.
*   **Torrent (Plano de Dados):** Sistema de alta vazão (Throughput) focado em background. Responsável pelo download de blocos históricos, propagação de blocos cheios (Full Blocks) e distribuição de Snapshots de estado.

#### 2. Rede Soberana e Privada
Diferente de clientes torrent convencionais, a implementação da Jamii (em `pkg/torrent/engine.go`) é configurada para máxima segurança:
*   **Isolamento Total:** DHT, PEX (Peer Exchange) e Trackers públicos estão desativados.
*   **Topologia Injetada:** A rede de validadores é injetada manualmente como `Trusted Peers`. Isso cria uma malha P2P privada onde os nós se conectam apenas entre si, eliminando vetores de ataque de redes públicas.

#### 3. Eficiência de Armazenamento: O Arquivo Virtual (Zero Duplication)
Para evitar o desperdício de espaço em disco (não duplicar o banco de dados em arquivos torrent), o Jamii utiliza a técnica de **Virtual File Storage**:
*   **Abstração de I/O:** O motor de torrent não lê arquivos físicos do sistema operacional. Ele utiliza uma interface customizada que mapeia "pedaços" do torrent diretamente para leituras no **StateDB (PebbleDB)**.
*   **On-the-fly Serialization:** Quando um par remoto solicita um dado, o nó lê os blocos do banco, serializa-os em memória e os entrega via protocolo torrent. O dado físico existe em apenas um lugar: o banco de dados da blockchain.

#### 4. Integração Dinâmica via DTS
O Torrent não substitui a descoberta de dados, ele apenas executa a entrega. O fluxo de trabalho integrado é:
1.  **Sinalização:** Um validador anuncia via **DTS** que um novo chunk de blocos (ex: 1.000 alturas) está disponível, informando o seu **InfoHash**.
2.  **Descoberta:** Nós que precisam desses dados (seja por estarem em `HALT` ou por terem perdido um bloco pesado) instruem seu motor Torrent a buscar esse InfoHash.
3.  **Download Multidirecional (Swarm):** O nó baixa pedaços de diferentes validadores ao mesmo tempo, utilizando a soma da banda de toda a rede para se recuperar o mais rápido possível e voltar ao modo de consenso ativo.

#### 5. Snapshots e Poda (Pruning)
O Torrent é essencial para a estratégia de **Snapshots**:
*   **State Snapshots:** Fotos compactas do estado atual (saldos e contratos) são compartilhadas via torrent para que novos nós possam "pular" o processamento de blocos antigos e chegar à altura atual em minutos.
*   **Poda Geográfica:** Nem todos os nós precisam semear toda a chain. Nós podem escolher ser "Seeders" apenas dos últimos blocos ou de fatias específicas, garantindo a disponibilidade de dados sem sobrecarregar o hardware individual.

---

## PARTE 6: Conquistas de Performance e Sincronismo (Sprint 9.2)

A Sprint 9.2 marcou a transição da Jamii de um protótipo funcional para uma infraestrutura de throughput massivo, atingindo o recorde histórico de **454.5 TX/s sustentados** em cluster real.

### 6.1. Witness-Aided Quorum Acceleration
Esta inovação arquitetural resolve o dilema clássico "Segurança vs. Velocidade" em árvores Verkle.
*   **O Problema:** Provas IPA (Inner Product Argument) levam centenas de milissegundos para serem verificadas, o que em blocos densos (3.000+ TXs) impedia o quórum de fechar dentro da janela do `BlockPeriod`.
*   **A Solução:** O Proposer anexa um **Block Witness** contendo os estados iniciais das contas. Validadores realizam uma **Verificação Otimista** em RAM para emitir seus votos de `PREPARE` e `COMMIT` instantaneamente.
*   **Resultado:** Redução de 70% na latência de finalização. A prova IPA completa é verificada de forma assíncrona antes do commit final, garantindo integridade absoluta sem sacrificar o TPS.

### 6.2. Estabilização DTS e Consenso (DTS & Consensus Resilience)
O motor P2P e o laço de consenso foram blindados contra saturação de buffer, picos de I/O e validadores lentos/syncing.
*   **DTS WriteDeadline:** Aumentado para **10 segundos**. Esta mudança estratégica impede que o SO derrube conexões legítimas durante a reconstrução de blocos gigantes (3.500+ TXs).
*   **Race Condition Mitigation:** Ajuste na ordem de precedência entre a montagem de blocos e o timer da rodada, eliminando o modo "Catch-up" desnecessário e garantindo que o dado chegue sempre antes do sinal de consenso.
*   **Watchdog de Proposer Não-Pronto (Early Leader Change):** Caso o propositor designado de uma rodada de consenso seja detectado como "not ready" (sincronizando) ou offline (via conectividade P2P), o nó antecipa o voto de `RoundChange` após 2 segundos de carência (grace period), poupando até 8 segundos de ociosidade em relação ao timeout padrão de 10s.
*   **Inicialização Segura e Poda de Logs:** O mapa de prontidão da rede (`peerReady`) é preenchido com `false` para todos os validadores no boot para assegurar que comecem como "not ready" até mandarem mensagem de status. Adicionalmente, logs individuais de votos debug de `COMMIT` foram removidos e o log de quórum de commit foi simplificado para `Quorum to COMMIT reached.` para evitar poluição visual em redes com muitos validadores.

### 6.3. Governança de Validadores via Gênese (Genesis Validator Keys) & Resiliência de Boot
A evolução da Jamii Blockchain para um modelo federado com governança determinística eliminou a troca de chaves públicas de validadores por vias dinâmicas inseguras ou voláteis (gossip e handshakes P2P).

*   **Governança de Chaves no Gênese:** As chaves públicas PQC híbridas (Secp256k1 + ML-DSA-65) dos validadores ativos são lidas diretamente do arquivo `genesis.json` na inicialização do nó. Elas são estruturadas sob a chave `"validators"` como uma lista contendo `"address"` (Endereço Soberano `jamii1...`) e `"publicKey"` (string hexadecimal de 3984 caracteres representando o payload criptográfico completo). Ao bootar, o nó decodifica e registra essas identidades estritamente na tabela de chaves (`ident:`), permitindo a validação criptográfica imediata das assinaturas das mensagens de consenso.
*   **Saneamento de Manifestos PQC:** Os manifestos `node_address.json` gerados no provisionamento dos nós foram atualizados para expor a mesma chave pública híbrida de 3984 caracteres no formato completo (prefixado com o identificador de algoritmo híbrido `02220000000002...`). Isso resolve falhas de tamanho de vetor na carga inicial de chaves Dilithium (`invalid ML-DSA public key`).
*   **Resiliência de Boot (Consensus Unlock):** Em ambientes onde o banco de dados PebbleDB continha tabelas e metadados, mas a chain local não possuía blocos indexados (`latest == nil`), o consenso entrava em impasse ou travamento na inicialização. Implementou-se um fallback automático: caso o banco exista mas não haja blocos canônicos ativos, o nó reconstrói e materializa o bloco gênese #0 a partir do arquivo de configuração, gravando-o no banco de dados local para desbloquear o consenso.

### 6.4. Execução Stateless (RAM Storage Mode) & Otimizações DTS
Com o objetivo de suportar nós de baixo consumo e clientes leves de alta performance (sem dependência de persistência em disco físico), a Jamii introduziu o suporte nativo a nós 100% Stateless:

*   **RAM Storage Mode:** Através da flag `--stateless` na linha de comando ou `"stateless": true` no arquivo de configuração `config.yaml`, o nó inicializa o seu banco de dados em memória RAM (`store.NewMemoryStore()`) em vez de abrir um PebbleDB físico no disco (`store.NewPebbleStore`).
*   **Regra de Segurança de Validator Stateless:** Um nó em modo stateless não pode ser um validador ativo na rede (visto que ele não persiste dados de estado de consenso e transações essenciais). Durante o boot, se a flag stateless estiver ativa, o nó deriva seu endereço soberano a partir da chave do nó e verifica se ele está listado como validador ativo no `genesis.json`. Em caso de conflito, a inicialização é abortada com o erro `invalid configuration: a stateless node cannot be listed as an active validator in genesis`.
*   **Remoção de Gossip de Identidades (MsgIdentities):** Com a governança das chaves migrada de forma determinística para o gênese e contratos de votação on-chain, o protocolo DTS de gossip dinâmico de identidades (`MsgIdentities`) e a troca de chaves via handshake foram removidos. Isso eliminou um overhead de rede significativo causado pelo tráfego constante de chaves híbridas Dilithium de ~3.9 KB, protegendo o plano de controle de ataques de spam e saturação de buffer.

---

## PARTE 7: Ecossistema e Interfaces Externas

### 7.1. Servidor JSON-RPC (A Ponte de Integração)
O servidor RPC da Jamii foi redesenhado para oferecer performance industrial e compatibilidade estrita com ferramentas de mercado (Web3.js, Ethers, MetaMask).

*   **Arquitetura:** Baseada no padrão de Request/Response atômico, com suporte nativo ao mercado de taxas EIP-1559.
*   **Módulos Suportados:** `eth_*` (Core), `net_*` (Rede) e `web3_*` (Utils).
*   **Otimização:** Uso de pools de conexão e buffers SSZ para serialização rápida de blocos e transações.

**O Dispatcher Geth-Compliant:**
O servidor (`pkg/rpc/server.go`) atua como um roteador de alta velocidade. Métodos como `eth_blockNumber`, `eth_getBalance` e `eth_getTransactionReceipt` são resolvidos diretamente contra o `Blockchain` e o `StateDB`.

**Execução Read-Only com `eth_call`:**
Para permitir a simulação de transações e a leitura de estados de contratos sem custo de gás ou alteração no banco de dados, o Jamii implementa o `eth_call`.
*   **Snapshot Isolado:** Cada chamada cria uma instância temporária da JEVM que opera sobre um snapshot do estado atual.
*   **Segurança:** Nenhuma alteração feita durante um `eth_call` é persistida, garantindo que consultas externas sejam puramente informativas.

**Propagação e MemPool:**
Ao receber uma transação via `eth_sendRawTransaction`, o servidor a encaminha para a MemPool local. Se a transação for válida (assinatura correta, nonce em ordem e saldo suficiente), ela é armazenada e simultaneamente propagada para o restante da rede através do motor **DTS (MT_TRANSACTION)**.

### 7.2. Padrão Único de Transações: eth_sendRawTransaction
Diferente de implementações Ethereum legadas, a Jamii Blockchain adota o **eth_sendRawTransaction** como o único endpoint para postagem de dados. 

**Segurança por Design (Server-Side Privacy):**
O Jamii proíbe o uso de `eth_sendTransaction`. Este mandato garante que as **Chaves Privadas** nunca residam na memória do nó, eliminando o risco de roubo de ativos em caso de comprometimento do servidor. A assinatura é uma responsabilidade exclusiva do Cliente (SDK Java, Wallet CLI, etc).

**Polimorfismo de Transação:**
O nó utiliza o conteúdo da transação assinada para determinar sua finalidade, sem necessidade de múltiplos endpoints:

| Tipo de Operação | Campo `To` | Campo `Data` | Ação do Motor |
| :--- | :--- | :--- | :--- |
| **Transferência** | Endereço do Destinatário | Vazio | Movimentação de Saldo Simples. |
| **Deploy de Contrato** | **Nulo (nil)** | Bytecode | Criação de conta e instalação de código. |
| **Chamada de Contrato** | Endereço do Contrato | Input Data | Execução da JamiiVM sobre o código destino. |

### 7.3. SDK Java Soberano (`sdk/java`)
Para suportar o desenvolvimento de aplicações empresariais e mobile (Android), criamos o **Jamii Java SDK**.

*   **PQC Native:** Primeira biblioteca Java do mundo com suporte nativo a ML-DSA-65 (Dilithium) integrado ao fluxo de transações blockchain.
*   **Fluent API:** Design baseado em builders para construção simplificada de transações, gestão de carteiras e interação com contratos inteligentes.
*   **Android Ready:** Otimizado para baixo consumo de bateria e memória, permitindo que dispositivos móveis operem como nós observadores seguros.


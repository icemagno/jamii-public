# 📜 Guia de Especificação do Bloco Gênese — Jamii Blockchain (`genesis.json`)

Este documento especifica a estrutura, os parâmetros e os princípios de funcionamento do arquivo `genesis.json` da **Jamii Blockchain**.

O arquivo `genesis.json` estabelece o **Bloco #0 (Bloco Gênese)**, definindo a identidade pétrea da rede, o estado inicial das contas (`alloc`), as regras do consenso IBFT 2.0 e os validadores fundamentais da blockchain.

> [!NOTE]
> Todos os nós participantes de uma mesma rede Jamii devem utilizar exatamente o mesmo arquivo `genesis.json`. Qualquer divergência no arquivo resultará em um `StateRoot` incompatível no Bloco #0 e o nó será rejeitado pela rede.

---

## 📋 Estrutura Geral do Arquivo

O arquivo `genesis.json` é um objeto JSON composto por seis seções principais:

```json
{
  "config": { ... },
  "initialBaseFee": "1000000000",
  "timestamp": 1746758400,
  "gasLimit": 30000000,
  "alloc": { ... },
  "validators": [ ... ]
}
```

### Campos Raiz

| Campo | Tipo | Descrição |
| :--- | :---: | :--- |
| `config` | `object` | Objeto de configuração imutável da identidade e regras da blockchain ([`ChainConfig`](file:///c:/Magno/Projetos/jamii/pkg/params/config.go#L188)). |
| `initialBaseFee` | `string` | Taxa base inicial de gas por bloco na menor unidade (Wei). Padrão: `"1000000000"` (1 Gwei). |
| `timestamp` | `integer` | Carimbo de data/hora de criação do Bloco #0 em segundos (Unix Timestamp). |
| `gasLimit` | `integer` | Limite máximo de gas permitido em cada bloco produzido na rede (ex: `30000000`). |
| `alloc` | `object` | Mapa de contas, contratos e saldos pré-alocados no estado inicial da rede. |
| `validators` | `array` | Lista contendo a autoridade inicial de validadores IBFT 2.0 (máximo de 30 validadores). |

---

## ⚙️ Detalhamento do Bloco `config` (`ChainConfig`)

A seção `config` especifica os parâmetros operacionais imutáveis que regem a execução e o consenso da rede:

```json
"config": {
  "chainId": "2026",
  "isFreeGas": false,
  "treeType": 2,
  "blockPeriod": 3,
  "requestTimeout": 10,
  "maxTxsPerBlock": 900,
  "maxMempoolSlotSize": 15000,
  "maxMempoolMemorySize": 536870912,
  "maxTxBatchSize": 50,
  "validatorContract": "0x00000000000000000000000000000000fffffffd"
}
```

### Parâmetros da `ChainConfig`

| Parâmetro | Tipo | Padrão | Descrição |
| :--- | :---: | :---: | :--- |
| `chainId` | `string` | `"2026"` | Identificador único da blockchain. Funciona como a "carteira de identidade" da rede. |
| `isFreeGas` | `boolean` | `false` | Se `true`, desabilita a cobrança de saldo por taxa de execução de transações. |
| `treeType` | `integer` | `2` | Estrutura de dados da árvore de estado: `1` para SMT (Sparse Merkle Trie), `2` para Verkle Trie. |
| `blockPeriod` | `integer` | `2` | Intervalo mínimo de tempo em segundos entre a produção de blocos consecutivos (Consenso IBFT). |
| `requestTimeout` | `integer` | `10` | Tempo limite em segundos que o consenso aguarda por uma proposta antes de iniciar o *Round Change*. |
| `maxTxsPerBlock` | `integer` | `1000` | Limite máximo de transações que o líder pode incluir em um único bloco. |
| `maxMempoolSlotSize` | `integer` | `10000` | Quantidade máxima de transações mantidas simultaneamente na MemPool. |
| `maxMempoolMemorySize` | `integer` | `536870912` | Limite máximo de memória física (RAM) alocada para a MemPool em bytes (ex: 512 MB = `536870912`). |
| `maxTxBatchSize` | `integer` | `50` | Quantidade máxima de transações empacotadas por lote durante a propagação P2P. |
| `validatorContract` | `string` | *opcional* | Endereço pré-compilado soberano do contrato de registro de validadores on-chain. |
| `treasury` | `string` | *opcional* | Endereço do cofre de tesouraria para retenção de penalidades de *slashing*. |

> [!IMPORTANT]
> Uma vez inicializada a blockchain, a alteração de qualquer parâmetro dentro do bloco `config` fará com que os nós recusem a inicialização devido ao mecanismo de **Trava de Identidade da Rede (`meta:chain`)**.

---

## 💰 Detalhamento do Bloco `alloc` (`GenesisAlloc`)

A seção `alloc` define o estado inicial do banco de dados da blockchain (`StateDB`). Ela é utilizada para conceder saldos iniciais a contas e implantar código binário de contratos inteligentes ou pré-compilados.

```json
"alloc": {
  "jamii1z6ppc63fese7v8dv0pnngyjlwtpu8cu9auqtt95": {
    "balance": "865836200000000000000000000",
    "nonce": 0,
    "code": null,
    "storage": null
  },
  "0xa660f35cb1d29acd4ab4771df5ea44fa06202228": {
    "balance": "1000000000000000000000000000",
    "nonce": 0,
    "code": null,
    "storage": null
  }
}
```

### Estrutura de cada Conta (`GenesisAccount`)

* **Endereço (Chave)**: Pode ser especificado tanto em formato **Soberano Bech32 (`jamii1...`)** quanto em hexadecimal EVM (`0x...`).
* **`balance`** (`string`): Saldo inicial alocado em Wei (unidade mínima). Deve ser uma string representando o valor inteiro decimal em 256 bits.
* **`nonce`** (`integer`): Contador de transações da conta no Bloco #0 (usualmente `0`).
* **`code`** (`string`/`bytes`, opcional): Bytecode binário formatado em Hexadecimal para preencher a conta como um contrato inteligente.
* **`storage`** (`object`, opcional): Mapeamento de slots de memória Hex para Hex (`Slot -> Value`) para definir o estado de armazenamento inicial do contrato.

---

## 🛡️ Detalhamento do Bloco `validators` (`ValidatorConfig`)

A lista `validators` especifica o conjunto inicial de nós validadores que possuem autoridade para propor e assinar blocos no consenso IBFT 2.0.

```json
"validators": [
  {
    "address": "jamii1z7fsyg98ejec8vhg7mdyruus0v676jd5ryxa62v",
    "publicKey": "0222000000000276073a73deff787355b5b4a09a...",
    "alias": "Adequate Albernaz"
  },
  {
    "address": "jamii1zvn6k33jjwlkup7acutrkf4hppms3pcwk8syw74",
    "publicKey": "02220000000002b4318653620665d937e3697840...",
    "alias": "Barbarian Bonaparte"
  }
]
```

### Propriedades do Validador

* **`address`** (`string`): Endereço Soberano Jamii (Bech32, `jamii1...`) derivado da chave pública do validador.
* **`publicKey`** (`string`): Chave pública pós-quântica/híbrida do validador em formato Hexadecimal (`0x...`). Usada para assinar propostas de bloco e validar assinaturas do consenso IBFT.
* **`alias`** (`string`, opcional): Apelido legível atribuído ao nó para identificação no console de depuração e no registro de nós.

> [!WARNING]
> **Limite Industrial de Validadores no Gênese:** A Jamii impõe um limite máximo de **30 validadores iniciais** no arquivo `genesis.json` ([`pkg/core/genesis.go`](file:///c:/Magno/Projetos/jamii/pkg/core/genesis.go#L89)). Tentar inicializar uma gênese com mais de 30 validadores gerará um erro fatal de compilação do bloco.

---

## 🔒 Mecanismos de Integridade e Trava Pétrea de Rede

### 1. Persistência da Identidade (`meta:chain`)
No primeiro boot de um novo nó, o objeto `ChainConfig` contido no `genesis.json` é gravado permanentemente no banco de dados PebbleDB sob a chave de metadados `meta:chain`.

Em boots subsequentes, a função `SetChain` ([`pkg/params/config.go`](file:///c:/Magno/Projetos/jamii/pkg/params/config.go#L87)) compara a configuração no banco com a fornecida no `genesis.json`. Se houver divergência em qualquer parâmetro (`chainId`, `treeType`, `blockPeriod`, etc.), a execução é abortada imediatamente com a mensagem:

```
[FATAL] CRITICAL: Network Identity Conflict!
This database belongs to another network. Execution aborted.
```

### 2. Cálculo Determinístico do `StateRoot`
Ao executar o método `Commit()` no `genesis.json`, o nó:
1. Aplica todas as alocações da seção `alloc` no `StateDB`.
2. Pré-aloca o contrato de registro de validadores (caso `validatorContract` esteja definido).
3. Grava o estado na árvore de estado (SMT ou Verkle Trie) e gera o hash da raiz de estado (`StateRoot`).
4. Codifica os validadores no campo `ExtraData` do cabeçalho de acordo com o padrão IBFT.
5. Gera o **Bloco #0**.

---

## 📄 Exemplo Completo de `genesis.json`

Abaixo está um exemplo completo e funcional de um arquivo `genesis.json` configurado para uma rede de testes privada Jamii:

```json
{
  "config": {
    "chainId": "2026",
    "isFreeGas": false,
    "treeType": 2,
    "blockPeriod": 3,
    "requestTimeout": 10,
    "maxTxsPerBlock": 900,
    "maxMempoolSlotSize": 15000,
    "maxMempoolMemorySize": 536870912,
    "maxTxBatchSize": 50,
    "validatorContract": "0x00000000000000000000000000000000fffffffd"
  },
  "initialBaseFee": "1000000000",
  "timestamp": 1746758400,
  "gasLimit": 30000000,
  "alloc": {
    "jamii1z6ppc63fese7v8dv0pnngyjlwtpu8cu9auqtt95": {
      "balance": "1000000000000000000000000000",
      "nonce": 0,
      "code": null,
      "storage": null
    },
    "jamii1zj5nhccs62hsp44j3tqln7a8r58vr5fxhsu7f5g": {
      "balance": "500000000000000000000000000",
      "nonce": 0,
      "code": null,
      "storage": null
    }
  },
  "validators": [
    {
      "address": "jamii1z7fsyg98ejec8vhg7mdyruus0v676jd5ryxa62v",
      "publicKey": "0222000000000276073a73deff787355b5b4a09a165aabb3870e5e04bf9a9ffaa8083296cf1a1301ef13771610737d7737c1bf4606c989e38d505c0cd9aeee80fec0bbc07521a7d0c10e03f88e63141b585a98cdfd46c6a4e057080896c88b4b1b59d2be4dcdcd2fbb7120534bb1819033e0652d510205d9a6417ccc1ca2c7e431b7d3cd52e7cb46de286fec7aaa31294e2322bd89e9cd5a7e5d8fbfc516eb7802b9abaf15c4e650b9280f2bbf07712123084229eb502561fd96eee29bf90384d70e43c7130de6ec1bb10cdb65535fd6fde14202248fcec567602e6c637e53ed6cd5765823e8bff6b5bbd81f8c167575a1290f72800bca514c002139bf49b92589821daad0665727c47cd71484c0eac9be3c75135e3a4550bf358a94c74dd799df273155c701a0f7b8ab3b5c6f9dd149dd08f75803463ec3d7d96d9847ce4db21a1eb15c1b0f660c299cccf18053c4f865483645c860bb4c0eb37b692dca70b005c194611c4a4488eb97476e2c2d0bc62bfa7b4f2c6e5721b13d42a20261b3a9a242fc15b2a896d908e0f62f5baf968fe0d96f8b519cb11770a88613bf4db6e51f5bcf6cf174f0b8c58889537a9ac458c4e96ee17723a6a91e072a0027c5e860b6a16d97415639522d247150846549c3007e6e88289396d8b2a4740875551f684f732fcff09317b4cb928f8d30384b3b878a1e36275f0c250b39d2fd38b9af526b8f44d7e5f766bdb5672a22c278c9ad8ea17dbe538b012c3ab898c5827fd71482a57a7be631408748279d15918dfc3a73482a14d0afacfa206cf94904fef0df45228ce336b2382f0c3b86275ff11e404904bafc7c6a64795e159cb62749b29bd4797161ea1e0fb9c29a0a158f8ffc00d63e0457c2f534f88e23428bc344c4e8e7bfadd0359891350952410ff59d4484cc8f2354398ab418b25778899075836546f302496d912064247609d2e48d47fdc6375c13cfa9b2a314a63e0af1702e300e43fd102841f0df5ce3372e77e42d75c412150ecb731f21ef3f1c44ea1b7b4d1a8ee2ffcae8aa7c7bbd0055ecd54948b678b839475b032520227451a007b955398f1bb94885dc3b542f52843b55d8d9bf64b6724a90fc63a6014f175ed5691236158621e9c73182e22bea3c6bc5c2207b1a5dfaa4e4ea853b73fef90058b7cf7c2fbbdf5493b95fb5f1295992050f6399b705cfe1e87f4c0a3e08116caf82c6efb06ccb3a24228ff3425e596edc012447ec11e8bd08bb0bb4a6635d727a04dfb78c1bc5e3a54e46050d3b2207b6c8db85c5425cb6c2fa8f25bb1ec9f1957c889b518e7f5ec184cbbba4919f76c55535faf19331b1951f29f23d74053df67cc15212d3e5830258915ad048dfd2fb5062d22959093b1de35f201a7dfbd40d6f1891765260313e9c6d6ba79843261d240a167b69bf1644600577941fc4989e2f19f8cd2043f762465b4267c10aa1fe5d7fe178ea6ea5902e7ae9ace67b488b3058c8824bdb860afde8f0413ef6bcc170b50c02697531c880e96ad84c0e419106ce296af80563ce0da071966f18418dcdf7d13436067f7d592fc4b481846249515c378b58d5460fa503bc8d5efff88bef27cd5b249200ac6e7ad8cda7b24773c3b6fb0537a622cb5837e33e7341afcb6c5a2481ac64dd06616a4f4e9b8c5196903d51f21e673d6371e96ace3a85b027b244f903a53da9f4ece6933121707a727bce9935edc4ba7973ffe7512af8fdf15a08ac5f4fa5591c58fa7eab469bce642ebaec1abc1412de31e064d2d750436a1c6a30d4fb74d871b1a379b49d0ac7e50f97f78761ce0184cc2f2de8f170ef5b2bc6d33c9fbb17eb5094346f3079af76fa149ad0ace9530e3b455fbdba436c764879a851da5a68aa449730aa4e7a981e8529646914f86857f7e47bf1c1954562968d1b2ac36b823688cfd4a8c0389fcaa18f5d89e783dcea7e9b1540d902c0abce49adc8b215d0e69e63e41a4cdd1b5460d0f3c5c8d5d97770babc70637937d218357f06e8a6d44b3fb309af25242eccaeb688b76449a036449e1e3251ade21f584bf4d3edab86bd723025d83eb53016b4aef5a907a7b0a5103ca6f35baff04a43bd2daee1460debc7f4c7b8c8fbf114329e80d47fa5faa3cafc230deb6945d4344e1dc36b567d81a13a7f43ece13f163b9a0004f5114068ba3c728d733246307aa3f445138c6544e9b0420e0d0ff626a32adc830747c1b781beaf4738028d8bcd941684ad2a0636082c13c01b20a8841d9a12deb1cd8a629b9279d189e03f7b90e41cfb723d4ea0fe7fca7b1f93a8be54d674f80e20bba9d6e8cff38a7083cbaeb321dd981d394ed23c0d1997eaca9bb880f1d99dc44c3a749d9cde0e766aa25753e8fcf8be87aef0183d87c574fffcdd0417396e267023905b8611943abb82d4d50efdaabda0b03038e5f5eec1f642325cdd6c9ba7561c900f5d1f2f0825b52a08a8a04e6ec048c36e1b12e42c365227771824c400f34aa157743dfac5dad29955881b746c264e9783de21025d67611adaf8260410d28ca4c91762485d30f1a7e4bfb263a84d420f74ac3a9b1e6795da7ae08f80a7df9585d983e1ee0f01287cd72f2158fae50cc670dcade3818c08c9fa089e24ffa6fa5979c2f4bf8500615e8918402f6a29d9eb6285a99362308285e41996b3ea4c1ef02bd71ecbd477cb4aae48043aacd7b49494ce887385df7fee54f3b2d657c1c28c56f39b78024c0413d5e3909cb458e24431f8cd76f35e6eed8b23af54563028dc99fbbfade47b47f4f2b0a43f286df21efac7e21e09ffc8cb8be1f9ae26e97ebf70",
      "alias": "Adequate Albernaz"
    }
  ]
}
```

# ⚙️ Guia de Configuração Operacional — Jamii Blockchain (`config.yaml`)

Este documento detalha todos os parâmetros de configuração suportados no arquivo `config.yaml` da **Jamii Blockchain**. 

O nó Jamii adota uma arquitetura de centralização de configuração inspirada no Hyperledger Besu e Go-Ethereum, utilizando **YAML** para opções operacionais do nó e **JSON** para a definição da rede Gênese (`genesis.json`).

> [!NOTE]
> Se qualquer parâmetro for omitido no arquivo `config.yaml`, o nó carregará automaticamente o valor padrão seguro de produção definido em código ([`pkg/node/config.go`](file:///c:/Magno/Projetos/jamii/pkg/node/config.go)).

---

## 📋 Tabela de Parâmetros Operacionais

| Parâmetro YAML | Tipo | Valor Padrão | Descrição |
| :--- | :---: | :---: | :--- |
| `data_dir` | `string` | `"./data"` | Diretório base para o banco de dados de estado (PebbleDB) e arquivo de identidade. |
| `node_key_file` | `string` | `"./nodekey"` | Caminho para a chave privada pós-quântica do nó (ML-DSA / Dilithium + Secp256k1). |
| `miner_enabled` | `boolean` | `true` | Habilita a participação ativa do nó como validador no consenso IBFT 2.0. |
| `rpc_port` | `integer` | `8545` | Porta HTTP/TCP para o servidor JSON-RPC 2.0 (consultas Web3 e envio de transações). |
| `p2p_port` | `integer` | `30303` | Porta TCP do protocolo DTS (Distributed Transaction Store - Control Plane). |
| `static_peers` | `array` | `[]` | Lista de vizinhos estáticos para o canal de controle DTS (Formato Mandatário: `ENDEREÇO_SOBERANO@host:port`). |
| `torrent_port` | `integer` | `6881` | Porta TCP para a rede BitTorrent (Data Plane - Sincronismo de Chunks de Blocos). |
| `torrent_peers` | `array` | `[]` | Lista de vizinhos BitTorrent estáticos (Formato: `ENDEREÇO_SOBERANO@host:port`). |
| `log_level` | `string` | `"info"` | Nível de detalhamento dos logs (`trace`, `debug`, `info`, `warn`, `error`). |
| `genesis_file` | `string` | `"./genesis.json"` | Caminho para o arquivo JSON contendo a especificação do Bloco #0 da rede. |
| `peers_file` | `string` | `"peers.json"` | Caminho opcional para o registro local de resolução de nomes lógicos de pares. |
| `pg_conn_str` | `string` | `""` | String de conexão para o banco relacional PostgreSQL (opcional para indexação SQL). |
| `sync_from` | `string` | `""` | Endereço soberano de um nó preferencial como referência inicial de sincronismo. |
| `stateless` | `boolean` | `false` | Habilita o modo 100% Stateless (execução em memória RAM sem banco de dados no disco). |
| `is_archive_node` | `boolean` | `false` | Habilita o nó como Arquivador Soberano (serve blocos históricos antigos para a rede). |
| `prune_enabled` | `boolean` | `true` | Habilita o Pruning em disco no PebbleDB (descarte automatizado de estado antigo). |
| `prune_retain_blocks` | `integer` | `1024` | Quantidade de blocos recentes mantidos em disco na janela móvel. |
| `prune_batch_size` | `integer` | `256` | Frequência (em blocos) com que o ciclo de limpeza física do banco é executado. |

---

## 🧹 Detalhamento: Pruning & Retenção de Histórico

A Jamii implementa a arquitetura **Bonsai Turbo** com Pruning automatizado no PebbleDB. O descarte de histórico mantém o tamanho do banco de dados reduzido e o processamento de I/O em tempo constante $O(1)$.

### 1. Nó Validador Padrão (Produção)
Por padrão, todo nó validador limpa blocos e recibos antigos para focar no consenso do bloco atual:
```yaml
prune_enabled: true
prune_retain_blocks: 1024
prune_batch_size: 256
```
* **Efeito:** O nó mantém apenas os últimos **1.024 blocos** no disco. Blocos mais antigos são fisicamente purgados do PebbleDB.

### 2. Nó Validador de Testes (Janela de Retenção Expandida)
Em ambientes de teste, se você quiser permitir que nós entrem mais tarde sem perder histórico recente:
```yaml
prune_enabled: true
prune_retain_blocks: 50000
prune_batch_size: 1000
```
* **Efeito:** O validador preserva até **50.000 blocos** em disco antes de iniciar o expurgo.

### 3. Nó Arquivador Dedicado (`Archiver`)
Nós de arquivo guardam a história completa da blockchain desde o Bloco #0 até o topo, servindo de fonte para o *SyncManager* e para o Explorer:
```yaml
is_archive_node: true
prune_enabled: false
```
> [!IMPORTANT]
> Em nós onde `is_archive_node: true` está configurado, o `prune_enabled` deve ser obrigatoriamente `false`.

---

## 🌐 Endereçamento Soberano de Peers (`static_peers`)

De acordo com os mandatos de segurança da Jamii (Diretivas 13 e 14), todos os pares definidos em `static_peers` e `torrent_peers` devem seguir **rigorosamente** o formato de Endereço Soberano (Bech32):

```yaml
static_peers:
  - "jamii1zvn6k33jjwlkup7acutrkf4hppms3pcwk8syw74@127.0.0.1:30304"
  - "jamii1z6nunrx9j26fqzsusrqsj2dqts2z87qf66nzp05@127.0.0.1:30305"

torrent_peers:
  - "jamii1zvn6k33jjwlkup7acutrkf4hppms3pcwk8syw74@127.0.0.1:42091"
  - "jamii1z6nunrx9j26fqzsusrqsj2dqts2z87qf66nzp05@127.0.0.1:42092"
```

> [!CAUTION]
> É estritamente proibido o uso de endereços hexadecimais (`0x...`) ou IP puro sem o prefixo do Endereço Soberano (`jamii1...`). Conexões sem validação de identidade soberana são rejeitadas no handshake do protocolo DTS para prevenção de ataques de Sybil e Eclipse.

---

## ⚡ Exemplos Práticos de Arquivos `config.yaml`

### Exemplo 1: Validador Padrão (`n1/config.yaml`)
```yaml
data_dir: "./"
node_key_file: "./nodekey"
miner_enabled: true
rpc_port: 8545
p2p_port: 30303
torrent_port: 42090
log_level: "info"
genesis_file: "./genesis.json"
prune_enabled: true
prune_retain_blocks: 1024
static_peers:
  - "jamii1zvn6k33jjwlkup7acutrkf4hppms3pcwk8syw74@127.0.0.1:30304"
torrent_peers:
  - "jamii1zvn6k33jjwlkup7acutrkf4hppms3pcwk8syw74@42091"
```

### Exemplo 2: Nó Arquivador Histórico (`archiver/config.yaml`)
```yaml
data_dir: "./"
node_key_file: "./nodekey"
miner_enabled: false
is_archive_node: true
prune_enabled: false
rpc_port: 8546
p2p_port: 30310
torrent_port: 42020
log_level: "info"
genesis_file: "./genesis.json"
static_peers:
  - "jamii1z7fsyg98ejec8vhg7mdyruus0v676jd5ryxa62v@127.0.0.1:30303"
```

### Exemplo 3: Nó Leitor Stateless (RAM Only)
```yaml
data_dir: "./"
node_key_file: "./nodekey"
miner_enabled: false
stateless: true
rpc_port: 8547
p2p_port: 30311
torrent_port: 42021
log_level: "info"
genesis_file: "./genesis.json"
static_peers:
  - "jamii1z7fsyg98ejec8vhg7mdyruus0v676jd5ryxa62v@127.0.0.1:30303"
```

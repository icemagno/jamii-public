# Jamii Wallet: Exemplo de Uso do SDK em Java

Este diretório contém uma aplicação Spring Boot de exemplo que consome o **Jamii Java SDK** puro como dependência do repositório Maven local. Ela implementa uma interface web de Wallet moderna e profissional para consulta e envio de transações na Jamii Blockchain.

---

## 🚀 Funcionalidades
1. **Interface Web Premium**: Uma interface construída em HTML5/CSS3 estilizada no padrão *glassmorphism* com tema escuro (Dark Mode) e neon, disponível em `http://localhost:8080`.
2. **Dados da Conta**: Exibe dinamicamente o endereço Bech32 da carteira ativa, o caminho do arquivo de keystore carregado no backend e o saldo em tempo real consultado no nó.
3. **Envio de Transação**: Formulário integrado para transferência de fundos (Wei) para qualquer endereço Bech32 (`jamii1...`). A transação é montada, assinada localmente (offline) com o SDK e transmitida de forma segura à rede.
4. **Configurações Dinâmicas (RPC)**: Painel de configurações interativo para alterar o Host e Porta da JSON-RPC API. As preferências são persistidas no `localStorage` do navegador e repassadas ao backend nas consultas AJAX.
5. **Validador de Conexão**: Botão para testar a conectividade com o nó blockchain configurado e receber feedback instantâneo na modal de configurações.

---

## ⚙️ Configuração Inicial

As propriedades de inicialização da carteira e senha são armazenadas no arquivo `src/main/resources/application.properties`:

```properties
jamii.wallet.path=c:/magno/carteiras-jamii/alice.json
jamii.wallet.password=jamii
server.port=8080
```

- **`jamii.wallet.path`**: O caminho completo no disco do seu computador para o arquivo JSON da carteira que deseja gerenciar (se vazio ou inexistente, a aplicação fará fallback para as carteiras de teste locais `alice.json` ou `bob.json`).
- **`jamii.wallet.password`**: A senha associada ao arquivo de keystore carregado (por padrão, a senha para `alice.json` e `bob.json` é `jamii`).
- **`server.port`**: A porta de execução do servidor Tomcat embutido do Spring (padrão `8080`).

---

## ⚡ Como Compilar e Rodar

Para facilitar o processo de desenvolvimento e execução, use o script em lote fornecido na raiz deste diretório:

```bash
# Execute o script run.bat
.\run.bat
```

Este script executará as seguintes etapas de forma automática:
1. Compilará e empacotará o projeto usando `mvn clean package`.
2. Verificará a integridade do empacotamento.
3. Iniciará a aplicação Java executando `java -jar target/jamii-sdk-usage-example-0.1.jar`.

Com a aplicação rodando, acesse a interface pelo seu navegador em: **[http://localhost:8080](http://localhost:8080)**.

---

## 📇 Detalhes das APIs REST Internas
- **`GET /api/wallet?rpcUrl=HOST:PORTA`**: Retorna dados da conta remetente ativa, do saldo atual obtido via RPC e o status de conexão com o nó.
- **`POST /api/transfer`**: Recebe um payload JSON contendo o destinatário, valor (Wei) e rpcUrl, realizando a validação de fundos, assinatura do blob e transmissão à rede, retornando o hash da transação confirmada e os dados do recibo.

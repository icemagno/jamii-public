# Jamii Blockchain: Java SDK

Este diretório contém o **Jamii Java SDK**, uma biblioteca Java pura de alto desempenho projetada para interagir com a Jamii Blockchain de forma robusta e descentralizada.

## 🧱 Características do SDK
- **Independência de Frameworks**: SDK puro (sem dependência do ecossistema Spring Boot), permitindo sua fácil integração em qualquer aplicação Java.
- **Client JSON-RPC nativo**: Construído sobre o `java.net.http.HttpClient` do JDK 11+, garantindo consultas e envios rápidos de transações.
- **Segurança e Criptografia**: Integração nativa com a BouncyCastle para suporte às operações criptográficas essenciais.
- **SSZ Serialization**: Implementação do serializador SSZ para transações em conformidade com as regras da Jamii Blockchain.
- **Endereçamento Soberano**: Utilitários para decodificação e validação de chaves e endereços Bech32 (`jamii1...`).

---

## 🛠️ Requisitos
- **Java Development Kit (JDK)**: Versão 22 ou superior.
- **Maven**: Versão 3.x ou superior.

---

## 🚀 Como Compilar e Instalar Localmente

Para compilar e instalar o SDK no repositório Maven local do seu computador (`~/.m2/repository`), você pode simplesmente executar o script em lote fornecido:

```bash
# Navegue até a pasta do SDK e execute o deploy local
cd sdk/java/jamii-sdk/
.\deploy_local.bat
```

Isso executará o comando `mvn clean install`, que gerará o arquivo JAR e o disponibilizará para outros projetos no mesmo computador.

---

## 📦 Como Usar em Outros Projetos

Uma vez instalado localmente, adicione a seguinte dependência ao arquivo `pom.xml` do seu projeto Java:

```xml
<dependency>
    <groupId>com.jamii</groupId>
    <artifactId>jamii-sdk-example</artifactId>
    <version>0.1</version>
</dependency>
```

---

## 📂 Estrutura do Pacote
- **`com.jamii.sdk.rpc`**: Contém o `JamiiClient`, o ponto central para interações HTTP JSON-RPC (consultar saldo, obter nonce, transmitir transações).
- **`com.jamii.sdk.core`**: Contém o `JamiiWallet`, `JamiiKeyPair`, `JamiiSigner` e o codificador `JamiiCodec`.
- **`com.jamii.sdk.model`**: Contém os modelos de dados da blockchain (`Transaction`, `JamiiBlock`).
- **`com.jamii.sdk.bech32`**: Utilitários de codificação e decodificação do padrão Bech32 soberano.

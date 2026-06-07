# 🧠 Consenso IBFT 2.0: Arquitetura Isolada e Testes de Máquina de Estados

Este documento explica como a Jamii Blockchain implementa e testa o algoritmo de consenso IBFT 2.0 (portado do Hyperledger Besu) sem a necessidade de uma rede P2P ativa durante a fase de desenvolvimento e testes unitários.

---

## 1. O Desafio do Teste de Consenso
O IBFT 2.0 é um algoritmo de Tolerância a Falhas Bizantinas. Para que um bloco seja produzido, a rede exige um quórum de $2F + 1$ votos (onde $F$ é o número de nós maliciosos permitidos). Em uma rede de 4 nós, são necessários 3 votos para aprovar um bloco.

**O problema:** Como testar essa mecânica complexa de forma rápida, determinística e sem precisar provisionar 4 máquinas reais ou instâncias de rede locais?

**A solução:** **Injeção de Atores Virtuais** e **Isolamento da Máquina de Estados**.

---

## 2. A Máquina de Estados Isolada (O "Cérebro")
A arquitetura do consenso foi projetada com separação estrita de responsabilidades:
- O **Cérebro** (Máquina de Estados: `Controller`, `Round`, `RoundState`) processa regras matemáticas. Ele não sabe o que é um socket TCP/IP.
- Os **Ouvidos e Boca** (Interface `Transmitter` e o listener da rede) lidam com o tráfego P2P.

Ao isolar o Cérebro, podemos testá-lo injetando mensagens diretamente em suas funções de entrada (`HandleMessage`), simulando um ambiente de rede perfeito ou hostil.

---

## 3. Como o Teste Unitário Funciona na Prática

O teste `TestIBFT_FullConsensusCycle` (em `pkg/consensus/ibft/controller_test.go`) orquestra um teatro virtual com 4 identidades: **V0, V1, V2 e V3**.

### A. O Cenário
1. O teste instancia **apenas o Cérebro do nó V1**.
2. É dito ao V1: *"Você é o V1, seus pares são V0, V2, V3, e o quórum é 3"*.
3. O `Transmitter` (Boca) fornecido ao V1 é um "Mock" (um dublê) que apenas anota na memória o que o V1 tentou falar para a rede.

### B. O Teatro (Injeção de Mensagens)
O script de teste assume o papel da "Rede" (e dos outros 3 nós) e começa a injetar mensagens assinadas diretamente na função `HandleMessage` do V1:

1. **Ato 1 (Proposta):** O teste forja uma mensagem `Proposal` do líder **V0** e entrega ao V1.
   - *Ação do V1:* O Cérebro do V1 valida que V0 é o líder, aceita a proposta e usa o Mock Transmitter para "gritar" um `Prepare` para a rede. O teste verifica se o Mock registrou esse grito.
2. **Ato 2 (Votos de Prepare):** O teste forja uma mensagem `Prepare` de **V2** e entrega ao V1.
   - *Ação do V1:* O V1 conta os votos de Prepare. Com o dele (V1) e o de V2, ele atinge o quórum de Prepare (Quorum - 1 = 2 votos no IBFT 2.0). O V1 então emite um `Commit` através do Mock Transmitter.
3. **Ato 3 (Votos de Commit e Finalização):** O teste forja mensagens de `Commit` de **V0** e **V2**.
   - *Ação do V1:* O V1 acumula os commits. Quando o total chega a 3 (V1, V0, V2), o Cérebro do V1 atinge o estado `Committed` e dispara o gatilho `OnBlockFinalized`, instruindo o Motor de Execução a gravar o bloco no SSD.

---

## 4. Evolução para a Rede Real (Produção)
A beleza desta arquitetura é que o Cérebro (`Controller`) **não mudará uma única linha de código** quando formos para a rede real. 

A única alteração ocorrerá na camada externa:
- O `Transmitter` passará a empacotar os bytes e enviá-los via DevP2P ou libp2p.
- Um servidor TCP receberá os bytes da internet, verificará a assinatura e chamará o mesmo `HandleMessage(msg)` que usamos no teste.

---

## 5. Refinamentos Industriais (Sprint 3.6)
Para atingir a conformidade total com o Hyperledger Besu e garantir a segurança de uma Mainnet, a Jamii introduziu dois mecanismos cruciais:

### A. Validação Ativa de Propostas e Aceleração via Witness
Diferente de implementações simplificadas, o Cérebro da Jamii agora integra-se ao **Motor de Execução** durante a fase de Proposta. 
*   **Proposer:** Ao emitir uma `PROPOSAL`, o líder gera uma **Block Witness** (estados iniciais) que é incluída no Skeleton.
*   **Validator:** Ao receber uma `PROPOSAL`, o nó invoca `VerifyPayload(digest)`. Graças à Witness, o nó executa as transações de forma Stateless e otimista, pulando a matemática pesada da Verkle Tree (IPA/MSM) durante o round de votação. 

### B. State Promotion (Finalização Instantânea)
O motor de consenso agora utiliza o mecanismo de **State Promotion**. Quando o quórum de `COMMIT` é atingido:
1.  O sistema recupera o estado já executado e validado (Sandbox) do `PayloadPool`.
2.  Em vez de re-executar, ele promove os nós da Trie e mutações de RAM diretamente para o estado canônico.
3.  Isso reduz o tempo de "Block Sealing" em ~90%, transformando o compromisso de finalização em uma operação pura de I/O de escrita.

### C. Persistência de Seals (Crash-Resilience)
O sistema agora persiste as assinaturas binárias reais (`Seals`) de cada voto de Commit no disco. Isso garante que, se um nó sofrer um crash e reiniciar no meio de uma rodada, ele possa recuperar as provas criptográficas legítimas para finalizar o bloco.

# 🛡️ JAMII Blockchain - Especificação de Entropia e Derivação de Chaves (ENTROPY_SPEC)

**Projeto:** Jamii Blockchain  
**Status:** Homologado / Especificação Oficial  
**Versão:** 1.0.0  
**Autor:** Engenharia de Criptografia Jamii  
**Revisores:** Diretoria de Criptografia & CASNAV (Centro de Análises de Sistemas Navais)  

---

## 📋 1. Visão Geral

A segurança de uma blockchain pós-quântica reside não apenas na força dos seus algoritmos de assinatura (como o ML-DSA-65), mas fundamentalmente na **qualidade e rastreabilidade da entropia de origem** que alimenta a geração de sementes (*seeds*), chaves privadas e vetores de inicialização (*nonces*).

Este documento estabelece o padrão oficial da JAMII Blockchain para:
1. Fonte de Entropia e Geradores de Números Pseudo-Aleatórios Criptográficos (CSPRNG/DRBG).
2. Demonstração Teórica da Conservação de Entropia (Teorema de Shannon: H(f(X)) ≤ H(X)).
3. A Cadeia Completa de Derivação Híbrida (BIP-39 $\rightarrow$ Master Seed $\rightarrow$ ECDSA + ML-DSA-65).
4. Especificação de Armazenamento e Cifra de Keystore (Scrypt + AES-256-GCM).
5. O Roteiro de Integração com Geradores de Números Aleatórios Quânticos (QRNG) via CPQD/Dobslit (Fase 3).

---

## 🧮 2. Teoria da Informação & Conservação da Entropia

### 2.1 A Desigualdade do Processamento de Dados (Shannon)
Segundo o Teorema Fundamental da Teoria da Informação e a Desigualdade de Processamento de Dados:

$$\mathcal{H}(f(X)) \le \mathcal{H}(X)$$

Onde $\mathcal{H}(X)$ representa a min-entropia da fonte original e $f(X)$ representa qualquer Função de Derivação de Chave (KDF), algoritmo de Hash ou expansor pseudo-aleatório.

> **Mandato Criptográfico Jamii:** Uma KDF (como PBKDF2, HMAC ou HKDF) pode expandir o comprimento em bytes de um segredo, mas **não pode criar min-entropia inexistente na semente inicial**.

### 2.2 Orçamento de Entropia (*Entropy Budget*)

| Mnemônico (BIP-39) | Bits de Entropia Inicial | Segurança Clássica (secp256k1) | Segurança Pós-Quântica (ML-DSA-65) | Status na JAMII |
| :--- | :--- | :--- | :--- | :--- |
| **12 Palavras** | 128 bits | 128 bits (Equivalente) | Bounded at 128 bits | Suportado (Carteiras Leves) |
| **24 Palavras** | **256 bits** | **256 bits (Total)** | **192-256 bits (NIST Level 3/5)** | **Padrão Obrigatório (Validadores e Contas Soberanas)** |

* **Conclusão:** Para que o algoritmo ML-DSA-65 (NIST FIPS 204) atinja seu nível máximo de segurança pós-quântica reivindicado, as chaves soberanas e de validadores da JAMII exigem obrigatoriamente sementes geradas com **256 bits de entropia pura (24 palavras)**.

---

## 🎲 3. Arquitetura de Geradores de Entropia (CSPRNG/DRBG)

A JAMII utiliza uma arquitetura de entropia em camadas para garantir aleatoriedade criptográfica resistente a falhas e manipulação.

```
+-------------------------------------------------------------------+
|                     Camada Físico / Kernel                        |
|   Linux: getrandom(2) / /dev/urandom (ChaCha20 DRBG)               |
|   Windows: BCryptGenRandom (NIST SP 800-90A AES-CTR DRBG)         |
|   macOS/iOS: SecRandomCopyBytes / arc4random_buf                  |
+-------------------------------------------------------------------+
                                 │
                                 ▼
+-------------------------------------------------------------------+
|                   Camada de Aplicação (Go Core)                   |
|                        crypto/rand.Reader                         |
+-------------------------------------------------------------------+
                                 │
                                 ▼
+-------------------------------------------------------------------+
|               Geração de Mnemônico (pkg/wallet)                   |
|                   bip39.NewEntropy(256)                           |
+-------------------------------------------------------------------+
```

### 3.1 Conformidade com Padrões NIST
- **NIST SP 800-90A Rev. 1:** O gerador subjacente do SO (`crypto/rand.Reader`) implementa DRBGs aprovados pelo NIST (AES-CTR DRBG ou Hash/HMAC DRBG com *reseed* automático pelo kernel).
- **NIST SP 800-90B:** Requisitos, caracterização e validação de fontes de entropia não-determinísticas (*Noise Sources* / *Entropy Sources*), incluindo testes contínuos de saúde (*Health Tests*) executados pelo kernel do sistema operacional antes de alimentar a aplicação.
- **NIST SP 800-90C:** Recomendação para construções de Geradores de Números Aleatórios (RBGs - *Random Bit Generators*) agregando fontes de entropia validadas (SP 800-90B) com DRBGs (SP 800-90A) para formação de RBGs Híbridos/Naturais (NRBG) robustos.

### 3.2 Higienização Física da RAM (*Memory Wiping*)
Para prevenir vazamento de segredos em *dumps* de memória ou ataques de inicialização a frio (*Cold Boot Attacks*), o pacote `pkg/wallet` executa a zeragem física de buffers via loops explícitos e amarras de compilação:

```go
func (w *Wallet) Wipe() {
    if w.Mnemonic != nil {
        for i := range w.Mnemonic { w.Mnemonic[i] = 0 }
        runtime.KeepAlive(w.Mnemonic)
    }
    if w.Private != nil {
        w.Private.Zero()
    }
}
```

---

## ⛓️ 4. Cadeia Completa de Derivação Híbrida (Pipeline de Chaves)

A criação de uma conta na JAMII segue uma cadeia de derivação estritamente determinística, garantindo **Identidade Unificada**: a partir de um único mnemônico, deriva-se a chave clássica de compatibilidade Ethereum e a chave soberana pós-quântica.

```
[ Entropia Inicial: 256 bits ]
              │
              ▼
[ Mnemônico BIP-39: 24 Palavras ]
              │
              ▼ PBKDF2-HMAC-SHA512 (2048 iterações, Salt = "mnemonic" + passphrase)
[ Master Seed: 512 bits ]
              │
      ┌───────┴────────────────────────────────────────┐
      │                                                │
      │ BIP-44 Derivation Path                         │ HMAC-SHA512 ("Jamii ML-DSA Seed")
      │ m/44'/60'/0'/0/0                               │ [0..31]
      ▼                                                ▼
[ ECDSA Private Key ]                         [ 256-bit PQC Seed ]
 (secp256k1 - 256 bits)                               │
      │                                                ▼
      │                                     [ ML-DSA-65 Private Key ]
      │                                      (NIST FIPS 204 - Lattice)
      │                                                │
      └───────────────────────┬────────────────────────┘
                              │
                              ▼
                [ Par Híbrido (pkg/crypto/signer) ]
                              │
              ┌───────────────┴───────────────┐
              ▼                               ▼
     [ Mirror Address ]               [ Sovereign Address ]
       Hex: 0x1234...                   Bech32: jamii1...
```

### 4.1 Especificação Matemática dos Passos

1. **Entropia de Origem ($E$):**
   $$E \leftarrow \text{CSPRNG}(256 \text{ bits})$$

2. **Mnemônico BIP-39 ($M$):**
   $$M = \text{BIP-39-Encode}(E) \quad \Rightarrow \quad \text{24 palavras}$$

3. **Semente Mestre ($S$):**
   $$S = \text{PBKDF2-HMAC-SHA512}(M, \text{"mnemonic"} \parallel \text{passphrase}, \text{iterations}=2048, \text{len}=64 \text{ bytes})$$

4. **Derivação Clássica (Ethereum Mirror - secp256k1):**
   $$SK_{\text{trad}} = \text{HD-Derive-BIP44}(S, \text{"m/44'/60'/0'/0/0"})$$
   $$\text{Address}_{\text{Mirror}} = \text{Keccak-256}(\text{PubKey}(SK_{\text{trad}}))[12..31] \quad (\text{Format Hex } 0x...)$$

5. **Derivação PQC (Jamii Sovereign - ML-DSA-65):**
   $$PQC_{\text{seed}} = \text{HMAC-SHA512}(\text{Key}=\text{"Jamii ML-DSA Seed"}, \text{Data}=S)[0..31]$$
   $$SK_{\text{quant}} = \text{ML-DSA-65.NewKeyFromSeed}(PQC_{\text{seed}})$$
   $$\text{Address}_{\text{Sovereign}} = \text{Bech32}(\text{Blake2b-256}(\text{PubKey}(SK_{\text{trad}}) \parallel \text{PubKey}(SK_{\text{quant}}))) \quad (\text{Format } jamii1...)$$
   * **Nota de Nomenclatura:** A semente $PQC_{\text{seed}}$ é uma semente determinística de 256 bits gerada especificamente para derivação de chaves pós-quânticas (ML-DSA-65), derivada da aleatoriedade inicial da master seed.

---

## 🔒 5. Proteção de Armazenamento (Cifra Keystore JSON)

Quando a carteira é salva em disco no formato Keystore JSON (Versão 3), a chave privada híbrida é protegida por cifragem simétrica derivada de senha:

### 5.1 Parâmetros KDF (Scrypt)
- **Custo de Memória/CPU ($N$):** $131\,072$ ($2^{17}$)
- **Tamanho de Bloco ($r$):** $8$
- **Paralelização ($p$):** $1$
- **Tamanho da Chave Derivada ($dklen$):** 32 bytes (256 bits)
- **Salt:** 32 bytes aleatórios gerados via `crypto/rand` por Keystore.

### 5.2 Parâmetros de Cifra Simétrica (AES-GCM)
- **Algoritmo:** AES-256 no modo GCM (*Galois/Counter Mode* - Cifragem Autenticada).
- **Tamanho do Nonce (IV):** 96 bits (12 bytes) aleatórios gerados via `crypto/rand`.
- **Autenticação Integre:** Tag GCM integrada para prevenir adulteração do *ciphertext*.

---

## ⚛️ 6. Roteiro de Integração QRNG (Fase 3 - CPQD / Dobslit)

Para elevar a JAMII ao padrão máximo de certificação de segurança internacional, a Fase 3 introduzirá o suporte nativo a **Geradores de Números Aleatórios Quânticos (QRNG - Quantum Random Number Generators)** através de parcerias e testbeds do **CPQD** e **Dobslit**.

```
+------------------------+      +------------------------+      +------------------------+
|   OS Kernel CSPRNG     |      |    Hardware TRNG       |      |   CPQD / Dobslit QRNG  |
|  (/dev/urandom DRBG)   |      |  (TPM / CPU RDSEED)    |      | (Laser / Vacuum Noise) |
+------------------------+      +------------------------+      +------------------------+
            │                               │                               │
            └───────────────────────┬───────┴───────────────────────────────┘
                                    │
                                    ▼
                 +--------------------------------------+
                 |      Entropy Aggregator (HKDF)       |
                 | HKDF-Extract(Salt, OS || TRNG || QRNG) |
                 +--------------------------------------+
                                    │
                                    ▼
                 +--------------------------------------+
                 |   Quantum-Seeded Master Key Generation |
                 |     Validadores & HSMs Institucionais|
                 +--------------------------------------+
```

### 6.1 Arquitetura do Agregador de Entropia Híbrido (*Entropy Aggregator*)
Nós Validadores, Proposores IBFT e Módulos de Custódia Soberana utilizarão o agregador de entropia:

$$\text{PRK} = \text{HKDF-Extract}(\text{Salt}=\text{Random}_{32}, \text{IKM} = \text{CSPRNG}_{\text{OS}} \parallel \text{TRNG}_{\text{Hardware}} \parallel \text{QRNG}_{\text{CPQD}})$$

* **Garantia de Segurança Teórica e Conformidade NIST SP 800-90C:** Em conformidade com a NIST SP 800-90C para RBGs Híbridos/Naturais (NRBG), a combinação de fontes via condicionamento e extração de entropia (`HKDF-Extract`) garante defesa em profundidade. Mesmo no caso hipotético de comprometimento, calibração falha ou *backdoor* em duas das três fontes, a min-entropia resultante satisfaz $\mathcal{H}_\infty(\text{PRK}) \ge \max(\mathcal{H}_\infty(X_i))$, sendo tão segura quanto a fonte mais forte e não-comprometida do conjunto.

### 6.2 Casos de Uso do QRNG na JAMII
1. **Geração de Chaves de Validador:** Inicialização de nós validadores da rede com sementes provadamente quânticas.
2. **HSM & Custódia Corporativa:** Integração via PKCS#11 com módulos HSM alimentados por hardware QRNG nacional/internacional.
3. **Entropy-as-a-Service (EaaS) em Smart Contracts:** Disponibilização de aleatoriedade quântica auditável para dApps, sorteios e oráculos por meio de pré-compilados dedicados no topo do espaço de endereçamento da JAMII (`0x00...00ffffffffffffffffffffffffffffffffffffff`).

### 6.3 Diretiva de Independência de Hardware e Degradação Graciosa (*Graceful Fallback*)

> [!IMPORTANT]
> **Mandato de Autonomia de Nó:** A JAMII Blockchain **NÃO** possui dependência obrigatória de hardware quântico ou dispositivos físicos especializados. Qualquer computador pessoal, servidor em nuvem (AWS, GCP, DigitalOcean), contêiner Docker ou ambiente virtualizado sem hardware quântico ou HSM pode executar um nó validador completo com 100% de segurança e conformidade.

* **Fallback Automático e Transparente:** O `EntropyAggregator` detecta dinamicamente a presença de fontes de hardware. Caso o QRNG ou o TRNG de hardware estejam ausentes ou indisponíveis, o sistema executa o fallback gracioso para o **CSPRNG do Kernel do Sistema Operacional** (`crypto/rand.Reader`), que fornece os **256 bits de entropia pura** exigidos (24 palavras BIP-39).
* **Papel do QRNG:** O ruído quântico (CPQD/Dobslit) atua como um aditivo de alta segurança (*Institutional Grade*) voltado para certificação de validadores institucionais/governamentais e para *Entropy-as-a-Service (EaaS)* em smart contracts.

#### Matriz de Compatibilidade de Ambientes

| Ambiente de Execução do Nó | Fontes de Entropia Agregadas | Status de Execução | Nível de Segurança da Semente |
| :--- | :--- | :---: | :--- |
| **Dev Local / Docker / VPS Nuvem** | `CSPRNG (OS)` | 🟢 **100% Operacional** | **256 bits** (BIP-39 24 Palavras) |
| **Servidor Bare-Metal (CPU Moderna)** | `CSPRNG (OS)` $\parallel$ `TRNG (CPU RDSEED)` | 🟢 **100% Operacional** | **256 bits+** (Dupla Proteção) |
| **Validador Institucional (QRNG CPQD)** | `CSPRNG (OS)` $\parallel$ `TRNG` $\parallel$ `QRNG (Quântico)` | 🟢 **100% Operacional** | **256 bits+** (Certificação Quântica) |

---

## 🧪 7. Auditoria e Bateria de Testes Criptográficos

O código de manipulação de entropia e chaves deve passar continuadamente pelos seguintes testes automatizados no pipeline da JAMII:

1. **Testes Estatísticos de Aleatoriedade:** Validação da saída do gerador via suíte NIST SP 800-22 e Dieharder.
2. **Testes Adversariais (`pkg/crypto/adversarial_test.go`):** Injeção de assinaturas corrompidas e verificações sob condições extremas.
3. **Higienização de Memória (`pkg/wallet/wallet_test.go`):** Validação de que buffers zerados pelo método `Wipe()` não contêm resíduos de chave após desalocação.

---

## 🛠️ 8. Impacto Previsto e Mapeamento de Implementação

Esta seção fornece a análise de impacto técnico detalhada, estimativa de complexidade por módulo e o mapeamento de arquivos afetados na JAMII Blockchain para a plena execução desta especificação de entropia.

### 8.1 Avaliação Geral de Complexidade Arquitetural
* **Grau Global de Dificuldade:** **MÉDIO (Nível 3 de 5)**.
* **Justificativa:** A JAMII Blockchain já possui motores sólidos de criptografia (`pkg/crypto/signer`), pré-compilados (`pkg/vm/precompile.go`) e carteiras (`pkg/wallet`). A implementação é cirúrgica, exigindo desacoplamento modular e rigor na preservação das invariantes de consenso e determinismo da EVM.
* **Garantia de Independência de Hardware:** O design do `EntropyAggregator` impõe o padrão de fallback gracioso, garantindo zero acoplamento rígido com placas de hardware ou serviços remotos de QRNG.

---

### 8.2 Mapeamento de Módulos e Arquivos Afetados

#### A. Trava Obrigatória de 256 bits (24 Palavras BIP-39)
> [!IMPORTANT]
> **Alerta de Proteção de Módulo Homologado:** O pacote `pkg/wallet` é um módulo homologado (Diretiva 1 da Jamii). Alterações em `wallet.go` exigem aprovação prévia e justificativa técnica de conformidade.

1. **[`pkg/wallet/wallet.go`](file:///c:/Magno/Projetos/jamii/pkg/wallet/wallet.go):**
   * **Alterações:** Adicionar validação estrita de tamanho nos construtores `NewWalletFromMnemonic` e `NewEntropy`, forçando 256 bits de entropia inicial (24 palavras) para identidades do tipo `Validator` e `Sovereign`. Emitir erro `ErrInvalidEntropyLength` caso mnemônicos de 12 palavras sejam fornecidos para contas institucionais.
   * **Higienização de Nomenclatura:** Atualizar comentários e variáveis internas de `quantSeed` / `Quantum Seed` para `pqcSeed` / `PQC Seed`, alinhando à derivação determinística para o ML-DSA-65.
2. **`cmd/jamii` (Framework CLI):**
   * **Alterações:** Atualizar os comandos `jamii wallet create` e `jamii validator keygen` para gerar por padrão mnemônicos BIP-39 de 24 palavras (256 bits) e aplicar a validação no parse de entrada de comandos CLI.
3. **SDKs e UIs (`jamii-wallet` & Java SDK):**
   * **Alterações:** Atualizar as bibliotecas de cliente para sugerir e validar 24 palavras no fluxo de criação de contas institucionais/validadores.

#### B. Agregador de Entropia Híbrido (`EntropyAggregator`) & Driver QRNG (CPQD / Dobslit)
1. **`pkg/crypto/entropy/aggregator.go` (NOVO MÓDULO):**
   * **Responsabilidade:** Implementar o construto $\text{PRK} = \text{HKDF-Extract}(\text{Salt}, \text{OS} \parallel \text{TRNG} \parallel \text{QRNG})$.
   * **Entradas:** `crypto/rand` (Kernel SO), instruções CPU `RDSEED`/`RDRAND` (Hardware TRNG) e o driver QRNG.
2. **`pkg/crypto/entropy/qrng_client.go` (NOVO MÓDULO):**
   * **Responsabilidade:** Conector HTTP/REST ou PKCS#11 para os testbeds do CPQD/Dobslit.
   * **Mecanismo de Resiliência (*Circuit Breaker*):** Timeout agressivo (ex: 200ms) com fallback gracioso automático para `OS_CSPRNG || Hardware_TRNG` para evitar que a indisponibilidade de rede ou falha no servidor QRNG paralise o boot ou a proposta de bloco do nó validador.

#### C. Pré-compilado Entropy-as-a-Service (EaaS) na JamiiVM
1. **`pkg/core/types/header.go`:**
   * **Desafio Fundamental - Determinismo da EVM:** A execução de Smart Contracts na JamiiVM deve ser 100% determinística em todos os nós da rede. Um contrato inteligente não pode realizar chamadas I/O assíncronas ao QRNG durante a execução.
   * **Solução:** O Proposor da rodada IBFT captura a entropia quântica/agregada no início do bloco e injeta a semente no campo `RandomnessSeed` (no cabeçalho do bloco), validado pelo quórum.
2. **[`pkg/vm/precompile.go`](file:///c:/Magno/Projetos/jamii/pkg/vm/precompile.go):**
   * **Alterações:** Registrar o novo pré-compilado soberano de EaaS no endereço topo de espaço (ex: `0x00000000000000000000000000000000fffffffc`). Ao ser invocado por uma chamada `staticcall`, o pré-compilado lê deterministicamente o `RandomnessSeed` do contexto do bloco atual e retorna aleatoriedade quântica auditável para os contratos inteligentes.

---

### 8.3 Roteiro de Execução por Fases

```
┌───────────────────────────────────────────────────────────────────────────┐
│ Fase 1: Trava de Entropia 256-bit & Ajuste PQC Seed                       │
│ ➔ Validação 24 palavras na CLI/SDK + Atualização em pkg/wallet            │
└───────────────────────────────────────────────────────────────────────────┘
                                     │
                                     ▼
┌───────────────────────────────────────────────────────────────────────────┐
│ Fase 2: Módulo Agregador & Conector QRNG CPQD/Dobslit                     │
│ ➔ Desenvolvimento de pkg/crypto/entropy com Fallback/Circuit-Breaker      │
└───────────────────────────────────────────────────────────────────────────┘
                                     │
                                     ▼
┌───────────────────────────────────────────────────────────────────────────┐
│ Fase 3: Pré-compilado EaaS Determinístico na JamiiVM                      │
│ ➔ Injeção de semente no Header + Registro de Precompile 0x...fffffffc     │
└───────────────────────────────────────────────────────────────────────────┘
```


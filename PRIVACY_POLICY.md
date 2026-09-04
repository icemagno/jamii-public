# Privacy Policy for Jamii Wallet

**Last Updated:** September 4, 2026

## 1. Introduction

Welcome to **Jamii Wallet** ("we," "our," or "us"). Jamii Wallet is a non-custodial, post-quantum secure digital asset wallet application designed for the Jamii Blockchain network. 

We respect your privacy and are committed to protecting your personal data. This Privacy Policy explains how Jamii Wallet handles your information, what permissions are required, and how your data remains under your absolute control.

By downloading, installing, or using Jamii Wallet, you agree to the terms of this Privacy Policy.

---

## 2. Non-Custodial Architecture & Zero Data Collection

Jamii Wallet is built on a strict **non-custodial architecture**:

- **No Personal Identifiable Information (PII):** We do not collect, store, or transmit your name, email address, phone number, physical address, IP address, or any other personal identifier.
- **No User Tracking or Analytics:** We do not track your activity inside the application, nor do we use third-party analytics, telemetry, or advertising frameworks.
- **No Server-Side Key Custody:** Your private keys, post-quantum keypairs (ML-DSA-65, Falcon-512), classical keys (secp256k1), and recovery phrases (BIP-39 seed phrases) are generated and stored **locally on your device**. We never have access to your keys or funds.

---

## 3. Device Permissions & How They Are Used

Jamii Wallet requests specific device permissions strictly to enable wallet functionalities:

### A. Camera Access (`CAMERA`)
- **Purpose:** Used exclusively to scan QR codes for recipient blockchain addresses, contract payload data, or connection requests.
- **Data Handling:** Camera frames are processed strictly in real-time on your device. Video streams and captured images are **never** recorded, stored, or transmitted to any external server.

### B. Internet Access (`INTERNET`)
- **Purpose:** Required to interact directly with public Jamii Blockchain JSON-RPC nodes to query account balances, estimate transaction fees, broadcast signed transactions, and retrieve block data.

### C. Secure Storage (`expo-secure-store` / Hardware KeyStore)
- **Purpose:** Used to encrypt and store your private keys, seed phrases, and wallet state safely on your device using OS-level secure enclaves / hardware keystore facilities.

### D. Notifications & Haptic Feedback (`POST_NOTIFICATIONS`, `VIBRATE`)
- **Purpose:** Used for local app feedback (haptic response on keypresses or successful QR code scans) and local transaction status updates.

---

## 4. Blockchain Transactions & Public Data

When you perform transactions using Jamii Wallet:

- **Public Ledger:** Signed blockchain transactions are submitted to the decentralized Jamii Blockchain network. Transaction details (including sender address, recipient address, transfer amount, timestamp, and transaction hash) become publicly visible on the blockchain ledger.
- **Anonymity:** Your blockchain address is cryptographically derived and is not linked to your personal identity by Jamii Wallet.

---

## 5. Third-Party Services & Links

Jamii Wallet does not share any data with third parties. If you interact with external services (such as dApps, decentralized exchanges, or block explorers linked via external URLs), your interactions with those third parties are governed by their respective privacy policies.

---

## 6. Security & User Responsibilities

Because Jamii Wallet is non-custodial:
- You are solely responsible for backing up your **BIP-39 recovery phrase** (seed phrase) and keeping your device secure.
- If you lose your recovery phrase, neither Jamii Wallet developers nor anyone else can recover your account or restore access to your assets.

---

## 7. Children's Privacy

Jamii Wallet is not directed toward children under the age of 18 (or the applicable age of majority in your jurisdiction). We do not knowingly collect information from children.

---

## 8. Changes to This Privacy Policy

We may update this Privacy Policy from time to time to reflect app updates or regulatory requirements. Any changes will be published in this document with an updated "Last Updated" date.

---

## 9. Contact Us

If you have any questions or concerns regarding this Privacy Policy, please contact us via our official project channels:

- **Project Repository:** [https://github.com/icemagno/jamii](https://github.com/icemagno/jamii)
- **Developer Contact:** [magno.mabreu@gmail.com](mailto:magno.mabreu@gmail.com)

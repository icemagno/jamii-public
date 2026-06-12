package com.jamii.sdk.example;

import com.jamii.sdk.core.JamiiWallet;
import com.jamii.sdk.rpc.JamiiClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {

    @Value("${jamii.wallet.path}")
    private String walletPath;

    @Value("${jamii.wallet.password}")
    private String walletPassword;

    @GetMapping("/wallet")
    public Map<String, Object> getWalletInfo(@RequestParam(value = "rpcUrl", required = false) String rpcUrl) {
        Map<String, Object> response = new HashMap<>();
        try {
            String password = this.walletPassword != null ? this.walletPassword : "jamii";
            
            String targetRpc = (rpcUrl != null && !rpcUrl.trim().isEmpty()) ? rpcUrl : "http://localhost:8545";
            JamiiClient client = new JamiiClient(targetRpc);

            File walletFile = null;
            if (this.walletPath != null && !this.walletPath.isEmpty()) {
                walletFile = new File(this.walletPath);
            }

            if (walletFile == null || !walletFile.exists()) {
                // Fallback local
                walletFile = new File("./alice.json");
                if (!walletFile.exists()) {
                    walletFile = new File("../alice.json");
                }
            }

            if (!walletFile.exists()) {
                response.put("error", "Arquivo keystore principal nao encontrado!");
                return response;
            }

            JamiiWallet wallet = new JamiiWallet(walletFile, password);
            String address = wallet.getAddress().toJamii1();
            
            BigInteger balance = BigInteger.ZERO;
            String errorMsg = null;
            try {
                balance = client.getBalance(address);
            } catch (Exception e) {
                // Se o no nao estiver rodando localmente
                errorMsg = e.getMessage();
            }

            response.put("address", address);
            response.put("balance", balance.toString());
            response.put("keystorePath", walletFile.getAbsolutePath());
            if (errorMsg != null) {
                response.put("nodeStatus", "Offline (" + errorMsg + ")");
            } else {
                response.put("nodeStatus", "Online");
            }
        } catch (Exception e) {
            response.put("error", e.getMessage());
        }
        return response;
    }

    @PostMapping("/transfer")
    public Map<String, Object> transfer(@RequestBody TransferRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String toAddress = request.getTo();
            String valueStr = request.getValue();
            String rpcUrl = request.getRpcUrl();

            if (toAddress == null || toAddress.trim().isEmpty()) {
                throw new IllegalArgumentException("Endereco de destino obrigatorio!");
            }
            if (valueStr == null || valueStr.trim().isEmpty()) {
                throw new IllegalArgumentException("Valor da transferencia obrigatorio!");
            }
            BigInteger transferValue = new BigInteger(valueStr);

            String password = this.walletPassword != null ? this.walletPassword : "jamii";
            
            String targetRpc = (rpcUrl != null && !rpcUrl.trim().isEmpty()) ? rpcUrl : "http://localhost:8545";
            JamiiClient client = new JamiiClient(targetRpc);

            File walletFile = null;
            if (this.walletPath != null && !this.walletPath.isEmpty()) {
                walletFile = new File(this.walletPath);
            }

            if (walletFile == null || !walletFile.exists()) {
                walletFile = new File("./alice.json");
                if (!walletFile.exists()) {
                    walletFile = new File("../alice.json");
                }
            }

            if (!walletFile.exists()) {
                throw new RuntimeException("Arquivo keystore principal nao encontrado!");
            }

            JamiiWallet wallet = new JamiiWallet(walletFile, password);
            String fromAddress = wallet.getAddress().toJamii1();

            // 1. Validar saldo
            BigInteger balance = client.getBalance(fromAddress);
            
            // Taxas estimadas
            long nonce = client.getTransactionCount(fromAddress);
            long chainId = client.getChainId();
            BigInteger baseFee = client.getGasPrice();
            BigInteger priorityFee = BigInteger.valueOf(100000000L); // 0.1 Gwei
            BigInteger maxFee = baseFee.add(priorityFee);
            long gasLimit = 21000;
            
            BigInteger gasCost = maxFee.multiply(BigInteger.valueOf(gasLimit));
            BigInteger totalNeeded = transferValue.add(gasCost);

            if (balance.compareTo(totalNeeded) < 0) {
                throw new RuntimeException("Saldo insuficiente! Necessario: " + totalNeeded + " Wei (Valor + Taxas), Saldo: " + balance + " Wei");
            }

            // 2. Criar Transacao
            com.jamii.sdk.model.Transaction tx = new com.jamii.sdk.model.Transaction();
            tx.nonce = nonce;
            tx.chainId = chainId;
            tx.gasLimit = gasLimit;
            tx.maxPriorityFeePerGas = priorityFee;
            tx.maxFeePerGas = maxFee;
            tx.value = transferValue;
            tx.to = toAddress;
            tx.pubKey = wallet.getKeyPair().getHybridPublicKey();

            // 3. Assinar
            com.jamii.sdk.core.JamiiSigner.signTransaction(tx, wallet.getKeyPair());

            // 4. Codificar SSZ e Enviar
            byte[] encodedTx = com.jamii.sdk.core.JamiiCodec.encodeSsz(tx);
            String txHash = client.sendRawTransaction(encodedTx);

            response.put("txHash", txHash);
            response.put("status", "pending");
            
            // 5. Polling por recibo (timeout 10s)
            Map receipt = null;
            for (int i = 0; i < 10; i++) {
                try {
                    receipt = client.getTransactionReceipt(txHash);
                    if (receipt != null) break;
                } catch (Exception ignored) {}
                Thread.sleep(1000);
            }

            if (receipt != null) {
                String status = (String) receipt.get("status");
                if ("0x1".equals(status)) {
                    response.put("status", "success");
                    response.put("message", "Transacao executada com sucesso!");
                } else {
                    response.put("status", "failed");
                    response.put("message", "Transacao falhou na execucao da blockchain.");
                }
                response.put("receipt", receipt);
            } else {
                response.put("status", "timeout");
                response.put("message", "Transacao transmitida, mas o recibo nao foi gerado a tempo (timeout). Verifique em instantes.");
            }

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
        }
        return response;
    }

    public static class TransferRequest {
        private String to;
        private String value;
        private String rpcUrl;

        public String getTo() {
            return to;
        }

        public void setTo(String to) {
            this.to = to;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getRpcUrl() {
            return rpcUrl;
        }

        public void setRpcUrl(String rpcUrl) {
            this.rpcUrl = rpcUrl;
        }
    }
}

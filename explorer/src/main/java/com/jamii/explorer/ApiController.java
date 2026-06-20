package com.jamii.explorer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import com.jamii.sdk.core.JamiiWallet;
import com.jamii.sdk.core.JamiiSigner;
import com.jamii.sdk.core.JamiiCodec;
import com.jamii.sdk.crypto.JamiiKeyPair;
import com.jamii.sdk.address.JamiiAddress;
import com.jamii.sdk.rpc.JamiiClient;
import jakarta.servlet.http.HttpSession;
import java.io.File;
import java.io.FileWriter;
import java.math.BigInteger;
import org.springframework.beans.factory.annotation.Value;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // Permite chamadas de qualquer origem (CORS)
public class ApiController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${jamii.jsonrpc.url:http://localhost:8545}")
    private String jsonRpcUrl;

    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        try {
            Long latestBlock = jdbcTemplate.queryForObject("SELECT COALESCE(MAX(height), 0) FROM blocks", Long.class);
            Long totalTxs = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM transactions", Long.class);
            Long totalGas = jdbcTemplate.queryForObject("SELECT COALESCE(SUM(gas_used), 0) FROM blocks", Long.class);
            Long totalContracts = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM account_flat WHERE code_hash IS NOT NULL " +
                "AND code_hash != '0xc5d2460186f7233c927e7db2dcc703c0e500b653ca82273b7bfad8045d85a470' " +
                "AND code_hash != 'c5d2460186f7233c927e7db2dcc703c0e500b653ca82273b7bfad8045d85a470' " +
                "AND code_hash != '0x0000000000000000000000000000000000000000000000000000000000000000' " +
                "AND code_hash != '0000000000000000000000000000000000000000000000000000000000000000' " +
                "AND code_hash != ''", 
                Long.class
            );
            Long activeValidators = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM identities", Long.class);

            double tps = 0.0;
            if (latestBlock != null && latestBlock > 0) {
                // 1. Obter os últimos 10 blocos que contêm transações
                List<Map<String, Object>> activeBlocks = jdbcTemplate.queryForList(
                    "SELECT timestamp, tx_count, height FROM blocks WHERE tx_count > 0 ORDER BY height DESC LIMIT 10"
                );

                if (!activeBlocks.isEmpty()) {
                    long totalActiveTxs = 0;
                    for (Map<String, Object> b : activeBlocks) {
                        totalActiveTxs += ((Number) b.get("tx_count")).longValue();
                    }

                    long newestTs = ((Number) activeBlocks.get(0).get("timestamp")).longValue();
                    long oldestHeight = ((Number) activeBlocks.get(activeBlocks.size() - 1).get("height")).longValue();

                    // Buscar timestamp do bloco anterior ao mais antigo do lote ativo
                    Long prevTs = null;
                    try {
                        prevTs = jdbcTemplate.queryForObject(
                            "SELECT timestamp FROM blocks WHERE height = ?",
                            Long.class,
                            oldestHeight - 1
                        );
                    } catch (Exception ignored) {}

                    long duration = 0;
                    if (prevTs != null) {
                        duration = newestTs - prevTs;
                    } else {
                        long oldestTs = ((Number) activeBlocks.get(activeBlocks.size() - 1).get("timestamp")).longValue();
                        duration = newestTs - oldestTs;
                    }

                    if (duration > 0) {
                        tps = (double) totalActiveTxs / duration;
                    } else {
                        // Auto-detectar o blockPeriod médio da rede a partir dos timestamps gravados
                        double blockPeriod = 2.0; // Fallback extremo se o histórico for insuficiente
                        try {
                            List<Map<String, Object>> recentTimes = jdbcTemplate.queryForList(
                                "SELECT timestamp FROM blocks ORDER BY height DESC LIMIT 10"
                            );
                            if (recentTimes.size() > 1) {
                                long tNew = ((Number) recentTimes.get(0).get("timestamp")).longValue();
                                long tOld = ((Number) recentTimes.get(recentTimes.size() - 1).get("timestamp")).longValue();
                                double diff = tNew - tOld;
                                if (diff > 0) {
                                    blockPeriod = diff / (recentTimes.size() - 1);
                                }
                            }
                        } catch (Exception ignored) {}

                        tps = (double) totalActiveTxs / (activeBlocks.size() * blockPeriod);
                    }

                    // Regra de Resfriamento: se o bloco ativo mais recente ocorreu há mais de 30s do último bloco geral
                    Long latestBlockTs = jdbcTemplate.queryForObject(
                        "SELECT timestamp FROM blocks WHERE height = ?",
                        Long.class,
                        latestBlock
                    );
                    if (latestBlockTs != null && (latestBlockTs - newestTs) > 30) {
                        tps = 0.0; // Rede em repouso
                    }
                }
            }

            stats.put("latestBlock", latestBlock != null ? latestBlock : 0);
            stats.put("totalTransactions", totalTxs != null ? totalTxs : 0);
            stats.put("totalGasUsed", totalGas != null ? totalGas : 0);
            stats.put("totalContracts", totalContracts != null ? totalContracts : 0);
            stats.put("activeValidators", activeValidators != null ? activeValidators : 0);
            stats.put("tps", tps);
            stats.put("status", "ONLINE");
        } catch (Exception e) {
            stats.put("status", "ERROR");
            stats.put("error", e.getMessage());
        }
        return stats;
    }

    @GetMapping("/blocks")
    public List<Map<String, Object>> getBlocks() {
        return jdbcTemplate.queryForList(
            "SELECT height, hash, parent_hash, state_root, timestamp, validator_address, base_fee, gas_used, tx_count " +
            "FROM blocks ORDER BY height DESC LIMIT 10"
        );
    }

    @GetMapping("/blocks/{height}")
    public Map<String, Object> getBlockByHeight(@PathVariable Long height) {
        try {
            Map<String, Object> block = jdbcTemplate.queryForMap(
                "SELECT height, hash, parent_hash, state_root, timestamp, validator_address, base_fee, gas_used, tx_count " +
                "FROM blocks WHERE height = ?", height
            );
            List<Map<String, Object>> txs = jdbcTemplate.queryForList(
                "SELECT hash, block_height, tx_index, sender, receiver, value, gas_price, gas_limit, nonce " +
                "FROM transactions WHERE block_height = ? ORDER BY tx_index ASC", height
            );
            block.put("transactions", txs);
            return block;
        } catch (EmptyResultDataAccessException e) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", "Block not found");
            return err;
        }
    }

    @GetMapping("/txs")
    public List<Map<String, Object>> getTransactions() {
        return jdbcTemplate.queryForList(
            "SELECT hash, block_height, tx_index, sender, receiver, value, gas_price, gas_limit, nonce " +
            "FROM transactions ORDER BY block_height DESC, tx_index DESC LIMIT 10"
        );
    }

    @GetMapping("/txs/{hash}")
    public Map<String, Object> getTransactionByHash(@PathVariable String hash) {
        try {
            return jdbcTemplate.queryForMap(
                "SELECT t.hash, t.block_height, t.tx_index, t.sender, t.receiver, t.value, t.gas_price, t.gas_limit, t.nonce, encode(t.data, 'hex') as data_hex, " +
                "r.status, r.gas_used, r.cumulative_gas " +
                "FROM transactions t LEFT JOIN receipts r ON t.hash = r.tx_hash " +
                "WHERE t.hash = ? OR t.hash = ?", hash, "0x" + (hash.startsWith("0x") ? hash.substring(2) : hash)
            );
        } catch (EmptyResultDataAccessException e) {
            try {
                com.jamii.sdk.rpc.JamiiClient client = new com.jamii.sdk.rpc.JamiiClient(jsonRpcUrl);
                Map receipt = client.getTransactionReceipt(hash);
                if (receipt != null) {
                    Map<String, Object> tx = new HashMap<>();
                    tx.put("hash", receipt.get("transactionHash"));
                    
                    String blockNumHex = (String) receipt.get("blockNumber");
                    long blockHeight = blockNumHex.startsWith("0x") ? Long.decode(blockNumHex) : Long.parseLong(blockNumHex);
                    tx.put("block_height", blockHeight);
                    
                    String txIndexHex = (String) receipt.get("transactionIndex");
                    long txIndex = txIndexHex.startsWith("0x") ? Long.decode(txIndexHex) : Long.parseLong(txIndexHex);
                    tx.put("tx_index", txIndex);
                    
                    tx.put("sender", receipt.get("from"));
                    tx.put("receiver", receipt.get("to"));
                    tx.put("value", "0");
                    tx.put("gas_price", "0");
                    tx.put("gas_limit", "0");
                    tx.put("nonce", 0);
                    tx.put("data_hex", "");
                    
                    String statusHex = (String) receipt.get("status");
                    int status = statusHex.startsWith("0x") ? Integer.decode(statusHex) : Integer.parseInt(statusHex);
                    tx.put("status", status);
                    
                    String gasUsedHex = (String) receipt.get("gasUsed");
                    long gasUsed = gasUsedHex.startsWith("0x") ? Long.decode(gasUsedHex) : Long.parseLong(gasUsedHex);
                    tx.put("gas_used", gasUsed);
                    
                    String cumGasHex = (String) receipt.get("cumulativeGasUsed");
                    if (cumGasHex == null) {
                        cumGasHex = (String) receipt.get("cumulative_gas");
                    }
                    long cumGas = cumGasHex != null ? (cumGasHex.startsWith("0x") ? Long.decode(cumGasHex) : Long.parseLong(cumGasHex)) : gasUsed;
                    tx.put("cumulative_gas", cumGas);
                    
                    return tx;
                }
            } catch (Exception rpcEx) {
                // Silently ignore and fallback
            }
            Map<String, Object> err = new HashMap<>();
            err.put("error", "Transaction not found");
            return err;
        }
    }

    @GetMapping("/accounts/{address}")
    public Map<String, Object> getAccount(@PathVariable String address) {
        Map<String, Object> account;
        try {
            account = new HashMap<>(jdbcTemplate.queryForMap(
                "SELECT address, balance, nonce, CASE WHEN code_hash LIKE '0x%' THEN substring(code_hash from 3) ELSE code_hash END as code_hash_hex " +
                "FROM account_flat WHERE address = ? OR mirror_address = ?", address, address
            ));
        } catch (EmptyResultDataAccessException e) {
            account = new HashMap<>();
            account.put("address", address);
            account.put("balance", 0);
            account.put("nonce", 0);
            account.put("code_hash_hex", null);
        }

        List<Map<String, Object>> txs = jdbcTemplate.queryForList(
            "SELECT hash, block_height, tx_index, sender, receiver, value, gas_price " +
            "FROM transactions WHERE sender = ? OR receiver = ? " +
            "ORDER BY block_height DESC LIMIT 10", address, address
        );
        account.put("transactions", txs);
        return account;
    }

    @GetMapping("/accounts/recent")
    public List<Map<String, Object>> getRecentAccounts() {
        return jdbcTemplate.queryForList(
            "SELECT address, balance, nonce, last_update_height " +
            "FROM account_flat " +
            "WHERE last_update_height > 0 " +
            "ORDER BY last_update_height DESC LIMIT 10"
        );
    }

    @GetMapping("/contracts")
    public List<Map<String, Object>> getContracts() {
        return jdbcTemplate.queryForList(
            "SELECT address, balance, nonce, last_update_height " +
            "FROM account_flat " +
            "WHERE code_hash IS NOT NULL " +
            "AND code_hash != '0xc5d2460186f7233c927e7db2dcc703c0e500b653ca82273b7bfad8045d85a470' " +
            "AND code_hash != 'c5d2460186f7233c927e7db2dcc703c0e500b653ca82273b7bfad8045d85a470' " +
            "AND code_hash != '0x0000000000000000000000000000000000000000000000000000000000000000' " +
            "AND code_hash != '0000000000000000000000000000000000000000000000000000000000000000' " +
            "AND code_hash != '' " +
            "ORDER BY last_update_height DESC LIMIT 20"
        );
    }

    @GetMapping("/accounts/top")
    public List<Map<String, Object>> getTopAccounts() {
        return jdbcTemplate.queryForList(
            "SELECT address, balance, nonce " +
            "FROM account_flat " +
            "WHERE balance IS NOT NULL AND balance > 0 " +
            "ORDER BY balance DESC LIMIT 10"
        );
    }

    @GetMapping("/search")
    public Map<String, Object> search(@RequestParam String q) {
        String query = q.trim();
        Map<String, Object> result = new HashMap<>();

        // 1. Verificar Altura do Bloco (apenas números)
        if (query.matches("\\d+")) {
            long height = Long.parseLong(query);
            try {
                jdbcTemplate.queryForObject("SELECT height FROM blocks WHERE height = ?", Long.class, height);
                result.put("type", "block");
                result.put("target", query);
                return result;
            } catch (Exception e) {}
        }

        // 2. Verificar Hash de Bloco ou Transação (hexadecimal de 64 caracteres)
        String cleanHash = query.toLowerCase();
        if (cleanHash.startsWith("0x")) {
            cleanHash = cleanHash.substring(2);
        }
        if (cleanHash.matches("[0-9a-f]{64}")) {
            // Verificar se é hash de bloco
            try {
                jdbcTemplate.queryForObject("SELECT height FROM blocks WHERE hash = ? OR hash = ?", Long.class, query, "0x" + cleanHash);
                result.put("type", "block");
                result.put("target", query);
                return result;
            } catch (Exception e) {}

            // Verificar se é hash de transação
            try {
                String txHash = jdbcTemplate.queryForObject("SELECT hash FROM transactions WHERE hash = ? OR hash = ?", String.class, query, "0x" + cleanHash);
                result.put("type", "tx");
                result.put("target", txHash);
                return result;
            } catch (Exception e) {}
        }

        // 3. Verificar Endereço (Bech32 "jamii1..." ou Mirror "0x..." com 40 hexs)
        if (query.startsWith("jamii1") || query.startsWith("0x") || query.length() == 40) {
            result.put("type", "address");
            result.put("target", query);
            return result;
        }

        result.put("type", "not_found");
        return result;
    }

    @PostMapping("/wallet/login")
    public Map<String, Object> loginWallet(@RequestBody LoginRequest request, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        File tempFile = null;
        try {
            if (request.getKeystoreJson() == null || request.getKeystoreJson().trim().isEmpty()) {
                throw new IllegalArgumentException("Arquivo de carteira JSON obrigatorio!");
            }
            if (request.getPassword() == null) {
                throw new IllegalArgumentException("Senha obrigatoria!");
            }

            tempFile = File.createTempFile("jamii_keystore_", ".json");
            try (FileWriter writer = new FileWriter(tempFile)) {
                writer.write(request.getKeystoreJson());
            }

            JamiiWallet wallet = new JamiiWallet(tempFile, request.getPassword());
            JamiiKeyPair keyPair = wallet.getKeyPair();
            
            if (keyPair == null) {
                throw new RuntimeException("Falha ao recuperar chaves da carteira.");
            }

            session.setAttribute("jamii_keypair", keyPair);
            
            response.put("status", "success");
            response.put("address", wallet.getAddress().toJamii1());
            response.put("mirror", wallet.getAddress().toEthereum());
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
        return response;
    }

    @PostMapping("/wallet/logout")
    public Map<String, Object> logoutWallet(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        session.removeAttribute("jamii_keypair");
        session.invalidate();
        response.put("status", "success");
        return response;
    }

    @GetMapping("/wallet/info")
    public Map<String, Object> getWalletInfo(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        try {
            JamiiKeyPair keyPair = (JamiiKeyPair) session.getAttribute("jamii_keypair");
            if (keyPair == null) {
                response.put("logged", false);
                return response;
            }

            String address = keyPair.getAddress().toJamii1();
            
            JamiiClient client = new JamiiClient(jsonRpcUrl);
            BigInteger balance = BigInteger.ZERO;
            String nodeStatus = "Online";
            try {
                balance = client.getBalance(address);
            } catch (Exception e) {
                nodeStatus = "Offline (" + e.getMessage() + ")";
            }

            response.put("logged", true);
            response.put("address", address);
            response.put("mirror", keyPair.getAddress().toEthereum());
            response.put("balance", balance.toString());
            response.put("nodeStatus", nodeStatus);
        } catch (Exception e) {
            response.put("error", e.getMessage());
        }
        return response;
    }

    @PostMapping("/wallet/transfer")
    public Map<String, Object> transferWallet(@RequestBody WalletTransferRequest request, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        try {
            JamiiKeyPair keyPair = (JamiiKeyPair) session.getAttribute("jamii_keypair");
            if (keyPair == null) {
                throw new RuntimeException("Sessao de carteira expirada ou nao autenticada. Faca login novamente!");
            }

            String toAddress = request.getTo();
            String valueStr = request.getValue();

            if (toAddress == null || toAddress.trim().isEmpty()) {
                throw new IllegalArgumentException("Endereco de destino obrigatorio!");
            }
            if (valueStr == null || valueStr.trim().isEmpty()) {
                throw new IllegalArgumentException("Valor da transferencia obrigatorio!");
            }
            BigInteger transferValue = new BigInteger(valueStr);

            JamiiClient client = new JamiiClient(jsonRpcUrl);
            String fromAddress = keyPair.getAddress().toJamii1();

            BigInteger balance = client.getBalance(fromAddress);
            
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

            com.jamii.sdk.model.Transaction tx = new com.jamii.sdk.model.Transaction();
            tx.nonce = nonce;
            tx.chainId = chainId;
            tx.gasLimit = gasLimit;
            tx.maxPriorityFeePerGas = priorityFee;
            tx.maxFeePerGas = maxFee;
            tx.value = transferValue;
            tx.to = toAddress;
            tx.pubKey = keyPair.getHybridPublicKey();

            com.jamii.sdk.core.JamiiSigner.signTransaction(tx, keyPair);

            byte[] encodedTx = com.jamii.sdk.core.JamiiCodec.encodeSsz(tx);
            String txHash = client.sendRawTransaction(encodedTx);

            response.put("status", "success");
            response.put("txHash", txHash);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
        }
        return response;
    }

    @PostMapping("/wallet/create")
    public Map<String, Object> createWallet(@RequestBody CreateWalletRequest request, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        File tempFile = null;
        try {
            if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                throw new IllegalArgumentException("Senha obrigatoria para gerar nova carteira!");
            }

            tempFile = File.createTempFile("jamii_new_keystore_", ".json");
            JamiiWallet wallet = new JamiiWallet(request.getPassword());
            wallet.saveToFile(tempFile);

            String keystoreJson = new String(java.nio.file.Files.readAllBytes(tempFile.toPath()), java.nio.charset.StandardCharsets.UTF_8);

            // Efetuar login automatico
            session.setAttribute("jamii_keypair", wallet.getKeyPair());

            response.put("status", "success");
            response.put("address", wallet.getAddress().toJamii1());
            response.put("mirror", wallet.getAddress().toEthereum());
            response.put("mnemonic", wallet.getMnemonic());
            response.put("keystoreJson", keystoreJson);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
        return response;
    }

    @PostMapping("/wallet/recover")
    public Map<String, Object> recoverWallet(@RequestBody RecoverWalletRequest request, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        File tempFile = null;
        try {
            if (request.getMnemonic() == null || request.getMnemonic().trim().isEmpty()) {
                throw new IllegalArgumentException("Mnemonico de 12 palavras obrigatorio!");
            }
            if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                throw new IllegalArgumentException("Senha obrigatoria!");
            }

            tempFile = File.createTempFile("jamii_rec_keystore_", ".json");
            JamiiWallet wallet = new JamiiWallet(request.getMnemonic(), request.getPassword());
            wallet.saveToFile(tempFile);

            String keystoreJson = new String(java.nio.file.Files.readAllBytes(tempFile.toPath()), java.nio.charset.StandardCharsets.UTF_8);

            // Efetuar login automatico
            session.setAttribute("jamii_keypair", wallet.getKeyPair());

            response.put("status", "success");
            response.put("address", wallet.getAddress().toJamii1());
            response.put("mirror", wallet.getAddress().toEthereum());
            response.put("keystoreJson", keystoreJson);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
        return response;
    }

    public static class CreateWalletRequest {
        private String password;
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class RecoverWalletRequest {
        private String mnemonic;
        private String password;
        public String getMnemonic() { return mnemonic; }
        public void setMnemonic(String mnemonic) { this.mnemonic = mnemonic; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class LoginRequest {
        private String keystoreJson;
        private String password;

        public String getKeystoreJson() { return keystoreJson; }
        public void setKeystoreJson(String keystoreJson) { this.keystoreJson = keystoreJson; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class WalletTransferRequest {
        private String to;
        private String value;

        public String getTo() { return to; }
        public void setTo(String to) { this.to = to; }
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
    }
}

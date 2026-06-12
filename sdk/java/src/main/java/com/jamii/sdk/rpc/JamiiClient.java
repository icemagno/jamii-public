package com.jamii.sdk.rpc;

import com.jamii.sdk.model.JamiiBlock;
import com.jamii.sdk.model.Transaction;
import org.bouncycastle.util.encoders.Hex;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class JamiiClient {
    private final String rpcUrl;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JamiiClient(String rpcUrl) {
        this.rpcUrl = rpcUrl;
    }

    private Map sendRpcRequest(Map<String, Object> body) {
        try {
            String requestBody = objectMapper.writeValueAsString(body);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(rpcUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                throw new RuntimeException("HTTP error code: " + response.statusCode() + " - " + response.body());
            }
            
            return objectMapper.readValue(response.body(), Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send RPC request", e);
        }
    }

    public long getTransactionCount(String address) {
        Map resp = sendRpcRequest(Map.of("jsonrpc", "2.0", "method", "eth_getTransactionCount", "params", new Object[]{address, "latest"}, "id", 1));
        if (resp != null && resp.containsKey("error")) throw new RuntimeException(resp.get("error").toString());
        String result = (String) resp.get("result");
        return Long.decode(result);
    }

    public long getChainId() {
        Map resp = sendRpcRequest(Map.of("jsonrpc", "2.0", "method", "eth_chainId", "params", new Object[]{}, "id", 1));
        if (resp != null && resp.containsKey("error")) throw new RuntimeException(resp.get("error").toString());
        String result = (String) resp.get("result");
        return Long.decode(result);
    }

    // --- Métodos de Consulta (Read-only) ---

    public BigInteger getBalance(String address) {
        Map resp = sendRpcRequest(Map.of("jsonrpc", "2.0", "method", "eth_getBalance", "params", new Object[]{address, "latest"}, "id", 1));
        if (resp != null && resp.containsKey("error")) throw new RuntimeException(resp.get("error").toString());
        String result = (String) resp.get("result");
        return new BigInteger(result.substring(2), 16);
    }

    public BigInteger getGasPrice() {
        Map resp = sendRpcRequest(Map.of("jsonrpc", "2.0", "method", "eth_gasPrice", "params", new Object[]{}, "id", 1));
        if (resp != null && resp.containsKey("error")) throw new RuntimeException(resp.get("error").toString());
        String result = (String) resp.get("result");
        return new BigInteger(result.substring(2), 16);
    }

    public JamiiBlock getBlockByNumber(long number, boolean fullTx) {
        String hexNumber = "0x" + Long.toHexString(number);
        Map resp = sendRpcRequest(Map.of("jsonrpc", "2.0", "method", "eth_getBlockByNumber", "params", new Object[]{hexNumber, fullTx}, "id", 1));
        if (resp != null && resp.containsKey("error")) throw new RuntimeException(resp.get("error").toString());
        
        Object result = resp.get("result");
        if (result == null) return null;
        
        // Converte o Map do JSON-RPC para o objeto JamiiBlock tipado
        return objectMapper.convertValue(result, JamiiBlock.class);
    }

    public String sendRawTransaction(byte[] rawTx) {
        return sendRawTransaction("0x" + Hex.toHexString(rawTx));
    }

    public String sendRawTransaction(String hex) {
        Map resp = sendRpcRequest(Map.of("jsonrpc", "2.0", "method", "eth_sendRawTransaction", "params", new Object[]{hex}, "id", 1));
        if (resp != null && resp.containsKey("error")) throw new RuntimeException(resp.get("error").toString());
        return resp != null ? (String) resp.get("result") : null;
    }

    public Map getTransactionReceipt(String txHash) {
        Map resp = sendRpcRequest(Map.of("jsonrpc", "2.0", "method", "eth_getTransactionReceipt", "params", new Object[]{txHash}, "id", 1));
        if (resp != null && resp.containsKey("error")) throw new RuntimeException(resp.get("error").toString());
        return resp != null ? (Map) resp.get("result") : null;
    }

    public String ethCall(String from, String to, String data) {
        Map<String, String> callObj = new java.util.HashMap<>();
        if (from != null) callObj.put("from", from);
        callObj.put("to", to);
        callObj.put("data", data);

        Map resp = sendRpcRequest(Map.of("jsonrpc", "2.0", "method", "eth_call", "params", new Object[]{callObj, "latest"}, "id", 1));
        if (resp != null && resp.containsKey("error")) throw new RuntimeException(resp.get("error").toString());
        return resp != null ? (String) resp.get("result") : null;
    }
}

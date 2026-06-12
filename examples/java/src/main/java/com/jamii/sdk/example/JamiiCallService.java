package com.jamii.sdk.example;

import com.jamii.sdk.rpc.JamiiClient;
import org.springframework.stereotype.Service;
import java.math.BigInteger;

@Service
public class JamiiCallService {

    private final JamiiClient jamiiClient = new JamiiClient("http://localhost:8545");

    public void runContractCall(String contractAddress) {
        System.out.println("🔍 [SDK-Call] Invocando Contrato Inteligente...");

        // Seletor da função getNodeBalance() -> 0x1e35fed8
        String data = "0x1e35fed8";

        try {
            System.out.println("Invocando getNodeBalance() em " + contractAddress + "...");
            String resultHex = jamiiClient.ethCall(null, contractAddress, data);
            
            System.out.println("Result (Hex): " + resultHex);

            if (resultHex != null && resultHex.length() > 2) {
                BigInteger balance = new BigInteger(resultHex.substring(2), 16);
                System.out.println("💰 Saldo do Nó (lido do Contrato): " + balance + " Wei");
            } else {
                System.out.println("⚠️ O contrato retornou um resultado vazio.");
            }
        } catch (Exception e) {
            System.err.println("❌ Erro na chamada do contrato: " + e.getMessage());
        }
    }
}

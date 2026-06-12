package com.jamii.sdk.example;

import com.jamii.sdk.core.*;
import com.jamii.sdk.address.JamiiAddress;
import com.jamii.sdk.crypto.JamiiKeyPair;
import com.jamii.sdk.model.Transaction;
import com.jamii.sdk.rpc.JamiiClient;
import org.springframework.stereotype.Service;

import java.io.File;
import java.math.BigInteger;

@Service
public class JamiiService {

    private final JamiiClient jamiiClient = new JamiiClient("http://localhost:8545");

    public void runProductionBatch(String keystorePath, String password, String toAddress, int totalTxs, int batchSize) throws Exception {
        System.out.println("🚀 [SDK-App] Iniciando Batch de Produção via SDK...");

        JamiiWallet wallet = new JamiiWallet(new File(keystorePath), password);
        JamiiKeyPair keyPair = wallet.getKeyPair();
        JamiiAddress senderAddr = keyPair.getAddress();
        byte[] hybridPk = keyPair.getHybridPublicKey();

        System.out.println("💳 Remetente Identificado: " + senderAddr);
        long nonce = jamiiClient.getTransactionCount(senderAddr.toJamii1());
        long chainId = jamiiClient.getChainId();
        
        // Parâmetros idênticos ao exemplo em Go
        BigInteger maxPriorityFee = BigInteger.valueOf(100000000L); // 0.1 Gwei
        BigInteger maxFee = BigInteger.valueOf(2000000000L); // 2 Gwei
        long gasLimit = 21000;

        for (int b = 0; b < (totalTxs / batchSize); b++) {
            for (int i = 0; i < batchSize; i++) {
                Transaction tx = new Transaction();
                tx.nonce = nonce;
                tx.chainId = chainId;
                tx.gasLimit = gasLimit; 
                tx.maxPriorityFeePerGas = maxPriorityFee;
                tx.maxFeePerGas = maxFee;
                tx.value = BigInteger.ONE;
                tx.to = toAddress;
                tx.pubKey = hybridPk;

                JamiiSigner.signTransaction(tx, keyPair);

                String result = jamiiClient.sendRawTransaction(JamiiCodec.encodeSsz(tx));
                System.out.println("✅ TX Sent! Hash: " + result);
                
                nonce++;
            }
            System.out.println("⏳ Aguardando 5 segundos para o próximo lote...");
            Thread.sleep(5000);
        }
    }
}

package com.jamii.sdk.example;

import com.jamii.sdk.core.JamiiCodec;
import com.jamii.sdk.core.JamiiSigner;
import com.jamii.sdk.core.JamiiWallet;
import com.jamii.sdk.model.Transaction;
import com.jamii.sdk.rpc.JamiiClient;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.stereotype.Service;

import java.io.File;
import java.math.BigInteger;
import java.util.Map;

@Service
public class JamiiDeployService {

    private final JamiiClient jamiiClient = new JamiiClient("http://localhost:8545");

    public String runContractDeploy(String keystorePath, String password) throws Exception {
        System.out.println("🚀 [SDK-Deploy] Iniciando Deploy de Contrato via SDK...");
        String deployedAddress = null;

        // 1. Carregar Carteira
        JamiiWallet wallet = new JamiiWallet(new File(keystorePath), password);
        String aliceAddress = wallet.getAddress().toJamii1();
        System.out.println("💳 Remetente (Alice): " + aliceAddress);

        BigInteger balanceBefore = jamiiClient.getBalance(aliceAddress);
        System.out.println("💰 Saldo Anterior: " + balanceBefore);

        // 2. Preparar Bytecode (WalletBalance.bin)
        String binHex = "6080604052348015600e575f5ffd5b5061017d8061001c5f395ff3fe608060405234801561000f575f5ffd5b5060043610610034575f3560e01c80631e35fed814610038578063d80fffb814610056575b5f5ffd5b610040610074565b60405161004d91906100d6565b60405180910390f35b61005e6100a6565b60405161006b919061012e565b60405180910390f35b5f73fa3e5088bd87226dfc9ea7f2f1a74ed97f2e997573ffffffffffffffffffffffffffffffffffffffff1631905090565b73fa3e5088bd87226dfc9ea7f2f1a74ed97f2e997581565b5f819050919050565b6100d0816100be565b82525050565b5f6020820190506100e95f8301846100c7565b92915050565b5f73ffffffffffffffffffffffffffffffffffffffff82169050919050565b5f610118826100ef565b9050919050565b6101288161010e565b82525050565b5f6020820190506101415f83018461011f565b9291505056fea26469706673582212205c956b045f919d1531bd5ec001ba15c4a4f93c49a70f92670429fb7b6974f10c64736f6c634300081f0033";
        byte[] contractCode = Hex.decode(binHex);

        // 3. Configurar Transação (EIP-1559 Compliance)
        long nonce = jamiiClient.getTransactionCount(wallet.getAddress().toJamii1());
        long chainId = jamiiClient.getChainId();
        
        BigInteger baseFee = jamiiClient.getGasPrice();
        BigInteger priorityFee = BigInteger.valueOf(1000000000L); // 1 Gwei
        // MANDATO SOBERANO: MaxFee deve ser >= BaseFee + PriorityFee
        BigInteger maxFee = baseFee.add(priorityFee); 

        Transaction tx = new Transaction();
        tx.nonce = nonce;
        tx.chainId = chainId;
        tx.gasLimit = 1500000; // Aumentado para garantir folga no deploy
        tx.maxFeePerGas = maxFee;
        tx.maxPriorityFeePerGas = priorityFee;
        tx.value = BigInteger.ZERO;
        tx.to = null; // Deploy
        tx.data = contractCode;
        tx.pubKey = wallet.getKeyPair().getHybridPublicKey();

        // 4. Assinar e Enviar
        System.out.println("✍️ Assinando Deploy (Nonce: " + nonce + ")...");
        JamiiSigner.signTransaction(tx, wallet.getKeyPair());

        try {
            String txHash = jamiiClient.sendRawTransaction(JamiiCodec.encodeSsz(tx));
            System.out.println("✅ Deploy Sent! TxHash: " + txHash);
            System.out.println("🔍 Aguardando inclusão no bloco...");

            // Polling para o recibo
            Map receipt = null;
            for (int i = 0; i < 30; i++) {
                receipt = jamiiClient.getTransactionReceipt(txHash);
                if (receipt != null) break;
                Thread.sleep(1000);
            }

            if (receipt != null) {
                String contractAddress = (String) receipt.get("contractAddress");
                String status = (String) receipt.get("status");
                System.out.println("🎊 Transação Minerada!");
                System.out.println("📌 Status: " + ("0x1".equals(status) ? "Sucesso" : "Falha"));
                System.out.println("🚀 Endereço do Contrato: " + contractAddress);

                BigInteger balanceAfter = jamiiClient.getBalance(aliceAddress);
                System.out.println("💰 Saldo Posterior: " + balanceAfter);
                System.out.println("📉 Gasto Total: " + balanceBefore.subtract(balanceAfter));
                
                deployedAddress = contractAddress;
            } else {
                System.out.println("⏳ Timeout ao aguardar recibo da transação.");
            }

        } catch (Exception e) {
            System.err.println("❌ Erro no deploy: " + e.getMessage());
            e.printStackTrace();
        }
        return deployedAddress;
    }
}

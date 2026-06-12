package com.jamii.sdk.core;

import com.jamii.sdk.model.Transaction;
import com.jamii.sdk.util.NumericUtil;
import org.bouncycastle.util.encoders.Hex;
import java.math.BigInteger;
import java.util.Arrays;

public class VerifySerialization {
    public static void main(String[] args) {
        Transaction tx = new Transaction();
        tx.nonce = 10091;
        tx.gasLimit = 21000;
        tx.chainId = 2026;
        tx.maxFeePerGas = BigInteger.valueOf(2000000000);
        tx.maxPriorityFeePerGas = BigInteger.valueOf(100000000);
        tx.value = BigInteger.ONE;
        tx.to = "jamii1zryc0mck309ve9d2predwz76errgpducq6wyt0c";
        tx.data = new byte[0];
        tx.pubKey = new byte[1992];
        
        byte[] encoded = JamiiCodec.encodeSsz(tx);
        System.out.println("Java Buffer: " + Hex.toHexString(Arrays.copyOf(encoded, 100)));
    }
}

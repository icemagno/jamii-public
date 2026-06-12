package com.jamii.sdk.model;

import com.jamii.sdk.address.JamiiAddress;
import java.math.BigInteger;

public class Transaction {
    public long nonce;
    public long gasLimit;
    public long chainId;
    public BigInteger maxFeePerGas = BigInteger.ZERO;
    public BigInteger maxPriorityFeePerGas = BigInteger.ZERO;
    public BigInteger value = BigInteger.ZERO;
    public String to; // Bech32
    public byte[] data = new byte[0];
    public byte[] pubKey;
    public byte[] signature;

    public Transaction() {
        this.data = new byte[0];
    }

    public byte[] getToAddressHash() {
        if (to == null || to.isEmpty()) return null; 
        JamiiAddress addr = JamiiAddress.fromBech32(to);
        byte[] res = new byte[21];
        res[0] = addr.getVersion();
        byte[] hash = addr.getHash();
        if (hash.length != 20) throw new RuntimeException("Hash de endereço inválido no SDK (esperado 20, obtido " + hash.length + ")");
        System.arraycopy(hash, 0, res, 1, 20);
        return res;
    }
}

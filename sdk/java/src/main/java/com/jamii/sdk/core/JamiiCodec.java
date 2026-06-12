package com.jamii.sdk.core;

import com.jamii.sdk.model.Transaction;
import com.jamii.sdk.util.NumericUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class JamiiCodec {
    private static final int ADDRESS_SIZE = 21;
    private static final int UINT256_SIZE = 32;

    public static byte[] encodeSsz(Transaction tx) {
        return encodeSsz(tx, false);
    }

    public static byte[] encodeSsz(Transaction tx, boolean excludeSig) {
        // [Version(4)][Nonce(8)][GasLimit(8)][ChainId(8)][Offsets(28)] = 56 bytes
        int staticSize = 56;
        
        // 1. Preparar partes variáveis
        byte[] maxFee = NumericUtil.toUint256(tx.maxFeePerGas);
        byte[] maxPriority = NumericUtil.toUint256(tx.maxPriorityFeePerGas);
        byte[] value = NumericUtil.toUint256(tx.value);
        byte[] to = (tx.to != null && !tx.to.isEmpty()) ? tx.getToAddressHash() : null;
        byte[] data = tx.data != null ? tx.data : new byte[0];
        byte[] pubKey = tx.pubKey != null ? tx.pubKey : new byte[0];
        byte[] sig = (tx.signature != null && !excludeSig) ? tx.signature : new byte[0];

        // 2. Calcular offsets e tamanho total
        int off0 = staticSize;
        int off1 = off0 + UINT256_SIZE; // MaxFee
        int off2 = off1 + UINT256_SIZE; // MaxPriority
        int off3 = off2 + UINT256_SIZE; // Value
        int off4 = (to != null) ? off3 + ADDRESS_SIZE : off3 + 0; // To
        int off5 = off4 + data.length; // Data
        int off6 = off5 + pubKey.length; // PubKey
        int totalSize = off6 + sig.length; // Signature

        ByteBuffer buf = ByteBuffer.allocate(totalSize);
        buf.order(ByteOrder.LITTLE_ENDIAN);

        // Header Estático
        buf.putInt(1); // Version V1
        buf.putLong(tx.nonce);
        buf.putLong(tx.gasLimit);
        buf.putLong(tx.chainId);

        // Tabela de Offsets
        buf.putInt(off0);
        buf.putInt(off1);
        buf.putInt(off2);
        buf.putInt(to != null ? off3 : 0);
        buf.putInt(off4);
        buf.putInt(off5);
        buf.putInt(off6);

        // Dados das Variáveis
        buf.put(maxFee);
        buf.put(maxPriority);
        buf.put(value);
        if (to != null) buf.put(to);
        buf.put(data);
        buf.put(pubKey);
        if (!excludeSig && sig.length > 0) {
            buf.put(sig);
        }

        return buf.array();
    }
}

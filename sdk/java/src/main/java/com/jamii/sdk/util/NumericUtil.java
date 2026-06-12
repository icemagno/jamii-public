package com.jamii.sdk.util;

import org.bouncycastle.crypto.digests.KeccakDigest;
import org.bouncycastle.crypto.digests.SHAKEDigest;
import java.math.BigInteger;

public class NumericUtil {
    /**
     * Converte um BigInteger para um array de 32 bytes em BigEndian (padrão Ethereum/Jamii).
     * Garante que o resultado tenha exatamente 32 bytes e remove o byte de sinal extra do Java.
     */
    public static byte[] toUint256(BigInteger val) {
        if (val == null) val = BigInteger.ZERO;
        byte[] raw = val.toByteArray();
        byte[] res = new byte[32];
        
        // BigInteger.toByteArray() retorna o array em BigEndian.
        // Se o valor for positivo e o bit mais significativo for 1, o Java adiciona um 0x00 no início.
        int srcPos = 0;
        int length = raw.length;

        if (raw.length > 32) {
            // Valor maior que 256 bits (truncar)
            srcPos = raw.length - 32;
            length = 32;
        } else if (raw.length > 0 && raw[0] == 0) {
            // Remover byte de sinal extra se houver
            srcPos = 1;
            length = raw.length - 1;
        }
        
        // Copia para o final do buffer de 32 bytes (Padding à esquerda com zeros)
        int destPos = 32 - length;
        System.arraycopy(raw, srcPos, res, destPos, length);
        return res;
    }

    public static byte[] keccak256(byte[] data) {
        KeccakDigest d = new KeccakDigest(256);
        d.update(data, 0, data.length);
        byte[] res = new byte[32];
        d.doFinal(res, 0);
        return res;
    }

    public static byte[] shake256(byte[] data, int outputSize) {
        SHAKEDigest d = new SHAKEDigest(256);
        d.update(data, 0, data.length);
        byte[] res = new byte[outputSize];
        d.doFinal(res, 0, outputSize);
        return res;
    }

    public static byte[] concat(byte[]... arrays) {
        int t = 0; for (byte[] a : arrays) if (a != null) t += a.length;
        byte[] r = new byte[t]; int p = 0;
        for (byte[] a : arrays) { if (a != null) { System.arraycopy(a, 0, r, p, a.length); p += a.length; } }
        return r;
    }
}

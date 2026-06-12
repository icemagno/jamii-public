package com.jamii.sdk.crypto;

import com.jamii.sdk.address.JamiiAddress;
import com.jamii.sdk.util.NumericUtil;

public class JamiiKeyPair {
    private final byte[] traditionalPrivateKey;
    private final byte[] quantumPrivateKey;
    private final byte[] traditionalPublicKey;
    private final byte[] quantumPublicKey;

    public JamiiKeyPair(byte[] tradPriv, byte[] quantPriv, byte[] tradPub, byte[] quantPub) {
        this.traditionalPrivateKey = tradPriv;
        this.quantumPrivateKey = quantPriv;
        this.traditionalPublicKey = tradPub;
        this.quantumPublicKey = quantPub;
    }

    private static final org.bouncycastle.asn1.x9.X9ECParameters CURVE_PARAMS = org.bouncycastle.crypto.ec.CustomNamedCurves.getByName("secp256k1");
    private static final org.bouncycastle.crypto.params.ECDomainParameters CURVE = new org.bouncycastle.crypto.params.ECDomainParameters(
            CURVE_PARAMS.getCurve(), CURVE_PARAMS.getG(), CURVE_PARAMS.getN(), CURVE_PARAMS.getH());

    public JamiiAddress getAddress() {
        // No Jamii, o endereço é derivado da parte tradicional (Secp256k1)
        // Keccak256(pubTradUncompressed[1:]) -> últimos 20 bytes
        
        // Descomprimir a chave (33 bytes -> 65 bytes)
        org.bouncycastle.math.ec.ECPoint q = CURVE.getCurve().decodePoint(traditionalPublicKey);
        byte[] uncompressed = q.getEncoded(false); // 65 bytes (com prefixo 0x04)
        
        // Remover prefixo 0x04 para o hash estilo Ethereum
        byte[] pubForHash = new byte[64];
        System.arraycopy(uncompressed, 1, pubForHash, 0, 64);
        
        byte[] hash = NumericUtil.keccak256(pubForHash);
        byte[] addressHash = new byte[20];
        System.arraycopy(hash, 12, addressHash, 0, 20);
        return new JamiiAddress(addressHash);
    }

    public byte[] getTraditionalPrivateKey() { return traditionalPrivateKey; }
    public byte[] getQuantumPrivateKey() { return quantumPrivateKey; }
    public byte[] getTraditionalPublicKey() { return traditionalPublicKey; }
    public byte[] getQuantumPublicKey() { return quantumPublicKey; }

    public byte[] getTraditionalPublicKeyWithPrefix() {
        byte[] res = new byte[1 + traditionalPublicKey.length];
        res[0] = 0x00; // Secp256k1
        System.arraycopy(traditionalPublicKey, 0, res, 1, traditionalPublicKey.length);
        return res;
    }

    public byte[] getQuantumPublicKeyWithPrefix() {
        byte[] res = new byte[1 + quantumPublicKey.length];
        res[0] = 0x01; // ML-DSA-65
        System.arraycopy(quantumPublicKey, 0, res, 1, quantumPublicKey.length);
        return res;
    }

    public byte[] getHybridPublicKey() {
        byte[] tradWithPrefix = getTraditionalPublicKeyWithPrefix();
        byte[] quantWithPrefix = getQuantumPublicKeyWithPrefix();
        
        // Formato Jamii: [0x02][lenTrad(4)][tradPubWithPrefix][quantPubWithPrefix]
        java.nio.ByteBuffer buf = java.nio.ByteBuffer.allocate(1 + 4 + tradWithPrefix.length + quantWithPrefix.length);
        buf.order(java.nio.ByteOrder.LITTLE_ENDIAN);
        buf.put((byte) 0x02); // Hybrid
        buf.putInt(tradWithPrefix.length);
        buf.put(tradWithPrefix);
        buf.put(quantWithPrefix);
        return buf.array();
    }
}

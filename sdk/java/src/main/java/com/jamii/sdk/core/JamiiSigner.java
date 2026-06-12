package com.jamii.sdk.core;

import com.jamii.sdk.crypto.JamiiKeyPair;
import com.jamii.sdk.model.Transaction;
import com.jamii.sdk.util.NumericUtil;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ParametersWithContext;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.bouncycastle.crypto.signers.HMacDSAKCalculator;

// USE THE CORRECT CORE PACKAGE FOR BC 1.84 (FIPS 204 FINAL)
import org.bouncycastle.crypto.params.MLDSAParameters;
import org.bouncycastle.crypto.params.MLDSAPrivateKeyParameters;
import org.bouncycastle.crypto.signers.MLDSASigner;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

public class JamiiSigner {

    private static final X9ECParameters CURVE_PARAMS = CustomNamedCurves.getByName("secp256k1");
    private static final ECDomainParameters CURVE = new ECDomainParameters(
            CURVE_PARAMS.getCurve(), CURVE_PARAMS.getG(), CURVE_PARAMS.getN(), CURVE_PARAMS.getH());

    public static void signTransaction(Transaction tx, JamiiKeyPair keyPair) {
        // 1. Digest base (SHAKE-256 conforme keccak.Sum no Go)
        byte[] encodedNoSig = JamiiCodec.encodeSsz(tx, true);
        byte[] signingHash = NumericUtil.shake256(encodedNoSig, 32);
        
        // 2. Strong Binding (Keccak-256 conforme hybrid.go no Go)
        // Concatena: SigningHash + tradPubWithPrefix + quantPubWithPrefix
        byte[] boundMessage = NumericUtil.keccak256(NumericUtil.concat(
            signingHash, 
            keyPair.getTraditionalPublicKeyWithPrefix(), 
            keyPair.getQuantumPublicKeyWithPrefix()
        ));

        // 3. Assinaturas
        byte[] sigTrad = signSecp256k1(boundMessage, keyPair.getTraditionalPrivateKey());
        byte[] sigQuant = signMLDSA65(boundMessage, keyPair.getQuantumPrivateKey());

        // 4. Protocolo Jamii Signature V1: [lenTrad(4)][sigTrad][sigQuant]
        ByteBuffer sigBuf = ByteBuffer.allocate(4 + sigTrad.length + sigQuant.length);
        sigBuf.order(ByteOrder.LITTLE_ENDIAN);
        sigBuf.putInt(sigTrad.length);
        sigBuf.put(sigTrad);
        sigBuf.put(sigQuant);
        
        tx.signature = sigBuf.array();
    }

    public static byte[] signSecp256k1(byte[] digest, byte[] privateKey) {
        // MANDATO INDUSTRIAL: O nó Go (hybrid.go) já passa o digest (boundMessage) pronto para o signatário tradicional.
        // Assinamos o digest de 32 bytes diretamente para evitar duplo hashing e garantir compatibilidade.
        if (digest.length != 32) {
            throw new IllegalArgumentException("Secp256k1 requer um digest de exatamente 32 bytes (recebido: " + digest.length + ")");
        }

        ECPrivateKeyParameters privKey = new ECPrivateKeyParameters(new BigInteger(1, privateKey), CURVE);
        ECDSASigner signer = new ECDSASigner(new HMacDSAKCalculator(new SHA256Digest()));
        signer.init(true, privKey);
        BigInteger[] components = signer.generateSignature(digest);
        
        BigInteger r = components[0];
        BigInteger s = components[1];
        
        // Low-S (Ethereum Compliance)
        BigInteger n = CURVE.getN();
        BigInteger halfN = n.shiftRight(1);
        if (s.compareTo(halfN) > 0) {
            s = n.subtract(s);
        }
        
        byte[] rBytes = r.toByteArray();
        byte[] sBytes = s.toByteArray();
        byte[] sig = new byte[65];
        
        // Padding e cópia dos componentes R e S (32 bytes cada)
        int rLen = Math.min(rBytes.length, 32);
        int sLen = Math.min(sBytes.length, 32);
        System.arraycopy(rBytes, rBytes.length - rLen, sig, 32 - rLen, rLen);
        System.arraycopy(sBytes, sBytes.length - sLen, sig, 64 - sLen, sLen);
        sig[64] = 0x00; // RecID
        
        return sig;
    }

    public static byte[] signMLDSA65(byte[] message, byte[] privateKeyBytes) {
        try {
            MLDSAPrivateKeyParameters priv = new MLDSAPrivateKeyParameters(MLDSAParameters.ml_dsa_65, privateKeyBytes);
            MLDSASigner signer = new MLDSASigner();
            
            // COMPATIBILIDADE SOBERANA (NIST FIPS 204):
            // O nó Go (Circl) usa o contexto "jamii-sovereign-v1".
            byte[] context = "jamii-sovereign-v1".getBytes(StandardCharsets.UTF_8);
            
            // Delega para o Bouncy Castle a injeção correta do contexto FIPS 204: (0 || len(ctx) || ctx || msg)
            ParametersWithContext paramsWithContext = new ParametersWithContext(priv, context);
            
            signer.init(true, paramsWithContext);
            signer.update(message, 0, message.length);
            return signer.generateSignature();
            
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar assinatura ML-DSA: " + e.getMessage(), e);
        }
    }
}

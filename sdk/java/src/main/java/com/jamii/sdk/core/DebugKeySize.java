package com.jamii.sdk.core;

import com.jamii.sdk.crypto.JamiiKeyPair;
import org.bouncycastle.pqc.crypto.mldsa.MLDSAPrivateKeyParameters;
import org.bouncycastle.pqc.crypto.mldsa.MLDSAParameters;
import org.bouncycastle.util.encoders.Hex;

public class DebugKeySize {
    public static void main(String[] args) {
        // Dummy key of 4000 bytes (ML-DSA-65 priv key is 4000 bytes)
        byte[] priv = new byte[4000];
        MLDSAPrivateKeyParameters mlPriv = new MLDSAPrivateKeyParameters(MLDSAParameters.ml_dsa_65, priv);
        byte[] pub = mlPriv.getPublicKey();
        System.out.println("ML-DSA-65 Public Key Size: " + pub.length);
    }
}

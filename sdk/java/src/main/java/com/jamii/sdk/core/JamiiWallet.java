package com.jamii.sdk.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jamii.sdk.address.JamiiAddress;
import com.jamii.sdk.crypto.JamiiKeyPair;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.generators.ECKeyPairGenerator;
import org.bouncycastle.crypto.generators.SCrypt;
import org.bouncycastle.crypto.modes.GCMBlockCipher;
import org.bouncycastle.crypto.generators.MLDSAKeyPairGenerator;
import org.bouncycastle.crypto.params.*;
import org.bouncycastle.util.encoders.Hex;

// USE THE CORRECT CORE PACKAGE FOR BC 1.84 (FIPS 204 FINAL)
import org.bouncycastle.crypto.params.MLDSAParameters;
import org.bouncycastle.crypto.params.MLDSAPrivateKeyParameters;
import org.bouncycastle.crypto.params.MLDSAPublicKeyParameters;
import org.bouncycastle.crypto.params.MLDSAKeyGenerationParameters;
import org.web3j.crypto.MnemonicUtils;
import org.web3j.crypto.Bip32ECKeyPair;
import org.bouncycastle.crypto.prng.FixedSecureRandom;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.digests.SHA512Digest;
import org.bouncycastle.crypto.params.KeyParameter;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.UUID;

public class JamiiWallet {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JamiiKeyPair keyPair;
    private final String password;
    private String mnemonic;

    private static final X9ECParameters CURVE_PARAMS = CustomNamedCurves.getByName("secp256k1");
    private static final ECDomainParameters CURVE = new ECDomainParameters(
            CURVE_PARAMS.getCurve(), CURVE_PARAMS.getG(), CURVE_PARAMS.getN(), CURVE_PARAMS.getH());

    // Construtor para carregar carteira existente
    public JamiiWallet(File keystoreFile, String password) throws Exception {
        this.password = password;
        byte[] decrypted = decryptKeystore(keystoreFile, password);
        
        ByteBuffer buf = ByteBuffer.wrap(decrypted).order(ByteOrder.LITTLE_ENDIAN);
        
        // 1. O Formato da Private Key no Keystore é [Algorithm:0x02][lenTrad(4)][tradWithPrefix][quantWithPrefix]
        byte masterAlgo = buf.get();
        if (masterAlgo != 0x02) throw new RuntimeException("Keystore inválido: esperado algoritmo híbrido (0x02)");
        
        int tradLen = buf.getInt();
        byte[] tradWithPrefix = new byte[tradLen];
        buf.get(tradWithPrefix);
        byte[] quantWithPrefix = new byte[buf.remaining()];
        buf.get(quantWithPrefix);

        // 2. Extrair Raw Keys (Remover Prefixos)
        if (tradWithPrefix[0] != 0x00) throw new RuntimeException("Keystore inválido: perna tradicional não é Secp256k1");
        byte[] privTradRaw = new byte[tradWithPrefix.length - 1];
        System.arraycopy(tradWithPrefix, 1, privTradRaw, 0, privTradRaw.length);

        if (quantWithPrefix[0] != 0x01) throw new RuntimeException("Keystore inválido: perna quântica não é ML-DSA-65");
        byte[] privQuantRaw = new byte[quantWithPrefix.length - 1];
        System.arraycopy(quantWithPrefix, 1, privQuantRaw, 0, privQuantRaw.length);

        // 3. Derivar Públicas
        ECPrivateKeyParameters ecPriv = new ECPrivateKeyParameters(new BigInteger(1, privTradRaw), CURVE);
        ECPublicKeyParameters ecPub = new ECPublicKeyParameters(CURVE.getG().multiply(ecPriv.getD()), CURVE);
        byte[] pubTrad = ecPub.getQ().getEncoded(true); // Compressed

        MLDSAPrivateKeyParameters mlPriv = new MLDSAPrivateKeyParameters(MLDSAParameters.ml_dsa_65, privQuantRaw);
        byte[] pubQuant = mlPriv.getPublicKey();

        this.keyPair = new JamiiKeyPair(privTradRaw, privQuantRaw, pubTrad, pubQuant);
    }

    // Construtor para criar nova carteira derivando de mnemônico gerado
    public JamiiWallet(String password) throws Exception {
        this.password = password;
        
        // 1. Gerar entropia para 12 palavras (128 bits / 16 bytes)
        byte[] entropy = new byte[16];
        new SecureRandom().nextBytes(entropy);
        
        // 2. Mapear para mnemônico
        this.mnemonic = MnemonicUtils.generateMnemonic(entropy);
        
        // 3. Derivar chaves híbridas a partir do mnemônico
        byte[] seed = MnemonicUtils.generateSeed(this.mnemonic, "");
        
        // 4. Derivar tradicional (Secp256k1) via BIP-32 m/44'/60'/0'/0/0
        Bip32ECKeyPair master = Bip32ECKeyPair.generateKeyPair(seed);
        int[] path = {
            44 | Bip32ECKeyPair.HARDENED_BIT,
            60 | Bip32ECKeyPair.HARDENED_BIT,
            0 | Bip32ECKeyPair.HARDENED_BIT,
            0,
            0
        };
        Bip32ECKeyPair child = Bip32ECKeyPair.deriveKeyPair(master, path);
        byte[] privTrad = child.getPrivateKey().toByteArray();
        if (privTrad.length > 32) {
            byte[] tmp = new byte[32];
            System.arraycopy(privTrad, privTrad.length - 32, tmp, 0, 32);
            privTrad = tmp;
        } else if (privTrad.length < 32) {
            byte[] tmp = new byte[32];
            System.arraycopy(privTrad, 0, tmp, 32 - privTrad.length, privTrad.length);
            privTrad = tmp;
        }
        
        // 5. Derivar tradicional public key
        ECPrivateKeyParameters ecPriv = new ECPrivateKeyParameters(new BigInteger(1, privTrad), CURVE);
        ECPublicKeyParameters ecPub = new ECPublicKeyParameters(CURVE.getG().multiply(ecPriv.getD()), CURVE);
        byte[] pubTrad = ecPub.getQ().getEncoded(true);
        
        // 6. Derivar quântica (ML-DSA-65) a partir do quantSeed via HMAC-SHA512
        HMac hmac = new HMac(new SHA512Digest());
        hmac.init(new KeyParameter("Jamii ML-DSA Seed".getBytes(StandardCharsets.UTF_8)));
        hmac.update(seed, 0, seed.length);
        byte[] hmacOut = new byte[64];
        hmac.doFinal(hmacOut, 0);
        byte[] quantSeed = new byte[32];
        System.arraycopy(hmacOut, 0, quantSeed, 0, 32);
        
        FixedSecureRandom fixedRandom = new FixedSecureRandom(quantSeed);
        MLDSAKeyPairGenerator mlGen = new MLDSAKeyPairGenerator();
        mlGen.init(new MLDSAKeyGenerationParameters(fixedRandom, MLDSAParameters.ml_dsa_65));
        AsymmetricCipherKeyPair mlKp = mlGen.generateKeyPair();
        byte[] privQuant = ((MLDSAPrivateKeyParameters) mlKp.getPrivate()).getEncoded();
        byte[] pubQuant = ((MLDSAPublicKeyParameters) mlKp.getPublic()).getEncoded();
        
        this.keyPair = new JamiiKeyPair(privTrad, privQuant, pubTrad, pubQuant);
    }

    // Construtor para importar carteira a partir de mnemônico
    public JamiiWallet(String mnemonic, String password) throws Exception {
        this.password = password;
        this.mnemonic = mnemonic;
        
        byte[] seed = MnemonicUtils.generateSeed(mnemonic, "");
        
        // Derivar tradicional (Secp256k1) via BIP-32 m/44'/60'/0'/0/0
        Bip32ECKeyPair master = Bip32ECKeyPair.generateKeyPair(seed);
        int[] path = {
            44 | Bip32ECKeyPair.HARDENED_BIT,
            60 | Bip32ECKeyPair.HARDENED_BIT,
            0 | Bip32ECKeyPair.HARDENED_BIT,
            0,
            0
        };
        Bip32ECKeyPair child = Bip32ECKeyPair.deriveKeyPair(master, path);
        byte[] privTrad = child.getPrivateKey().toByteArray();
        if (privTrad.length > 32) {
            byte[] tmp = new byte[32];
            System.arraycopy(privTrad, privTrad.length - 32, tmp, 0, 32);
            privTrad = tmp;
        } else if (privTrad.length < 32) {
            byte[] tmp = new byte[32];
            System.arraycopy(privTrad, 0, tmp, 32 - privTrad.length, privTrad.length);
            privTrad = tmp;
        }
        
        ECPrivateKeyParameters ecPriv = new ECPrivateKeyParameters(new BigInteger(1, privTrad), CURVE);
        ECPublicKeyParameters ecPub = new ECPublicKeyParameters(CURVE.getG().multiply(ecPriv.getD()), CURVE);
        byte[] pubTrad = ecPub.getQ().getEncoded(true);
        
        // Derivar quântica (ML-DSA-65) a partir do quantSeed via HMAC-SHA512
        HMac hmac = new HMac(new SHA512Digest());
        hmac.init(new KeyParameter("Jamii ML-DSA Seed".getBytes(StandardCharsets.UTF_8)));
        hmac.update(seed, 0, seed.length);
        byte[] hmacOut = new byte[64];
        hmac.doFinal(hmacOut, 0);
        byte[] quantSeed = new byte[32];
        System.arraycopy(hmacOut, 0, quantSeed, 0, 32);
        
        FixedSecureRandom fixedRandom = new FixedSecureRandom(quantSeed);
        MLDSAKeyPairGenerator mlGen = new MLDSAKeyPairGenerator();
        mlGen.init(new MLDSAKeyGenerationParameters(fixedRandom, MLDSAParameters.ml_dsa_65));
        AsymmetricCipherKeyPair mlKp = mlGen.generateKeyPair();
        byte[] privQuant = ((MLDSAPrivateKeyParameters) mlKp.getPrivate()).getEncoded();
        byte[] pubQuant = ((MLDSAPublicKeyParameters) mlKp.getPublic()).getEncoded();
        
        this.keyPair = new JamiiKeyPair(privTrad, privQuant, pubTrad, pubQuant);
    }

    public String getMnemonic() {
        return mnemonic;
    }

    public JamiiKeyPair getKeyPair() {
        return keyPair;
    }

    public JamiiAddress getAddress() {
        return keyPair.getAddress();
    }

    public void saveToFile(File file) throws Exception {
        // Formato Jamii Private Key Híbrida: [Hybrid(1)][lenTrad(4)][0x00][privTrad][0x01][privQuant]
        byte[] privTrad = keyPair.getTraditionalPrivateKey();
        byte[] privQuant = keyPair.getQuantumPrivateKey();
        
        ByteBuffer buf = ByteBuffer.allocate(1 + 4 + (1 + privTrad.length) + (1 + privQuant.length));
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.put((byte) 0x02); // Hybrid
        buf.putInt(1 + privTrad.length);
        buf.put((byte) 0x00); // Secp256k1 prefix
        buf.put(privTrad);
        buf.put((byte) 0x01); // ML-DSA prefix
        buf.put(privQuant);
        byte[] plaintext = buf.array();

        // KDF: SCrypt
        byte[] salt = new byte[32];
        new SecureRandom().nextBytes(salt);
        int n = 131072, r = 8, p = 1, dklen = 32;
        byte[] derivedKey = SCrypt.generate(password.getBytes(StandardCharsets.UTF_8), salt, n, r, p, dklen);

        // Cipher: AES-256-GCM
        byte[] iv = new byte[12];
        new SecureRandom().nextBytes(iv);
        GCMBlockCipher gcm = new GCMBlockCipher(new AESEngine());
        AEADParameters params = new AEADParameters(new KeyParameter(derivedKey), 128, iv);
        gcm.init(true, params);

        byte[] ciphertext = new byte[gcm.getOutputSize(plaintext.length)];
        int len = gcm.processBytes(plaintext, 0, plaintext.length, ciphertext, 0);
        gcm.doFinal(ciphertext, len);

        // Build JSON
        ObjectNode root = objectMapper.createObjectNode();
        root.put("address", keyPair.getAddress().toJamii1());
        root.put("mirror", keyPair.getAddress().toEthereum());
        root.put("id", UUID.randomUUID().toString());
        root.put("version", 3);

        ObjectNode crypto = root.putObject("crypto");
        crypto.put("ciphertext", Hex.toHexString(ciphertext));
        crypto.put("cipher", "aes-256-gcm");
        
        ObjectNode cipherParams = crypto.putObject("cipherparams");
        cipherParams.put("iv", Hex.toHexString(iv));

        ObjectNode kdfParams = crypto.putObject("kdfparams");
        kdfParams.put("n", n);
        kdfParams.put("r", r);
        kdfParams.put("p", p);
        kdfParams.put("dklen", dklen);
        kdfParams.put("salt", Hex.toHexString(salt));
        crypto.put("kdf", "scrypt");

        try (FileOutputStream fos = new FileOutputStream(file)) {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(fos, root);
        }
    }

    private byte[] decryptKeystore(File keystoreFile, String password) throws Exception {
        JsonNode root = objectMapper.readTree(keystoreFile);
        JsonNode crypto = root.get("crypto");
        JsonNode kdf = crypto.get("kdfparams");
        
        byte[] salt = Hex.decode(kdf.get("salt").asText());
        int n = kdf.get("n").asInt();
        int r = kdf.get("r").asInt();
        int p = kdf.get("p").asInt();
        int dklen = kdf.get("dklen").asInt();

        byte[] derivedKey = SCrypt.generate(password.getBytes(StandardCharsets.UTF_8), salt, n, r, p, dklen);

        byte[] iv = Hex.decode(crypto.get("cipherparams").get("iv").asText());
        byte[] ciphertext = Hex.decode(crypto.get("ciphertext").asText());

        GCMBlockCipher gcm = new GCMBlockCipher(new AESEngine());
        AEADParameters params = new AEADParameters(new KeyParameter(derivedKey), 128, iv);
        gcm.init(false, params);

        byte[] plaintext = new byte[gcm.getOutputSize(ciphertext.length)];
        int len = gcm.processBytes(ciphertext, 0, ciphertext.length, plaintext, 0);
        gcm.doFinal(plaintext, len);

        return plaintext;
    }
}

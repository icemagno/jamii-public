package com.jamii.sdk.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class JamiiWalletTest {

    @Test
    public void testWalletGenerationAndRecovery() throws Exception {
        String password = "jamii-test-password-123";
        
        // 1. Gerar carteira
        JamiiWallet original = new JamiiWallet(password);
        
        String mnemonic = original.getMnemonic();
        assertNotNull(mnemonic, "Mnemonic should not be null");
        
        String[] words = mnemonic.split("\\s+");
        assertEquals(12, words.length, "Mnemonic must have exactly 12 words");
        
        String sovereignAddress = original.getAddress().toJamii1();
        String mirrorAddress = original.getAddress().toEthereum();
        assertNotNull(sovereignAddress, "Sovereign address should not be null");
        assertTrue(sovereignAddress.startsWith("jamii1"), "Address should be Bech32 style starting with 'jamii'");
        assertTrue(mirrorAddress.startsWith("0x"), "Mirror address should be Ethereum style starting with '0x'");

        // 2. Recuperar a partir do mnemônico
        JamiiWallet recovered = new JamiiWallet(mnemonic, password);
        
        assertEquals(sovereignAddress, recovered.getAddress().toJamii1(), "Addresses must match after recovery");
        assertEquals(mirrorAddress, recovered.getAddress().toEthereum(), "Mirror addresses must match after recovery");
        
        assertArrayEquals(original.getKeyPair().getTraditionalPrivateKey(), 
                         recovered.getKeyPair().getTraditionalPrivateKey(), 
                         "Traditional private keys must match");
                         
        assertArrayEquals(original.getKeyPair().getQuantumPrivateKey(), 
                         recovered.getKeyPair().getQuantumPrivateKey(), 
                         "Quantum private keys must match");
    }

    @Test
    public void testGoWalletCliCompatibility() throws Exception {
        String mnemonic = "gravity classic high dynamic review modify print visual direct match focus track";
        String expectedSovereign = "jamii1zj8z6kld653ggmmyhj0hfatmpkhst0c8znzvmyw";
        String expectedMirror = "0x91c5aB7dbAa4508dec9793EE9EAF61b5e0b7E0e2";
        
        JamiiWallet wallet = new JamiiWallet(mnemonic, "jamii-password");
        
        assertEquals(expectedSovereign, wallet.getAddress().toJamii1(), "Sovereign address must match Go CLI output");
        assertEquals(expectedMirror.toLowerCase(), wallet.getAddress().toEthereum().toLowerCase(), "Mirror address must match Go CLI output");
    }

    @Test
    public void testKeystoreRoundtrip(@TempDir File tempDir) throws Exception {
        String password = "super-secret-jamii";
        JamiiWallet original = new JamiiWallet(password);
        
        File keystoreFile = new File(tempDir, "keystore.json");
        original.saveToFile(keystoreFile);
        
        assertTrue(keystoreFile.exists(), "Keystore file must be created");
        
        // 1. Carregar com a senha correta
        JamiiWallet reloaded = new JamiiWallet(keystoreFile, password);
        assertEquals(original.getAddress().toJamii1(), reloaded.getAddress().toJamii1(), "Addresses must match after reload");
        
        assertArrayEquals(original.getKeyPair().getTraditionalPrivateKey(), 
                         reloaded.getKeyPair().getTraditionalPrivateKey(), 
                         "Traditional private keys must match after reload");
                         
        assertArrayEquals(original.getKeyPair().getQuantumPrivateKey(), 
                         reloaded.getKeyPair().getQuantumPrivateKey(), 
                         "Quantum private keys must match after reload");

        // 2. Carregar com a senha incorreta deve falhar
        assertThrows(Exception.class, () -> {
            new JamiiWallet(keystoreFile, "wrong-password");
        }, "Decryption with wrong password must fail");
    }
}

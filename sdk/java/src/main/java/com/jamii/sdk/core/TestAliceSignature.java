package com.jamii.sdk.core;

import com.jamii.sdk.crypto.JamiiKeyPair;
import com.jamii.sdk.util.NumericUtil;
import org.bouncycastle.util.encoders.Hex;
import java.io.File;

public class TestAliceSignature {
    public static void main(String[] args) throws Exception {
        // EXACT BYTES FROM NODE LOG (JAVA FAIL CASE)
        byte[] digest = Hex.decode("4cce245a8e7c2c1758e099e7b76777bc3600b233ddddee39c8457ced79caa4f6");
        byte[] tradPub = Hex.decode("00027e284c493bc33bdf7c97ad472454e0e274c024e7f6950a044e5f414c1936c95d");
        
        // Let's extract QuantPub from the raw hex provided in the log
        // The raw hex provided by user has the full transaction. 
        // Offsets: PubKey is at 201 bytes (0xC9)
        // PubKey structure: [0x02][lenTrad(4)][tradPub(34)][quantPub(2593)]
        
        String rawHex = "010000006b270000000000000852000000000000ea0700000000000038000000580000007800000098000000ad000000ad0000007508000000000000000000000000000000000000000000000000000000000000773594000000000000000000000000000000000000000000000000000000000005f5e1000000000000000000000000000000000000000000000000000000000000000001021930fde2d1795992b5411e5ae17b5918d016f300022200000000027e284c493bc33bdf7c97ad472454e0e274c024e7f6950a044e5f414c1936c95d01014aa7da6b53942002a59c57140e10e39292e3903e11cedf280d532c541991f2c6241b8645320f2a842e2011fe56eddacb433b0f4f98853c4fad5f740187cd6175eda5b7cca7065ba5384b79cc0a7669c83e152f6ff1c390d242c7dab643d9ec6b0c137802a1a6eaff1eacdc1af9d5a7a41e04e9b8cbb62d39ad125b9ad0a0968bc2281283e5b004a5a7ed398a835ea271182ffbcd16e0138bd5846d9a9d0b59bc7c0c8b99e92778b4a4f8876435a85b4ac161ba781143e5dab4634e3b51a9c3b81d996f4a28c55648f7ac36b75318e57a9db3c891eb3fa12773a47a4c7d2489508be785561bd328625694b28b801f1f71c40a347c5703f5fe89cc9d0f2e44e4f67a23fbf609a1833294806e6fa5da92288c03a592a8599acaf7080f3ec009a41d5bd5b4e157c98969053073e2ae57c3cd7bf64db9af39222a975eae19371db9a7c27216e3318d9907c42142c781539a39f36816cd55dfed90b5aaf1004ac130f2d33cfab1cef613e103156e08f929d22d38041bfc89ee799f74c955f74296f14a81a82e352b059f090e591584a7e2468dda79e8170d966a94ce4f172a97c4440729483ef82ad56d85e1f71a408ea84a017fe56c9301a48521ce01ca7b27f07f5b5379b04dfb00f72ca77328b650c519a74dc0b2bab550215799c6c742b33859a5770dddd7864af4084465b6eeae1c54423ae9465ff5df98ffc9eaf5709d7e0f8c34968f348a063b4db7107eb94084225629feab5d361eff512b4cf5ee92c9664d83455738e8364b45e99c678242d6e81da4ca2acfaf6f8c1cb8fc30bb5db18af0cf4a8c3445a6f511f42e01a199e4f1a11aa7a9502ec9ca0dbf5f53c6d97906f2473fb782d36a20bb8d4c282a278a038f82efbbcb9ebeb3c2d0c5b589431e096951abe412c2d5826fcf7b3b9b278b9a993ccb08f9f82c86b8cf32803e7c15ff55e547a14d77fec8d59f65e109962b6f34ff8cb0959607569bd24b0ffe4c66a9e4a90181f3574bbbc31076f58f3261ee04f8671d4e03cd3c4e1a2539392e1b88733c5803853bfd9d87ad4daf878170d58c603c90da0a9cfec9c3bec5394c94e47949301317e8aaf9b8351da8ef639a14979d7396b995bb6a08c1bab6891f02d59d494ba57bfcda3a9587e55aeef20c2b2b2dd6edb20d1cd98d4f87b0059602cb4425875409caf13e3c81c9d72204883fbb62395af7aab2b9a2ccdfb6627144947fd01bafd4fdbd4a7e0057be6d8713a7a9c45936c40a4ccbd207e8b0f68ca9ee0e58af16487d7987e9606493d0a9786eb8d57a66fa74d25d3b172622916c614d0aeaa0acd5e603111a2ad6e4e5c1f66cc8a9d8622d21c821c216b1f89b2df1342f9291f64e46d7212d05813a8b4bf389dcbbbedb3f9c0c43fef0214d2ad5ebdbbedbe86ef52ad4929a77cbd9df4300bd87c6f63c0a7efb2d32b01cc19a82e57217cb48655fa186e44a9a476700100046dfa553652dce3a7c257c3543550b884d02aa5c2deef4d85538c32888e4244584992a19da30fc8013151a7e92aac2ef36a71a8b0547447cd58c6d0afcda16768c16ae154ce58dd21ef56920cacf912639b5635aa14378581ab007be557c2536615133ca6f76d5a9d95abdf24062010e31c407e76cb2f1d0a77fbd4775bc625e39e0bb33f338de5c8447b25c118f52061c59dc532a889188bf8c52fca93a2f7390bc417213cac43b38baaaaa901fd231ff8558bea9bb6a97b290b1beab66d809a25ff6c00bb6d156b49bb87ddaf0d2ddbd4e46e721b860baaa54df55b941df5889d0765b835b2c3b8f6bcfcd8c15b22f4693689bcdb5866804aa022974accd5e1b6ebf68d2995f27c56c851c8b1f8a24dd43b46dd51ddb873f572e61166f186fa1c31ee18bc95acfcc7205f6a34422bb84da8c97581898389e76acf7e003b336be930b66dfe12b5115160910f94acdbe586a123ad1d541dd64652d788f5707746887948fde6803c9a9be74793881d5e8a4e20c910d02ee3935063575c5bbffabcca65d932e1cbf2faa70491254f81797e9ab20c9fa42e252cadb0c96ad2866253d4d6f3ee8785b51d3d69dbbe6d4ce9d51e86e2f407d9c5209a06dbc8669e149b9f4c8e89c7b4770d41737e024783da9c6c9e526831cedc56d93351f464393fe9a86b610d12ee2ab6a05f6b0bf078addc9d16cc40fb9d2935babe4bdd459eb6eddb9794f2a3f7720d10685ea65de195c9e7fda69ef60f4691ebdd23a6262628e7ec5def66fa51ec0d9f0d559aeed05a6ff5df441e5972f15df1efbf1c84c273acb50fd1709729632f4904bd328dfabd235ff8a9b5abab45876c9cc3607b9414707b215347152e8132e681cfbe73fae945278a3fdaf030a1b598a46837ed9aea8fe7148c69bdc1d39982e5c853d33142031c548e8d2e4c4ce797f331c05585468c7076db538991ec6bbefd9674011883b7fefacd1460a04cae43373cffe92f926589388b0409a21e573f62ab1afc435fb8186bd4a65993b308bd507915e7f4f869a2eded15e4c57bf243ce734782e04ff6b19b50dc2aded1466399e6024ef788e8aea77c2b9fb3cc89f79322f6cad45c6ebab45fe8227c7f18f256f6e0c03bfb31a8af7d777557a22daf6c2a8b298808cf1eb4277ca0fc65637a728bc6d42de87be4c909123217ed9017b3d4da06401a8be7179c0cd17af9e220010839efae0c5999ced839a8cbb3ad5294f6d53201605118921f0f2c408be0b42a1ccee6aaf673c77fd85a8e0bd7b203bada92b5c50e4c1";
        byte[] raw = Hex.decode(rawHex);
        
        // PubKey offset is at byte 48:52 (30:34 hex index)
        // But wait, the rawHex has offsets starting at 28.
        // Offsets (7 slots): 0, 1, 2, 3, 4, 5, 6
        // PubKey offset is slot 5.
        // Index in raw: 28 + (5 * 4) = 48
        // Let's decode offsets properly
        java.nio.ByteBuffer buf = java.nio.ByteBuffer.wrap(raw).order(java.nio.ByteOrder.LITTLE_ENDIAN);
        int off5 = buf.getInt(48);
        int off6 = buf.getInt(52);
        
        byte[] pubKeyFull = new byte[off6 - off5];
        System.arraycopy(raw, off5, pubKeyFull, 0, pubKeyFull.length);
        
        // Structure of pubKeyFull: [0x02][lenTrad(4)][tradPub(34)][quantPub(2593?)]
        java.nio.ByteBuffer pBuf = java.nio.ByteBuffer.wrap(pubKeyFull).order(java.nio.ByteOrder.LITTLE_ENDIAN);
        pBuf.get(); // 0x02
        int tradLen = pBuf.getInt();
        byte[] trad = new byte[tradLen];
        pBuf.get(trad);
        byte[] quant = new byte[pBuf.remaining()];
        pBuf.get(quant);
        
        System.out.println("Java Re-extracted TradPub: " + Hex.toHexString(trad));
        System.out.println("Java Re-extracted QuantPub (first 10): " + Hex.toHexString(java.util.Arrays.copyOf(quant, 10)));

        byte[] bound = NumericUtil.keccak256(NumericUtil.concat(digest, trad, quant));
        System.out.println("Java Re-calculated Bound: " + Hex.toHexString(bound));
    }
}

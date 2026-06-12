package com.jamii.sdk.address;

import com.jamii.sdk.bech32.Bech32;
import org.bouncycastle.util.encoders.Hex;

public class JamiiAddress {
    private final byte version;
    private final byte[] hash;
    private static final String HRP = "jamii";

    public JamiiAddress(byte version, byte[] hash) {
        if (hash.length != 20) throw new IllegalArgumentException("Hash de endereço deve ter 20 bytes");
        this.version = version;
        this.hash = hash;
    }

    public JamiiAddress(byte[] hash) {
        this((byte) 2, hash); // Default V2
    }

    public static JamiiAddress fromBech32(String bech32Addr) {
        if (bech32Addr.startsWith("0x")) {
            return new JamiiAddress((byte) 0, Hex.decode(bech32Addr.substring(2)));
        }
        Bech32.Bech32Data decoded = Bech32.decode(bech32Addr);
        if (decoded == null) throw new IllegalArgumentException("Endereço Bech32 inválido");
        if (!decoded.hrp.equals(HRP)) throw new IllegalArgumentException("HRP inválido: " + decoded.hrp);
        
        byte version = decoded.data[0];
        byte[] payload5 = new byte[decoded.data.length - 1];
        System.arraycopy(decoded.data, 1, payload5, 0, payload5.length);
        byte[] hash = Bech32.convertBits(payload5, 5, 8, false);
        if (hash == null) throw new IllegalArgumentException("Falha na conversão de bits do payload");
        
        return new JamiiAddress(version, hash);
    }

    public String toJamii1() {
        if (version == 0) return toEthereum();
        byte[] converted = Bech32.convertBits(hash, 8, 5, true);
        byte[] versioned = new byte[converted.length + 1];
        versioned[0] = version;
        System.arraycopy(converted, 0, versioned, 1, converted.length);
        return Bech32.encode(HRP, versioned);
    }

    public String toEthereum() {
        return "0x" + Hex.toHexString(hash);
    }

    public byte getVersion() { return version; }
    public byte[] getHash() { return hash; }

    @Override
    public String toString() { return toJamii1(); }
}

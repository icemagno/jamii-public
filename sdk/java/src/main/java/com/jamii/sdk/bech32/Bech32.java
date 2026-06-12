package com.jamii.sdk.bech32;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

public class Bech32 {
    private static final String CHARSET = "qpzry9x8gf2tvdw0s3jn54khce6mua7l";

    public static class Bech32Data {
        public final String hrp;
        public final byte[] data;
        public Bech32Data(String hrp, byte[] data) { this.hrp = hrp; this.data = data; }
    }

    public static String encode(String hrp, byte[] values) {
        // JAMII ALIGNMENT: Go implementation (btcutil/bech32) uses classic Bech32 (BIP 173)
        // for ALL addresses, regardless of version.
        byte[] checksum = createChecksum(hrp, values, false); // ALWAYS FALSE
        byte[] combined = new byte[values.length + checksum.length];
        System.arraycopy(values, 0, combined, 0, values.length);
        System.arraycopy(checksum, 0, combined, values.length, checksum.length);
        StringBuilder sb = new StringBuilder(hrp.length() + 1 + combined.length);
        sb.append(hrp).append('1');
        for (byte b : combined) sb.append(CHARSET.charAt(b));
        return sb.toString();
    }

    private static int polymod(byte[] values) {
        int chk = 1;
        for (byte v : values) {
            int b = chk >> 25;
            chk = ((chk & 0x1ffffff) << 5) ^ (v & 0xff);
            if ((b & 1) != 0) chk ^= 0x3b6a57b2;
            if ((b & 2) != 0) chk ^= 0x26508e6d;
            if ((b & 4) != 0) chk ^= 0x1ea119fa;
            if ((b & 8) != 0) chk ^= 0x3d4233dd;
            if ((b & 16) != 0) chk ^= 0x2a1462b3;
        }
        return chk;
    }

    private static byte[] expandHrp(String hrp) {
        int len = hrp.length();
        byte[] ret = new byte[len * 2 + 1];
        for (int i = 0; i < len; i++) {
            ret[i] = (byte) (hrp.charAt(i) >> 5);
            ret[i + len + 1] = (byte) (hrp.charAt(i) & 31);
        }
        return ret;
    }

    private static byte[] createChecksum(String hrp, byte[] data, boolean isBech32m) {
        byte[] expanded = expandHrp(hrp);
        byte[] values = new byte[expanded.length + data.length + 6];
        System.arraycopy(expanded, 0, values, 0, expanded.length);
        System.arraycopy(data, 0, values, expanded.length, data.length);
        // BIP-0350 Checksum constant: 1 for Bech32, 0x2bc830a3 for Bech32m
        int constant = isBech32m ? 0x2bc830a3 : 1;
        int mod = polymod(values) ^ constant;
        byte[] ret = new byte[6];
        for (int i = 0; i < 6; i++) ret[i] = (byte) ((mod >> (5 * (5 - i))) & 31);
        return ret;
    }

    public static Bech32Data decode(String bech) {
        if (bech.length() < 8 || bech.length() > 90) return null;
        int pos = bech.lastIndexOf('1');
        if (pos < 1 || pos + 7 > bech.length()) return null;
        String hrp = bech.substring(0, pos).toLowerCase();
        byte[] data = new byte[bech.length() - pos - 1];
        for (int i = 0; i < data.length; i++) {
            int d = CHARSET.indexOf(bech.toLowerCase().charAt(pos + 1 + i));
            if (d == -1) return null;
            data[i] = (byte) d;
        }
        
        // No Jamii, tentamos primeiro Bech32 clássico para combinar com btcutil
        if (verifyChecksum(hrp, data, 1)) {
            byte[] actualData = new byte[data.length - 6];
            System.arraycopy(data, 0, actualData, 0, data.length - 6);
            return new Bech32Data(hrp, actualData);
        }
        
        // Fallback para Bech32m se necessário para compatibilidade futura
        if (verifyChecksum(hrp, data, 0x2bc830a3)) {
            byte[] actualData = new byte[data.length - 6];
            System.arraycopy(data, 0, actualData, 0, data.length - 6);
            return new Bech32Data(hrp, actualData);
        }
        
        return null;
    }

    private static boolean verifyChecksum(String hrp, byte[] data, int constant) {
        byte[] expanded = expandHrp(hrp);
        byte[] values = new byte[expanded.length + data.length];
        System.arraycopy(expanded, 0, values, 0, expanded.length);
        System.arraycopy(data, 0, values, expanded.length, data.length);
        return polymod(values) == constant;
    }

    public static byte[] convertBits(byte[] data, int fromBits, int toBits, boolean pad) {
        int acc = 0;
        int bits = 0;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int maxv = (1 << toBits) - 1;
        for (byte value : data) {
            int v = value & 0xff;
            acc = (acc << fromBits) | v;
            bits += fromBits;
            while (bits >= toBits) {
                bits -= toBits;
                out.write((acc >> bits) & maxv);
            }
        }
        if (pad) {
            if (bits > 0) out.write((acc << (toBits - bits)) & maxv);
        } else if (bits >= fromBits || ((acc << (toBits - bits)) & maxv) != 0) {
            return null;
        }
        return out.toByteArray();
    }
}

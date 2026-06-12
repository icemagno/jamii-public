package com.jamii.sdk.bech32;

import org.bouncycastle.util.encoders.Hex;

public class TestBech32 {
    public static void main(String[] args) {
        byte[] payload = new byte[20];
        for (int i = 0; i < 20; i++) payload[i] = (byte) 0xAA;

        byte[] converted = Bech32.convertBits(payload, 8, 5, true);
        byte[] versioned = new byte[converted.length + 1];
        versioned[0] = 2; // Version 2
        System.arraycopy(converted, 0, versioned, 1, converted.length);
        
        String addr = Bech32.encode("jamii", versioned);
        System.out.println("Java Current Bech32: " + addr);
    }
}

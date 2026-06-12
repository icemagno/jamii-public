package com.jamii.sdk.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigInteger;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class JamiiBlock {
    public String hash;
    public String parentHash;
    public String sha3Uncles;
    public String miner;
    public String stateRoot;
    public String transactionsRoot;
    public String receiptsRoot;
    public String logsBloom;
    
    @JsonProperty("number")
    public String numberHex;
    
    @JsonProperty("gasLimit")
    public String gasLimitHex;
    
    @JsonProperty("gasUsed")
    public String gasUsedHex;
    
    @JsonProperty("timestamp")
    public String timestampHex;
    
    public List<Object> transactions; // Pode conter hashes (String) ou objetos completos

    public long getNumber() {
        return parseHex(numberHex).longValue();
    }

    public long getTimestamp() {
        return parseHex(timestampHex).longValue();
    }

    public BigInteger getGasUsed() {
        return parseHex(gasUsedHex);
    }

    public BigInteger getGasLimit() {
        return parseHex(gasLimitHex);
    }

    private BigInteger parseHex(String hex) {
        if (hex == null) return BigInteger.ZERO;
        if (hex.startsWith("0x")) {
            return new BigInteger(hex.substring(2), 16);
        }
        return new BigInteger(hex, 16);
    }
}

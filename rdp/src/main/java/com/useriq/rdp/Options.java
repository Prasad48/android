package com.useriq.rdp;

public class Options {
    final String host;
    final String emuId;
    final int maxSize;
    final int bitRate;

    public Options(String host, String emuId, int maxSize, int bitRate) {
        this.host = host;
        this.emuId = emuId;
        this.maxSize = maxSize;
        this.bitRate = bitRate;
    }

    @Override
    public String toString() {
        return "Options{" +
                "maxSize=" + maxSize +
                ", bitRate=" + bitRate +
                ", host='" + host + '\'' +
                ", emuId='" + emuId + '\'' +
                '}';
    }
}

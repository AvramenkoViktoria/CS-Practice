package com.naukma.network.messaging;

import java.util.HexFormat;

public class FakeSender implements Sender {
    @Override
    public void send(byte[] data) {
        System.out.println(
                "SEND: "
                        + HexFormat.of()
                        .formatHex(data)
        );
    }
}
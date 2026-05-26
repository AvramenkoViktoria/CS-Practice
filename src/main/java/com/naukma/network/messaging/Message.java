package com.naukma.network;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Message {
    private int type;
    private int userId;
    private byte[] payload;
}
package com.naukma.network.messaging;

public record RawMessage(byte[] data, String requestId) {}
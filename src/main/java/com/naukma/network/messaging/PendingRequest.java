package com.naukma.network.messaging;

public record PendingRequest(String requestId, ClientChannel channel) {}
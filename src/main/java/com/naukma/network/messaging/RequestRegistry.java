package com.naukma.network.messaging;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class RequestRegistry {

    private final ConcurrentHashMap<String, PendingRequest> byId = new ConcurrentHashMap<>();
    private final ConcurrentLinkedQueue<PendingRequest> fifo = new ConcurrentLinkedQueue<>();

    public void register(String requestId, PendingRequest pending) {
        byId.put(requestId, pending);
        fifo.add(pending);
    }

    public PendingRequest remove(String requestId) {
        PendingRequest pending = byId.remove(requestId);
        if (pending != null) {
            fifo.remove(pending);
        }
        return pending;
    }

    public PendingRequest pollOldest() {
        PendingRequest pending = fifo.poll();
        if (pending != null) {
            byId.remove(pending.requestId());
        }
        return pending;
    }

    public int size() {
        return byId.size();
    }
}

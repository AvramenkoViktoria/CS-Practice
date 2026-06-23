package com.naukma.network.messaging;

import lombok.AllArgsConstructor;

import java.util.logging.Logger;

@AllArgsConstructor
public class MultiProtocolSender implements Sender {

    private static final Logger log = Logger.getLogger(MultiProtocolSender.class.getName());

    private final RequestRegistry registry;

    @Override
    public void send(byte[] data) {
        PendingRequest pending = registry.pollOldest();

        if (pending == null) {
            log.warning("Sender: no pending request to deliver response to - " + data.length + " bytes dropped.");
            return;
        }

        deliverTo(pending, data);
    }

    private void deliverTo(PendingRequest pending, byte[] data) {
        try {
            pending.channel().send(data);
            log.fine("Sender: delivered " + data.length + " bytes for request " + pending.requestId());
        } catch (Exception e) {
            log.warning("Sender: failed to deliver response for request "
                    + pending.requestId() + " — " + e.getMessage());
        }
    }
}

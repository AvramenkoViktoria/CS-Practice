package com.naukma.network.encryption;

import com.naukma.network.messaging.Message;
import com.naukma.network.messaging.MessageMapper;
import com.naukma.network.messaging.RawMessage;
import com.naukma.network.packet.Packet;
import lombok.AllArgsConstructor;

import java.util.concurrent.BlockingQueue;

@AllArgsConstructor
public class DecoderWorker implements Runnable {
    private final BlockingQueue<RawMessage> input;

    private final BlockingQueue<Packet> output;

    private final PacketDecoder decoder;

    private final MessageMapper mapper;

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                RawMessage raw = input.take();
                Message message = decoder.decode(raw.data());
                Packet packet = mapper.toPacket(message);
                output.put(packet);
            }
        } catch (Exception ignored) {
        }
    }
}
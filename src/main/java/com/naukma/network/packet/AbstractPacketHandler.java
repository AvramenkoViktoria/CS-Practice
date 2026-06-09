package com.naukma.network.packet;

import com.naukma.model.Warehouse;
import lombok.AllArgsConstructor;

import java.sql.SQLException;

@AllArgsConstructor
public abstract class AbstractPacketHandler<T extends Packet> implements PacketHandler<T> {

    protected final Warehouse warehouse;

    @Override
    public final String process(T packet) {
        try {
            return doProcess(packet);
        } catch (SQLException e) {
            return "Error: Database error - " + e.getMessage();
        } catch (IllegalArgumentException e) {
            return "Error: Invalid input - " + e.getMessage();
        } catch (Exception e) {
            return "Error: Unexpected error in " + getClass().getSimpleName() + " - " + e.getMessage();
        }
    }

    protected abstract String doProcess(T packet) throws SQLException;

    @Override
    public abstract Class<T> getPacketType();
}
/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2024 ViaVersion and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.viaversion.viaversion.protocols.v1_20_5to1_21.storage;

import com.viaversion.viaversion.api.connection.StorableObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.Protocol1_20_5To1_21;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundPackets1_21;

public final class EfficiencyAttributeStorage implements StorableObject {

    private static final int MINING_EFFICIENCY_ID = 19;
    private final Object lock = new Object(); // Slightly sloppy locking, but should be good enough
    private volatile boolean loginSent;
    private volatile StoredEfficiency efficiencyLevel;

    public void setEfficiencyLevel(final StoredEfficiency efficiencyLevel, final UserConnection connection) {
        this.efficiencyLevel = efficiencyLevel;
        sendAttributesPacket(connection);
    }

    public void onLoginSent(final UserConnection connection) {
        this.loginSent = true;
        sendAttributesPacket(connection);
    }

    private void sendAttributesPacket(final UserConnection connection) {
        final StoredEfficiency efficiency;
        synchronized (lock) {
            // Older servers (and often Bungee) will send world state packets before sending the login packet
            if (!loginSent || efficiencyLevel == null) {
                return;
            }

            efficiency = efficiencyLevel;
            efficiencyLevel = null;
        }

        final PacketWrapper attributesPacket = PacketWrapper.create(ClientboundPackets1_21.UPDATE_ATTRIBUTES, connection);
        attributesPacket.write(Types.VAR_INT, efficiency.entityId());

        attributesPacket.write(Types.VAR_INT, 1); // Size
        attributesPacket.write(Types.VAR_INT, MINING_EFFICIENCY_ID); // Attribute ID
        attributesPacket.write(Types.DOUBLE, 0D); // Base

        final int level = efficiency.level;
        if (level > 0) {
            final double modifierAmount = (level * level) + 1D;
            attributesPacket.write(Types.VAR_INT, 1); // Modifiers
            attributesPacket.write(Types.STRING, "minecraft:enchantment.efficiency/mainhand"); // Id
            attributesPacket.write(Types.DOUBLE, modifierAmount);
            attributesPacket.write(Types.BYTE, (byte) 0); // 'Add' operation
        } else {
            attributesPacket.write(Types.VAR_INT, 0); // Modifiers
        }

        attributesPacket.scheduleSend(Protocol1_20_5To1_21.class);
    }

    public record StoredEfficiency(int entityId, int level) {
    }
}

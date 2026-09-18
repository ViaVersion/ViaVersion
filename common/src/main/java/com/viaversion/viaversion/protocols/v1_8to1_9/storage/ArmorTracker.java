/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2026 ViaVersion and contributors
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
package com.viaversion.viaversion.protocols.v1_8to1_9.storage;

import com.viaversion.viaversion.api.connection.StorableObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_8to1_9.Protocol1_8To1_9;
import com.viaversion.viaversion.protocols.v1_8to1_9.data.ArmorTypes1_8;
import com.viaversion.viaversion.protocols.v1_8to1_9.packet.ClientboundPackets1_9;
import java.util.UUID;

public final class ArmorTracker implements StorableObject {

    private static final UUID ARMOR_ATTRIBUTE = UUID.fromString("2AD3F246-FEE1-4E67-B886-69FD380BB150");

    // Helmet through boots, matching player container slots 5 through 8.
    private final int[] armorPoints = new int[4];

    public void setArmor(int slot, int itemId) {
        armorPoints[slot] = ArmorTypes1_8.findById(itemId).getArmorPoints();
    }

    public void sendArmorUpdate(UserConnection connection) {
        EntityTracker1_9 entityTracker = connection.getEntityTracker(Protocol1_8To1_9.class);
        if (!entityTracker.hasClientEntityId()) {
            return;
        }

        int armor = 0;
        for (int points : armorPoints) {
            armor += points;
        }

        PacketWrapper wrapper = PacketWrapper.create(ClientboundPackets1_9.UPDATE_ATTRIBUTES, connection);
        wrapper.write(Types.VAR_INT, entityTracker.clientEntityId());
        wrapper.write(Types.INT, 1);
        wrapper.write(Types.STRING, "generic.armor");
        wrapper.write(Types.DOUBLE, 0D);
        wrapper.write(Types.VAR_INT, 1);
        wrapper.write(Types.UUID, ARMOR_ATTRIBUTE);
        wrapper.write(Types.DOUBLE, (double) armor);
        wrapper.write(Types.BYTE, (byte) 0);
        wrapper.scheduleSend(Protocol1_8To1_9.class);
    }
}

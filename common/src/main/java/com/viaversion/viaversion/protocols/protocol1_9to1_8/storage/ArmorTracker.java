/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2022 ViaVersion and contributors
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
package com.viaversion.viaversion.protocols.protocol1_9to1_8.storage;

import com.viaversion.viaversion.api.connection.StorableObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.ArmorType;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.ClientboundPackets1_9;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.Protocol1_9To1_8;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ArmorTracker implements StorableObject {

    public final Map<Short, Integer> armorSlotsTracker = new HashMap<>();
    private double armorPoints;

    public void onSetSlot(final short slotId, final int itemId, final UserConnection userConnection) {
        if (slotId >= 5 && slotId <= 8) { // Armor slots
            if (!armorSlotsTracker.containsKey(slotId) && itemId != 0) {
                armorSlotsTracker.put(slotId, itemId);
                armorPoints += ArmorType.findById(itemId).getArmorPoints();
            } else {
                final int lastItem = armorSlotsTracker.get(slotId);
                armorSlotsTracker.remove(slotId);
                armorPoints -= ArmorType.findById(lastItem).getArmorPoints();
            }

            EntityTracker1_9 tracker = userConnection.getEntityTracker(Protocol1_9To1_8.class);
            final PacketWrapper propertiesPacket = PacketWrapper.create(ClientboundPackets1_9.ENTITY_PROPERTIES, userConnection);

            propertiesPacket.write(Type.VAR_INT, tracker.getProvidedEntityId());
            propertiesPacket.write(Type.INT, 1);
            propertiesPacket.write(Type.STRING, "generic.armor");
            propertiesPacket.write(Type.DOUBLE, 0.0);
            propertiesPacket.write(Type.VAR_INT, 1);
            propertiesPacket.write(Type.UUID, UUID.fromString("2AD3F246-FEE1-4E67-B886-69FD380BB150"));
            propertiesPacket.write(Type.DOUBLE, armorPoints);
            propertiesPacket.write(Type.BYTE, (byte) 0);

            try {
                propertiesPacket.scheduleSend(Protocol1_9To1_8.class);
            } catch (Exception ignored) {
            }
        }
    }
}

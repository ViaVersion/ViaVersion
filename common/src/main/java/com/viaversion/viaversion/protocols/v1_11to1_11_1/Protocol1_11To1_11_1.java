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
package com.viaversion.viaversion.protocols.v1_11to1_11_1;

import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_11to1_11_1.rewriter.ItemPacketRewriter1_11_1;
import com.viaversion.viaversion.protocols.v1_9_1to1_9_3.packet.ClientboundPackets1_9_3;
import com.viaversion.viaversion.protocols.v1_9_1to1_9_3.packet.ServerboundPackets1_9_3;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public class Protocol1_11To1_11_1 extends AbstractProtocol<ClientboundPackets1_9_3, ClientboundPackets1_9_3, ServerboundPackets1_9_3, ServerboundPackets1_9_3> {

    private final ItemPacketRewriter1_11_1 itemRewriter = new ItemPacketRewriter1_11_1(this);

    public Protocol1_11To1_11_1() {
        super(ClientboundPackets1_9_3.class, ClientboundPackets1_9_3.class, ServerboundPackets1_9_3.class, ServerboundPackets1_9_3.class);
    }

    @Override
    protected void registerPackets() {
        itemRewriter.register();

        registerClientbound(ClientboundPackets1_9_3.AWARD_STATS, wrapper -> {
            Object2IntMap<String> filteredStats = new Object2IntOpenHashMap<>();
            int size = wrapper.read(Types.VAR_INT);

            for (int i = 0; i < size; i++) {
                String name = wrapper.read(Types.STRING);
                int value = wrapper.read(Types.VAR_INT);

                if (name.equals("stat.treasureFished") || name.equals("stat.junkFished")) {
                    continue; // removed in 1.11.1
                }

                filteredStats.put(name, value);
            }

            wrapper.write(Types.VAR_INT, filteredStats.size()); // size

            for (final Object2IntMap.Entry<String> entry : filteredStats.object2IntEntrySet()) {
                wrapper.write(Types.STRING, entry.getKey()); // name
                wrapper.write(Types.VAR_INT, entry.getIntValue()); // value
            }
        });
    }

    @Override
    public ItemPacketRewriter1_11_1 getItemRewriter() {
        return itemRewriter;
    }
}

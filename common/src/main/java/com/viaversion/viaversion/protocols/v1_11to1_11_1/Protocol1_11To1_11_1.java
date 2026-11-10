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

public class Protocol1_11To1_11_1 extends AbstractProtocol<ClientboundPackets1_9_3, ClientboundPackets1_9_3, ServerboundPackets1_9_3, ServerboundPackets1_9_3> {

    private final ItemPacketRewriter1_11_1 itemRewriter = new ItemPacketRewriter1_11_1(this);

    public Protocol1_11To1_11_1() {
        super(ClientboundPackets1_9_3.class, ClientboundPackets1_9_3.class, ServerboundPackets1_9_3.class, ServerboundPackets1_9_3.class);
    }

    @Override
    protected void registerPackets() {
        itemRewriter.register();

        registerClientbound(ClientboundPackets1_9_3.AWARD_STATS, wrapper -> {
            int size = wrapper.passthrough(Types.VAR_INT);
            int removed = 0;

            for (int i = 0; i < size; i++) {
                String name = wrapper.read(Types.STRING);
                int value = wrapper.read(Types.VAR_INT);

                if (name.equals("stat.treasureFished") || name.equals("stat.junkFished")) { // removed in 1.11.1
                    removed++;
                    continue;
                }

                wrapper.write(Types.STRING, name); // name
                wrapper.write(Types.VAR_INT, value); // value
            }

            wrapper.set(Types.VAR_INT, 0, size - removed); // size
        });
    }

    @Override
    public ItemPacketRewriter1_11_1 getItemRewriter() {
        return itemRewriter;
    }
}

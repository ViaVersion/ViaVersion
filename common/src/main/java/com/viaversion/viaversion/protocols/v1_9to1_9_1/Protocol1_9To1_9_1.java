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
package com.viaversion.viaversion.protocols.v1_9to1_9_1;

import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_8to1_9.packet.ClientboundPackets1_9;
import com.viaversion.viaversion.protocols.v1_8to1_9.packet.ServerboundPackets1_9;

public class Protocol1_9To1_9_1 extends AbstractProtocol<ClientboundPackets1_9, ClientboundPackets1_9, ServerboundPackets1_9, ServerboundPackets1_9> {

    public Protocol1_9To1_9_1() {
        super(ClientboundPackets1_9.class, ClientboundPackets1_9.class, ServerboundPackets1_9.class, ServerboundPackets1_9.class);
    }

    @Override
    protected void registerPackets() {
        // Currently supports 1.9.1 and 1.9.2

        registerClientbound(ClientboundPackets1_9.LOGIN, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.INT); // 0 - Player ID
                map(Types.UNSIGNED_BYTE); // 1 - Player Gamemode
                // 1.9.1 PRE 2 Changed this
                map(Types.BYTE, Types.INT); // 2 - Player Dimension
                map(Types.UNSIGNED_BYTE); // 3 - World Difficulty
                map(Types.UNSIGNED_BYTE); // 4 - Max Players (Tab)
                map(Types.STRING); // 5 - Level Type
                map(Types.BOOLEAN); // 6 - Reduced Debug info
            }
        });

        registerClientbound(ClientboundPackets1_9.SOUND, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); // 0 - Sound ID

                handler(wrapper -> {
                    int sound = wrapper.get(Types.VAR_INT, 0);

                    if (sound >= 415) // Add 1 to every sound id since there is no Elytra sound on a 1.9 server
                        wrapper.set(Types.VAR_INT, 0, sound + 1);
                });
            }
        });
    }
}

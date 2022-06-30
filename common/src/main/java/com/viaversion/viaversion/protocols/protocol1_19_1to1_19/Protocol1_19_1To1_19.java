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
package com.viaversion.viaversion.protocols.protocol1_19_1to1_19;

import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.protocol.remapper.PacketRemapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.base.ServerboundLoginPackets;
import com.viaversion.viaversion.protocols.protocol1_19to1_18_2.ClientboundPackets1_19;
import com.viaversion.viaversion.protocols.protocol1_19to1_18_2.ServerboundPackets1_19;

public final class Protocol1_19_1To1_19 extends AbstractProtocol<ClientboundPackets1_19, ClientboundPackets1_19, ServerboundPackets1_19, ServerboundPackets1_19> {

    @Override
    protected void registerPackets() {
        // Skip 1.19 and assume 1.18.2->1.19.1 translation
        registerClientbound(ClientboundPackets1_19.SYSTEM_CHAT, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.COMPONENT);
                handler(wrapper -> {
                    wrapper.read(Type.VAR_INT); // Chat type
                    wrapper.write(Type.BOOLEAN, false); // Overlay
                });
            }
        });

        registerServerbound(State.LOGIN, ServerboundLoginPackets.HELLO.getId(), ServerboundLoginPackets.HELLO.getId(), new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.STRING); // Name
                map(Type.OPTIONAL_PROFILE_KEY); // Public profile key
                read(Type.OPTIONAL_UUID); // Profile uuid
            }
        });
    }
}

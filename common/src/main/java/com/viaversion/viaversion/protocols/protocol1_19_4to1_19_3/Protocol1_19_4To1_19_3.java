/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2023 ViaVersion and contributors
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
package com.viaversion.viaversion.protocols.protocol1_19_4to1_19_3;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.entities.Entity1_19_3Types;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.data.entity.EntityTrackerBase;
import com.viaversion.viaversion.protocols.protocol1_19_3to1_19_1.ClientboundPackets1_19_3;
import com.viaversion.viaversion.protocols.protocol1_19_3to1_19_1.Protocol1_19_3To1_19_1;
import com.viaversion.viaversion.protocols.protocol1_19_3to1_19_1.ServerboundPackets1_19_3;
import com.viaversion.viaversion.protocols.protocol1_19_4to1_19_3.packets.EntityPackets;
import com.viaversion.viaversion.rewriter.CommandRewriter;

public final class Protocol1_19_4To1_19_3 extends AbstractProtocol<ClientboundPackets1_19_3, ClientboundPackets1_19_4, ServerboundPackets1_19_3, ServerboundPackets1_19_4> {

    private final EntityPackets entityRewriter = new EntityPackets(this);

    public Protocol1_19_4To1_19_3() {
        super(ClientboundPackets1_19_3.class, ClientboundPackets1_19_4.class, ServerboundPackets1_19_3.class, ServerboundPackets1_19_4.class);
    }

    @Override
    protected void registerPackets() {
        entityRewriter.register();

        final CommandRewriter<ClientboundPackets1_19_3> commandRewriter = new CommandRewriter<ClientboundPackets1_19_3>(this) {
            @Override
            public void handleArgument(final PacketWrapper wrapper, final String argumentType) throws Exception {
                if (argumentType.equals("minecraft:time")) {
                    // Minimum
                    wrapper.write(Type.INT, 0);
                } else {
                    super.handleArgument(wrapper, argumentType);
                }
            }

            @Override
            protected String argumentType(final int argumentTypeId) {
                return Protocol1_19_3To1_19_1.MAPPINGS.getArgumentTypeMappings().mappedIdentifier(argumentTypeId);
            }

            @Override
            protected int mappedArgumentTypeId(final String mappedArgumentType) {
                return Protocol1_19_3To1_19_1.MAPPINGS.getArgumentTypeMappings().mappedId(mappedArgumentType);
            }
        };
        commandRewriter.registerDeclareCommands1_19(ClientboundPackets1_19_3.DECLARE_COMMANDS);
    }

    @Override
    public void init(final UserConnection user) {
        addEntityTracker(user, new EntityTrackerBase(user, Entity1_19_3Types.PLAYER));
    }
}
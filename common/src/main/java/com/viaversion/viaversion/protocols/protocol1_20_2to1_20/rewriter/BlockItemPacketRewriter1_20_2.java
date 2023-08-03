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
package com.viaversion.viaversion.protocols.protocol1_20_2to1_20.rewriter;

import com.viaversion.viaversion.api.minecraft.metadata.ChunkPosition;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_19_4to1_19_3.ClientboundPackets1_19_4;
import com.viaversion.viaversion.protocols.protocol1_20_2to1_20.Protocol1_20_2To1_20;
import com.viaversion.viaversion.protocols.protocol1_20_2to1_20.packet.ServerboundPackets1_20_2;
import com.viaversion.viaversion.rewriter.ItemRewriter;

public final class BlockItemPacketRewriter1_20_2 extends ItemRewriter<ClientboundPackets1_19_4, ServerboundPackets1_20_2, Protocol1_20_2To1_20> {

    public BlockItemPacketRewriter1_20_2(final Protocol1_20_2To1_20 protocol) {
        super(protocol);
    }

    @Override
    public void registerPackets() {
        protocol.registerClientbound(ClientboundPackets1_19_4.UNLOAD_CHUNK, wrapper -> {
            final int x = wrapper.read(Type.INT);
            final int z = wrapper.read(Type.INT);
            wrapper.write(Type.CHUNK_POSITION, new ChunkPosition(x, z));
        });
    }
}
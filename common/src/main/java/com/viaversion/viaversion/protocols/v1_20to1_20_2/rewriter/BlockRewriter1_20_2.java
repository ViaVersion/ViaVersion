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
package com.viaversion.viaversion.protocols.v1_20to1_20_2.rewriter;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.NumberTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.blockentity.BlockEntity;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_18;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_20_2;
import com.viaversion.viaversion.protocols.v1_19_3to1_19_4.packet.ClientboundPackets1_19_4;
import com.viaversion.viaversion.protocols.v1_20to1_20_2.data.PotionEffects1_20_2;
import com.viaversion.viaversion.rewriter.BlockRewriter;

public final class BlockRewriter1_20_2 extends BlockRewriter<ClientboundPackets1_19_4> {

    public BlockRewriter1_20_2(final Protocol<ClientboundPackets1_19_4, ?, ?, ?> protocol) {
        super(protocol, Types.BLOCK_POSITION1_14, Types.NAMED_COMPOUND_TAG, ChunkType1_18::new, ChunkType1_20_2::new);
    }

    @Override
    public void handleBlockEntity(final UserConnection connection, final BlockEntity blockEntity) {
        final CompoundTag tag = blockEntity.tag();
        if (tag == null) {
            return;
        }

        final Tag primaryEffect = tag.remove("Primary");
        if (primaryEffect instanceof NumberTag && ((NumberTag) primaryEffect).asInt() != 0) {
            tag.put("primary_effect", new StringTag(PotionEffects1_20_2.idToKeyOrLuck(((NumberTag) primaryEffect).asInt() - 1)));
        }

        final Tag secondaryEffect = tag.remove("Secondary");
        if (secondaryEffect instanceof NumberTag && ((NumberTag) secondaryEffect).asInt() != 0) {
            tag.put("secondary_effect", new StringTag(PotionEffects1_20_2.idToKeyOrLuck(((NumberTag) secondaryEffect).asInt() - 1)));
        }
    }
}

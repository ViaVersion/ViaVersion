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
package com.viaversion.viaversion.protocols.v1_21_2to1_21_4.rewriter;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.FloatTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.NumberTag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.blockentity.BlockEntity;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_21_2to1_21_4.Protocol1_21_2To1_21_4;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ClientboundPacket1_21_2;
import com.viaversion.viaversion.rewriter.BlockRewriter;
import com.viaversion.viaversion.util.TagUtil;

public final class BlockPacketRewriter1_21_4 extends BlockRewriter<ClientboundPacket1_21_2> {

    public BlockPacketRewriter1_21_4(final Protocol1_21_2To1_21_4 protocol) {
        super(protocol, Types.BLOCK_POSITION1_14, Types.COMPOUND_TAG);
    }

    @Override
    public void handleBlockEntity(final UserConnection connection, final BlockEntity blockEntity) {
        if (blockEntity.tag() == null) {
            return;
        }

        final CompoundTag item = blockEntity.tag().getCompoundTag("item");
        if (item == null) {
            return;
        }

        final CompoundTag components = item.getCompoundTag("components");
        if (components == null) {
            return;
        }

        // May be displayed in brushable blocks and other block entities
        final NumberTag customModelData = TagUtil.getNamespacedNumberTag(components, "custom_model_data");
        if (customModelData != null) {
            final ListTag<FloatTag> floats = new ListTag<>(FloatTag.class);
            floats.add(new FloatTag(customModelData.asFloat()));

            final CompoundTag updatedCustomModelData = new CompoundTag();
            updatedCustomModelData.put("floats", floats);
            TagUtil.removeNamespaced(components, "custom_model_data");
            components.put("custom_model_data", updatedCustomModelData);
        }
    }
}

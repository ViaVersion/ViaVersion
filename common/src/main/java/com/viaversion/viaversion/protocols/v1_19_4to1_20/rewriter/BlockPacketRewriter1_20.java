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
package com.viaversion.viaversion.protocols.v1_19_4to1_20.rewriter;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.blockentity.BlockEntity;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_19_3to1_19_4.packet.ClientboundPackets1_19_4;
import com.viaversion.viaversion.protocols.v1_19_4to1_20.Protocol1_19_4To1_20;
import com.viaversion.viaversion.rewriter.BlockRewriter;
import com.viaversion.viaversion.util.ComponentUtil;

public final class BlockPacketRewriter1_20 extends BlockRewriter<ClientboundPackets1_19_4> {

    public BlockPacketRewriter1_20(final Protocol1_19_4To1_20 protocol) {
        super(protocol, Types.BLOCK_POSITION1_14, Types.NAMED_COMPOUND_TAG);
    }

    @Override
    public void handleBlockEntity(final UserConnection connection, final BlockEntity blockEntity) {
        final CompoundTag tag = blockEntity.tag();
        if (blockEntity.tag() == null || (blockEntity.typeId() != 7 && blockEntity.typeId() != 8)) {
            return;
        }

        final CompoundTag frontText = new CompoundTag();
        tag.put("front_text", frontText);

        final ListTag<StringTag> messages = new ListTag<>(StringTag.class);
        for (int i = 1; i < 5; i++) {
            final Tag text = tag.remove("Text" + i);
            messages.add(text instanceof StringTag ? (StringTag) text : new StringTag(ComponentUtil.emptyJsonComponentString()));
        }
        frontText.put("messages", messages);

        final ListTag<StringTag> filteredMessages = new ListTag<>(StringTag.class);
        for (int i = 1; i < 5; i++) {
            final Tag text = tag.remove("FilteredText" + i);
            filteredMessages.add(text instanceof StringTag ? (StringTag) text : messages.get(i - 1));
        }
        if (!filteredMessages.equals(messages)) {
            frontText.put("filtered_messages", filteredMessages);
        }

        final Tag color = tag.remove("Color");
        if (color != null) {
            frontText.put("color", color);
        }

        final Tag glowing = tag.remove("GlowingText");
        if (glowing != null) {
            frontText.put("has_glowing_text", glowing);
        }
    }
}

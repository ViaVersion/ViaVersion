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
package com.viaversion.viaversion.protocols.v1_21_4to1_21_5.rewriter;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.blockentity.BlockEntity;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_21_4to1_21_5.Protocol1_21_4To1_21_5;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ClientboundPacket1_21_2;
import com.viaversion.viaversion.rewriter.BlockRewriter;

public final class BlockPacketRewriter1_21_5 extends BlockRewriter<ClientboundPacket1_21_2> {

    private static final int SIGN_BOCK_ENTITY_ID = 7;
    private static final int HANGING_SIGN_BOCK_ENTITY_ID = 8;
    private final Protocol1_21_4To1_21_5 protocol;

    public BlockPacketRewriter1_21_5(final Protocol1_21_4To1_21_5 protocol) {
        super(protocol, Types.BLOCK_POSITION1_14, Types.COMPOUND_TAG);
        this.protocol = protocol;
    }

    @Override
    public void handleBlockEntity(final UserConnection connection, final BlockEntity blockEntity) {
        final CompoundTag tag = blockEntity.tag();
        if (tag == null) {
            return;
        }

        if (blockEntity.typeId() == SIGN_BOCK_ENTITY_ID || blockEntity.typeId() == HANGING_SIGN_BOCK_ENTITY_ID) {
            updateSignMessages(connection, tag.getCompoundTag("front_text"));
            updateSignMessages(connection, tag.getCompoundTag("back_text"));
        }

        final String customName = tag.getString("CustomName");
        if (customName != null) {
            tag.put("CustomName", protocol.getComponentRewriter().uglyJsonToTag(connection, customName));
        }
    }

    private void updateSignMessages(final UserConnection connection, final CompoundTag tag) {
        if (tag == null) {
            return;
        }

        final ListTag<StringTag> messages = tag.getListTag("messages", StringTag.class);
        tag.put("messages", protocol.getComponentRewriter().updateComponentList(connection, messages, true));

        final ListTag<StringTag> filteredMessages = tag.getListTag("filtered_messages", StringTag.class);
        if (filteredMessages != null) {
            tag.put("filtered_messages", protocol.getComponentRewriter().updateComponentList(connection, filteredMessages, true));
        }
    }
}

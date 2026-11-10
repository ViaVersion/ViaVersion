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
package com.viaversion.viaversion.rewriter.block;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.FullMappings;
import com.viaversion.viaversion.api.minecraft.blockentity.BlockEntity;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.rewriter.BlockRewriter;
import org.checkerframework.checker.nullness.qual.Nullable;

public class BlockRewriter1_21_5<C extends ClientboundPacketType> extends BlockRewriter<C> {
    private int signId = -1;
    private int hangingSignId = -1;
    private int shelfId = -1;
    private int campfireId = -1;
    private int vaultId = -1;

    public BlockRewriter1_21_5(final Protocol<C, ?, ?, ?> protocol, final ChunkTypeSupplier chunkTypeSupplier, @Nullable final ChunkTypeSupplier mappedChunkTypeSupplier) {
        super(protocol, Types.BLOCK_POSITION1_14, Types.TRUSTED_COMPOUND_TAG, chunkTypeSupplier, mappedChunkTypeSupplier);
    }

    public BlockRewriter1_21_5(final Protocol<C, ?, ?, ?> protocol, final ChunkTypeSupplier chunkTypeSupplier) {
        this(protocol, chunkTypeSupplier, chunkTypeSupplier);
    }

    @Override
    public void handleBlockEntity(final UserConnection connection, final BlockEntity blockEntity) {
        final CompoundTag tag = blockEntity.tag();
        if (tag == null || protocol.getComponentRewriter() == null) {
            return;
        }

        final int typeId = blockEntity.typeId();
        if (typeId == signId || typeId == hangingSignId) {
            // Update sign text components, relevant as of 1.21.5 given they are properly parsed and should not error on the client
            updateSignMessages(connection, tag.getCompoundTag("front_text"));
            updateSignMessages(connection, tag.getCompoundTag("back_text"));
        } else if (typeId == shelfId || typeId == campfireId) {
            // Update items that are actually visible to the client
            final ListTag<CompoundTag> items = tag.getListTag("Items", CompoundTag.class);
            if (items != null) {
                for (final CompoundTag item : items) {
                    updateItem(connection, item);
                }
            }
        } else if (typeId == vaultId) {
            final CompoundTag sharedData = tag.getCompoundTag("shared_data");
            if (sharedData != null) {
                updateItem(connection, sharedData.getCompoundTag("display_item"));
            }
        }
    }

    @Override
    public void onMappingDataLoaded() {
        final FullMappings blockEntityMappings = protocol.getMappingData() != null ? protocol.getMappingData().getBlockEntityMappings() : null;
        if (blockEntityMappings != null) {
            signId = blockEntityMappings.mappedId("sign");
            hangingSignId = blockEntityMappings.mappedId("hanging_sign");
            shelfId = blockEntityMappings.mappedId("shelf");
            campfireId = blockEntityMappings.mappedId("campfire");
            vaultId = blockEntityMappings.mappedId("vault");
        }
    }

    private void updateItem(final UserConnection connection, @Nullable final CompoundTag item) {
        if (item != null && item.getStringTag("id") != null) {
            protocol.getComponentRewriter().handleShowItem(connection, item);
        }
    }

    public void updateSignMessages(final UserConnection connection, final CompoundTag tag) {
        if (tag == null) {
            return;
        }

        final ListTag<?> messages = tag.getListTag("messages");
        protocol.getComponentRewriter().processTag(connection, messages);

        final ListTag<?> filteredMessages = tag.getListTag("filtered_messages");
        if (filteredMessages != null) {
            protocol.getComponentRewriter().processTag(connection, filteredMessages);
        }
    }
}

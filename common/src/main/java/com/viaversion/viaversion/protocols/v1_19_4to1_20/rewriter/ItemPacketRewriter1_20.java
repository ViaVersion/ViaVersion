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

import com.viaversion.viaversion.api.minecraft.BlockChangeRecord;
import com.viaversion.viaversion.api.minecraft.chunks.Chunk;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_18;
import com.viaversion.viaversion.protocols.v1_19_3to1_19_4.packet.ClientboundPackets1_19_4;
import com.viaversion.viaversion.protocols.v1_19_3to1_19_4.packet.ServerboundPackets1_19_4;
import com.viaversion.viaversion.protocols.v1_19_3to1_19_4.rewriter.RecipeRewriter1_19_4;
import com.viaversion.viaversion.protocols.v1_19_4to1_20.Protocol1_19_4To1_20;
import com.viaversion.viaversion.rewriter.BlockRewriter;
import com.viaversion.viaversion.rewriter.ItemRewriter;
import com.viaversion.viaversion.rewriter.RecipeRewriter;
import com.viaversion.viaversion.util.Key;

public final class ItemPacketRewriter1_20 extends ItemRewriter<ClientboundPackets1_19_4, ServerboundPackets1_19_4, Protocol1_19_4To1_20> {

    public ItemPacketRewriter1_20(final Protocol1_19_4To1_20 protocol) {
        super(protocol, Types.ITEM1_13_2, Types.ITEM1_13_2_ARRAY);
    }

    @Override
    public void registerPackets() {
        final BlockRewriter<ClientboundPackets1_19_4> blockRewriter = new BlockPacketRewriter1_20(protocol);
        blockRewriter.registerBlockEvent(ClientboundPackets1_19_4.BLOCK_EVENT);
        blockRewriter.registerBlockUpdate(ClientboundPackets1_19_4.BLOCK_UPDATE);
        blockRewriter.registerLevelEvent(ClientboundPackets1_19_4.LEVEL_EVENT, 1010, 2001);
        blockRewriter.registerBlockEntityData(ClientboundPackets1_19_4.BLOCK_ENTITY_DATA);

        registerOpenScreen(ClientboundPackets1_19_4.OPEN_SCREEN);
        registerCooldown(ClientboundPackets1_19_4.COOLDOWN);
        registerSetContent1_17_1(ClientboundPackets1_19_4.CONTAINER_SET_CONTENT);
        registerSetSlot1_17_1(ClientboundPackets1_19_4.CONTAINER_SET_SLOT);
        registerSetEquipment(ClientboundPackets1_19_4.SET_EQUIPMENT);
        registerContainerClick1_17_1(ServerboundPackets1_19_4.CONTAINER_CLICK);
        registerMerchantOffers1_19(ClientboundPackets1_19_4.MERCHANT_OFFERS);
        registerSetCreativeModeSlot(ServerboundPackets1_19_4.SET_CREATIVE_MODE_SLOT);
        registerContainerSetData(ClientboundPackets1_19_4.CONTAINER_SET_DATA);

        protocol.registerClientbound(ClientboundPackets1_19_4.UPDATE_ADVANCEMENTS, wrapper -> {
            wrapper.passthrough(Types.BOOLEAN); // Reset/clear
            int size = wrapper.passthrough(Types.VAR_INT); // Mapping size
            for (int i = 0; i < size; i++) {
                wrapper.passthrough(Types.STRING); // Identifier
                wrapper.passthrough(Types.OPTIONAL_STRING); // Parent

                // Display data
                if (wrapper.passthrough(Types.BOOLEAN)) {
                    wrapper.passthrough(Types.COMPONENT); // Title
                    wrapper.passthrough(Types.COMPONENT); // Description
                    handleItemToClient(wrapper.user(), wrapper.passthrough(Types.ITEM1_13_2)); // Icon
                    wrapper.passthrough(Types.VAR_INT); // Frame type
                    int flags = wrapper.passthrough(Types.INT); // Flags
                    if ((flags & 1) != 0) {
                        wrapper.passthrough(Types.STRING); // Background texture
                    }
                    wrapper.passthrough(Types.FLOAT); // X
                    wrapper.passthrough(Types.FLOAT); // Y
                }

                wrapper.passthrough(Types.STRING_ARRAY); // Critereon triggers

                int requirements = wrapper.passthrough(Types.VAR_INT);
                for (int array = 0; array < requirements; array++) {
                    wrapper.passthrough(Types.STRING_ARRAY);
                }

                wrapper.write(Types.BOOLEAN, false); // Sends telemetry
            }
        });

        protocol.registerClientbound(ClientboundPackets1_19_4.OPEN_SIGN_EDITOR, wrapper -> {
            wrapper.passthrough(Types.BLOCK_POSITION1_14);
            wrapper.write(Types.BOOLEAN, true); // Front text
        });
        protocol.registerServerbound(ServerboundPackets1_19_4.SIGN_UPDATE, wrapper -> {
            wrapper.passthrough(Types.BLOCK_POSITION1_14);
            final boolean frontText = wrapper.read(Types.BOOLEAN);
            if (!frontText) {
                wrapper.cancel();
            }
        });

        protocol.registerClientbound(ClientboundPackets1_19_4.LEVEL_CHUNK_WITH_LIGHT, new PacketHandlers() {
            @Override
            protected void register() {
                handler(wrapper -> {
                    final Chunk chunk = blockRewriter.handleChunk1_19(wrapper, ChunkType1_18::new);
                    blockRewriter.handleBlockEntities(chunk, wrapper.user());
                });
                read(Types.BOOLEAN); // Trust edges
            }
        });

        protocol.registerClientbound(ClientboundPackets1_19_4.LIGHT_UPDATE, wrapper -> {
            wrapper.passthrough(Types.VAR_INT); // X
            wrapper.passthrough(Types.VAR_INT); // Y
            wrapper.read(Types.BOOLEAN); // Trust edges
        });

        protocol.registerClientbound(ClientboundPackets1_19_4.SECTION_BLOCKS_UPDATE, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.LONG); // Chunk position
                read(Types.BOOLEAN); // Suppress light updates
                handler(wrapper -> {
                    for (final BlockChangeRecord record : wrapper.passthrough(Types.VAR_LONG_BLOCK_CHANGE_ARRAY)) {
                        record.setBlockId(protocol.getMappingData().getNewBlockStateId(record.getBlockId()));
                    }
                });
            }
        });

        final RecipeRewriter<ClientboundPackets1_19_4> recipeRewriter = new RecipeRewriter1_19_4<>(protocol);
        protocol.registerClientbound(ClientboundPackets1_19_4.UPDATE_RECIPES, wrapper -> {
            final int size = wrapper.passthrough(Types.VAR_INT);
            int newSize = size;
            for (int i = 0; i < size; i++) {
                final String type = wrapper.read(Types.STRING);
                final String cutType = Key.stripMinecraftNamespace(type);
                if (cutType.equals("smithing")) {
                    newSize--;
                    wrapper.read(Types.STRING); // Recipe identifier
                    wrapper.read(Types.ITEM1_13_2_ARRAY); // Base
                    wrapper.read(Types.ITEM1_13_2_ARRAY); // Additions
                    wrapper.read(Types.ITEM1_13_2); // Result
                    continue;
                }

                wrapper.write(Types.STRING, type);
                wrapper.passthrough(Types.STRING); // Recipe Identifier
                recipeRewriter.handleRecipeType(wrapper, cutType);
            }

            wrapper.set(Types.VAR_INT, 0, newSize);
        });
    }
}

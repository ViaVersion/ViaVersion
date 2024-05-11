/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2024 ViaVersion and contributors
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
import com.viaversion.viaversion.api.minecraft.BlockChangeRecord;
import com.viaversion.viaversion.api.minecraft.blockentity.BlockEntity;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_18;
import com.viaversion.viaversion.protocols.v1_19_3to1_19_4.packet.ClientboundPackets1_19_4;
import com.viaversion.viaversion.protocols.v1_19_3to1_19_4.packet.ServerboundPackets1_19_4;
import com.viaversion.viaversion.protocols.v1_19_3to1_19_4.rewriter.RecipeRewriter1_19_4;
import com.viaversion.viaversion.protocols.v1_19_4to1_20.Protocol1_19_4To1_20;
import com.viaversion.viaversion.rewriter.BlockRewriter;
import com.viaversion.viaversion.rewriter.ItemRewriter;
import com.viaversion.viaversion.rewriter.RecipeRewriter;
import com.viaversion.viaversion.util.ComponentUtil;
import com.viaversion.viaversion.util.Key;

public final class ItemPacketRewriter1_20 extends ItemRewriter<ClientboundPackets1_19_4, ServerboundPackets1_19_4, Protocol1_19_4To1_20> {

    public ItemPacketRewriter1_20(final Protocol1_19_4To1_20 protocol) {
        super(protocol, Types.ITEM1_13_2, Types.ITEM1_13_2_ARRAY);
    }

    @Override
    public void registerPackets() {
        final BlockRewriter<ClientboundPackets1_19_4> blockRewriter = BlockRewriter.for1_14(protocol);
        blockRewriter.registerBlockAction(ClientboundPackets1_19_4.BLOCK_EVENT);
        blockRewriter.registerBlockChange(ClientboundPackets1_19_4.BLOCK_UPDATE);
        blockRewriter.registerEffect(ClientboundPackets1_19_4.LEVEL_EVENT, 1010, 2001);
        blockRewriter.registerBlockEntityData(ClientboundPackets1_19_4.BLOCK_ENTITY_DATA, this::handleBlockEntity);

        registerOpenWindow(ClientboundPackets1_19_4.OPEN_SCREEN);
        registerSetCooldown(ClientboundPackets1_19_4.COOLDOWN);
        registerWindowItems1_17_1(ClientboundPackets1_19_4.CONTAINER_SET_CONTENT);
        registerSetSlot1_17_1(ClientboundPackets1_19_4.CONTAINER_SET_SLOT);
        registerEntityEquipmentArray(ClientboundPackets1_19_4.SET_EQUIPMENT);
        registerClickWindow1_17_1(ServerboundPackets1_19_4.CONTAINER_CLICK);
        registerTradeList1_19(ClientboundPackets1_19_4.MERCHANT_OFFERS);
        registerCreativeInvAction(ServerboundPackets1_19_4.SET_CREATIVE_MODE_SLOT);
        registerWindowPropertyEnchantmentHandler(ClientboundPackets1_19_4.CONTAINER_SET_DATA);
        registerSpawnParticle1_19(ClientboundPackets1_19_4.LEVEL_PARTICLES);

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
                handler(blockRewriter.chunkDataHandler1_19(ChunkType1_18::new, (user, blockEntity) -> handleBlockEntity(blockEntity)));
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

    private void handleBlockEntity(final BlockEntity blockEntity) {
        // Check for signs
        if (blockEntity.typeId() != 7 && blockEntity.typeId() != 8) {
            return;
        }

        final CompoundTag tag = blockEntity.tag();
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
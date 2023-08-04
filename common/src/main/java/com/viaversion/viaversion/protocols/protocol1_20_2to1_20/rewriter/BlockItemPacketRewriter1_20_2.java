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

import com.viaversion.viaversion.api.data.ParticleMappings;
import com.viaversion.viaversion.api.data.entity.EntityTracker;
import com.viaversion.viaversion.api.minecraft.chunks.Chunk;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.metadata.ChunkPosition;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_18to1_17_1.types.Chunk1_18Type;
import com.viaversion.viaversion.protocols.protocol1_19_4to1_19_3.ClientboundPackets1_19_4;
import com.viaversion.viaversion.protocols.protocol1_19_4to1_19_3.rewriter.RecipeRewriter1_19_4;
import com.viaversion.viaversion.protocols.protocol1_20_2to1_20.Protocol1_20_2To1_20;
import com.viaversion.viaversion.protocols.protocol1_20_2to1_20.packet.ServerboundPackets1_20_2;
import com.viaversion.viaversion.protocols.protocol1_20_2to1_20.types.Chunk1_20_2Type;
import com.viaversion.viaversion.protocols.protocol1_20to1_19_4.Protocol1_20To1_19_4;
import com.viaversion.viaversion.rewriter.ItemRewriter;
import com.viaversion.viaversion.util.MathUtil;

public final class BlockItemPacketRewriter1_20_2 extends ItemRewriter<ClientboundPackets1_19_4, ServerboundPackets1_20_2, Protocol1_20_2To1_20> {

    public BlockItemPacketRewriter1_20_2(final Protocol1_20_2To1_20 protocol) {
        super(protocol, Type.ITEM1_20_2, Type.ITEM1_20_2_VAR_INT_ARRAY);
    }

    @Override
    public void registerPackets() {
        protocol.registerClientbound(ClientboundPackets1_19_4.UNLOAD_CHUNK, wrapper -> {
            final int x = wrapper.read(Type.INT);
            final int z = wrapper.read(Type.INT);
            wrapper.write(Type.CHUNK_POSITION, new ChunkPosition(x, z));
        });

        protocol.registerClientbound(ClientboundPackets1_19_4.NBT_QUERY, wrapper -> {
            wrapper.passthrough(Type.VAR_INT); // Transaction id
            wrapper.write(Type.NAMELESS_NBT, wrapper.read(Type.NBT));
        });

        protocol.registerClientbound(ClientboundPackets1_19_4.BLOCK_ENTITY_DATA, wrapper -> {
            wrapper.passthrough(Type.POSITION1_14); // Position
            wrapper.passthrough(Type.VAR_INT); // Type
            wrapper.write(Type.NAMELESS_NBT, wrapper.read(Type.NBT));
        });

        protocol.registerClientbound(ClientboundPackets1_19_4.CHUNK_DATA, wrapper -> {
            final EntityTracker tracker = protocol.getEntityRewriter().tracker(wrapper.user());
            final Type<Chunk> chunkType = new Chunk1_18Type(tracker.currentWorldSectionHeight(),
                    MathUtil.ceilLog2(Protocol1_20To1_19_4.MAPPINGS.getBlockStateMappings().mappedSize()),
                    MathUtil.ceilLog2(tracker.biomesSent()));
            final Chunk chunk = wrapper.read(chunkType);

            final Type<Chunk> newChunkType = new Chunk1_20_2Type(tracker.currentWorldSectionHeight(),
                    MathUtil.ceilLog2(Protocol1_20To1_19_4.MAPPINGS.getBlockStateMappings().mappedSize()),
                    MathUtil.ceilLog2(tracker.biomesSent()));
            wrapper.write(newChunkType, chunk);
        });

        // Replace the NBT type everywhere
        protocol.registerClientbound(ClientboundPackets1_19_4.WINDOW_ITEMS, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.UNSIGNED_BYTE); // Window id
                map(Type.VAR_INT); // State id
                handler(wrapper -> {
                    wrapper.write(Type.ITEM1_20_2_VAR_INT_ARRAY, wrapper.read(Type.FLAT_VAR_INT_ITEM_ARRAY_VAR_INT)); // Items
                    wrapper.write(Type.ITEM1_20_2, wrapper.read(Type.FLAT_VAR_INT_ITEM)); // Carried item
                });
            }
        });
        protocol.registerClientbound(ClientboundPackets1_19_4.SET_SLOT, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.UNSIGNED_BYTE); // Window id
                map(Type.VAR_INT); // State id
                map(Type.SHORT); // Slot id
                map(Type.FLAT_VAR_INT_ITEM, Type.ITEM1_20_2); // Item
            }
        });
        protocol.registerClientbound(ClientboundPackets1_19_4.ADVANCEMENTS, wrapper -> {
            wrapper.passthrough(Type.BOOLEAN); // Reset/clear
            int size = wrapper.passthrough(Type.VAR_INT); // Mapping size
            for (int i = 0; i < size; i++) {
                wrapper.passthrough(Type.STRING); // Identifier

                // Parent
                if (wrapper.passthrough(Type.BOOLEAN))
                    wrapper.passthrough(Type.STRING);

                // Display data
                if (wrapper.passthrough(Type.BOOLEAN)) {
                    wrapper.passthrough(Type.COMPONENT); // Title
                    wrapper.passthrough(Type.COMPONENT); // Description
                    wrapper.write(Type.ITEM1_20_2, wrapper.read(Type.FLAT_VAR_INT_ITEM)); // Icon
                    wrapper.passthrough(Type.VAR_INT); // Frame type
                    int flags = wrapper.passthrough(Type.INT); // Flags
                    if ((flags & 1) != 0) {
                        wrapper.passthrough(Type.STRING); // Background texture
                    }
                    wrapper.passthrough(Type.FLOAT); // X
                    wrapper.passthrough(Type.FLOAT); // Y
                }

                wrapper.passthrough(Type.STRING_ARRAY); // Criteria

                int arrayLength = wrapper.passthrough(Type.VAR_INT);
                for (int array = 0; array < arrayLength; array++) {
                    wrapper.passthrough(Type.STRING_ARRAY); // String array
                }

                wrapper.passthrough(Type.BOOLEAN); // Send telemetry
            }
        });
        protocol.registerClientbound(ClientboundPackets1_19_4.ENTITY_EQUIPMENT, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT); // 0 - Entity ID
                handler(wrapper -> {
                    byte slot;
                    do {
                        slot = wrapper.passthrough(Type.BYTE);
                        wrapper.write(Type.ITEM1_20_2, wrapper.read(Type.FLAT_VAR_INT_ITEM));
                    } while ((slot & 0xFFFFFF80) != 0);
                });
            }
        });
        protocol.registerServerbound(ServerboundPackets1_20_2.CLICK_WINDOW, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.UNSIGNED_BYTE); // Window Id
                map(Type.VAR_INT); // State id
                map(Type.SHORT); // Slot
                map(Type.BYTE); // Button
                map(Type.VAR_INT); // Mode

                handler(wrapper -> {
                    // Affected items
                    int length = wrapper.passthrough(Type.VAR_INT);
                    for (int i = 0; i < length; i++) {
                        wrapper.passthrough(Type.SHORT); // Slot
                        wrapper.write(Type.FLAT_VAR_INT_ITEM, wrapper.read(Type.ITEM1_20_2));
                    }

                    // Carried item
                    wrapper.write(Type.FLAT_VAR_INT_ITEM, wrapper.read(Type.ITEM1_20_2));
                });
            }
        });
        protocol.registerClientbound(ClientboundPackets1_19_4.TRADE_LIST, wrapper -> {
            wrapper.passthrough(Type.VAR_INT); // Container id
            int size = wrapper.passthrough(Type.VAR_INT);
            for (int i = 0; i < size; i++) {
                wrapper.write(Type.ITEM1_20_2, wrapper.read(Type.FLAT_VAR_INT_ITEM)); // Input
                wrapper.write(Type.ITEM1_20_2, wrapper.read(Type.FLAT_VAR_INT_ITEM)); // Output
                wrapper.write(Type.ITEM1_20_2, wrapper.read(Type.FLAT_VAR_INT_ITEM)); // Second Item

                wrapper.passthrough(Type.BOOLEAN); // Trade disabled
                wrapper.passthrough(Type.INT); // Number of tools uses
                wrapper.passthrough(Type.INT); // Maximum number of trade uses

                wrapper.passthrough(Type.INT); // XP
                wrapper.passthrough(Type.INT); // Special price
                wrapper.passthrough(Type.FLOAT); // Price multiplier
                wrapper.passthrough(Type.INT); // Demand
            }
        });
        protocol.registerServerbound(ServerboundPackets1_20_2.CREATIVE_INVENTORY_ACTION, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.SHORT); // 0 - Slot
                map(Type.ITEM1_20_2, Type.FLAT_VAR_INT_ITEM); // 1 - Clicked Item
            }
        });
        protocol.registerClientbound(ClientboundPackets1_19_4.SPAWN_PARTICLE, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT); // 0 - Particle ID
                map(Type.BOOLEAN); // 1 - Long Distance
                map(Type.DOUBLE); // 2 - X
                map(Type.DOUBLE); // 3 - Y
                map(Type.DOUBLE); // 4 - Z
                map(Type.FLOAT); // 5 - Offset X
                map(Type.FLOAT); // 6 - Offset Y
                map(Type.FLOAT); // 7 - Offset Z
                map(Type.FLOAT); // 8 - Particle Data
                map(Type.INT); // 9 - Particle Count
                handler(wrapper -> {
                    int id = wrapper.get(Type.VAR_INT, 0);
                    ParticleMappings mappings = Protocol1_20To1_19_4.MAPPINGS.getParticleMappings();
                    if (mappings.isItemParticle(id)) {
                        wrapper.write(Type.ITEM1_20_2, wrapper.read(Type.FLAT_VAR_INT_ITEM));
                    }
                });
            }
        });

        new RecipeRewriter1_19_4<ClientboundPackets1_19_4>(protocol) {
            @Override
            public void handleCraftingShapeless(final PacketWrapper wrapper) throws Exception {
                wrapper.passthrough(Type.STRING); // Group
                wrapper.passthrough(Type.VAR_INT); // Crafting book category
                handleIngredients(wrapper);

                final Item result = wrapper.read(itemType());
                rewrite(result);
                wrapper.write(Type.ITEM1_20_2, result);
            }

            @Override
            public void handleSmelting(final PacketWrapper wrapper) throws Exception {
                wrapper.passthrough(Type.STRING); // Group
                wrapper.passthrough(Type.VAR_INT); // Crafting book category
                handleIngredient(wrapper);

                final Item result = wrapper.read(itemType());
                rewrite(result);
                wrapper.write(Type.ITEM1_20_2, result);

                wrapper.passthrough(Type.FLOAT); // EXP
                wrapper.passthrough(Type.VAR_INT); // Cooking time
            }

            @Override
            public void handleCraftingShaped(final PacketWrapper wrapper) throws Exception {
                final int ingredients = wrapper.passthrough(Type.VAR_INT) * wrapper.passthrough(Type.VAR_INT);
                wrapper.passthrough(Type.STRING); // Group
                wrapper.passthrough(Type.VAR_INT); // Crafting book category
                for (int i = 0; i < ingredients; i++) {
                    handleIngredient(wrapper);
                }

                final Item result = wrapper.read(itemType());
                rewrite(result);
                wrapper.write(Type.ITEM1_20_2, result);

                wrapper.passthrough(Type.BOOLEAN); // Show notification
            }

            @Override
            public void handleStonecutting(final PacketWrapper wrapper) throws Exception {
                wrapper.passthrough(Type.STRING); // Group
                handleIngredient(wrapper);

                final Item result = wrapper.read(itemType());
                rewrite(result);
                wrapper.write(Type.ITEM1_20_2, result);
            }

            @Override
            public void handleSmithing(final PacketWrapper wrapper) throws Exception {
                handleIngredient(wrapper); // Base
                handleIngredient(wrapper); // Addition

                final Item result = wrapper.read(itemType());
                rewrite(result);
                wrapper.write(Type.ITEM1_20_2, result);
            }

            @Override
            public void handleSmithingTransform(final PacketWrapper wrapper) throws Exception {
                handleIngredient(wrapper); // Template
                handleIngredient(wrapper); // Base
                handleIngredient(wrapper); // Additions

                final Item result = wrapper.read(itemType());
                rewrite(result);
                wrapper.write(Type.ITEM1_20_2, result);
            }

            @Override
            protected void handleIngredient(final PacketWrapper wrapper) throws Exception {
                final Item[] items = wrapper.read(itemArrayType());
                wrapper.write(Type.ITEM1_20_2_VAR_INT_ARRAY, items);
                for (final Item item : items) {
                    rewrite(item);
                }
            }
        }.register(ClientboundPackets1_19_4.DECLARE_RECIPES);
    }
}
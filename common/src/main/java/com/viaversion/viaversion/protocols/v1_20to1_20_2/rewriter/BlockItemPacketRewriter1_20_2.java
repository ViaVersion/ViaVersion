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
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.NumberTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.entity.EntityTracker;
import com.viaversion.viaversion.api.minecraft.ChunkPosition;
import com.viaversion.viaversion.api.minecraft.blockentity.BlockEntity;
import com.viaversion.viaversion.api.minecraft.chunks.Chunk;
import com.viaversion.viaversion.api.minecraft.chunks.ChunkSection;
import com.viaversion.viaversion.api.minecraft.chunks.DataPalette;
import com.viaversion.viaversion.api.minecraft.chunks.PaletteType;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_18;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_20_2;
import com.viaversion.viaversion.protocols.v1_19_3to1_19_4.packet.ClientboundPackets1_19_4;
import com.viaversion.viaversion.protocols.v1_19_3to1_19_4.rewriter.RecipeRewriter1_19_4;
import com.viaversion.viaversion.protocols.v1_20to1_20_2.Protocol1_20To1_20_2;
import com.viaversion.viaversion.protocols.v1_20to1_20_2.data.PotionEffects1_20_2;
import com.viaversion.viaversion.protocols.v1_20to1_20_2.packet.ServerboundPackets1_20_2;
import com.viaversion.viaversion.rewriter.BlockRewriter;
import com.viaversion.viaversion.rewriter.ItemRewriter;
import com.viaversion.viaversion.util.MathUtil;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class BlockItemPacketRewriter1_20_2 extends ItemRewriter<ClientboundPackets1_19_4, ServerboundPackets1_20_2, Protocol1_20To1_20_2> {

    public BlockItemPacketRewriter1_20_2(final Protocol1_20To1_20_2 protocol) {
        super(protocol, Types.ITEM1_13_2, Types.ITEM1_13_2_ARRAY, Types.ITEM1_20_2, Types.ITEM1_20_2_ARRAY);
    }

    @Override
    public void registerPackets() {
        final BlockRewriter<ClientboundPackets1_19_4> blockRewriter = BlockRewriter.for1_14(protocol);
        blockRewriter.registerBlockEvent(ClientboundPackets1_19_4.BLOCK_EVENT);
        blockRewriter.registerBlockUpdate(ClientboundPackets1_19_4.BLOCK_UPDATE);
        blockRewriter.registerSectionBlocksUpdate1_20(ClientboundPackets1_19_4.SECTION_BLOCKS_UPDATE);
        blockRewriter.registerLevelEvent(ClientboundPackets1_19_4.LEVEL_EVENT, 1010, 2001);

        registerSetContent1_17_1(ClientboundPackets1_19_4.CONTAINER_SET_CONTENT);
        registerSetSlot1_17_1(ClientboundPackets1_19_4.CONTAINER_SET_SLOT);
        registerContainerClick1_17_1(ServerboundPackets1_20_2.CONTAINER_CLICK);
        registerMerchantOffers1_19(ClientboundPackets1_19_4.MERCHANT_OFFERS);
        registerSetCreativeModeSlot(ServerboundPackets1_20_2.SET_CREATIVE_MODE_SLOT);

        protocol.registerServerbound(ServerboundPackets1_20_2.SET_BEACON, wrapper -> {
            // Effects start at 1 before 1.20.2
            if (wrapper.passthrough(Types.BOOLEAN)) { // Primary effect
                wrapper.write(Types.VAR_INT, wrapper.read(Types.VAR_INT) + 1);
            }
            if (wrapper.passthrough(Types.BOOLEAN)) { // Secondary effect
                wrapper.write(Types.VAR_INT, wrapper.read(Types.VAR_INT) + 1);
            }
        });

        protocol.registerClientbound(ClientboundPackets1_19_4.FORGET_LEVEL_CHUNK, wrapper -> {
            final int x = wrapper.read(Types.INT);
            final int z = wrapper.read(Types.INT);
            wrapper.write(Types.CHUNK_POSITION, new ChunkPosition(x, z));
        });

        protocol.registerClientbound(ClientboundPackets1_19_4.TAG_QUERY, wrapper -> {
            wrapper.passthrough(Types.VAR_INT); // Transaction id
            wrapper.write(Types.COMPOUND_TAG, wrapper.read(Types.NAMED_COMPOUND_TAG));
        });

        protocol.registerClientbound(ClientboundPackets1_19_4.BLOCK_ENTITY_DATA, wrapper -> {
            wrapper.passthrough(Types.BLOCK_POSITION1_14); // Position
            wrapper.passthrough(Types.VAR_INT); // Type
            wrapper.write(Types.TRUSTED_COMPOUND_TAG, handleBlockEntity(wrapper.read(Types.NAMED_COMPOUND_TAG)));
        });

        protocol.registerClientbound(ClientboundPackets1_19_4.LEVEL_CHUNK_WITH_LIGHT, wrapper -> {
            final EntityTracker tracker = protocol.getEntityRewriter().tracker(wrapper.user());
            final Type<Chunk> chunkType = new ChunkType1_18(tracker.currentWorldSectionHeight(),
                MathUtil.ceilLog2(protocol.getMappingData().getBlockStateMappings().size()),
                MathUtil.ceilLog2(tracker.biomesSent()));
            final Chunk chunk = wrapper.read(chunkType);

            final Type<Chunk> newChunkType = new ChunkType1_20_2(tracker.currentWorldSectionHeight(),
                MathUtil.ceilLog2(protocol.getMappingData().getBlockStateMappings().mappedSize()),
                MathUtil.ceilLog2(tracker.biomesSent()));
            wrapper.write(newChunkType, chunk);

            for (final ChunkSection section : chunk.getSections()) {
                final DataPalette blockPalette = section.palette(PaletteType.BLOCKS);
                for (int i = 0; i < blockPalette.size(); i++) {
                    final int id = blockPalette.idByIndex(i);
                    blockPalette.setIdByIndex(i, protocol.getMappingData().getNewBlockStateId(id));
                }
            }

            for (final BlockEntity blockEntity : chunk.blockEntities()) {
                handleBlockEntity(blockEntity.tag());
            }
        });

        protocol.registerClientbound(ClientboundPackets1_19_4.UPDATE_ADVANCEMENTS, wrapper -> {
            wrapper.passthrough(Types.BOOLEAN); // Reset/clear
            final int size = wrapper.passthrough(Types.VAR_INT); // Mapping size
            for (int i = 0; i < size; i++) {
                wrapper.passthrough(Types.STRING); // Identifier
                wrapper.passthrough(Types.OPTIONAL_STRING); // Parent

                // Display data
                if (wrapper.passthrough(Types.BOOLEAN)) {
                    wrapper.passthrough(Types.COMPONENT); // Title
                    wrapper.passthrough(Types.COMPONENT); // Description
                    wrapper.write(Types.ITEM1_20_2, handleItemToClient(wrapper.user(), wrapper.read(Types.ITEM1_13_2))); // Icon
                    wrapper.passthrough(Types.VAR_INT); // Frame type
                    final int flags = wrapper.passthrough(Types.INT); // Flags
                    if ((flags & 1) != 0) {
                        wrapper.passthrough(Types.STRING); // Background texture
                    }
                    wrapper.passthrough(Types.FLOAT); // X
                    wrapper.passthrough(Types.FLOAT); // Y
                }

                // Remove criterion triggers
                wrapper.read(Types.STRING_ARRAY); // Criteria

                final int requirements = wrapper.passthrough(Types.VAR_INT);
                for (int array = 0; array < requirements; array++) {
                    wrapper.passthrough(Types.STRING_ARRAY);
                }

                wrapper.passthrough(Types.BOOLEAN); // Send telemetry
            }
        });
        protocol.registerClientbound(ClientboundPackets1_19_4.SET_EQUIPMENT, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); // 0 - Entity ID
                handler(wrapper -> {
                    byte slot;
                    do {
                        slot = wrapper.passthrough(Types.BYTE);
                        wrapper.write(Types.ITEM1_20_2, handleItemToClient(wrapper.user(), wrapper.read(Types.ITEM1_13_2)));
                    } while ((slot & 0xFFFFFF80) != 0);
                });
            }
        });

        new RecipeRewriter1_19_4<>(protocol) {
            @Override
            protected Type<Item> mappedItemType() {
                return BlockItemPacketRewriter1_20_2.this.mappedItemType();
            }

            @Override
            protected Type<Item[]> mappedItemArrayType() {
                return BlockItemPacketRewriter1_20_2.this.mappedItemArrayType();
            }

        }.register(ClientboundPackets1_19_4.UPDATE_RECIPES);
    }

    @Override
    public @Nullable Item handleItemToClient(final UserConnection connection, @Nullable final Item item) {
        if (item == null) {
            return null;
        }

        if (item.tag() != null) {
            to1_20_2Effects(item);
        }

        return super.handleItemToClient(connection, item);
    }

    @Override
    public @Nullable Item handleItemToServer(final UserConnection connection, @Nullable final Item item) {
        if (item == null) {
            return null;
        }

        if (item.tag() != null) {
            to1_20_1Effects(item);
        }

        return super.handleItemToServer(connection, item);
    }

    public static void to1_20_2Effects(final Item item) {
        final Tag customPotionEffectsTag = item.tag().remove("CustomPotionEffects");
        if (customPotionEffectsTag instanceof ListTag<?> effectsTag) {
            item.tag().put("custom_potion_effects", customPotionEffectsTag);

            for (final Tag tag : effectsTag) {
                if (!(tag instanceof CompoundTag effectTag)) {
                    continue;
                }

                final Tag idTag = effectTag.remove("Id");
                if (idTag instanceof NumberTag) {
                    // Empty effect removed
                    final String key = PotionEffects1_20_2.idToKey(((NumberTag) idTag).asInt() - 1);
                    if (key != null) {
                        effectTag.put("id", new StringTag(key));
                    }
                }

                renameTag(effectTag, "Amplifier", "amplifier");
                renameTag(effectTag, "Duration", "duration");
                renameTag(effectTag, "Ambient", "ambient");
                renameTag(effectTag, "ShowParticles", "show_particles");
                renameTag(effectTag, "ShowIcon", "show_icon");
                renameTag(effectTag, "HiddenEffect", "hidden_effect");
                renameTag(effectTag, "FactorCalculationData", "factor_calculation_data");
            }
        }
    }

    public static void to1_20_1Effects(final Item item) {
        final Tag customPotionEffectsTag = item.tag().remove("custom_potion_effects");
        if (customPotionEffectsTag instanceof ListTag<?> effectsTag) {
            item.tag().put("CustomPotionEffects", effectsTag);

            for (final Tag tag : effectsTag) {
                if (!(tag instanceof CompoundTag effectTag)) {
                    continue;
                }

                if (effectTag.remove("id") instanceof StringTag idTag) {
                    final int id = PotionEffects1_20_2.keyToId(idTag.getValue());
                    effectTag.putInt("Id", id + 1); // Account for empty effect at id 0
                }

                renameTag(effectTag, "amplifier", "Amplifier");
                renameTag(effectTag, "duration", "Duration");
                renameTag(effectTag, "ambient", "Ambient");
                renameTag(effectTag, "show_particles", "ShowParticles");
                renameTag(effectTag, "show_icon", "ShowIcon");
                renameTag(effectTag, "hidden_effect", "HiddenEffect");
                renameTag(effectTag, "factor_calculation_data", "FactorCalculationData");
            }
        }
    }

    private static void renameTag(final CompoundTag tag, final String entryName, final String toEntryName) {
        final Tag entry = tag.remove(entryName);
        if (entry != null) {
            tag.put(toEntryName, entry);
        }
    }

    private @Nullable CompoundTag handleBlockEntity(@Nullable final CompoundTag tag) {
        if (tag == null) {
            return null;
        }

        final Tag primaryEffect = tag.remove("Primary");
        if (primaryEffect instanceof NumberTag && ((NumberTag) primaryEffect).asInt() != 0) {
            tag.put("primary_effect", new StringTag(PotionEffects1_20_2.idToKeyOrLuck(((NumberTag) primaryEffect).asInt() - 1)));
        }

        final Tag secondaryEffect = tag.remove("Secondary");
        if (secondaryEffect instanceof NumberTag && ((NumberTag) secondaryEffect).asInt() != 0) {
            tag.put("secondary_effect", new StringTag(PotionEffects1_20_2.idToKeyOrLuck(((NumberTag) secondaryEffect).asInt() - 1)));
        }
        return tag;
    }
}

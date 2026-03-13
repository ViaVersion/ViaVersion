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
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.api.minecraft.ChunkPosition;
import com.viaversion.viaversion.api.minecraft.blockentity.BlockEntity;
import com.viaversion.viaversion.api.minecraft.blockentity.BlockEntityImpl;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_19_3to1_19_4.packet.ClientboundPackets1_19_4;
import com.viaversion.viaversion.protocols.v1_19_3to1_19_4.rewriter.RecipeRewriter1_19_4;
import com.viaversion.viaversion.protocols.v1_20to1_20_2.Protocol1_20To1_20_2;
import com.viaversion.viaversion.protocols.v1_20to1_20_2.data.PotionEffects1_20_2;
import com.viaversion.viaversion.protocols.v1_20to1_20_2.packet.ServerboundPackets1_20_2;
import com.viaversion.viaversion.rewriter.ItemRewriter;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class BlockItemPacketRewriter1_20_2 extends ItemRewriter<ClientboundPackets1_19_4, ServerboundPackets1_20_2, Protocol1_20To1_20_2> {

    public BlockItemPacketRewriter1_20_2(final Protocol1_20To1_20_2 protocol) {
        super(protocol, Types.ITEM1_13_2, Types.ITEM1_13_2_ARRAY, Types.ITEM1_20_2, Types.ITEM1_20_2_ARRAY);
    }

    @Override
    public void registerPackets() {
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

        protocol.replaceClientbound(ClientboundPackets1_19_4.BLOCK_ENTITY_DATA, wrapper -> {
            final BlockPosition position = wrapper.passthrough(Types.BLOCK_POSITION1_14);
            final int typeId = wrapper.passthrough(Types.VAR_INT);

            final CompoundTag tag = wrapper.read(Types.NAMED_COMPOUND_TAG);
            final BlockEntity blockEntity = new BlockEntityImpl(BlockEntity.pack(position.x(), position.z()), (short) position.y(), typeId, tag);
            protocol.getBlockRewriter().handleBlockEntity(wrapper.user(), blockEntity);
            wrapper.write(Types.TRUSTED_COMPOUND_TAG, blockEntity.tag());
        });

        protocol.replaceClientbound(ClientboundPackets1_19_4.UPDATE_ADVANCEMENTS, wrapper -> {
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
}

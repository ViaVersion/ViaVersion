/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2026 ViaVersion and contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.viaversion.viaversion.api.minecraft.item.data;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.MappingData.MappingType;
import com.viaversion.viaversion.api.minecraft.Holder;
import com.viaversion.viaversion.api.minecraft.SoundEvent;
import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.ArrayType;
import com.viaversion.viaversion.util.Copyable;
import com.viaversion.viaversion.util.Key;
import com.viaversion.viaversion.util.Rewritable;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.Nullable;

public record BlockTransformData(CompoundTag blockStateProvider, Holder<SoundEvent> sound, int transformParticle,
                                 int[] disallowedFaces, @Nullable String loot, int dropStrategy,
                                 boolean updateFromNeighbors, int transformType,
                                 boolean consumeOnUse, int itemDamagePerUse) implements Rewritable, Copyable {

    public static final Type<BlockTransformData> TYPE = new Type<>(BlockTransformData.class) {
        @Override
        public BlockTransformData read(final ByteBuf buffer) {
            final CompoundTag blockStateProvider = Types.COMPOUND_TAG.read(buffer);
            final Holder<SoundEvent> sound = Types.SOUND_EVENT.read(buffer);
            final int transformParticle = Types.VAR_INT.readPrimitive(buffer);
            final int[] disallowedFaces = Types.VAR_INT_ARRAY_PRIMITIVE.read(buffer);
            final String loot = Types.OPTIONAL_STRING.read(buffer);
            final int dropStrategy = Types.VAR_INT.readPrimitive(buffer);
            final boolean updateFromNeighbors = Types.BOOLEAN.read(buffer);
            final int transformType = Types.VAR_INT.readPrimitive(buffer);
            final boolean consumeOnUse = Types.BOOLEAN.read(buffer);
            final int itemDamagePerUse = Types.VAR_INT.readPrimitive(buffer);
            return new BlockTransformData(blockStateProvider, sound, transformParticle, disallowedFaces, loot, dropStrategy, updateFromNeighbors, transformType, consumeOnUse, itemDamagePerUse);
        }

        @Override
        public void write(final ByteBuf buffer, final BlockTransformData value) {
            Types.COMPOUND_TAG.write(buffer, value.blockStateProvider);
            Types.SOUND_EVENT.write(buffer, value.sound);
            Types.VAR_INT.writePrimitive(buffer, value.transformParticle);
            Types.VAR_INT_ARRAY_PRIMITIVE.write(buffer, value.disallowedFaces);
            Types.OPTIONAL_STRING.write(buffer, value.loot);
            Types.VAR_INT.writePrimitive(buffer, value.dropStrategy);
            Types.BOOLEAN.write(buffer, value.updateFromNeighbors);
            Types.VAR_INT.writePrimitive(buffer, value.transformType);
            Types.BOOLEAN.write(buffer, value.consumeOnUse);
            Types.VAR_INT.writePrimitive(buffer, value.itemDamagePerUse);
        }

        @Override
        public void write(final Ops ops, final BlockTransformData data) {
            final Holder<SoundEvent> defaultSound = Holder.of(ops.context().registryAccess().id(MappingType.SOUND, "intentionally_empty"));
            ops.writeMap(map -> map
                .write("block_state_provider", Types.COMPOUND_TAG, data.blockStateProvider)
                .writeOptional("sound", Types.SOUND_EVENT, data.sound, defaultSound)
                .writeOptional("particle", EnumTypes.TRANSFORM_PARTICLE, data.transformParticle, 0)
                .writeOptional("disallowed_faces", EnumTypes.DIRECTION.new EnumArrayType(), data.disallowedFaces, new int[0])
                .writeOptional("loot", Types.STRING, data.loot)
                .writeOptional("drop_strategy", Types.VAR_INT, data.dropStrategy, 1)
                .writeOptional("update_from_neighbors", Types.BOOLEAN, data.updateFromNeighbors, true)
                .writeOptional("transform_type", Types.VAR_INT, data.transformType, 0)
                .writeOptional("consume_on_use", Types.BOOLEAN, data.consumeOnUse, true)
                .writeOptional("item_damage_per_use", Types.VAR_INT, data.itemDamagePerUse, 0)
            );
        }
    };
    public static final ArrayType<BlockTransformData> ARRAY_TYPE = new ArrayType<>(TYPE);

    @Override
    public BlockTransformData rewrite(final UserConnection connection, final Protocol<?, ?, ?, ?> protocol, final boolean clientbound) {
        CompoundTag updatedTag = blockStateProvider().copy();
        if (updateBlockStateProvider(protocol, updatedTag)) {
            // Back it up clientbound so server data doesn't disappear; remove it for newly spawned items serverbound.
            // Given it's not even encoded properly over the network and would otherwise require keeping full block state mappings
            // and is not needed on the client, we only do the bare minimum to guarantee compatibility.
            if (clientbound) {
                updatedTag.put("backup", this.blockStateProvider);
            } else {
                final CompoundTag backupTag = updatedTag.getCompoundTag("backup");
                updatedTag = backupTag != null ? backupTag : dummyBlockStateProvider();
            }
        }
        return new BlockTransformData(updatedTag, sound, transformParticle, disallowedFaces, loot, dropStrategy, updateFromNeighbors, transformType, consumeOnUse, itemDamagePerUse);
    }

    private CompoundTag dummyBlockStateProvider() {
        final CompoundTag state = new CompoundTag();
        state.putString("Name", "dirt");

        final CompoundTag tag = new CompoundTag();
        tag.putString("type", "simple_state_provider");
        tag.put("state", state);
        return tag;
    }

    private boolean updateBlockStateProvider(final Protocol<?, ?, ?, ?> protocol, final CompoundTag tag) {
        boolean changed = false;
        final String type = Key.stripMinecraftNamespace(tag.getString("type"));
        switch (type) {
            case "simple_state_provider", "rotated_block_provider" -> {
                changed |= updateBlockState(protocol, tag.getCompoundTag("state"));
            }
            case "weighted_state_provider" -> {
                for (final CompoundTag entry : tag.getListTag("entries", CompoundTag.class)) {
                    changed |= updateBlockState(protocol, entry.getCompoundTag("data"));
                }
            }
            case "noise_threshold_provider" -> {
                changed |= updateBlockState(protocol, tag.getCompoundTag("default_state"));
                for (final CompoundTag entry : tag.getListTag("low_states", CompoundTag.class)) {
                    changed |= updateBlockState(protocol, entry);
                }
                for (final CompoundTag entry : tag.getListTag("high_states", CompoundTag.class)) {
                    changed |= updateBlockState(protocol, entry);
                }
            }
            case "noise_provider", "dual_noise_provider" -> {
                for (final CompoundTag entry : tag.getListTag("states", CompoundTag.class)) {
                    changed |= updateBlockState(protocol, entry);
                }
            }
            case "randomized_int_state_provider" -> {
                changed |= updateBlockStateProvider(protocol, tag.getCompoundTag("source"));
                // "property" field is generic and can be left unchanged. If invalid, it'll be defaulted
            }
            case "rule_based_state_provider" -> {
                final CompoundTag fallback = tag.getCompoundTag("fallback");
                if (fallback != null) {
                    changed |= updateBlockStateProvider(protocol, fallback);
                }

                // Clear rules since parsing block state predicates is quite a lot
                tag.put("rules", new ListTag<>(CompoundTag.class));
                changed = true;

                /*
                for (final CompoundTag entry : tag.getListTag("rules", CompoundTag.class)) {
                    changed |= updateBlockStateProvider(protocol, entry.getCompoundTag("then"));
                    // "if_true" block state predicate (different to the advancement block predicate)...
                }
                */
            }
            case "copy_properties_provider" -> {
                changed |= updateBlockStateProvider(protocol, tag.getCompoundTag("source_block_state_provider"));
            }
        }
        return changed;
    }

    private boolean updateBlockState(final Protocol<?, ?, ?, ?> protocol, final CompoundTag blockStateTag) {
        // {"Name": "minecraft:grass_block", "Properties": {"snowy": "true"}}
        final String block = blockStateTag.getString("Name");
        final int blockId = protocol.getMappingData().getFullBlockMappings().id(block);
        if (blockId == -1 || protocol.getMappingData().changedBlocks().contains(blockId)) {
            // Return dummy block state
            blockStateTag.putString("Name", "dirt");
            blockStateTag.remove("Properties");
            return true;
        }
        return false;
    }

    @Override
    public BlockTransformData copy() {
        return new BlockTransformData(blockStateProvider.copy(), sound, transformParticle, disallowedFaces, loot, dropStrategy, updateFromNeighbors, transformType, consumeOnUse, itemDamagePerUse);
    }
}

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
package com.viaversion.viaversion.codec.nbt;

import com.google.common.base.Preconditions;
import com.viaversion.nbt.tag.ByteArrayTag;
import com.viaversion.nbt.tag.ByteTag;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.DoubleTag;
import com.viaversion.nbt.tag.FloatTag;
import com.viaversion.nbt.tag.IntArrayTag;
import com.viaversion.nbt.tag.IntTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.LongArrayTag;
import com.viaversion.nbt.tag.LongTag;
import com.viaversion.nbt.tag.ShortTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.minecraft.codec.CodecContext;
import com.viaversion.viaversion.api.minecraft.codec.ThrowingOps;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.codec.OpsBase;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Re-usable (but not thread-safe) ops for writing NBT.
 */
public final class NbtOps extends OpsBase {

    private Tag result;

    public NbtOps(final CodecContext context) {
        super(context);
    }

    /**
     * Serializes the given value into its NBT representation.
     *
     * @param context codec context
     * @param type    type of the value
     * @param value   value to serialize
     * @param <V>     value type
     * @return the serialized tag
     */
    public static <V> Tag serialize(final CodecContext context, final Type<V> type, final V value) {
        final NbtOps ops = new NbtOps(context);
        type.write(ops, value);
        return ops.complete();
    }

    /**
     * Returns the written tag, clearing it from this instance.
     *
     * @return the written tag
     */
    public Tag complete() {
        final Tag tag = this.result;
        Preconditions.checkNotNull(tag, "No value written");
        this.result = null;
        return tag;
    }

    @Override
    public void writeByte(final byte b) {
        this.result = new ByteTag(b);
    }

    @Override
    public void writeBytes(final byte[] array) {
        this.result = new ByteArrayTag(array);
    }

    @Override
    public void writeBoolean(final boolean b) {
        this.result = new ByteTag(b);
    }

    @Override
    public void writeShort(final short s) {
        this.result = new ShortTag(s);
    }

    @Override
    public void writeString(final CharSequence sequence) {
        this.result = new StringTag(sequence.toString());
    }

    @Override
    public void writeInt(final int i) {
        this.result = new IntTag(i);
    }

    @Override
    public void writeLong(final long l) {
        this.result = new LongTag(l);
    }

    @Override
    public void writeFloat(final float f) {
        this.result = new FloatTag(f);
    }

    @Override
    public void writeDouble(final double d) {
        this.result = new DoubleTag(d);
    }

    @Override
    public void writeInts(final int[] array) {
        this.result = new IntArrayTag(array);
    }

    @Override
    public void writeLongs(final long[] array) {
        this.result = new LongArrayTag(array);
    }

    @Override
    public void writeList(final Consumer<ListSerializer> consumer) {
        final ListNbtSerializer serializer = new ListNbtSerializer();
        consumer.accept(serializer);
        // Only set after the consumer ran, nested writes use the same result field
        this.result = ListTag.of(serializer.entries);
    }

    @Override
    public void writeMap(final Consumer<MapSerializer> consumer) {
        final MapNbtSerializer serializer = new MapNbtSerializer();
        consumer.accept(serializer);
        this.result = serializer.tag;
    }

    private <V> Tag serialize(final Type<V> type, final V value) {
        type.write(this, value);
        return complete();
    }

    private Tag serializeList(final Consumer<ListSerializer> consumer) {
        writeList(consumer);
        return complete();
    }

    private Tag serializeMap(final Consumer<MapSerializer> consumer) {
        writeMap(consumer);
        return complete();
    }

    private final class MapNbtSerializer implements MapSerializer {

        private final CompoundTag tag = new CompoundTag();

        @Override
        public <K, V> MapSerializer write(final Type<K> keyType, final K key, final Type<V> valueType, final V value) {
            tag.put(serializeKey(keyType, key), serialize(valueType, value));
            return this;
        }

        @Override
        public MapSerializer writeList(final String key, final Consumer<ListSerializer> consumer) {
            tag.put(key, serializeList(consumer));
            return this;
        }

        @Override
        public MapSerializer writeMap(final String key, final Consumer<MapSerializer> consumer) {
            tag.put(key, serializeMap(consumer));
            return this;
        }

        @Override
        public <T> MapSerializer writeInlinedMap(final Type<T> valueType, final T value) {
            // Only allow writeMap calls here, writing its entries into this map
            valueType.write(new ThrowingOps() {
                @Override
                public CodecContext context() {
                    return NbtOps.this.context();
                }

                @Override
                public void writeMap(final Consumer<MapSerializer> consumer) {
                    consumer.accept(MapNbtSerializer.this);
                }
            }, value);
            return this;
        }

        private <K> String serializeKey(final Type<K> keyType, final K key) {
            final Tag keyTag = NbtOps.this.serialize(keyType, key);
            Preconditions.checkArgument(keyTag instanceof StringTag, "Map keys have to be strings, got %s from %s", keyTag, keyType.getTypeName());
            return ((StringTag) keyTag).getValue();
        }
    }

    private final class ListNbtSerializer implements ListSerializer {

        private final List<Tag> entries = new ArrayList<>();

        @Override
        public <V> ListSerializer write(final Type<V> valueType, final V value) {
            entries.add(serialize(valueType, value));
            return this;
        }

        @Override
        public ListSerializer writeList(final Consumer<ListSerializer> consumer) {
            entries.add(serializeList(consumer));
            return this;
        }

        @Override
        public ListSerializer writeMap(final Consumer<MapSerializer> consumer) {
            entries.add(serializeMap(consumer));
            return this;
        }
    }
}

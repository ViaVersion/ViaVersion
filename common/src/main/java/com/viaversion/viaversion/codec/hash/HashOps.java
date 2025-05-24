/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2025 ViaVersion and contributors
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
package com.viaversion.viaversion.codec.hash;

import com.viaversion.viaversion.api.minecraft.codec.CodecContext;
import com.viaversion.viaversion.api.minecraft.codec.hash.Hasher;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.codec.OpsBase;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

public class HashOps extends OpsBase implements Hasher {

    private static final byte TAG_MAP_START = 2;
    private static final byte TAG_MAP_END = 3;
    private static final byte TAG_LIST_START = 4;
    private static final byte TAG_LIST_END = 5;
    private static final byte TAG_BYTE = 6;
    private static final byte TAG_SHORT = 7;
    private static final byte TAG_INT = 8;
    private static final byte TAG_LONG = 9;
    private static final byte TAG_FLOAT = 10;
    private static final byte TAG_DOUBLE = 11;
    private static final byte TAG_STRING = 12;
    private static final byte TAG_BOOLEAN = 13;
    private static final byte TAG_BYTE_ARRAY_START = 14;
    private static final byte TAG_BYTE_ARRAY_END = 15;
    private static final byte TAG_INT_ARRAY_START = 16;
    private static final byte TAG_INT_ARRAY_END = 17;
    private static final byte TAG_LONG_ARRAY_START = 18;
    private static final byte TAG_LONG_ARRAY_END = 19;
    static final byte[] FALSE = {TAG_BOOLEAN, 0};
    static final byte[] TRUE = {TAG_BOOLEAN, 1};
    static final byte[] EMPTY_LIST = {TAG_LIST_START, TAG_LIST_END};
    static final byte[] EMPTY_MAP = {TAG_MAP_START, TAG_MAP_END};
    private final HashBuilder hashBuilder;

    public HashOps(final CodecContext context, final HashFunction hashFunction) {
        super(context);
        this.hashBuilder = new HashBuilder(hashFunction);
    }

    @Override
    public void writeByte(final byte b) {
        hashBuilder.preSize(Byte.BYTES + Byte.BYTES)
            .writeByte(TAG_BYTE)
            .writeByte(b);
    }

    @Override
    public void writeBytes(final byte[] array) {
        hashBuilder.preSize(array.length + 2 * Byte.BYTES)
            .writeByte(TAG_BYTE_ARRAY_START)
            .writeBytes(array)
            .writeByte(TAG_BYTE_ARRAY_END);
    }

    @Override
    public void writeBoolean(final boolean b) {
        hashBuilder.writeBytesDirect(b ? TRUE : FALSE);
    }

    @Override
    public void writeShort(final short s) {
        hashBuilder.preSize(Byte.BYTES + Short.BYTES)
            .writeByte(TAG_SHORT)
            .writeShort(s);
    }

    @Override
    public void writeString(final CharSequence sequence) {
        hashBuilder.preSize(sequence.length() + Byte.BYTES + Integer.BYTES)
            .writeByte(TAG_STRING)
            .writeInt(sequence.length())
            .writeString(sequence);
    }

    @Override
    public void writeInt(final int i) {
        hashBuilder.preSize(Byte.BYTES + Integer.BYTES)
            .writeByte(TAG_INT)
            .writeInt(i);
    }

    @Override
    public void writeLong(final long l) {
        hashBuilder.preSize(Byte.BYTES + Long.BYTES)
            .writeByte(TAG_LONG)
            .writeLong(l);
    }

    @Override
    public void writeFloat(final float f) {
        hashBuilder.preSize(Byte.BYTES + Float.BYTES)
            .writeByte(TAG_FLOAT)
            .writeFloat(f);
    }

    @Override
    public void writeDouble(final double d) {
        hashBuilder.preSize(Byte.BYTES + Double.BYTES)
            .writeByte(TAG_DOUBLE)
            .writeDouble(d);
    }

    @Override
    public void writeInts(final int[] array) {
        hashBuilder.preSize(array.length * Integer.BYTES + 2 * Byte.BYTES);
        hashBuilder.writeByte(TAG_INT_ARRAY_START);
        for (final int i : array) {
            hashBuilder.writeInt(i);
        }
        hashBuilder.writeByte(TAG_INT_ARRAY_END);
    }

    @Override
    public void writeLongs(final long[] array) {
        hashBuilder.preSize(array.length * Long.BYTES + 2 * Byte.BYTES);
        hashBuilder.writeByte(TAG_LONG_ARRAY_START);
        for (final long l : array) {
            hashBuilder.writeLong(l);
        }
        hashBuilder.writeByte(TAG_LONG_ARRAY_END);
    }

    @Override
    public void writeList(final Consumer<ListSerializer> consumer) {
        final ListHashBuilder listHasher = new ListHashBuilder();
        consumer.accept(listHasher);
        listHasher.applyHashToParent();
    }

    @Override
    public void writeMap(final Consumer<MapSerializer> consumer) {
        final MapHashBuilder mapHasher = new MapHashBuilder();
        consumer.accept(mapHasher);
        mapHasher.applyHashToParent();
    }

    @Override
    public <V> void write(final Type<V> type, final V value) {
        type.write(this, value);
    }

    @Override
    public int hash() {
        return this.hashBuilder.hash();
    }

    @Override
    public void reset() {
        this.hashBuilder.reset();
    }

    private abstract class CollectionHashBuilder {

        protected final HashOps hasher = new HashOps(context(), hashBuilder.function());

        protected int listHash(final Consumer<ListSerializer> consumer) {
            hasher.reset();
            final ListHashBuilder listHasher = hasher.new ListHashBuilder();
            consumer.accept(listHasher);
            listHasher.applyHashToParent();
            return HashOps.this.hash();
        }

        protected int mapHash(final Consumer<MapSerializer> consumer) {
            hasher.reset();
            final MapHashBuilder mapHasher = hasher.new MapHashBuilder();
            consumer.accept(mapHasher);
            mapHasher.applyHashToParent();
            return HashOps.this.hash();
        }

        protected <V> int hash(final Type<V> type, final V value) {
            hasher.reset();
            type.write(hasher, value);
            return hasher.hash();
        }
    }

    private final class MapHashBuilder extends CollectionHashBuilder implements MapSerializer {

        private final List<Entry> entries = new ArrayList<>();

        @Override
        public <K, T> MapSerializer write(final Type<K> keyType, final K key, final Type<T> valueType, final T value) {
            entries.add(new Entry(hash(keyType, key), hash(valueType, value)));
            return this;
        }

        @Override
        public MapSerializer writeList(final String key, final Consumer<ListSerializer> consumer) {
            entries.add(new Entry(hash(Types.STRING, key), listHash(consumer)));
            return this;
        }

        @Override
        public MapSerializer writeMap(final String key, final Consumer<MapSerializer> consumer) {
            entries.add(new Entry(hash(Types.STRING, key), mapHash(consumer)));
            return this;
        }

        public void applyHashToParent() {
            if (entries.isEmpty()) {
                hashBuilder.writeBytesDirect(EMPTY_MAP);
                return;
            }

            final int size = entries.size();
            hashBuilder.preSize((size * Integer.BYTES * 2) + (2 * Byte.BYTES));
            hashBuilder.writeByte(TAG_MAP_START);
            entries.sort(Comparator.naturalOrder());
            for (int i = 0; i < size; i++) {
                final Entry entry = entries.get(i);
                hashBuilder.writeInt(entry.key);
                hashBuilder.writeInt(entry.value);
            }
            hashBuilder.writeByte(TAG_MAP_END);
        }

        private record Entry(int key, int value) implements Comparable<Entry> {

            @Override
            public int compareTo(final Entry o) {
                // For some reason ordered via unsigned values
                if (this.key != o.key) {
                    return Long.compare(padToLong(this.key), padToLong(o.key));
                } else {
                    return Long.compare(padToLong(this.value), padToLong(o.value));
                }
            }

            private static long padToLong(final int value) {
                return value & 0xFFFFFFFFL;
            }
        }
    }

    private final class ListHashBuilder extends CollectionHashBuilder implements ListSerializer {

        private final IntList entries = new IntArrayList();

        @Override
        public <T> ListSerializer write(final Type<T> valueType, final T value) {
            entries.add(hash(valueType, value));
            return this;
        }

        @Override
        public ListSerializer writeList(final Consumer<ListSerializer> consumer) {
            entries.add(listHash(consumer));
            return this;
        }

        @Override
        public ListSerializer writeMap(final Consumer<MapSerializer> consumer) {
            entries.add(mapHash(consumer));
            return this;
        }

        public void applyHashToParent() {
            if (entries.isEmpty()) {
                hashBuilder.writeBytesDirect(EMPTY_LIST);
                return;
            }

            final int size = entries.size();
            hashBuilder.preSize(size * Integer.BYTES + 2 * Byte.BYTES);
            hashBuilder.writeByte(TAG_LIST_START);
            for (int i = 0; i < size; i++) {
                hashBuilder.writeInt(entries.getInt(i));
            }
            hashBuilder.writeByte(TAG_LIST_END);
        }
    }
}

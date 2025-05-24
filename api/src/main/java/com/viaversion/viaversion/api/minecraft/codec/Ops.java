/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2025 ViaVersion and contributors
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
package com.viaversion.viaversion.api.minecraft.codec;

import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import java.util.Objects;
import java.util.function.Consumer;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface Ops {

    CodecContext context();

    void writeByte(byte b);

    void writeBytes(byte[] array);

    void writeBoolean(boolean b);

    void writeShort(short s);

    void writeString(CharSequence sequence);

    void writeInt(int i);

    void writeLong(long l);

    void writeFloat(float f);

    void writeDouble(double d);

    void writeInts(int[] array);

    void writeLongs(long[] array);

    void writeList(Consumer<ListSerializer> consumer);

    void writeMap(Consumer<MapSerializer> consumer);

    <V> void write(final Type<V> type, final V value);

    interface ListSerializer {

        <V> ListSerializer write(Type<V> valueType, V value);

        ListSerializer writeList(Consumer<ListSerializer> consumer);

        ListSerializer writeMap(Consumer<MapSerializer> consumer);
    }

    interface MapSerializer {

        <K, V> MapSerializer write(Type<K> keyType, K key, Type<V> valueType, V value);

        default <V> MapSerializer write(final String key, final Type<V> type, final V value) {
            write(Types.STRING, key, type, value);
            return this;
        }

        /**
         * Writes the given value or default value if the value is null.
         *
         * @param key map key
         * @param type type of the value
         * @param value the value to write, or null
         * @param def default value to write if the value is null
         * @return self
         * @param <V> value type
         */
        default <V> MapSerializer write(final String key, final Type<V> type, @Nullable final V value, final V def) {
            write(Types.STRING, key, type, value != null ? value : def);
            return this;
        }

        /**
         * Writes the given value if it is not null.
         *
         * @param key map key
         * @param type type of the value
         * @param value the value to write, or null
         * @return self
         * @param <V> value type
         */
        default <V> MapSerializer writeOptional(final String key, final Type<V> type, @Nullable final V value) {
            if (value != null) {
                write(Types.STRING, key, type, value);
            }
            return this;
        }

        /**
         * Writes the given value if it is not null and not equal to the default value.
         *
         * @param key map key
         * @param type type of the value
         * @param value the value to write, or null
         * @param def default value to compare against
         * @return self
         * @param <V> value type
         */
        default <V> MapSerializer writeOptional(final String key, final Type<V> type, @Nullable final V value, final V def) {
            if (value != null && !Objects.deepEquals(value, def)) {
                write(Types.STRING, key, type, value);
            }
            return this;
        }

        MapSerializer writeList(String key, Consumer<ListSerializer> consumer);

        MapSerializer writeMap(String key, Consumer<MapSerializer> consumer);
    }
}

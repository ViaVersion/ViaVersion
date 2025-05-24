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
package com.viaversion.viaversion.api.minecraft.item.data;

import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.util.Copyable;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;

public record BlockStateProperties(Map<String, String> properties) implements Copyable {

    public static final Type<BlockStateProperties> TYPE = new Type<>(BlockStateProperties.class) {
        @Override
        public BlockStateProperties read(final ByteBuf buffer) {
            final int size = Types.VAR_INT.readPrimitive(buffer);
            final Map<String, String> properties = new Object2ObjectOpenHashMap<>();
            for (int i = 0; i < size; i++) {
                properties.put(Types.STRING.read(buffer), Types.STRING.read(buffer));
            }
            return new BlockStateProperties(properties);
        }

        @Override
        public void write(final ByteBuf buffer, final BlockStateProperties value) {
            Types.VAR_INT.writePrimitive(buffer, value.properties.size());
            for (final Map.Entry<String, String> entry : value.properties.entrySet()) {
                Types.STRING.write(buffer, entry.getKey());
                Types.STRING.write(buffer, entry.getValue());
            }
        }

        @Override
        public void write(final Ops ops, final BlockStateProperties value) {
            ops.writeMap(map -> {
                for (final Map.Entry<String, String> entry : value.properties.entrySet()) {
                    map.write(Types.STRING, entry.getKey(), Types.STRING, entry.getValue());
                }
            });
        }
    };

    @Override
    public BlockStateProperties copy() {
        return new BlockStateProperties(new Object2ObjectOpenHashMap<>(properties));
    }
}

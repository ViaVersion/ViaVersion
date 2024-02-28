/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2024 ViaVersion and contributors
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

import com.viaversion.viaversion.api.type.Type;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;

public final class BlockStateProperties {

    public static final Type<BlockStateProperties> TYPE = new Type<BlockStateProperties>(BlockStateProperties.class) {
        @Override
        public BlockStateProperties read(final ByteBuf buffer) throws Exception {
            final int size = Type.VAR_INT.readPrimitive(buffer);
            final Map<String, String> properties = new Object2ObjectOpenHashMap<>(size);
            for (int i = 0; i < size; i++) {
                properties.put(Type.STRING.read(buffer), Type.STRING.read(buffer));
            }
            return new BlockStateProperties(properties);
        }

        @Override
        public void write(final ByteBuf buffer, final BlockStateProperties value) throws Exception {
            Type.VAR_INT.writePrimitive(buffer, value.properties.size());
            for (final Map.Entry<String, String> entry : value.properties.entrySet()) {
                Type.STRING.write(buffer, entry.getKey());
                Type.STRING.write(buffer, entry.getValue());
            }
        }
    };

    private final Map<String, String> properties;

    public BlockStateProperties(final Map<String, String> properties) {
        this.properties = properties;
    }

    public Map<String, String> properties() {
        return properties;
    }
}

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
import com.viaversion.viaversion.api.type.types.ArrayType;
import io.netty.buffer.ByteBuf;

public final class AttributeModifier {

    public static final Type<AttributeModifier> TYPE = new Type<AttributeModifier>(AttributeModifier.class) {
        @Override
        public AttributeModifier read(final ByteBuf buffer) throws Exception {
            final int attribute = Type.VAR_INT.readPrimitive(buffer);
            final ModifierData modifier = ModifierData.TYPE.read(buffer);
            final int slot = Type.VAR_INT.readPrimitive(buffer);
            return new AttributeModifier(attribute, modifier, slot);
        }

        @Override
        public void write(final ByteBuf buffer, final AttributeModifier value) throws Exception {
            Type.VAR_INT.writePrimitive(buffer, value.attribute);
            ModifierData.TYPE.write(buffer, value.modifier);
            Type.VAR_INT.writePrimitive(buffer, value.slotType);
        }
    };
    public static final Type<AttributeModifier[]> ARRAY_TYPE = new ArrayType<>(TYPE);

    private final int attribute;
    private final ModifierData modifier;
    private final int slotType;

    public AttributeModifier(final int attribute, final ModifierData modifier, final int slotType) {
        this.attribute = attribute;
        this.modifier = modifier;
        this.slotType = slotType;
    }

    public int attribute() {
        return attribute;
    }

    public ModifierData modifier() {
        return modifier;
    }

    public int slotType() {
        return slotType;
    }
}

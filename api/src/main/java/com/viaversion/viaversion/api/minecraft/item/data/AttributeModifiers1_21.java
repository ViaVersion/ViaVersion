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
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.ArrayType;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;

public record AttributeModifiers1_21(AttributeModifier[] modifiers, boolean showInTooltip) {

    public static final Type<AttributeModifiers1_21> TYPE = new Type<>(AttributeModifiers1_21.class) {
        @Override
        public AttributeModifiers1_21 read(final ByteBuf buffer) {
            final AttributeModifier[] modifiers = AttributeModifier.ARRAY_TYPE.read(buffer);
            final boolean showInTooltip = buffer.readBoolean();
            return new AttributeModifiers1_21(modifiers, showInTooltip);
        }

        @Override
        public void write(final ByteBuf buffer, final AttributeModifiers1_21 value) {
            AttributeModifier.ARRAY_TYPE.write(buffer, value.modifiers());
            buffer.writeBoolean(value.showInTooltip());
        }
    };

    public AttributeModifiers1_21 rewrite(final Int2IntFunction rewriteFunction) {
        final AttributeModifier[] modifiers = new AttributeModifier[this.modifiers.length];
        for (int i = 0; i < this.modifiers.length; i++) {
            final AttributeModifier modifier = this.modifiers[i];
            modifiers[i] = new AttributeModifier(rewriteFunction.applyAsInt(modifier.attribute()), modifier.modifier(), modifier.slotType());
        }
        return new AttributeModifiers1_21(modifiers, showInTooltip);
    }

    public record AttributeModifier(int attribute, ModifierData modifier, int slotType) {

        public static final Type<AttributeModifier> TYPE = new Type<>(AttributeModifier.class) {
            @Override
            public AttributeModifier read(final ByteBuf buffer) {
                final int attribute = Types.VAR_INT.readPrimitive(buffer);
                final ModifierData modifier = ModifierData.TYPE.read(buffer);
                final int slot = Types.VAR_INT.readPrimitive(buffer);
                return new AttributeModifier(attribute, modifier, slot);
            }

            @Override
            public void write(final ByteBuf buffer, final AttributeModifier value) {
                Types.VAR_INT.writePrimitive(buffer, value.attribute);
                ModifierData.TYPE.write(buffer, value.modifier);
                Types.VAR_INT.writePrimitive(buffer, value.slotType);
            }
        };
        public static final Type<AttributeModifier[]> ARRAY_TYPE = new ArrayType<>(TYPE);
    }

    public record ModifierData(String id, double amount, int operation) {

        public static final Type<ModifierData> TYPE = new Type<>(ModifierData.class) {
            @Override
            public ModifierData read(final ByteBuf buffer) {
                final String id = Types.STRING.read(buffer);
                final double amount = buffer.readDouble();
                final int operation = Types.VAR_INT.readPrimitive(buffer);
                return new ModifierData(id, amount, operation);
            }

            @Override
            public void write(final ByteBuf buffer, final ModifierData value) {
                Types.STRING.write(buffer, value.id);
                buffer.writeDouble(value.amount);
                Types.VAR_INT.writePrimitive(buffer, value.operation);
            }
        };
    }
}

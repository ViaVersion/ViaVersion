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

import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.ArrayType;
import com.viaversion.viaversion.util.Copyable;
import io.netty.buffer.ByteBuf;
import java.util.UUID;

public record AttributeModifiers1_20_5(AttributeModifier[] modifiers, boolean showInTooltip) implements Copyable {

    public static final Type<AttributeModifiers1_20_5> TYPE = new Type<>(AttributeModifiers1_20_5.class) {
        @Override
        public AttributeModifiers1_20_5 read(final ByteBuf buffer) {
            final AttributeModifier[] modifiers = AttributeModifier.ARRAY_TYPE.read(buffer);
            final boolean showInTooltip = buffer.readBoolean();
            return new AttributeModifiers1_20_5(modifiers, showInTooltip);
        }

        @Override
        public void write(final ByteBuf buffer, final AttributeModifiers1_20_5 value) {
            AttributeModifier.ARRAY_TYPE.write(buffer, value.modifiers());
            buffer.writeBoolean(value.showInTooltip());
        }
    };

    @Override
    public AttributeModifiers1_20_5 copy() {
        return new AttributeModifiers1_20_5(Copyable.copy(modifiers), showInTooltip);
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

    public record ModifierData(UUID uuid, String name, double amount, int operation) {

        public static final Type<ModifierData> TYPE = new Type<>(ModifierData.class) {
            @Override
            public ModifierData read(final ByteBuf buffer) {
                final UUID uuid = Types.UUID.read(buffer);
                final String name = Types.STRING.read(buffer);
                final double amount = buffer.readDouble();
                final int operation = Types.VAR_INT.readPrimitive(buffer);
                return new ModifierData(uuid, name, amount, operation);
            }

            @Override
            public void write(final ByteBuf buffer, final ModifierData value) {
                Types.UUID.write(buffer, value.uuid);
                Types.STRING.write(buffer, value.name);
                buffer.writeDouble(value.amount);
                Types.VAR_INT.writePrimitive(buffer, value.operation);
            }
        };
    }
}

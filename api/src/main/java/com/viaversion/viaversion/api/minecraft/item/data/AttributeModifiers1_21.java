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

import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.ArrayType;
import com.viaversion.viaversion.util.Copyable;
import com.viaversion.viaversion.util.Key;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;

public record AttributeModifiers1_21(AttributeModifier[] modifiers, boolean showInTooltip) implements Copyable {

    public AttributeModifiers1_21(final AttributeModifier[] modifiers) {
        this(modifiers, true);
    }

    public static final Type<AttributeModifiers1_21> TYPE1_21 = new Type<>(AttributeModifiers1_21.class) {
        @Override
        public AttributeModifiers1_21 read(final ByteBuf buffer) {
            final AttributeModifier[] modifiers = AttributeModifier.ARRAY_TYPE1_21.read(buffer);
            final boolean showInTooltip = buffer.readBoolean();
            return new AttributeModifiers1_21(modifiers, showInTooltip);
        }

        @Override
        public void write(final ByteBuf buffer, final AttributeModifiers1_21 value) {
            AttributeModifier.ARRAY_TYPE1_21.write(buffer, value.modifiers());
            buffer.writeBoolean(value.showInTooltip());
        }
    };
    public static final Type<AttributeModifiers1_21> TYPE1_21_5 = new Type<>(AttributeModifiers1_21.class) {
        @Override
        public AttributeModifiers1_21 read(final ByteBuf buffer) {
            final AttributeModifier[] modifiers = AttributeModifier.ARRAY_TYPE1_21.read(buffer);
            return new AttributeModifiers1_21(modifiers);
        }

        @Override
        public void write(final ByteBuf buffer, final AttributeModifiers1_21 value) {
            AttributeModifier.ARRAY_TYPE1_21.write(buffer, value.modifiers());
        }

        @Override
        public void write(final Ops ops, final AttributeModifiers1_21 value) {
            ops.write(AttributeModifier.ARRAY_TYPE1_21, value.modifiers);
        }
    };
    public static final Type<AttributeModifiers1_21> TYPE1_21_6 = new Type<>(AttributeModifiers1_21.class) {
        @Override
        public AttributeModifiers1_21 read(final ByteBuf buffer) {
            final AttributeModifier[] modifiers = AttributeModifier.ARRAY_TYPE1_21_6.read(buffer);
            return new AttributeModifiers1_21(modifiers);
        }

        @Override
        public void write(final ByteBuf buffer, final AttributeModifiers1_21 value) {
            AttributeModifier.ARRAY_TYPE1_21_6.write(buffer, value.modifiers());
        }

        @Override
        public void write(final Ops ops, final AttributeModifiers1_21 value) {
            ops.write(AttributeModifier.ARRAY_TYPE1_21_6, value.modifiers);
        }
    };

    public AttributeModifiers1_21 rewrite(final Int2IntFunction rewriteFunction) {
        final AttributeModifier[] modifiers = new AttributeModifier[this.modifiers.length];
        for (int i = 0; i < this.modifiers.length; i++) {
            final AttributeModifier modifier = this.modifiers[i];
            modifiers[i] = new AttributeModifier(rewriteFunction.applyAsInt(modifier.attribute()), modifier.modifier(), modifier.slotType(), modifier.display());
        }
        return new AttributeModifiers1_21(modifiers, showInTooltip);
    }

    @Override
    public AttributeModifiers1_21 copy() {
        return new AttributeModifiers1_21(Copyable.copy(modifiers), showInTooltip);
    }

    public record AttributeModifier(int attribute, ModifierData modifier, int slotType, Display display) {
        private static final String[] EQUIPMENT_SLOT_GROUPS = {"any", "mainhand", "offhand", "hand", "feet", "legs", "chest", "head", "armor", "body", "saddle"};
        private static final String[] OPERATION = {"add_value", "add_multiplied_base", "add_multiplied_total"};

        public AttributeModifier(final int attribute, final ModifierData modifier, final int slotType) {
            this(attribute, modifier, slotType, Display.DEFAULT);
        }

        public static final Type<AttributeModifier> TYPE1_21 = new Type<>(AttributeModifier.class) {
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

            @Override
            public void write(final Ops ops, final AttributeModifier value) {
                final Key attribute = ops.context().registryAccess().attributeModifier(value.attribute);
                ops.writeMap(map -> map
                    .write("type", Types.RESOURCE_LOCATION, attribute)
                    .write("id", Types.RESOURCE_LOCATION, Key.of(value.modifier.id()))
                    .write("amount", Types.DOUBLE, value.modifier.amount)
                    .write("operation", Types.STRING, OPERATION[value.modifier.operation()])
                    .writeOptional("slot", Types.STRING, EQUIPMENT_SLOT_GROUPS[value.slotType()], "any"));
            }
        };
        public static final Type<AttributeModifier[]> ARRAY_TYPE1_21 = new ArrayType<>(TYPE1_21);

        public static final Type<AttributeModifier> TYPE1_21_6 = new Type<>(AttributeModifier.class) {
            @Override
            public AttributeModifier read(final ByteBuf buffer) {
                final int attribute = Types.VAR_INT.readPrimitive(buffer);
                final ModifierData modifier = ModifierData.TYPE.read(buffer);
                final int slot = Types.VAR_INT.readPrimitive(buffer);
                final int displayType = Types.VAR_INT.readPrimitive(buffer);
                final Display display = displayType == OverrideText.ID ? new OverrideText(Types.TAG.read(buffer)) : new Display(displayType);
                return new AttributeModifier(attribute, modifier, slot, display);
            }

            @Override
            public void write(final ByteBuf buffer, final AttributeModifier value) {
                Types.VAR_INT.writePrimitive(buffer, value.attribute);
                ModifierData.TYPE.write(buffer, value.modifier);
                Types.VAR_INT.writePrimitive(buffer, value.slotType);
                value.display.write(buffer);
            }

            @Override
            public void write(final Ops ops, final AttributeModifier value) {
                final Key attribute = ops.context().registryAccess().attributeModifier(value.attribute);
                ops.writeMap(map -> {
                    map.write("type", Types.RESOURCE_LOCATION, attribute)
                        .write("id", Types.RESOURCE_LOCATION, Key.of(value.modifier.id()))
                        .write("amount", Types.DOUBLE, value.modifier.amount)
                        .write("operation", Types.STRING, OPERATION[value.modifier.operation()])
                        .writeOptional("slot", Types.STRING, EQUIPMENT_SLOT_GROUPS[value.slotType()], "any");
                    if (value.display.equals(Display.DEFAULT)) {
                        return;
                    }

                    map.writeMap("display", display -> {
                        display.write("type", Types.STRING, Display.DISPLAY_TYPES[value.display.id()]);
                        if (value.display instanceof final OverrideText overrideText) {
                            display.write("value", Types.TAG, overrideText.component);
                        }
                    });
                });
            }
        };
        public static final Type<AttributeModifier[]> ARRAY_TYPE1_21_6 = new ArrayType<>(TYPE1_21_6);
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

    public static class Display implements Copyable {
        public static final String[] DISPLAY_TYPES = {"default", "hidden", "override_text"};
        public static final Display DEFAULT = new Display(0);
        private final int id;

        public Display(final int id) {
            this.id = id;
        }

        public int id() {
            return this.id;
        }

        public void write(final ByteBuf buf) {
            Types.VAR_INT.writePrimitive(buf, this.id);
        }

        @Override
        public Display copy() {
            return this;
        }
    }

    public static final class OverrideText extends Display {
        public static final int ID = 2;
        private final Tag component;

        public OverrideText(final Tag component) {
            super(ID);
            this.component = component;
        }

        public Tag component() {
            return this.component;
        }

        @Override
        public void write(final ByteBuf buf) {
            super.write(buf);
            Types.TAG.write(buf, this.component);
        }

        @Override
        public OverrideText copy() {
            return new OverrideText(this.component.copy());
        }
    }
}

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

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.Holder;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.util.Rewritable;
import io.netty.buffer.ByteBuf;

public record ArmorTrim(Holder<ArmorTrimMaterial> material, Holder<ArmorTrimPattern> pattern,
                        boolean showInTooltip) implements Rewritable {

    public ArmorTrim(Holder<ArmorTrimMaterial> material, Holder<ArmorTrimPattern> pattern) {
        this(material, pattern, true);
    }

    public static final Type<ArmorTrim> TYPE1_20_5 = new Type<>(ArmorTrim.class) {
        @Override
        public ArmorTrim read(final ByteBuf buffer) {
            final Holder<ArmorTrimMaterial> material = ArmorTrimMaterial.TYPE1_20_5.read(buffer);
            final Holder<ArmorTrimPattern> pattern = ArmorTrimPattern.TYPE1_20_5.read(buffer);
            final boolean showInTooltip = buffer.readBoolean();
            return new ArmorTrim(material, pattern, showInTooltip);
        }

        @Override
        public void write(final ByteBuf buffer, final ArmorTrim value) {
            ArmorTrimMaterial.TYPE1_20_5.write(buffer, value.material);
            ArmorTrimPattern.TYPE1_20_5.write(buffer, value.pattern);
            buffer.writeBoolean(value.showInTooltip);
        }
    };
    public static final Type<ArmorTrim> TYPE1_21_2 = new Type<>(ArmorTrim.class) {
        @Override
        public ArmorTrim read(final ByteBuf buffer) {
            final Holder<ArmorTrimMaterial> material = ArmorTrimMaterial.TYPE1_21_2.read(buffer);
            final Holder<ArmorTrimPattern> pattern = ArmorTrimPattern.TYPE1_20_5.read(buffer);
            final boolean showInTooltip = buffer.readBoolean();
            return new ArmorTrim(material, pattern, showInTooltip);
        }

        @Override
        public void write(final ByteBuf buffer, final ArmorTrim value) {
            ArmorTrimMaterial.TYPE1_21_2.write(buffer, value.material);
            ArmorTrimPattern.TYPE1_20_5.write(buffer, value.pattern);
            buffer.writeBoolean(value.showInTooltip);
        }
    };
    public static final Type<ArmorTrim> TYPE1_21_4 = new Type<>(ArmorTrim.class) {
        @Override
        public ArmorTrim read(final ByteBuf buffer) {
            final Holder<ArmorTrimMaterial> material = ArmorTrimMaterial.TYPE1_21_4.read(buffer);
            final Holder<ArmorTrimPattern> pattern = ArmorTrimPattern.TYPE1_20_5.read(buffer);
            final boolean showInTooltip = buffer.readBoolean();
            return new ArmorTrim(material, pattern, showInTooltip);
        }

        @Override
        public void write(final ByteBuf buffer, final ArmorTrim value) {
            ArmorTrimMaterial.TYPE1_21_4.write(buffer, value.material);
            ArmorTrimPattern.TYPE1_20_5.write(buffer, value.pattern);
            buffer.writeBoolean(value.showInTooltip);
        }
    };
    public static final Type<ArmorTrim> TYPE1_21_5 = new Type<>(ArmorTrim.class) {
        @Override
        public ArmorTrim read(final ByteBuf buffer) {
            final Holder<ArmorTrimMaterial> material = ArmorTrimMaterial.TYPE1_21_5.read(buffer);
            final Holder<ArmorTrimPattern> pattern = ArmorTrimPattern.TYPE1_21_5.read(buffer);
            return new ArmorTrim(material, pattern);
        }

        @Override
        public void write(final ByteBuf buffer, final ArmorTrim value) {
            ArmorTrimMaterial.TYPE1_21_5.write(buffer, value.material);
            ArmorTrimPattern.TYPE1_21_5.write(buffer, value.pattern);
        }
    };

    @Override
    public ArmorTrim rewrite(final UserConnection connection, final Protocol<?, ?, ?, ?> protocol, final boolean clientbound) {
        Holder<ArmorTrimMaterial> material = this.material;
        if (material.isDirect()) {
            material = Holder.of(material.value().rewrite(connection, protocol, clientbound));
        }

        Holder<ArmorTrimPattern> pattern = this.pattern;
        if (pattern.isDirect()) {
            pattern = Holder.of(pattern.value().rewrite(connection, protocol, clientbound));
        }
        return new ArmorTrim(material, pattern, showInTooltip);
    }
}

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
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.misc.HolderType;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.util.Map;

public record ArmorTrimMaterial(String assetName, int itemId, float itemModelIndex,
                                Map<String, String> overrideArmorMaterials, Tag description) {

    public ArmorTrimMaterial(final String assetName, final int itemId, final Map<String, String> overrideArmorMaterials, final Tag description) {
        this(assetName, itemId, 0F, overrideArmorMaterials, description);
    }

    public static final HolderType<ArmorTrimMaterial> TYPE1_20_5 = new HolderType<>() {
        // The override key is an int, but given we don't use it at all and that creating a new type is annoying,
        // we'll just store it in the string map:tm:
        @Override
        public ArmorTrimMaterial readDirect(final ByteBuf buffer) {
            final String assetName = Types.STRING.read(buffer);
            final int item = Types.VAR_INT.readPrimitive(buffer);
            final float itemModelIndex = buffer.readFloat();

            final int overrideArmorMaterialsSize = Types.VAR_INT.readPrimitive(buffer);
            final Map<String, String> overrideArmorMaterials = new Object2ObjectArrayMap<>(overrideArmorMaterialsSize);
            for (int i = 0; i < overrideArmorMaterialsSize; i++) {
                final int key = Types.VAR_INT.readPrimitive(buffer);
                final String value = Types.STRING.read(buffer);
                overrideArmorMaterials.put(Integer.toString(key), value);
            }

            final Tag description = Types.TAG.read(buffer);
            return new ArmorTrimMaterial(assetName, item, itemModelIndex, overrideArmorMaterials, description);
        }

        @Override
        public void writeDirect(final ByteBuf buffer, final ArmorTrimMaterial value) {
            Types.STRING.write(buffer, value.assetName());
            Types.VAR_INT.writePrimitive(buffer, value.itemId());
            buffer.writeFloat(value.itemModelIndex());

            Types.VAR_INT.writePrimitive(buffer, value.overrideArmorMaterials().size());
            for (final Map.Entry<String, String> entry : value.overrideArmorMaterials().entrySet()) {
                Types.VAR_INT.writePrimitive(buffer, Integer.parseInt(entry.getKey()));
                Types.STRING.write(buffer, entry.getValue());
            }

            Types.TAG.write(buffer, value.description());
        }
    };

    public static final HolderType<ArmorTrimMaterial> TYPE1_21_2 = new HolderType<>() {
        @Override
        public ArmorTrimMaterial readDirect(final ByteBuf buffer) {
            final String assetName = Types.STRING.read(buffer);
            final int item = Types.VAR_INT.readPrimitive(buffer);
            final float itemModelIndex = buffer.readFloat();

            final int overrideArmorMaterialsSize = Types.VAR_INT.readPrimitive(buffer);
            final Map<String, String> overrideArmorMaterials = new Object2ObjectArrayMap<>(overrideArmorMaterialsSize);
            for (int i = 0; i < overrideArmorMaterialsSize; i++) {
                final String key = Types.STRING.read(buffer);
                final String value = Types.STRING.read(buffer);
                overrideArmorMaterials.put(key, value);
            }

            final Tag description = Types.TAG.read(buffer);
            return new ArmorTrimMaterial(assetName, item, itemModelIndex, overrideArmorMaterials, description);
        }

        @Override
        public void writeDirect(final ByteBuf buffer, final ArmorTrimMaterial value) {
            Types.STRING.write(buffer, value.assetName());
            Types.VAR_INT.writePrimitive(buffer, value.itemId());
            buffer.writeFloat(value.itemModelIndex());

            Types.VAR_INT.writePrimitive(buffer, value.overrideArmorMaterials().size());
            for (final Map.Entry<String, String> entry : value.overrideArmorMaterials().entrySet()) {
                Types.STRING.write(buffer, entry.getKey());
                Types.STRING.write(buffer, entry.getValue());
            }

            Types.TAG.write(buffer, value.description());
        }
    };

    public static final HolderType<ArmorTrimMaterial> TYPE1_21_4 = new HolderType<>() {
        @Override
        public ArmorTrimMaterial readDirect(final ByteBuf buffer) {
            final String assetName = Types.STRING.read(buffer);
            final int item = Types.VAR_INT.readPrimitive(buffer);

            final int overrideArmorMaterialsSize = Types.VAR_INT.readPrimitive(buffer);
            final Map<String, String> overrideArmorMaterials = new Object2ObjectArrayMap<>(overrideArmorMaterialsSize);
            for (int i = 0; i < overrideArmorMaterialsSize; i++) {
                final String key = Types.STRING.read(buffer);
                final String value = Types.STRING.read(buffer);
                overrideArmorMaterials.put(key, value);
            }

            final Tag description = Types.TAG.read(buffer);
            return new ArmorTrimMaterial(assetName, item, overrideArmorMaterials, description);
        }

        @Override
        public void writeDirect(final ByteBuf buffer, final ArmorTrimMaterial value) {
            Types.STRING.write(buffer, value.assetName());
            Types.VAR_INT.writePrimitive(buffer, value.itemId());

            Types.VAR_INT.writePrimitive(buffer, value.overrideArmorMaterials().size());
            for (final Map.Entry<String, String> entry : value.overrideArmorMaterials().entrySet()) {
                Types.STRING.write(buffer, entry.getKey());
                Types.STRING.write(buffer, entry.getValue());
            }

            Types.TAG.write(buffer, value.description());
        }
    };

    public ArmorTrimMaterial rewrite(final Int2IntFunction idRewriteFunction) {
        return new ArmorTrimMaterial(assetName, idRewriteFunction.applyAsInt(itemId), itemModelIndex, overrideArmorMaterials, description);
    }
}

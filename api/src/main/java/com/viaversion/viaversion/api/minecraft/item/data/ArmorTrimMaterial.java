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

import com.github.steveice10.opennbt.tag.builtin.Tag;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.misc.HolderType;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public final class ArmorTrimMaterial {

    public static final HolderType<ArmorTrimMaterial> TYPE = new HolderType<ArmorTrimMaterial>() {
        @Override
        public ArmorTrimMaterial readDirect(final ByteBuf buffer) throws Exception {
            final String assetName = Type.STRING.read(buffer);
            final int item = Type.VAR_INT.readPrimitive(buffer);
            final float itemModelIndex = buffer.readFloat();

            final int overrideArmorMaterialsSize = Type.VAR_INT.readPrimitive(buffer);
            final Int2ObjectMap<String> overrideArmorMaterials = new Int2ObjectOpenHashMap<>(overrideArmorMaterialsSize);
            for (int i = 0; i < overrideArmorMaterialsSize; i++) {
                final int key = Type.VAR_INT.readPrimitive(buffer);
                final String value = Type.STRING.read(buffer);
                overrideArmorMaterials.put(key, value);
            }

            final Tag description = Type.TAG.read(buffer);
            return new ArmorTrimMaterial(assetName, item, itemModelIndex, overrideArmorMaterials, description);
        }

        @Override
        public void writeDirect(final ByteBuf buffer, final ArmorTrimMaterial value) throws Exception {
            Type.STRING.write(buffer, value.assetName());
            Type.VAR_INT.writePrimitive(buffer, value.itemId());
            buffer.writeFloat(value.itemModelIndex());

            Type.VAR_INT.writePrimitive(buffer, value.overrideArmorMaterials().size());
            for (final Int2ObjectMap.Entry<String> entry : value.overrideArmorMaterials().int2ObjectEntrySet()) {
                Type.VAR_INT.writePrimitive(buffer, entry.getIntKey());
                Type.STRING.write(buffer, entry.getValue());
            }

            Type.TAG.write(buffer, value.description());
        }
    };

    private final String assetName;
    private final int itemId;
    private final float itemModelIndex;
    private final Int2ObjectMap<String> overrideArmorMaterials;
    private final Tag description;

    public ArmorTrimMaterial(final String assetName, final int itemId, final float itemModelIndex, final Int2ObjectMap<String> overrideArmorMaterials, final Tag description) {
        this.assetName = assetName;
        this.itemId = itemId;
        this.itemModelIndex = itemModelIndex;
        this.overrideArmorMaterials = overrideArmorMaterials;
        this.description = description;
    }

    public String assetName() {
        return assetName;
    }

    public int itemId() {
        return itemId;
    }

    public float itemModelIndex() {
        return itemModelIndex;
    }

    public Int2ObjectMap<String> overrideArmorMaterials() {
        return overrideArmorMaterials;
    }

    public Tag description() {
        return description;
    }
}

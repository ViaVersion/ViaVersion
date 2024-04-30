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
package com.viaversion.viaversion.api.type.types.version;

import com.viaversion.viaversion.api.minecraft.Particle;
import com.viaversion.viaversion.api.minecraft.data.StructuredData;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.metadata.Metadata;
import com.viaversion.viaversion.api.minecraft.metadata.types.MetaTypes1_20_5;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.ArrayType;
import com.viaversion.viaversion.api.type.types.item.ItemCostType1_20_5;
import com.viaversion.viaversion.api.type.types.item.ItemType1_20_5;
import com.viaversion.viaversion.api.type.types.item.StructuredDataType;
import com.viaversion.viaversion.api.type.types.metadata.MetaListType;
import com.viaversion.viaversion.api.type.types.metadata.MetadataType;
import com.viaversion.viaversion.api.type.types.misc.ParticleType;
import java.util.List;

// Most of these are only safe to use after protocol loading
public final class Types1_20_5 {

    public static final StructuredDataType STRUCTURED_DATA = new StructuredDataType();
    public static final Type<StructuredData<?>[]> STRUCTURED_DATA_ARRAY = new ArrayType<>(STRUCTURED_DATA);
    public static final Type<Item> ITEM = new ItemType1_20_5(STRUCTURED_DATA);
    public static final Type<Item[]> ITEM_ARRAY = new ArrayType<>(ITEM);
    public static final Type<Item> ITEM_COST = new ItemCostType1_20_5(STRUCTURED_DATA_ARRAY);
    public static final Type<Item> OPTIONAL_ITEM_COST = new ItemCostType1_20_5.OptionalItemCostType(ITEM_COST);

    public static final ParticleType PARTICLE = new ParticleType();
    public static final ArrayType<Particle> PARTICLES = new ArrayType<>(PARTICLE);
    public static final MetaTypes1_20_5 META_TYPES = new MetaTypes1_20_5(PARTICLE, PARTICLES);
    public static final Type<Metadata> METADATA = new MetadataType(META_TYPES);
    public static final Type<List<Metadata>> METADATA_LIST = new MetaListType(METADATA);
}

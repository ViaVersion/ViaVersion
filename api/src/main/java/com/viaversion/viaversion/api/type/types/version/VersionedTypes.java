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
package com.viaversion.viaversion.api.type.types.version;

import com.viaversion.viaversion.api.minecraft.data.version.StructuredDataKeys1_20_5;
import com.viaversion.viaversion.api.minecraft.data.version.StructuredDataKeys1_21_2;
import com.viaversion.viaversion.api.minecraft.data.version.StructuredDataKeys1_21_5;
import com.viaversion.viaversion.api.minecraft.entitydata.types.EntityDataTypes1_20_5;
import com.viaversion.viaversion.api.minecraft.entitydata.types.EntityDataTypes1_21;
import com.viaversion.viaversion.api.minecraft.entitydata.types.EntityDataTypes1_21_2;
import com.viaversion.viaversion.api.minecraft.entitydata.types.EntityDataTypes1_21_5;
import com.viaversion.viaversion.api.minecraft.entitydata.types.EntityDataTypes1_21_9;

/**
 * Holds versioned accessors for highly volatile types, specifically around items and data components.
 * This includes anything that relies on them, such as entity data and particles.
 * <p>
 * Only safe to use after protocol loading
 */
public final class VersionedTypes {

    public static final Types1_20_5<StructuredDataKeys1_20_5, EntityDataTypes1_20_5> V1_20_5 = new Types1_20_5<>(StructuredDataKeys1_20_5::new, EntityDataTypes1_20_5::new);
    public static final Types1_21 V1_21 = new Types1_21(StructuredDataKeys1_20_5::new, EntityDataTypes1_21::new);
    public static final Types1_20_5<StructuredDataKeys1_21_2, EntityDataTypes1_21_2> V1_21_2 = new Types1_20_5<>(StructuredDataKeys1_21_2::new, EntityDataTypes1_21_2::new);
    public static final Types1_20_5<StructuredDataKeys1_21_2, EntityDataTypes1_21_2> V1_21_4 = new Types1_20_5<>(StructuredDataKeys1_21_2::new, EntityDataTypes1_21_2::new);
    public static final Types1_20_5<StructuredDataKeys1_21_5, EntityDataTypes1_21_5> V1_21_5 = new Types1_20_5<>(StructuredDataKeys1_21_5::new, EntityDataTypes1_21_5::new);
    public static final Types1_20_5<StructuredDataKeys1_21_5, EntityDataTypes1_21_5> V1_21_6 = new Types1_20_5<>(StructuredDataKeys1_21_5::new, EntityDataTypes1_21_5::new);
    public static final Types1_20_5<StructuredDataKeys1_21_5, EntityDataTypes1_21_9> V1_21_9 = new Types1_20_5<>(StructuredDataKeys1_21_5::new, EntityDataTypes1_21_9::new);
}

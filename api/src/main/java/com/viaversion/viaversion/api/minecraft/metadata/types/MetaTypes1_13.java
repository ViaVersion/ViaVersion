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
package com.viaversion.viaversion.api.minecraft.metadata.types;

import com.viaversion.viaversion.api.minecraft.metadata.MetaType;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.misc.ParticleType;

public final class MetaTypes1_13 extends AbstractMetaTypes {

    public final MetaType byteType = add(0, Types.BYTE);
    public final MetaType varIntType = add(1, Types.VAR_INT);
    public final MetaType floatType = add(2, Types.FLOAT);
    public final MetaType stringType = add(3, Types.STRING);
    public final MetaType componentType = add(4, Types.COMPONENT);
    public final MetaType optionalComponentType = add(5, Types.OPTIONAL_COMPONENT);
    public final MetaType itemType = add(6, Types.ITEM1_13);
    public final MetaType booleanType = add(7, Types.BOOLEAN);
    public final MetaType rotationsType = add(8, Types.ROTATIONS);
    public final MetaType blockPositionType = add(9, Types.BLOCK_POSITION1_8);
    public final MetaType optionalBlockPositionType = add(10, Types.OPTIONAL_POSITION1_8);
    public final MetaType directionType = add(11, Types.VAR_INT);
    public final MetaType optionalUUIDType = add(12, Types.OPTIONAL_UUID);
    public final MetaType optionalBlockStateType = add(13, Types.VAR_INT);
    public final MetaType compoundTagType = add(14, Types.NAMED_COMPOUND_TAG);
    public final MetaType particleType;

    public MetaTypes1_13(final ParticleType particleType) {
        super(16);
        this.particleType = add(15, particleType);
    }
}

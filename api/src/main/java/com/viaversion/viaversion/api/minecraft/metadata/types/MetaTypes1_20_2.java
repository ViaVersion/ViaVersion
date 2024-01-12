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
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.misc.ParticleType;

public final class MetaTypes1_20_2 extends AbstractMetaTypes {

    public final MetaType byteType = add(0, Type.BYTE);
    public final MetaType varIntType = add(1, Type.VAR_INT);
    public final MetaType longType = add(2, Type.VAR_LONG);
    public final MetaType floatType = add(3, Type.FLOAT);
    public final MetaType stringType = add(4, Type.STRING);
    public final MetaType componentType = add(5, Type.COMPONENT);
    public final MetaType optionalComponentType = add(6, Type.OPTIONAL_COMPONENT);
    public final MetaType itemType = add(7, Type.ITEM1_20_2);
    public final MetaType booleanType = add(8, Type.BOOLEAN);
    public final MetaType rotationType = add(9, Type.ROTATION);
    public final MetaType positionType = add(10, Type.POSITION1_14);
    public final MetaType optionalPositionType = add(11, Type.OPTIONAL_POSITION_1_14);
    public final MetaType directionType = add(12, Type.VAR_INT);
    public final MetaType optionalUUIDType = add(13, Type.OPTIONAL_UUID);
    public final MetaType blockStateType = add(14, Type.VAR_INT);
    public final MetaType optionalBlockStateType = add(15, Type.VAR_INT);
    public final MetaType nbtType = add(16, Type.COMPOUND_TAG);
    public final MetaType particleType;
    public final MetaType villagerDatatType = add(18, Type.VILLAGER_DATA);
    public final MetaType optionalVarIntType = add(19, Type.OPTIONAL_VAR_INT);
    public final MetaType poseType = add(20, Type.VAR_INT);
    public final MetaType catVariantType = add(21, Type.VAR_INT);
    public final MetaType frogVariantType = add(22, Type.VAR_INT);
    public final MetaType optionalGlobalPosition = add(23, Type.OPTIONAL_GLOBAL_POSITION);
    public final MetaType paintingVariantType = add(24, Type.VAR_INT);
    public final MetaType snifferState = add(25, Type.VAR_INT);
    public final MetaType vectorType = add(26, Type.VECTOR3F);
    public final MetaType quaternionType = add(27, Type.QUATERNION);

    public MetaTypes1_20_2(final ParticleType particleType) {
        super(28);
        this.particleType = add(17, particleType);
    }
}

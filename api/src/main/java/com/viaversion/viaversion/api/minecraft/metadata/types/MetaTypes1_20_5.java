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

import com.viaversion.viaversion.api.minecraft.Particle;
import com.viaversion.viaversion.api.minecraft.metadata.MetaType;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.ArrayType;
import com.viaversion.viaversion.api.type.types.misc.ParticleType;
import com.viaversion.viaversion.api.type.types.version.Types1_20_5;

public final class MetaTypes1_20_5 extends AbstractMetaTypes {

    public final MetaType byteType = add(0, Types.BYTE);
    public final MetaType varIntType = add(1, Types.VAR_INT);
    public final MetaType longType = add(2, Types.VAR_LONG);
    public final MetaType floatType = add(3, Types.FLOAT);
    public final MetaType stringType = add(4, Types.STRING);
    public final MetaType componentType = add(5, Types.TAG);
    public final MetaType optionalComponentType = add(6, Types.OPTIONAL_TAG);
    public final MetaType itemType = add(7, Types1_20_5.ITEM);
    public final MetaType booleanType = add(8, Types.BOOLEAN);
    public final MetaType rotationsType = add(9, Types.ROTATIONS);
    public final MetaType blockPositionType = add(10, Types.BLOCK_POSITION1_14);
    public final MetaType optionalBlockPositionType = add(11, Types.OPTIONAL_POSITION_1_14);
    public final MetaType directionType = add(12, Types.VAR_INT);
    public final MetaType optionalUUIDType = add(13, Types.OPTIONAL_UUID);
    public final MetaType blockStateType = add(14, Types.VAR_INT);
    public final MetaType optionalBlockStateType = add(15, Types.VAR_INT);
    public final MetaType compoundTagType = add(16, Types.COMPOUND_TAG);
    public final MetaType particleType;
    public final MetaType particlesType;
    public final MetaType villagerDatatType = add(19, Types.VILLAGER_DATA);
    public final MetaType optionalVarIntType = add(20, Types.OPTIONAL_VAR_INT);
    public final MetaType poseType = add(21, Types.VAR_INT);
    public final MetaType catVariantType = add(22, Types.VAR_INT);
    public final MetaType wolfVariantType = add(23, Types.VAR_INT);
    public final MetaType frogVariantType = add(24, Types.VAR_INT);
    public final MetaType optionalGlobalPosition = add(25, Types.OPTIONAL_GLOBAL_POSITION);
    public final MetaType paintingVariantType = add(26, Types.VAR_INT);
    public final MetaType snifferState = add(27, Types.VAR_INT);
    public final MetaType armadilloState = add(28, Types.VAR_INT);
    public final MetaType vector3FType = add(29, Types.VECTOR3F);
    public final MetaType quaternionType = add(30, Types.QUATERNION);

    public MetaTypes1_20_5(final ParticleType particleType, final ArrayType<Particle> particlesType) {
        super(31);
        this.particleType = add(17, particleType);
        this.particlesType = add(18, particlesType);
    }
}

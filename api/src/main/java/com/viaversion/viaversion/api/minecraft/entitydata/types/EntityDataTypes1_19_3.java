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
package com.viaversion.viaversion.api.minecraft.entitydata.types;

import com.viaversion.viaversion.api.minecraft.entitydata.EntityDataType;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.misc.ParticleType;

public final class EntityDataTypes1_19_3 extends AbstractEntityDataTypes {

    public final EntityDataType byteType = add(0, Types.BYTE);
    public final EntityDataType varIntType = add(1, Types.VAR_INT);
    public final EntityDataType longType = add(2, Types.VAR_LONG);
    public final EntityDataType floatType = add(3, Types.FLOAT);
    public final EntityDataType stringType = add(4, Types.STRING);
    public final EntityDataType componentType = add(5, Types.COMPONENT);
    public final EntityDataType optionalComponentType = add(6, Types.OPTIONAL_COMPONENT);
    public final EntityDataType itemType = add(7, Types.ITEM1_13_2);
    public final EntityDataType booleanType = add(8, Types.BOOLEAN);
    public final EntityDataType rotationsType = add(9, Types.ROTATIONS);
    public final EntityDataType blockPositionType = add(10, Types.BLOCK_POSITION1_14);
    public final EntityDataType optionalBlockPositionType = add(11, Types.OPTIONAL_POSITION_1_14);
    public final EntityDataType directionType = add(12, Types.VAR_INT);
    public final EntityDataType optionalUUIDType = add(13, Types.OPTIONAL_UUID);
    public final EntityDataType optionalBlockStateType = add(14, Types.VAR_INT);
    public final EntityDataType compoundTagType = add(15, Types.NAMED_COMPOUND_TAG);
    public final EntityDataType particleType;
    public final EntityDataType villagerDatatType = add(17, Types.VILLAGER_DATA);
    public final EntityDataType optionalVarIntType = add(18, Types.OPTIONAL_VAR_INT);
    public final EntityDataType poseType = add(19, Types.VAR_INT);
    public final EntityDataType catVariantType = add(20, Types.VAR_INT);
    public final EntityDataType frogVariantType = add(21, Types.VAR_INT);
    public final EntityDataType optionalGlobalPosition = add(22, Types.OPTIONAL_GLOBAL_POSITION);
    public final EntityDataType paintingVariantType = add(23, Types.VAR_INT);

    public EntityDataTypes1_19_3(final ParticleType particleType) {
        super(24);
        this.particleType = add(16, particleType);
    }
}

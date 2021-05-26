/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2021 ViaVersion and contributors
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
import com.viaversion.viaversion.api.type.types.version.Types1_17;

public enum MetaType1_17 implements MetaType {
    BYTE(0, Type.BYTE),
    VAR_INT(1, Type.VAR_INT),
    FLOAT(2, Type.FLOAT),
    STRING(3, Type.STRING),
    COMPONENT(4, Type.COMPONENT),
    OPT_COMPONENT(5, Type.OPTIONAL_COMPONENT),
    ITEM(6, Type.FLAT_VAR_INT_ITEM),
    BOOLEAN(7, Type.BOOLEAN),
    ROTATION(8, Type.ROTATION),
    POSITION(9, Type.POSITION1_14),
    OPT_POSITION(10, Type.OPTIONAL_POSITION_1_14),
    DIRECTION(11, Type.VAR_INT),
    OPT_UUID(12, Type.OPTIONAL_UUID),
    BLOCK_STATE(13, Type.VAR_INT),
    NBT(14, Type.NBT),
    PARTICLE(15, Types1_17.PARTICLE),
    VILLAGER_DATA(16, Type.VILLAGER_DATA),
    OPT_VAR_INT(17, Type.OPTIONAL_VAR_INT),
    POSE(18, Type.VAR_INT);

    private static final MetaType1_17[] VALUES = values();
    private final int typeId;
    private final Type type;

    MetaType1_17(int typeId, Type type) {
        this.typeId = typeId;
        this.type = type;
    }

    public static MetaType1_17 byId(int id) {
        return VALUES[id];
    }

    @Override
    public int typeId() {
        return typeId;
    }

    @Override
    public Type type() {
        return type;
    }
}

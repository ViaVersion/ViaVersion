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
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;

public enum EntityDataTypes1_9 implements EntityDataType {
    BYTE(Types.BYTE),
    VAR_INT(Types.VAR_INT),
    FLOAT(Types.FLOAT),
    STRING(Types.STRING),
    COMPONENT(Types.COMPONENT),
    ITEM(Types.ITEM1_8),
    BOOLEAN(Types.BOOLEAN),
    ROTATIONS(Types.ROTATIONS),
    BLOCK_POSITION(Types.BLOCK_POSITION1_8),
    OPTIONAL_BLOCK_POSITION(Types.OPTIONAL_POSITION1_8),
    DIRECTION(Types.VAR_INT),
    OPTIONAL_UUID(Types.OPTIONAL_UUID),
    OPTIONAL_BLOCK_STATE(Types.VAR_INT);

    private final Type<?> type;

    EntityDataTypes1_9(Type<?> type) {
        this.type = type;
    }

    public static EntityDataTypes1_9 byId(int id) {
        return values()[id];
    }

    @Override
    public int typeId() {
        return ordinal();
    }

    @Override
    public Type type() {
        return type;
    }
}

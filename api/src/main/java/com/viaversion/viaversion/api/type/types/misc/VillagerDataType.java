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
package com.viaversion.viaversion.api.type.types.misc;

import com.viaversion.viaversion.api.minecraft.VillagerData;
import com.viaversion.viaversion.api.type.Type;
import io.netty.buffer.ByteBuf;

public class VillagerDataType extends Type<VillagerData> {
    public VillagerDataType() {
        super(VillagerData.class);
    }

    @Override
    public VillagerData read(ByteBuf buffer) throws Exception {
        return new VillagerData(Type.VAR_INT.readPrimitive(buffer), Type.VAR_INT.readPrimitive(buffer), Type.VAR_INT.readPrimitive(buffer));
    }

    @Override
    public void write(ByteBuf buffer, VillagerData object) throws Exception {
        Type.VAR_INT.writePrimitive(buffer, object.type());
        Type.VAR_INT.writePrimitive(buffer, object.profession());
        Type.VAR_INT.writePrimitive(buffer, object.level());
    }
}

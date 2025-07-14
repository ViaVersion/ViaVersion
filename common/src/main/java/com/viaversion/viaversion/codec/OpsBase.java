/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2025 ViaVersion and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.viaversion.viaversion.codec;

import com.viaversion.viaversion.api.minecraft.codec.CodecContext;
import com.viaversion.viaversion.api.minecraft.codec.Ops;

public abstract class OpsBase implements Ops {

    private final CodecContext context;

    protected OpsBase(final CodecContext context) {
        this.context = context;
    }

    @Override
    public CodecContext context() {
        return context;
    }
}

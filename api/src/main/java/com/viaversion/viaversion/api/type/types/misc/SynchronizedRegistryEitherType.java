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
package com.viaversion.viaversion.api.type.types.misc;

import com.viaversion.viaversion.api.minecraft.RegistryKey;
import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.EitherType;
import com.viaversion.viaversion.util.Either;
import com.viaversion.viaversion.util.Key;

// ???
public class SynchronizedRegistryEitherType extends EitherType<Integer, String> {

    private final RegistryKey registryKey;

    public SynchronizedRegistryEitherType(final RegistryKey registryKey) {
        super(Types.VAR_INT, Types.STRING);
        this.registryKey = registryKey;
    }

    @Override
    public void write(final Ops ops, final Either<Integer, String> value) {
        if (value.isLeft()) {
            final Key key = ops.context().registryAccess().registryKey(this.registryKey.key().toString(), value.left());
            Types.RESOURCE_LOCATION.write(ops, key);
        } else {
            Types.RESOURCE_LOCATION.write(ops, Key.of(value.right()));
        }
    }
}

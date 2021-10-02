/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2021 ViaVersion and contributors
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
package com.viaversion.viaversion.compatibility.unsafe;

import com.viaversion.viaversion.compatibility.ForcefulFieldModifier;

import java.lang.reflect.Field;
import java.util.Objects;

@SuppressWarnings({
        "java:S1191", // SonarLint/-Qube/-Cloud: We need Unsafe for the modifier implementation.
        "java:S3011", // ^: We need to circumvent the access restrictions of fields.
})
public final class UnsafeBackedForcefulFieldModifier implements ForcefulFieldModifier {
    private final sun.misc.Unsafe unsafe;

    public UnsafeBackedForcefulFieldModifier() throws ReflectiveOperationException {
        final Field theUnsafeField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        theUnsafeField.setAccessible(true);
        this.unsafe = (sun.misc.Unsafe) theUnsafeField.get(null);
    }

    @Override
    public void setField(final Field field, final Object holder, final Object object) {
        Objects.requireNonNull(field, "field must not be null");

        final Object ufo = holder != null ? holder : this.unsafe.staticFieldBase(field);
        final long offset = holder != null ? this.unsafe.objectFieldOffset(field) : this.unsafe.staticFieldOffset(field);

        this.unsafe.putObject(ufo, offset, object);
    }
}

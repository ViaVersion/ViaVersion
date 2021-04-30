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
package com.viaversion.viaversion.compatibility;

import java.lang.reflect.Field;

/**
 * Exposes a way to modify a {@link Field}, regardless of its limitations (given it is accessible by the caller).
 * <p>
 * <i>Note:</i> This is <b>explicitly</b> an implementation detail. Do not rely on this within plugins and any
 * non-ViaVersion code.
 * </p>
 */
public interface ForcefulFieldModifier {
    /**
     * Sets the field regardless of field finality.
     * <p>
     * <i>Note:</i> This does not set the accessibility of the field.
     * </p>
     *
     * @param field the field to set the modifiers of. Will throw if {@code null}.
     * @param holder the eye of the beholder. For static fields, use {@code null}.
     * @param object the new value to set of the object.
     */
    void setField(final Field field, final Object holder, final Object object)
        throws ReflectiveOperationException;
}

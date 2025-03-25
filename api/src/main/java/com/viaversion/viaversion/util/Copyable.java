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
package com.viaversion.viaversion.util;

import com.viaversion.nbt.tag.Tag;
import java.lang.reflect.Array;

public interface Copyable {

    default <T> T copy(final T object) {
        if (object == null) {
            return null;
        } else if (object instanceof Tag tag) {
            return (T) tag.copy();
        } else if (object instanceof Copyable copyable) {
            return (T) copyable.copy();
        } else if (object.getClass().isArray()) {
            final Object[] array = (Object[]) object;
            final Object[] copy = (Object[]) Array.newInstance(array.getClass().getComponentType(), array.length);
            for (int i = 0; i < array.length; i++) {
                copy[i] = copy(array[i]);
            }
            return (T) copy;
        } else {
            return object;
        }
    }

    Object copy();

}

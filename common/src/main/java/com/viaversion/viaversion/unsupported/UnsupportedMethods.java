/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2024 ViaVersion and contributors
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
package com.viaversion.viaversion.unsupported;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Set;

public final class UnsupportedMethods {

    private final String className;
    private final Set<String> methodNames;

    public UnsupportedMethods(String className, Set<String> methodNames) {
        this.className = className;
        this.methodNames = Collections.unmodifiableSet(methodNames);
    }

    public String getClassName() {
        return className;
    }

    public final boolean findMatch() {
        try {
            for (Method method : Class.forName(className).getDeclaredMethods()) {
                if (methodNames.contains(method.getName())) {
                    return true;
                }
            }
        } catch (ClassNotFoundException ignored) {
        }
        return false;
    }
}

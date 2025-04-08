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
package com.viaversion.viaversion.util;

import java.util.regex.Pattern;

public final class Key {

    private static final Pattern PATTERN = Pattern.compile("([0-9a-z_.-]*:)?[0-9a-z_/.-]*");
    private static final int MINECRAFT_NAMESPACE_LENGTH = "minecraft:".length();

    public static String stripNamespace(final String identifier) {
        int index = identifier.indexOf(':');
        if (index == -1) {
            return identifier;
        }
        return identifier.substring(index + 1);
    }

    public static String namespace(final String identifier) {
        final int index = identifier.indexOf(':');
        if (index == -1) {
            return "minecraft";
        } else if (index == 0) {
            return "minecraft";
        }
        return identifier.substring(0, index);
    }

    public static String stripMinecraftNamespace(final String identifier) {
        if (identifier.startsWith("minecraft:")) {
            return identifier.substring(MINECRAFT_NAMESPACE_LENGTH);
        } else if (!identifier.isEmpty() && identifier.charAt(0) == ':') {
            return identifier.substring(1);
        }
        return identifier;
    }

    public static boolean equals(final String firstIdentifier, final String secondIdentifier) {
        return stripMinecraftNamespace(firstIdentifier).equals(stripMinecraftNamespace(secondIdentifier));
    }

    public static String namespaced(final String identifier) {
        final int index = identifier.indexOf(':');
        if (index == -1) {
            return "minecraft:" + identifier;
        } else if (index == 0) {
            return "minecraft" + identifier;
        }
        return identifier;
    }

    public static boolean isValid(final String identifier) {
        return PATTERN.matcher(identifier).matches();
    }
}

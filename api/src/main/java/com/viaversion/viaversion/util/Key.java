/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2026 ViaVersion and contributors
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

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Represents a Minecraft Identifier/ResourceLocation.
 * <p>
 * Internally split into {@link CustomKey} and {@link MinecraftKey} to reduce data held
 * and keep the runtime/allocation cost of {@link #original()} and {@link #toString()} minimal.
 */
public abstract sealed class Key permits MinecraftKey, CustomKey {

    static final String MINECRAFT_NAMESPACE = "minecraft";

    /**
     * Creates a new key with the given namespace and path.
     *
     * @param namespace the namespace of the identifier
     * @param path      the path of the identifier
     * @return a new key with the given namespace and path
     */
    public static Key of(final String namespace, final String path) {
        if (namespace.equals(MINECRAFT_NAMESPACE) || namespace.isEmpty()) {
            return ofPath(path); // ends up compacting it
        }
        return new CustomKey(namespace + ':' + path, namespace, path);
    }

    /**
     * Creates a new key with the given path and the default namespace "minecraft".
     *
     * @param path the path of the identifier
     * @return a new key with the given path and the default namespace
     */
    public static Key ofPath(final String path) {
        return new MinecraftKey.CompactMinecraftKey(path);
    }

    /**
     * Creates a new key from the given identifier string, with or without an explicit namespace.
     *
     * @param identifier the identifier string
     * @return a new key with the given identifier
     */
    public static Key of(final String identifier) {
        final int separatorIndex = identifier.indexOf(':');
        if (separatorIndex == -1) {
            return ofPath(identifier);
        }

        final String path = identifier.substring(separatorIndex + 1);
        if (separatorIndex == 0) {
            return new MinecraftKey.ColonPrefixedMinecraftKey(identifier, path);
        }

        final String namespace = identifier.substring(0, separatorIndex);
        if (namespace.equals(MINECRAFT_NAMESPACE)) {
            return new MinecraftKey.FullMinecraftKey(identifier, path);
        }

        return new CustomKey(identifier, namespace, path);
    }

    /**
     * Tries to create a new key from the given identifier string, with or without an explicit namespace.
     *
     * @param identifier the identifier string
     * @return a new key with the given identifier, or null if the identifier is invalid
     */
    public static @Nullable Key tryParse(final String identifier) {
        try {
            return of(identifier);
        } catch (final IllegalArgumentException e) {
            return null;
        }
    }

    public abstract String namespace();

    public abstract String path();

    /**
     * Returns the unmodified original identifier, possbily without an explicit namespace.
     *
     * @return the original identifier, possibly without an explicit namespace
     */
    public abstract String original();

    /**
     * Returns the identifier in a minimized form. If the namespace is "minecraft", it will return just the path.
     *
     * @return the identifier in a minimized form
     */
    public abstract String minimized();

    public abstract boolean hasMinecraftNamespace();

    public Key withNamespace(final String namespace) {
        return of(namespace, this.path());
    }

    public Key withPath(final String path) {
        return of(this.namespace(), path);
    }

    public final boolean equals(final String identifier) {
        return this.equals(Key.of(identifier));
    }

    public static String stripNamespace(final String identifier) {
        int index = identifier.indexOf(':');
        if (index == -1) {
            return identifier;
        }
        return identifier.substring(index + 1);
    }

    public static String namespace(final String identifier) {
        final int index = identifier.indexOf(':');
        if (index == -1 || index == 0) {
            return MINECRAFT_NAMESPACE;
        }
        return identifier.substring(0, index);
    }

    public static String stripMinecraftNamespace(final String identifier) {
        if (identifier.startsWith("minecraft:")) {
            return identifier.substring(MinecraftKey.MINECRAFT_NAMESPACE_LENGTH + 1);
        } else if (!identifier.isEmpty() && identifier.charAt(0) == ':') {
            return identifier.substring(1);
        }
        return identifier;
    }

    public static boolean equals(final String firstIdentifier, final String secondIdentifier) {
        return firstIdentifier != null && secondIdentifier != null && of(firstIdentifier).equals(of(secondIdentifier));
    }

    public static String namespaced(final String identifier) {
        final int index = identifier.indexOf(':');
        if (index == -1) {
            return "minecraft:" + identifier;
        } else if (index == 0) {
            return MINECRAFT_NAMESPACE + identifier;
        }
        return identifier;
    }

    public static boolean isValid(final String identifier) {
        final int separatorIndex = identifier.indexOf(':');
        if (separatorIndex == -1) {
            return MinecraftKey.isValidPath(identifier);
        } else if (separatorIndex == 0) {
            return MinecraftKey.isValidPath(identifier.substring(1));
        }

        final String namespace = identifier.substring(0, separatorIndex);
        final String path = identifier.substring(separatorIndex + 1);
        return MinecraftKey.isValidNamespace(namespace) && MinecraftKey.isValidPath(path);
    }
}

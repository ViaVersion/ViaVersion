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

import org.checkerframework.checker.nullness.qual.Nullable;

public final class Key {

    private static final String MINECRAFT_NAMESPACE = "minecraft";
    private static final int MINECRAFT_NAMESPACE_LENGTH = MINECRAFT_NAMESPACE.length();
    private final String original;
    private final String namespace;
    private final String path;

    private Key(final String original, final String namespace, final String path) {
        if (!isValidNamespace(namespace)) {
            throw new IllegalArgumentException("Invalid namespace: " + namespace);
        }
        if (!isValidPath(path)) {
            throw new IllegalArgumentException("Invalid path: " + path);
        }
        this.original = original; // assume this is correct; also saves whether the namespace was explitly set
        this.namespace = namespace;
        this.path = path;
    }

    /**
     * Creates a new key with the given namespace and path.
     *
     * @param namespace the namespace of the identifier
     * @param path the path of the identifier
     * @return a new key with the given namespace and path
     */
    public static Key of(final String namespace, final String path) {
        return new Key(namespace + ':' + path, namespace, path);
    }

    /**
     * Creates a new key with the given path and the default namespace "minecraft".
     *
     * @param path the path of the identifier
     * @return a new key with the given path and the default namespace
     */
    public static Key ofPath(final String path) {
        return new Key(path, MINECRAFT_NAMESPACE, path);
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

        final String namespace = separatorIndex == 0 ? MINECRAFT_NAMESPACE : identifier.substring(0, separatorIndex);
        final String path = identifier.substring(separatorIndex + 1);
        return new Key(identifier, namespace, path);
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
            return MINECRAFT_NAMESPACE;
        } else if (index == 0) {
            return MINECRAFT_NAMESPACE;
        }
        return identifier.substring(0, index);
    }

    public static String stripMinecraftNamespace(final String identifier) {
        if (identifier.startsWith("minecraft:")) {
            return identifier.substring(MINECRAFT_NAMESPACE_LENGTH + 1);
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
            return isValidPath(identifier);
        } else if (separatorIndex == 0) {
            return isValidPath(identifier.substring(1));
        }

        final String namespace = identifier.substring(0, separatorIndex);
        final String path = identifier.substring(separatorIndex + 1);
        return isValidNamespace(namespace) && isValidPath(path);
    }

    public String namespace() {
        return this.namespace;
    }

    public String path() {
        return this.path;
    }

    /**
     * Returns the unmodified original identifier, possbily without an explicit namespace.
     *
     * @return the original identifier, possibly without an explicit namespace
     */
    public String original() {
        return this.original;
    }

    /**
     * Returns the identifier in a minimized form. If the namespace is "minecraft", it will return just the path.
     *
     * @return the identifier in a minimized form
     */
    public String minimized() {
        return this.hasMinecraftNamespace() ? this.path : this.toString();
    }

    public boolean hasMinecraftNamespace() {
        return this.namespace.equals(MINECRAFT_NAMESPACE);
    }

    public Key withNamespace(final String namespace) {
        return of(namespace, this.path);
    }

    public Key withPath(final String path) {
        return of(this.namespace, path);
    }

    @Override
    public final boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof final Key key)) return false;
        return this.namespace.equals(key.namespace) && this.path.equals(key.path);
    }

    public final boolean equals(final String identifier) {
        return this.equals(Key.of(identifier));
    }

    @Override
    public int hashCode() {
        int result = this.namespace.hashCode();
        result = 31 * result + this.path.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return this.namespace + ':' + this.path;
    }

    private static boolean isValidNamespace(final String namespace) {
        //noinspection StringEquality - a quick way out
        if (namespace == MINECRAFT_NAMESPACE) {
            return true;
        }
        for (int i = 0, length = namespace.length(); i < length; i++) {
            final char c = namespace.charAt(i);
            if (!(c >= 'a' && c <= 'z' || c >= '0' && c <= '9' || c == '_' || c == '-' || c == '.')) {
                return false;
            }
        }
        return true;
    }

    private static boolean isValidPath(final String path) {
        for (int i = 0, length = path.length(); i < length; i++) {
            final char c = path.charAt(i);
            if (!(c >= 'a' && c <= 'z' || c >= '0' && c <= '9' || c == '_' || c == '-' || c == '.' || c == '/')) {
                return false;
            }
        }
        return true;
    }
}

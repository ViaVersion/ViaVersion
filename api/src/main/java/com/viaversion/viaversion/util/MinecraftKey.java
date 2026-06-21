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


abstract sealed class MinecraftKey extends Key {

    static final int MINECRAFT_NAMESPACE_LENGTH = MINECRAFT_NAMESPACE.length();
    private final String path;

    /**
     * Minecraft key with a custom path.
     *
     * @param path path
     */
    protected MinecraftKey(final String path) {
        if (!isValidPath(path)) {
            throw new IllegalArgumentException("Invalid path: " + path);
        }
        this.path = path;
    }

    @Override
    public String namespace() {
        return MINECRAFT_NAMESPACE;
    }

    @Override
    public String path() {
        return path;
    }

    @Override
    public String minimized() {
        return this.path;
    }

    @Override
    public boolean hasMinecraftNamespace() {
        return true;
    }

    @Override
    public final boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof final MinecraftKey key)) return false;
        return this.path.equals(key.path);
    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }

    static boolean isValidNamespace(final String namespace) {
        for (int i = 0, length = namespace.length(); i < length; i++) {
            final char c = namespace.charAt(i);
            if (!(c >= 'a' && c <= 'z' || c >= '0' && c <= '9' || c == '_' || c == '-' || c == '.')) {
                return false;
            }
        }
        return true;
    }

    static boolean isValidPath(final String path) {
        for (int i = 0, length = path.length(); i < length; i++) {
            final char c = path.charAt(i);
            if (!(c >= 'a' && c <= 'z' || c >= '0' && c <= '9' || c == '_' || c == '-' || c == '.' || c == '/')) {
                return false;
            }
        }
        return true;
    }

    /**
     * Compact Minecraft key, e.g. {@code "dirt"}
     */
    static final class CompactMinecraftKey extends MinecraftKey {

        private String identifier;

        CompactMinecraftKey(final String path) {
            super(path);
        }

        @Override
        public String original() {
            return path();
        }

        @Override
        public String toString() {
            if (identifier == null) {
                identifier = MINECRAFT_NAMESPACE + ':' + this.path();
            }
            return identifier;
        }
    }

    /**
     * Colon-prefixed Minecraft keys, e.g. {@code ":dirt"}
     */
    static final class ColonPrefixedMinecraftKey extends MinecraftKey {

        private final String original;
        private String identifier;

        ColonPrefixedMinecraftKey(final String original, final String path) {
            super(path);
            this.original = original;
        }

        @Override
        public String original() {
            return original;
        }

        @Override
        public String toString() {
            if (identifier == null) {
                identifier = MINECRAFT_NAMESPACE + ':' + this.path();
            }
            return identifier;
        }
    }

    /**
     * Full Minecraft keys, e.g. {@code "minecraft:dirt"}
     */
    static final class FullMinecraftKey extends MinecraftKey {

        private final String identifier;

        FullMinecraftKey(final String identifier, final String path) {
            super(path);
            this.identifier = identifier;
        }

        @Override
        public String original() {
            return identifier;
        }

        @Override
        public String toString() {
            return identifier;
        }
    }
}

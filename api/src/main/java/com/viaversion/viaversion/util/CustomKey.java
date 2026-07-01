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

final class CustomKey extends Key {

    private final String original;
    private final String namespace;
    private final String path;

    /**
     * Non-Minecraft key.
     *
     * @param original  full key including the ':' separator
     * @param namespace non-Minecraft namespace
     * @param path      path
     */
    CustomKey(final String original, final String namespace, final String path) {
        if (!MinecraftKey.isValidNamespace(namespace)) {
            throw new IllegalArgumentException("Invalid namespace: " + namespace);
        }
        if (!MinecraftKey.isValidPath(path)) {
            throw new IllegalArgumentException("Invalid path: " + path);
        }
        this.original = original;
        this.namespace = namespace;
        this.path = path;
    }

    @Override
    public String namespace() {
        return namespace;
    }

    @Override
    public String path() {
        return path;
    }

    @Override
    public String original() {
        return original;
    }

    @Override
    public String minimized() {
        return this.original;
    }

    @Override
    public boolean hasMinecraftNamespace() {
        return false;
    }

    @Override
    public final boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof final CustomKey key)) return false;
        return this.original.equals(key.original);
    }

    @Override
    public int hashCode() {
        return this.original.hashCode();
    }

    @Override
    public String toString() {
        return this.original;
    }
}

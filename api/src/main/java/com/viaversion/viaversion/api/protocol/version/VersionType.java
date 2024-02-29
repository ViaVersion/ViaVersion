/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2024 ViaVersion and contributors
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
package com.viaversion.viaversion.api.protocol.version;

/**
 * Categories of Minecraft versions from classic to modern releases, ordered by date.
 */
public enum VersionType {

    /**
     * Classic versions of Minecraft (Classic 0.0.2a to 0.30).
     */
    CLASSIC,
    /**
     * Alpha versions of Minecraft (Alpha 1.0.0 to 1.0.17).
     */
    ALPHA_INITIAL,
    /**
     * Alpha versions of Minecraft (Alpha 1.1.0 to 1.2.6).
     */
    ALPHA_LATER,
    /**
     * Beta versions of Minecraft (Beta 1.0 to 1.1_02).
     */
    BETA_INITIAL,
    /**
     * Beta versions of Minecraft (Beta 1.2 to 1.9-pre6/1.0.0-RC2).
     */
    BETA_LATER,
    /**
     * Pre-netty release versions of Minecraft (1.0.0 to the 1.7.2 snapshot 13w39b).
     */
    RELEASE_INITIAL,
    /**
     * Modern release versions of Minecraft (13w41a to latest).
     */
    RELEASE,
    /**
     * Any version that doesn't fit in the above categories (e.g. April Fools).
     * <p>
     * Protocol versions using this type must override the compareTo method.
     * Protocol versions using this type must add base protocols to the pipeline manually.
     */
    SPECIAL
}
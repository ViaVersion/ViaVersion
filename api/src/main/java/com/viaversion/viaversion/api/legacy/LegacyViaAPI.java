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
package com.viaversion.viaversion.api.legacy;

import com.viaversion.viaversion.api.legacy.bossbar.BossBar;
import com.viaversion.viaversion.api.legacy.bossbar.BossColor;
import com.viaversion.viaversion.api.legacy.bossbar.BossStyle;

/**
 * API only applicable on or to legacy versions of Minecraft.
 *
 * @param <T> player type
 */
public interface LegacyViaAPI<T> {

    /**
     * Creates a new bossbar instance. This only works on pre 1.9 servers for 1.9+ clients.
     *
     * @param title  title
     * @param health health, between 0 and 1 (inclusive)
     * @param color  color
     * @param style  style
     * @return new bossbar instance
     */
    BossBar createLegacyBossBar(String title, float health, BossColor color, BossStyle style);

    /**
     * Creates a new bossbar instance with full health. This only works on pre 1.9 servers for 1.9+ clients.
     *
     * @param title title
     * @param color color
     * @param style style
     * @return new bossbar instance
     */
    default BossBar createLegacyBossBar(String title, BossColor color, BossStyle style) {
        return createLegacyBossBar(title, 1F, color, style);
    }
}

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
package us.myles.ViaVersion.api.boss;

import java.util.Set;
import java.util.UUID;

@Deprecated
public class BossBar<T> {

    private final com.viaversion.viaversion.api.legacy.bossbar.BossBar bossBar;

    public BossBar(com.viaversion.viaversion.api.legacy.bossbar.BossBar bossBar) {
        this.bossBar = bossBar;
    }

    public String getTitle() {
        return bossBar.getTitle();
    }

    public BossBar setTitle(String title) {
        bossBar.setTitle(title);
        return this;
    }

    public float getHealth() {
        return bossBar.getHealth();
    }

    public BossBar setHealth(float health) {
        bossBar.setHealth(health);
        return this;
    }

    public BossColor getColor() {
        return BossColor.values()[bossBar.getColor().ordinal()];
    }

    public BossBar setColor(BossColor color) {
        bossBar.setColor(com.viaversion.viaversion.api.legacy.bossbar.BossColor.values()[color.ordinal()]);
        return this;
    }

    public BossStyle getStyle() {
        return BossStyle.values()[bossBar.getStyle().ordinal()];
    }

    public BossBar setStyle(BossStyle style) {
        bossBar.setStyle(com.viaversion.viaversion.api.legacy.bossbar.BossStyle.values()[style.ordinal()]);
        return this;
    }

    @Deprecated
    public BossBar addPlayer(T player) {
        return this;
    }

    public BossBar addPlayer(UUID player) {
        bossBar.addPlayer(player);
        return this;
    }

    @Deprecated
    public BossBar addPlayers(T... players) {
        return this;
    }

    @Deprecated
    public BossBar removePlayer(T player) {
        return this;
    }

    public BossBar removePlayer(UUID uuid) {
        bossBar.removePlayer(uuid);
        return this;
    }

    public BossBar addFlag(BossFlag flag) {
        bossBar.addFlag(com.viaversion.viaversion.api.legacy.bossbar.BossFlag.values()[flag.ordinal()]);
        return this;
    }

    public BossBar removeFlag(BossFlag flag) {
        bossBar.removeFlag(com.viaversion.viaversion.api.legacy.bossbar.BossFlag.values()[flag.ordinal()]);
        return this;
    }

    public boolean hasFlag(BossFlag flag) {
        return bossBar.hasFlag(com.viaversion.viaversion.api.legacy.bossbar.BossFlag.values()[flag.ordinal()]);
    }

    public Set<UUID> getPlayers() {
        return bossBar.getPlayers();
    }

    public BossBar show() {
        bossBar.show();
        return this;
    }

    public BossBar hide() {
        bossBar.hide();
        return this;
    }

    public boolean isVisible() {
        return bossBar.isVisible();
    }

    public UUID getId() {
        return bossBar.getId();
    }
}

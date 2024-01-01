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
package us.myles.ViaVersion.api;

import io.netty.buffer.ByteBuf;
import java.util.SortedSet;
import java.util.UUID;
import us.myles.ViaVersion.api.boss.BossBar;
import us.myles.ViaVersion.api.boss.BossColor;
import us.myles.ViaVersion.api.boss.BossStyle;

/**
 * @deprecated may be removed at some point, use {@link com.viaversion.viaversion.api.Via}
 */
@Deprecated
public class Via<T> implements ViaAPI<T> {

    private static final ViaAPI INSTANCE = new Via();

    private Via() {
    }

    @Deprecated
    public static ViaAPI getAPI() {
        return INSTANCE;
    }

    @Override
    public int getPlayerVersion(T player) {
        return com.viaversion.viaversion.api.Via.getAPI().getPlayerVersion(player);
    }

    @Override
    public int getPlayerVersion(UUID uuid) {
        return com.viaversion.viaversion.api.Via.getAPI().getPlayerVersion(uuid);
    }

    @Override
    public boolean isInjected(UUID playerUUID) {
        return com.viaversion.viaversion.api.Via.getAPI().isInjected(playerUUID);
    }

    @Override
    public String getVersion() {
        return com.viaversion.viaversion.api.Via.getAPI().getVersion();
    }

    @Override
    public void sendRawPacket(T player, ByteBuf packet) {
        com.viaversion.viaversion.api.Via.getAPI().sendRawPacket(player, packet);
    }

    @Override
    public void sendRawPacket(UUID uuid, ByteBuf packet) {
        com.viaversion.viaversion.api.Via.getAPI().sendRawPacket(uuid, packet);
    }

    @Override
    public BossBar createBossBar(String title, BossColor color, BossStyle style) {
        return new BossBar(com.viaversion.viaversion.api.Via.getAPI().legacyAPI().createLegacyBossBar(title,
                com.viaversion.viaversion.api.legacy.bossbar.BossColor.values()[color.ordinal()],
                com.viaversion.viaversion.api.legacy.bossbar.BossStyle.values()[style.ordinal()]));
    }

    @Override
    public BossBar createBossBar(String title, float health, BossColor color, BossStyle style) {
        return new BossBar(com.viaversion.viaversion.api.Via.getAPI().legacyAPI().createLegacyBossBar(title, health,
                com.viaversion.viaversion.api.legacy.bossbar.BossColor.values()[color.ordinal()],
                com.viaversion.viaversion.api.legacy.bossbar.BossStyle.values()[style.ordinal()]));
    }

    @Override
    public SortedSet<Integer> getSupportedVersions() {
        return com.viaversion.viaversion.api.Via.getAPI().getSupportedVersions();
    }

    @Override
    public SortedSet<Integer> getFullSupportedVersions() {
        return com.viaversion.viaversion.api.Via.getAPI().getFullSupportedVersions();
    }
}

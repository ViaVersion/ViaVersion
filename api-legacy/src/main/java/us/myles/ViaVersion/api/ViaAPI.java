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

import com.viaversion.viaversion.api.Via;
import io.netty.buffer.ByteBuf;
import java.util.SortedSet;
import java.util.UUID;
import us.myles.ViaVersion.api.boss.BossBar;
import us.myles.ViaVersion.api.boss.BossColor;
import us.myles.ViaVersion.api.boss.BossStyle;

/**
 * @deprecated may be removed at some point, use {@link Via#getAPI()}
 */
@Deprecated
public interface ViaAPI<T> {

    int getPlayerVersion(T player);

    int getPlayerVersion(UUID uuid);

    default boolean isPorted(UUID playerUUID) {
        return isInjected(playerUUID);
    }

    boolean isInjected(UUID playerUUID);

    String getVersion();

    void sendRawPacket(T player, ByteBuf packet);

    void sendRawPacket(UUID uuid, ByteBuf packet);

    BossBar createBossBar(String title, BossColor color, BossStyle style);

    BossBar createBossBar(String title, float health, BossColor color, BossStyle style);

    SortedSet<Integer> getSupportedVersions();

    SortedSet<Integer> getFullSupportedVersions();
}

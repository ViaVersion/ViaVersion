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
package com.viaversion.viaversion.api;

import com.google.common.base.Preconditions;
import com.viaversion.viaversion.api.configuration.ViaVersionConfig;
import com.viaversion.viaversion.api.platform.ViaPlatform;
import com.viaversion.viaversion.api.platform.ViaServerProxyPlatform;

public final class Via {
    private static ViaManager manager;

    /**
     * Returns the API associated with the current platform.
     *
     * @return API instance
     * @throws IllegalArgumentException if the platform has not loaded yet
     */
    public static ViaAPI getAPI() {
        return manager().getPlatform().getApi();
    }

    /**
     * Returns the ViaManager with methods beyond the simple API {@link ViaAPI} provides.
     *
     * @return manager to interact with various Via parts
     * @throws IllegalArgumentException if the platform has not loaded yet
     */
    public static ViaManager getManager() {
        return manager();
    }

    /**
     * Returns the config associated with the current platform.
     *
     * @return config instance
     * @throws IllegalArgumentException if the platform has not loaded yet
     */
    public static ViaVersionConfig getConfig() {
        return manager().getPlatform().getConf();
    }

    public static ViaPlatform getPlatform() {
        return manager().getPlatform();
    }

    public static ViaServerProxyPlatform<?> proxyPlatform() {
        Preconditions.checkArgument(manager().getPlatform() instanceof ViaServerProxyPlatform, "Platform is not proxying Minecraft servers!");
        return (ViaServerProxyPlatform<?>) manager().getPlatform();
    }

    /**
     * Register the ViaManager associated with the platform.
     *
     * @param viaManager The ViaManager
     * @throws IllegalArgumentException if the manager has already been set
     */
    public static void init(ViaManager viaManager) {
        Preconditions.checkArgument(manager == null, "ViaManager is already set");
        Via.manager = viaManager;
    }

    private static ViaManager manager() {
        Preconditions.checkArgument(manager != null, "ViaVersion has not loaded the platform yet");
        return manager;
    }

    public static boolean isLoaded() {
        return manager != null;
    }
}

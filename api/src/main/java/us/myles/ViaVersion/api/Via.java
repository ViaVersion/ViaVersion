/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2021 ViaVersion and contributors
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

import com.google.common.base.Preconditions;
import us.myles.ViaVersion.ViaManager;
import us.myles.ViaVersion.api.platform.ViaPlatform;

public class Via {
    private static ViaManager manager;

    /**
     * Register the ViaManager associated with the platform.
     *
     * @param viaManager The ViaManager
     */
    public static void init(ViaManager viaManager) {
        Preconditions.checkArgument(manager == null, "ViaManager is already set");
        Via.manager = viaManager;
    }

    /**
     * Returns the API associated with the current platform.
     *
     * @return API instance
     */
    public static ViaAPI getAPI() {
        Preconditions.checkArgument(manager != null, "ViaVersion has not loaded the platform yet");
        return manager.getPlatform().getApi();
    }

    /**
     * Get the config associated with the current platform.
     *
     * @return Config instance
     */
    public static ViaVersionConfig getConfig() {
        Preconditions.checkArgument(manager != null, "ViaVersion has not loaded the platform yet");
        return manager.getPlatform().getConf();
    }

    public static ViaPlatform getPlatform() {
        return manager.getPlatform();
    }

    public static ViaManager getManager() {
        return manager;
    }
}

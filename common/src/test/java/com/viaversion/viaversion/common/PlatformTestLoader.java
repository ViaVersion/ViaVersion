/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2025 ViaVersion and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.viaversion.viaversion.common;

import com.viaversion.viaversion.ViaManagerImpl;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.platform.ViaPlatformLoader;
import com.viaversion.viaversion.commands.ViaCommandHandler;
import com.viaversion.viaversion.common.dummy.TestPlatform;
import com.viaversion.viaversion.platform.NoopInjector;
import java.util.concurrent.CompletionException;

final class PlatformTestLoader {

    private static boolean loaded;
    private static Throwable failure;

    static synchronized void ensureLoaded() throws InterruptedException {
        if (loaded) {
            return;
        }
        if (failure != null) {
            throw new AssertionError("Platform bootstrap previously failed", failure);
        }

        try {
            if (!Via.isLoaded()) {
                ViaManagerImpl.initAndLoad(new TestPlatform(), new NoopInjector(), new ViaCommandHandler(false), ViaPlatformLoader.NOOP);
            }

            while (!Via.getManager().getProtocolManager().checkForMappingCompletion(true)) {
                Thread.sleep(100);
            }

            loaded = true;
        } catch (final CompletionException e) {
            failure = e.getCause();
            throw new AssertionError("Async mapping loading failed", e.getCause());
        } catch (final InterruptedException e) {
            failure = e;
            Thread.currentThread().interrupt();
            throw e;
        } catch (final Throwable t) {
            failure = t;
            throw t;
        }
    }
}

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
package com.viaversion.viaversion.api.platform.providers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.Nullable;

public class ViaProviders {
    private final Map<Class<? extends Provider>, Provider> providers = new HashMap<>();
    private final List<Class<? extends Provider>> lonelyProviders = new ArrayList<>();

    public void require(Class<? extends Provider> provider) {
        lonelyProviders.add(provider);
    }

    public <T extends Provider> void register(Class<T> provider, T value) {
        providers.put(provider, value);
    }

    public <T extends Provider> void use(Class<T> provider, T value) {
        lonelyProviders.remove(provider);
        providers.put(provider, value);
    }

    public @Nullable <T extends Provider> T get(Class<T> provider) {
        Provider rawProvider = providers.get(provider);
        if (rawProvider != null) {
            return (T) rawProvider;
        } else {
            if (lonelyProviders.contains(provider)) {
                throw new IllegalStateException("There was no provider for " + provider + ", one is required!");
            }
            return null;
        }
    }
}

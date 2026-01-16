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
package com.viaversion.viaversion.api.platform;

import com.google.gson.JsonObject;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import java.util.SortedSet;

public interface ViaInjector {

    /**
     * Inject into the current Platform. May be empty on platforms not having to inject their pipeline
     * or using alternative ways such as Mixin.
     *
     * @throws Exception if there is an error with injecting
     */
    void inject() throws Exception;

    /**
     * Uninject into the current Platform. May be empty on platforms not having to inject their pipeline
     * or using alternative ways such as Mixin.
     *
     * @throws Exception if there is an error with uninjecting
     */
    void uninject() throws Exception;

    /**
     * Returns true if the protocol version cannot be used in the early init.
     * Namely, this returns true for forks of Vanilla without extra API to get the protocol version.
     *
     * @return true if the protocol version cannot be used in the early init
     */
    default boolean lateProtocolVersionSetting() {
        return false;
    }

    /**
     * Returns the server protocol version.
     * For proxies, this returns the lowest supported protocol version.
     *
     * @return server protocol version
     * @throws Exception if there is an error with getting this info, e.g. not bound
     * @see ViaPlatform#isProxy()
     */
    ProtocolVersion getServerProtocolVersion() throws Exception;

    /**
     * Returns the supported server protocol versions.
     *
     * @return server protocol versions
     * @throws Exception if there is an error with getting this info, e.g. not bound
     * @see ViaPlatform#isProxy()
     */
    default SortedSet<ProtocolVersion> getServerProtocolVersions() throws Exception {
        final SortedSet<ProtocolVersion> versions = new ObjectLinkedOpenHashSet<>();
        versions.add(getServerProtocolVersion());
        return versions;
    }

    /**
     * Get the name of the encoder for then netty pipeline for this platform.
     *
     * @return The name
     */
    default String getEncoderName() {
        return "via-encoder";
    }

    /**
     * Get the name of the decoder for then netty pipeline for this platform.
     *
     * @return The name
     */
    default String getDecoderName() {
        return "via-decoder";
    }

    /**
     * Get any relevant data for debugging injection issues.
     *
     * @return JSONObject containing the data
     */
    JsonObject getDump();
}

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

import it.unimi.dsi.fastutil.ints.IntLinkedOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import java.util.SortedSet;

public interface ServerProtocolVersion {

    /**
     * Returns the lowest supported protocol version by this server.
     * This and {@link #highestSupportedProtocolVersion()} should only differ on proxy servers supporting multiple versions.
     *
     * @return lowest supported protocol version
     */
    ProtocolVersion lowestSupportedProtocolVersion();

    /**
     * Returns the highest supported protocol version by this server.
     * This and {@link #lowestSupportedProtocolVersion()} should only differ on proxy servers supporting multiple versions.
     *
     * @return highest supported protocol version
     */
    ProtocolVersion highestSupportedProtocolVersion();

    /**
     * Returns a sorted set of all supported protocol version by this server.
     * For non-proxy servers, this should return a singleton set.
     *
     * @return sorted set of supported protocol versions
     */
    SortedSet<ProtocolVersion> supportedProtocolVersions();

    /**
     * Returns true if the actual protocol version has not yet been identified.
     * In that case, all methods above will return -1.
     *
     * @return true if set, false if unknown (yet)
     */
    default boolean isKnown() {
        return lowestSupportedProtocolVersion().isKnown() && highestSupportedProtocolVersion().isKnown();
    }

    @Deprecated
    default int lowestSupportedVersion() {
        return lowestSupportedProtocolVersion().getVersion();
    }

    @Deprecated
    default int highestSupportedVersion() {
        return highestSupportedProtocolVersion().getVersion();
    }

    @Deprecated
    default IntSortedSet supportedVersions() {
        return supportedProtocolVersions().stream().mapToInt(ProtocolVersion::getVersion)
            .collect(IntLinkedOpenHashSet::new, IntSortedSet::add, IntSortedSet::addAll);
    }
}

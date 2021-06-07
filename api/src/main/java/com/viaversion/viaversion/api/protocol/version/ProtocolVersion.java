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
package com.viaversion.viaversion.api.protocol.version;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ProtocolVersion {
    private static final Int2ObjectMap<ProtocolVersion> VERSIONS = new Int2ObjectOpenHashMap<>();
    private static final List<ProtocolVersion> VERSION_LIST = new ArrayList<>();

    // Before the Netty rewrite
    public static final ProtocolVersion v1_4_6 = register(51, "1.4.6/7", new VersionRange("1.4", 6, 7));
    public static final ProtocolVersion v1_5_1 = register(60, "1.5.1");
    public static final ProtocolVersion v1_5_2 = register(61, "1.5.2");
    public static final ProtocolVersion v_1_6_1 = register(73, "1.6.1");
    public static final ProtocolVersion v_1_6_2 = register(74, "1.6.2");
    public static final ProtocolVersion v_1_6_3 = register(77, "1.6.3");
    public static final ProtocolVersion v_1_6_4 = register(78, "1.6.4");
    // After the Netty rewrite
    public static final ProtocolVersion v1_7_1 = register(4, "1.7-1.7.5", new VersionRange("1.7", 0, 5));
    public static final ProtocolVersion v1_7_6 = register(5, "1.7.6-1.7.10", new VersionRange("1.7", 6, 10));
    public static final ProtocolVersion v1_8 = register(47, "1.8.x");
    public static final ProtocolVersion v1_9 = register(107, "1.9");
    public static final ProtocolVersion v1_9_1 = register(108, "1.9.1");
    public static final ProtocolVersion v1_9_2 = register(109, "1.9.2");
    public static final ProtocolVersion v1_9_3 = register(110, "1.9.3/4", new VersionRange("1.9", 3, 4));
    public static final ProtocolVersion v1_10 = register(210, "1.10.x");
    public static final ProtocolVersion v1_11 = register(315, "1.11");
    public static final ProtocolVersion v1_11_1 = register(316, "1.11.1/2", new VersionRange("1.11", 1, 2));
    public static final ProtocolVersion v1_12 = register(335, "1.12");
    public static final ProtocolVersion v1_12_1 = register(338, "1.12.1");
    public static final ProtocolVersion v1_12_2 = register(340, "1.12.2");
    public static final ProtocolVersion v1_13 = register(393, "1.13");
    public static final ProtocolVersion v1_13_1 = register(401, "1.13.1");
    public static final ProtocolVersion v1_13_2 = register(404, "1.13.2");
    public static final ProtocolVersion v1_14 = register(477, "1.14");
    public static final ProtocolVersion v1_14_1 = register(480, "1.14.1");
    public static final ProtocolVersion v1_14_2 = register(485, "1.14.2");
    public static final ProtocolVersion v1_14_3 = register(490, "1.14.3");
    public static final ProtocolVersion v1_14_4 = register(498, "1.14.4");
    public static final ProtocolVersion v1_15 = register(573, "1.15");
    public static final ProtocolVersion v1_15_1 = register(575, "1.15.1");
    public static final ProtocolVersion v1_15_2 = register(578, "1.15.2");
    public static final ProtocolVersion v1_16 = register(735, "1.16");
    public static final ProtocolVersion v1_16_1 = register(736, "1.16.1");
    public static final ProtocolVersion v1_16_2 = register(751, "1.16.2");
    public static final ProtocolVersion v1_16_3 = register(753, "1.16.3");
    public static final ProtocolVersion v1_16_4 = register(754, "1.16.4/5", new VersionRange("1.16", 4, 5));
    public static final ProtocolVersion v1_17 = register(755, "1.17");
    public static final ProtocolVersion unknown = register(-1, "UNKNOWN");

    public static ProtocolVersion register(int version, String name) {
        return register(version, -1, name);
    }

    public static ProtocolVersion register(int version, int snapshotVersion, String name) {
        return register(version, snapshotVersion, name, null);
    }

    public static ProtocolVersion register(int version, String name, @Nullable VersionRange versionRange) {
        return register(version, -1, name, versionRange);
    }

    /**
     * Registers a protocol version.
     *
     * @param version         release protocol version
     * @param snapshotVersion snapshot protocol version, or -1 if not a snapshot
     * @param name            version name
     * @param versionRange    range of versions that are supported by this protocol version, null if not a range
     * @return registered {@link ProtocolVersion}
     */
    public static ProtocolVersion register(int version, int snapshotVersion, String name, @Nullable VersionRange versionRange) {
        ProtocolVersion protocol = new ProtocolVersion(version, snapshotVersion, name, versionRange);
        VERSION_LIST.add(protocol);
        VERSIONS.put(protocol.getVersion(), protocol);
        if (protocol.isSnapshot()) {
            VERSIONS.put(protocol.getFullSnapshotVersion(), protocol);
        }
        return protocol;
    }

    /**
     * Returns whether a protocol with the given protocol version is registered.
     *
     * @param version protocol version
     * @return true if this protocol version has been registered
     */
    public static boolean isRegistered(int version) {
        return VERSIONS.containsKey(version);
    }

    /**
     * Returns a {@link ProtocolVersion} instance, even if this protocol version
     * has not been registered. See {@link #isRegistered(int)} berorehand or {@link #isKnown()}.
     *
     * @param version protocol version
     * @return registered or unknown {@link ProtocolVersion}
     */
    public static @NonNull ProtocolVersion getProtocol(int version) {
        ProtocolVersion protocolVersion = VERSIONS.get(version);
        if (protocolVersion != null) {
            return protocolVersion;
        } else {
            return new ProtocolVersion(version, "Unknown (" + version + ")");
        }
    }

    /**
     * Returns the internal index of the stored protocol version.
     *
     * @param version protocol version instance
     * @return internal index of the stored protocol version
     */
    public static int getIndex(ProtocolVersion version) {
        return VERSION_LIST.indexOf(version);
    }

    /**
     * Returns an immutable list of registered protocol versions.
     *
     * @return immutable list of registered protocol versions
     */
    public static List<ProtocolVersion> getProtocols() {
        return Collections.unmodifiableList(VERSION_LIST);
    }

    /**
     * Returns the registered protocol version if present, else null.
     * This accepts the actual registered names (like "1.16.4/5") as well as
     * included versions for version ranges and wildcards.
     *
     * @param protocol version name, e.g. "1.16.3"
     * @return registered protocol version if present, else null
     */
    public static @Nullable ProtocolVersion getClosest(String protocol) {
        for (ProtocolVersion version : VERSIONS.values()) {
            String name = version.getName();
            if (name.equals(protocol)) {
                return version;
            }

            if (version.isVersionWildcard()) {
                // Test against the major version with and without a minor version
                String majorVersion = name.substring(0, name.length() - 2);
                if (majorVersion.equals(protocol) || (protocol.startsWith(name.substring(0, name.length() - 1)))) {
                    return version;
                }
            } else if (version.isRange()) {
                if (version.getIncludedVersions().contains(protocol)) {
                    return version;
                }
            }
        }
        return null;
    }

    private final int version;
    private final int snapshotVersion;
    private final String name;
    private final boolean versionWildcard;
    private final Set<String> includedVersions;

    /**
     * @param version protocol version
     * @param name    version name
     */
    public ProtocolVersion(int version, String name) {
        this(version, -1, name, null);
    }

    /**
     * @param version         protocol version
     * @param snapshotVersion actual snapshot protocol version, -1 if not a snapshot
     * @param name            version name
     * @param versionRange    range of versions that are supported by this protocol version, null if not a range
     */
    public ProtocolVersion(int version, int snapshotVersion, String name, @Nullable VersionRange versionRange) {
        this.version = version;
        this.snapshotVersion = snapshotVersion;
        this.name = name;
        this.versionWildcard = name.endsWith(".x");

        Preconditions.checkArgument(!versionWildcard || versionRange == null, "A version cannot be a wildcard and a range at the same time!");
        if (versionRange != null) {
            includedVersions = new LinkedHashSet<>();
            for (int i = versionRange.getRangeFrom(); i <= versionRange.getRangeTo(); i++) {
                if (i == 0) {
                    includedVersions.add(versionRange.getBaseVersion()); // Keep both the base version and with ".0" appended
                }

                includedVersions.add(versionRange.getBaseVersion() + "." + i);
            }
        } else {
            includedVersions = Collections.singleton(name);
        }
    }

    /**
     * Returns the release protocol version.
     *
     * @return release version
     */
    public int getVersion() {
        return version;
    }

    /**
     * Returns the snapshot protocol version without the snapshot indicator bit if this is a snapshot protocol version.
     *
     * @return snapshot protocol version without the snapshot indicator bit
     * @throws IllegalArgumentException if the version if not a snapshot version
     * @see #isSnapshot()
     */
    public int getSnapshotVersion() {
        Preconditions.checkArgument(isSnapshot());
        return snapshotVersion;
    }

    /**
     * Returns the snapshot protocol version with the snapshot indicator bit if this is a snapshot protocol version.
     *
     * @return snapshot protocol version with the snapshot indicator bit
     * @throws IllegalArgumentException if the version if not a snapshot version
     * @see #isSnapshot()
     */
    public int getFullSnapshotVersion() {
        Preconditions.checkArgument(isSnapshot());
        return (1 << 30) | snapshotVersion; // Bit indicating snapshot versions
    }

    /**
     * Returns the release version if release, snapshot version (with the snapshot indicator bit) if snapshot.
     *
     * @return release version if release, snapshot version (with the snapshot indicator bit) if snapshot
     */
    public int getOriginalVersion() {
        return snapshotVersion == -1 ? version : ((1 << 30) | snapshotVersion);
    }

    /**
     * Returns whether the protocol is set. Should only be unknown for unregistered protocols returned by {@link #getProtocol(int)}.
     *
     * @return true if the protocol is set
     */
    public boolean isKnown() {
        return version != -1;
    }

    /**
     * Returns whether the protocol includes a range of versions (but not an entire major version range), for example 1.7-1.7.5.
     *
     * @return true if the protocol includes a range of versions
     * @see #getIncludedVersions()
     */
    public boolean isRange() {
        return includedVersions.size() != 1;
    }

    /**
     * Returns an immutable set of all included versions if the protocol is a version range.
     * If the protocol only includes a single Minecraft version or the entire major version as a wildcard ({@link #isVersionWildcard()}),
     * the set will only contain the string given in {@link #getName()}.
     *
     * @return immutable set of all included versions if the protocol is a version range
     * @see #isRange()
     */
    public Set<String> getIncludedVersions() {
        return Collections.unmodifiableSet(includedVersions);
    }

    /**
     * Returns whether the protocol includes an entire major version range (for example 1.8.x).
     *
     * @return true if the protocol includes an entire major version range
     */
    public boolean isVersionWildcard() {
        return versionWildcard;
    }

    /**
     * Returns the version name.
     *
     * @return version name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns whether this represents a snapshot version.
     *
     * @return true if this represents a snapshot version, false otherwise
     */
    public boolean isSnapshot() {
        return snapshotVersion != -1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProtocolVersion that = (ProtocolVersion) o;
        return version == that.version;
    }

    @Override
    public int hashCode() {
        return version;
    }

    @Override
    public String toString() {
        return String.format("%s (%d)", this.name, this.version);
    }
}

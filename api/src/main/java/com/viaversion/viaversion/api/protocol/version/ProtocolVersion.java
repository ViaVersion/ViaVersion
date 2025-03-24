/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2025 ViaVersion and contributors
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class ProtocolVersion implements Comparable<ProtocolVersion> {

    // These need to be at the top of the class to be initialized first
    private static final Map<VersionType, Int2ObjectMap<ProtocolVersion>> VERSIONS = new EnumMap<>(VersionType.class);
    private static final List<ProtocolVersion> VERSION_LIST = new ArrayList<>();

    public static final ProtocolVersion v1_7_2 = register(4, "1.7.2-1.7.5", new SubVersionRange("1.7", 2, 5));
    @Deprecated(forRemoval = true)
    public static final ProtocolVersion v1_7_1 = v1_7_2;
    public static final ProtocolVersion v1_7_6 = register(5, "1.7.6-1.7.10", new SubVersionRange("1.7", 6, 10));
    public static final ProtocolVersion v1_8 = register(47, "1.8.x", new SubVersionRange("1.8", 0, 9));
    public static final ProtocolVersion v1_9 = register(107, "1.9");
    public static final ProtocolVersion v1_9_1 = register(108, "1.9.1");
    public static final ProtocolVersion v1_9_2 = register(109, "1.9.2");
    public static final ProtocolVersion v1_9_3 = register(110, "1.9.3-1.9.4", new SubVersionRange("1.9", 3, 4));
    public static final ProtocolVersion v1_10 = register(210, "1.10.x", new SubVersionRange("1.10", 0, 2));
    public static final ProtocolVersion v1_11 = register(315, "1.11");
    public static final ProtocolVersion v1_11_1 = register(316, "1.11.1-1.11.2", new SubVersionRange("1.11", 1, 2));
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
    public static final ProtocolVersion v1_16_4 = register(754, "1.16.4-1.16.5", new SubVersionRange("1.16", 4, 5));
    public static final ProtocolVersion v1_17 = register(755, "1.17");
    public static final ProtocolVersion v1_17_1 = register(756, "1.17.1");
    public static final ProtocolVersion v1_18 = register(757, "1.18-1.18.1", new SubVersionRange("1.18", 0, 1));
    public static final ProtocolVersion v1_18_2 = register(758, "1.18.2");
    public static final ProtocolVersion v1_19 = register(759, "1.19");
    public static final ProtocolVersion v1_19_1 = register(760, "1.19.1-1.19.2", new SubVersionRange("1.19", 1, 2));
    public static final ProtocolVersion v1_19_3 = register(761, "1.19.3");
    public static final ProtocolVersion v1_19_4 = register(762, "1.19.4");
    public static final ProtocolVersion v1_20 = register(763, "1.20-1.20.1", new SubVersionRange("1.20", 0, 1));
    public static final ProtocolVersion v1_20_2 = register(764, "1.20.2");
    public static final ProtocolVersion v1_20_3 = register(765, "1.20.3-1.20.4", new SubVersionRange("1.20", 3, 4));
    public static final ProtocolVersion v1_20_5 = register(766, "1.20.5-1.20.6", new SubVersionRange("1.20", 5, 6));
    public static final ProtocolVersion v1_21 = register(767, "1.21-1.21.1", new SubVersionRange("1.21", 0, 1));
    public static final ProtocolVersion v1_21_2 = register(768, "1.21.2-1.21.3", new SubVersionRange("1.21", 2, 3));
    public static final ProtocolVersion v1_21_4 = register(769, "1.21.4");
    public static final ProtocolVersion v1_21_5 = register(770, "1.21.5");
    public static final ProtocolVersion unknown = new ProtocolVersion(VersionType.SPECIAL, -1, -1, "UNKNOWN", null);

    static {
        unknown.known = false;
    }

    public static ProtocolVersion register(int version, String name) {
        return register(version, -1, name);
    }

    public static ProtocolVersion register(int version, int snapshotVersion, String name) {
        final ProtocolVersion protocolVersion = new ProtocolVersion(VersionType.RELEASE, version, snapshotVersion, name, null);
        register(protocolVersion);
        return protocolVersion;
    }

    public static ProtocolVersion register(int version, String name, @Nullable SubVersionRange versionRange) {
        final ProtocolVersion protocolVersion = new ProtocolVersion(VersionType.RELEASE, version, -1, name, versionRange);
        register(protocolVersion);
        return protocolVersion;
    }

    /**
     * Registers a protocol version.
     *
     * @param protocolVersion protocol version to register
     */
    public static void register(ProtocolVersion protocolVersion) {
        VERSION_LIST.add(protocolVersion);
        VERSION_LIST.sort(ProtocolVersion::compareTo);

        final Int2ObjectMap<ProtocolVersion> versions = VERSIONS.computeIfAbsent(protocolVersion.versionType, $ -> new Int2ObjectOpenHashMap<>());
        versions.put(protocolVersion.version, protocolVersion);
        if (protocolVersion.isSnapshot()) {
            versions.put(protocolVersion.getFullSnapshotVersion(), protocolVersion);
        }
    }

    /**
     * Returns whether a protocol with the given protocol version is registered.
     *
     * @param version protocol version
     * @return true if this protocol version has been registered
     */
    public static boolean isRegistered(final VersionType versionType, final int version) {
        final Int2ObjectMap<ProtocolVersion> versions = VERSIONS.get(versionType);
        return versions != null && versions.containsKey(version);
    }

    public static boolean isRegistered(int version) {
        return isRegistered(VersionType.RELEASE, version);
    }

    /**
     * Returns a ProtocolVersion instance, even if this protocol version
     * has not been registered. See {@link #isRegistered(VersionType, int)} beforehand or {@link #isKnown()}.
     *
     * @param versionType protocol version type
     * @param version     protocol version
     * @return registered or unknown ProtocolVersion
     */
    public static @NonNull ProtocolVersion getProtocol(final VersionType versionType, final int version) {
        final Int2ObjectMap<ProtocolVersion> versions = VERSIONS.get(versionType);
        if (versions != null) {
            final ProtocolVersion protocolVersion = versions.get(version);
            if (protocolVersion != null) {
                return protocolVersion;
            }
        }

        // Will be made nullable instead in the future...
        final ProtocolVersion unknown = new ProtocolVersion(versionType, version, -1, "Unknown (" + version + ")", null);
        unknown.known = false;
        return unknown;
    }

    public static @NonNull ProtocolVersion getProtocol(final int version) {
        return getProtocol(VersionType.RELEASE, version);
    }

    /**
     * Returns the internal index of the stored protocol version.
     *
     * @param version protocol version instance
     * @return internal index of the stored protocol version
     * @deprecated comparison should be done via the comparison methods
     */
    @Deprecated(forRemoval = true)
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
     * This accepts the actual registered names (like "1.16.4/1.16.5") as well as
     * included versions for version ranges and wildcards.
     *
     * @param protocol version name, e.g. "1.16.3"
     * @return registered protocol version if present, else null
     */
    public static @Nullable ProtocolVersion getClosest(String protocol) {
        for (ProtocolVersion version : VERSION_LIST) {
            String name = version.getName();
            if (name.equals(protocol) || version.isRange() && version.getIncludedVersions().contains(protocol)) {
                return version;
            }
        }
        return null;
    }

    private final VersionType versionType;
    private final int version;
    private final int snapshotVersion;
    private final String name;
    private final Set<String> includedVersions;
    @Deprecated // Remove when getProtocol is made nullable
    private boolean known = true;

    /**
     * Constructs a new ProtocolVersion instance.
     *
     * @param versionType     protocol version type
     * @param version         protocol version
     * @param snapshotVersion actual snapshot protocol version, -1 if not a snapshot
     * @param name            version name
     * @param versionRange    range of versions that are supported by this protocol version, null if not a range
     */
    public ProtocolVersion(VersionType versionType, int version, int snapshotVersion, String name, @Nullable SubVersionRange versionRange) {
        this.versionType = versionType;
        this.version = version;
        this.snapshotVersion = snapshotVersion;
        this.name = name;

        Preconditions.checkArgument(!(isVersionWildcard() && versionRange == null), "A wildcard name must have a version range");
        if (versionRange != null) {
            includedVersions = new LinkedHashSet<>();
            for (int i = versionRange.rangeFrom(); i <= versionRange.rangeTo(); i++) {
                if (i == 0) {
                    includedVersions.add(versionRange.baseVersion()); // Keep both the base version and with ".0" appended
                }

                includedVersions.add(versionRange.baseVersion() + "." + i);
            }
        } else {
            includedVersions = Collections.singleton(name);
        }
    }

    /**
     * Returns the type of version (excluding whether it is a snapshot).
     *
     * @return version type
     * @see #isSnapshot()
     */
    public VersionType getVersionType() {
        return versionType;
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
     * @throws IllegalArgumentException if the version is not a snapshot version
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
     * @throws IllegalArgumentException if the version is not a snapshot version
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
     * Returns whether the protocol version is {@link #unknown}. For checking if the protocol version is registered, use {@link #isRegistered(VersionType, int)}
     *
     * @return true if the protocol version is unknown
     */
    public boolean isKnown() {
        return known;
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
        return this.name.endsWith(".x");
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

    /**
     * Returns whether this protocol version is equal to the other protocol version.
     *
     * @param other other protocol version
     * @return true if this protocol version is equal to the other protocol version
     */
    public boolean equalTo(final ProtocolVersion other) {
        return this.compareTo(other) == 0;
    }

    /**
     * Returns whether this protocol version is higher than the other protocol version.
     *
     * @param other other protocol version
     * @return true if this protocol version is higher than the other protocol version
     */
    public boolean newerThan(final ProtocolVersion other) {
        return this.compareTo(other) > 0;
    }

    /**
     * Returns whether this protocol version is higher than or equal to the other protocol version.
     *
     * @param other other protocol version
     * @return true if this protocol version is higher than or equal to the other protocol version
     */
    public boolean newerThanOrEqualTo(final ProtocolVersion other) {
        return this.compareTo(other) >= 0;
    }

    /**
     * Returns whether this protocol version is lower than the other protocol version.
     *
     * @param other other protocol version
     * @return true if this protocol version is lower than the other protocol version
     */
    public boolean olderThan(final ProtocolVersion other) {
        return this.compareTo(other) < 0;
    }

    /**
     * Returns whether this protocol version is lower than or equal to the other protocol version.
     *
     * @param other other protocol version
     * @return true if this protocol version is lower than or equal to the other protocol version
     */
    public boolean olderThanOrEqualTo(final ProtocolVersion other) {
        return this.compareTo(other) <= 0;
    }

    /**
     * Returns whether this protocol version is between the given protocol versions, inclusive.
     *
     * @param min minimum version
     * @param max maximum version
     * @return true if this protocol version is between the given protocol versions, inclusive
     */
    public boolean betweenInclusive(final ProtocolVersion min, final ProtocolVersion max) {
        return this.newerThanOrEqualTo(min) && this.olderThanOrEqualTo(max);
    }

    /**
     * Returns whether this protocol version is between the given protocol versions, exclusive.
     *
     * @param min minimum version
     * @param max maximum version
     * @return true if this protocol version is between the given protocol versions, exclusive
     */
    public boolean betweenExclusive(final ProtocolVersion min, final ProtocolVersion max) {
        return this.newerThan(min) && this.olderThan(max);
    }

    /**
     * Returns a custom comparator used to compare protocol versions.
     * Must be overridden if the version type is {@link VersionType#SPECIAL}
     *
     * @return custom comparator
     */
    protected @Nullable Comparator<ProtocolVersion> customComparator() {
        return null;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final ProtocolVersion that = (ProtocolVersion) o;
        return version == that.version && versionType == that.versionType && snapshotVersion == that.snapshotVersion;
    }

    @Override
    public int hashCode() {
        int result = versionType.hashCode();
        result = 31 * result + version;
        result = 31 * result + snapshotVersion;
        return result;
    }

    @Override
    public String toString() {
        return String.format("%s (%d)", this.name, this.version);
    }

    @Override
    public int compareTo(final ProtocolVersion other) {
        // Cursed custom comparators
        if (this.versionType == VersionType.SPECIAL && customComparator() != null) {
            return customComparator().compare(this, other);
        } else if (other.versionType == VersionType.SPECIAL && other.customComparator() != null) {
            return other.customComparator().compare(this, other);
        }

        if (this.versionType != other.versionType) {
            // Compare by version type first since version ids have reset multiple times
            return this.versionType.ordinal() < other.versionType.ordinal() ? -1 : 1;
        } else if (this.version != other.version) {
            // Compare by release version
            return this.version < other.version ? -1 : 1;
        }
        // Finally, compare by snapshot version
        return Integer.compare(this.snapshotVersion, other.snapshotVersion);
    }
}

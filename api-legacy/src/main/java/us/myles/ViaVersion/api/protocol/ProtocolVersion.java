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
package us.myles.ViaVersion.api.protocol;

import com.google.common.base.Preconditions;
import com.viaversion.viaversion.api.protocol.version.SubVersionRange;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * @deprecated will no longer be updated and may be removed at some point, use {@link com.viaversion.viaversion.api.protocol.version.ProtocolVersion}
 */
@Deprecated
public class ProtocolVersion {
    private static final Int2ObjectMap<ProtocolVersion> versions = new Int2ObjectOpenHashMap<>();
    private static final List<ProtocolVersion> versionList = new ArrayList<>();

    public static final ProtocolVersion v1_4_6 = register(51, "1.4.6/7", new SubVersionRange("1.4", 6, 7));
    public static final ProtocolVersion v1_5_1 = register(60, "1.5.1");
    public static final ProtocolVersion v1_5_2 = register(61, "1.5.2");
    public static final ProtocolVersion v_1_6_1 = register(73, "1.6.1");
    public static final ProtocolVersion v_1_6_2 = register(74, "1.6.2");
    public static final ProtocolVersion v_1_6_3 = register(77, "1.6.3");
    public static final ProtocolVersion v_1_6_4 = register(78, "1.6.4");
    public static final ProtocolVersion v1_7_1 = register(4, "1.7-1.7.5", new SubVersionRange("1.7", 0, 5));
    public static final ProtocolVersion v1_7_6 = register(5, "1.7.6-1.7.10", new SubVersionRange("1.7", 6, 10));
    public static final ProtocolVersion v1_8 = register(47, "1.8.x");
    public static final ProtocolVersion v1_9 = register(107, "1.9");
    public static final ProtocolVersion v1_9_1 = register(108, "1.9.1");
    public static final ProtocolVersion v1_9_2 = register(109, "1.9.2");
    public static final ProtocolVersion v1_9_3 = register(110, "1.9.3/1.9.4", new SubVersionRange("1.9", 3, 4));
    public static final ProtocolVersion v1_10 = register(210, "1.10.x");
    public static final ProtocolVersion v1_11 = register(315, "1.11");
    public static final ProtocolVersion v1_11_1 = register(316, "1.11.1/1.11.2", new SubVersionRange("1.11", 1, 2));
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
    public static final ProtocolVersion v1_16_4 = register(754, "1.16.4/1.16.5", new SubVersionRange("1.16", 4, 5));
    public static final ProtocolVersion v1_17 = register(755, "1.17");
    public static final ProtocolVersion v1_17_1 = register(756, "1.17.1");
    public static final ProtocolVersion v1_18 = register(757, "1.18/1.18.1", new SubVersionRange("1.18", 0, 1));
    public static final ProtocolVersion v1_18_2 = register(758, "1.18.2");
    public static final ProtocolVersion v1_19 = register(759, "1.19");
    public static final ProtocolVersion v1_19_1 = register(760, "1.19.1/1.19.2", new SubVersionRange("1.19", 1, 2));
    public static final ProtocolVersion v1_19_3 = register(761, "1.19.3");
    public static final ProtocolVersion v1_19_4 = register(762, "1.19.4");
    public static final ProtocolVersion v1_20 = register(763, "1.20/1.20.1", new SubVersionRange("1.20", 0, 1));
    public static final ProtocolVersion v1_20_2 = register(764, "1.20.2");
    public static final ProtocolVersion v1_20_3 = register(765, "1.20.3/1.20.4", new SubVersionRange("1.20", 3, 4));
    public static final ProtocolVersion v1_20_5 = register(766, "1.20.5");
    public static final ProtocolVersion unknown = register(-1, "UNKNOWN");

    public static ProtocolVersion register(int version, String name) {
        return register(version, -1, name);
    }

    public static ProtocolVersion register(int version, int snapshotVersion, String name) {
        return register(version, snapshotVersion, name, null);
    }

    public static ProtocolVersion register(int version, String name, @Nullable SubVersionRange versionRange) {
        return register(version, -1, name, versionRange);
    }

    public static ProtocolVersion register(int version, int snapshotVersion, String name, @Nullable SubVersionRange versionRange) {
        ProtocolVersion protocol = new ProtocolVersion(version, snapshotVersion, name, versionRange);
        versionList.add(protocol);
        versions.put(protocol.getVersion(), protocol);
        if (protocol.isSnapshot()) {
            versions.put(protocol.getFullSnapshotVersion(), protocol);
        }
        return protocol;
    }

    public static boolean isRegistered(int id) {
        return versions.containsKey(id);
    }

    public static @NonNull ProtocolVersion getProtocol(int id) {
        ProtocolVersion protocolVersion = versions.get(id);
        if (protocolVersion != null) {
            return protocolVersion;
        } else {
            return new ProtocolVersion(id, "Unknown (" + id + ")");
        }
    }

    public static int getIndex(ProtocolVersion version) {
        return versionList.indexOf(version);
    }

    public static List<ProtocolVersion> getProtocols() {
        return Collections.unmodifiableList(new ArrayList<>(versions.values()));
    }

    public static @Nullable ProtocolVersion getClosest(String protocol) {
        for (ProtocolVersion version : versions.values()) {
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

    public ProtocolVersion(int version, String name) {
        this(version, -1, name, null);
    }

    public ProtocolVersion(int version, int snapshotVersion, String name, @Nullable SubVersionRange versionRange) {
        this.version = version;
        this.snapshotVersion = snapshotVersion;
        this.name = name;
        this.versionWildcard = name.endsWith(".x");

        Preconditions.checkArgument(!versionWildcard || versionRange == null, "A version cannot be a wildcard and a range at the same time!");
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

    public int getVersion() {
        return version;
    }

    public int getSnapshotVersion() {
        Preconditions.checkArgument(isSnapshot());
        return snapshotVersion;
    }

    public int getFullSnapshotVersion() {
        Preconditions.checkArgument(isSnapshot());
        return (1 << 30) | snapshotVersion; // Bit indicating snapshot versions
    }

    public int getOriginalVersion() {
        return snapshotVersion == -1 ? version : ((1 << 30) | snapshotVersion);
    }

    public boolean isKnown() {
        return version != -1;
    }

    public boolean isRange() {
        return includedVersions.size() != 1;
    }

    public Set<String> getIncludedVersions() {
        return Collections.unmodifiableSet(includedVersions);
    }

    public boolean isVersionWildcard() {
        return versionWildcard;
    }

    public String getName() {
        return name;
    }

    public boolean isSnapshot() {
        return snapshotVersion != -1;
    }

    public int getId() {
        return version;
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

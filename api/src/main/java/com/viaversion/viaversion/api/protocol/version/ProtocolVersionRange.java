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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Can be used to compare a range of {@link ProtocolVersion}s.
 */
public class ProtocolVersionRange {

    private final ProtocolVersion min;
    private final ProtocolVersion max;
    private final List<ProtocolVersionRange> ranges;

    private ProtocolVersionRange(final ProtocolVersion min, final ProtocolVersion max) {
        this.min = min;
        this.max = max;
        this.ranges = new ArrayList<>();
    }

    /**
     * Creates a new range that includes all versions newer or equal to the given version.
     *
     * @param version the version
     * @return the range
     */
    public static ProtocolVersionRange andNewer(final ProtocolVersion version) {
        return new ProtocolVersionRange(version, null);
    }

    /**
     * Creates a new range that includes all versions older or equal to the given version.
     *
     * @param version the version
     * @return the range
     */
    public static ProtocolVersionRange single(final ProtocolVersion version) {
        return new ProtocolVersionRange(version, version);
    }

    /**
     * Creates a new range that includes all versions older or equal to the given version.
     * @param version the version
     * @return the range
     */
    public static ProtocolVersionRange andOlder(final ProtocolVersion version) {
        return new ProtocolVersionRange(null, version);
    }

    /**
     * Creates a new range that includes all versions between the given versions.
     * @param min the minimum version
     * @param max the maximum version
     * @return the range
     */
    public static ProtocolVersionRange of(final ProtocolVersion min, final ProtocolVersion max) {
        return new ProtocolVersionRange(min, max);
    }

    /**
     * Creates a new range that includes all versions.
     * @return the range
     */
    public static ProtocolVersionRange all() {
        return new ProtocolVersionRange(null, null);
    }

    /**
     * Merges the given range into this range.
     * @param range the range
     * @return A merged range that includes all versions from this and the given range
     */
    public ProtocolVersionRange add(final ProtocolVersionRange range) {
        this.ranges.add(range);
        return this;
    }

    /**
     * Checks if the given version is included in this range.
     * @param version the version
     * @return {@code true} if the version is included
     */
    public boolean contains(final ProtocolVersion version) {
        if (this.ranges.stream().anyMatch(range -> range.contains(version))) return true;
        if (this.min == null && this.max == null) return true;
        else if (this.min == null) return version.lowerThanOrEquals(this.max);
        else if (this.max == null) return version.higherThanOrEquals(this.min);
        return version.higherThanOrEquals(this.min) && version.lowerThanOrEquals(this.max);
    }

    public ProtocolVersion getMin() {
        return this.min;
    }

    public ProtocolVersion getMax() {
        return this.max;
    }

    @Override
    public String toString() {
        if (this.min == null && this.max == null) return "*";
        else {
            StringBuilder rangeString = new StringBuilder();
            if (!this.ranges.isEmpty()) {
                for (ProtocolVersionRange range : this.ranges) rangeString.append(", ").append(range.toString());
            }
            if (this.min == null) return "<= " + this.max.getName() + rangeString;
            else if (this.max == null) return ">= " + this.min.getName() + rangeString;
            else if (Objects.equals(this.min, this.max)) return this.min.getName();
            else return this.min.getName() + " - " + this.max.getName() + rangeString;
        }
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        ProtocolVersionRange that = (ProtocolVersionRange) object;
        return min == that.min && max == that.max && Objects.equals(ranges, that.ranges);
    }

    @Override
    public int hashCode() {
        return Objects.hash(min, max, ranges);
    }

    public static ProtocolVersionRange fromString(String str) {
        if ("*".equals(str)) return all();
        else if (str.contains(",")) {
            String[] rangeParts = str.split(", ");
            ProtocolVersionRange versionRange = null;

            for (String part : rangeParts) {
                if (versionRange == null) versionRange = parseSinglePart(part);
                else versionRange.add(parseSinglePart(part));
            }
            return versionRange;
        } else {
            return parseSinglePart(str);
        }
    }

    private static ProtocolVersionRange parseSinglePart(String part) {
        if (part.startsWith("<= ")) return andOlder(ProtocolVersion.getClosest(part.substring(3)));
        else if (part.startsWith(">= ")) return andNewer(ProtocolVersion.getClosest(part.substring(3)));
        else if (part.contains(" - ")) {
            String[] rangeParts = part.split(" - ");
            ProtocolVersion min = ProtocolVersion.getClosest(rangeParts[0]);
            ProtocolVersion max = ProtocolVersion.getClosest(rangeParts[1]);
            return of(min, max);
        } else return single(ProtocolVersion.getClosest(part));
    }


}

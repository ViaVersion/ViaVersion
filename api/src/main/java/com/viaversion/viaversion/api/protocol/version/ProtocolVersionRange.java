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

import com.google.common.collect.Range;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Wrapper class file for {@link com.google.common.collect.Range} to support multiple ranges. This class is used to
 * compare {@link ProtocolVersion} objects.
 */
public class ProtocolVersionRange {

    private List<Range<ProtocolVersion>> ranges;

    private ProtocolVersionRange(final List<Range<ProtocolVersion>> ranges) {
        if (ranges != null) {
            this.ranges = new ArrayList<>(ranges);
        }
    }

    /**
     * Returns a range that contains all versions.
     *
     * @return the range
     */
    public static ProtocolVersionRange all() {
        return new ProtocolVersionRange(null);
    }

    /**
     * Returns a range that contains only the given version.
     *
     * @param min the version
     * @param max the version
     * @return the range
     */
    public static ProtocolVersionRange of(final ProtocolVersion min, final ProtocolVersion max) {
        return new ProtocolVersionRange(Collections.singletonList(Range.open(min, max)));
    }

    /**
     * Returns a range that contains only the given version.
     *
     * @param range the version
     * @return the range
     */
    public static ProtocolVersionRange of(final Range<ProtocolVersion> range) {
        return new ProtocolVersionRange(Collections.singletonList(range));
    }

    /**
     * Returns a range that contains only the given version. The list can be immutable.
     *
     * @param ranges the version
     * @return the range
     */
    public static ProtocolVersionRange of(final List<Range<ProtocolVersion>> ranges) {
        return new ProtocolVersionRange(ranges);
    }

    /**
     * Adds a new range to this range. This method is only available if the range is not already containing all versions.
     *
     * @param range the range to add
     * @return this range
     */
    public ProtocolVersionRange add(final Range<ProtocolVersion> range) {
        if (ranges == null) {
            throw new UnsupportedOperationException("Range already contains all versions. Cannot add a new range.");
        }
        ranges.add(range);
        return this;
    }

    /**
     * Checks if the given version is included in this range.
     *
     * @param version the version
     * @return {@code true} if the version is included
     */
    public boolean contains(final ProtocolVersion version) {
        if (this.ranges == null) return true;
        return this.ranges.stream().anyMatch(range -> range.contains(version));
    }

    @Override
    public String toString() {
        if (this.ranges != null) {
            StringBuilder rangeString = new StringBuilder();
            int i = 0;
            for (Range<ProtocolVersion> range : this.ranges) {
                i++;
                final ProtocolVersion min = range.hasLowerBound() ? range.lowerEndpoint() : null;
                final ProtocolVersion max = range.hasUpperBound() ? range.upperEndpoint() : null;

                if (min == null) rangeString.append("<= ").append(max.getName());
                else if (max == null) rangeString.append(">= ").append(min.getName());
                else if (Objects.equals(min, max)) rangeString.append(min.getName());
                else rangeString.append(min.getName()).append(" - ").append(max.getName());

                if (i != this.ranges.size()) {
                    rangeString.append(", ");
                }
            }
            return rangeString.toString();
        }
        return "*";
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        ProtocolVersionRange that = (ProtocolVersionRange) object;
        return Objects.equals(ranges, that.ranges);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ranges);
    }

    /**
     * Parses a range from a string.
     *
     * @param str the string
     * @return the range
     */
    public static ProtocolVersionRange fromString(final String str) {
        if ("*".equals(str)) return all();
        else if (str.contains(",")) {
            String[] rangeParts = str.split(", ");
            ProtocolVersionRange versionRange = null;

            for (String part : rangeParts) {
                if (versionRange == null) versionRange = of(parseSinglePart(part));
                else versionRange.add(parseSinglePart(part));
            }
            return versionRange;
        } else {
            return of(parseSinglePart(str));
        }
    }

    private static Range<ProtocolVersion> parseSinglePart(final String part) {
        if (part.startsWith("<= ")) return Range.atMost(ProtocolVersion.getClosest(part.substring(3)));
        else if (part.startsWith(">= ")) return Range.atLeast(ProtocolVersion.getClosest(part.substring(3)));
        else if (part.contains(" - ")) {
            String[] rangeParts = part.split(" - ");
            ProtocolVersion min = ProtocolVersion.getClosest(rangeParts[0]);
            ProtocolVersion max = ProtocolVersion.getClosest(rangeParts[1]);
            return Range.open(min, max);
        } else return Range.singleton(ProtocolVersion.getClosest(part));
    }

}

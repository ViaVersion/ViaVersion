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

import com.google.common.base.Preconditions;

public class SubVersionRange {
    private final String baseVersion;
    private final int rangeFrom;
    private final int rangeTo;

    /**
     * Creates a new version range. Giving "1.7", 0, and 5 for example would represent the range from 1.7-1.7.5.
     *
     * @param baseVersion base version
     * @param rangeFrom   minor version the range begins at, must be greater than or equal to 0
     * @param rangeTo     minor version the range ends at, must be greater than {@code rangeFrom}
     */
    public SubVersionRange(String baseVersion, int rangeFrom, int rangeTo) {
        Preconditions.checkNotNull(baseVersion);
        Preconditions.checkArgument(rangeFrom >= 0);
        Preconditions.checkArgument(rangeTo > rangeFrom);
        this.baseVersion = baseVersion;
        this.rangeFrom = rangeFrom;
        this.rangeTo = rangeTo;
    }

    /**
     * Returns the major version name.
     *
     * @return major version name
     */
    public String baseVersion() {
        return baseVersion;
    }

    /**
     * Returns the lowest included minor version.
     *
     * @return lowest included minor version
     */
    public int rangeFrom() {
        return rangeFrom;
    }

    /**
     * Returns the highest included minor version.
     *
     * @return highest included minor version
     */
    public int rangeTo() {
        return rangeTo;
    }

}

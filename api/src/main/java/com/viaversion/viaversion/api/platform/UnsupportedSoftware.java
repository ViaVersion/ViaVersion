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
package com.viaversion.viaversion.api.platform;

import java.util.Collections;
import java.util.List;

public final class UnsupportedSoftware {

    private final String name;
    private final List<String> classNames;
    private final String reason;

    public UnsupportedSoftware(String name, List<String> classNames, String reason) {
        this.name = name;
        this.classNames = Collections.unmodifiableList(classNames);
        this.reason = reason;
    }

    public UnsupportedSoftware(String name, String className, String reason) {
        this.name = name;
        this.classNames = Collections.singletonList(className);
        this.reason = reason;
    }

    /**
     * Returns the software name.
     *
     * @return software name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns an immutable list of the fully qualified class name.
     *
     * @return immutable list of fully qualified class name
     */
    public List<String> getClassNames() {
        return classNames;
    }

    /**
     * Returns the reason for being unsupported by Via.
     *
     * @return reason for being unsupported
     */
    public String getReason() {
        return reason;
    }

    /**
     * Returns whether at least one of the held class names exists.
     *
     * @return true if at least one of the classes exists
     */
    public boolean findMatch() {
        for (String className : classNames) {
            try {
                Class.forName(className);
                return true;
            } catch (ClassNotFoundException ignored) {
            }
        }
        return false;
    }

    public static final class Reason {

        public static final String DANGEROUS_SERVER_SOFTWARE = "You are using server software that - outside of possibly breaking ViaVersion - can also cause severe damage to your server's integrity as a whole.";
    }
}

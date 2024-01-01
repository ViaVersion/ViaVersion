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
package com.viaversion.viaversion.exception;

import java.util.HashMap;
import java.util.Map;

public class InformativeException extends Exception {
    private final Map<String, Object> info = new HashMap<>();
    private boolean shouldBePrinted = true;
    private int sources;

    public InformativeException(Throwable cause) {
        super(cause);
    }

    public InformativeException set(String key, Object value) {
        info.put(key, value);
        return this;
    }

    public InformativeException addSource(Class<?> sourceClazz) {
        return set("Source " + sources++, getSource(sourceClazz));
    }

    private String getSource(Class<?> sourceClazz) {
        return sourceClazz.isAnonymousClass() ? sourceClazz.getName() + " (Anonymous)" : sourceClazz.getName();
    }

    public boolean shouldBePrinted() {
        return shouldBePrinted;
    }

    public void setShouldBePrinted(final boolean shouldBePrinted) {
        this.shouldBePrinted = shouldBePrinted;
    }

    @Override
    public String getMessage() {
        StringBuilder builder = new StringBuilder("Please report this on the Via support Discord or open an issue on the relevant GitHub repository\n");
        boolean first = true;
        for (Map.Entry<String, Object> entry : info.entrySet()) {
            if (!first) {
                builder.append(", ");
            }
            builder.append(entry.getKey()).append(": ").append(entry.getValue());
            first = false;
        }
        return builder.toString();
    }

    @Override
    public Throwable fillInStackTrace() {
        // Don't record this stack
        return this;
    }
}

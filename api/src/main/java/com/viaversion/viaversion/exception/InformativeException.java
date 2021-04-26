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
package com.viaversion.viaversion.exception;

import java.util.HashMap;
import java.util.Map;

public class InformativeException extends Exception {
    private final Map<String, Object> info = new HashMap<>();
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
        if (sourceClazz.isAnonymousClass()) {
            return sourceClazz.getName() + " (Anonymous)";
        } else {
            return sourceClazz.getName();
        }
    }

    @Override
    public String getMessage() {
        StringBuilder builder = new StringBuilder();
        builder.append("Please post this error to https://github.com/ViaVersion/ViaVersion/issues and follow the issue template\n{");
        int i = 0;
        for (Map.Entry<String, Object> entry : info.entrySet()) {
            builder.append(i == 0 ? "" : ", ").append(entry.getKey()).append(": ").append(entry.getValue().toString());
            i++;
        }
        builder.append("}\nActual Error: ");

        return builder.toString();
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        // Don't record this stack
        return this;
    }
}

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
package com.viaversion.viaversion.util;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.protocol.Protocol;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Wrapper class for logging messages with the protocol name. Should be created inside the {@link Protocol} implementation.
 */
public class ProtocolLogger {

    private final Logger logger;
    private final String name;

    public ProtocolLogger(final Class<? extends Protocol> protocol) {
        this(Via.getPlatform().getLogger(), protocol);
    }

    public ProtocolLogger(final Logger logger, final Class<? extends Protocol> protocol) {
        this.logger = logger;
        this.name = ProtocolUtil.toNiceName(protocol);
    }

    public void log(final Level level, final String msg) {
        logger.log(level, formatMessage(msg));
    }

    public void log(final Level level, final String msg, final Throwable thrown) {
        logger.log(level, formatMessage(msg), thrown);
    }

    public void warning(final String msg) {
        logger.warning(formatMessage(msg));
    }

    public void severe(final String msg) {
        logger.severe(formatMessage(msg));
    }

    private String formatMessage(final String msg) {
        return "(" + name + ") " + msg;
    }
}

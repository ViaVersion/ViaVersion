/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2024 ViaVersion and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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

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
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.viaversion.viaversion.util.ProtocolUtil.toNiceName;

/**
 * Utility for protocol-specific logging messages.
 */
public class LogUtil {
    public static final LogUtil INSTANCE = new LogUtil(Via.getPlatform().getLogger());

    private final Logger logger;

    public LogUtil(final Logger logger) {
        this.logger = logger;
    }

    public void conversionWarning(Class<? extends Protocol> protocol, String message, Object... args) {
        if (!Via.getConfig().isSuppressConversionWarnings() || Via.getManager().isDebug()) {
            logger.log(Level.WARNING, toNiceName(protocol) + ": " + message, args);
        }
    }

    public void conversionWarning(Class<? extends Protocol> protocol, String message, Throwable t) {
        if (!Via.getConfig().isSuppressConversionWarnings() || Via.getManager().isDebug()) {
            logger.log(Level.WARNING, toNiceName(protocol) + ": " + message, t);
        }
    }

    public void conversionWarning(Class<? extends Protocol> protocol, Supplier<String> message, Throwable t) {
        if (!Via.getConfig().isSuppressConversionWarnings() || Via.getManager().isDebug()) {
            logger.log(Level.WARNING, toNiceName(protocol) + ": " + message.get(), t);
        }
    }

    // ---------------------------------------------------------------------------------

    public void warning(Class<? extends Protocol> protocol, final String message, Object... args) {
        logger.log(Level.WARNING, toNiceName(protocol) + ": " + message, args);
    }

    public void error(Class<? extends Protocol> protocol, String message, Object... args) {
        logger.log(Level.SEVERE, toNiceName(protocol) + ": " + message, args);
    }

    public void error(Class<? extends Protocol> protocol, String message, Throwable t) {
        logger.log(Level.SEVERE, toNiceName(protocol) + ": " + message, t);
    }
}

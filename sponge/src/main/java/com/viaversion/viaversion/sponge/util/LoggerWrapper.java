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
package com.viaversion.viaversion.sponge.util;

import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import org.apache.logging.log4j.Logger;

public class LoggerWrapper extends java.util.logging.Logger {
    private final Logger base;

    public LoggerWrapper(Logger logger) {
        super("logger", null);
        this.base = logger;
    }

    @Override
    public void log(LogRecord record) {
        log(record.getLevel(), record.getMessage());
    }

    @Override
    public void log(Level level, String msg) {
        if (level == Level.FINE)
            base.debug(msg);
        else if (level == Level.WARNING)
            base.warn(msg);
        else if (level == Level.SEVERE)
            base.error(msg);
        else if (level == Level.INFO)
            base.info(msg);
        else
            base.trace(msg);
    }

    @Override
    public void log(Level level, String msg, Object param1) {
        if (level == Level.FINE)
            base.debug(msg, param1);
        else if (level == Level.WARNING)
            base.warn(msg, param1);
        else if (level == Level.SEVERE)
            base.error(msg, param1);
        else if (level == Level.INFO)
            base.info(msg, param1);
        else
            base.trace(msg, param1);
    }

    @Override
    public void log(Level level, String msg, Object[] params) {
        log(level, MessageFormat.format(msg, params)); // workaround not formatting correctly
    }

    @Override
    public void log(Level level, String msg, Throwable params) {
        if (level == Level.FINE)
            base.debug(msg, params);
        else if (level == Level.WARNING)
            base.warn(msg, params);
        else if (level == Level.SEVERE)
            base.error(msg, params);
        else if (level == Level.INFO)
            base.info(msg, params);
        else
            base.trace(msg, params);
    }

}

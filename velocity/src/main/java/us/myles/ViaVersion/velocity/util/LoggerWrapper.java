package us.myles.ViaVersion.velocity.util;

import org.slf4j.Logger;

import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.LogRecord;

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

package us.myles.ViaVersion.sponge;

import org.slf4j.Logger;

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
        if (level == Level.FINEST) {
            base.trace(msg);
            return;
        }
        if (level == Level.FINE) {
            base.debug(msg);
            return;
        }
        if (level == Level.WARNING) {
            base.warn(msg);
            return;
        }
        if (level == Level.SEVERE) {
            base.error(msg);
            return;
        }
        if (level == Level.INFO) {
            base.info(msg);
            return;
        }
        base.trace(msg);
        return;
    }

    @Override
    public void log(Level level, String msg, Object param1) {
        if (level == Level.FINEST) {
            base.trace(msg, param1);
            return;
        }
        if (level == Level.FINE) {
            base.debug(msg, param1);
            return;
        }
        if (level == Level.WARNING) {
            base.warn(msg, param1);
            return;
        }
        if (level == Level.SEVERE) {
            base.error(msg, param1);
            return;
        }
        if (level == Level.INFO) {
            base.info(msg, param1);
            return;
        }
        base.trace(msg, param1);
        return;
    }

    @Override
    public void log(Level level, String msg, Object[] params) {
        if (level == Level.FINEST) {
            base.trace(msg, params);
            return;
        }
        if (level == Level.FINE) {
            base.debug(msg, params);
            return;
        }
        if (level == Level.WARNING) {
            base.warn(msg, params);
            return;
        }
        if (level == Level.SEVERE) {
            base.error(msg, params);
            return;
        }
        if (level == Level.INFO) {
            base.info(msg, params);
            return;
        }
        base.trace(msg, params);
        return;
    }

    @Override
    public void log(Level level, String msg, Throwable params) {
        if (level == Level.FINEST) {
            base.trace(msg, params);
            return;
        }
        if (level == Level.FINE) {
            base.debug(msg, params);
            return;
        }
        if (level == Level.WARNING) {
            base.warn(msg, params);
            return;
        }
        if (level == Level.SEVERE) {
            base.error(msg, params);
            return;
        }
        if (level == Level.INFO) {
            base.info(msg, params);
            return;
        }
        base.trace(msg, params);
        return;
    }

}

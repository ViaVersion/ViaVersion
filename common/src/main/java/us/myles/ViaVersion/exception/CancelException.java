package us.myles.ViaVersion.exception;

import us.myles.ViaVersion.api.Via;

/**
 * Used for cancelling packets.
 */
public class CancelException extends Exception {
    public static final CancelException CACHED = new CancelException("Cached - Enable /viaver debug to not use cached exception") {
        @Override
        public Throwable fillInStackTrace() {
            return this;
        }
    };

    public CancelException() {
    }

    public CancelException(String message) {
        super(message);
    }

    public CancelException(String message, Throwable cause) {
        super(message, cause);
    }

    public CancelException(Throwable cause) {
        super(cause);
    }

    public CancelException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    /**
     * Returns a cached CancelException or a new instance when {@link us.myles.ViaVersion.ViaManager#isDebug()} is true.
     *
     * @return a CancelException instance
     */
    public static CancelException generate() {
        return Via.getManager().isDebug() ? new CancelException() : CACHED;
    }
}

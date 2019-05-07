package us.myles.ViaVersion.exception;

public class CancelException extends Exception {
    public static final CancelException CACHED = new CancelException("Cached - Enable /viaver debug to not use cached exception") {
        @Override
        public synchronized Throwable fillInStackTrace() {
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
}

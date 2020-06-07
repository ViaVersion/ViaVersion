package us.myles.ViaVersion.exception;

import io.netty.handler.codec.EncoderException;
import us.myles.ViaVersion.api.Via;

/**
 * Used for cancelling packets in encode handlers
 */
public class CancelEncoderException extends EncoderException {
    public static final CancelEncoderException CACHED = new CancelEncoderException("CACHED") {
        @Override
        public synchronized Throwable fillInStackTrace() {
            return this;
        }
    };

    public CancelEncoderException() {
        super();
    }

    public CancelEncoderException(String message, Throwable cause) {
        super(message, cause);
    }

    public CancelEncoderException(String message) {
        super(message);
    }

    public CancelEncoderException(Throwable cause) {
        super(cause);
    }

    /**
     * Returns a cached CancelEncoderException or a new instance when {@link us.myles.ViaVersion.ViaManager}#isDebug() is true
     * @param cause cause for being used when a new instance is creeated
     * @return a CancelEncoderException instance
     */
    public static CancelEncoderException generate(Throwable cause) {
        if (Via.getManager().isDebug()) return new CancelEncoderException(cause);
        return CACHED;
    }
}

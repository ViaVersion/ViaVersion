package us.myles.ViaVersion.exception;

import io.netty.handler.codec.DecoderException;
import us.myles.ViaVersion.api.Via;

/**
 * Used for cancelling packets in decode handlers.
 */
public class CancelDecoderException extends DecoderException {
    public static final CancelDecoderException CACHED = new CancelDecoderException("CACHED") {
        @Override
        public Throwable fillInStackTrace() {
            return this;
        }
    };

    public CancelDecoderException() {
        super();
    }

    public CancelDecoderException(String message, Throwable cause) {
        super(message, cause);
    }

    public CancelDecoderException(String message) {
        super(message);
    }

    public CancelDecoderException(Throwable cause) {
        super(cause);
    }

    /**
     * Returns a cached CancelDecoderException or a new instance when {@link us.myles.ViaVersion.ViaManager#isDebug()} is true.
     *
     * @param cause cause for being used when a new instance is creeated
     * @return a CancelDecoderException instance
     */
    public static CancelDecoderException generate(Throwable cause) {
        return Via.getManager().isDebug() ? new CancelDecoderException(cause) : CACHED;
    }
}

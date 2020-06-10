package us.myles.ViaVersion.exception;

import io.netty.handler.codec.EncoderException;
import us.myles.ViaVersion.api.Via;

/**
 * Thrown during packet encoding when an outgoing packet should be cancelled.
 * Specifically extends {@link EncoderException} to prevent netty from wrapping the exception.
 */
public class CancelEncoderException extends EncoderException implements CancelCodecException {
    public static final CancelEncoderException CACHED = new CancelEncoderException("This packet is supposed to be cancelled; If you have debug enabled, you can ignore these") {
        @Override
        public Throwable fillInStackTrace() {
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
     * Returns a cached CancelEncoderException or a new instance when {@link us.myles.ViaVersion.ViaManager#isDebug()} is true.
     *
     * @param cause cause for being used when a new instance is creeated
     * @return a CancelEncoderException instance
     */
    public static CancelEncoderException generate(Throwable cause) {
        return Via.getManager().isDebug() ? new CancelEncoderException(cause) : CACHED;
    }
}

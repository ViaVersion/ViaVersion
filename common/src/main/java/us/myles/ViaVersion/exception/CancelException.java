package us.myles.ViaVersion.exception;

import io.netty.handler.codec.CodecException;
import us.myles.ViaVersion.api.Via;

/**
 * Thrown during packet transformation to cancel the packet.
 * Internally catched to then throw the appropriate {@link CodecException} for Netty's handler.
 */
public class CancelException extends Exception {
    public static final CancelException CACHED = new CancelException("This packet is supposed to be cancelled; If you have debug enabled, you can ignore these") {
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

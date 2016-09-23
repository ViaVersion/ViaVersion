package us.myles.ViaVersion.api.type;

import io.netty.buffer.ByteBuf;

public interface ByteBufWriter<T> {
    /**
     * Write an object to a type to a ByteBuf
     *
     * @param buffer The buffer to write to
     * @param object The object to write
     * @throws Exception Throws if it failed to write
     */
    void write(ByteBuf buffer, T object) throws Exception;
}

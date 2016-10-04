package us.myles.ViaVersion.api.type;

import io.netty.buffer.ByteBuf;

public interface ByteBufReader<T> {
    /**
     * Read a value from a ByteBuf
     *
     * @param buffer The buffer to read from.
     * @return The type based on the class type.
     * @throws Exception Throws exception if it failed reading.
     */
    T read(ByteBuf buffer) throws Exception;
}

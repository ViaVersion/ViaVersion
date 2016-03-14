package us.myles.ViaVersion2.api.type;

import io.netty.buffer.ByteBuf;

public interface ByteBufReader<T> {
    public T read(ByteBuf buffer) throws Exception;
}

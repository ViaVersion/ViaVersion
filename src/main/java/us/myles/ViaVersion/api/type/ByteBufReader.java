package us.myles.ViaVersion.api.type;

import io.netty.buffer.ByteBuf;

public interface ByteBufReader<T> {
    T read(ByteBuf buffer) throws Exception;
}

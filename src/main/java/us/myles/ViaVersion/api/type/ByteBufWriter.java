package us.myles.ViaVersion.api.type;

import io.netty.buffer.ByteBuf;

public interface ByteBufWriter<T> {
    void write(ByteBuf buffer, T object) throws Exception;
}

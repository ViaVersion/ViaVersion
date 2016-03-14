package us.myles.ViaVersion2.api.type;

import io.netty.buffer.ByteBuf;

public interface ByteBufWriter<T> {
    public void write(ByteBuf buffer, T object) throws Exception;
}

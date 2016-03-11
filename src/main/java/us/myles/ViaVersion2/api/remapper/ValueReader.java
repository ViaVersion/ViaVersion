package us.myles.ViaVersion2.api.remapper;

import io.netty.buffer.ByteBuf;
import us.myles.ViaVersion2.api.PacketWrapper;

public interface ValueReader<T> {
    public T read(PacketWrapper wrapper);
}

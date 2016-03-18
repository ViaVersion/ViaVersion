package us.myles.ViaVersion.api.remapper;

import us.myles.ViaVersion.api.PacketWrapper;

public interface ValueReader<T> {
    T read(PacketWrapper wrapper) throws Exception;
}

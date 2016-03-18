package us.myles.ViaVersion.api.remapper;

import us.myles.ViaVersion.api.PacketWrapper;

public interface ValueWriter<T> {
    void write(PacketWrapper writer, T inputValue) throws Exception;
}

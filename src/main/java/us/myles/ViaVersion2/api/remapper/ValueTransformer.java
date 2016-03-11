package us.myles.ViaVersion2.api.remapper;

import us.myles.ViaVersion2.api.PacketWrapper;

public interface ValueTransformer<T> {
    public void write(PacketWrapper writer, T inputValue);
}

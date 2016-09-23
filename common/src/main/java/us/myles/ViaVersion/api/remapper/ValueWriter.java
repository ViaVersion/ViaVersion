package us.myles.ViaVersion.api.remapper;

import us.myles.ViaVersion.api.PacketWrapper;

public interface ValueWriter<T> {
    /**
     * Write a value to a packet
     *
     * @param writer     The packet wrapper to write to
     * @param inputValue The value to write
     * @throws Exception Throws exception if it fails to write
     */
    void write(PacketWrapper writer, T inputValue) throws Exception;
}

package us.myles.ViaVersion.api.remapper;

import us.myles.ViaVersion.api.PacketWrapper;

public interface ValueReader<T> {
    /**
     * Reads value from a PacketWrapper
     *
     * @param wrapper The wrapper to read from
     * @return Returns the desired type
     * @throws Exception Throws exception if it fails to read
     */
    T read(PacketWrapper wrapper) throws Exception;
}

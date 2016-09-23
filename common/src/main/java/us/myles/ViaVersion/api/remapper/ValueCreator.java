package us.myles.ViaVersion.api.remapper;

import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.exception.InformativeException;

public abstract class ValueCreator implements ValueWriter {
    /**
     * Write new values to a Packet.
     *
     * @param wrapper The packet to write to
     * @throws Exception Throws exception if it fails to write.
     */
    public abstract void write(PacketWrapper wrapper) throws Exception;

    @Override
    public void write(PacketWrapper writer, Object inputValue) throws Exception {
        try {
            write(writer);
        } catch (InformativeException e) {
            e.addSource(this.getClass());
            throw e;
        }
    }
}

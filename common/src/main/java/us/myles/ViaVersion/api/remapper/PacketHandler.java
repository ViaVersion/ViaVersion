package us.myles.ViaVersion.api.remapper;

import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.exception.InformativeException;

public abstract class PacketHandler implements ValueWriter {
    /**
     * Handle a packet
     *
     * @param wrapper The associated wrapper
     * @throws Exception Throws exception if it failed to handle the packet
     */
    public abstract void handle(PacketWrapper wrapper) throws Exception;

    @Override
    public void write(PacketWrapper writer, Object inputValue) throws Exception {
        try {
            handle(writer);
        } catch (InformativeException e) {
            e.addSource(this.getClass());
            throw e;
        }
    }
}

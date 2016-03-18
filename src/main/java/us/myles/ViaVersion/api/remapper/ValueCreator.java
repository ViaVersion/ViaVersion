package us.myles.ViaVersion.api.remapper;

import us.myles.ViaVersion.api.PacketWrapper;

public abstract class ValueCreator implements ValueWriter {
    public abstract void write(PacketWrapper wrapper) throws Exception;

    @Override
    public void write(PacketWrapper writer, Object inputValue) throws Exception {
        write(writer);
    }
}

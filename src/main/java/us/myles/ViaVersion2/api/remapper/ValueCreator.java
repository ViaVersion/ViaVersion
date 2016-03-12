package us.myles.ViaVersion2.api.remapper;

import us.myles.ViaVersion2.api.PacketWrapper;

public abstract class ValueCreator implements ValueWriter {
    public abstract void write(PacketWrapper wrapper);

    @Override
    public void write(PacketWrapper writer, Object inputValue) {
        write(writer);
    }
}

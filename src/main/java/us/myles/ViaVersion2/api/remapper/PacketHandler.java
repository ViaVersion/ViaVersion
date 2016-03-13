package us.myles.ViaVersion2.api.remapper;

import us.myles.ViaVersion2.api.PacketWrapper;

public abstract class PacketHandler implements ValueWriter {
    public abstract void handle(PacketWrapper wrapper);

    @Override
    public void write(PacketWrapper writer, Object inputValue) {
        handle(writer);
    }
}

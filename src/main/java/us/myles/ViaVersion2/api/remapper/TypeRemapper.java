package us.myles.ViaVersion2.api.remapper;

import us.myles.ViaVersion2.api.PacketWrapper;
import us.myles.ViaVersion2.api.type.Type;

public class TypeRemapper<T> implements ValueReader<T>, ValueWriter<T> {
    private final Type<T> type;

    public TypeRemapper(Type<T> type) {
        this.type = type;
    }

    @Override
    public T read(PacketWrapper wrapper) throws Exception {
        return wrapper.read(type);
    }

    @Override
    public void write(PacketWrapper output, T inputValue) {
        output.write(type, inputValue);
    }
}

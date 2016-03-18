package us.myles.ViaVersion.api.remapper;

import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.type.Type;

public abstract class ValueTransformer<T1, T2> implements ValueWriter<T1> {
    private final Type<T2> outputType;

    public ValueTransformer(Type<T2> outputType) {
        this.outputType = outputType;
    }

    public abstract T2 transform(PacketWrapper wrapper, T1 inputValue) throws Exception;

    @Override
    public void write(PacketWrapper writer, T1 inputValue) throws Exception {
        writer.write(outputType, transform(writer, inputValue));
    }
}

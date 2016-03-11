package us.myles.ViaVersion2.api.type.types;

import io.netty.buffer.ByteBuf;
import us.myles.ViaVersion2.api.type.Type;

public class DoubleType extends Type<Double> {
    public DoubleType() {
        super(Double.class);
    }

    @Override
    public Double read(ByteBuf buffer) {
        return buffer.readDouble();
    }

    @Override
    public void write(ByteBuf buffer, Double object) {
        buffer.writeDouble(object);
    }
}

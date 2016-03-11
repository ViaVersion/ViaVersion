package us.myles.ViaVersion2.api.type.types;

import io.netty.buffer.ByteBuf;
import us.myles.ViaVersion2.api.type.Type;

public class FloatType extends Type<Float> {
    public FloatType() {
        super(Float.class);
    }

    @Override
    public Float read(ByteBuf buffer) {
        return buffer.readFloat();
    }

    @Override
    public void write(ByteBuf buffer, Float object) {
        buffer.writeFloat(object);
    }
}

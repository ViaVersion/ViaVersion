package us.myles.ViaVersion2.api.type.types;

import io.netty.buffer.ByteBuf;
import us.myles.ViaVersion2.api.type.Type;

public class BooleanType extends Type<Boolean> {
    public BooleanType() {
        super(Boolean.class);
    }

    @Override
    public Boolean read(ByteBuf buffer) {
        return buffer.readBoolean();
    }

    @Override
    public void write(ByteBuf buffer, Boolean object) {
        buffer.writeBoolean(object);
    }
}

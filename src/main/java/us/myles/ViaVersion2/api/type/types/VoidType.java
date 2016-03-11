package us.myles.ViaVersion2.api.type.types;

import io.netty.buffer.ByteBuf;
import us.myles.ViaVersion2.api.type.Type;

public class VoidType extends Type<Void> {
    public VoidType() {
        super(Void.class);
    }

    @Override
    public Void read(ByteBuf buffer) {
        return null;
    }

    @Override
    public void write(ByteBuf buffer, Void object) {

    }
}

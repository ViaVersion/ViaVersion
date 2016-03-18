package us.myles.ViaVersion.api.type.types;

import io.netty.buffer.ByteBuf;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.api.type.TypeConverter;

public class VoidType extends Type<Void> implements TypeConverter<Void> {
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

    @Override
    public Void from(Object o) {
        return null;
    }
}

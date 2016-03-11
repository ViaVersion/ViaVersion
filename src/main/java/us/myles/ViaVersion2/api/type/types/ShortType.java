package us.myles.ViaVersion2.api.type.types;

import io.netty.buffer.ByteBuf;
import us.myles.ViaVersion2.api.type.Type;

public class ShortType extends Type<Short> {
    public ShortType() {
        super(Short.class);
    }

    @Override
    public Short read(ByteBuf buffer) {
        return buffer.readShort();
    }

    @Override
    public void write(ByteBuf buffer, Short object) {
        buffer.writeShort(object);
    }
}

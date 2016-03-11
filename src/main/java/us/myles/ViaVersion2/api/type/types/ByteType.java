package us.myles.ViaVersion2.api.type.types;

import io.netty.buffer.ByteBuf;
import us.myles.ViaVersion2.api.type.Type;

public class ByteType extends Type<Byte> {
    public ByteType() {
        super(Byte.class);
    }

    @Override
    public Byte read(ByteBuf buffer) {
        return buffer.readByte();
    }

    @Override
    public void write(ByteBuf buffer, Byte object) {
        buffer.writeByte(object);
    }
}

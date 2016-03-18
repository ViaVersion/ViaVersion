package us.myles.ViaVersion.api.type.types;

import io.netty.buffer.ByteBuf;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.api.type.TypeConverter;

public class ByteType extends Type<Byte> implements TypeConverter<Byte> {
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


    @Override
    public Byte from(Object o) {
        if (o instanceof Number) {
            return ((Number) o).byteValue();
        }
        if (o instanceof Boolean) {
            return (Boolean) o ? (byte) 1 : 0;
        }
        return (Byte) o;
    }
}

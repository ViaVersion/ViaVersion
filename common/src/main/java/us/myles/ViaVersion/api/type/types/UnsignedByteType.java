package us.myles.ViaVersion.api.type.types;

import io.netty.buffer.ByteBuf;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.api.type.TypeConverter;

public class UnsignedByteType extends Type<Short> implements TypeConverter<Short> {
    public UnsignedByteType() {
        super("Unsigned Byte", Short.class);
    }

    @Override
    public Short read(ByteBuf buffer) {
        return buffer.readUnsignedByte();
    }

    @Override
    public void write(ByteBuf buffer, Short object) {
        buffer.writeByte(object);
    }

    @Override
    public Short from(Object o) {
        if (o instanceof Number) {
            return ((Number) o).shortValue();
        }
        if (o instanceof Boolean) {
            return ((Boolean) o) ? (short) 1 : 0;
        }
        return (short) o;
    }
}

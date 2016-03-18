package us.myles.ViaVersion.api.type.types;

import io.netty.buffer.ByteBuf;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.api.type.TypeConverter;

public class UnsignedShortType extends Type<Integer> implements TypeConverter<Integer> {
    public UnsignedShortType() {
        super(Integer.class);
    }

    @Override
    public Integer read(ByteBuf buffer) {
        return buffer.readUnsignedShort();
    }

    @Override
    public void write(ByteBuf buffer, Integer object) {
        buffer.writeShort(object);
    }

    @Override
    public Integer from(Object o) {
        if (o instanceof Number) {
            return ((Number) o).intValue();
        }
        if (o instanceof Boolean) {
            return ((Boolean) o) ? 1 : 0;
        }
        return (Integer) o;
    }
}

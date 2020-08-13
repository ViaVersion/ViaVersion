package us.myles.ViaVersion.api.type.types;

import io.netty.buffer.ByteBuf;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.api.type.TypeConverter;

public class ShortType extends Type<Short> implements TypeConverter<Short> {

    public ShortType() {
        super(Short.class);
    }

    public short readPrimitive(ByteBuf buffer) {
        return buffer.readShort();
    }

    public void writePrimitive(ByteBuf buffer, short object) {
        buffer.writeShort(object);
    }

    /**
     * @deprecated use {@link #readPrimitive(ByteBuf)} for manual reading to avoid wrapping
     */
    @Override
    @Deprecated
    public Short read(ByteBuf buffer) {
        return buffer.readShort();
    }

    /**
     * @deprecated use {@link #writePrimitive(ByteBuf, short)} for manual reading to avoid wrapping
     */
    @Override
    @Deprecated
    public void write(ByteBuf buffer, Short object) {
        buffer.writeShort(object);
    }

    @Override
    public Short from(Object o) {
        if (o instanceof Number) {
            return ((Number) o).shortValue();
        }
        if (o instanceof Boolean) {
            return ((Boolean) o) ? (short) 1 : 0;
        }
        return (Short) o;
    }
}

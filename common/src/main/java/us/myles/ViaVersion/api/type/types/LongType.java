package us.myles.ViaVersion.api.type.types;

import io.netty.buffer.ByteBuf;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.api.type.TypeConverter;

public class LongType extends Type<Long> implements TypeConverter<Long> {

    public LongType() {
        super(Long.class);
    }

    /**
     * @deprecated use {@link #readPrimitive(ByteBuf)} for manual reading to avoid wrapping
     */
    @Override
    @Deprecated
    public Long read(ByteBuf buffer) {
        return buffer.readLong();
    }

    /**
     * @deprecated use {@link #readPrimitive(ByteBuf)} for manual reading to avoid wrapping
     */
    @Override
    @Deprecated
    public void write(ByteBuf buffer, Long object) {
        buffer.writeLong(object);
    }

    @Override
    public Long from(Object o) {
        if (o instanceof Number) {
            return ((Number) o).longValue();
        }
        if (o instanceof Boolean) {
            return ((Boolean) o) ? 1L : 0;
        }
        return (Long) o;
    }

    public long readPrimitive(ByteBuf buffer) {
        return buffer.readLong();
    }

    public void writePrimitive(ByteBuf buffer, long object) {
        buffer.writeLong(object);
    }
}

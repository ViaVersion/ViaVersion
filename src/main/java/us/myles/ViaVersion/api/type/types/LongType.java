package us.myles.ViaVersion.api.type.types;

import io.netty.buffer.ByteBuf;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.api.type.TypeConverter;

public class LongType extends Type<Long> implements TypeConverter<Long> {
    public LongType() {
        super(Long.class);
    }

    @Override
    public Long read(ByteBuf buffer) {
        return buffer.readLong();
    }

    @Override
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
}

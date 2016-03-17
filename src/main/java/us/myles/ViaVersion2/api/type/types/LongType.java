package us.myles.ViaVersion2.api.type.types;

import io.netty.buffer.ByteBuf;
import us.myles.ViaVersion2.api.type.Type;
import us.myles.ViaVersion2.api.type.TypeConverter;

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
        if(o instanceof Boolean){
            return ((Boolean)o) == true ? 1L : 0;
        }
        return (Long) o;
    }
}

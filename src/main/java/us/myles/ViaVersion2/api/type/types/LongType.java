package us.myles.ViaVersion2.api.type.types;

import io.netty.buffer.ByteBuf;
import us.myles.ViaVersion2.api.type.Type;

public class LongType extends Type<Long> {
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
}

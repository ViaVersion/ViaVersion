package us.myles.ViaVersion.api.type.types;

import io.netty.buffer.ByteBuf;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.api.type.TypeConverter;

public class VarLongType extends Type<Long> implements TypeConverter<Long> {

    public VarLongType() {
        super("VarLong", Long.class);
    }

    @Override
    public void write(ByteBuf buffer, Long object) {
        int part;
        while (true) {
            part = (int) (object & 0x7F);

            object >>>= 7;
            if (object != 0) {
                part |= 0x80;
            }

            buffer.writeByte(part);

            if (object == 0) {
                break;
            }
        }
    }

    @Override
    public Long read(ByteBuf buffer) {
        long out = 0;
        int bytes = 0;
        byte in;
        while (true) {
            in = buffer.readByte();

            out |= (in & 0x7F) << (bytes++ * 7);

            if (bytes > 10) { // 10 is maxBytes
                throw new RuntimeException("VarLong too big");
            }

            if ((in & 0x80) != 0x80) {
                break;
            }
        }

        return out;
    }


    @Override
    public Long from(Object o) {
        if (o instanceof Number) {
            return ((Number) o).longValue();
        }
        if (o instanceof Boolean) {
            return ((Boolean) o) ? 1L : 0L;
        }
        return (Long) o;
    }
}
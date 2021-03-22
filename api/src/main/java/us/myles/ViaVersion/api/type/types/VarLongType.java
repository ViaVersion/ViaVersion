package us.myles.ViaVersion.api.type.types;

import io.netty.buffer.ByteBuf;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.api.type.TypeConverter;

public class VarLongType extends Type<Long> implements TypeConverter<Long> {

    public VarLongType() {
        super("VarLong", Long.class);
    }

    public long readPrimitive(ByteBuf buffer) {
        long out = 0;
        int bytes = 0;
        byte in;
        do {
            in = buffer.readByte();

            out |= (long) (in & 0x7F) << (bytes++ * 7);

            if (bytes > 10) { // 10 is maxBytes
                throw new RuntimeException("VarLong too big");
            }
        } while ((in & 0x80) == 0x80);
        return out;
    }

    public void writePrimitive(ByteBuf buffer, long object) {
        int part;
        do {
            part = (int) (object & 0x7F);

            object >>>= 7;
            if (object != 0) {
                part |= 0x80;
            }

            buffer.writeByte(part);
        } while (object != 0);
    }

    /**
     * @deprecated use {@link #readPrimitive(ByteBuf)} for manual reading to avoid wrapping
     */
    @Override
    @Deprecated
    public Long read(ByteBuf buffer) {
        return readPrimitive(buffer);
    }

    /**
     * @deprecated use {@link #writePrimitive(ByteBuf, long)} for manual reading to avoid wrapping
     */
    @Override
    @Deprecated
    public void write(ByteBuf buffer, Long object) {
        writePrimitive(buffer, object);
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
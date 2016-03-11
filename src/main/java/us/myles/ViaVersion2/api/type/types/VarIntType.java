package us.myles.ViaVersion2.api.type.types;

import io.netty.buffer.ByteBuf;
import us.myles.ViaVersion2.api.type.Type;

public class VarIntType extends Type<Integer>{

    public VarIntType() {
        super("VarInt", Integer.class);
    }

    @Override
    public void write(ByteBuf buffer, Integer object) {
        int part;
        while (true) {
            part = object & 0x7F;

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
    public Integer read(ByteBuf buffer) {
        int out = 0;
        int bytes = 0;
        byte in;
        while (true) {
            in = buffer.readByte();

            out |= (in & 0x7F) << (bytes++ * 7);

            if (bytes > 5) { // 5 is maxBytes
                throw new RuntimeException("VarInt too big");
            }

            if ((in & 0x80) != 0x80) {
                break;
            }
        }

        return out;
    }
}
package us.myles.ViaVersion2.api.type.types;

import io.netty.buffer.ByteBuf;
import us.myles.ViaVersion2.api.type.Type;

public class IntType extends Type<Integer> {
    public IntType() {
        super(Integer.class);
    }

    @Override
    public Integer read(ByteBuf buffer) {
        return buffer.readInt();
    }

    @Override
    public void write(ByteBuf buffer, Integer object) {
        buffer.writeInt(object);
    }
}

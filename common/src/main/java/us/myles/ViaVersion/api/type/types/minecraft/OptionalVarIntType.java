package us.myles.ViaVersion.api.type.types.minecraft;

import io.netty.buffer.ByteBuf;
import us.myles.ViaVersion.api.type.Type;

public class OptionalVarIntType extends Type<Integer> {
    public OptionalVarIntType() {
        super(Integer.class);
    }

    @Override
    public Integer read(ByteBuf buffer) throws Exception {
        int read = Type.VAR_INT.read(buffer);
        if (read == 0) return null;
        return read - 1;
    }

    @Override
    public void write(ByteBuf buffer, Integer object) throws Exception {
        if (object == null) Type.VAR_INT.write(buffer, 0);
        else Type.VAR_INT.write(buffer, object + 1);
    }
}

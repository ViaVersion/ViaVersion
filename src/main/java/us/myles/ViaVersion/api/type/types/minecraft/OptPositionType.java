package us.myles.ViaVersion.api.type.types.minecraft;

import io.netty.buffer.ByteBuf;
import us.myles.ViaVersion.api.minecraft.Position;
import us.myles.ViaVersion.api.type.Type;

public class OptPositionType extends Type<Position> {
    public OptPositionType() {
        super(Position.class);
    }

    @Override
    public Position read(ByteBuf buffer) throws Exception {
        boolean present = buffer.readBoolean();
        if (!present) return null;
        return Type.POSITION.read(buffer);
    }

    @Override
    public void write(ByteBuf buffer, Position object) throws Exception {
        buffer.writeBoolean(object != null);
        if (object != null)
            Type.POSITION.write(buffer, object);
    }
}

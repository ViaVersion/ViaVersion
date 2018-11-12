package us.myles.ViaVersion.api.type.types.minecraft;

import io.netty.buffer.ByteBuf;
import us.myles.ViaVersion.api.minecraft.Position;
import us.myles.ViaVersion.api.type.Type;

public class Position1_14Type extends Type<Position> {
    public Position1_14Type() {
        super(Position.class);
    }

    @Override
    public Position read(ByteBuf buffer) {
        long val = buffer.readLong();
        long x = (val >> 38);
        long y = val & 0xfff;
        long z = (((val << 38) >> 38)) >> 12;

        return new Position(x, y, z);
    }

    @Override
    public void write(ByteBuf buffer, Position object) {
        buffer.writeLong(((object.getX() & 0x3ffffff) << 38) | (object.getY() & 0xfff) | ((object.getZ() & 0x3ffffff) << 12));
    }
}

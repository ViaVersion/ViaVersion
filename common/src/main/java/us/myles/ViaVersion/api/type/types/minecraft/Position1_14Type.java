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
        long y = val << 52 >> 52;
        long z = val << 26 >> 38;

        return new Position((int) x, (short) y, (int) z);
    }

    @Override
    public void write(ByteBuf buffer, Position object) {
        buffer.writeLong((((long) object.getX() & 0x3ffffff) << 38)
                | (object.getY() & 0xfff)
                | ((((long) object.getZ()) & 0x3ffffff) << 12));
    }
}

package us.myles.ViaVersion2.api.type.types.minecraft;

import io.netty.buffer.ByteBuf;
import org.bukkit.util.Vector;
import us.myles.ViaVersion2.api.type.Type;

public class VectorType extends Type<Vector> {
    public VectorType() {
        super(Vector.class);
    }

    @Override
    public Vector read(ByteBuf buffer) {
        int x = Type.INT.read(buffer);
        int y = Type.INT.read(buffer);
        int z = Type.INT.read(buffer);

        return new Vector(x, y, z);
    }

    @Override
    public void write(ByteBuf buffer, Vector object) {
        Type.INT.write(buffer, object.getBlockX());
        Type.INT.write(buffer, object.getBlockY());
        Type.INT.write(buffer, object.getBlockZ());
    }
}

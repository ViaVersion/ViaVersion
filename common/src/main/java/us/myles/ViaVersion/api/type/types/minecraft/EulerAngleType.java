package us.myles.ViaVersion.api.type.types.minecraft;

import io.netty.buffer.ByteBuf;
import org.bukkit.util.EulerAngle;
import us.myles.ViaVersion.api.type.Type;

public class EulerAngleType extends Type<EulerAngle> {
    public EulerAngleType() {
        super(EulerAngle.class);
    }

    @Override
    public EulerAngle read(ByteBuf buffer) throws Exception {
        float x = Type.FLOAT.read(buffer);
        float y = Type.FLOAT.read(buffer);
        float z = Type.FLOAT.read(buffer);

        return new EulerAngle(x, y, z);
    }

    @Override
    public void write(ByteBuf buffer, EulerAngle object) throws Exception {
        Type.FLOAT.write(buffer, (float) object.getX());
        Type.FLOAT.write(buffer, (float) object.getY());
        Type.FLOAT.write(buffer, (float) object.getZ());
    }
}
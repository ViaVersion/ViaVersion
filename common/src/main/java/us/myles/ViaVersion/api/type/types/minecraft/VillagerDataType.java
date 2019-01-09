package us.myles.ViaVersion.api.type.types.minecraft;

import io.netty.buffer.ByteBuf;
import us.myles.ViaVersion.api.minecraft.VillagerData;
import us.myles.ViaVersion.api.type.Type;

public class VillagerDataType extends Type<VillagerData> {
    public VillagerDataType() {
        super(VillagerData.class);
    }

    @Override
    public VillagerData read(ByteBuf buffer) throws Exception {
        return new VillagerData(Type.VAR_INT.read(buffer), Type.VAR_INT.read(buffer), Type.VAR_INT.read(buffer));
    }

    @Override
    public void write(ByteBuf buffer, VillagerData object) throws Exception {
        Type.VAR_INT.write(buffer, object.getType());
        Type.VAR_INT.write(buffer, object.getProfession());
        Type.VAR_INT.write(buffer, object.getLevel());
    }
}

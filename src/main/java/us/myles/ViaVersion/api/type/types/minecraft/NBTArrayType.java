package us.myles.ViaVersion.api.type.types.minecraft;

import io.netty.buffer.ByteBuf;
import org.spacehq.opennbt.tag.builtin.CompoundTag;
import us.myles.ViaVersion.api.type.Type;

import java.util.ArrayList;
import java.util.List;

public class NBTArrayType extends Type<List<CompoundTag>> {
    public NBTArrayType() {
        super("NBT Array", List.class);
    }

    @Override
    public List<CompoundTag> read(ByteBuf buffer) throws Exception {
        int amount = Type.VAR_INT.read(buffer);

        List<CompoundTag> nbtData = new ArrayList<>();
        for (int i = 0; i < amount; i++)
            nbtData.add(Type.NBT.read(buffer));

        return nbtData;
    }

    @Override
    public void write(ByteBuf buffer, List<CompoundTag> list) throws Exception {
        Type.VAR_INT.write(buffer, list.size());

        for (CompoundTag tag : list)
            Type.NBT.write(buffer, tag);
    }
}

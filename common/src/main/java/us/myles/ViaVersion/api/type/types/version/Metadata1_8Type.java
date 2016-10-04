package us.myles.ViaVersion.api.type.types.version;


import io.netty.buffer.ByteBuf;
import us.myles.ViaVersion.api.minecraft.metadata.Metadata;
import us.myles.ViaVersion.api.minecraft.metadata.types.MetaType1_8;
import us.myles.ViaVersion.api.type.types.minecraft.MetaTypeTemplate;

public class Metadata1_8Type extends MetaTypeTemplate {

    @Override
    public Metadata read(ByteBuf buffer) throws Exception {
        byte item = buffer.readByte();
        if (item == 127) return null; // end of metadata
        int typeID = (item & 0xE0) >> 5;
        MetaType1_8 type = MetaType1_8.byId(typeID);
        int id = item & 0x1F;
        return new Metadata(id, type, type.getType().read(buffer));
    }

    @Override
    public void write(ByteBuf buffer, Metadata meta) throws Exception {
        byte item = (byte) (meta.getMetaType().getTypeID() << 5 | meta.getId() & 0x1F);
        buffer.writeByte(item);
        meta.getMetaType().getType().write(buffer, meta.getValue());
    }
}

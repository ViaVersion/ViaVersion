package us.myles.ViaVersion.protocols.protocol1_9to1_8.types;


import io.netty.buffer.ByteBuf;
import us.myles.ViaVersion.api.minecraft.metadata.Metadata;
import us.myles.ViaVersion.api.type.types.minecraft.MetaTypeTemplate;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.metadata.MetadataTypes;

public class Metadata1_8Type extends MetaTypeTemplate {

    @Override
    public Metadata read(ByteBuf buffer) throws Exception {
        byte item = buffer.readByte();
        if (item == 127) return null; // end of metadata
        int typeID = (item & 0xE0) >> 5;
        MetadataTypes type = MetadataTypes.byId(typeID);
        int id = item & 0x1F;
        return new Metadata(id, typeID, type.getType(), type.getType().read(buffer));
    }

    @Override
    public void write(ByteBuf buffer, Metadata object) throws Exception {
        throw new UnsupportedOperationException("1.8 Metadata writing is not implemented!");
    }
}

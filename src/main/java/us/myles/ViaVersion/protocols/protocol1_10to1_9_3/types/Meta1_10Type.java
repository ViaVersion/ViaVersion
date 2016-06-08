package us.myles.ViaVersion.protocols.protocol1_10to1_9_3.types;

import io.netty.buffer.ByteBuf;
import us.myles.ViaVersion.api.minecraft.metadata.Metadata;
import us.myles.ViaVersion.api.type.types.minecraft.MetaTypeTemplate;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.metadata.NewType;

public class Meta1_10Type extends MetaTypeTemplate {

    @Override
    public Metadata read(ByteBuf buffer) throws Exception {
        short index = buffer.readUnsignedByte();

        if (index == 0xff) return null; //End of metadata
        NewType type = NewType.byId(buffer.readByte());

        return new Metadata(index, type.getTypeID(), type.getType(), type.getType().read(buffer));
    }

    @Override
    public void write(ByteBuf buffer, Metadata object) throws Exception {
        if (object == null) {
            buffer.writeByte(255);
        } else {
            buffer.writeByte(object.getId());
            buffer.writeByte(object.getTypeID());
            object.getType().write(buffer, object.getValue());
        }
    }
}

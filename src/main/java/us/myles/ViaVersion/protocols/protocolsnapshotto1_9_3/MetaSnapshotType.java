package us.myles.ViaVersion.protocols.protocolsnapshotto1_9_3;

import io.netty.buffer.ByteBuf;
import us.myles.ViaVersion.api.minecraft.metadata.Metadata;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.metadata.NewType;

public class MetaSnapshotType extends Type<Metadata> {
    public MetaSnapshotType() {
        super(Metadata.class);
    }

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

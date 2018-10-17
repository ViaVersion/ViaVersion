package us.myles.ViaVersion.api.type.types.version;

import io.netty.buffer.ByteBuf;
import us.myles.ViaVersion.api.minecraft.metadata.Metadata;
import us.myles.ViaVersion.api.minecraft.metadata.types.MetaType1_13_2;
import us.myles.ViaVersion.api.type.types.minecraft.MetaTypeTemplate;

public class Metadata1_13_2Type extends MetaTypeTemplate {
	@Override
	public Metadata read(ByteBuf buffer) throws Exception {
		short index = buffer.readUnsignedByte();

		if (index == 0xff) return null; //End of metadata
		MetaType1_13_2 type = MetaType1_13_2.byId(buffer.readByte());

		return new Metadata(index, type, type.getType().read(buffer));
	}

	@Override
	public void write(ByteBuf buffer, Metadata object) throws Exception {
		if (object == null) {
			buffer.writeByte(255);
		} else {
			buffer.writeByte(object.getId());
			buffer.writeByte(object.getMetaType().getTypeID());
			object.getMetaType().getType().write(buffer, object.getValue());
		}
	}
}

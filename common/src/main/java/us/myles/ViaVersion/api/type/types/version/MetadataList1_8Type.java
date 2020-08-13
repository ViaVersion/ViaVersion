package us.myles.ViaVersion.api.type.types.version;

import io.netty.buffer.ByteBuf;
import us.myles.ViaVersion.api.minecraft.metadata.Metadata;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.api.type.types.minecraft.AbstractMetaListType;

public class MetadataList1_8Type extends AbstractMetaListType {
    @Override
    protected Type<Metadata> getType() {
        return Types1_8.METADATA;
    }

    @Override
    protected void writeEnd(final Type<Metadata> type, final ByteBuf buffer) throws Exception {
        buffer.writeByte(0x7f);
    }
}

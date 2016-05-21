package us.myles.ViaVersion.protocols.protocol1_9to1_8.types;

import io.netty.buffer.ByteBuf;
import us.myles.ViaVersion.api.minecraft.metadata.Metadata;
import us.myles.ViaVersion.api.type.types.minecraft.MetaListTypeTemplate;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.Protocol1_9TO1_8;

import java.util.ArrayList;
import java.util.List;

public class MetadataListType extends MetaListTypeTemplate {

    @Override
    public List<Metadata> read(ByteBuf buffer) throws Exception {
        List<Metadata> list = new ArrayList<>();
        Metadata m;
        do {
            m = Protocol1_9TO1_8.METADATA.read(buffer);
            if (m != null) {
                list.add(m);
            }
        } while (m != null);

        return list;
    }

    @Override
    public void write(ByteBuf buffer, List<Metadata> object) throws Exception {
        for (Metadata m : object) {
            Protocol1_9TO1_8.METADATA.write(buffer, m);
        }
        // Write end of list
        Protocol1_9TO1_8.METADATA.write(buffer, null);
    }
}

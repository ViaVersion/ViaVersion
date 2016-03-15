package us.myles.ViaVersion2.api.protocol1_9to1_8.types;

import io.netty.buffer.ByteBuf;
import us.myles.ViaVersion2.api.metadata.Metadata;
import us.myles.ViaVersion2.api.protocol1_9to1_8.Protocol1_9TO1_8;
import us.myles.ViaVersion2.api.type.Type;

import java.util.ArrayList;
import java.util.List;

public class MetadataListType extends Type<List<Metadata>> {
    public MetadataListType() {
        super("Metadata List", List.class);
    }

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
            System.out.println("Writing meta data: " + m.getType().getTypeName() + " - " + m.getId() + " - " + m.getTypeID());
        }
        // Write end of list
        Protocol1_9TO1_8.METADATA.write(buffer, null);
    }
}

package us.myles.ViaVersion.protocols.protocol1_10to1_9_3.types;

import io.netty.buffer.ByteBuf;
import us.myles.ViaVersion.api.minecraft.metadata.Metadata;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.api.type.types.minecraft.MetaListTypeTemplate;
import us.myles.ViaVersion.protocols.protocol1_10to1_9_3.Protocol1_10To1_9_3_4;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.metadata.NewType;

import java.util.ArrayList;
import java.util.List;

public class MetaList1_10Type extends MetaListTypeTemplate {

    @Override
    public List<Metadata> read(ByteBuf buffer) throws Exception {
        List<Metadata> list = new ArrayList<>();
        Metadata meta;
        do {
            meta = Protocol1_10To1_9_3_4.METADATA.read(buffer);
            if (meta != null)
                list.add(meta);
        } while (meta != null);

        return list;
    }

    @Override
    public void write(ByteBuf buffer, List<Metadata> object) throws Exception {
        for (Metadata m : object) {
            if (m.getId() >= 5)
                m.setId(m.getId() + 1);
            if (m.getId() == 4)
                Protocol1_10To1_9_3_4.METADATA.write(buffer, new Metadata(5, NewType.Boolean.getTypeID(), Type.BOOLEAN, false)); // No gravity metadata
            Protocol1_10To1_9_3_4.METADATA.write(buffer, m);
        }

        // Write end of list
        Protocol1_10To1_9_3_4.METADATA.write(buffer, null);
    }
}

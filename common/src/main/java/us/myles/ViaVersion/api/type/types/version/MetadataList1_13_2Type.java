package us.myles.ViaVersion.api.type.types.version;

import io.netty.buffer.ByteBuf;
import us.myles.ViaVersion.api.minecraft.metadata.Metadata;
import us.myles.ViaVersion.api.type.types.minecraft.MetaListTypeTemplate;

import java.util.ArrayList;
import java.util.List;

public class MetadataList1_13_2Type extends MetaListTypeTemplate {
    @Override
    public List<Metadata> read(ByteBuf buffer) throws Exception {
        List<Metadata> list = new ArrayList<>();
        Metadata meta;
        do {
            meta = Types1_13_2.METADATA.read(buffer);
            if (meta != null)
                list.add(meta);
        } while (meta != null);

        return list;
    }

    @Override
    public void write(ByteBuf buffer, List<Metadata> object) throws Exception {
        for (Metadata m : object)
            Types1_13_2.METADATA.write(buffer, m);

        // Write end of list
        Types1_13_2.METADATA.write(buffer, null);
    }
}

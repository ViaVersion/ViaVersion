package us.myles.ViaVersion.api.type.types.minecraft;

import us.myles.ViaVersion.api.minecraft.metadata.Metadata;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.protocols.protocol1_10to1_9_3.types.Metadata1_9Type;
import us.myles.ViaVersion.protocols.protocol1_10to1_9_3.types.MetadataList1_9Type;

import java.util.List;

public class Types1_9 {
    /**
     * Metadata list type for 1.9
     */
    public static final Type<List<Metadata>> METADATA_LIST = new MetadataList1_9Type();

    /**
     * Metadata type for 1.9
     */
    public static final Type<Metadata> METADATA = new Metadata1_9Type();
}

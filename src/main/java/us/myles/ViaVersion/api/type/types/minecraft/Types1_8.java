package us.myles.ViaVersion.api.type.types.minecraft;

import us.myles.ViaVersion.api.minecraft.metadata.Metadata;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.types.Metadata1_8Type;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.types.MetadataList1_8Type;

import java.util.List;

public class Types1_8 {
    /**
     * Metadata list type for 1.8
     */
    public static Type<List<Metadata>> METADATA_LIST = new MetadataList1_8Type();

    /**
     * Metadata type for 1.8
     */
    public static Type<Metadata> METADATA = new Metadata1_8Type();
}

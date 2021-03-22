package us.myles.ViaVersion.api.type.types.version;

import us.myles.ViaVersion.api.minecraft.chunks.ChunkSection;
import us.myles.ViaVersion.api.minecraft.metadata.Metadata;
import us.myles.ViaVersion.api.type.Type;

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

    public static final Type<ChunkSection> CHUNK_SECTION = new ChunkSectionType1_9();
}

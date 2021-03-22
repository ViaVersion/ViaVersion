package us.myles.ViaVersion.api.type.types.version;

import us.myles.ViaVersion.api.minecraft.metadata.Metadata;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.api.type.types.Particle;
import us.myles.ViaVersion.api.type.types.minecraft.Particle1_17Type;

import java.util.List;

public class Types1_17 {

    public static final Type<List<Metadata>> METADATA_LIST = new MetadataList1_17Type();
    public static final Type<Metadata> METADATA = new Metadata1_17Type();
    public static final Type<Particle> PARTICLE = new Particle1_17Type();
}

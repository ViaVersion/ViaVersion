package us.myles.ViaVersion.api.type.types.minecraft;

import us.myles.ViaVersion.api.minecraft.metadata.Metadata;
import us.myles.ViaVersion.api.type.Type;

import java.util.List;

public abstract class MetaListTypeTemplate extends Type<List<Metadata>> {
    public MetaListTypeTemplate() {
        super("MetaData List", List.class);
    }

    @Override
    public Class<? extends Type> getBaseClass() {
        return MetaListTypeTemplate.class;
    }
}

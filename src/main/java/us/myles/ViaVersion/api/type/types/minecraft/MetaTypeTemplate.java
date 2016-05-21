package us.myles.ViaVersion.api.type.types.minecraft;

import us.myles.ViaVersion.api.minecraft.metadata.Metadata;
import us.myles.ViaVersion.api.type.Type;

public abstract class MetaTypeTemplate extends Type<Metadata> {
    public MetaTypeTemplate() {
        super("Metadata type", Metadata.class);
    }

    @Override
    public Class<? extends Type> getBaseClass() {
        return MetaTypeTemplate.class;
    }
}

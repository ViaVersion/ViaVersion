package us.myles.ViaVersion.api.type.types.version;

import us.myles.ViaVersion.api.minecraft.metadata.MetaType;
import us.myles.ViaVersion.api.minecraft.metadata.types.MetaType1_14;
import us.myles.ViaVersion.api.type.types.minecraft.ModernMetaType;

public class Metadata1_14Type extends ModernMetaType {
    @Override
    protected MetaType getType(final int index) {
        return MetaType1_14.byId(index);
    }
}

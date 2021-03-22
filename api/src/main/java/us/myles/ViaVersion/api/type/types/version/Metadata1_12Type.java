package us.myles.ViaVersion.api.type.types.version;

import us.myles.ViaVersion.api.minecraft.metadata.MetaType;
import us.myles.ViaVersion.api.minecraft.metadata.types.MetaType1_12;
import us.myles.ViaVersion.api.type.types.minecraft.ModernMetaType;

public class Metadata1_12Type extends ModernMetaType {
    @Override
    protected MetaType getType(final int index) {
        return MetaType1_12.byId(index);
    }
}

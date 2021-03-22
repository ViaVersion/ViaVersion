package us.myles.ViaVersion.api.type.types.minecraft;

import us.myles.ViaVersion.api.minecraft.item.Item;
import us.myles.ViaVersion.api.type.Type;

public abstract class BaseItemArrayType extends Type<Item[]> {
    public BaseItemArrayType() {
        super(Item[].class);
    }

    public BaseItemArrayType(String typeName) {
        super(typeName, Item[].class);
    }

    @Override
    public Class<? extends Type> getBaseClass() {
        return BaseItemArrayType.class;
    }
}

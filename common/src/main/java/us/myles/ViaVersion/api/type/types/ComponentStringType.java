package us.myles.ViaVersion.api.type.types;

import us.myles.ViaVersion.api.type.Type;

public class ComponentStringType extends StringType {

    public ComponentStringType() {
        super(262144);
    }

    @Override
    public Class<? extends Type> getBaseClass() {
        return StringType.class;
    }
}

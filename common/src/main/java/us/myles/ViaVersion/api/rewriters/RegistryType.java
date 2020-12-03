package us.myles.ViaVersion.api.rewriters;

public enum RegistryType {

    BLOCK,
    ITEM,
    FLUID,
    ENTITY,
    GAME_EVENT;

    private static final RegistryType[] VALUES = values();

    public static RegistryType[] getValues() {
        return VALUES;
    }
}

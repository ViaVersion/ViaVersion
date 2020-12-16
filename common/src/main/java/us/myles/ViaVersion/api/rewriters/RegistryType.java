package us.myles.ViaVersion.api.rewriters;

public enum RegistryType {

    BLOCK("block"),
    ITEM("item"),
    FLUID("fluid"),
    ENTITY("entity_type"),
    GAME_EVENT("game_event");

    public static RegistryType[] getValues() {
        return VALUES;
    }

    private static final RegistryType[] VALUES = values();

    private final String resourceLocation;

    RegistryType(final String resourceLocation) {
        this.resourceLocation = resourceLocation;
    }

    public String getResourceLocation() {
        return resourceLocation;
    }
}

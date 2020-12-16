package us.myles.ViaVersion.api.rewriters;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public enum RegistryType {

    BLOCK("block"),
    ITEM("item"),
    FLUID("fluid"),
    ENTITY("entity_type"),
    GAME_EVENT("game_event");

    private static final Map<String, RegistryType> MAP = new HashMap<>();
    private static final RegistryType[] VALUES = values();

    static {
        for (RegistryType type : getValues()) {
            MAP.put(type.resourceLocation, type);
        }
    }

    public static RegistryType[] getValues() {
        return VALUES;
    }

    @Nullable
    public static RegistryType getByKey(String resourceKey) {
        return MAP.get(resourceKey);
    }

    private final String resourceLocation;

    RegistryType(final String resourceLocation) {
        this.resourceLocation = resourceLocation;
    }

    public String getResourceLocation() {
        return resourceLocation;
    }
}

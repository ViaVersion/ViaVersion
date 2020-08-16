package us.myles.ViaVersion.protocols.protocol1_15to1_14_4.data;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;
import us.myles.ViaVersion.api.data.Mappings;

public class MappingData extends us.myles.ViaVersion.api.data.MappingData {

    public MappingData() {
        super("1.14", "1.15", true);
    }

    @Override
    protected Mappings loadFromArray(JsonObject oldMappings, JsonObject newMappings, @Nullable JsonObject diffMappings, String key) {
        if (!key.equals("sounds")) {
            return super.loadFromArray(oldMappings, newMappings, diffMappings, key);
        }

        // Ignore removed sounds
        return new Mappings(oldMappings.getAsJsonArray(key), newMappings.getAsJsonArray(key), false);
    }
}

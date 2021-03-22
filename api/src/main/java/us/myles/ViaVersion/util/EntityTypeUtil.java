package us.myles.ViaVersion.util;

import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.entities.EntityType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class EntityTypeUtil {

    public static EntityType[] toOrderedArray(EntityType[] values) {
        List<EntityType> types = new ArrayList<>();
        for (EntityType type : values) {
            if (type.getId() != -1) {
                types.add(type);
            }
        }

        types.sort(Comparator.comparingInt(EntityType::getId));
        return types.toArray(new EntityType[0]);
    }

    public static EntityType getTypeFromId(EntityType[] values, int typeId, EntityType fallback) {
        EntityType type;
        if (typeId < 0 || typeId >= values.length || (type = values[typeId]) == null) {
            Via.getPlatform().getLogger().severe("Could not find " + fallback.getClass().getSimpleName() + " type id " + typeId);
            return fallback;
        }
        return type;
    }
}

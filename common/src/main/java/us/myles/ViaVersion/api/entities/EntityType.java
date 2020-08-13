package us.myles.ViaVersion.api.entities;

import org.jetbrains.annotations.Nullable;

public interface EntityType {

    int getId();

    @Nullable
    EntityType getParent();

    String name();

    default boolean is(EntityType... types) {
        for (EntityType type : types)
            if (is(type))
                return true;
        return false;
    }

    default boolean is(EntityType type) {
        return this == type;
    }

    default boolean isOrHasParent(EntityType type) {
        EntityType parent = this;

        do {
            if (parent.equals(type))
                return true;

            parent = parent.getParent();
        } while (parent != null);

        return false;
    }
}

package us.myles.ViaVersion.api.entities;

import org.jetbrains.annotations.Nullable;

public interface EntityType {

    /**
     * @return entity id
     */
    int getId();

    /**
     * @return parent entity type if present
     */
    @Nullable
    EntityType getParent();

    String name();

    default boolean is(EntityType... types) {
        for (EntityType type : types)
            if (this == type) {
                return true;
            }
        return false;
    }

    default boolean is(EntityType type) {
        return this == type;
    }

    /**
     * @param type entity type to check against
     * @return true if the current type is equal to the given type, or has it as a parent type
     */
    default boolean isOrHasParent(EntityType type) {
        EntityType parent = this;

        do {
            if (parent == type) {
                return true;
            }

            parent = parent.getParent();
        } while (parent != null);

        return false;
    }
}

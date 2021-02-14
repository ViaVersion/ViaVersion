package us.myles.ViaVersion.common.entities;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import us.myles.ViaVersion.api.entities.Entity1_14Types;
import us.myles.ViaVersion.api.entities.Entity1_15Types;
import us.myles.ViaVersion.api.entities.Entity1_16Types;
import us.myles.ViaVersion.api.entities.Entity1_16_2Types;
import us.myles.ViaVersion.api.entities.Entity1_17Types;
import us.myles.ViaVersion.api.entities.EntityType;

import java.util.function.Function;

/**
 * Test to make sure the array storage approach of entity types works correctly.
 */
public class EntityTypesTest {

    @Test
    void testArrayOrder() {
        testArrayOrder(Entity1_14Types.values(), Entity1_14Types::getTypeFromId);
        testArrayOrder(Entity1_15Types.values(), Entity1_15Types::getTypeFromId);
        testArrayOrder(Entity1_16Types.values(), Entity1_16Types::getTypeFromId);
        testArrayOrder(Entity1_16_2Types.values(), Entity1_16_2Types::getTypeFromId);
        testArrayOrder(Entity1_17Types.values(), Entity1_17Types::getTypeFromId);
    }

    private void testArrayOrder(EntityType[] types, Function<Integer, EntityType> returnFunction) {
        for (EntityType type : types) {
            if (type.getId() != -1) {
                Assertions.assertEquals(type, returnFunction.apply(type.getId()));
            }
        }
    }
}

package us.myles.ViaVersion.api.entities;

import com.google.common.base.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import us.myles.ViaVersion.api.Via;


// todo 18w10d
public class Entity1_13Types {
    public static EntityType getTypeFromId(int typeID, boolean isObject) {
        Optional<EntityType> type;

        if (isObject)
            type = ObjectTypes.getPCEntity(typeID);
        else
            type = EntityType.findById(typeID);

        if (!type.isPresent()) {
            Via.getPlatform().getLogger().severe("Could not find type id " + typeID + " isObject=" + isObject);
            return EntityType.ENTITY; // Fall back to the basic ENTITY
        }

        return type.get();
    }

    @AllArgsConstructor
    @Getter
    public enum EntityType {
        ENTITY(-1),
        DROPPED_ITEM(30, ENTITY),
        EXPERIENCE_ORB(20, ENTITY),
        LEASH_HITCH(33, ENTITY), // Actually entity hanging but it doesn't make a lot of difference for metadata
        PAINTING(47, ENTITY), // Actually entity hanging but it doesn't make a lot of difference for metadata
        ARROW(2, ENTITY),
        SNOWBALL(65, ENTITY), // Actually EntityProjectile
        FIREBALL(32, ENTITY),
        SMALL_FIREBALL(63, ENTITY),
        ENDER_PEARL(72, ENTITY), // Actually EntityProjectile
        ENDER_SIGNAL(21, ENTITY),
        THROWN_EXP_BOTTLE(73, ENTITY),
        ITEM_FRAME(31, ENTITY), // Actually EntityHanging
        WITHER_SKULL(82, ENTITY),
        PRIMED_TNT(53, ENTITY),
        FALLING_BLOCK(22, ENTITY),
        FIREWORK(23, ENTITY),
        SPECTRAL_ARROW(66, ARROW),
        SHULKER_BULLET(58, ENTITY),
        DRAGON_FIREBALL(12, FIREBALL),
        EVOCATION_FANGS(18, ENTITY),


        ENTITY_LIVING(-1, ENTITY),
        ENTITY_INSENTIENT(-1, ENTITY_LIVING),
        ENTITY_AGEABLE(-1, ENTITY_INSENTIENT),
        ENTITY_TAMEABLE_ANIMAL(-1, ENTITY_AGEABLE),
        ENTITY_HUMAN(-1, ENTITY_LIVING),

        ARMOR_STAND(1, ENTITY_LIVING),
        ENTITY_ILLAGER_ABSTRACT(-1, ENTITY_INSENTIENT),
        EVOCATION_ILLAGER(19, ENTITY_ILLAGER_ABSTRACT),
        VEX(75, ENTITY_INSENTIENT),
        VINDICATION_ILLAGER(78, ENTITY_ILLAGER_ABSTRACT),
        ILLUSION_ILLAGER(29, EVOCATION_ILLAGER),

        // Vehicles
        MINECART_ABSTRACT(-1, ENTITY),
        MINECART_COMMAND(39, MINECART_ABSTRACT),
        BOAT(5, ENTITY),
        MINECART_RIDEABLE(37, MINECART_ABSTRACT),
        MINECART_CHEST(38, MINECART_ABSTRACT),
        MINECART_FURNACE(40, MINECART_ABSTRACT),
        MINECART_TNT(43, MINECART_ABSTRACT),
        MINECART_HOPPER(41, MINECART_ABSTRACT),
        MINECART_MOB_SPAWNER(42, MINECART_ABSTRACT),

        CREEPER(10, ENTITY_INSENTIENT),

        ABSTRACT_SKELETON(-1, ENTITY_INSENTIENT),
        SKELETON(60, ABSTRACT_SKELETON),
        WITHER_SKELETON(81, ABSTRACT_SKELETON),
        STRAY(69, ABSTRACT_SKELETON),

        SPIDER(67, ENTITY_INSENTIENT),
        GIANT(25, ENTITY_INSENTIENT),

        ZOMBIE(84, ENTITY_INSENTIENT),
        HUSK(28, ZOMBIE),
        ZOMBIE_VILLAGER(86, ZOMBIE),

        SLIME(62, ENTITY_INSENTIENT),
        GHAST(24, ENTITY_INSENTIENT),
        PIG_ZOMBIE(51, ZOMBIE),
        ENDERMAN(16, ENTITY_INSENTIENT),
        CAVE_SPIDER(6, SPIDER),
        SILVERFISH(59, ENTITY_INSENTIENT),
        BLAZE(4, ENTITY_INSENTIENT),
        MAGMA_CUBE(36, SLIME),
        ENDER_DRAGON(15, ENTITY_INSENTIENT),
        WITHER(80, ENTITY_INSENTIENT),
        BAT(3, ENTITY_INSENTIENT),
        WITCH(79, ENTITY_INSENTIENT),
        ENDERMITE(17, ENTITY_INSENTIENT),

        GUARDIAN(26, ENTITY_INSENTIENT),
        ELDER_GUARDIAN(13, GUARDIAN), // Moved down to avoid illegal forward reference

        IRON_GOLEM(77, ENTITY_INSENTIENT), // moved up to avoid illegal forward references
        SHULKER(57, IRON_GOLEM),
        PIG(49, ENTITY_AGEABLE),
        SHEEP(56, ENTITY_AGEABLE),
        COW(9, ENTITY_AGEABLE),
        CHICKEN(7, ENTITY_AGEABLE),
        SQUID(68, ENTITY_INSENTIENT),
        WOLF(83, ENTITY_TAMEABLE_ANIMAL),
        MUSHROOM_COW(45, COW),
        SNOWMAN(64, IRON_GOLEM),
        OCELOT(46, ENTITY_TAMEABLE_ANIMAL),
        PARROT(48, ENTITY_TAMEABLE_ANIMAL),

        ABSTRACT_HORSE(-1, ENTITY_AGEABLE),
        HORSE(27, ABSTRACT_HORSE),
        SKELETON_HORSE(61, ABSTRACT_HORSE),
        ZOMBIE_HORSE(85, ABSTRACT_HORSE),

        CHESTED_HORSE(-1, ABSTRACT_HORSE),
        DONKEY(11, CHESTED_HORSE),
        MULE(44, CHESTED_HORSE),
        LIAMA(34, CHESTED_HORSE),


        RABBIT(54, ENTITY_AGEABLE),
        POLAR_BEAR(52, ENTITY_AGEABLE),
        VILLAGER(76, ENTITY_AGEABLE),
        ENDER_CRYSTAL(14, ENTITY),
        SPLASH_POTION(-1, ENTITY),
        LINGERING_POTION(-1, SPLASH_POTION),
        AREA_EFFECT_CLOUD(-1, ENTITY),
        EGG(-1, ENTITY),
        FISHING_HOOK(-1, ENTITY),
        LIGHTNING(-1, ENTITY),
        WEATHER(-1, ENTITY),
        PLAYER(-1, ENTITY_HUMAN),
        COMPLEX_PART(-1, ENTITY),
        LIAMA_SPIT(-1, ENTITY);

        private final int id;
        private final EntityType parent;

        EntityType(int id) {
            this.id = id;
            this.parent = null;
        }

        public static Optional<EntityType> findById(int id) {
            if (id == -1)  // Check if this is called
                return Optional.absent();

            for (EntityType ent : EntityType.values())
                if (ent.getId() == id)
                    return Optional.of(ent);

            return Optional.absent();
        }

        public boolean is(EntityType... types) {
            for (EntityType type : types)
                if (is(type))
                    return true;
            return false;
        }

        public boolean is(EntityType type) {
            return this == type;
        }

        public boolean isOrHasParent(EntityType type) {
            EntityType parent = this;

            do {
                if (parent.equals(type))
                    return true;

                parent = parent.getParent();
            } while (parent != null);

            return false;
        }
    }

    @AllArgsConstructor
    @Getter
    public enum ObjectTypes {
        AREA_EFFECT_CLOUD(0, EntityType.AREA_EFFECT_CLOUD),
        ARMOR_STAND(1, EntityType.ARMOR_STAND),
        BOAT(5, EntityType.BOAT),
        ITEM(30, EntityType.DROPPED_ITEM),
        MINECART(37, EntityType.MINECART_ABSTRACT),
        TNT_PRIMED(53, EntityType.PRIMED_TNT),
        ENDER_CRYSTAL(14, EntityType.ENDER_CRYSTAL),
        TIPPED_ARROW(2, EntityType.ARROW),
        SNOWBALL(65, EntityType.SNOWBALL),
        EGG(71, EntityType.EGG),
        FIREBALL(32, EntityType.FIREBALL),
        SMALL_FIREBALL(63, EntityType.SMALL_FIREBALL),
        ENDER_PEARL(72, EntityType.ENDER_PEARL),
        WITHER_SKULL(82, EntityType.WITHER_SKULL),
        SHULKER_BULLET(58, EntityType.SHULKER_BULLET),
        LIAMA_SPIT(35, EntityType.LIAMA_SPIT),
        FALLING_BLOCK(22, EntityType.FALLING_BLOCK),
        ITEM_FRAME(31, EntityType.ITEM_FRAME),
        ENDER_SIGNAL(21, EntityType.ENDER_SIGNAL),
        POTION(74, EntityType.SPLASH_POTION),
        THROWN_EXP_BOTTLE(73, EntityType.THROWN_EXP_BOTTLE),
        FIREWORK(23, EntityType.FIREWORK),
        LEASH(33, EntityType.LEASH_HITCH),
        EVOCATION_FANGS(18, EntityType.EVOCATION_FANGS),
        FISHIHNG_HOOK(90, EntityType.FISHING_HOOK),
        SPECTRAL_ARROW(66, EntityType.SPECTRAL_ARROW),
        DRAGON_FIREBALL(12, EntityType.DRAGON_FIREBALL);

        private final int id;
        private final EntityType type;

        public static Optional<ObjectTypes> findById(int id) {
            if (id == -1)
                return Optional.absent();

            for (ObjectTypes ent : ObjectTypes.values())
                if (ent.getId() == id)
                    return Optional.of(ent);

            return Optional.absent();
        }

        public static Optional<EntityType> getPCEntity(int id) {
            Optional<ObjectTypes> output = findById(id);

            if (!output.isPresent())
                return Optional.absent();
            return Optional.of(output.get().getType());
        }
    }
}

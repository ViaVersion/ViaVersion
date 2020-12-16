package us.myles.ViaVersion.api.entities;

import us.myles.ViaVersion.api.Via;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Entity1_17Types {

    public static EntityType getTypeFromId(int typeID) {
        Optional<EntityType> type = EntityType.findById(typeID);
        if (!type.isPresent()) {
            Via.getPlatform().getLogger().severe("Could not find 1.17 type id " + typeID);
            return EntityType.ENTITY; // Fall back to the basic ENTITY
        }
        return type.get();
    }

    public enum EntityType implements us.myles.ViaVersion.api.entities.EntityType {
        ENTITY(-1),

        AREA_EFFECT_CLOUD(0, ENTITY),
        END_CRYSTAL(19, ENTITY),
        EVOKER_FANGS(24, ENTITY),
        EXPERIENCE_ORB(25, ENTITY),
        EYE_OF_ENDER(26, ENTITY),
        FALLING_BLOCK(27, ENTITY),
        FIREWORK_ROCKET(28, ENTITY),
        ITEM(38, ENTITY),
        LLAMA_SPIT(44, ENTITY),
        TNT(65, ENTITY),
        SHULKER_BULLET(72, ENTITY),
        FISHING_BOBBER(108, ENTITY),

        LIVINGENTITY(-1, ENTITY),
        ARMOR_STAND(1, LIVINGENTITY),
        PLAYER(107, LIVINGENTITY),

        ABSTRACT_INSENTIENT(-1, LIVINGENTITY),
        ENDER_DRAGON(20, ABSTRACT_INSENTIENT),

        BEE(5, ABSTRACT_INSENTIENT),

        ABSTRACT_CREATURE(-1, ABSTRACT_INSENTIENT),

        ABSTRACT_AGEABLE(-1, ABSTRACT_CREATURE),
        VILLAGER(94, ABSTRACT_AGEABLE),
        WANDERING_TRADER(96, ABSTRACT_AGEABLE),

        // Animals
        ABSTRACT_ANIMAL(-1, ABSTRACT_AGEABLE),
        AXOLOTL(3, ABSTRACT_ANIMAL),
        DOLPHIN(14, ABSTRACT_INSENTIENT),
        CHICKEN(10, ABSTRACT_ANIMAL),
        COW(12, ABSTRACT_ANIMAL),
        MOOSHROOM(54, COW),
        PANDA(57, ABSTRACT_INSENTIENT),
        PIG(60, ABSTRACT_ANIMAL),
        POLAR_BEAR(64, ABSTRACT_ANIMAL),
        RABBIT(67, ABSTRACT_ANIMAL),
        SHEEP(70, ABSTRACT_ANIMAL),
        TURTLE(92, ABSTRACT_ANIMAL),
        FOX(29, ABSTRACT_ANIMAL),

        ABSTRACT_TAMEABLE_ANIMAL(-1, ABSTRACT_ANIMAL),
        CAT(8, ABSTRACT_TAMEABLE_ANIMAL),
        OCELOT(55, ABSTRACT_TAMEABLE_ANIMAL),
        WOLF(101, ABSTRACT_TAMEABLE_ANIMAL),

        ABSTRACT_PARROT(-1, ABSTRACT_TAMEABLE_ANIMAL),
        PARROT(58, ABSTRACT_PARROT),

        // Horses
        ABSTRACT_HORSE(-1, ABSTRACT_ANIMAL),
        CHESTED_HORSE(-1, ABSTRACT_HORSE),
        DONKEY(15, CHESTED_HORSE),
        MULE(53, CHESTED_HORSE),
        LLAMA(43, CHESTED_HORSE),
        TRADER_LLAMA(90, CHESTED_HORSE),
        HORSE(34, ABSTRACT_HORSE),
        SKELETON_HORSE(75, ABSTRACT_HORSE),
        ZOMBIE_HORSE(104, ABSTRACT_HORSE),

        // Golem
        ABSTRACT_GOLEM(-1, ABSTRACT_CREATURE),
        SNOW_GOLEM(78, ABSTRACT_GOLEM),
        IRON_GOLEM(37, ABSTRACT_GOLEM),
        SHULKER(71, ABSTRACT_GOLEM),

        // Fish
        ABSTRACT_FISHES(-1, ABSTRACT_CREATURE),
        COD(11, ABSTRACT_FISHES),
        PUFFERFISH(66, ABSTRACT_FISHES),
        SALMON(69, ABSTRACT_FISHES),
        TROPICAL_FISH(91, ABSTRACT_FISHES),

        // Monsters
        ABSTRACT_MONSTER(-1, ABSTRACT_CREATURE),
        BLAZE(6, ABSTRACT_MONSTER),
        CREEPER(13, ABSTRACT_MONSTER),
        ENDERMITE(22, ABSTRACT_MONSTER),
        ENDERMAN(21, ABSTRACT_MONSTER),
        GIANT(31, ABSTRACT_MONSTER),
        SILVERFISH(73, ABSTRACT_MONSTER),
        VEX(93, ABSTRACT_MONSTER),
        WITCH(97, ABSTRACT_MONSTER),
        WITHER(98, ABSTRACT_MONSTER),
        RAVAGER(68, ABSTRACT_MONSTER),

        ABSTRACT_PIGLIN(-1, ABSTRACT_MONSTER),

        PIGLIN(61, ABSTRACT_PIGLIN),
        PIGLIN_BRUTE(62, ABSTRACT_PIGLIN),

        HOGLIN(33, ABSTRACT_ANIMAL),
        STRIDER(84, ABSTRACT_ANIMAL),
        ZOGLIN(102, ABSTRACT_MONSTER),

        // Illagers
        ABSTRACT_ILLAGER_BASE(-1, ABSTRACT_MONSTER),
        ABSTRACT_EVO_ILLU_ILLAGER(-1, ABSTRACT_ILLAGER_BASE),
        EVOKER(23, ABSTRACT_EVO_ILLU_ILLAGER),
        ILLUSIONER(36, ABSTRACT_EVO_ILLU_ILLAGER),
        VINDICATOR(95, ABSTRACT_ILLAGER_BASE),
        PILLAGER(63, ABSTRACT_ILLAGER_BASE),

        // Skeletons
        ABSTRACT_SKELETON(-1, ABSTRACT_MONSTER),
        SKELETON(74, ABSTRACT_SKELETON),
        STRAY(83, ABSTRACT_SKELETON),
        WITHER_SKELETON(99, ABSTRACT_SKELETON),

        // Guardians
        GUARDIAN(32, ABSTRACT_MONSTER),
        ELDER_GUARDIAN(18, GUARDIAN),

        // Spiders
        SPIDER(81, ABSTRACT_MONSTER),
        CAVE_SPIDER(9, SPIDER),

        // Zombies
        ZOMBIE(103, ABSTRACT_MONSTER),
        DROWNED(17, ZOMBIE),
        HUSK(35, ZOMBIE),
        ZOMBIFIED_PIGLIN(106, ZOMBIE),
        ZOMBIE_VILLAGER(105, ZOMBIE),

        // Flying entities
        ABSTRACT_FLYING(-1, ABSTRACT_INSENTIENT),
        GHAST(30, ABSTRACT_FLYING),
        PHANTOM(59, ABSTRACT_FLYING),

        ABSTRACT_AMBIENT(-1, ABSTRACT_INSENTIENT),
        BAT(4, ABSTRACT_AMBIENT),

        ABSTRACT_WATERMOB(-1, ABSTRACT_INSENTIENT),
        SQUID(82, ABSTRACT_WATERMOB),

        // Slimes
        SLIME(76, ABSTRACT_INSENTIENT),
        MAGMA_CUBE(45, SLIME),

        // Hangable objects
        ABSTRACT_HANGING(-1, ENTITY),
        LEASH_KNOT(41, ABSTRACT_HANGING),
        ITEM_FRAME(39, ABSTRACT_HANGING),
        PAINTING(56, ABSTRACT_HANGING),

        ABSTRACT_LIGHTNING(-1, ENTITY),
        LIGHTNING_BOLT(42, ABSTRACT_LIGHTNING),

        // Arrows
        ABSTRACT_ARROW(-1, ENTITY),
        ARROW(2, ABSTRACT_ARROW),
        SPECTRAL_ARROW(80, ABSTRACT_ARROW),
        TRIDENT(89, ABSTRACT_ARROW),

        // Fireballs
        ABSTRACT_FIREBALL(-1, ENTITY),
        DRAGON_FIREBALL(16, ABSTRACT_FIREBALL),
        FIREBALL(40, ABSTRACT_FIREBALL),
        SMALL_FIREBALL(77, ABSTRACT_FIREBALL),
        WITHER_SKULL(100, ABSTRACT_FIREBALL),

        // Projectiles
        PROJECTILE_ABSTRACT(-1, ENTITY),
        SNOWBALL(79, PROJECTILE_ABSTRACT),
        ENDER_PEARL(86, PROJECTILE_ABSTRACT),
        EGG(85, PROJECTILE_ABSTRACT),
        POTION(88, PROJECTILE_ABSTRACT),
        EXPERIENCE_BOTTLE(87, PROJECTILE_ABSTRACT),

        // Vehicles
        MINECART_ABSTRACT(-1, ENTITY),
        CHESTED_MINECART_ABSTRACT(-1, MINECART_ABSTRACT),
        CHEST_MINECART(47, CHESTED_MINECART_ABSTRACT),
        HOPPER_MINECART(50, CHESTED_MINECART_ABSTRACT),
        MINECART(46, MINECART_ABSTRACT),
        FURNACE_MINECART(49, MINECART_ABSTRACT),
        COMMAND_BLOCK_MINECART(48, MINECART_ABSTRACT),
        TNT_MINECART(52, MINECART_ABSTRACT),
        SPAWNER_MINECART(51, MINECART_ABSTRACT),
        BOAT(7, ENTITY);


        private static final Map<Integer, EntityType> TYPES = new HashMap<>();

        private final int id;
        private final EntityType parent;

        EntityType(int id) {
            this.id = id;
            this.parent = null;
        }

        EntityType(int id, EntityType parent) {
            this.id = id;
            this.parent = parent;
        }

        @Override
        public int getId() {
            return id;
        }

        @Override
        public EntityType getParent() {
            return parent;
        }

        static {
            for (EntityType type : EntityType.values()) {
                TYPES.put(type.id, type);
            }
        }

        public static Optional<EntityType> findById(int id) {
            if (id == -1)
                return Optional.empty();
            return Optional.ofNullable(TYPES.get(id));
        }
    }
}

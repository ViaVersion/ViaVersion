package us.myles.ViaVersion.api.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import us.myles.ViaVersion.api.Via;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


public class Entity1_16Types {

    public static EntityType getTypeFromId(int typeID) {
        Optional<EntityType> type = EntityType.findById(typeID);

        if (!type.isPresent()) {
            Via.getPlatform().getLogger().severe("Could not find 1.16 type id " + typeID);
            return EntityType.ENTITY; // Fall back to the basic ENTITY
        }

        return type.get();
    }


    @AllArgsConstructor
    @Getter
    public enum EntityType implements us.myles.ViaVersion.api.entities.EntityType {
        ENTITY(-1),

        AREA_EFFECT_CLOUD(0, ENTITY),
        END_CRYSTAL(18, ENTITY),
        EVOKER_FANGS(23, ENTITY),
        EXPERIENCE_ORB(24, ENTITY),
        EYE_OF_ENDER(25, ENTITY),
        FALLING_BLOCK(26, ENTITY),
        FIREWORK_ROCKET(27, ENTITY),
        ITEM(37, ENTITY),
        LLAMA_SPIT(42, ENTITY),
        TNT(62, ENTITY),
        SHULKER_BULLET(69, ENTITY),
        FISHING_BOBBER(106, ENTITY),

        LIVINGENTITY(-1, ENTITY),
        ARMOR_STAND(1, LIVINGENTITY),
        PLAYER(105, LIVINGENTITY),

        ABSTRACT_INSENTIENT(-1, LIVINGENTITY),
        ENDER_DRAGON(19, ABSTRACT_INSENTIENT),

        BEE(4, ABSTRACT_INSENTIENT),

        ABSTRACT_CREATURE(-1, ABSTRACT_INSENTIENT),

        ABSTRACT_AGEABLE(-1, ABSTRACT_CREATURE),
        VILLAGER(91, ABSTRACT_AGEABLE),
        WANDERING_TRADER(93, ABSTRACT_AGEABLE),

        // Animals
        ABSTRACT_ANIMAL(-1, ABSTRACT_AGEABLE),
        DOLPHIN(13, ABSTRACT_INSENTIENT),
        CHICKEN(9, ABSTRACT_ANIMAL),
        COW(11, ABSTRACT_ANIMAL),
        MOOSHROOM(52, COW),
        PANDA(55, ABSTRACT_INSENTIENT),
        PIG(58, ABSTRACT_ANIMAL),
        POLAR_BEAR(61, ABSTRACT_ANIMAL),
        RABBIT(64, ABSTRACT_ANIMAL),
        SHEEP(67, ABSTRACT_ANIMAL),
        TURTLE(89, ABSTRACT_ANIMAL),
        FOX(28, ABSTRACT_ANIMAL),

        ABSTRACT_TAMEABLE_ANIMAL(-1, ABSTRACT_ANIMAL),
        CAT(7, ABSTRACT_TAMEABLE_ANIMAL),
        OCELOT(53, ABSTRACT_TAMEABLE_ANIMAL),
        WOLF(98, ABSTRACT_TAMEABLE_ANIMAL),

        ABSTRACT_PARROT(-1, ABSTRACT_TAMEABLE_ANIMAL),
        PARROT(56, ABSTRACT_PARROT),

        // Horses
        ABSTRACT_HORSE(-1, ABSTRACT_ANIMAL),
        CHESTED_HORSE(-1, ABSTRACT_HORSE),
        DONKEY(14, CHESTED_HORSE),
        MULE(51, CHESTED_HORSE),
        LLAMA(41, CHESTED_HORSE),
        TRADER_LLAMA(87, CHESTED_HORSE),
        HORSE(33, ABSTRACT_HORSE),
        SKELETON_HORSE(72, ABSTRACT_HORSE),
        ZOMBIE_HORSE(101, ABSTRACT_HORSE),

        // Golem
        ABSTRACT_GOLEM(-1, ABSTRACT_CREATURE),
        SNOW_GOLEM(75, ABSTRACT_GOLEM),
        IRON_GOLEM(36, ABSTRACT_GOLEM),
        SHULKER(68, ABSTRACT_GOLEM),

        // Fish
        ABSTRACT_FISHES(-1, ABSTRACT_CREATURE),
        COD(10, ABSTRACT_FISHES),
        PUFFERFISH(63, ABSTRACT_FISHES),
        SALMON(66, ABSTRACT_FISHES),
        TROPICAL_FISH(88, ABSTRACT_FISHES),

        // Monsters
        ABSTRACT_MONSTER(-1, ABSTRACT_CREATURE),
        BLAZE(5, ABSTRACT_MONSTER),
        CREEPER(12, ABSTRACT_MONSTER),
        ENDERMITE(21, ABSTRACT_MONSTER),
        ENDERMAN(20, ABSTRACT_MONSTER),
        GIANT(30, ABSTRACT_MONSTER),
        SILVERFISH(70, ABSTRACT_MONSTER),
        VEX(90, ABSTRACT_MONSTER),
        WITCH(94, ABSTRACT_MONSTER),
        WITHER(95, ABSTRACT_MONSTER),
        RAVAGER(65, ABSTRACT_MONSTER),
        PIGLIN(59, ABSTRACT_MONSTER),

        HOGLIN(32, ABSTRACT_ANIMAL),
        STRIDER(81, ABSTRACT_ANIMAL),
        ZOGLIN(99, ABSTRACT_MONSTER),

        // Illagers
        ABSTRACT_ILLAGER_BASE(-1, ABSTRACT_MONSTER),
        ABSTRACT_EVO_ILLU_ILLAGER(-1, ABSTRACT_ILLAGER_BASE),
        EVOKER(22, ABSTRACT_EVO_ILLU_ILLAGER),
        ILLUSIONER(35, ABSTRACT_EVO_ILLU_ILLAGER),
        VINDICATOR(92, ABSTRACT_ILLAGER_BASE),
        PILLAGER(60, ABSTRACT_ILLAGER_BASE),

        // Skeletons
        ABSTRACT_SKELETON(-1, ABSTRACT_MONSTER),
        SKELETON(71, ABSTRACT_SKELETON),
        STRAY(80, ABSTRACT_SKELETON),
        WITHER_SKELETON(96, ABSTRACT_SKELETON),

        // Guardians
        GUARDIAN(31, ABSTRACT_MONSTER),
        ELDER_GUARDIAN(17, GUARDIAN),

        // Spiders
        SPIDER(78, ABSTRACT_MONSTER),
        CAVE_SPIDER(8, SPIDER),

        // Zombies
        ZOMBIE(100, ABSTRACT_MONSTER),
        DROWNED(16, ZOMBIE),
        HUSK(34, ZOMBIE),
        ZOMBIFIED_PIGLIN(103, ZOMBIE),
        ZOMBIE_VILLAGER(102, ZOMBIE),

        // Flying entities
        ABSTRACT_FLYING(-1, ABSTRACT_INSENTIENT),
        GHAST(29, ABSTRACT_FLYING),
        PHANTOM(57, ABSTRACT_FLYING),

        ABSTRACT_AMBIENT(-1, ABSTRACT_INSENTIENT),
        BAT(3, ABSTRACT_AMBIENT),

        ABSTRACT_WATERMOB(-1, ABSTRACT_INSENTIENT),
        SQUID(79, ABSTRACT_WATERMOB),

        // Slimes
        SLIME(73, ABSTRACT_INSENTIENT),
        MAGMA_CUBE(43, SLIME),

        // Hangable objects
        ABSTRACT_HANGING(-1, ENTITY),
        LEASH_KNOT(40, ABSTRACT_HANGING),
        ITEM_FRAME(38, ABSTRACT_HANGING),
        PAINTING(54, ABSTRACT_HANGING),

        ABSTRACT_LIGHTNING(-1, ENTITY),
        LIGHTNING_BOLT(104, ABSTRACT_LIGHTNING),

        // Arrows
        ABSTRACT_ARROW(-1, ENTITY),
        ARROW(2, ABSTRACT_ARROW),
        SPECTRAL_ARROW(77, ABSTRACT_ARROW),
        TRIDENT(86, ABSTRACT_ARROW),

        // Fireballs
        ABSTRACT_FIREBALL(-1, ENTITY),
        DRAGON_FIREBALL(15, ABSTRACT_FIREBALL),
        FIREBALL(39, ABSTRACT_FIREBALL),
        SMALL_FIREBALL(74, ABSTRACT_FIREBALL),
        WITHER_SKULL(97, ABSTRACT_FIREBALL),

        // Projectiles
        PROJECTILE_ABSTRACT(-1, ENTITY),
        SNOWBALL(76, PROJECTILE_ABSTRACT),
        ENDER_PEARL(83, PROJECTILE_ABSTRACT),
        EGG(82, PROJECTILE_ABSTRACT),
        POTION(85, PROJECTILE_ABSTRACT),
        EXPERIENCE_BOTTLE(84, PROJECTILE_ABSTRACT),

        // Vehicles
        MINECART_ABSTRACT(-1, ENTITY),
        CHESTED_MINECART_ABSTRACT(-1, MINECART_ABSTRACT),
        CHEST_MINECART(45, CHESTED_MINECART_ABSTRACT),
        HOPPER_MINECART(48, CHESTED_MINECART_ABSTRACT),
        MINECART(44, MINECART_ABSTRACT),
        FURNACE_MINECART(47, MINECART_ABSTRACT),
        COMMAND_BLOCK_MINECART(46, MINECART_ABSTRACT),
        TNT_MINECART(50, MINECART_ABSTRACT),
        SPAWNER_MINECART(49, MINECART_ABSTRACT),
        BOAT(6, ENTITY);

        private static final Map<Integer, EntityType> TYPES = new HashMap<>();

        private final int id;
        private final EntityType parent;

        EntityType(int id) {
            this.id = id;
            this.parent = null;
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

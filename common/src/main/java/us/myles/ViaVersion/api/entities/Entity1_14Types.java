package us.myles.ViaVersion.api.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import us.myles.ViaVersion.api.Via;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


public class Entity1_14Types {

    public static EntityType getTypeFromId(int typeID) {
        Optional<EntityType> type = EntityType.findById(typeID);

        if (!type.isPresent()) {
            Via.getPlatform().getLogger().severe("Could not find 1.14 type id " + typeID);
            return EntityType.ENTITY; // Fall back to the basic ENTITY
        }

        return type.get();
    }

    @AllArgsConstructor
    @Getter
    public enum EntityType implements us.myles.ViaVersion.api.entities.EntityType {
        // Auto generated

        ENTITY(-1),

        AREA_EFFECT_CLOUD(0, ENTITY),
        ENDER_CRYSTAL(17, ENTITY),
        EVOCATION_FANGS(21, ENTITY),
        XP_ORB(23, ENTITY),
        EYE_OF_ENDER_SIGNAL(24, ENTITY),
        FALLING_BLOCK(25, ENTITY),
        FIREWORKS_ROCKET(26, ENTITY),
        ITEM(34, ENTITY),
        LLAMA_SPIT(39, ENTITY),
        TNT(58, ENTITY),
        SHULKER_BULLET(63, ENTITY),
        FISHING_BOBBER(101, ENTITY),

        LIVINGENTITY(-1, ENTITY),
        ARMOR_STAND(1, LIVINGENTITY),
        PLAYER(100, LIVINGENTITY),

        ABSTRACT_INSENTIENT(-1, LIVINGENTITY),
        ENDER_DRAGON(18, ABSTRACT_INSENTIENT),

        ABSTRACT_CREATURE(-1, ABSTRACT_INSENTIENT),

        ABSTRACT_AGEABLE(-1, ABSTRACT_CREATURE),
        VILLAGER(84, ABSTRACT_AGEABLE),
        WANDERING_TRADER(88, ABSTRACT_AGEABLE),

        // Animals
        ABSTRACT_ANIMAL(-1, ABSTRACT_AGEABLE),
        DOLPHIN(13, ABSTRACT_INSENTIENT),
        CHICKEN(8, ABSTRACT_ANIMAL),
        COW(10, ABSTRACT_ANIMAL),
        MOOSHROOM(49, COW),
        PANDA(52, ABSTRACT_INSENTIENT),
        PIG(54, ABSTRACT_ANIMAL),
        POLAR_BEAR(57, ABSTRACT_ANIMAL),
        RABBIT(59, ABSTRACT_ANIMAL),
        SHEEP(61, ABSTRACT_ANIMAL),
        TURTLE(77, ABSTRACT_ANIMAL),
        FOX(27, ABSTRACT_ANIMAL),

        ABSTRACT_TAMEABLE_ANIMAL(-1, ABSTRACT_ANIMAL),
        CAT(6, ABSTRACT_TAMEABLE_ANIMAL),
        OCELOT(50, ABSTRACT_TAMEABLE_ANIMAL),
        WOLF(93, ABSTRACT_TAMEABLE_ANIMAL),

        ABSTRACT_PARROT(-1, ABSTRACT_TAMEABLE_ANIMAL),
        PARROT(53, ABSTRACT_PARROT),

        // Horses
        ABSTRACT_HORSE(-1, ABSTRACT_ANIMAL),
        CHESTED_HORSE(-1, ABSTRACT_HORSE),
        DONKEY(12, CHESTED_HORSE),
        MULE(48, CHESTED_HORSE),
        LLAMA(38, CHESTED_HORSE),
        TRADER_LLAMA(75, CHESTED_HORSE),
        HORSE(31, ABSTRACT_HORSE),
        SKELETON_HORSE(66, ABSTRACT_HORSE),
        ZOMBIE_HORSE(95, ABSTRACT_HORSE),

        // Golem
        ABSTRACT_GOLEM(-1, ABSTRACT_CREATURE),
        SNOWMAN(69, ABSTRACT_GOLEM),
        VILLAGER_GOLEM(85, ABSTRACT_GOLEM),
        SHULKER(62, ABSTRACT_GOLEM),

        // Fish
        ABSTRACT_FISHES(-1, ABSTRACT_CREATURE),
        COD(9, ABSTRACT_FISHES),
        PUFFER_FISH(55, ABSTRACT_FISHES),
        SALMON_MOB(60, ABSTRACT_FISHES),
        TROPICAL_FISH(76, ABSTRACT_FISHES),

        // Monsters
        ABSTRACT_MONSTER(-1, ABSTRACT_CREATURE),
        BLAZE(4, ABSTRACT_MONSTER),
        CREEPER(11, ABSTRACT_MONSTER),
        ENDERMITE(20, ABSTRACT_MONSTER),
        ENDERMAN(19, ABSTRACT_MONSTER),
        GIANT(29, ABSTRACT_MONSTER),
        SILVERFISH(64, ABSTRACT_MONSTER),
        VEX(83, ABSTRACT_MONSTER),
        WITCH(89, ABSTRACT_MONSTER),
        WITHER(90, ABSTRACT_MONSTER),
        RAVAGER(98, ABSTRACT_MONSTER),

        // Illagers
        ABSTRACT_ILLAGER_BASE(-1, ABSTRACT_MONSTER),
        ABSTRACT_EVO_ILLU_ILLAGER(-1, ABSTRACT_ILLAGER_BASE),
        EVOCATION_ILLAGER(22, ABSTRACT_EVO_ILLU_ILLAGER),
        ILLUSION_ILLAGER(33, ABSTRACT_EVO_ILLU_ILLAGER),
        VINDICATION_ILLAGER(86, ABSTRACT_ILLAGER_BASE),
        PILLAGER(87, ABSTRACT_ILLAGER_BASE),

        // Skeletons
        ABSTRACT_SKELETON(-1, ABSTRACT_MONSTER),
        SKELETON(65, ABSTRACT_SKELETON),
        STRAY(74, ABSTRACT_SKELETON),
        WITHER_SKELETON(91, ABSTRACT_SKELETON),

        // Guardians
        GUARDIAN(30, ABSTRACT_MONSTER),
        ELDER_GUARDIAN(16, GUARDIAN),

        // Spiders
        SPIDER(72, ABSTRACT_MONSTER),
        CAVE_SPIDER(7, SPIDER),

        // Zombies - META CHECKED
        ZOMBIE(94, ABSTRACT_MONSTER),
        DROWNED(15, ZOMBIE),
        HUSK(32, ZOMBIE),
        ZOMBIE_PIGMAN(56, ZOMBIE),
        ZOMBIE_VILLAGER(96, ZOMBIE),

        // Flying entities
        ABSTRACT_FLYING(-1, ABSTRACT_INSENTIENT),
        GHAST(28, ABSTRACT_FLYING),
        PHANTOM(97, ABSTRACT_FLYING),

        ABSTRACT_AMBIENT(-1, ABSTRACT_INSENTIENT),
        BAT(3, ABSTRACT_AMBIENT),

        ABSTRACT_WATERMOB(-1, ABSTRACT_INSENTIENT),
        SQUID(73, ABSTRACT_WATERMOB),

        // Slimes
        SLIME(67, ABSTRACT_INSENTIENT),
        MAGMA_CUBE(40, SLIME),

        // Hangable objects
        ABSTRACT_HANGING(-1, ENTITY),
        LEASH_KNOT(37, ABSTRACT_HANGING),
        ITEM_FRAME(35, ABSTRACT_HANGING),
        PAINTING(51, ABSTRACT_HANGING),

        ABSTRACT_LIGHTNING(-1, ENTITY),
        LIGHTNING_BOLT(99, ABSTRACT_LIGHTNING),

        // Arrows
        ABSTRACT_ARROW(-1, ENTITY),
        ARROW(2, ABSTRACT_ARROW),
        SPECTRAL_ARROW(71, ABSTRACT_ARROW),
        TRIDENT(82, ABSTRACT_ARROW),

        // Fireballs
        ABSTRACT_FIREBALL(-1, ENTITY),
        DRAGON_FIREBALL(14, ABSTRACT_FIREBALL),
        FIREBALL(36, ABSTRACT_FIREBALL),
        SMALL_FIREBALL(68, ABSTRACT_FIREBALL),
        WITHER_SKULL(92, ABSTRACT_FIREBALL),

        // Projectiles
        PROJECTILE_ABSTRACT(-1, ENTITY),
        SNOWBALL(70, PROJECTILE_ABSTRACT),
        ENDER_PEARL(79, PROJECTILE_ABSTRACT),
        EGG(78, PROJECTILE_ABSTRACT),
        POTION(81, PROJECTILE_ABSTRACT),
        XP_BOTTLE(80, PROJECTILE_ABSTRACT),

        // Vehicles
        MINECART_ABSTRACT(-1, ENTITY),
        CHESTED_MINECART_ABSTRACT(-1, MINECART_ABSTRACT),
        CHEST_MINECART(42, CHESTED_MINECART_ABSTRACT),
        HOPPER_MINECART(45, CHESTED_MINECART_ABSTRACT),
        MINECART(41, MINECART_ABSTRACT),
        FURNACE_MINECART(44, MINECART_ABSTRACT),
        COMMANDBLOCK_MINECART(43, MINECART_ABSTRACT),
        TNT_MINECART(47, MINECART_ABSTRACT),
        SPAWNER_MINECART(46, MINECART_ABSTRACT),
        BOAT(5, ENTITY),
        ;

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

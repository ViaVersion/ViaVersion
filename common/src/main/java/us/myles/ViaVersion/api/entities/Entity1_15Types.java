package us.myles.ViaVersion.api.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import us.myles.ViaVersion.api.Via;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


public class Entity1_15Types {

    public static EntityType getTypeFromId(int typeID) {
        Optional<EntityType> type = EntityType.findById(typeID);

        if (!type.isPresent()) {
            Via.getPlatform().getLogger().severe("Could not find 1.15 type id " + typeID);
            return EntityType.ENTITY; // Fall back to the basic ENTITY
        }

        return type.get();
    }


    @AllArgsConstructor
    @Getter
    public enum EntityType implements us.myles.ViaVersion.api.entities.EntityType {
        ENTITY(-1),

        AREA_EFFECT_CLOUD(0, ENTITY),
        ENDER_CRYSTAL(18, ENTITY),
        EVOCATION_FANGS(22, ENTITY),
        XP_ORB(24, ENTITY),
        EYE_OF_ENDER_SIGNAL(25, ENTITY),
        FALLING_BLOCK(26, ENTITY),
        FIREWORKS_ROCKET(27, ENTITY),
        ITEM(35, ENTITY),
        LLAMA_SPIT(40, ENTITY),
        TNT(59, ENTITY),
        SHULKER_BULLET(64, ENTITY),
        FISHING_BOBBER(102, ENTITY),

        LIVINGENTITY(-1, ENTITY),
        ARMOR_STAND(1, LIVINGENTITY),
        PLAYER(101, LIVINGENTITY),

        ABSTRACT_INSENTIENT(-1, LIVINGENTITY),
        ENDER_DRAGON(19, ABSTRACT_INSENTIENT),

        BEE(4, ABSTRACT_INSENTIENT),

        ABSTRACT_CREATURE(-1, ABSTRACT_INSENTIENT),

        ABSTRACT_AGEABLE(-1, ABSTRACT_CREATURE),
        VILLAGER(85, ABSTRACT_AGEABLE),
        WANDERING_TRADER(89, ABSTRACT_AGEABLE),

        // Animals
        ABSTRACT_ANIMAL(-1, ABSTRACT_AGEABLE),
        DOLPHIN(14, ABSTRACT_INSENTIENT),
        CHICKEN(9, ABSTRACT_ANIMAL),
        COW(11, ABSTRACT_ANIMAL),
        MOOSHROOM(50, COW),
        PANDA(53, ABSTRACT_INSENTIENT),
        PIG(55, ABSTRACT_ANIMAL),
        POLAR_BEAR(58, ABSTRACT_ANIMAL),
        RABBIT(60, ABSTRACT_ANIMAL),
        SHEEP(62, ABSTRACT_ANIMAL),
        TURTLE(78, ABSTRACT_ANIMAL),
        FOX(28, ABSTRACT_ANIMAL),

        ABSTRACT_TAMEABLE_ANIMAL(-1, ABSTRACT_ANIMAL),
        CAT(7, ABSTRACT_TAMEABLE_ANIMAL),
        OCELOT(51, ABSTRACT_TAMEABLE_ANIMAL),
        WOLF(94, ABSTRACT_TAMEABLE_ANIMAL),

        ABSTRACT_PARROT(-1, ABSTRACT_TAMEABLE_ANIMAL),
        PARROT(54, ABSTRACT_PARROT),

        // Horses
        ABSTRACT_HORSE(-1, ABSTRACT_ANIMAL),
        CHESTED_HORSE(-1, ABSTRACT_HORSE),
        DONKEY(13, CHESTED_HORSE),
        MULE(49, CHESTED_HORSE),
        LLAMA(39, CHESTED_HORSE),
        TRADER_LLAMA(76, CHESTED_HORSE),
        HORSE(32, ABSTRACT_HORSE),
        SKELETON_HORSE(67, ABSTRACT_HORSE),
        ZOMBIE_HORSE(96, ABSTRACT_HORSE),

        // Golem
        ABSTRACT_GOLEM(-1, ABSTRACT_CREATURE),
        SNOWMAN(70, ABSTRACT_GOLEM),
        VILLAGER_GOLEM(86, ABSTRACT_GOLEM),
        SHULKER(63, ABSTRACT_GOLEM),

        // Fish
        ABSTRACT_FISHES(-1, ABSTRACT_CREATURE),
        COD(10, ABSTRACT_FISHES),
        PUFFER_FISH(56, ABSTRACT_FISHES),
        SALMON_MOB(61, ABSTRACT_FISHES),
        TROPICAL_FISH(77, ABSTRACT_FISHES),

        // Monsters
        ABSTRACT_MONSTER(-1, ABSTRACT_CREATURE),
        BLAZE(5, ABSTRACT_MONSTER),
        CREEPER(12, ABSTRACT_MONSTER),
        ENDERMITE(21, ABSTRACT_MONSTER),
        ENDERMAN(20, ABSTRACT_MONSTER),
        GIANT(30, ABSTRACT_MONSTER),
        SILVERFISH(65, ABSTRACT_MONSTER),
        VEX(84, ABSTRACT_MONSTER),
        WITCH(90, ABSTRACT_MONSTER),
        WITHER(91, ABSTRACT_MONSTER),
        RAVAGER(99, ABSTRACT_MONSTER),

        // Illagers
        ABSTRACT_ILLAGER_BASE(-1, ABSTRACT_MONSTER),
        ABSTRACT_EVO_ILLU_ILLAGER(-1, ABSTRACT_ILLAGER_BASE),
        EVOCATION_ILLAGER(23, ABSTRACT_EVO_ILLU_ILLAGER),
        ILLUSION_ILLAGER(34, ABSTRACT_EVO_ILLU_ILLAGER),
        VINDICATION_ILLAGER(87, ABSTRACT_ILLAGER_BASE),
        PILLAGER(88, ABSTRACT_ILLAGER_BASE),

        // Skeletons
        ABSTRACT_SKELETON(-1, ABSTRACT_MONSTER),
        SKELETON(66, ABSTRACT_SKELETON),
        STRAY(75, ABSTRACT_SKELETON),
        WITHER_SKELETON(92, ABSTRACT_SKELETON),

        // Guardians
        GUARDIAN(31, ABSTRACT_MONSTER),
        ELDER_GUARDIAN(17, GUARDIAN),

        // Spiders
        SPIDER(73, ABSTRACT_MONSTER),
        CAVE_SPIDER(8, SPIDER),

        // Zombies
        ZOMBIE(95, ABSTRACT_MONSTER),
        DROWNED(16, ZOMBIE),
        HUSK(33, ZOMBIE),
        ZOMBIE_PIGMAN(57, ZOMBIE),
        ZOMBIE_VILLAGER(97, ZOMBIE),

        // Flying entities
        ABSTRACT_FLYING(-1, ABSTRACT_INSENTIENT),
        GHAST(29, ABSTRACT_FLYING),
        PHANTOM(98, ABSTRACT_FLYING),

        ABSTRACT_AMBIENT(-1, ABSTRACT_INSENTIENT),
        BAT(3, ABSTRACT_AMBIENT),

        ABSTRACT_WATERMOB(-1, ABSTRACT_INSENTIENT),
        SQUID(74, ABSTRACT_WATERMOB),

        // Slimes
        SLIME(68, ABSTRACT_INSENTIENT),
        MAGMA_CUBE(41, SLIME),

        // Hangable objects
        ABSTRACT_HANGING(-1, ENTITY),
        LEASH_KNOT(38, ABSTRACT_HANGING),
        ITEM_FRAME(36, ABSTRACT_HANGING),
        PAINTING(52, ABSTRACT_HANGING),

        ABSTRACT_LIGHTNING(-1, ENTITY),
        LIGHTNING_BOLT(100, ABSTRACT_LIGHTNING),

        // Arrows
        ABSTRACT_ARROW(-1, ENTITY),
        ARROW(2, ABSTRACT_ARROW),
        SPECTRAL_ARROW(72, ABSTRACT_ARROW),
        TRIDENT(83, ABSTRACT_ARROW),

        // Fireballs
        ABSTRACT_FIREBALL(-1, ENTITY),
        DRAGON_FIREBALL(15, ABSTRACT_FIREBALL),
        FIREBALL(37, ABSTRACT_FIREBALL),
        SMALL_FIREBALL(69, ABSTRACT_FIREBALL),
        WITHER_SKULL(93, ABSTRACT_FIREBALL),

        // Projectiles
        PROJECTILE_ABSTRACT(-1, ENTITY),
        SNOWBALL(71, PROJECTILE_ABSTRACT),
        ENDER_PEARL(80, PROJECTILE_ABSTRACT),
        EGG(79, PROJECTILE_ABSTRACT),
        POTION(82, PROJECTILE_ABSTRACT),
        XP_BOTTLE(81, PROJECTILE_ABSTRACT),

        // Vehicles
        MINECART_ABSTRACT(-1, ENTITY),
        CHESTED_MINECART_ABSTRACT(-1, MINECART_ABSTRACT),
        CHEST_MINECART(43, CHESTED_MINECART_ABSTRACT),
        HOPPER_MINECART(46, CHESTED_MINECART_ABSTRACT),
        MINECART(42, MINECART_ABSTRACT),
        FURNACE_MINECART(45, MINECART_ABSTRACT),
        COMMANDBLOCK_MINECART(44, MINECART_ABSTRACT),
        TNT_MINECART(48, MINECART_ABSTRACT),
        SPAWNER_MINECART(47, MINECART_ABSTRACT),
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

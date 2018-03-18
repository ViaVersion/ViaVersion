package us.myles.ViaVersion.api.entities;

import com.google.common.base.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import us.myles.ViaVersion.api.Via;


// TODO auto generate 18w11a with PAaaS
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
        // Auto generated

        ENTITY(-1), // abm

        AREA_EFFECT_CLOUD(0, ENTITY), // abk
        ENDER_CRYSTAL(15, ENTITY), // aho
        EVOCATION_FANGS(19, ENTITY), // ala
        XP_ORB(21, ENTITY), // abs
        EYE_OF_ENDER_SIGNAL(22, ENTITY), // alb
        FALLING_BLOCK(23, ENTITY), // aix
        FIREWORKS_ROCKET(24, ENTITY), // alc
        ITEM(31, ENTITY), // aiy
        LLAMA_SPIT(36, ENTITY), // ale
        TNT(54, ENTITY), // aiz
        SHULKER_BULLET(59, ENTITY), // alh
        FISHING_BOBBER(92, ENTITY), // ais

        LIVINGENTITY(-1, ENTITY), // abv

        PLAYER(91, LIVINGENTITY), // aks

        ABW(-1, LIVINGENTITY), // abw
        ACD(-1, ABW), // acd
        ABJ(-1, ACD), // abj
        AGD(-1, ABJ), // agd
        ACG(-1, AGD), // acg
        AGR(-1, ACG), // agr

        PARROT(49, AGR), // agk
        OCELOT(47, ACG), // agj
        WOLF(85, ACG), // agy
        CHICKEN(7, AGD), // age
        COW(9, AGD), // agg
        MOOSHROOM(46, COW), // agi
        POLAR_BEAR(53, AGD), // agm
        PIG(50, AGD), // agl
        RABBIT(55, AGD), // ago
        SHEEP(57, AGD), // agq
        TURTLE(72, AGD), // agv

        // Horses
        ABSTRACT_HORSE(-1, AGD), // aha
        CHESTED_HORSE(-1, ABSTRACT_HORSE), // agz
        DONKEY(11, CHESTED_HORSE), // ahb
        MULE(45, CHESTED_HORSE), // ahf
        LLAMA(35, CHESTED_HORSE), // ahe
        HORSE(28, ABSTRACT_HORSE), // ahc
        SKELETON_HORSE(62, ABSTRACT_HORSE), // ahg
        ZOMBIE_HORSE(87, ABSTRACT_HORSE), // ahi

        VILLAGER(78, ABJ), // akn

        // Golem
        ABSTRACT_GOLEM(-1, ACD), // agc
        SNOWMAN(65, ABSTRACT_GOLEM), // ags
        VILLAGER_GOLEM(79, ABSTRACT_GOLEM), // agw
        SHULKER(58, ABSTRACT_GOLEM), // ajx

        AGB(-1, ACD), // agb
        COD_MOB(8, AGB), // agf
        PUFFER_FISH(51, AGB), // agn
        SALMON_MOB(56, AGB), // agp
        TROPICAL_FISH(71, AGB), // agu
        AJS(-1, ACD), // ajs
        WITHER(82, AJS), // aij
        AJB(-1, AJS), // ajb
        AKB(-1, AJB), // akb
        EVOCATION_ILLAGER(20, AKB), // ajl
        ILLUSION_ILLAGER(30, AKB), // ajq
        VINDICATION_ILLAGER(80, AJB), // akf
        BLAZE(4, AJS), // ajd
        AJC(-1, AJS), // ajc
        SKELETON(61, AJC), // ajz
        STRAY(70, AJC), // akd
        WITHER_SKELETON(83, AJC), // akh
        CREEPER(10, AJS), // ajf
        SPIDER(68, AJS), // akc
        CAVE_SPIDER(6, SPIDER), // aje
        GUARDIAN(27, AJS), // ajo
        ELDER_GUARDIAN(14, GUARDIAN), // ajh
        ZOMBIE(86, AJS), // aki
        DROWNED(13, ZOMBIE), // ajg
        HUSK(29, ZOMBIE), // ajp
        ZOMBIE_PIGMAN(52, ZOMBIE), // aju
        ZOMBIE_VILLAGER(88, ZOMBIE), // akj
        ENDERMITE(18, AJS), // ajj
        ENDERMAN(17, AJS), // aji
        GIANT(26, AJS), // ajn
        SILVERFISH(60, AJS), // ajy
        VEX(77, AJS), // ake
        WITCH(81, AJS), // akg
        ABT(-1, ABW), // abt
        GHAST(25, ABT), // ajm
        PHANTOM(89, ABT), // ajt
        AFY(-1, ABW), // afy
        BAT(3, AFY), // afz
        AGX(-1, ABW), // agx
        SQUID(69, AGX), // agt
        ENDER_DRAGON(16, ABW), // ahp
        SLIME(63, ABW), // aka
        MAGMA_CUBE(37, SLIME), // ajr
        ARMOR_STAND(1, LIVINGENTITY), // ail
        AHN(-1, ENTITY), // ahn
        AIM(-1, ENTITY), // aim
        LEASH_KNOT(34, AIM), // aio
        ITEM_FRAME(32, AIM), // ain
        PAINTING(48, AIM), // aiq
        AIU(-1, ENTITY), // aiu
        LIGHTNING_BOLT(90, AIU), // aiv
        AKW(-1, ENTITY), // akw
        ARROW(2, AKW), // aky
        SPECTRAL_ARROW(67, AKW), // alk
        TRIDENT(93, AKW), // alq
        AKX(-1, ENTITY), // akx
        DRAGON_FIREBALL(12, AKX), // akz
        FIREBALL(33, AKX), // ald
        SMALL_FIREBALL(64, AKX), // ali
        WITHER_SKULL(84, AKX), // alr

        // Projectiles
        PROJECTILE_ABSTRACT(-1, ENTITY), // all
        SNOWBALL(66, PROJECTILE_ABSTRACT), // alj
        ENDER_PEARL(74, PROJECTILE_ABSTRACT), // aln
        EGG(73, PROJECTILE_ABSTRACT), // alm
        POTION(76, PROJECTILE_ABSTRACT), // alp
        XP_BOTTLE(75, PROJECTILE_ABSTRACT), // alo

        // Vehicles
        MINECART_ABSTRACT(-1, ENTITY), // alt
        CHESTED_MINECART_ABSTRACT(-1, MINECART_ABSTRACT), // alu
        CHEST_MINECART(39, CHESTED_MINECART_ABSTRACT), // alx
        HOPPER_MINECART(42, CHESTED_MINECART_ABSTRACT), // ama
        MINECART(38, MINECART_ABSTRACT), // alw
        FURNACE_MINECART(41, MINECART_ABSTRACT), // alz
        COMMANDBLOCK_MINECART(40, MINECART_ABSTRACT), // aly
        TNT_MINECART(44, MINECART_ABSTRACT), // amc
        SPAWNER_MINECART(43, MINECART_ABSTRACT), // amb
        BOAT(5, ENTITY); // alv


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
        BOAT(1, EntityType.BOAT),
        ITEM(2, EntityType.ITEM),
        AREA_EFFECT_CLOUD(3, EntityType.AREA_EFFECT_CLOUD),
        MINECART(10, EntityType.MINECART_ABSTRACT),
        TNT_PRIMED(50, EntityType.TNT),
        ENDER_CRYSTAL(51, EntityType.ENDER_CRYSTAL),
        TIPPED_ARROW(60, EntityType.ARROW),
        SNOWBALL(61, EntityType.SNOWBALL),
        EGG(62, EntityType.EGG),
        FIREBALL(63, EntityType.FIREBALL),
        SMALL_FIREBALL(64, EntityType.SMALL_FIREBALL),
        ENDER_PEARL(65, EntityType.ENDER_PEARL),
        WITHER_SKULL(66, EntityType.WITHER_SKULL),
        SHULKER_BULLET(67, EntityType.SHULKER_BULLET),
        LIAMA_SPIT(68, EntityType.LLAMA_SPIT),
        FALLING_BLOCK(70, EntityType.FALLING_BLOCK),
        ITEM_FRAME(71, EntityType.ITEM_FRAME),
        ENDER_SIGNAL(72, EntityType.EYE_OF_ENDER_SIGNAL),
        POTION(73, EntityType.POTION),
        THROWN_EXP_BOTTLE(75, EntityType.XP_BOTTLE),
        FIREWORK(76, EntityType.FIREWORKS_ROCKET),
        LEASH(77, EntityType.LEASH_KNOT),
        ARMOR_STAND(78, EntityType.ARMOR_STAND),
        EVOCATION_FANGS(79, EntityType.EVOCATION_FANGS),
        FISHIHNG_HOOK(90, EntityType.FISHING_BOBBER),
        SPECTRAL_ARROW(91, EntityType.SPECTRAL_ARROW),
        DRAGON_FIREBALL(93, EntityType.DRAGON_FIREBALL);

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

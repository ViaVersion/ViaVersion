package us.myles.ViaVersion.protocols.protocol1_14to1_13_2.data;

import com.google.common.base.Optional;

import java.util.HashMap;
import java.util.Map;

public class EntityTypeRewriter {
    private static Map<Integer, Integer> entityTypes = new HashMap<>();
    private static Map<Integer, Integer> objectTypes = new HashMap<>();

    static {
        regEnt(6, 7);  // cave_spider
        regEnt(7, 8); // chicken
        regEnt(8, 9); // cod
        regEnt(9, 10); // cow
        regEnt(10, 11); // creeper
        regEnt(11, 12); // donkey
        regEnt(12, 13); // dolphin
        regEnt(13, 14); // dragon_fireball
        regEnt(14, 15); // drowned
        regEnt(15, 16); // elder_guardian
        regEnt(16, 17); // end_crystal
        regEnt(17, 18); // ender_dragon
        regEnt(18, 19); // enderman
        regEnt(19, 20); // endermite
        regEnt(20, 21); // evoker_fangs
        regEnt(21, 22); // evoker
        regEnt(22, 23); // experience_orb
        regEnt(23, 24); // eye_of_ender
        regEnt(24, 25); // falling_block
        regEnt(25, 26); // firework_rocket
        regEnt(26, 27); // ghast
        regEnt(27, 28); // giant
        regEnt(28, 29); // guardian
        regEnt(29, 30); // husk
        regEnt(31, 32); // illusioner
        regEnt(32, 33); // item
        regEnt(33, 34); // item_frame
        regEnt(35, 36); // leash_knot
        regEnt(34, 35); // fireball
        regEnt(36, 37); // llama
        regEnt(37, 38); // llama_spit
        regEnt(38, 39); // magma_cube
        regEnt(39, 40); // minecart
        regEnt(40, 41); // chest_minecart
        regEnt(41, 42); // command_block_minecart
        regEnt(42, 43); // furnace_minecart
        regEnt(43, 44); // hopper_minecart
        regEnt(45, 46); // tnt_minecart
        regEnt(46, 47); // mule
        regEnt(47, 48); // mooshroom
        regEnt(48, 49); // ocelot
        regEnt(49, 50); // painting
        regEnt(50, 52); // parrot
        regEnt(51, 53); // pig
        regEnt(52, 54); // pufferfish
        regEnt(53, 55); // zombie_pigman
        regEnt(54, 56); // polar_bear
        regEnt(55, 57); // tnt
        regEnt(56, 58); // rabbit
        regEnt(57, 59); // salmon
        regEnt(58, 60); // sheep
        regEnt(59, 61); // shulker
        regEnt(60, 62); // shulker_bullet
        regEnt(61, 63); // silverfish
        regEnt(62, 64); // skeleton
        regEnt(63, 65); // skeleton_horse
        regEnt(64, 66); // slime
        regEnt(65, 67); // small_fireball
        regEnt(66, 68); // snowgolem
        regEnt(67, 69); // snowball
        regEnt(68, 70); // spectral_arrow
        regEnt(69, 71); // spider
        regEnt(70, 72); // squid
        regEnt(71, 73); // stray
        regEnt(72, 74); // tropical_fish
        regEnt(73, 75); // turtle
        regEnt(74, 76); // egg
        regEnt(75, 77); // ender_pearl
        regEnt(76, 78); // experience_bottle
        regEnt(77, 79); // potion
        regEnt(78, 80); // vex
        regEnt(79, 81); // villager
        regEnt(80, 82); // iron_golem
        regEnt(81, 83); // vindicator
        regEnt(82, 85); // witch
        regEnt(83, 86); // wither
        regEnt(84, 87); // wither_skeleton
        regEnt(85, 88); // wither_skull
        regEnt(86, 89); // wolf
        regEnt(87, 90); // zombie
        regEnt(88, 91); // zombie_horse
        regEnt(89, 92); // zombie_villager
        regEnt(90, 93); // phantom
        regEnt(91, 95); // lightning_bolt
        regEnt(92, 96); // player
        regEnt(93, 97); // fishing_bobber
        regEnt(94, 98); // trident
    }

    private static void regEnt(int type1_13, int type1_14) {
        entityTypes.put(type1_13, type1_14);
    }

    public static Optional<Integer> getNewId(int type1_13) {
        return Optional.fromNullable(entityTypes.get(type1_13));
    }
}

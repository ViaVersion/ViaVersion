package us.myles.ViaVersion.protocols.protocol1_14to1_13_2.data;

import com.google.common.base.Optional;

import java.util.HashMap;
import java.util.Map;

public class EntityTypeRewriter {
    private static Map<Integer, Integer> entityTypes = new HashMap<>();

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
        regEnt(26, 28); // ghast
        regEnt(27, 29); // giant
        regEnt(28, 30); // guardian
        regEnt(29, 31); // horse
        regEnt(30, 32); // husk
        regEnt(31, 33); // illusioner
        regEnt(32, 34); // item
        regEnt(33, 35); // item_frame
        regEnt(34, 36); // fireball
        regEnt(35, 37); // leash_knot
        regEnt(36, 38); // llama
        regEnt(37, 39); // llama_spit
        regEnt(38, 40); // magma_cube
        regEnt(39, 41); // minecart
        regEnt(40, 42); // chest_minecart
        regEnt(41, 43); // command_block_minecart
        regEnt(42, 44); // furnace_minecart
        regEnt(43, 45); // hopper_minecart
        regEnt(44, 46); // spawner_minecart
        regEnt(45, 47); // tnt_minecart
        regEnt(46, 48); // mule
        regEnt(47, 49); // mooshroom
        regEnt(48, 6); // ocelot -> cat TODO Remap untamed ocelot to ocelot?
        regEnt(49, 51); // painting
        regEnt(50, 53); // parrot
        regEnt(51, 54); // pig
        regEnt(52, 55); // pufferfish
        regEnt(53, 56); // zombie_pigman
        regEnt(54, 57); // polar_bear
        regEnt(55, 58); // tnt
        regEnt(56, 59); // rabbit
        regEnt(57, 60); // salmon
        regEnt(58, 61); // sheep
        regEnt(59, 62); // shulker
        regEnt(60, 63); // shulker_bullet
        regEnt(61, 64); // silverfish
        regEnt(62, 65); // skeleton
        regEnt(63, 66); // skeleton_horse
        regEnt(64, 67); // slime
        regEnt(65, 68); // small_fireball
        regEnt(66, 69); // snowgolem
        regEnt(67, 70); // snowball
        regEnt(68, 71); // spectral_arrow
        regEnt(69, 72); // spider
        regEnt(70, 73); // squid
        regEnt(71, 74); // stray
        regEnt(72, 76); // tropical_fish
        regEnt(73, 77); // turtle
        regEnt(74, 78); // egg
        regEnt(75, 79); // ender_pearl
        regEnt(76, 80); // experience_bottle
        regEnt(77, 81); // potion
        regEnt(78, 83); // vex
        regEnt(79, 84); // villager
        regEnt(80, 85); // iron_golem
        regEnt(81, 86); // vindicator
        regEnt(82, 89); // witch
        regEnt(83, 90); // wither
        regEnt(84, 91); // wither_skeleton
        regEnt(85, 92); // wither_skull
        regEnt(86, 93); // wolf
        regEnt(87, 94); // zombie
        regEnt(88, 95); // zombie_horse
        regEnt(89, 96); // zombie_villager
        regEnt(90, 97); // phantom
        regEnt(91, 99); // lightning_bolt
        regEnt(92, 100); // player
        regEnt(93, 101); // fishing_bobber
        regEnt(94, 82); // trident
    }

    private static void regEnt(int type1_13, int type1_14) {
        entityTypes.put(type1_13, type1_14);
    }

    public static Optional<Integer> getNewId(int type1_13) {
        return Optional.fromNullable(entityTypes.get(type1_13));
    }
}

package us.myles.ViaVersion.protocols.protocol1_14to1_13_2.data;

import com.google.common.base.Optional;

import java.util.HashMap;
import java.util.Map;

public class EntityTypeRewriter {
    private static Map<Integer, Integer> entityTypes = new HashMap<>();
    private static Map<Integer, Integer> objectTypes = new HashMap<>();

    static {
        regEnt(74, 75); // egg
        regEnt(75, 76); // ender_pearl
        regEnt(76, 77); // experience_bottle
        regEnt(93, 96); // fishing_bobber
        regEnt(80, 81); // iron_golem
        regEnt(91, 94); // lightning_bolt
        regEnt(50, 51); // parrot
        regEnt(90, 92); // phantom
        regEnt(51, 52); // pig
        regEnt(92, 95); // player
        regEnt(54, 55); // polar_bear
        regEnt(77, 78); // potion
        regEnt(52, 53); // pufferfish
        regEnt(56, 57); // rabbit
        regEnt(57, 58); // salmon
        regEnt(58, 59); // sheep
        regEnt(59, 60); // shulker
        regEnt(60, 61); // shulker_bullet
        regEnt(61, 62); // silverfish
        regEnt(62, 63); // skeleton
        regEnt(63, 64); // skeleton_horse
        regEnt(64, 65); // slime
        regEnt(65, 66); // small_fireball
        regEnt(66, 67); // snowgolem
        regEnt(67, 68); // snowball
        regEnt(68, 69); // spectral_arrow
        regEnt(69, 70); // spider
        regEnt(70, 71); // squid
        regEnt(71, 72); // stray
        regEnt(55, 56); // tnt
        regEnt(94, 97); // trident
        regEnt(72, 73); // tropical_fish
        regEnt(73, 74); // turtle
        regEnt(78, 79); // vex
        regEnt(79, 80); // villager
        regEnt(81, 82); // vindicator
        regEnt(82, 84); // witch
        regEnt(83, 85); // wither
        regEnt(84, 86); // wither_skeleton
        regEnt(85, 87); // wither_skull
        regEnt(86, 88); // wolf
        regEnt(87, 89); // zombie
        regEnt(88, 90); // zombie_horse
        regEnt(53, 54); // zombie_pigman
        regEnt(89, 91); // zombie_villager
    }

    private static void regEnt(int type1_13, int type1_14) {
        entityTypes.put(type1_13, type1_14);
    }

    public static Optional<Integer> getNewId(int type1_13) {
        return Optional.fromNullable(entityTypes.get(type1_13));
    }
}

package us.myles.ViaVersion.protocols.protocol1_16_2to1_16_1.data;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

public class BiomeMappings {

    private static final Int2IntMap BIOMES = new Int2IntOpenHashMap();

    static {
        BIOMES.put(127, 51);
        BIOMES.put(129, 52);
        BIOMES.put(130, 53);
        BIOMES.put(131, 54);
        BIOMES.put(132, 55);
        BIOMES.put(133, 56);
        BIOMES.put(134, 57);
        BIOMES.put(140, 58);
        BIOMES.put(149, 59);
        BIOMES.put(151, 60);
        BIOMES.put(155, 61);
        BIOMES.put(156, 62);
        BIOMES.put(157, 63);
        BIOMES.put(158, 64);
        BIOMES.put(160, 65);
        BIOMES.put(161, 66);
        BIOMES.put(162, 67);
        BIOMES.put(163, 68);
        BIOMES.put(164, 69);
        BIOMES.put(165, 70);
        BIOMES.put(166, 71);
        BIOMES.put(167, 72);
        BIOMES.put(168, 73);
        BIOMES.put(169, 74);
        BIOMES.put(170, 75);
        BIOMES.put(171, 76);
        BIOMES.put(172, 77);
        BIOMES.put(173, 78);
    }

    public static Int2IntMap getBiomes() {
        return BIOMES;
    }

    public static int getNewBiomeId(int biomeId) {
        return BIOMES.getOrDefault(biomeId, biomeId);
    }
}

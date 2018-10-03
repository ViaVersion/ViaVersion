package us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2.chunks;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.IntTag;
import com.github.steveice10.opennbt.tag.builtin.StringTag;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mojang changed the way how tile entities inside chunk packets work in 1.10.1
 * It requires now to have all tile entity data included in the chunk packet, otherwise it'll crash.
 */
public class FakeTileEntity {
    private static final Map<Integer, CompoundTag> tileEntities = new ConcurrentHashMap<>();

    static {
        register(Arrays.asList(61, 62), "Furnace");
        register(Arrays.asList(54, 146), "Chest");
        register(130, "EnderChest");
        register(84, "RecordPlayer");
        register(23, "Trap"); // Dispenser
        register(158, "Dropper");
        register(Arrays.asList(63, 68), "Sign");
        register(52, "MobSpawner");
        register(25, "Music"); // Note Block
        register(Arrays.asList(33, 34, 29, 36), "Piston");
        register(117, "Cauldron"); // Brewing stand
        register(116, "EnchantTable");
        register(Arrays.asList(119, 120), "Airportal"); // End portal
        register(138, "Beacon");
        register(144, "Skull");
        register(Arrays.asList(178, 151), "DLDetector");
        register(154, "Hopper");
        register(Arrays.asList(149, 150), "Comparator");
        register(140, "FlowerPot");
        register(Arrays.asList(176, 177), "Banner");
        register(209, "EndGateway");
        register(137, "Control");
    }

    private static void register(Integer material, String name) {
        CompoundTag comp = new CompoundTag("");
        comp.put(new StringTag(name));
        tileEntities.put(material, comp);
    }

    private static void register(List<Integer> materials, String name) {
        for (int m : materials)
            register(m, name);
    }

    public static boolean hasBlock(int block) {
        return tileEntities.containsKey(block);
    }

    public static CompoundTag getFromBlock(int x, int y, int z, int block) {
        CompoundTag originalTag = tileEntities.get(block);
        if (originalTag != null) {
            CompoundTag tag = originalTag.clone();
            tag.put(new IntTag("x", x));
            tag.put(new IntTag("y", y));
            tag.put(new IntTag("z", z));
            return tag;
        }
        return null;
    }
}

package us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2;

import org.bukkit.Material;
import org.spacehq.opennbt.tag.builtin.CompoundTag;
import org.spacehq.opennbt.tag.builtin.IntTag;
import org.spacehq.opennbt.tag.builtin.StringTag;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mojang changed the way how tile entities inside chunk packets work in 1.10.1
 * It requires now to have all tile entity data included in the chunk packet, otherwise it'll crash.
 */
public class FakeTileEntity {
    private static Map<Integer, CompoundTag> tileEntities = new ConcurrentHashMap<>();

    static {
        register(Material.FURNACE, "Furnace");
        register(Arrays.asList(Material.CHEST, Material.TRAPPED_CHEST), "Chest");
        register(Material.ENDER_CHEST, "EnderChest");
        register(Material.JUKEBOX, "RecordPlayer");
        register(Material.DISPENSER, "Trap");
        register(Material.DROPPER, "Dropper");
        register(Arrays.asList(Material.SIGN_POST, Material.WALL_SIGN), "Sign");
        register(Material.MOB_SPAWNER, "MobSpawner");
        register(Material.NOTE_BLOCK, "Music");
        register(Arrays.asList(Material.PISTON_BASE, Material.PISTON_EXTENSION, Material.PISTON_STICKY_BASE, Material.PISTON_MOVING_PIECE), "Piston");
        register(Arrays.asList(Material.BREWING_STAND, Material.CAULDRON), "Cauldron");
        register(Material.ENCHANTMENT_TABLE, "EnchantTable");
        register(Arrays.asList(Material.ENDER_PORTAL, Material.ENDER_PORTAL_FRAME), "Airportal");
        register(Material.BEACON, "Beacon");
        register(Arrays.asList(Material.SKULL, Material.SKULL_ITEM), "Skull");
        register(Arrays.asList(Material.DAYLIGHT_DETECTOR, Material.DAYLIGHT_DETECTOR_INVERTED), "DLDetector");
        register(Material.HOPPER, "Hopper");
        register(Arrays.asList(Material.REDSTONE_COMPARATOR, Material.REDSTONE_COMPARATOR_OFF, Material.REDSTONE_COMPARATOR_ON), "Comparator");
        register(Material.FLOWER_POT, "FlowerPot");
        register(Arrays.asList(Material.STANDING_BANNER, Material.WALL_BANNER, Material.BANNER), "Banner");
        register(209, "EndGateway");
        register(Material.COMMAND.getId(), "Control");
    }

    private static void register(Integer material, String name) {
        CompoundTag comp = new CompoundTag("");
        comp.put(new StringTag(name));
        tileEntities.put(material, comp);
    }

    private static void register(Material material, String name) {
        register(material.getId(), name);
    }

    private static void register(List<Material> materials, String name) {
        for (Material m : materials)
            register(m.getId(), name);
    }

    public static boolean hasBlock(int block) {
        return tileEntities.containsKey(block);
    }

    public static CompoundTag getFromBlock(int x, int y, int z, int block) {
        if (tileEntities.containsKey(block)) {
            CompoundTag tag = tileEntities.get(block).clone();
            tag.put(new IntTag("x", x));
            tag.put(new IntTag("y", y));
            tag.put(new IntTag("z", z));
            return tag;
        }
        return null;
    }
}

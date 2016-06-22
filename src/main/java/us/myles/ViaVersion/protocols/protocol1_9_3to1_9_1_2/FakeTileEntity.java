package us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2;

import org.bukkit.Material;
import org.spacehq.opennbt.tag.builtin.*;

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
        register(Material.FURNACE, "Furnace",
                new ShortTag("BurnTime"),
                new ShortTag("CookTime"),
                new ShortTag("CookTimeTotal")); // Maybe items todo test
        register(Arrays.asList(Material.CHEST, Material.TRAPPED_CHEST), "Chest",
                new ByteArrayTag("Items")); // Todo test
        register(Material.ENDER_CHEST, "EnderChest");
        register(Material.JUKEBOX, "RecordPlayer"); // No required tags found, todo test
        register(Material.DISPENSER, "Trap"); // todo test
        register(Material.DROPPER, "Dropper"); // todo test
        register(Arrays.asList(Material.SIGN_POST, Material.WALL_SIGN), "Sign",
                new StringTag("Text1", ""),
                new StringTag("Text2", ""),
                new StringTag("Text3", ""),
                new StringTag("Text4", "")); // todo test
        register(Material.MOB_SPAWNER, "MobSpawner"); // todo test
        register(Material.NOTE_BLOCK, "Music",
                new ByteTag("note"),
                new ByteTag("enabled")); // todo test
        register(Material.PISTON_BASE, "Piston",
                new IntTag("blockId"),
                new IntTag("blockData"),
                new IntTag("facing"),
                new FloatTag("progress"),
                new ByteTag("extending")); //TODO test, maybe assign values to them instead of 0.
        register(Arrays.asList(Material.BREWING_STAND, Material.CAULDRON), "Cauldron",
                new ShortTag("BrewTime"),
                new ByteArrayTag("Items")
        ); //todo test both, spigot tells me this is a brewing stand.
        register(Material.ENCHANTMENT_TABLE, "EnchantTable"); //// TODO: test
        register(Arrays.asList(Material.ENDER_PORTAL, Material.ENDER_PORTAL_FRAME), "Airportal"); //todo test
        register(Material.BEACON, "Beacon",
                new IntTag("Primary"),
                new IntTag("Secondary"),
                new IntTag("Levels")); //todo test
        register(Arrays.asList(Material.SKULL, Material.SKULL_ITEM), "Skull",
                new ByteTag("SkullType"),
                new ByteTag("Rot")); //todo test
        register(Arrays.asList(Material.DAYLIGHT_DETECTOR, Material.DAYLIGHT_DETECTOR_INVERTED), "DLDetector"); //todo test
        register(Material.HOPPER, "Hopper",
                new IntTag("TransferCooldown")); // todo test
        register(Arrays.asList(Material.REDSTONE_COMPARATOR, Material.REDSTONE_COMPARATOR_OFF, Material.REDSTONE_COMPARATOR_ON), "Comparator",
                new IntTag("OutputSignal")); //todo test
        register(Material.FLOWER_POT, "FlowerPot",
                new StringTag("Item", ""),
                new IntTag("Data")); //todo test
        register(Arrays.asList(Material.STANDING_BANNER, Material.WALL_BANNER, Material.BANNER), "Banner",
                new IntTag("Base")); //todo test
        register(209, "EndGateway", new LongTag("age")); // todo test
        register(Material.COMMAND.getId(), "Control",
                new StringTag("Command", "ViaVersion"),
                new IntTag("SuccessCount"),
                new StringTag("CustomName", "ViaVersion"),
                new ByteTag("TrackOutput"),
                new ByteTag("powered"),
                new ByteTag("conditionMet"),
                new ByteTag("auto")); // todo test


    }

    private static void register(Integer material, String name, Tag... tags) {
        CompoundTag comp = new CompoundTag("");
        comp.put(new StringTag(name));
        for (Tag tag : tags)
            comp.put(tag);
        tileEntities.put(material, comp);
    }

    private static void register(Material material, String name, Tag... tags) {
        register(material.getId(), name, tags);
    }

    private static void register(List<Material> materials, String name, Tag... tags) {
        for (Material m : materials)
            register(m.getId(), name, tags);
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

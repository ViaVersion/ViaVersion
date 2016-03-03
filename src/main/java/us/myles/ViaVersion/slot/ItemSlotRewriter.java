package us.myles.ViaVersion.slot;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.spacehq.opennbt.tag.builtin.CompoundTag;
import org.spacehq.opennbt.tag.builtin.StringTag;

import io.netty.buffer.ByteBuf;

import us.myles.ViaVersion.CancelException;
import us.myles.ViaVersion.util.PacketUtil;

public class ItemSlotRewriter {

    public static void rewrite1_9To1_8(ByteBuf input, ByteBuf output) throws CancelException {
        try {
            ItemStack item = readItemStack(input);
            fixIdsFrom1_9To1_8(item);
            writeItemStack(item, output);
        } catch (Exception e) {
            System.out.println("Error while rewriting an item slot.");
            e.printStackTrace();
            throw new CancelException();
        }
    }

    public static void rewrite1_8To1_9(ByteBuf input, ByteBuf output) throws CancelException {
        try {
            ItemStack item = readItemStack(input);
            fixIdsFrom1_8To1_9(item);
            writeItemStack(item, output);
        } catch (Exception e) {
            System.out.println("Error while rewriting an item slot.");
            e.printStackTrace();
            throw new CancelException();
        }
    }

    public static void fixIdsFrom1_9To1_8(ItemStack item) {
        if (item != null) {
            if (item.id == Material.MONSTER_EGG.getId() && item.data == 0) {
                CompoundTag tag = item.tag;
                int data = 0;
                if (tag != null && tag.get("EntityTag") instanceof CompoundTag) {
                    CompoundTag entityTag = tag.get("EntityTag");
                    if (entityTag.get("id") instanceof StringTag) {
                        StringTag id = entityTag.get("id");
                        data = ENTTIY_NAME_TO_ID.get(id.getValue());
                    }
                }
                item.tag = null;
                item.data = (short) data;
            }
        }
    }

    public static void fixIdsFrom1_8To1_9(ItemStack item) {
        if (item != null) {
            if (item.id == Material.MONSTER_EGG.getId() && item.data != 0) {
                CompoundTag tag = item.tag;
                if (tag == null) {
                    tag = new CompoundTag("tag");
                }
                CompoundTag entityTag = new CompoundTag("EntityTag");
                StringTag id = new StringTag("id", ENTTIY_ID_TO_NAME.get(Integer.valueOf(item.data)));
                entityTag.put(id);
                tag.put(entityTag);
                item.tag = tag;
                item.data = 0;
            }
        }
    }

    public static ItemStack readItemStack(ByteBuf input) throws IOException {
        short id = input.readShort();
        if (id < 0) {
            return null;
        } else {
            ItemStack item = new ItemStack();
            item.id = id;
            item.amount = input.readByte();
            item.data = input.readShort();
            item.tag = PacketUtil.readNBT(input);
            return item;
        }
    }

    public static void writeItemStack(ItemStack item, ByteBuf output) throws IOException {
        if (item == null) {
            output.writeShort(-1);
        } else {
            output.writeShort(item.id);
            output.writeByte(item.amount);
            output.writeShort(item.data);
            PacketUtil.writeNBT(output, item.tag);
        }
    }

    public static class ItemStack {

        private short id;
        private byte amount;
        private short data;
        private CompoundTag tag;

        public static ItemStack fromBukkit(org.bukkit.inventory.ItemStack stack) {
            ItemStack item = new ItemStack();
            item.id = (short) stack.getTypeId();
            item.amount = (byte) stack.getAmount();
            item.data = stack.getData().getData();
            // TODO: nbt
            return item;
        }
    }

    private static Map<String, Integer> ENTTIY_NAME_TO_ID = new HashMap<>();
    private static Map<Integer, String> ENTTIY_ID_TO_NAME = new HashMap<>();

    static {
        register(1, "Item");
        register(2, "XPOrb");
        register(7, "ThrownEgg");
        register(8, "LeashKnot");
        register(9, "Painting");
        register(10, "Arrow");
        register(11, "Snowball");
        register(12, "Fireball");
        register(13, "SmallFireball");
        register(14, "ThrownEnderpearl");
        register(15, "EyeOfEnderSignal");
        register(16, "ThrownPotion");
        register(17, "ThrownExpBottle");
        register(18, "ItemFrame");
        register(19, "WitherSkull");
        register(20, "PrimedTnt");
        register(21, "FallingSand");
        register(22, "FireworksRocketEntity");
        register(30, "ArmorStand");
        register(40, "MinecartCommandBlock");
        register(41, "Boat");
        register(42, "MinecartRideable");
        register(43, "MinecartChest");
        register(44, "MinecartFurnace");
        register(45, "MinecartTNT");
        register(46, "MinecartHopper");
        register(47, "MinecartSpawner");
        register(48, "Mob");
        register(49, "Monster");
        register(50, "Creeper");
        register(51, "Skeleton");
        register(52, "Spider");
        register(53, "Giant");
        register(54, "Zombie");
        register(55, "Slime");
        register(56, "Ghast");
        register(57, "PigZombie");
        register(58, "Enderman");
        register(59, "CaveSpider");
        register(60, "Silverfish");
        register(61, "Blaze");
        register(62, "LavaSlime");
        register(63, "EnderDragon");
        register(64, "WitherBoss");
        register(65, "Bat");
        register(66, "Witch");
        register(67, "Endermite");
        register(68, "Guardian");
        register(90, "Pig");
        register(91, "Sheep");
        register(92, "Cow");
        register(93, "Chicken");
        register(94, "Squid");
        register(95, "Wolf");
        register(96, "MushroomCow");
        register(97, "SnowMan");
        register(98, "Ozelot");
        register(99, "VillagerGolem");
        register(100, "EntityHorse");
        register(101, "Rabbit");
        register(120, "Villager");
        register(200, "EnderCrystal");
    }

    private static void register(Integer id, String name) {
        ENTTIY_ID_TO_NAME.put(id, name);
        ENTTIY_NAME_TO_ID.put(name, id);
    }
}

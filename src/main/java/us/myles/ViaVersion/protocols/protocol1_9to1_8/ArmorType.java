package us.myles.ViaVersion.protocols.protocol1_9to1_8;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

@RequiredArgsConstructor
@Getter
public enum ArmorType {

    LEATHER_HELMET(1, 298, Material.LEATHER_HELMET),
    LEATHER_CHESTPLATE(3, 299, Material.LEATHER_CHESTPLATE),
    LEATHER_LEGGINGS(2, 300, Material.LEATHER_LEGGINGS),
    LEATHER_BOOTS(1, 301, Material.LEATHER_BOOTS),
    CHAINMAIL_HELMET(2, 302, Material.CHAINMAIL_HELMET),
    CHAINMAIL_CHESTPLATE(5, 303, Material.CHAINMAIL_CHESTPLATE),
    CHAINMAIL_LEGGINGS(4, 304, Material.CHAINMAIL_LEGGINGS),
    CHAINMAIL_BOOTS(1, 305, Material.CHAINMAIL_BOOTS),
    IRON_HELMET(2, 306, Material.IRON_HELMET),
    IRON_CHESTPLATE(6, 307, Material.IRON_CHESTPLATE),
    IRON_LEGGINGS(5, 308, Material.IRON_LEGGINGS),
    IRON_BOOTS(2, 309, Material.IRON_BOOTS),
    DIAMOND_HELMET(3, 310, Material.DIAMOND_HELMET),
    DIAMOND_CHESTPLATE(8, 311, Material.DIAMOND_CHESTPLATE),
    DIAMOND_LEGGINGS(6, 312, Material.DIAMOND_LEGGINGS),
    DIAMOND_BOOTS(3, 313, Material.DIAMOND_BOOTS),
    GOLD_HELMET(2, 314, Material.GOLD_HELMET),
    GOLD_CHESTPLATE(5, 315, Material.GOLD_CHESTPLATE),
    GOLD_LEGGINGS(3, 316, Material.GOLD_LEGGINGS),
    GOLD_BOOTS(1, 317, Material.GOLD_BOOTS),
    NONE(0, 0, Material.AIR);

    private static HashMap<Material, ArmorType> armor;

    static {
        armor = new HashMap<Material, ArmorType>();
        for (ArmorType a : ArmorType.values()) {
            armor.put(a.getType(), a);
        }
    }

    private final int armorPoints;
    private final int id;
    private final Material type;

    public static ArmorType findByType(Material type) {
        ArmorType t = armor.get(type);
        return t == null ? ArmorType.NONE : t;
    }

    public static int calculateArmorPoints(ItemStack[] armor) {
        int total = 0;
        for (ItemStack anArmor : armor) {
            if (anArmor != null)
                total += findByType(anArmor.getType()).getArmorPoints();
        }
        return total;
    }

    public static ArmorType findById(int id) {
        for (ArmorType a : ArmorType.values())
            if (a.getId() == id)
                return a;
        return ArmorType.NONE;
    }

    public static boolean isArmor(Material material) {
        for (ArmorType a : ArmorType.values())
            if (a.getType() == material)
                return true;
        return false;
    }

    public static int calculateArmorPoints(int[] armor) {
        int total = 0;
        for (int anArmor : armor) {
            if (anArmor != -1)
                total += findById(anArmor).getArmorPoints();
        }
        return total;
    }

    public Material getType() {
        return this.type;
    }

}
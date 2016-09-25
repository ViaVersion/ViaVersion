package us.myles.ViaVersion.protocols.protocol1_9to1_8;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;

@RequiredArgsConstructor
@Getter
public enum ArmorType {

    LEATHER_HELMET(1, 298),
    LEATHER_CHESTPLATE(3, 299),
    LEATHER_LEGGINGS(2, 300),
    LEATHER_BOOTS(1, 301),
    CHAINMAIL_HELMET(2, 302),
    CHAINMAIL_CHESTPLATE(5, 303),
    CHAINMAIL_LEGGINGS(4, 304),
    CHAINMAIL_BOOTS(1, 305),
    IRON_HELMET(2, 306),
    IRON_CHESTPLATE(6, 307),
    IRON_LEGGINGS(5, 308),
    IRON_BOOTS(2, 309),
    DIAMOND_HELMET(3, 310),
    DIAMOND_CHESTPLATE(8, 311),
    DIAMOND_LEGGINGS(6, 312),
    DIAMOND_BOOTS(3, 313),
    GOLD_HELMET(2, 314),
    GOLD_CHESTPLATE(5, 315),
    GOLD_LEGGINGS(3, 316),
    GOLD_BOOTS(1, 317),
    NONE(0, 0);

    private static HashMap<Integer, ArmorType> armor;

    static {
        armor = new HashMap<>();
        for (ArmorType a : ArmorType.values()) {
            armor.put(a.getId(), a);
        }
    }

    private final int armorPoints;
    private final int id;

    public static ArmorType findById(int id) {
        for (ArmorType a : ArmorType.values())
            if (a.getId() == id)
                return a;
        return ArmorType.NONE;
    }

    public static boolean isArmor(int id) {
        for (ArmorType a : ArmorType.values())
            if (a.getId() == id)
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

    public int getId() {
        return this.id;
    }
}
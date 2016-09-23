package us.myles.ViaVersion.util;

import org.bukkit.entity.EntityType;

public class EntityUtil {

    public static EntityType getTypeFromID(int typeID, boolean isObject) {
        if (isObject) {
            return getObjectFromID(typeID);
        } else {
            return EntityType.fromId(typeID);
        }
    }

    // based on http://wiki.vg/index.php?title=Entities
    public static EntityType getObjectFromID(int objectID) {
        EntityType type;
        switch (objectID) {
            case 2:
                type = EntityType.DROPPED_ITEM;
                break;
            case 77:
                type = EntityType.LEASH_HITCH;
                break;
            case 60:
                type = EntityType.ARROW;
                break;
            case 61:
                type = EntityType.SNOWBALL;
                break;
            case 63:
                type = EntityType.FIREBALL;
                break;
            case 64:
                type = EntityType.SMALL_FIREBALL;
                break;
            case 65:
                type = EntityType.ENDER_PEARL;
                break;
            case 72:
                type = EntityType.ENDER_SIGNAL;
                break;
            case 75:
                type = EntityType.THROWN_EXP_BOTTLE;
                break;
            case 71:
                type = EntityType.ITEM_FRAME;
                break;
            case 66:
                type = EntityType.WITHER_SKULL;
                break;
            case 50:
                type = EntityType.PRIMED_TNT;
                break;
            case 70:
                type = EntityType.FALLING_BLOCK;
                break;
            case 76:
                type = EntityType.FIREWORK;
                break;
            case 78:
                type = EntityType.ARMOR_STAND;
                break;
            case 1:
                type = EntityType.BOAT;
                break;
            case 10:
                type = EntityType.MINECART;
                break;
            case 51:
                type = EntityType.ENDER_CRYSTAL;
                break;
            case 73:
                type = EntityType.SPLASH_POTION;
                break;
            case 62:
                type = EntityType.EGG;
                break;
            case 90:
                type = EntityType.FISHING_HOOK;
                break;
            default:
                type = EntityType.fromId(objectID);
                if (type == null) {
                    System.out.println("Unable to find entity type for " + objectID);
                    type = EntityType.UNKNOWN;
                }
                break;
        }
        return type;
    }
}

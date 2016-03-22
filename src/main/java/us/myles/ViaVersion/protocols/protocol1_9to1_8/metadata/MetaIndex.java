package us.myles.ViaVersion.protocols.protocol1_9to1_8.metadata;

import lombok.Getter;
import org.bukkit.entity.*;

@Getter
public enum MetaIndex {

    // entity
    ENTITY_STATUS(org.bukkit.entity.Entity.class, 0, Type.Byte, NewType.Byte),
    ENTITY_AIR(org.bukkit.entity.Entity.class, 1, Type.Short, NewType.VarInt),
    ENTITY_SILENT(org.bukkit.entity.Entity.class, 4, Type.Byte, NewType.Boolean),
    // living entity
    LIVINGENTITY_NAMETAG(LivingEntity.class, 2, Type.String, NewType.String),
    LIVINGENTITY_ALWAYS_SHOW_NAMETAG(LivingEntity.class, 3, Type.Byte, NewType.Boolean),
    LIVINGENTITY_HEALTH(LivingEntity.class, 6, Type.Float, NewType.Float),
    LIVINGENTITY_POTION_EFFECT_COLOR(LivingEntity.class, 7, Type.Int, NewType.VarInt),
    LIVINGENTITY_IS_POTION_AMBIENT(LivingEntity.class, 8, Type.Byte, NewType.Boolean),
    LIVINGENTITY_NUMBER_OF_ARROWS_IN(LivingEntity.class, 9, Type.Byte, NewType.VarInt),
    LIVINGENTITY_NO_AI(LivingEntity.class, 15, Type.Byte, 10, NewType.Byte), // in 1.9 this is combined with Left handed, oh.
    // ageable
    AGEABLE_AGE(Ageable.class, 12, Type.Byte, 11, NewType.Boolean),
    // armour stand
    STAND_INFO(ArmorStand.class, 10, Type.Byte, NewType.Byte),
    STAND_HEAD_POS(ArmorStand.class, 11, Type.Rotation, NewType.Vector3F),
    STAND_BODY_POS(ArmorStand.class, 12, Type.Rotation, NewType.Vector3F),
    STAND_LA_POS(ArmorStand.class, 13, Type.Rotation, NewType.Vector3F),
    STAND_RA_POS(ArmorStand.class, 14, Type.Rotation, NewType.Vector3F),
    STAND_LL_POS(ArmorStand.class, 15, Type.Rotation, NewType.Vector3F),
    STAND_RL_POS(ArmorStand.class, 16, Type.Rotation, NewType.Vector3F),
    // human, discountined?
    PLAYER_SKIN_FLAGS(HumanEntity.class, 10, Type.Byte, 12, NewType.Byte), // unsigned on 1.8
    PLAYER_HUMAN_BYTE(HumanEntity.class, 16, Type.Byte, NewType.Discontinued), // unused on 1.8
    PLAYER_ADDITIONAL_HEARTS(HumanEntity.class, 17, Type.Float, 10, NewType.Float),
    PLAYER_SCORE(HumanEntity.class, 18, Type.Int, 11, NewType.VarInt),
    PLAYER_HAND(HumanEntity.class, -1, Type.NonExistent, 5, NewType.Byte), // new in 1.9
    SOMETHING_ANTICHEAT_PLUGINS_FOR_SOME_REASON_USE(HumanEntity.class, 11, Type.Byte, NewType.Discontinued), //For what we know, This doesn't exists. If you think it exists and knows what it does. Please tell us.
    // horse
    HORSE_INFO(Horse.class, 16, Type.Int, 12, NewType.Byte),
    HORSE_TYPE(Horse.class, 19, Type.Byte, 13, NewType.VarInt),
    HORSE_SUBTYPE(Horse.class, 20, Type.Int, 14, NewType.VarInt),
    HORSE_OWNER(Horse.class, 21, Type.String, 15, NewType.OptUUID),
    HORSE_ARMOR(Horse.class, 22, Type.Int, 16, NewType.VarInt),
    // bat
    BAT_ISHANGING(Bat.class, 16, Type.Byte, 11, NewType.Byte),
    // tameable
    TAMING_INFO(Tameable.class, 16, Type.Byte, 12, NewType.Byte),
    TAMING_OWNER(Tameable.class, 17, Type.String, 13, NewType.OptUUID),
    // ocelot
    OCELOT_TYPE(Ocelot.class, 18, Type.Byte, 14, NewType.VarInt),
    // wolf
    WOLF_HEALTH(Wolf.class, 18, Type.Float, 14, NewType.Float),
    WOLF_BEGGING(Wolf.class, 19, Type.Byte, 15, NewType.Boolean),
    WOLF_COLLAR(Wolf.class, 20, Type.Byte, 16, NewType.VarInt),
    // pig
    PIG_SADDLE(Pig.class, 16, Type.Byte, 12, NewType.Boolean),
    // rabbit
    RABBIT_TYPE(Rabbit.class, 18, Type.Byte, 12, NewType.VarInt),
    // sheep
    SHEEP_COLOR(Sheep.class, 16, Type.Byte, 12, NewType.Byte),
    // villager
    VILLAGER_PROFESSION(Villager.class, 16, Type.Int, 12, NewType.VarInt),
    // enderman
    ENDERMAN_BLOCK(Enderman.class, 16, Type.Short, 11, NewType.BlockID), // special case
    ENDERMAN_BLOCKDATA(Enderman.class, 17, Type.Byte, 11, NewType.BlockID), // special case
    ENDERMAN_ISSCREAMING(Enderman.class, 18, Type.Byte, 12, NewType.Boolean),
    // zombie
    ZOMBIE_ISCHILD(Zombie.class, 12, Type.Byte, 11, NewType.Boolean),
    ZOMBIE_ISVILLAGER(Zombie.class, 13, Type.Byte, 12, NewType.VarInt),
    ZOMBIE_ISCONVERTING(Zombie.class, 14, Type.Byte, 13, NewType.Boolean),
    // ZOMBIE_RISINGHANDS added in 1.9
    // blaze
    BLAZE_ONFIRE(Blaze.class, 16, Type.Byte, 11, NewType.Byte),
    // spider
    SPIDER_CIMBING(Spider.class, 16, Type.Byte, 11, NewType.Byte),
    // creeper
    CREEPER_FUSE(Creeper.class, 16, Type.Byte, 11, NewType.VarInt), // -1 idle, 1 is fuse
    CREEPER_ISPOWERED(Creeper.class, 17, Type.Byte, 12, NewType.Boolean),
    CREEPER_ISIGNITED(Creeper.class, 18, Type.Byte, 13, NewType.Boolean),
    // ghast
    GHAST_ISATTACKING(Ghast.class, 16, Type.Byte, 11, NewType.Boolean),
    // slime
    SLIME_SIZE(Slime.class, 16, Type.Byte, 11, NewType.VarInt),
    // skeleton
    SKELETON_TYPE(Skeleton.class, 13, Type.Byte, 11, NewType.VarInt),
    // witch
    WITCH_AGGRO(Witch.class, 21, Type.Byte, 11, NewType.Boolean),
    // iron golem
    IRON_PLAYERMADE(IronGolem.class, 16, Type.Byte, 11, NewType.Byte),
    // wither
    WITHER_TARGET1(Wither.class, 17, Type.Int, 11, NewType.VarInt),
    WITHER_TARGET2(Wither.class, 18, Type.Int, 12, NewType.VarInt),
    WITHER_TARGET3(Wither.class, 19, Type.Int, 13, NewType.VarInt),
    WITHER_INVULN_TIME(Wither.class, 20, Type.Int, 14, NewType.VarInt),
    WITHER_PROPERTIES(Wither.class, 10, Type.Byte, NewType.Byte),
    WITHER_UNKNOWN(Wither.class, 11, Type.Byte, NewType.Discontinued),
    // wither skull
    WITHERSKULL_INVULN(WitherSkull.class, 10, Type.Byte, 5, NewType.Boolean),
    // guardian
    GUARDIAN_INFO(Guardian.class, 16, Type.Int, 11, NewType.Byte),
    GUARDIAN_TARGET(Guardian.class, 17, Type.Int, 12, NewType.VarInt),
    // boat
    BOAT_SINCEHIT(Boat.class, 17, Type.Int, 5, NewType.VarInt),
    BOAT_FORWARDDIR(Boat.class, 18, Type.Int, 6, NewType.VarInt),
    BOAT_DMGTAKEN(Boat.class, 19, Type.Float, 7, NewType.Float),
    // BOAT_TYPE in 1.9
    // minecart
    MINECART_SHAKINGPOWER(Minecart.class, 17, Type.Int, 5, NewType.VarInt),
    MINECART_SHAKINGDIRECTION(Minecart.class, 18, Type.Int, 6, NewType.VarInt),
    MINECART_DAMAGETAKEN(Minecart.class, 19, Type.Float, 7, NewType.Float), // also shaking modifier :P
    MINECART_BLOCK(Minecart.class, 20, Type.Int, 8, NewType.VarInt),
    MINECART_BLOCK_Y(Minecart.class, 21, Type.Int, 9, NewType.VarInt),
    MINECART_SHOWBLOCK(Minecart.class, 22, Type.Byte, 10, NewType.Boolean),
    // Command minecart (they are still broken)
    MINECART_COMMANDBLOCK_COMMAND(Minecart.class, 23, Type.String, 11, NewType.String),
    MINECART_COMMANDBLOCK_OUTPUT(Minecart.class, 24, Type.String, 12, NewType.Chat),
    // furnace cart
    FURNACECART_ISPOWERED(Minecart.class, 16, Type.Byte, 11, NewType.Boolean),
    // item drop
    ITEM_ITEM(Item.class, 10, Type.Slot, 5, NewType.Slot),
    // arrow
    ARROW_ISCRIT(Arrow.class, 16, Type.Byte, 5, NewType.Byte),
    // firework
    FIREWORK_INFO(Firework.class, 8, Type.Slot, 5, NewType.Slot),
    // item frame
    ITEMFRAME_ITEM(ItemFrame.class, 8, Type.Slot, 5, NewType.Slot),
    ITEMFRAME_ROTATION(ItemFrame.class, 9, Type.Byte, 6, NewType.VarInt),
    // ender crystal
    ENDERCRYSTAL_HEALTH(EnderCrystal.class, 8, Type.Int, NewType.Discontinued),
    // Ender dragon boss bar issues
    ENDERDRAGON_UNKNOWN(EnderDragon.class, 5, Type.Byte, NewType.Discontinued),
    ENDERDRAGON_NAME(EnderDragon.class, 10, Type.String, NewType.Discontinued),
    // Normal Ender dragon
    ENDERDRAGON_FLAG(EnderDragon.class, 15, Type.Byte, NewType.Discontinued),
    ENDERDRAGON_PHASE(EnderDragon.class, 11, Type.Byte, NewType.VarInt);

    private Class<?> clazz;
    private int newIndex;
    private NewType newType;
    private Type oldType;
    private int index;

    MetaIndex(Class<?> type, int index, Type oldType, NewType newType) {
        this.clazz = type;
        this.index = index;
        this.newIndex = index;
        this.oldType = oldType;
        this.newType = newType;
    }

    MetaIndex(Class<?> type, int index, Type oldType, int newIndex, NewType newType) {
        this.clazz = type;
        this.index = index;
        this.oldType = oldType;
        this.newIndex = newIndex;
        this.newType = newType;
    }

    public static MetaIndex getIndex(EntityType type, int index) {
        Class<? extends org.bukkit.entity.Entity> entityClass = type.getEntityClass();
        if (entityClass == null) {
            System.out.println("Could not get entity class for " + type);
            return null;
        }
        for (MetaIndex mi : MetaIndex.values()) {
            if (mi.getIndex() == index) {
                // To fix issue with armour stands colliding with new values
                if (mi.getApplicableClass().equals(LivingEntity.class)) continue;

                if ((mi.getApplicableClass().isAssignableFrom(entityClass) ||
                        mi.getApplicableClass().equals(entityClass))) {
                    return mi;
                }
            }
        }
        // fall back to living entity
        for (MetaIndex mi : MetaIndex.values()) {
            if (mi.getIndex() == index) {
                if (mi.getApplicableClass().isAssignableFrom(LivingEntity.class) ||
                        mi.getApplicableClass().equals(LivingEntity.class)) {
                    return mi;
                }
            }
        }
        return null;
    }

    public Class<?> getApplicableClass() {
        return this.clazz;
    }
}


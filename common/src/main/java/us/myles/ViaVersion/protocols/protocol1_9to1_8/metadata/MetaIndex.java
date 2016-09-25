package us.myles.ViaVersion.protocols.protocol1_9to1_8.metadata;

import com.google.common.base.Optional;
import lombok.Getter;
import us.myles.ViaVersion.api.Pair;
import us.myles.ViaVersion.util.EntityUtil;

import java.util.HashMap;

import static us.myles.ViaVersion.util.EntityUtil.EntityType.*;

@Getter
public enum MetaIndex {

    // entity
    ENTITY_STATUS(ENTITY, 0, Type.Byte, NewType.Byte),
    ENTITY_AIR(ENTITY, 1, Type.Short, NewType.VarInt),
    ENTITY_SILENT(ENTITY, 4, Type.Byte, NewType.Boolean),
    // living entity
    LIVINGENTITY_NAMETAG(ENTITY_LIVING, 2, Type.String, NewType.String),
    LIVINGENTITY_ALWAYS_SHOW_NAMETAG(ENTITY_LIVING, 3, Type.Byte, NewType.Boolean),
    LIVINGENTITY_HEALTH(ENTITY_LIVING, 6, Type.Float, NewType.Float),
    LIVINGENTITY_POTION_EFFECT_COLOR(ENTITY_LIVING, 7, Type.Int, NewType.VarInt),
    LIVINGENTITY_IS_POTION_AMBIENT(ENTITY_LIVING, 8, Type.Byte, NewType.Boolean),
    LIVINGENTITY_NUMBER_OF_ARROWS_IN(ENTITY_LIVING, 9, Type.Byte, NewType.VarInt),
    LIVINGENTITY_NO_AI(ENTITY_LIVING, 15, Type.Byte, 10, NewType.Byte), // in 1.9 this is combined with Left handed, oh.
    // ageable
    AGEABLE_AGE(ENTITY_AGEABLE, 12, Type.Byte, 11, NewType.Boolean),
    // armour stand
    STAND_INFO(ARMOR_STAND, 10, Type.Byte, NewType.Byte),
    STAND_HEAD_POS(ARMOR_STAND, 11, Type.Rotation, NewType.Vector3F),
    STAND_BODY_POS(ARMOR_STAND, 12, Type.Rotation, NewType.Vector3F),
    STAND_LA_POS(ARMOR_STAND, 13, Type.Rotation, NewType.Vector3F),
    STAND_RA_POS(ARMOR_STAND, 14, Type.Rotation, NewType.Vector3F),
    STAND_LL_POS(ARMOR_STAND, 15, Type.Rotation, NewType.Vector3F),
    STAND_RL_POS(ARMOR_STAND, 16, Type.Rotation, NewType.Vector3F),
    // human, discountined?
    PLAYER_SKIN_FLAGS(ENTITY_HUMAN, 10, Type.Byte, 12, NewType.Byte), // unsigned on 1.8
    PLAYER_HUMAN_BYTE(ENTITY_HUMAN, 16, Type.Byte, NewType.Discontinued), // unused on 1.8
    PLAYER_ADDITIONAL_HEARTS(ENTITY_HUMAN, 17, Type.Float, 10, NewType.Float),
    PLAYER_SCORE(ENTITY_HUMAN, 18, Type.Int, 11, NewType.VarInt),
    PLAYER_HAND(ENTITY_HUMAN, -1, Type.NonExistent, 5, NewType.Byte), // new in 1.9
    SOMETHING_ANTICHEAT_PLUGINS_FOR_SOME_REASON_USE(ENTITY_HUMAN, 11, Type.Byte, NewType.Discontinued), //For what we know, This doesn't exists. If you think it exists and knows what it does. Please tell us.
    // horse
    HORSE_INFO(HORSE, 16, Type.Int, 12, NewType.Byte),
    HORSE_TYPE(HORSE, 19, Type.Byte, 13, NewType.VarInt),
    HORSE_SUBTYPE(HORSE, 20, Type.Int, 14, NewType.VarInt),
    HORSE_OWNER(HORSE, 21, Type.String, 15, NewType.OptUUID),
    HORSE_ARMOR(HORSE, 22, Type.Int, 16, NewType.VarInt),
    // bat
    BAT_ISHANGING(BAT, 16, Type.Byte, 11, NewType.Byte),
    // tameable
    TAMING_INFO(ENTITY_TAMEABLE_ANIMAL, 16, Type.Byte, 12, NewType.Byte),
    TAMING_OWNER(ENTITY_TAMEABLE_ANIMAL, 17, Type.String, 13, NewType.OptUUID),
    // ocelot
    OCELOT_TYPE(OCELOT, 18, Type.Byte, 14, NewType.VarInt),
    // wolf
    WOLF_HEALTH(WOLF, 18, Type.Float, 14, NewType.Float),
    WOLF_BEGGING(WOLF, 19, Type.Byte, 15, NewType.Boolean),
    WOLF_COLLAR(WOLF, 20, Type.Byte, 16, NewType.VarInt),
    // pig
    PIG_SADDLE(PIG, 16, Type.Byte, 12, NewType.Boolean),
    // rabbit
    RABBIT_TYPE(RABBIT, 18, Type.Byte, 12, NewType.VarInt),
    // sheep
    SHEEP_COLOR(SHEEP, 16, Type.Byte, 12, NewType.Byte),
    // villager
    VILLAGER_PROFESSION(VILLAGER, 16, Type.Int, 12, NewType.VarInt),
    // enderman
    ENDERMAN_BLOCK(ENDERMAN, 16, Type.Short, 11, NewType.BlockID), // special case
    ENDERMAN_BLOCKDATA(ENDERMAN, 17, Type.Byte, 11, NewType.BlockID), // special case
    ENDERMAN_ISSCREAMING(ENDERMAN, 18, Type.Byte, 12, NewType.Boolean),
    // zombie
    ZOMBIE_ISCHILD(ZOMBIE, 12, Type.Byte, 11, NewType.Boolean),
    ZOMBIE_ISVILLAGER(ZOMBIE, 13, Type.Byte, 12, NewType.VarInt),
    ZOMBIE_ISCONVERTING(ZOMBIE, 14, Type.Byte, 13, NewType.Boolean),
    // ZOMBIE_RISINGHANDS added in 1.9
    // blaze
    BLAZE_ONFIRE(BLAZE, 16, Type.Byte, 11, NewType.Byte),
    // spider
    SPIDER_CIMBING(SPIDER, 16, Type.Byte, 11, NewType.Byte),
    // creeper
    CREEPER_FUSE(CREEPER, 16, Type.Byte, 11, NewType.VarInt), // -1 idle, 1 is fuse
    CREEPER_ISPOWERED(CREEPER, 17, Type.Byte, 12, NewType.Boolean),
    CREEPER_ISIGNITED(CREEPER, 18, Type.Byte, 13, NewType.Boolean),
    // ghast
    GHAST_ISATTACKING(GHAST, 16, Type.Byte, 11, NewType.Boolean),
    // slime
    SLIME_SIZE(SLIME, 16, Type.Byte, 11, NewType.VarInt),
    // skeleton
    SKELETON_TYPE(SKELETON, 13, Type.Byte, 11, NewType.VarInt),
    // witch
    WITCH_AGGRO(WITCH, 21, Type.Byte, 11, NewType.Boolean),
    // iron golem
    IRON_PLAYERMADE(IRON_GOLEM, 16, Type.Byte, 11, NewType.Byte),
    // wither
    WITHER_TARGET1(WITHER, 17, Type.Int, 11, NewType.VarInt),
    WITHER_TARGET2(WITHER, 18, Type.Int, 12, NewType.VarInt),
    WITHER_TARGET3(WITHER, 19, Type.Int, 13, NewType.VarInt),
    WITHER_INVULN_TIME(WITHER, 20, Type.Int, 14, NewType.VarInt),
    WITHER_PROPERTIES(WITHER, 10, Type.Byte, NewType.Byte),
    WITHER_UNKNOWN(WITHER, 11, Type.Byte, NewType.Discontinued),
    // wither skull
    WITHERSKULL_INVULN(WITHER_SKULL, 10, Type.Byte, 5, NewType.Boolean),
    // guardian
    GUARDIAN_INFO(GUARDIAN, 16, Type.Int, 11, NewType.Byte),
    GUARDIAN_TARGET(GUARDIAN, 17, Type.Int, 12, NewType.VarInt),
    // boat
    BOAT_SINCEHIT(BOAT, 17, Type.Int, 5, NewType.VarInt),
    BOAT_FORWARDDIR(BOAT, 18, Type.Int, 6, NewType.VarInt),
    BOAT_DMGTAKEN(BOAT, 19, Type.Float, 7, NewType.Float),
    // BOAT_TYPE in 1.9
    // minecart
    MINECART_SHAKINGPOWER(MINECART_ABSTRACT, 17, Type.Int, 5, NewType.VarInt),
    MINECART_SHAKINGDIRECTION(MINECART_ABSTRACT, 18, Type.Int, 6, NewType.VarInt),
    MINECART_DAMAGETAKEN(MINECART_ABSTRACT, 19, Type.Float, 7, NewType.Float), // also shaking modifier :P
    MINECART_BLOCK(MINECART_ABSTRACT, 20, Type.Int, 8, NewType.VarInt),
    MINECART_BLOCK_Y(MINECART_ABSTRACT, 21, Type.Int, 9, NewType.VarInt),
    MINECART_SHOWBLOCK(MINECART_ABSTRACT, 22, Type.Byte, 10, NewType.Boolean),
    // Command minecart (they are still broken)
    MINECART_COMMANDBLOCK_COMMAND(MINECART_ABSTRACT, 23, Type.String, 11, NewType.String),
    MINECART_COMMANDBLOCK_OUTPUT(MINECART_ABSTRACT, 24, Type.String, 12, NewType.Chat),
    // furnace cart
    FURNACECART_ISPOWERED(MINECART_ABSTRACT, 16, Type.Byte, 11, NewType.Boolean),
    // item drop
    ITEM_ITEM(DROPPED_ITEM, 10, Type.Slot, 5, NewType.Slot),
    // arrow
    ARROW_ISCRIT(ARROW, 16, Type.Byte, 5, NewType.Byte),
    // firework
    FIREWORK_INFO(FIREWORK, 8, Type.Slot, 5, NewType.Slot),
    // item frame
    ITEMFRAME_ITEM(ITEM_FRAME, 8, Type.Slot, 5, NewType.Slot),
    ITEMFRAME_ROTATION(ITEM_FRAME, 9, Type.Byte, 6, NewType.VarInt),
    // ender crystal
    ENDERCRYSTAL_HEALTH(ENDER_CRYSTAL, 8, Type.Int, NewType.Discontinued),
    // Ender dragon boss bar issues
    ENDERDRAGON_UNKNOWN(ENDER_DRAGON, 5, Type.Byte, NewType.Discontinued),
    ENDERDRAGON_NAME(ENDER_DRAGON, 10, Type.String, NewType.Discontinued),
    // Normal Ender dragon
    ENDERDRAGON_FLAG(ENDER_DRAGON, 15, Type.Byte, NewType.Discontinued),
    ENDERDRAGON_PHASE(ENDER_DRAGON, 11, Type.Byte, NewType.VarInt);

    private static final HashMap<Pair<EntityUtil.EntityType, Integer>, MetaIndex> metadataRewrites = new HashMap<>();

    static {
        for (MetaIndex index : MetaIndex.values())
            metadataRewrites.put(new Pair<>(index.getClazz(), index.getIndex()), index);
    }

    private EntityUtil.EntityType clazz;
    private int newIndex;
    private NewType newType;
    private Type oldType;
    private int index;

    MetaIndex(EntityUtil.EntityType type, int index, Type oldType, NewType newType) {
        this.clazz = type;
        this.index = index;
        this.newIndex = index;
        this.oldType = oldType;
        this.newType = newType;
    }

    MetaIndex(EntityUtil.EntityType type, int index, Type oldType, int newIndex, NewType newType) {
        this.clazz = type;
        this.index = index;
        this.oldType = oldType;
        this.newIndex = newIndex;
        this.newType = newType;
    }

    private static Optional<MetaIndex> getIndex(EntityUtil.EntityType type, int index) {
        Pair pair = new Pair<>(type, index);
        if (metadataRewrites.containsKey(pair)) {
            return Optional.of(metadataRewrites.get(pair));
        }

        return Optional.absent();
    }

    public static MetaIndex searchIndex(EntityUtil.EntityType type, int index) {
        EntityUtil.EntityType currentType = type;
        do {
            Optional<MetaIndex> optMeta = getIndex(currentType, index);

            if (optMeta.isPresent()){
                return optMeta.get();
            }

            currentType = currentType.getParent();
        } while (currentType != null);

        return null;
    }

}


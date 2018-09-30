package us.myles.ViaVersion.protocols.protocol1_9to1_8.metadata;

import com.google.common.base.Optional;
import lombok.Getter;
import us.myles.ViaVersion.api.Pair;
import us.myles.ViaVersion.api.entities.Entity1_10Types;
import us.myles.ViaVersion.api.minecraft.metadata.types.MetaType1_8;
import us.myles.ViaVersion.api.minecraft.metadata.types.MetaType1_9;

import java.util.HashMap;

import static us.myles.ViaVersion.api.entities.Entity1_10Types.EntityType.*;

@Getter
public enum MetaIndex {

    // entity
    ENTITY_STATUS(ENTITY, 0, MetaType1_8.Byte, MetaType1_9.Byte),
    ENTITY_AIR(ENTITY, 1, MetaType1_8.Short, MetaType1_9.VarInt),
    ENTITY_NAMETAG(ENTITY, 2, MetaType1_8.String, MetaType1_9.String), // in the entity class @ spigot 1.8.8, blame wiki.vg
    ENTITY_ALWAYS_SHOW_NAMETAG(ENTITY, 3, MetaType1_8.Byte, MetaType1_9.Boolean), // in the entity class @ Spigot 1.8.8, blame wiki.vg
    ENTITY_SILENT(ENTITY, 4, MetaType1_8.Byte, MetaType1_9.Boolean),
    // living entity
    LIVINGENTITY_HEALTH(ENTITY_LIVING, 6, MetaType1_8.Float, MetaType1_9.Float),
    LIVINGENTITY_POTION_EFFECT_COLOR(ENTITY_LIVING, 7, MetaType1_8.Int, MetaType1_9.VarInt),
    LIVINGENTITY_IS_POTION_AMBIENT(ENTITY_LIVING, 8, MetaType1_8.Byte, MetaType1_9.Boolean),
    LIVINGENTITY_NUMBER_OF_ARROWS_IN(ENTITY_LIVING, 9, MetaType1_8.Byte, MetaType1_9.VarInt),
    LIVINGENTITY_NO_AI(ENTITY_LIVING, 15, MetaType1_8.Byte, 10, MetaType1_9.Byte), // in 1.9 this is combined with Left handed, oh.
    // ageable
    AGEABLE_AGE(ENTITY_AGEABLE, 12, MetaType1_8.Byte, 11, MetaType1_9.Boolean),
    // armour stand
    STAND_INFO(ARMOR_STAND, 10, MetaType1_8.Byte, MetaType1_9.Byte),
    STAND_HEAD_POS(ARMOR_STAND, 11, MetaType1_8.Rotation, MetaType1_9.Vector3F),
    STAND_BODY_POS(ARMOR_STAND, 12, MetaType1_8.Rotation, MetaType1_9.Vector3F),
    STAND_LA_POS(ARMOR_STAND, 13, MetaType1_8.Rotation, MetaType1_9.Vector3F),
    STAND_RA_POS(ARMOR_STAND, 14, MetaType1_8.Rotation, MetaType1_9.Vector3F),
    STAND_LL_POS(ARMOR_STAND, 15, MetaType1_8.Rotation, MetaType1_9.Vector3F),
    STAND_RL_POS(ARMOR_STAND, 16, MetaType1_8.Rotation, MetaType1_9.Vector3F),
    // human, discountined?
    PLAYER_SKIN_FLAGS(ENTITY_HUMAN, 10, MetaType1_8.Byte, 12, MetaType1_9.Byte), // unsigned on 1.8
    PLAYER_HUMAN_BYTE(ENTITY_HUMAN, 16, MetaType1_8.Byte, MetaType1_9.Discontinued), // unused on 1.8
    PLAYER_ADDITIONAL_HEARTS(ENTITY_HUMAN, 17, MetaType1_8.Float, 10, MetaType1_9.Float),
    PLAYER_SCORE(ENTITY_HUMAN, 18, MetaType1_8.Int, 11, MetaType1_9.VarInt),
    PLAYER_HAND(ENTITY_HUMAN, -1, MetaType1_8.NonExistent, 5, MetaType1_9.Byte), // new in 1.9
    SOMETHING_ANTICHEAT_PLUGINS_FOR_SOME_REASON_USE(ENTITY_HUMAN, 11, MetaType1_8.Byte, MetaType1_9.Discontinued), //For what we know, This doesn't exists. If you think it exists and knows what it does. Please tell us.
    // horse
    HORSE_INFO(HORSE, 16, MetaType1_8.Int, 12, MetaType1_9.Byte),
    HORSE_TYPE(HORSE, 19, MetaType1_8.Byte, 13, MetaType1_9.VarInt),
    HORSE_SUBTYPE(HORSE, 20, MetaType1_8.Int, 14, MetaType1_9.VarInt),
    HORSE_OWNER(HORSE, 21, MetaType1_8.String, 15, MetaType1_9.OptUUID),
    HORSE_ARMOR(HORSE, 22, MetaType1_8.Int, 16, MetaType1_9.VarInt),
    // bat
    BAT_ISHANGING(BAT, 16, MetaType1_8.Byte, 11, MetaType1_9.Byte),
    // tameable
    TAMING_INFO(ENTITY_TAMEABLE_ANIMAL, 16, MetaType1_8.Byte, 12, MetaType1_9.Byte),
    TAMING_OWNER(ENTITY_TAMEABLE_ANIMAL, 17, MetaType1_8.String, 13, MetaType1_9.OptUUID),
    // ocelot
    OCELOT_TYPE(OCELOT, 18, MetaType1_8.Byte, 14, MetaType1_9.VarInt),
    // wolf
    WOLF_HEALTH(WOLF, 18, MetaType1_8.Float, 14, MetaType1_9.Float),
    WOLF_BEGGING(WOLF, 19, MetaType1_8.Byte, 15, MetaType1_9.Boolean),
    WOLF_COLLAR(WOLF, 20, MetaType1_8.Byte, 16, MetaType1_9.VarInt),
    // pig
    PIG_SADDLE(PIG, 16, MetaType1_8.Byte, 12, MetaType1_9.Boolean),
    // rabbit
    RABBIT_TYPE(RABBIT, 18, MetaType1_8.Byte, 12, MetaType1_9.VarInt),
    // sheep
    SHEEP_COLOR(SHEEP, 16, MetaType1_8.Byte, 12, MetaType1_9.Byte),
    // villager
    VILLAGER_PROFESSION(VILLAGER, 16, MetaType1_8.Int, 12, MetaType1_9.VarInt),
    // enderman
    ENDERMAN_BLOCK(ENDERMAN, 16, MetaType1_8.Short, 11, MetaType1_9.BlockID), // special case
    ENDERMAN_BLOCKDATA(ENDERMAN, 17, MetaType1_8.Byte, 11, MetaType1_9.BlockID), // special case
    ENDERMAN_ISSCREAMING(ENDERMAN, 18, MetaType1_8.Byte, 12, MetaType1_9.Boolean),
    // zombie
    ZOMBIE_ISCHILD(ZOMBIE, 12, MetaType1_8.Byte, 11, MetaType1_9.Boolean),
    ZOMBIE_ISVILLAGER(ZOMBIE, 13, MetaType1_8.Byte, 12, MetaType1_9.VarInt),
    ZOMBIE_ISCONVERTING(ZOMBIE, 14, MetaType1_8.Byte, 13, MetaType1_9.Boolean),
    // ZOMBIE_RISINGHANDS added in 1.9
    // blaze
    BLAZE_ONFIRE(BLAZE, 16, MetaType1_8.Byte, 11, MetaType1_9.Byte),
    // spider
    SPIDER_CIMBING(SPIDER, 16, MetaType1_8.Byte, 11, MetaType1_9.Byte),
    // creeper
    CREEPER_FUSE(CREEPER, 16, MetaType1_8.Byte, 11, MetaType1_9.VarInt), // -1 idle, 1 is fuse
    CREEPER_ISPOWERED(CREEPER, 17, MetaType1_8.Byte, 12, MetaType1_9.Boolean),
    CREEPER_ISIGNITED(CREEPER, 18, MetaType1_8.Byte, 13, MetaType1_9.Boolean),
    // ghast
    GHAST_ISATTACKING(GHAST, 16, MetaType1_8.Byte, 11, MetaType1_9.Boolean),
    // slime
    SLIME_SIZE(SLIME, 16, MetaType1_8.Byte, 11, MetaType1_9.VarInt),
    // skeleton
    SKELETON_TYPE(SKELETON, 13, MetaType1_8.Byte, 11, MetaType1_9.VarInt),
    // witch
    WITCH_AGGRO(WITCH, 21, MetaType1_8.Byte, 11, MetaType1_9.Boolean),
    // iron golem
    IRON_PLAYERMADE(IRON_GOLEM, 16, MetaType1_8.Byte, 11, MetaType1_9.Byte),
    // wither
    WITHER_TARGET1(WITHER, 17, MetaType1_8.Int, 11, MetaType1_9.VarInt),
    WITHER_TARGET2(WITHER, 18, MetaType1_8.Int, 12, MetaType1_9.VarInt),
    WITHER_TARGET3(WITHER, 19, MetaType1_8.Int, 13, MetaType1_9.VarInt),
    WITHER_INVULN_TIME(WITHER, 20, MetaType1_8.Int, 14, MetaType1_9.VarInt),
    WITHER_PROPERTIES(WITHER, 10, MetaType1_8.Byte, MetaType1_9.Byte),
    WITHER_UNKNOWN(WITHER, 11, MetaType1_8.Byte, MetaType1_9.Discontinued),
    // wither skull
    WITHERSKULL_INVULN(WITHER_SKULL, 10, MetaType1_8.Byte, 5, MetaType1_9.Boolean),
    // guardian
    GUARDIAN_INFO(GUARDIAN, 16, MetaType1_8.Int, 11, MetaType1_9.Byte),
    GUARDIAN_TARGET(GUARDIAN, 17, MetaType1_8.Int, 12, MetaType1_9.VarInt),
    // boat
    BOAT_SINCEHIT(BOAT, 17, MetaType1_8.Int, 5, MetaType1_9.VarInt),
    BOAT_FORWARDDIR(BOAT, 18, MetaType1_8.Int, 6, MetaType1_9.VarInt),
    BOAT_DMGTAKEN(BOAT, 19, MetaType1_8.Float, 7, MetaType1_9.Float),
    // BOAT_TYPE in 1.9
    // minecart
    MINECART_SHAKINGPOWER(MINECART_ABSTRACT, 17, MetaType1_8.Int, 5, MetaType1_9.VarInt),
    MINECART_SHAKINGDIRECTION(MINECART_ABSTRACT, 18, MetaType1_8.Int, 6, MetaType1_9.VarInt),
    MINECART_DAMAGETAKEN(MINECART_ABSTRACT, 19, MetaType1_8.Float, 7, MetaType1_9.Float), // also shaking modifier :P
    MINECART_BLOCK(MINECART_ABSTRACT, 20, MetaType1_8.Int, 8, MetaType1_9.VarInt),
    MINECART_BLOCK_Y(MINECART_ABSTRACT, 21, MetaType1_8.Int, 9, MetaType1_9.VarInt),
    MINECART_SHOWBLOCK(MINECART_ABSTRACT, 22, MetaType1_8.Byte, 10, MetaType1_9.Boolean),
    // Command minecart (they are still broken)
    MINECART_COMMANDBLOCK_COMMAND(MINECART_ABSTRACT, 23, MetaType1_8.String, 11, MetaType1_9.String),
    MINECART_COMMANDBLOCK_OUTPUT(MINECART_ABSTRACT, 24, MetaType1_8.String, 12, MetaType1_9.Chat),
    // furnace cart
    FURNACECART_ISPOWERED(MINECART_ABSTRACT, 16, MetaType1_8.Byte, 11, MetaType1_9.Boolean),
    // item drop
    ITEM_ITEM(DROPPED_ITEM, 10, MetaType1_8.Slot, 5, MetaType1_9.Slot),
    // arrow
    ARROW_ISCRIT(ARROW, 16, MetaType1_8.Byte, 5, MetaType1_9.Byte),
    // firework
    FIREWORK_INFO(FIREWORK, 8, MetaType1_8.Slot, 5, MetaType1_9.Slot),
    // item frame
    ITEMFRAME_ITEM(ITEM_FRAME, 8, MetaType1_8.Slot, 5, MetaType1_9.Slot),
    ITEMFRAME_ROTATION(ITEM_FRAME, 9, MetaType1_8.Byte, 6, MetaType1_9.VarInt),
    // ender crystal
    ENDERCRYSTAL_HEALTH(ENDER_CRYSTAL, 8, MetaType1_8.Int, MetaType1_9.Discontinued),
    // Ender dragon boss bar issues
    ENDERDRAGON_UNKNOWN(ENDER_DRAGON, 5, MetaType1_8.Byte, MetaType1_9.Discontinued),
    ENDERDRAGON_NAME(ENDER_DRAGON, 10, MetaType1_8.String, MetaType1_9.Discontinued),
    // Normal Ender dragon
    ENDERDRAGON_FLAG(ENDER_DRAGON, 15, MetaType1_8.Byte, MetaType1_9.Discontinued),
    ENDERDRAGON_PHASE(ENDER_DRAGON, 11, MetaType1_8.Byte, MetaType1_9.VarInt);

    private static final HashMap<Pair<Entity1_10Types.EntityType, Integer>, MetaIndex> metadataRewrites = new HashMap<>();

    static {
        for (MetaIndex index : MetaIndex.values())
            metadataRewrites.put(new Pair<>(index.getClazz(), index.getIndex()), index);
    }

    private Entity1_10Types.EntityType clazz;
    private int newIndex;
    private MetaType1_9 newType;
    private MetaType1_8 oldType;
    private int index;

    MetaIndex(Entity1_10Types.EntityType type, int index, MetaType1_8 oldType, MetaType1_9 newType) {
        this.clazz = type;
        this.index = index;
        this.newIndex = index;
        this.oldType = oldType;
        this.newType = newType;
    }

    MetaIndex(Entity1_10Types.EntityType type, int index, MetaType1_8 oldType, int newIndex, MetaType1_9 newType) {
        this.clazz = type;
        this.index = index;
        this.oldType = oldType;
        this.newIndex = newIndex;
        this.newType = newType;
    }

    private static Optional<MetaIndex> getIndex(Entity1_10Types.EntityType type, int index) {
        Pair pair = new Pair<>(type, index);
        return Optional.fromNullable(metadataRewrites.get(pair));
    }

    public static MetaIndex searchIndex(Entity1_10Types.EntityType type, int index) {
        Entity1_10Types.EntityType currentType = type;
        do {
            Optional<MetaIndex> optMeta = getIndex(currentType, index);

            if (optMeta.isPresent()) {
                return optMeta.get();
            }

            currentType = currentType.getParent();
        } while (currentType != null);

        return null;
    }

}


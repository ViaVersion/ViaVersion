/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2024 ViaVersion and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.viaversion.viaversion.protocols.protocol1_9to1_8.metadata;

import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_10;
import com.viaversion.viaversion.api.minecraft.metadata.types.MetaType1_8;
import com.viaversion.viaversion.api.minecraft.metadata.types.MetaType1_9;
import com.viaversion.viaversion.util.Pair;
import java.util.HashMap;
import java.util.Optional;
import org.checkerframework.checker.nullness.qual.Nullable;

import static com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_10.EntityType.*;

public enum MetaIndex {

    // Entity
    ENTITY_STATUS(ENTITY, 0, MetaType1_8.Byte, MetaType1_9.Byte),
    ENTITY_AIR(ENTITY, 1, MetaType1_8.Short, MetaType1_9.VarInt),
    ENTITY_NAMETAG(ENTITY, 2, MetaType1_8.String, MetaType1_9.String),
    ENTITY_ALWAYS_SHOW_NAMETAG(ENTITY, 3, MetaType1_8.Byte, MetaType1_9.Boolean),
    ENTITY_SILENT(ENTITY, 4, MetaType1_8.Byte, MetaType1_9.Boolean),

    // Living entity
    // hand state added in 1.9 (5/byte)
    LIVING_ENTITY_HEALTH(ENTITY_LIVING, 6, MetaType1_8.Float, MetaType1_9.Float),
    LIVING_ENTITY_POTION_EFFECT_COLOR(ENTITY_LIVING, 7, MetaType1_8.Int, MetaType1_9.VarInt),
    LIVING_ENTITY_IS_POTION_AMBIENT(ENTITY_LIVING, 8, MetaType1_8.Byte, MetaType1_9.Boolean),
    LIVING_ENTITY_NUMBER_OF_ARROWS_IN(ENTITY_LIVING, 9, MetaType1_8.Byte, MetaType1_9.VarInt),

    LIVING_ENTITY_NO_AI(ENTITY_LIVING, 15, MetaType1_8.Byte, 10, MetaType1_9.Byte),

    // Ageable entity
    AGEABLE_CREATURE_AGE(ENTITY_AGEABLE, 12, MetaType1_8.Byte, 11, MetaType1_9.Boolean),

    // Armor stand
    ARMOR_STAND_INFO(ARMOR_STAND, 10, MetaType1_8.Byte, MetaType1_9.Byte),
    ARMOR_STAND_HEAD_POS(ARMOR_STAND, 11, MetaType1_8.Rotation, MetaType1_9.Vector3F),
    ARMOR_STAND_BODY_POS(ARMOR_STAND, 12, MetaType1_8.Rotation, MetaType1_9.Vector3F),
    ARMOR_STAND_LA_POS(ARMOR_STAND, 13, MetaType1_8.Rotation, MetaType1_9.Vector3F),
    ARMOR_STAND_RA_POS(ARMOR_STAND, 14, MetaType1_8.Rotation, MetaType1_9.Vector3F),
    ARMOR_STAND_LL_POS(ARMOR_STAND, 15, MetaType1_8.Rotation, MetaType1_9.Vector3F),
    ARMOR_STAND_RL_POS(ARMOR_STAND, 16, MetaType1_8.Rotation, MetaType1_9.Vector3F),

    // Human (player)
    PLAYER_SKIN_FLAGS(ENTITY_HUMAN, 10, MetaType1_8.Byte, 12, MetaType1_9.Byte), // unsigned on 1.8
    PLAYER_BYTE(ENTITY_HUMAN, 16, MetaType1_8.Byte, null), // unused on 1.8
    PLAYER_ADDITIONAL_HEARTS(ENTITY_HUMAN, 17, MetaType1_8.Float, 10, MetaType1_9.Float),
    PLAYER_SCORE(ENTITY_HUMAN, 18, MetaType1_8.Int, 11, MetaType1_9.VarInt),
    PLAYER_HAND(ENTITY_HUMAN, 5, MetaType1_9.Byte), // new in 1.9

    // Horse
    HORSE_INFO(HORSE, 16, MetaType1_8.Int, 12, MetaType1_9.Byte),
    HORSE_TYPE(HORSE, 19, MetaType1_8.Byte, 13, MetaType1_9.VarInt),
    HORSE_SUBTYPE(HORSE, 20, MetaType1_8.Int, 14, MetaType1_9.VarInt),
    HORSE_OWNER(HORSE, 21, MetaType1_8.String, 15, MetaType1_9.OptUUID),
    HORSE_ARMOR(HORSE, 22, MetaType1_8.Int, 16, MetaType1_9.VarInt),

    // Bat
    BAT_IS_HANGING(BAT, 16, MetaType1_8.Byte, 11, MetaType1_9.Byte),

    // Tamable entity
    TAMABLE_ANIMAL_ANIMAL_INFO(ENTITY_TAMEABLE_ANIMAL, 16, MetaType1_8.Byte, 12, MetaType1_9.Byte),
    TAMABLE_ANIMAL_ANIMAL_OWNER(ENTITY_TAMEABLE_ANIMAL, 17, MetaType1_8.String, 13, MetaType1_9.OptUUID),

    // Ocelot
    OCELOT_TYPE(OCELOT, 18, MetaType1_8.Byte, 14, MetaType1_9.VarInt),

    // Wolf
    WOLF_HEALTH(WOLF, 18, MetaType1_8.Float, 14, MetaType1_9.Float),
    WOLF_BEGGING(WOLF, 19, MetaType1_8.Byte, 15, MetaType1_9.Boolean),
    WOLF_COLLAR(WOLF, 20, MetaType1_8.Byte, 16, MetaType1_9.VarInt),

    // Pig
    PIG_SADDLE(PIG, 16, MetaType1_8.Byte, 12, MetaType1_9.Boolean),

    // Rabbit
    RABBIT_TYPE(RABBIT, 18, MetaType1_8.Byte, 12, MetaType1_9.VarInt),

    // Sheep
    SHEEP_COLOR(SHEEP, 16, MetaType1_8.Byte, 12, MetaType1_9.Byte),

    // Villager
    VILLAGER_PROFESSION(VILLAGER, 16, MetaType1_8.Int, 12, MetaType1_9.VarInt),

    // Enderman
    ENDERMAN_BLOCK_STATE(ENDERMAN, 16, MetaType1_8.Short, 11, MetaType1_9.BlockID),
    ENDERMAN_BLOCK_DATA(ENDERMAN, 17, MetaType1_8.Byte, null), // always 0 when sent, never read by the client
    ENDERMAN_IS_SCREAMING(ENDERMAN, 18, MetaType1_8.Byte, 12, MetaType1_9.Boolean),

    // Zombie
    ZOMBIE_IS_CHILD(ZOMBIE, 12, MetaType1_8.Byte, 11, MetaType1_9.Boolean),
    ZOMBIE_IS_VILLAGER(ZOMBIE, 13, MetaType1_8.Byte, 12, MetaType1_9.VarInt), // now indicates villager type
    ZOMBIE_IS_CONVERTING(ZOMBIE, 14, MetaType1_8.Byte, 13, MetaType1_9.Boolean),
    // arms raised added in 1.9 (14/boolean)

    // Blaze
    BLAZE_ON_FIRE(BLAZE, 16, MetaType1_8.Byte, 11, MetaType1_9.Byte),

    // Spider
    SPIDER_CLIMBING(SPIDER, 16, MetaType1_8.Byte, 11, MetaType1_9.Byte),

    // Creeper
    CREEPER_FUSE(CREEPER, 16, MetaType1_8.Byte, 11, MetaType1_9.VarInt),
    CREEPER_IS_POWERED(CREEPER, 17, MetaType1_8.Byte, 12, MetaType1_9.Boolean),
    CREEPER_IS_IGNITED(CREEPER, 18, MetaType1_8.Byte, 13, MetaType1_9.Boolean),

    // Ghast
    GHAST_IS_ATTACKING(GHAST, 16, MetaType1_8.Byte, 11, MetaType1_9.Boolean),

    // Slime
    SLIME_SIZE(SLIME, 16, MetaType1_8.Byte, 11, MetaType1_9.VarInt),

    // Skeleton
    SKELETON_TYPE(SKELETON, 13, MetaType1_8.Byte, 11, MetaType1_9.VarInt),

    // Witch
    WITCH_AGGRESSIVE(WITCH, 21, MetaType1_8.Byte, 11, MetaType1_9.Boolean),

    // Iron golem
    IRON_GOLEM_PLAYER_MADE(IRON_GOLEM, 16, MetaType1_8.Byte, 11, MetaType1_9.Byte),

    // Wither
    WITHER_TARGET1(WITHER, 17, MetaType1_8.Int, 11, MetaType1_9.VarInt),
    WITHER_TARGET2(WITHER, 18, MetaType1_8.Int, 12, MetaType1_9.VarInt),
    WITHER_TARGET3(WITHER, 19, MetaType1_8.Int, 13, MetaType1_9.VarInt),
    WITHER_INVULNERABILITY_TIME(WITHER, 20, MetaType1_8.Int, 14, MetaType1_9.VarInt),

    // Wither skull
    WITHER_SKULL_INVULNERABILITY(WITHER_SKULL, 10, MetaType1_8.Byte, 5, MetaType1_9.Boolean),

    // Guardian
    GUARDIAN_INFO(GUARDIAN, 16, MetaType1_8.Int, 11, MetaType1_9.Byte),
    GUARDIAN_TARGET(GUARDIAN, 17, MetaType1_8.Int, 12, MetaType1_9.VarInt),

    // Boat
    BOAT_SINCE_HIT(BOAT, 17, MetaType1_8.Int, 5, MetaType1_9.VarInt),
    BOAT_FORWARD_DIRECTION(BOAT, 18, MetaType1_8.Int, 6, MetaType1_9.VarInt),
    BOAT_DAMAGE_TAKEN(BOAT, 19, MetaType1_8.Float, 7, MetaType1_9.Float),
    // boat type added in 1.9 (20/varint)

    // Minecart
    ABSTRACT_MINECART_SHAKING_POWER(MINECART_ABSTRACT, 17, MetaType1_8.Int, 5, MetaType1_9.VarInt),
    ABSTRACT_MINECART_SHAKING_DIRECTION(MINECART_ABSTRACT, 18, MetaType1_8.Int, 6, MetaType1_9.VarInt),
    ABSTRACT_MINECART_DAMAGE_TAKEN(MINECART_ABSTRACT, 19, MetaType1_8.Float, 7, MetaType1_9.Float),
    ABSTRACT_MINECART_BLOCK(MINECART_ABSTRACT, 20, MetaType1_8.Int, 8, MetaType1_9.VarInt),
    ABSTRACT_MINECART_BLOCK_Y(MINECART_ABSTRACT, 21, MetaType1_8.Int, 9, MetaType1_9.VarInt),
    ABSTRACT_MINECART_SHOW_BLOCK(MINECART_ABSTRACT, 22, MetaType1_8.Byte, 10, MetaType1_9.Boolean),

    // Command minecart
    MINECART_COMMAND_BLOCK_COMMAND(MINECART_ABSTRACT, 23, MetaType1_8.String, 11, MetaType1_9.String),
    MINECART_COMMAND_BLOCK_OUTPUT(MINECART_ABSTRACT, 24, MetaType1_8.String, 12, MetaType1_9.Chat),

    // Furnace minecart
    MINECART_FURNACE_IS_POWERED(MINECART_ABSTRACT, 16, MetaType1_8.Byte, 11, MetaType1_9.Boolean),

    // Item drop
    DROPPED_ITEM_ITEM(DROPPED_ITEM, 10, MetaType1_8.Slot, 5, MetaType1_9.Slot),

    // Arrow
    ARROW_IS_CRIT(ARROW, 16, MetaType1_8.Byte, 5, MetaType1_9.Byte),

    // Firework
    FIREWORK_INFO(FIREWORK, 8, MetaType1_8.Slot, 5, MetaType1_9.Slot),

    // Item frame
    ITEM_FRAME_ITEM(ITEM_FRAME, 8, MetaType1_8.Slot, 5, MetaType1_9.Slot),
    ITEM_FRAME_ROTATION(ITEM_FRAME, 9, MetaType1_8.Byte, 6, MetaType1_9.VarInt),

    // Ender crystal
    ENDER_CRYSTAL_HEALTH(ENDER_CRYSTAL, 8, MetaType1_8.Int, null),

    // Ender dragon
    ENDER_DRAGON_PHASE(ENDER_DRAGON, 11, MetaType1_9.VarInt);

    private static final HashMap<Pair<EntityTypes1_10.EntityType, Integer>, MetaIndex> metadataRewrites = new HashMap<>();

    static {
        for (MetaIndex index : MetaIndex.values()) {
            metadataRewrites.put(new Pair<>(index.clazz, index.index), index);
        }
    }

    private final EntityTypes1_10.EntityType clazz;
    private final int newIndex;
    private final MetaType1_9 newType;
    private final MetaType1_8 oldType;
    private final int index;

    MetaIndex(EntityTypes1_10.EntityType type, int index, MetaType1_8 oldType, @Nullable MetaType1_9 newType) {
        this.clazz = type;
        this.index = index;
        this.newIndex = index;
        this.oldType = oldType;
        this.newType = newType;
    }

    MetaIndex(EntityTypes1_10.EntityType type, int newIndex, @Nullable MetaType1_9 newType) {
        this.clazz = type;
        this.index = -1;
        this.oldType = null;
        this.newIndex = newIndex;
        this.newType = newType;
    }

    MetaIndex(EntityTypes1_10.EntityType type, int index, MetaType1_8 oldType, int newIndex, @Nullable MetaType1_9 newType) {
        this.clazz = type;
        this.index = index;
        this.oldType = oldType;
        this.newIndex = newIndex;
        this.newType = newType;
    }

    public EntityTypes1_10.EntityType getClazz() {
        return clazz;
    }

    public int getNewIndex() {
        return newIndex;
    }

    public @Nullable MetaType1_9 getNewType() {
        return newType;
    }

    public MetaType1_8 getOldType() {
        return oldType;
    }

    public int getIndex() {
        return index;
    }

    private static Optional<MetaIndex> getIndex(EntityType type, int index) {
        Pair pair = new Pair<>(type, index);
        return Optional.ofNullable(metadataRewrites.get(pair));
    }

    public static MetaIndex searchIndex(EntityType type, int index) {
        EntityType currentType = type;
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
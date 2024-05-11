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
package com.viaversion.viaversion.protocols.v1_8to1_9.data;

import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_10;
import com.viaversion.viaversion.api.minecraft.metadata.types.MetaType1_8;
import com.viaversion.viaversion.api.minecraft.metadata.types.MetaType1_9;
import com.viaversion.viaversion.util.Pair;
import java.util.HashMap;
import java.util.Optional;
import org.checkerframework.checker.nullness.qual.Nullable;

import static com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_10.EntityType.*;

public enum MetaIndex1_8 {

    // Entity
    ENTITY_STATUS(ENTITY, 0, MetaType1_8.BYTE, MetaType1_9.BYTE),
    ENTITY_AIR(ENTITY, 1, MetaType1_8.SHORT, MetaType1_9.VAR_INT),
    ENTITY_NAMETAG(ENTITY, 2, MetaType1_8.STRING, MetaType1_9.STRING),
    ENTITY_ALWAYS_SHOW_NAMETAG(ENTITY, 3, MetaType1_8.BYTE, MetaType1_9.BOOLEAN),
    ENTITY_SILENT(ENTITY, 4, MetaType1_8.BYTE, MetaType1_9.BOOLEAN),

    // Living entity
    // hand state added in 1.9 (5/byte)
    LIVING_ENTITY_HEALTH(ENTITY_LIVING, 6, MetaType1_8.FLOAT, MetaType1_9.FLOAT),
    LIVING_ENTITY_POTION_EFFECT_COLOR(ENTITY_LIVING, 7, MetaType1_8.INT, MetaType1_9.VAR_INT),
    LIVING_ENTITY_IS_POTION_AMBIENT(ENTITY_LIVING, 8, MetaType1_8.BYTE, MetaType1_9.BOOLEAN),
    LIVING_ENTITY_NUMBER_OF_ARROWS_IN(ENTITY_LIVING, 9, MetaType1_8.BYTE, MetaType1_9.VAR_INT),

    LIVING_ENTITY_NO_AI(ENTITY_LIVING, 15, MetaType1_8.BYTE, 10, MetaType1_9.BYTE),

    // Ageable entity
    AGEABLE_CREATURE_AGE(ENTITY_AGEABLE, 12, MetaType1_8.BYTE, 11, MetaType1_9.BOOLEAN),

    // Armor stand
    ARMOR_STAND_INFO(ARMOR_STAND, 10, MetaType1_8.BYTE, MetaType1_9.BYTE),
    ARMOR_STAND_HEAD_POS(ARMOR_STAND, 11, MetaType1_8.ROTATIONS, MetaType1_9.ROTATIONS),
    ARMOR_STAND_BODY_POS(ARMOR_STAND, 12, MetaType1_8.ROTATIONS, MetaType1_9.ROTATIONS),
    ARMOR_STAND_LA_POS(ARMOR_STAND, 13, MetaType1_8.ROTATIONS, MetaType1_9.ROTATIONS),
    ARMOR_STAND_RA_POS(ARMOR_STAND, 14, MetaType1_8.ROTATIONS, MetaType1_9.ROTATIONS),
    ARMOR_STAND_LL_POS(ARMOR_STAND, 15, MetaType1_8.ROTATIONS, MetaType1_9.ROTATIONS),
    ARMOR_STAND_RL_POS(ARMOR_STAND, 16, MetaType1_8.ROTATIONS, MetaType1_9.ROTATIONS),

    // Human (player)
    PLAYER_SKIN_FLAGS(ENTITY_HUMAN, 10, MetaType1_8.BYTE, 12, MetaType1_9.BYTE), // unsigned on 1.8
    PLAYER_BYTE(ENTITY_HUMAN, 16, MetaType1_8.BYTE, null), // unused on 1.8
    PLAYER_ADDITIONAL_HEARTS(ENTITY_HUMAN, 17, MetaType1_8.FLOAT, 10, MetaType1_9.FLOAT),
    PLAYER_SCORE(ENTITY_HUMAN, 18, MetaType1_8.INT, 11, MetaType1_9.VAR_INT),
    PLAYER_HAND(ENTITY_HUMAN, 5, MetaType1_9.BYTE), // new in 1.9

    // Horse
    HORSE_INFO(HORSE, 16, MetaType1_8.INT, 12, MetaType1_9.BYTE),
    HORSE_TYPE(HORSE, 19, MetaType1_8.BYTE, 13, MetaType1_9.VAR_INT),
    HORSE_SUBTYPE(HORSE, 20, MetaType1_8.INT, 14, MetaType1_9.VAR_INT),
    HORSE_OWNER(HORSE, 21, MetaType1_8.STRING, 15, MetaType1_9.OPTIONAL_UUID),
    HORSE_ARMOR(HORSE, 22, MetaType1_8.INT, 16, MetaType1_9.VAR_INT),

    // Bat
    BAT_IS_HANGING(BAT, 16, MetaType1_8.BYTE, 11, MetaType1_9.BYTE),

    // Tamable entity
    TAMABLE_ANIMAL_ANIMAL_INFO(ENTITY_TAMEABLE_ANIMAL, 16, MetaType1_8.BYTE, 12, MetaType1_9.BYTE),
    TAMABLE_ANIMAL_ANIMAL_OWNER(ENTITY_TAMEABLE_ANIMAL, 17, MetaType1_8.STRING, 13, MetaType1_9.OPTIONAL_UUID),

    // Ocelot
    OCELOT_TYPE(OCELOT, 18, MetaType1_8.BYTE, 14, MetaType1_9.VAR_INT),

    // Wolf
    WOLF_HEALTH(WOLF, 18, MetaType1_8.FLOAT, 14, MetaType1_9.FLOAT),
    WOLF_BEGGING(WOLF, 19, MetaType1_8.BYTE, 15, MetaType1_9.BOOLEAN),
    WOLF_COLLAR(WOLF, 20, MetaType1_8.BYTE, 16, MetaType1_9.VAR_INT),

    // Pig
    PIG_SADDLE(PIG, 16, MetaType1_8.BYTE, 12, MetaType1_9.BOOLEAN),

    // Rabbit
    RABBIT_TYPE(RABBIT, 18, MetaType1_8.BYTE, 12, MetaType1_9.VAR_INT),

    // Sheep
    SHEEP_COLOR(SHEEP, 16, MetaType1_8.BYTE, 12, MetaType1_9.BYTE),

    // Villager
    VILLAGER_PROFESSION(VILLAGER, 16, MetaType1_8.INT, 12, MetaType1_9.VAR_INT),

    // Enderman
    ENDERMAN_BLOCK_STATE(ENDERMAN, 16, MetaType1_8.SHORT, 11, MetaType1_9.OPTIONAL_BLOCK_STATE),
    ENDERMAN_BLOCK_DATA(ENDERMAN, 17, MetaType1_8.BYTE, null), // always 0 when sent, never read by the client
    ENDERMAN_IS_SCREAMING(ENDERMAN, 18, MetaType1_8.BYTE, 12, MetaType1_9.BOOLEAN),

    // Zombie
    ZOMBIE_IS_CHILD(ZOMBIE, 12, MetaType1_8.BYTE, 11, MetaType1_9.BOOLEAN),
    ZOMBIE_IS_VILLAGER(ZOMBIE, 13, MetaType1_8.BYTE, 12, MetaType1_9.VAR_INT), // now indicates villager type
    ZOMBIE_IS_CONVERTING(ZOMBIE, 14, MetaType1_8.BYTE, 13, MetaType1_9.BOOLEAN),
    // arms raised added in 1.9 (14/boolean)

    // Blaze
    BLAZE_ON_FIRE(BLAZE, 16, MetaType1_8.BYTE, 11, MetaType1_9.BYTE),

    // Spider
    SPIDER_CLIMBING(SPIDER, 16, MetaType1_8.BYTE, 11, MetaType1_9.BYTE),

    // Creeper
    CREEPER_FUSE(CREEPER, 16, MetaType1_8.BYTE, 11, MetaType1_9.VAR_INT),
    CREEPER_IS_POWERED(CREEPER, 17, MetaType1_8.BYTE, 12, MetaType1_9.BOOLEAN),
    CREEPER_IS_IGNITED(CREEPER, 18, MetaType1_8.BYTE, 13, MetaType1_9.BOOLEAN),

    // Ghast
    GHAST_IS_ATTACKING(GHAST, 16, MetaType1_8.BYTE, 11, MetaType1_9.BOOLEAN),

    // Slime
    SLIME_SIZE(SLIME, 16, MetaType1_8.BYTE, 11, MetaType1_9.VAR_INT),

    // Skeleton
    SKELETON_TYPE(SKELETON, 13, MetaType1_8.BYTE, 11, MetaType1_9.VAR_INT),

    // Witch
    WITCH_AGGRESSIVE(WITCH, 21, MetaType1_8.BYTE, 11, MetaType1_9.BOOLEAN),

    // Iron golem
    IRON_GOLEM_PLAYER_MADE(IRON_GOLEM, 16, MetaType1_8.BYTE, 11, MetaType1_9.BYTE),

    // Wither
    WITHER_TARGET1(WITHER, 17, MetaType1_8.INT, 11, MetaType1_9.VAR_INT),
    WITHER_TARGET2(WITHER, 18, MetaType1_8.INT, 12, MetaType1_9.VAR_INT),
    WITHER_TARGET3(WITHER, 19, MetaType1_8.INT, 13, MetaType1_9.VAR_INT),
    WITHER_INVULNERABILITY_TIME(WITHER, 20, MetaType1_8.INT, 14, MetaType1_9.VAR_INT),

    // Wither skull
    WITHER_SKULL_INVULNERABILITY(WITHER_SKULL, 10, MetaType1_8.BYTE, 5, MetaType1_9.BOOLEAN),

    // Guardian
    GUARDIAN_INFO(GUARDIAN, 16, MetaType1_8.INT, 11, MetaType1_9.BYTE),
    GUARDIAN_TARGET(GUARDIAN, 17, MetaType1_8.INT, 12, MetaType1_9.VAR_INT),

    // Boat
    BOAT_SINCE_HIT(BOAT, 17, MetaType1_8.INT, 5, MetaType1_9.VAR_INT),
    BOAT_FORWARD_DIRECTION(BOAT, 18, MetaType1_8.INT, 6, MetaType1_9.VAR_INT),
    BOAT_DAMAGE_TAKEN(BOAT, 19, MetaType1_8.FLOAT, 7, MetaType1_9.FLOAT),
    // boat type added in 1.9 (20/varint)

    // Minecart
    ABSTRACT_MINECART_SHAKING_POWER(MINECART_ABSTRACT, 17, MetaType1_8.INT, 5, MetaType1_9.VAR_INT),
    ABSTRACT_MINECART_SHAKING_DIRECTION(MINECART_ABSTRACT, 18, MetaType1_8.INT, 6, MetaType1_9.VAR_INT),
    ABSTRACT_MINECART_DAMAGE_TAKEN(MINECART_ABSTRACT, 19, MetaType1_8.FLOAT, 7, MetaType1_9.FLOAT),
    ABSTRACT_MINECART_BLOCK(MINECART_ABSTRACT, 20, MetaType1_8.INT, 8, MetaType1_9.VAR_INT),
    ABSTRACT_MINECART_BLOCK_Y(MINECART_ABSTRACT, 21, MetaType1_8.INT, 9, MetaType1_9.VAR_INT),
    ABSTRACT_MINECART_SHOW_BLOCK(MINECART_ABSTRACT, 22, MetaType1_8.BYTE, 10, MetaType1_9.BOOLEAN),

    // Command minecart
    MINECART_COMMAND_BLOCK_COMMAND(MINECART_ABSTRACT, 23, MetaType1_8.STRING, 11, MetaType1_9.STRING),
    MINECART_COMMAND_BLOCK_OUTPUT(MINECART_ABSTRACT, 24, MetaType1_8.STRING, 12, MetaType1_9.COMPONENT),

    // Furnace minecart
    MINECART_FURNACE_IS_POWERED(MINECART_ABSTRACT, 16, MetaType1_8.BYTE, 11, MetaType1_9.BOOLEAN),

    // Item drop
    DROPPED_ITEM_ITEM(DROPPED_ITEM, 10, MetaType1_8.ITEM, 5, MetaType1_9.ITEM),

    // Arrow
    ARROW_IS_CRIT(ARROW, 16, MetaType1_8.BYTE, 5, MetaType1_9.BYTE),

    // Firework
    FIREWORK_INFO(FIREWORK, 8, MetaType1_8.ITEM, 5, MetaType1_9.ITEM),

    // Item frame
    ITEM_FRAME_ITEM(ITEM_FRAME, 8, MetaType1_8.ITEM, 5, MetaType1_9.ITEM),
    ITEM_FRAME_ROTATION(ITEM_FRAME, 9, MetaType1_8.BYTE, 6, MetaType1_9.VAR_INT),

    // Ender crystal
    ENDER_CRYSTAL_HEALTH(ENDER_CRYSTAL, 8, MetaType1_8.INT, null),

    // Ender dragon
    ENDER_DRAGON_PHASE(ENDER_DRAGON, 11, MetaType1_9.VAR_INT);

    private static final HashMap<Pair<EntityTypes1_10.EntityType, Integer>, MetaIndex1_8> metadataRewrites = new HashMap<>();

    static {
        for (MetaIndex1_8 index : MetaIndex1_8.values()) {
            metadataRewrites.put(new Pair<>(index.clazz, index.index), index);
        }
    }

    private final EntityTypes1_10.EntityType clazz;
    private final int newIndex;
    private final MetaType1_9 newType;
    private final MetaType1_8 oldType;
    private final int index;

    MetaIndex1_8(EntityTypes1_10.EntityType type, int index, MetaType1_8 oldType, @Nullable MetaType1_9 newType) {
        this.clazz = type;
        this.index = index;
        this.newIndex = index;
        this.oldType = oldType;
        this.newType = newType;
    }

    MetaIndex1_8(EntityTypes1_10.EntityType type, int newIndex, @Nullable MetaType1_9 newType) {
        this.clazz = type;
        this.index = -1;
        this.oldType = null;
        this.newIndex = newIndex;
        this.newType = newType;
    }

    MetaIndex1_8(EntityTypes1_10.EntityType type, int index, MetaType1_8 oldType, int newIndex, @Nullable MetaType1_9 newType) {
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

    private static Optional<MetaIndex1_8> getIndex(EntityType type, int index) {
        Pair pair = new Pair<>(type, index);
        return Optional.ofNullable(metadataRewrites.get(pair));
    }

    public static MetaIndex1_8 searchIndex(EntityType type, int index) {
        EntityType currentType = type;
        do {
            Optional<MetaIndex1_8> optMeta = getIndex(currentType, index);

            if (optMeta.isPresent()) {
                return optMeta.get();
            }

            currentType = currentType.getParent();
        } while (currentType != null);

        return null;
    }

}
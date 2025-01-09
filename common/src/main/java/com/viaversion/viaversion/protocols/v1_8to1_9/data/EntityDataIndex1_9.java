/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2025 ViaVersion and contributors
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
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_9;
import com.viaversion.viaversion.api.minecraft.entitydata.types.EntityDataTypes1_8;
import com.viaversion.viaversion.api.minecraft.entitydata.types.EntityDataTypes1_9;
import com.viaversion.viaversion.util.Pair;
import java.util.HashMap;
import java.util.Optional;
import org.checkerframework.checker.nullness.qual.Nullable;

import static com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_9.EntityType.*;

public enum EntityDataIndex1_9 {

    // Entity
    ENTITY_STATUS(ENTITY, 0, EntityDataTypes1_8.BYTE, EntityDataTypes1_9.BYTE),
    ENTITY_AIR(ENTITY, 1, EntityDataTypes1_8.SHORT, EntityDataTypes1_9.VAR_INT),
    ENTITY_NAMETAG(ENTITY, 2, EntityDataTypes1_8.STRING, EntityDataTypes1_9.STRING),
    ENTITY_ALWAYS_SHOW_NAMETAG(ENTITY, 3, EntityDataTypes1_8.BYTE, EntityDataTypes1_9.BOOLEAN),
    ENTITY_SILENT(ENTITY, 4, EntityDataTypes1_8.BYTE, EntityDataTypes1_9.BOOLEAN),

    // Living entity (base)
    // hand state added in 1.9 (5/byte)
    LIVING_ENTITY_BASE_HEALTH(LIVING_ENTITY_BASE, 6, EntityDataTypes1_8.FLOAT, EntityDataTypes1_9.FLOAT),
    LIVING_ENTITY_BASE_POTION_EFFECT_COLOR(LIVING_ENTITY_BASE, 7, EntityDataTypes1_8.INT, EntityDataTypes1_9.VAR_INT),
    LIVING_ENTITY_BASE_IS_POTION_AMBIENT(LIVING_ENTITY_BASE, 8, EntityDataTypes1_8.BYTE, EntityDataTypes1_9.BOOLEAN),
    LIVING_ENTITY_BASE_NUMBER_OF_ARROWS_IN(LIVING_ENTITY_BASE, 9, EntityDataTypes1_8.BYTE, EntityDataTypes1_9.VAR_INT),

    LIVING_ENTITY_NO_AI(LIVING_ENTITY, 15, EntityDataTypes1_8.BYTE, 10, EntityDataTypes1_9.BYTE),

    // Ageable entity
    ABSTRACT_AGEABLE_AGE(ABSTRACT_AGEABLE, 12, EntityDataTypes1_8.BYTE, 11, EntityDataTypes1_9.BOOLEAN),

    // Armor stand
    ARMOR_STAND_INFO(ARMOR_STAND, 10, EntityDataTypes1_8.BYTE, EntityDataTypes1_9.BYTE),
    ARMOR_STAND_HEAD_POS(ARMOR_STAND, 11, EntityDataTypes1_8.ROTATIONS, EntityDataTypes1_9.ROTATIONS),
    ARMOR_STAND_BODY_POS(ARMOR_STAND, 12, EntityDataTypes1_8.ROTATIONS, EntityDataTypes1_9.ROTATIONS),
    ARMOR_STAND_LA_POS(ARMOR_STAND, 13, EntityDataTypes1_8.ROTATIONS, EntityDataTypes1_9.ROTATIONS),
    ARMOR_STAND_RA_POS(ARMOR_STAND, 14, EntityDataTypes1_8.ROTATIONS, EntityDataTypes1_9.ROTATIONS),
    ARMOR_STAND_LL_POS(ARMOR_STAND, 15, EntityDataTypes1_8.ROTATIONS, EntityDataTypes1_9.ROTATIONS),
    ARMOR_STAND_RL_POS(ARMOR_STAND, 16, EntityDataTypes1_8.ROTATIONS, EntityDataTypes1_9.ROTATIONS),

    // Human (player)
    PLAYER_SKIN_FLAGS(PLAYER, 10, EntityDataTypes1_8.BYTE, 12, EntityDataTypes1_9.BYTE), // unsigned on 1.8
    PLAYER_BYTE(PLAYER, 16, EntityDataTypes1_8.BYTE, null), // unused on 1.8
    PLAYER_ADDITIONAL_HEARTS(PLAYER, 17, EntityDataTypes1_8.FLOAT, 10, EntityDataTypes1_9.FLOAT),
    PLAYER_SCORE(PLAYER, 18, EntityDataTypes1_8.INT, 11, EntityDataTypes1_9.VAR_INT),
    PLAYER_HAND(PLAYER, 5, EntityDataTypes1_9.BYTE), // new in 1.9

    // Horse
    HORSE_INFO(HORSE, 16, EntityDataTypes1_8.INT, 12, EntityDataTypes1_9.BYTE),
    HORSE_TYPE(HORSE, 19, EntityDataTypes1_8.BYTE, 13, EntityDataTypes1_9.VAR_INT),
    HORSE_SUBTYPE(HORSE, 20, EntityDataTypes1_8.INT, 14, EntityDataTypes1_9.VAR_INT),
    HORSE_OWNER(HORSE, 21, EntityDataTypes1_8.STRING, 15, EntityDataTypes1_9.OPTIONAL_UUID),
    HORSE_ARMOR(HORSE, 22, EntityDataTypes1_8.INT, 16, EntityDataTypes1_9.VAR_INT),

    // Bat
    BAT_IS_HANGING(BAT, 16, EntityDataTypes1_8.BYTE, 11, EntityDataTypes1_9.BYTE),

    // Tamable entity
    TAMABLE_ANIMAL_ANIMAL_INFO(TAMABLE_ANIMAL, 16, EntityDataTypes1_8.BYTE, 12, EntityDataTypes1_9.BYTE),
    TAMABLE_ANIMAL_ANIMAL_OWNER(TAMABLE_ANIMAL, 17, EntityDataTypes1_8.STRING, 13, EntityDataTypes1_9.OPTIONAL_UUID),

    // Ocelot
    OCELOT_TYPE(OCELOT, 18, EntityDataTypes1_8.BYTE, 14, EntityDataTypes1_9.VAR_INT),

    // Wolf
    WOLF_HEALTH(WOLF, 18, EntityDataTypes1_8.FLOAT, 14, EntityDataTypes1_9.FLOAT),
    WOLF_BEGGING(WOLF, 19, EntityDataTypes1_8.BYTE, 15, EntityDataTypes1_9.BOOLEAN),
    WOLF_COLLAR(WOLF, 20, EntityDataTypes1_8.BYTE, 16, EntityDataTypes1_9.VAR_INT),

    // Pig
    PIG_SADDLE(PIG, 16, EntityDataTypes1_8.BYTE, 12, EntityDataTypes1_9.BOOLEAN),

    // Rabbit
    RABBIT_TYPE(RABBIT, 18, EntityDataTypes1_8.BYTE, 12, EntityDataTypes1_9.VAR_INT),

    // Sheep
    SHEEP_COLOR(SHEEP, 16, EntityDataTypes1_8.BYTE, 12, EntityDataTypes1_9.BYTE),

    // Villager
    VILLAGER_PROFESSION(VILLAGER, 16, EntityDataTypes1_8.INT, 12, EntityDataTypes1_9.VAR_INT),

    // Enderman
    ENDERMAN_BLOCK_STATE(ENDERMAN, 16, EntityDataTypes1_8.SHORT, 11, EntityDataTypes1_9.OPTIONAL_BLOCK_STATE),
    ENDERMAN_BLOCK_DATA(ENDERMAN, 17, EntityDataTypes1_8.BYTE, null), // always 0 when sent, never read by the client
    ENDERMAN_IS_SCREAMING(ENDERMAN, 18, EntityDataTypes1_8.BYTE, 12, EntityDataTypes1_9.BOOLEAN),

    // Zombie
    ZOMBIE_IS_CHILD(ZOMBIE, 12, EntityDataTypes1_8.BYTE, 11, EntityDataTypes1_9.BOOLEAN),
    ZOMBIE_IS_VILLAGER(ZOMBIE, 13, EntityDataTypes1_8.BYTE, 12, EntityDataTypes1_9.VAR_INT), // now indicates villager type
    ZOMBIE_IS_CONVERTING(ZOMBIE, 14, EntityDataTypes1_8.BYTE, 13, EntityDataTypes1_9.BOOLEAN),
    // arms raised added in 1.9 (14/boolean)

    // Blaze
    BLAZE_ON_FIRE(BLAZE, 16, EntityDataTypes1_8.BYTE, 11, EntityDataTypes1_9.BYTE),

    // Spider
    SPIDER_CLIMBING(SPIDER, 16, EntityDataTypes1_8.BYTE, 11, EntityDataTypes1_9.BYTE),

    // Creeper
    CREEPER_FUSE(CREEPER, 16, EntityDataTypes1_8.BYTE, 11, EntityDataTypes1_9.VAR_INT),
    CREEPER_IS_POWERED(CREEPER, 17, EntityDataTypes1_8.BYTE, 12, EntityDataTypes1_9.BOOLEAN),
    CREEPER_IS_IGNITED(CREEPER, 18, EntityDataTypes1_8.BYTE, 13, EntityDataTypes1_9.BOOLEAN),

    // Ghast
    GHAST_IS_ATTACKING(GHAST, 16, EntityDataTypes1_8.BYTE, 11, EntityDataTypes1_9.BOOLEAN),

    // Slime
    SLIME_SIZE(SLIME, 16, EntityDataTypes1_8.BYTE, 11, EntityDataTypes1_9.VAR_INT),

    // Skeleton
    SKELETON_TYPE(SKELETON, 13, EntityDataTypes1_8.BYTE, 11, EntityDataTypes1_9.VAR_INT),

    // Witch
    WITCH_AGGRESSIVE(WITCH, 21, EntityDataTypes1_8.BYTE, 11, EntityDataTypes1_9.BOOLEAN),

    // Iron golem
    IRON_GOLEM_PLAYER_MADE(IRON_GOLEM, 16, EntityDataTypes1_8.BYTE, 11, EntityDataTypes1_9.BYTE),

    // Wither
    WITHER_TARGET1(WITHER, 17, EntityDataTypes1_8.INT, 11, EntityDataTypes1_9.VAR_INT),
    WITHER_TARGET2(WITHER, 18, EntityDataTypes1_8.INT, 12, EntityDataTypes1_9.VAR_INT),
    WITHER_TARGET3(WITHER, 19, EntityDataTypes1_8.INT, 13, EntityDataTypes1_9.VAR_INT),
    WITHER_INVULNERABILITY_TIME(WITHER, 20, EntityDataTypes1_8.INT, 14, EntityDataTypes1_9.VAR_INT),

    // Wither skull
    WITHER_SKULL_INVULNERABILITY(WITHER_SKULL, 10, EntityDataTypes1_8.BYTE, 5, EntityDataTypes1_9.BOOLEAN),

    // Guardian
    GUARDIAN_INFO(GUARDIAN, 16, EntityDataTypes1_8.INT, 11, EntityDataTypes1_9.BYTE),
    GUARDIAN_TARGET(GUARDIAN, 17, EntityDataTypes1_8.INT, 12, EntityDataTypes1_9.VAR_INT),

    // Boat
    BOAT_SINCE_HIT(BOAT, 17, EntityDataTypes1_8.INT, 5, EntityDataTypes1_9.VAR_INT),
    BOAT_FORWARD_DIRECTION(BOAT, 18, EntityDataTypes1_8.INT, 6, EntityDataTypes1_9.VAR_INT),
    BOAT_DAMAGE_TAKEN(BOAT, 19, EntityDataTypes1_8.FLOAT, 7, EntityDataTypes1_9.FLOAT),
    // boat type added in 1.9 (20/varint)

    // Minecart
    ABSTRACT_MINECART_SHAKING_POWER(ABSTRACT_MINECART, 17, EntityDataTypes1_8.INT, 5, EntityDataTypes1_9.VAR_INT),
    ABSTRACT_MINECART_SHAKING_DIRECTION(ABSTRACT_MINECART, 18, EntityDataTypes1_8.INT, 6, EntityDataTypes1_9.VAR_INT),
    ABSTRACT_MINECART_DAMAGE_TAKEN(ABSTRACT_MINECART, 19, EntityDataTypes1_8.FLOAT, 7, EntityDataTypes1_9.FLOAT),
    ABSTRACT_MINECART_BLOCK(ABSTRACT_MINECART, 20, EntityDataTypes1_8.INT, 8, EntityDataTypes1_9.VAR_INT),
    ABSTRACT_MINECART_BLOCK_Y(ABSTRACT_MINECART, 21, EntityDataTypes1_8.INT, 9, EntityDataTypes1_9.VAR_INT),
    ABSTRACT_MINECART_SHOW_BLOCK(ABSTRACT_MINECART, 22, EntityDataTypes1_8.BYTE, 10, EntityDataTypes1_9.BOOLEAN),

    // Command minecart
    COMMAND_BLOCK_MINECART_COMMAND(COMMAND_BLOCK_MINECART, 23, EntityDataTypes1_8.STRING, 11, EntityDataTypes1_9.STRING),
    COMMAND_BLOCK_MINECART_OUTPUT(COMMAND_BLOCK_MINECART, 24, EntityDataTypes1_8.STRING, 12, EntityDataTypes1_9.COMPONENT),

    // Furnace minecart
    FURNACE_MINECART_IS_POWERED(FURNACE_MINECART, 16, EntityDataTypes1_8.BYTE, 11, EntityDataTypes1_9.BOOLEAN),

    // Item drop
    ITEM_ITEM(ITEM, 10, EntityDataTypes1_8.ITEM, 5, EntityDataTypes1_9.ITEM),

    // Arrow
    ARROW_IS_CRIT(ARROW, 16, EntityDataTypes1_8.BYTE, 5, EntityDataTypes1_9.BYTE),

    // Firework
    FIREWORK_ROCKET_INFO(FIREWORK_ROCKET, 8, EntityDataTypes1_8.ITEM, 5, EntityDataTypes1_9.ITEM),

    // Item frame
    ITEM_FRAME_ITEM(ITEM_FRAME, 8, EntityDataTypes1_8.ITEM, 5, EntityDataTypes1_9.ITEM),
    ITEM_FRAME_ROTATION(ITEM_FRAME, 9, EntityDataTypes1_8.BYTE, 6, EntityDataTypes1_9.VAR_INT),

    // Ender crystal
    END_CRYSTAL_HEALTH(END_CRYSTAL, 8, EntityDataTypes1_8.INT, null),

    // Ender dragon
    ENDER_DRAGON_PHASE(ENDER_DRAGON, 11, EntityDataTypes1_9.VAR_INT);

    private static final HashMap<Pair<EntityTypes1_9.EntityType, Integer>, EntityDataIndex1_9> entityDataRewriters = new HashMap<>();

    static {
        for (EntityDataIndex1_9 index : EntityDataIndex1_9.values()) {
            entityDataRewriters.put(new Pair<>(index.clazz, index.index), index);
        }
    }

    private final EntityTypes1_9.EntityType clazz;
    private final int newIndex;
    private final EntityDataTypes1_9 newType;
    private final EntityDataTypes1_8 oldType;
    private final int index;

    EntityDataIndex1_9(EntityTypes1_9.EntityType type, int index, EntityDataTypes1_8 oldType, @Nullable EntityDataTypes1_9 newType) {
        this.clazz = type;
        this.index = index;
        this.newIndex = index;
        this.oldType = oldType;
        this.newType = newType;
    }

    EntityDataIndex1_9(EntityTypes1_9.EntityType type, int newIndex, @Nullable EntityDataTypes1_9 newType) {
        this.clazz = type;
        this.index = -1;
        this.oldType = null;
        this.newIndex = newIndex;
        this.newType = newType;
    }

    EntityDataIndex1_9(EntityTypes1_9.EntityType type, int index, EntityDataTypes1_8 oldType, int newIndex, @Nullable EntityDataTypes1_9 newType) {
        this.clazz = type;
        this.index = index;
        this.oldType = oldType;
        this.newIndex = newIndex;
        this.newType = newType;
    }

    public EntityTypes1_9.EntityType getClazz() {
        return clazz;
    }

    public int getNewIndex() {
        return newIndex;
    }

    public @Nullable EntityDataTypes1_9 getNewType() {
        return newType;
    }

    public EntityDataTypes1_8 getOldType() {
        return oldType;
    }

    public int getIndex() {
        return index;
    }

    private static Optional<EntityDataIndex1_9> getIndex(EntityType type, int index) {
        Pair pair = new Pair<>(type, index);
        return Optional.ofNullable(entityDataRewriters.get(pair));
    }

    public static EntityDataIndex1_9 searchIndex(EntityType type, int index) {
        EntityType currentType = type;
        do {
            Optional<EntityDataIndex1_9> optData = getIndex(currentType, index);

            if (optData.isPresent()) {
                return optData.get();
            }

            currentType = currentType.getParent();
        } while (currentType != null);

        return null;
    }

}

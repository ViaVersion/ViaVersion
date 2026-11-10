/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2026 ViaVersion and contributors
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
package com.viaversion.viaversion.protocols.v1_20to1_20_2.packet;

public enum ClientboundPackets1_20_2 implements ClientboundPacket1_20_2 {

    BUNDLE_DELIMITER, // 0x00
    ADD_ENTITY, // 0x01
    ADD_EXPERIENCE_ORB, // 0x02
    ANIMATE, // 0x03
    AWARD_STATS, // 0x04
    BLOCK_CHANGED_ACK, // 0x05
    BLOCK_DESTRUCTION, // 0x06
    BLOCK_ENTITY_DATA, // 0x07
    BLOCK_EVENT, // 0x08
    BLOCK_UPDATE, // 0x09
    BOSS_EVENT, // 0x0A
    CHANGE_DIFFICULTY, // 0x0B
    CHUNK_BATCH_FINISHED, // 0x0C
    CHUNK_BATCH_START, // 0x0D
    CHUNKS_BIOMES, // 0x0E
    CLEAR_TITLES, // 0x0F
    COMMAND_SUGGESTIONS, // 0x10
    COMMANDS, // 0x11
    CONTAINER_CLOSE, // 0x12
    CONTAINER_SET_CONTENT, // 0x13
    CONTAINER_SET_DATA, // 0x14
    CONTAINER_SET_SLOT, // 0x15
    COOLDOWN, // 0x16
    CUSTOM_CHAT_COMPLETIONS, // 0x17
    CUSTOM_PAYLOAD, // 0x18
    DAMAGE_EVENT, // 0x19
    DELETE_CHAT, // 0x1A
    DISCONNECT, // 0x1B
    DISGUISED_CHAT, // 0x1C
    ENTITY_EVENT, // 0x1D
    EXPLODE, // 0x1E
    FORGET_LEVEL_CHUNK, // 0x1F
    GAME_EVENT, // 0x20
    HORSE_SCREEN_OPEN, // 0x21
    HURT_ANIMATION, // 0x22
    INITIALIZE_BORDER, // 0x23
    KEEP_ALIVE, // 0x24
    LEVEL_CHUNK_WITH_LIGHT, // 0x25
    LEVEL_EVENT, // 0x26
    LEVEL_PARTICLES, // 0x27
    LIGHT_UPDATE, // 0x28
    LOGIN, // 0x29
    MAP_ITEM_DATA, // 0x2A
    MERCHANT_OFFERS, // 0x2B
    MOVE_ENTITY_POS, // 0x2C
    MOVE_ENTITY_POS_ROT, // 0x2D
    MOVE_ENTITY_ROT, // 0x2E
    MOVE_VEHICLE, // 0x2F
    OPEN_BOOK, // 0x30
    OPEN_SCREEN, // 0x31
    OPEN_SIGN_EDITOR, // 0x32
    PING, // 0x33
    PONG_RESPONSE, // 0x34
    PLACE_GHOST_RECIPE, // 0x35
    PLAYER_ABILITIES, // 0x36
    PLAYER_CHAT, // 0x37
    PLAYER_COMBAT_END, // 0x38
    PLAYER_COMBAT_ENTER, // 0x39
    PLAYER_COMBAT_KILL, // 0x3A
    PLAYER_INFO_REMOVE, // 0x3B
    PLAYER_INFO_UPDATE, // 0x3C
    PLAYER_LOOK_AT, // 0x3D
    PLAYER_POSITION, // 0x3E
    RECIPE, // 0x3F
    REMOVE_ENTITIES, // 0x40
    REMOVE_MOB_EFFECT, // 0x41
    RESOURCE_PACK, // 0x42
    RESPAWN, // 0x43
    ROTATE_HEAD, // 0x44
    SECTION_BLOCKS_UPDATE, // 0x45
    SELECT_ADVANCEMENTS_TAB, // 0x46
    SERVER_DATA, // 0x47
    SET_ACTION_BAR_TEXT, // 0x48
    SET_BORDER_CENTER, // 0x49
    SET_BORDER_LERP_SIZE, // 0x4A
    SET_BORDER_SIZE, // 0x4B
    SET_BORDER_WARNING_DELAY, // 0x4C
    SET_BORDER_WARNING_DISTANCE, // 0x4D
    SET_CAMERA, // 0x4E
    SET_CARRIED_ITEM, // 0x4F
    SET_CHUNK_CACHE_CENTER, // 0x50
    SET_CHUNK_CACHE_RADIUS, // 0x51
    SET_DEFAULT_SPAWN_POSITION, // 0x52
    SET_DISPLAY_OBJECTIVE, // 0x53
    SET_ENTITY_DATA, // 0x54
    SET_ENTITY_LINK, // 0x55
    SET_ENTITY_MOTION, // 0x56
    SET_EQUIPMENT, // 0x57
    SET_EXPERIENCE, // 0x58
    SET_HEALTH, // 0x59
    SET_OBJECTIVE, // 0x5A
    SET_PASSENGERS, // 0x5B
    SET_PLAYER_TEAM, // 0x5C
    SET_SCORE, // 0x5D
    SET_SIMULATION_DISTANCE, // 0x5E
    SET_SUBTITLE_TEXT, // 0x5F
    SET_TIME, // 0x60
    SET_TITLE_TEXT, // 0x61
    SET_TITLES_ANIMATION, // 0x62
    SOUND_ENTITY, // 0x63
    SOUND, // 0x64
    START_CONFIGURATION, // 0x65
    STOP_SOUND, // 0x66
    SYSTEM_CHAT, // 0x67
    TAB_LIST, // 0x68
    TAG_QUERY, // 0x69
    TAKE_ITEM_ENTITY, // 0x6A
    TELEPORT_ENTITY, // 0x6B
    UPDATE_ADVANCEMENTS, // 0x6C
    UPDATE_ATTRIBUTES, // 0x6D
    UPDATE_MOB_EFFECT, // 0x6E
    UPDATE_RECIPES, // 0x6F
    UPDATE_TAGS; // 0x70

    @Override
    public int getId() {
        return ordinal();
    }

    @Override
    public String getName() {
        return name();
    }
}

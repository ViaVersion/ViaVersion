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
package com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet;

public enum ServerboundPackets1_21_6 implements ServerboundPacket1_21_6 {

    ACCEPT_TELEPORTATION,
    BLOCK_ENTITY_TAG_QUERY,
    BUNDLE_ITEM_SELECTED,
    CHANGE_DIFFICULTY,
    CHANGE_GAME_MODE,
    CHAT_ACK,
    CHAT_COMMAND,
    CHAT_COMMAND_SIGNED,
    CHAT,
    CHAT_SESSION_UPDATE,
    CHUNK_BATCH_RECEIVED,
    CLIENT_COMMAND,
    CLIENT_TICK_END,
    CLIENT_INFORMATION,
    COMMAND_SUGGESTION,
    CONFIGURATION_ACKNOWLEDGED,
    CONTAINER_BUTTON_CLICK,
    CONTAINER_CLICK,
    CONTAINER_CLOSE,
    CONTAINER_SLOT_STATE_CHANGED,
    COOKIE_RESPONSE,
    CUSTOM_PAYLOAD,
    DEBUG_SAMPLE_SUBSCRIPTION,
    EDIT_BOOK,
    ENTITY_TAG_QUERY,
    INTERACT,
    JIGSAW_GENERATE,
    KEEP_ALIVE,
    LOCK_DIFFICULTY,
    MOVE_PLAYER_POS,
    MOVE_PLAYER_POS_ROT,
    MOVE_PLAYER_ROT,
    MOVE_PLAYER_STATUS_ONLY,
    MOVE_VEHICLE,
    PADDLE_BOAT,
    PICK_ITEM_FROM_BLOCK,
    PICK_ITEM_FROM_ENTITY,
    PING_REQUEST,
    PLACE_RECIPE,
    PLAYER_ABILITIES,
    PLAYER_ACTION,
    PLAYER_COMMAND,
    PLAYER_INPUT,
    PLAYER_LOADED,
    PONG,
    RECIPE_BOOK_CHANGE_SETTINGS,
    RECIPE_BOOK_SEEN_RECIPE,
    RENAME_ITEM,
    RESOURCE_PACK,
    SEEN_ADVANCEMENTS,
    SELECT_TRADE,
    SET_BEACON,
    SET_CARRIED_ITEM,
    SET_COMMAND_BLOCK,
    SET_COMMAND_MINECART,
    SET_CREATIVE_MODE_SLOT,
    SET_JIGSAW_BLOCK,
    SET_STRUCTURE_BLOCK,
    SET_TEST_BLOCK,
    SIGN_UPDATE,
    SWING,
    TELEPORT_TO_ENTITY,
    TEST_INSTANCE_BLOCK_ACTION,
    USE_ITEM_ON,
    USE_ITEM,
    CUSTOM_CLICK_ACTION;

    @Override
    public int getId() {
        return ordinal();
    }

    @Override
    public String getName() {
        return name();
    }
}

package us.myles.ViaVersion.protocols.protocol1_12to1_11_1;

import us.myles.ViaVersion.api.protocol.ServerboundPacketType;

public enum ServerboundPackets1_12 implements ServerboundPacketType {

    TELEPORT_CONFIRM, // 0x00
    PREPARE_CRAFTING_GRID, // 0x01
    TAB_COMPLETE, // 0x02
    CHAT_MESSAGE, // 0x03
    CLIENT_STATUS, // 0x04
    CLIENT_SETTINGS, // 0x05
    WINDOW_CONFIRMATION, // 0x06
    CLICK_WINDOW_BUTTON, // 0x07
    CLICK_WINDOW, // 0x08
    CLOSE_WINDOW, // 0x09
    PLUGIN_MESSAGE, // 0x0A
    INTERACT_ENTITY, // 0x0B
    KEEP_ALIVE, // 0x0C
    PLAYER_MOVEMENT, // 0x0D
    PLAYER_POSITION, // 0x0E
    PLAYER_POSITION_AND_ROTATION, // 0x0F
    PLAYER_ROTATION, // 0x10
    VEHICLE_MOVE, // 0x11
    STEER_BOAT, // 0x12
    PLAYER_ABILITIES, // 0x13
    PLAYER_DIGGING, // 0x14
    ENTITY_ACTION, // 0x15
    STEER_VEHICLE, // 0x16
    RECIPE_BOOK_DATA, // 0x17
    RESOURCE_PACK_STATUS, // 0x18
    ADVANCEMENT_TAB, // 0x19
    HELD_ITEM_CHANGE, // 0x1A
    CREATIVE_INVENTORY_ACTION, // 0x1B
    UPDATE_SIGN, // 0x1C
    ANIMATION, // 0x1D
    SPECTATE, // 0x1E
    PLAYER_BLOCK_PLACEMENT, // 0x1F
    USE_ITEM, // 0x20
}

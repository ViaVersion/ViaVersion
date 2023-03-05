/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2023 ViaVersion and contributors
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
package com.viaversion.viaversion.rewriter;

import com.viaversion.viaversion.api.data.Mappings;
import com.viaversion.viaversion.api.data.ParticleMappings;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.ServerboundPacketType;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.rewriter.RewriterBase;
import com.viaversion.viaversion.api.type.Type;
import org.checkerframework.checker.nullness.qual.Nullable;

public abstract class ItemRewriter<C extends ClientboundPacketType, S extends ServerboundPacketType,
        T extends Protocol<C, ?, ?, S>> extends RewriterBase<T> implements com.viaversion.viaversion.api.rewriter.ItemRewriter<T> {

    protected ItemRewriter(T protocol) {
        super(protocol);
    }

    // These two methods always return the same item instance *for now*
    // It is made this way, so it's easy to handle new instance creation/implementation changes
    @Override
    public @Nullable Item handleItemToClient(@Nullable Item item) {
        if (item == null) return null;
        if (protocol.getMappingData() != null && protocol.getMappingData().getItemMappings() != null) {
            item.setIdentifier(protocol.getMappingData().getNewItemId(item.identifier()));
        }
        return item;
    }

    @Override
    public @Nullable Item handleItemToServer(@Nullable Item item) {
        if (item == null) return null;
        if (protocol.getMappingData() != null && protocol.getMappingData().getItemMappings() != null) {
            item.setIdentifier(protocol.getMappingData().getOldItemId(item.identifier()));
        }
        return item;
    }

    public void registerWindowItems(C packetType, Type<Item[]> type) {
        protocol.registerClientbound(packetType, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.UNSIGNED_BYTE); // Window id
                map(type); // Items
                handler(itemArrayHandler(type));
            }
        });
    }

    public void registerWindowItems1_17_1(C packetType) {
        protocol.registerClientbound(packetType, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.UNSIGNED_BYTE); // Window id
                map(Type.VAR_INT); // State id
                handler(wrapper -> {
                    Item[] items = wrapper.passthrough(Type.FLAT_VAR_INT_ITEM_ARRAY_VAR_INT);
                    for (Item item : items) {
                        handleItemToClient(item);
                    }

                    handleItemToClient(wrapper.passthrough(Type.FLAT_VAR_INT_ITEM)); // Carried item
                });
            }
        });
    }

    public void registerSetSlot(C packetType, Type<Item> type) {
        protocol.registerClientbound(packetType, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.UNSIGNED_BYTE); // Window id
                map(Type.SHORT); // Slot id
                map(type); // Item
                handler(itemToClientHandler(type));
            }
        });
    }

    public void registerSetSlot1_17_1(C packetType) {
        protocol.registerClientbound(packetType, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.UNSIGNED_BYTE); // Window id
                map(Type.VAR_INT); // State id
                map(Type.SHORT); // Slot id
                map(Type.FLAT_VAR_INT_ITEM); // Item
                handler(itemToClientHandler(Type.FLAT_VAR_INT_ITEM));
            }
        });
    }

    // Sub 1.16
    public void registerEntityEquipment(C packetType, Type<Item> type) {
        protocol.registerClientbound(packetType, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT); // 0 - Entity ID
                map(Type.VAR_INT); // 1 - Slot ID
                map(type); // 2 - Item

                handler(itemToClientHandler(type));
            }
        });
    }

    // 1.16+
    public void registerEntityEquipmentArray(C packetType) {
        protocol.registerClientbound(packetType, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT); // 0 - Entity ID

                handler(wrapper -> {
                    byte slot;
                    do {
                        slot = wrapper.passthrough(Type.BYTE);
                        // & 0x7F into an extra variable if slot is needed
                        handleItemToClient(wrapper.passthrough(Type.FLAT_VAR_INT_ITEM));
                    } while ((slot & 0xFFFFFF80) != 0);
                });
            }
        });
    }

    public void registerCreativeInvAction(S packetType, Type<Item> type) {
        protocol.registerServerbound(packetType, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.SHORT); // 0 - Slot
                map(type); // 1 - Clicked Item

                handler(itemToServerHandler(type));
            }
        });
    }

    public void registerClickWindow(S packetType, Type<Item> type) {
        protocol.registerServerbound(packetType, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.UNSIGNED_BYTE); // 0 - Window ID
                map(Type.SHORT); // 1 - Slot
                map(Type.BYTE); // 2 - Button
                map(Type.SHORT); // 3 - Action number
                map(Type.VAR_INT); // 4 - Mode
                map(type); // 5 - Clicked Item

                handler(itemToServerHandler(type));
            }
        });
    }

    public void registerClickWindow1_17_1(S packetType) {
        protocol.registerServerbound(packetType, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.UNSIGNED_BYTE); // Window Id
                map(Type.VAR_INT); // State id
                map(Type.SHORT); // Slot
                map(Type.BYTE); // Button
                map(Type.VAR_INT); // Mode

                handler(wrapper -> {
                    // Affected items
                    int length = wrapper.passthrough(Type.VAR_INT);
                    for (int i = 0; i < length; i++) {
                        wrapper.passthrough(Type.SHORT); // Slot
                        handleItemToServer(wrapper.passthrough(Type.FLAT_VAR_INT_ITEM));
                    }

                    // Carried item
                    handleItemToServer(wrapper.passthrough(Type.FLAT_VAR_INT_ITEM));
                });
            }
        });
    }

    public void registerSetCooldown(C packetType) {
        protocol.registerClientbound(packetType, wrapper -> {
            int itemId = wrapper.read(Type.VAR_INT);
            wrapper.write(Type.VAR_INT, protocol.getMappingData().getNewItemId(itemId));
        });
    }

    // 1.14.4+
    public void registerTradeList(C packetType) {
        protocol.registerClientbound(packetType, wrapper -> {
            wrapper.passthrough(Type.VAR_INT);
            int size = wrapper.passthrough(Type.UNSIGNED_BYTE);
            for (int i = 0; i < size; i++) {
                handleItemToClient(wrapper.passthrough(Type.FLAT_VAR_INT_ITEM)); // Input
                handleItemToClient(wrapper.passthrough(Type.FLAT_VAR_INT_ITEM)); // Output

                if (wrapper.passthrough(Type.BOOLEAN)) { // Has second item
                    handleItemToClient(wrapper.passthrough(Type.FLAT_VAR_INT_ITEM)); // Second Item
                }

                wrapper.passthrough(Type.BOOLEAN); // Trade disabled
                wrapper.passthrough(Type.INT); // Number of tools uses
                wrapper.passthrough(Type.INT); // Maximum number of trade uses

                wrapper.passthrough(Type.INT); // XP
                wrapper.passthrough(Type.INT); // Special price
                wrapper.passthrough(Type.FLOAT); // Price multiplier
                wrapper.passthrough(Type.INT); // Demand
            }
            //...
        });
    }

    public void registerTradeList1_19(C packetType) {
        protocol.registerClientbound(packetType, wrapper -> {
            wrapper.passthrough(Type.VAR_INT); // Container id
            int size = wrapper.passthrough(Type.VAR_INT);
            for (int i = 0; i < size; i++) {
                handleItemToClient(wrapper.passthrough(Type.FLAT_VAR_INT_ITEM)); // Input
                handleItemToClient(wrapper.passthrough(Type.FLAT_VAR_INT_ITEM)); // Output
                handleItemToClient(wrapper.passthrough(Type.FLAT_VAR_INT_ITEM)); // Second Item

                wrapper.passthrough(Type.BOOLEAN); // Trade disabled
                wrapper.passthrough(Type.INT); // Number of tools uses
                wrapper.passthrough(Type.INT); // Maximum number of trade uses

                wrapper.passthrough(Type.INT); // XP
                wrapper.passthrough(Type.INT); // Special price
                wrapper.passthrough(Type.FLOAT); // Price multiplier
                wrapper.passthrough(Type.INT); // Demand
            }
        });
    }

    public void registerAdvancements(C packetType, Type<Item> type) {
        protocol.registerClientbound(packetType, wrapper -> {
            wrapper.passthrough(Type.BOOLEAN); // Reset/clear
            int size = wrapper.passthrough(Type.VAR_INT); // Mapping size
            for (int i = 0; i < size; i++) {
                wrapper.passthrough(Type.STRING); // Identifier

                // Parent
                if (wrapper.passthrough(Type.BOOLEAN))
                    wrapper.passthrough(Type.STRING);

                // Display data
                if (wrapper.passthrough(Type.BOOLEAN)) {
                    wrapper.passthrough(Type.COMPONENT); // Title
                    wrapper.passthrough(Type.COMPONENT); // Description
                    handleItemToClient(wrapper.passthrough(type)); // Icon
                    wrapper.passthrough(Type.VAR_INT); // Frame type
                    int flags = wrapper.passthrough(Type.INT); // Flags
                    if ((flags & 1) != 0) {
                        wrapper.passthrough(Type.STRING); // Background texture
                    }
                    wrapper.passthrough(Type.FLOAT); // X
                    wrapper.passthrough(Type.FLOAT); // Y
                }

                wrapper.passthrough(Type.STRING_ARRAY); // Criteria

                int arrayLength = wrapper.passthrough(Type.VAR_INT);
                for (int array = 0; array < arrayLength; array++) {
                    wrapper.passthrough(Type.STRING_ARRAY); // String array
                }
            }
        });
    }

    public void registerWindowPropertyEnchantmentHandler(C packetType) {
        protocol.registerClientbound(packetType, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.UNSIGNED_BYTE); // Container id
                handler(wrapper -> {
                    Mappings mappings = protocol.getMappingData().getEnchantmentMappings();
                    if (mappings == null) {
                        return;
                    }

                    short property = wrapper.passthrough(Type.SHORT);
                    if (property >= 4 && property <= 6) { // Enchantment id
                        short enchantmentId = (short) mappings.getNewId(wrapper.read(Type.SHORT));
                        wrapper.write(Type.SHORT, enchantmentId);
                    }
                });
            }
        });
    }

    // Not the very best place for this, but has to stay here until *everything* is abstracted
    public void registerSpawnParticle(C packetType, Type<Item> itemType, Type<?> coordType) {
        protocol.registerClientbound(packetType, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.INT); // 0 - Particle ID
                map(Type.BOOLEAN); // 1 - Long Distance
                map(coordType); // 2 - X
                map(coordType); // 3 - Y
                map(coordType); // 4 - Z
                map(Type.FLOAT); // 5 - Offset X
                map(Type.FLOAT); // 6 - Offset Y
                map(Type.FLOAT); // 7 - Offset Z
                map(Type.FLOAT); // 8 - Particle Data
                map(Type.INT); // 9 - Particle Count
                handler(getSpawnParticleHandler(itemType));
            }
        });
    }

    public void registerSpawnParticle1_19(C packetType) {
        protocol.registerClientbound(packetType, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT); // 0 - Particle ID
                map(Type.BOOLEAN); // 1 - Long Distance
                map(Type.DOUBLE); // 2 - X
                map(Type.DOUBLE); // 3 - Y
                map(Type.DOUBLE); // 4 - Z
                map(Type.FLOAT); // 5 - Offset X
                map(Type.FLOAT); // 6 - Offset Y
                map(Type.FLOAT); // 7 - Offset Z
                map(Type.FLOAT); // 8 - Particle Data
                map(Type.INT); // 9 - Particle Count
                handler(getSpawnParticleHandler(Type.VAR_INT, Type.FLAT_VAR_INT_ITEM));
            }
        });
    }

    public PacketHandler getSpawnParticleHandler(Type<Item> itemType) {
        return getSpawnParticleHandler(Type.INT, itemType);
    }

    public PacketHandler getSpawnParticleHandler(Type<Integer> idType, Type<Item> itemType) {
        return wrapper -> {
            int id = wrapper.get(idType, 0);
            if (id == -1) return;

            ParticleMappings mappings = protocol.getMappingData().getParticleMappings();
            if (mappings.isBlockParticle(id)) {
                int data = wrapper.read(Type.VAR_INT);
                wrapper.write(Type.VAR_INT, protocol.getMappingData().getNewBlockStateId(data));
            } else if (mappings.isItemParticle(id)) {
                handleItemToClient(wrapper.passthrough(itemType));
            }

            int newId = protocol.getMappingData().getNewParticleId(id);
            if (newId != id) {
                wrapper.set(idType, 0, newId);
            }
        };
    }

    // Only sent to the client
    public PacketHandler itemArrayHandler(Type<Item[]> type) {
        return wrapper -> {
            Item[] items = wrapper.get(type, 0);
            for (Item item : items) {
                handleItemToClient(item);
            }
        };
    }

    public PacketHandler itemToClientHandler(Type<Item> type) {
        return wrapper -> handleItemToClient(wrapper.get(type, 0));
    }

    public PacketHandler itemToServerHandler(Type<Item> type) {
        return wrapper -> handleItemToServer(wrapper.get(type, 0));
    }
}

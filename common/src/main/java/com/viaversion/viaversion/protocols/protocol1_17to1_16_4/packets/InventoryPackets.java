/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2021 ViaVersion and contributors
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
package com.viaversion.viaversion.protocols.protocol1_17to1_16_4.packets;

import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketRemapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_16_2to1_16_1.ClientboundPackets1_16_2;
import com.viaversion.viaversion.protocols.protocol1_16_2to1_16_1.ServerboundPackets1_16_2;
import com.viaversion.viaversion.protocols.protocol1_16to1_15_2.data.RecipeRewriter1_16;
import com.viaversion.viaversion.protocols.protocol1_17to1_16_4.ClientboundPackets1_17;
import com.viaversion.viaversion.protocols.protocol1_17to1_16_4.Protocol1_17To1_16_4;
import com.viaversion.viaversion.protocols.protocol1_17to1_16_4.ServerboundPackets1_17;
import com.viaversion.viaversion.protocols.protocol1_17to1_16_4.storage.InventoryAcknowledgements;
import com.viaversion.viaversion.rewriter.ItemRewriter;

public final class InventoryPackets extends ItemRewriter<Protocol1_17To1_16_4> {

    public InventoryPackets(Protocol1_17To1_16_4 protocol) {
        super(protocol);
    }

    @Override
    public void registerPackets() {
        registerSetCooldown(ClientboundPackets1_16_2.COOLDOWN);
        registerWindowItems(ClientboundPackets1_16_2.WINDOW_ITEMS, Type.FLAT_VAR_INT_ITEM_ARRAY);
        registerTradeList(ClientboundPackets1_16_2.TRADE_LIST, Type.FLAT_VAR_INT_ITEM);
        registerSetSlot(ClientboundPackets1_16_2.SET_SLOT, Type.FLAT_VAR_INT_ITEM);
        registerAdvancements(ClientboundPackets1_16_2.ADVANCEMENTS, Type.FLAT_VAR_INT_ITEM);
        registerEntityEquipmentArray(ClientboundPackets1_16_2.ENTITY_EQUIPMENT, Type.FLAT_VAR_INT_ITEM);
        registerSpawnParticle(ClientboundPackets1_16_2.SPAWN_PARTICLE, Type.FLAT_VAR_INT_ITEM, Type.DOUBLE);

        new RecipeRewriter1_16(protocol).registerDefaultHandler(ClientboundPackets1_16_2.DECLARE_RECIPES);

        registerCreativeInvAction(ServerboundPackets1_17.CREATIVE_INVENTORY_ACTION, Type.FLAT_VAR_INT_ITEM);

        protocol.registerServerbound(ServerboundPackets1_17.EDIT_BOOK, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> handleItemToServer(wrapper.passthrough(Type.FLAT_VAR_INT_ITEM)));
            }
        });

        protocol.registerServerbound(ServerboundPackets1_17.CLICK_WINDOW, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.UNSIGNED_BYTE); // Window Id
                map(Type.SHORT); // Slot
                map(Type.BYTE); // Button
                handler(wrapper -> wrapper.write(Type.SHORT, (short) 0)); // Action id - doesn't matter, as the sent out confirmation packet will be cancelled
                map(Type.VAR_INT); // Action

                handler(wrapper -> {
                    // Affected items - throw them away!
                    int length = wrapper.read(Type.VAR_INT);
                    for (int i = 0; i < length; i++) {
                        wrapper.read(Type.SHORT); // Slot
                        wrapper.read(Type.FLAT_VAR_INT_ITEM);
                    }

                    // 1.17 clients send the then carried item, but 1.16 expects the clicked one
                    Item item = wrapper.read(Type.FLAT_VAR_INT_ITEM);
                    int action = wrapper.get(Type.VAR_INT, 0);
                    if (action == 5) {
                        // Quick craft (= dragging / mouse movement while clicking on an empty slot)
                        // The server always expects an empty item here
                        item = null;
                    } else {
                        // Use the item sent
                        handleItemToServer(item);
                    }

                    wrapper.write(Type.FLAT_VAR_INT_ITEM, item);
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_16_2.WINDOW_CONFIRMATION, null, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> {
                    short inventoryId = wrapper.read(Type.UNSIGNED_BYTE);
                    short confirmationId = wrapper.read(Type.SHORT);
                    boolean accepted = wrapper.read(Type.BOOLEAN);
                    if (!accepted) {
                        // Use the new ping packet to replace the removed acknowledgement, extra bit for fast dismissal
                        int id = (1 << 30) | (inventoryId << 16) | (confirmationId & 0xFFFF);
                        wrapper.user().get(InventoryAcknowledgements.class).addId(id);

                        PacketWrapper pingPacket = wrapper.create(ClientboundPackets1_17.PING);
                        pingPacket.write(Type.INT, id);
                        pingPacket.send(Protocol1_17To1_16_4.class);
                    }

                    wrapper.cancel();
                });
            }
        });

        // New pong packet
        protocol.registerServerbound(ServerboundPackets1_17.PONG, null, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> {
                    int id = wrapper.read(Type.INT);
                    // Check extra bit for fast dismissal
                    if ((id & (1 << 30)) != 0 && wrapper.user().get(InventoryAcknowledgements.class).removeId(id)) {
                        // Decode our requested inventory acknowledgement
                        short inventoryId = (short) ((id >> 16) & 0xFF);
                        short confirmationId = (short) (id & 0xFFFF);
                        PacketWrapper packet = wrapper.create(ServerboundPackets1_16_2.WINDOW_CONFIRMATION);
                        packet.write(Type.UNSIGNED_BYTE, inventoryId);
                        packet.write(Type.SHORT, confirmationId);
                        packet.write(Type.BYTE, (byte) 1); // Accept
                        packet.sendToServer(Protocol1_17To1_16_4.class);
                    }

                    wrapper.cancel();
                });
            }
        });
    }
}

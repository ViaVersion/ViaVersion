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
package com.viaversion.viaversion.protocols.protocol1_12to1_11_1.packets;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.protocol.remapper.PacketRemapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_12to1_11_1.BedRewriter;
import com.viaversion.viaversion.protocols.protocol1_12to1_11_1.Protocol1_12To1_11_1;
import com.viaversion.viaversion.protocols.protocol1_12to1_11_1.ServerboundPackets1_12;
import com.viaversion.viaversion.protocols.protocol1_12to1_11_1.providers.InventoryQuickMoveProvider;
import com.viaversion.viaversion.protocols.protocol1_9_3to1_9_1_2.ClientboundPackets1_9_3;
import com.viaversion.viaversion.rewriter.ItemRewriter;

public class InventoryPackets extends ItemRewriter<Protocol1_12To1_11_1> {

    public InventoryPackets(Protocol1_12To1_11_1 protocol) {
        super(protocol);
    }

    @Override
    public void registerPackets() {
        registerSetSlot(ClientboundPackets1_9_3.SET_SLOT, Type.ITEM);
        registerWindowItems(ClientboundPackets1_9_3.WINDOW_ITEMS, Type.ITEM_ARRAY);
        registerEntityEquipment(ClientboundPackets1_9_3.ENTITY_EQUIPMENT, Type.ITEM);

        // Plugin message Packet -> Trading
        protocol.registerClientbound(ClientboundPackets1_9_3.PLUGIN_MESSAGE, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.STRING); // 0 - Channel

                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        if (wrapper.get(Type.STRING, 0).equalsIgnoreCase("MC|TrList")) {
                            wrapper.passthrough(Type.INT); // Passthrough Window ID

                            int size = wrapper.passthrough(Type.UNSIGNED_BYTE);
                            for (int i = 0; i < size; i++) {
                                BedRewriter.toClientItem(wrapper.passthrough(Type.ITEM)); // Input Item
                                BedRewriter.toClientItem(wrapper.passthrough(Type.ITEM)); // Output Item

                                boolean secondItem = wrapper.passthrough(Type.BOOLEAN); // Has second item
                                if (secondItem) {
                                    BedRewriter.toClientItem(wrapper.passthrough(Type.ITEM)); // Second Item
                                }

                                wrapper.passthrough(Type.BOOLEAN); // Trade disabled
                                wrapper.passthrough(Type.INT); // Number of tools uses
                                wrapper.passthrough(Type.INT); // Maximum number of trade uses
                            }
                        }
                    }
                });
            }
        });


        protocol.registerServerbound(ServerboundPackets1_12.CLICK_WINDOW, new PacketRemapper() {
                    @Override
                    public void registerMap() {
                        map(Type.UNSIGNED_BYTE); // 0 - Window ID
                        map(Type.SHORT); // 1 - Slot
                        map(Type.BYTE); // 2 - Button
                        map(Type.SHORT); // 3 - Action number
                        map(Type.VAR_INT); // 4 - Mode
                        map(Type.ITEM); // 5 - Clicked Item

                        handler(new PacketHandler() {
                            @Override
                            public void handle(PacketWrapper wrapper) throws Exception {
                                Item item = wrapper.get(Type.ITEM, 0);
                                if (!Via.getConfig().is1_12QuickMoveActionFix()) {
                                    BedRewriter.toServerItem(item);
                                    return;
                                }
                                byte button = wrapper.get(Type.BYTE, 0);
                                int mode = wrapper.get(Type.VAR_INT, 0);
                                // QUICK_MOVE PATCH (Shift + (click/double click))
                                if (mode == 1 && button == 0 && item == null) {
                                    short windowId = wrapper.get(Type.UNSIGNED_BYTE, 0);
                                    short slotId = wrapper.get(Type.SHORT, 0);
                                    short actionId = wrapper.get(Type.SHORT, 1);
                                    InventoryQuickMoveProvider provider = Via.getManager().getProviders().get(InventoryQuickMoveProvider.class);
                                    boolean succeed = provider.registerQuickMoveAction(windowId, slotId, actionId, wrapper.user());
                                    if (succeed) {
                                        wrapper.cancel();
                                    }
                                    // otherwise just pass through so the server sends the PacketPlayOutTransaction packet.
                                } else {
                                    BedRewriter.toServerItem(item);
                                }
                            }
                        });
                    }
                }
        );

        // Creative Inventory Action
        registerCreativeInvAction(ServerboundPackets1_12.CREATIVE_INVENTORY_ACTION, Type.ITEM);
    }

    @Override
    public Item handleItemToServer(Item item) {
        if (item == null) return null;
        BedRewriter.toServerItem(item);

        boolean newItem = item.identifier() >= 235 && item.identifier() <= 252;
        newItem |= item.identifier() == 453;
        if (newItem) { // Replace server-side unknown items
            item.setIdentifier((short) 1);
            item.setData((short) 0);
        }
        return item;
    }
}

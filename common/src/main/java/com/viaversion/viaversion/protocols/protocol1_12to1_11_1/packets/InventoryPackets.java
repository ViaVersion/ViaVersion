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
package com.viaversion.viaversion.protocols.protocol1_12to1_11_1.packets;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_12to1_11_1.Protocol1_12To1_11_1;
import com.viaversion.viaversion.protocols.protocol1_12to1_11_1.ServerboundPackets1_12;
import com.viaversion.viaversion.protocols.protocol1_12to1_11_1.providers.InventoryQuickMoveProvider;
import com.viaversion.viaversion.protocols.protocol1_9_3to1_9_1_2.ClientboundPackets1_9_3;
import com.viaversion.viaversion.rewriter.ItemRewriter;
import org.checkerframework.checker.nullness.qual.Nullable;

public class InventoryPackets extends ItemRewriter<ClientboundPackets1_9_3, ServerboundPackets1_12, Protocol1_12To1_11_1> {

    public InventoryPackets(Protocol1_12To1_11_1 protocol) {
        super(protocol, Type.ITEM1_8, Type.ITEM1_8_SHORT_ARRAY);
    }

    @Override
    public void registerPackets() {
        registerSetSlot(ClientboundPackets1_9_3.SET_SLOT);
        registerWindowItems(ClientboundPackets1_9_3.WINDOW_ITEMS);
        registerEntityEquipment(ClientboundPackets1_9_3.ENTITY_EQUIPMENT);

        // Plugin message -> Trading
        protocol.registerClientbound(ClientboundPackets1_9_3.PLUGIN_MESSAGE, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.STRING); // 0 - Channel

                handlerSoftFail(wrapper -> {
                    if (wrapper.get(Type.STRING, 0).equals("MC|TrList")) {
                        wrapper.passthrough(Type.INT); // Passthrough Window ID

                        int size = wrapper.passthrough(Type.UNSIGNED_BYTE);
                        for (int i = 0; i < size; i++) {
                            handleItemToClient(wrapper.user(), wrapper.passthrough(Type.ITEM1_8)); // Input Item
                            handleItemToClient(wrapper.user(), wrapper.passthrough(Type.ITEM1_8)); // Output Item

                            boolean secondItem = wrapper.passthrough(Type.BOOLEAN); // Has second item
                            if (secondItem) {
                                handleItemToClient(wrapper.user(), wrapper.passthrough(Type.ITEM1_8)); // Second Item
                            }

                            wrapper.passthrough(Type.BOOLEAN); // Trade disabled
                            wrapper.passthrough(Type.INT); // Number of tools uses
                            wrapper.passthrough(Type.INT); // Maximum number of trade uses
                        }
                    }
                });
            }
        });


        protocol.registerServerbound(ServerboundPackets1_12.CLICK_WINDOW, new PacketHandlers() {
                    @Override
                    public void register() {
                        map(Type.UNSIGNED_BYTE); // 0 - Window ID
                        map(Type.SHORT); // 1 - Slot
                        map(Type.BYTE); // 2 - Button
                        map(Type.SHORT); // 3 - Action number
                        map(Type.VAR_INT); // 4 - Mode
                        map(Type.ITEM1_8); // 5 - Clicked Item

                        handler(wrapper -> {
                            Item item = wrapper.get(Type.ITEM1_8, 0);
                            if (!Via.getConfig().is1_12QuickMoveActionFix()) {
                                handleItemToServer(wrapper.user(), item);
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
                                handleItemToServer(wrapper.user(), item);
                            }
                        });
                    }
                }
        );

        registerCreativeInvAction(ServerboundPackets1_12.CREATIVE_INVENTORY_ACTION);
    }

    @Override
    public Item handleItemToServer(UserConnection connection, Item item) {
        if (item == null) return null;

        if (item.identifier() == 355) { // Bed rewrite
            item.setData((short) 0);
        }

        boolean newItem = item.identifier() >= 235 && item.identifier() <= 252;
        newItem |= item.identifier() == 453;
        if (newItem) { // Replace server-side unknown items
            item.setIdentifier(1);
            item.setData((short) 0);
        }
        return item;
    }

    @Override
    public @Nullable Item handleItemToClient(UserConnection connection, @Nullable Item item) {
        if (item == null) return null;
        if (item.identifier() == 355) { // Bed rewrite
            item.setData((short) 14);
        }
        return item;
    }
}
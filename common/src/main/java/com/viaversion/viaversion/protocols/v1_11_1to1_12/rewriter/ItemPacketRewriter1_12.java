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
package com.viaversion.viaversion.protocols.v1_11_1to1_12.rewriter;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_11_1to1_12.Protocol1_11_1To1_12;
import com.viaversion.viaversion.protocols.v1_11_1to1_12.packet.ServerboundPackets1_12;
import com.viaversion.viaversion.protocols.v1_11_1to1_12.provider.InventoryQuickMoveProvider;
import com.viaversion.viaversion.protocols.v1_9_1to1_9_3.packet.ClientboundPackets1_9_3;
import com.viaversion.viaversion.rewriter.ItemRewriter;
import org.checkerframework.checker.nullness.qual.Nullable;

public class ItemPacketRewriter1_12 extends ItemRewriter<ClientboundPackets1_9_3, ServerboundPackets1_12, Protocol1_11_1To1_12> {

    public ItemPacketRewriter1_12(Protocol1_11_1To1_12 protocol) {
        super(protocol, Types.ITEM1_8, Types.ITEM1_8_SHORT_ARRAY);
    }

    @Override
    public void registerPackets() {
        registerSetSlot(ClientboundPackets1_9_3.CONTAINER_SET_SLOT);
        registerSetContent(ClientboundPackets1_9_3.CONTAINER_SET_CONTENT);
        registerSetEquippedItem(ClientboundPackets1_9_3.SET_EQUIPPED_ITEM);
        registerCustomPayloadTradeList(ClientboundPackets1_9_3.CUSTOM_PAYLOAD);

        protocol.registerServerbound(ServerboundPackets1_12.CONTAINER_CLICK, new PacketHandlers() {
                @Override
                public void register() {
                    map(Types.BYTE); // 0 - Window ID
                    map(Types.SHORT); // 1 - Slot
                    map(Types.BYTE); // 2 - Button
                    map(Types.SHORT); // 3 - Action number
                    map(Types.VAR_INT); // 4 - Mode
                    map(Types.ITEM1_8); // 5 - Clicked Item

                    handler(wrapper -> {
                        Item item = wrapper.get(Types.ITEM1_8, 0);
                        if (!Via.getConfig().is1_12QuickMoveActionFix()) {
                            handleItemToServer(wrapper.user(), item);
                            return;
                        }
                        byte button = wrapper.get(Types.BYTE, 1);
                        int mode = wrapper.get(Types.VAR_INT, 0);
                        // QUICK_MOVE PATCH (Shift + (click/double click))
                        if (mode == 1 && button == 0 && item == null) {
                            short windowId = wrapper.get(Types.BYTE, 0);
                            short slotId = wrapper.get(Types.SHORT, 0);
                            short actionId = wrapper.get(Types.SHORT, 1);
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

        registerSetCreativeModeSlot(ServerboundPackets1_12.SET_CREATIVE_MODE_SLOT);
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

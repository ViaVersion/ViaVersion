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
package us.myles.ViaVersion.protocols.protocol1_17to1_16_4.packets;

import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.minecraft.item.Item;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.rewriters.ItemRewriter;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.protocols.protocol1_16_2to1_16_1.ClientboundPackets1_16_2;
import us.myles.ViaVersion.protocols.protocol1_16_2to1_16_1.ServerboundPackets1_16_2;
import us.myles.ViaVersion.protocols.protocol1_16to1_15_2.data.RecipeRewriter1_16;
import us.myles.ViaVersion.protocols.protocol1_17to1_16_4.Protocol1_17To1_16_4;
import us.myles.ViaVersion.protocols.protocol1_17to1_16_4.ServerboundPackets1_17;

public class InventoryPackets {

    public static void register(Protocol1_17To1_16_4 protocol) {
        ItemRewriter itemRewriter = new ItemRewriter(protocol, InventoryPackets::toClient, InventoryPackets::toServer);

        itemRewriter.registerSetCooldown(ClientboundPackets1_16_2.COOLDOWN);
        itemRewriter.registerWindowItems(ClientboundPackets1_16_2.WINDOW_ITEMS, Type.FLAT_VAR_INT_ITEM_ARRAY);
        itemRewriter.registerTradeList(ClientboundPackets1_16_2.TRADE_LIST, Type.FLAT_VAR_INT_ITEM);
        itemRewriter.registerSetSlot(ClientboundPackets1_16_2.SET_SLOT, Type.FLAT_VAR_INT_ITEM);
        itemRewriter.registerAdvancements(ClientboundPackets1_16_2.ADVANCEMENTS, Type.FLAT_VAR_INT_ITEM);
        itemRewriter.registerEntityEquipmentArray(ClientboundPackets1_16_2.ENTITY_EQUIPMENT, Type.FLAT_VAR_INT_ITEM);
        itemRewriter.registerSpawnParticle(ClientboundPackets1_16_2.SPAWN_PARTICLE, Type.FLAT_VAR_INT_ITEM, Type.DOUBLE);

        new RecipeRewriter1_16(protocol, InventoryPackets::toClient).registerDefaultHandler(ClientboundPackets1_16_2.DECLARE_RECIPES);

        itemRewriter.registerCreativeInvAction(ServerboundPackets1_17.CREATIVE_INVENTORY_ACTION, Type.FLAT_VAR_INT_ITEM);

        protocol.registerIncoming(ServerboundPackets1_17.EDIT_BOOK, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> InventoryPackets.toServer(wrapper.passthrough(Type.FLAT_VAR_INT_ITEM)));
            }
        });

        // This will cause desync issues to clients with a high latency
        protocol.registerIncoming(ServerboundPackets1_17.CLICK_WINDOW, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.UNSIGNED_BYTE); // Window Id
                map(Type.SHORT); // Slot
                map(Type.BYTE); // Button
                create(wrapper -> wrapper.write(Type.SHORT, (short) 0)); // Action id - doesn't matter, as the sent out confirmation packet will be cancelled
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
                        toServer(item);
                    }

                    wrapper.write(Type.FLAT_VAR_INT_ITEM, item);
                });
            }
        });

        protocol.registerOutgoing(ClientboundPackets1_16_2.WINDOW_CONFIRMATION, null, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> {
                    // Confirmation packets were removed - just instantly respond with a confirmation back
                    short inventoryId = wrapper.read(Type.UNSIGNED_BYTE);
                    short confirmationId = wrapper.read(Type.SHORT);
                    boolean accepted = wrapper.read(Type.BOOLEAN);
                    if (!accepted) {
                        PacketWrapper packet = wrapper.create(ServerboundPackets1_16_2.WINDOW_CONFIRMATION);
                        packet.write(Type.UNSIGNED_BYTE, inventoryId);
                        packet.write(Type.SHORT, confirmationId);
                        packet.write(Type.BYTE, (byte) 1); // Accept
                        packet.sendToServer(Protocol1_17To1_16_4.class, true, true);
                    }

                    wrapper.cancel();
                });
            }
        });
    }

    public static void toClient(Item item) {
        if (item == null) return;

        item.setIdentifier(Protocol1_17To1_16_4.MAPPINGS.getNewItemId(item.getIdentifier()));
    }

    public static void toServer(Item item) {
        if (item == null) return;

        item.setIdentifier(Protocol1_17To1_16_4.MAPPINGS.getOldItemId(item.getIdentifier()));
    }
}

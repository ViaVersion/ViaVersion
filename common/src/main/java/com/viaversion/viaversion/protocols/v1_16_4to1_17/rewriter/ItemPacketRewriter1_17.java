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
package com.viaversion.viaversion.protocols.v1_16_4to1_17.rewriter;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.IntTag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_16_1to1_16_2.packet.ClientboundPackets1_16_2;
import com.viaversion.viaversion.protocols.v1_16_1to1_16_2.packet.ServerboundPackets1_16_2;
import com.viaversion.viaversion.protocols.v1_16_4to1_17.Protocol1_16_4To1_17;
import com.viaversion.viaversion.protocols.v1_16_4to1_17.packet.ClientboundPackets1_17;
import com.viaversion.viaversion.protocols.v1_16_4to1_17.packet.ServerboundPackets1_17;
import com.viaversion.viaversion.rewriter.ItemRewriter;
import com.viaversion.viaversion.rewriter.RecipeRewriter;

public final class ItemPacketRewriter1_17 extends ItemRewriter<ClientboundPackets1_16_2, ServerboundPackets1_17, Protocol1_16_4To1_17> {

    public ItemPacketRewriter1_17(Protocol1_16_4To1_17 protocol) {
        super(protocol, Types.ITEM1_13_2, Types.ITEM1_13_2_SHORT_ARRAY);
    }

    @Override
    public void registerPackets() {
        registerCooldown(ClientboundPackets1_16_2.COOLDOWN);
        registerSetContent(ClientboundPackets1_16_2.CONTAINER_SET_CONTENT);
        registerMerchantOffers(ClientboundPackets1_16_2.MERCHANT_OFFERS);
        registerSetSlot(ClientboundPackets1_16_2.CONTAINER_SET_SLOT);
        registerAdvancements(ClientboundPackets1_16_2.UPDATE_ADVANCEMENTS);
        registerSetEquipment(ClientboundPackets1_16_2.SET_EQUIPMENT);

        new RecipeRewriter<>(protocol).register(ClientboundPackets1_16_2.UPDATE_RECIPES);

        registerSetCreativeModeSlot(ServerboundPackets1_17.SET_CREATIVE_MODE_SLOT);

        protocol.registerServerbound(ServerboundPackets1_17.EDIT_BOOK, wrapper -> handleItemToServer(wrapper.user(), wrapper.passthrough(Types.ITEM1_13_2)));

        protocol.registerServerbound(ServerboundPackets1_17.CONTAINER_CLICK, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.BYTE); // Window Id
                map(Types.SHORT); // Slot
                map(Types.BYTE); // Button
                handler(wrapper -> wrapper.write(Types.SHORT, (short) 0)); // Action id - doesn't matter, as the sent out confirmation packet will be cancelled
                map(Types.VAR_INT); // Action

                handler(wrapper -> {
                    // Affected items - throw them away!
                    int length = wrapper.read(Types.VAR_INT);
                    for (int i = 0; i < length; i++) {
                        wrapper.read(Types.SHORT); // Slot
                        wrapper.read(Types.ITEM1_13_2);
                    }

                    // 1.17 clients send the then carried item, but 1.16 expects the clicked one
                    Item item = wrapper.read(Types.ITEM1_13_2);
                    int action = wrapper.get(Types.VAR_INT, 0);
                    if (action == 5 || action == 1) {
                        // Quick craft (= dragging / mouse movement while clicking on an empty slot)
                        // OR Quick move (= shift click to move a whole stack to the other inventory)
                        // The server always expects an empty item here
                        item = null;
                    } else {
                        // Use the item sent
                        handleItemToServer(wrapper.user(), item);
                    }

                    wrapper.write(Types.ITEM1_13_2, item);
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_16_2.CONTAINER_ACK, null, wrapper -> {
            short inventoryId = wrapper.read(Types.UNSIGNED_BYTE);
            short confirmationId = wrapper.read(Types.SHORT);
            boolean accepted = wrapper.read(Types.BOOLEAN);
            if (!accepted) {
                // Use the new ping packet to replace the removed acknowledgement, extra bit for fast dismissal
                int id = (1 << 30) | (inventoryId << 16) | (confirmationId & 0xFFFF);

                PacketWrapper pingPacket = wrapper.create(ClientboundPackets1_17.PING);
                pingPacket.write(Types.INT, id);
                pingPacket.send(Protocol1_16_4To1_17.class);
            }

            wrapper.cancel();
        });

        // New pong packet
        protocol.registerServerbound(ServerboundPackets1_17.PONG, null, wrapper -> {
            int id = wrapper.read(Types.INT);
            // Check extra bit for fast dismissal
            if ((id & (1 << 30)) != 0) {
                // Decode our requested inventory acknowledgement
                byte inventoryId = (byte) ((id >> 16) & 0xFF);
                short confirmationId = (short) (id & 0xFFFF);
                PacketWrapper packet = wrapper.create(ServerboundPackets1_16_2.CONTAINER_ACK);
                packet.write(Types.BYTE, inventoryId);
                packet.write(Types.SHORT, confirmationId);
                packet.write(Types.BOOLEAN, true); // Accept
                packet.sendToServer(Protocol1_16_4To1_17.class);
            }

            wrapper.cancel();
        });
    }

    @Override
    public Item handleItemToClient(UserConnection connection, Item item) {
        if (item == null) return null;

        CompoundTag tag = item.tag();
        if (item.identifier() == 733) {
            if (tag == null) {
                item.setTag(tag = new CompoundTag());
            }
            if (tag.getNumberTag("map") == null) {
                tag.put("map", new IntTag(0));
            }
        }

        item.setIdentifier(this.protocol.getMappingData().getNewItemId(item.identifier()));
        return item;
    }

}

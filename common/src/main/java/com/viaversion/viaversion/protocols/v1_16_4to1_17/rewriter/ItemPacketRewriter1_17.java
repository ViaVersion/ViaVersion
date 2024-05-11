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
package com.viaversion.viaversion.protocols.v1_16_4to1_17.rewriter;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.IntTag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.v1_16_1to1_16_2.packet.ClientboundPackets1_16_2;
import com.viaversion.viaversion.protocols.v1_16_1to1_16_2.packet.ServerboundPackets1_16_2;
import com.viaversion.viaversion.protocols.v1_16_4to1_17.Protocol1_16_4To1_17;
import com.viaversion.viaversion.protocols.v1_16_4to1_17.packet.ClientboundPackets1_17;
import com.viaversion.viaversion.protocols.v1_16_4to1_17.packet.ServerboundPackets1_17;
import com.viaversion.viaversion.protocols.v1_16_4to1_17.storage.InventoryAcknowledgements;
import com.viaversion.viaversion.rewriter.ItemRewriter;
import com.viaversion.viaversion.rewriter.RecipeRewriter;

public final class ItemPacketRewriter1_17 extends ItemRewriter<ClientboundPackets1_16_2, ServerboundPackets1_17, Protocol1_16_4To1_17> {

    public ItemPacketRewriter1_17(Protocol1_16_4To1_17 protocol) {
        super(protocol, Type.ITEM1_13_2, Type.ITEM1_13_2_SHORT_ARRAY);
    }

    @Override
    public void registerPackets() {
        registerSetCooldown(ClientboundPackets1_16_2.COOLDOWN);
        registerWindowItems(ClientboundPackets1_16_2.CONTAINER_SET_CONTENT);
        registerTradeList(ClientboundPackets1_16_2.MERCHANT_OFFERS);
        registerSetSlot(ClientboundPackets1_16_2.CONTAINER_SET_SLOT);
        registerAdvancements(ClientboundPackets1_16_2.UPDATE_ADVANCEMENTS);
        registerEntityEquipmentArray(ClientboundPackets1_16_2.SET_EQUIPMENT);
        registerSpawnParticle(ClientboundPackets1_16_2.LEVEL_PARTICLES, Type.DOUBLE);

        new RecipeRewriter<>(protocol).register(ClientboundPackets1_16_2.UPDATE_RECIPES);

        registerCreativeInvAction(ServerboundPackets1_17.SET_CREATIVE_MODE_SLOT);

        protocol.registerServerbound(ServerboundPackets1_17.EDIT_BOOK, wrapper -> handleItemToServer(wrapper.user(), wrapper.passthrough(Type.ITEM1_13_2)));

        protocol.registerServerbound(ServerboundPackets1_17.CONTAINER_CLICK, new PacketHandlers() {
            @Override
            public void register() {
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
                        wrapper.read(Type.ITEM1_13_2);
                    }

                    // 1.17 clients send the then carried item, but 1.16 expects the clicked one
                    Item item = wrapper.read(Type.ITEM1_13_2);
                    int action = wrapper.get(Type.VAR_INT, 0);
                    if (action == 5 || action == 1) {
                        // Quick craft (= dragging / mouse movement while clicking on an empty slot)
                        // OR Quick move (= shift click to move a whole stack to the other inventory)
                        // The server always expects an empty item here
                        item = null;
                    } else {
                        // Use the item sent
                        handleItemToServer(wrapper.user(), item);
                    }

                    wrapper.write(Type.ITEM1_13_2, item);
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_16_2.CONTAINER_ACK, null, wrapper -> {
            short inventoryId = wrapper.read(Type.UNSIGNED_BYTE);
            short confirmationId = wrapper.read(Type.SHORT);
            boolean accepted = wrapper.read(Type.BOOLEAN);
            if (!accepted) {
                // Use the new ping packet to replace the removed acknowledgement, extra bit for fast dismissal
                int id = (1 << 30) | (inventoryId << 16) | (confirmationId & 0xFFFF);
                wrapper.user().get(InventoryAcknowledgements.class).addId(id);

                PacketWrapper pingPacket = wrapper.create(ClientboundPackets1_17.PING);
                pingPacket.write(Type.INT, id);
                pingPacket.send(Protocol1_16_4To1_17.class);
            }

            wrapper.cancel();
        });

        // New pong packet
        protocol.registerServerbound(ServerboundPackets1_17.PONG, null, wrapper -> {
            int id = wrapper.read(Type.INT);
            // Check extra bit for fast dismissal
            if ((id & (1 << 30)) != 0 && wrapper.user().get(InventoryAcknowledgements.class).removeId(id)) {
                // Decode our requested inventory acknowledgement
                short inventoryId = (short) ((id >> 16) & 0xFF);
                short confirmationId = (short) (id & 0xFFFF);
                PacketWrapper packet = wrapper.create(ServerboundPackets1_16_2.CONTAINER_ACK);
                packet.write(Type.UNSIGNED_BYTE, inventoryId);
                packet.write(Type.SHORT, confirmationId);
                packet.write(Type.BOOLEAN, true); // Accept
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

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
package com.viaversion.viaversion.protocols.v1_8to1_9.rewriter;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_8.packet.ClientboundPackets1_8;
import com.viaversion.viaversion.protocols.v1_8to1_9.rewriter.ItemRewriter;
import com.viaversion.viaversion.protocols.v1_8to1_9.Protocol1_8To1_9;
import com.viaversion.viaversion.protocols.v1_8to1_9.packet.ClientboundPackets1_9;
import com.viaversion.viaversion.protocols.v1_8to1_9.packet.ServerboundPackets1_9;
import com.viaversion.viaversion.protocols.v1_8to1_9.storage.EntityTracker1_9;
import com.viaversion.viaversion.protocols.v1_8to1_9.storage.InventoryTracker;

public class ItemPacketRewriter1_9 {

    public static void register(Protocol1_8To1_9 protocol) {
        protocol.registerClientbound(ClientboundPackets1_8.CONTAINER_SET_DATA, new PacketHandlers() {

            @Override
            public void register() {
                map(Types.UNSIGNED_BYTE); // 0 - Window ID
                map(Types.SHORT); // 1 - Property Key
                map(Types.SHORT); // 2 - Property Value

                handler(wrapper -> {
                    final short windowId = wrapper.get(Types.UNSIGNED_BYTE, 0);
                    final short property = wrapper.get(Types.SHORT, 0);
                    short value = wrapper.get(Types.SHORT, 1);
                    InventoryTracker inventoryTracker = wrapper.user().get(InventoryTracker.class);
                    if (inventoryTracker.getInventory() != null && inventoryTracker.getInventory().equalsIgnoreCase("minecraft:enchanting_table")) {
                        if (property > 3 && property < 7) {
                            // Send 2 properties, splitting it into enchantID & level
                            final short level = (short) (value >> 8);
                            final short enchantID = (short) (value & 0xFF);
                            wrapper.create(wrapper.getId(), propertyPacket -> {
                                propertyPacket.write(Types.UNSIGNED_BYTE, windowId);
                                propertyPacket.write(Types.SHORT, property);
                                propertyPacket.write(Types.SHORT, enchantID);
                            }).scheduleSend(Protocol1_8To1_9.class);

                            wrapper.set(Types.SHORT, 0, (short) (property + 3));
                            wrapper.set(Types.SHORT, 1, level);
                        }
                    }
                });
            }
        });
        protocol.registerClientbound(ClientboundPackets1_8.OPEN_SCREEN, new PacketHandlers() {

            @Override
            public void register() {
                map(Types.UNSIGNED_BYTE); // 0 - Window ID
                map(Types.STRING); // 1 - Window Type
                map(Types.STRING, Protocol1_8To1_9.STRING_TO_JSON); // 2 - Window Title
                map(Types.UNSIGNED_BYTE); // 3 - Slot Count
                // There is a horse parameter after this, we don't handle it and let it passthrough
                // Inventory tracking
                handler(wrapper -> {
                    String inventory = wrapper.get(Types.STRING, 0);
                    InventoryTracker inventoryTracker = wrapper.user().get(InventoryTracker.class);
                    inventoryTracker.setInventory(inventory);
                });
                // Brewing patch
                handler(wrapper -> {
                    String inventory = wrapper.get(Types.STRING, 0);
                    if (inventory.equals("minecraft:brewing_stand")) {
                        wrapper.set(Types.UNSIGNED_BYTE, 1, (short) (wrapper.get(Types.UNSIGNED_BYTE, 1) + 1));
                    }
                });
            }
        });
        protocol.registerClientbound(ClientboundPackets1_8.CONTAINER_SET_SLOT, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.UNSIGNED_BYTE); // 0 - Window ID
                map(Types.SHORT); // 1 - Slot ID
                map(Types.ITEM1_8); // 2 - Slot Value
                handler(wrapper -> {
                    Item stack = wrapper.get(Types.ITEM1_8, 0);

                    boolean showShieldWhenSwordInHand = Via.getConfig().isShowShieldWhenSwordInHand()
                            && Via.getConfig().isShieldBlocking();

                    // Check if it is the inventory of the player
                    if (showShieldWhenSwordInHand) {
                        InventoryTracker inventoryTracker = wrapper.user().get(InventoryTracker.class);
                        EntityTracker1_9 entityTracker = wrapper.user().getEntityTracker(Protocol1_8To1_9.class);

                        short slotID = wrapper.get(Types.SHORT, 0);
                        byte windowId = wrapper.get(Types.UNSIGNED_BYTE, 0).byteValue();

                        // Store item in slot
                        inventoryTracker.setItemId(windowId, slotID, stack == null ? 0 : stack.identifier());

                        // Sync shield item in offhand with main hand
                        entityTracker.syncShieldWithSword();
                    }

                    ItemRewriter.toClient(stack);
                });
                // Brewing patch
                handler(wrapper -> {
                    InventoryTracker inventoryTracker = wrapper.user().get(InventoryTracker.class);

                    short slotID = wrapper.get(Types.SHORT, 0);
                    if (inventoryTracker.getInventory() != null && inventoryTracker.getInventory().equals("minecraft:brewing_stand")) {
                        if (slotID >= 4) {
                            wrapper.set(Types.SHORT, 0, (short) (slotID + 1));
                        }
                    }
                });
            }
        });
        protocol.registerClientbound(ClientboundPackets1_8.CONTAINER_SET_CONTENT, new PacketHandlers() {

            @Override
            public void register() {
                map(Types.UNSIGNED_BYTE); // 0 - Window ID
                map(Types.ITEM1_8_SHORT_ARRAY); // 1 - Window Values

                handler(wrapper -> {
                    Item[] stacks = wrapper.get(Types.ITEM1_8_SHORT_ARRAY, 0);
                    Short windowId = wrapper.get(Types.UNSIGNED_BYTE, 0);

                    InventoryTracker inventoryTracker = wrapper.user().get(InventoryTracker.class);
                    EntityTracker1_9 entityTracker = wrapper.user().getEntityTracker(Protocol1_8To1_9.class);

                    boolean showShieldWhenSwordInHand = Via.getConfig().isShowShieldWhenSwordInHand()
                            && Via.getConfig().isShieldBlocking();

                    for (short i = 0; i < stacks.length; i++) {
                        Item stack = stacks[i];

                        // Store items in slots
                        if (showShieldWhenSwordInHand) {
                            inventoryTracker.setItemId(windowId, i, stack == null ? 0 : stack.identifier());
                        }

                        ItemRewriter.toClient(stack);
                    }

                    // Sync shield item in offhand with main hand
                    if (showShieldWhenSwordInHand) {
                        entityTracker.syncShieldWithSword();
                    }
                });
                // Brewing Patch
                handler(wrapper -> {
                    InventoryTracker inventoryTracker = wrapper.user().get(InventoryTracker.class);
                    if (inventoryTracker.getInventory() != null && inventoryTracker.getInventory().equals("minecraft:brewing_stand")) {
                        Item[] oldStack = wrapper.get(Types.ITEM1_8_SHORT_ARRAY, 0);
                        Item[] newStack = new Item[oldStack.length + 1];
                        for (int i = 0; i < newStack.length; i++) {
                            if (i > 4) {
                                newStack[i] = oldStack[i - 1];
                            } else {
                                if (i != 4) { // Leave index 3 blank
                                    newStack[i] = oldStack[i];
                                }
                            }
                        }
                        wrapper.set(Types.ITEM1_8_SHORT_ARRAY, 0, newStack);
                    }
                });
            }
        });
        protocol.registerClientbound(ClientboundPackets1_8.CONTAINER_CLOSE, new PacketHandlers() {

            @Override
            public void register() {
                map(Types.UNSIGNED_BYTE); // 0 - Window ID
                // Inventory tracking
                handler(wrapper -> {
                    InventoryTracker inventoryTracker = wrapper.user().get(InventoryTracker.class);
                    inventoryTracker.setInventory(null);
                    inventoryTracker.resetInventory(wrapper.get(Types.UNSIGNED_BYTE, 0));
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_8.MAP_ITEM_DATA, new PacketHandlers() {

            @Override
            public void register() {
                map(Types.VAR_INT); // 0 - Map ID
                map(Types.BYTE); // 1 - Map Scale
                handler(wrapper -> {
                    wrapper.write(Types.BOOLEAN, true); // 2 - Show marker
                });
                // Everything else is passed through
            }
        });


        /* Incoming Packets */
        protocol.registerServerbound(ServerboundPackets1_9.SET_CREATIVE_MODE_SLOT, new PacketHandlers() {

            @Override
            public void register() {
                map(Types.SHORT); // 0 - Slot ID
                map(Types.ITEM1_8); // 1 - Item
                handler(wrapper -> {
                    Item stack = wrapper.get(Types.ITEM1_8, 0);

                    boolean showShieldWhenSwordInHand = Via.getConfig().isShowShieldWhenSwordInHand()
                            && Via.getConfig().isShieldBlocking();

                    if (showShieldWhenSwordInHand) {
                        InventoryTracker inventoryTracker = wrapper.user().get(InventoryTracker.class);
                        EntityTracker1_9 entityTracker = wrapper.user().getEntityTracker(Protocol1_8To1_9.class);
                        short slotID = wrapper.get(Types.SHORT, 0);

                        // Update item in slot
                        inventoryTracker.setItemId((short) 0, slotID, stack == null ? 0 : stack.identifier());

                        // Sync shield item in offhand with main hand
                        entityTracker.syncShieldWithSword();
                    }

                    ItemRewriter.toServer(stack);
                });
                // Elytra throw patch
                handler(wrapper -> {
                    final short slot = wrapper.get(Types.SHORT, 0);
                    boolean throwItem = (slot == 45);
                    if (throwItem) {
                        // Send a packet wiping the slot
                        wrapper.create(ClientboundPackets1_9.CONTAINER_SET_SLOT, w -> {
                            w.write(Types.UNSIGNED_BYTE, (short) 0);
                            w.write(Types.SHORT, slot);
                            w.write(Types.ITEM1_8, null);
                        }).send(Protocol1_8To1_9.class);
                        // Finally reset to simulate throwing item
                        wrapper.set(Types.SHORT, 0, (short) -999); // Set slot to -999
                    }
                });
            }
        });

        protocol.registerServerbound(ServerboundPackets1_9.CONTAINER_CLICK, new PacketHandlers() {

            @Override
            public void register() {
                map(Types.UNSIGNED_BYTE); // 0 - Window ID
                map(Types.SHORT); // 1 - Slot ID
                map(Types.BYTE); // 2 - Button
                map(Types.SHORT); // 3 - Action
                map(Types.VAR_INT, Types.BYTE); // 4 - Mode
                map(Types.ITEM1_8); // 5 - Clicked Item
                handler(wrapper -> {
                    Item stack = wrapper.get(Types.ITEM1_8, 0);

                    if (Via.getConfig().isShowShieldWhenSwordInHand()) {
                        Short windowId = wrapper.get(Types.UNSIGNED_BYTE, 0);
                        byte mode = wrapper.get(Types.BYTE, 1);
                        short hoverSlot = wrapper.get(Types.SHORT, 0);
                        byte button = wrapper.get(Types.BYTE, 0);

                        // Move items in inventory to track the sword location
                        InventoryTracker inventoryTracker = wrapper.user().get(InventoryTracker.class);
                        inventoryTracker.handleWindowClick(wrapper.user(), windowId, mode, hoverSlot, button);
                    }

                    ItemRewriter.toServer(stack);
                });
                // Brewing patch and elytra throw patch
                handler(wrapper -> {
                    final short windowID = wrapper.get(Types.UNSIGNED_BYTE, 0);
                    final short slot = wrapper.get(Types.SHORT, 0);
                    boolean throwItem = (slot == 45 && windowID == 0);
                    InventoryTracker inventoryTracker = wrapper.user().get(InventoryTracker.class);
                    if (inventoryTracker.getInventory() != null && inventoryTracker.getInventory().equals("minecraft:brewing_stand")) {
                        if (slot == 4) {
                            throwItem = true;
                        }
                        if (slot > 4) {
                            wrapper.set(Types.SHORT, 0, (short) (slot - 1));
                        }
                    }

                    if (throwItem) {
                        // Send a packet wiping the slot
                        wrapper.create(ClientboundPackets1_9.CONTAINER_SET_SLOT, w -> {
                            w.write(Types.UNSIGNED_BYTE, windowID);
                            w.write(Types.SHORT, slot);
                            w.write(Types.ITEM1_8, null);
                        }).scheduleSend(Protocol1_8To1_9.class);
                        // Finally reset to simulate throwing item
                        wrapper.set(Types.BYTE, 0, (byte) 0); // Set button to 0
                        wrapper.set(Types.BYTE, 1, (byte) 0); // Set mode to 0
                        wrapper.set(Types.SHORT, 0, (short) -999); // Set slot to -999
                    }
                });
            }
        });

        protocol.registerServerbound(ServerboundPackets1_9.CONTAINER_CLOSE, new

                PacketHandlers() {

                    @Override
                    public void register() {
                        map(Types.UNSIGNED_BYTE); // 0 - Window ID

                        // Inventory tracking
                        handler(wrapper -> {
                            InventoryTracker inventoryTracker = wrapper.user().get(InventoryTracker.class);
                            inventoryTracker.setInventory(null);
                            inventoryTracker.resetInventory(wrapper.get(Types.UNSIGNED_BYTE, 0));
                        });
                    }
                });

        protocol.registerServerbound(ServerboundPackets1_9.SET_CARRIED_ITEM, new

                PacketHandlers() {
                    @Override
                    public void register() {
                        map(Types.SHORT); // 0 - Slot id

                        // Blocking patch
                        handler(wrapper -> {
                            boolean showShieldWhenSwordInHand = Via.getConfig().isShowShieldWhenSwordInHand()
                                    && Via.getConfig().isShieldBlocking();

                            EntityTracker1_9 entityTracker = wrapper.user().getEntityTracker(Protocol1_8To1_9.class);
                            if (entityTracker.isBlocking()) {
                                entityTracker.setBlocking(false);

                                if (!showShieldWhenSwordInHand) {
                                    entityTracker.setSecondHand(null);
                                }
                            }

                            if (showShieldWhenSwordInHand) {
                                // Update current held item slot index
                                entityTracker.setHeldItemSlot(wrapper.get(Types.SHORT, 0));

                                // Sync shield item in offhand with main hand
                                entityTracker.syncShieldWithSword();
                            }
                        });
                    }
                });
    }
}

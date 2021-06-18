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
package com.viaversion.viaversion.protocols.protocol1_9to1_8.packets;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.protocol.remapper.PacketRemapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_8.ClientboundPackets1_8;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.ItemRewriter;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.Protocol1_9To1_8;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.ServerboundPackets1_9;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.storage.EntityTracker1_9;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.storage.InventoryTracker;

public class InventoryPackets {

    public static void register(Protocol protocol) {
        protocol.registerClientbound(ClientboundPackets1_8.WINDOW_PROPERTY, new PacketRemapper() {

            @Override
            public void registerMap() {
                map(Type.UNSIGNED_BYTE); // 0 - Window ID
                map(Type.SHORT); // 1 - Property Key
                map(Type.SHORT); // 2 - Property Value

                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        final short windowId = wrapper.get(Type.UNSIGNED_BYTE, 0);
                        final short property = wrapper.get(Type.SHORT, 0);
                        short value = wrapper.get(Type.SHORT, 1);
                        InventoryTracker inventoryTracker = wrapper.user().get(InventoryTracker.class);
                        if (inventoryTracker.getInventory() != null) {
                            if (inventoryTracker.getInventory().equalsIgnoreCase("minecraft:enchanting_table")) {
                                if (property > 3 && property < 7) {
                                    // Send 2 properties, splitting it into enchantID & level
                                    final short level = (short) (value >> 8);
                                    final short enchantID = (short) (value & 0xFF);
                                    wrapper.create(wrapper.getId(), new PacketHandler() {
                                        @Override
                                        public void handle(PacketWrapper wrapper) throws Exception {
                                            wrapper.write(Type.UNSIGNED_BYTE, windowId);
                                            wrapper.write(Type.SHORT, property);
                                            wrapper.write(Type.SHORT, enchantID);
                                        }
                                    }).scheduleSend(Protocol1_9To1_8.class);

                                    wrapper.set(Type.SHORT, 0, (short) (property + 3));
                                    wrapper.set(Type.SHORT, 1, level);
                                }
                            }
                        }
                    }
                });
            }
        });
        protocol.registerClientbound(ClientboundPackets1_8.OPEN_WINDOW, new PacketRemapper() {

            @Override
            public void registerMap() {
                map(Type.UNSIGNED_BYTE); // 0 - Window ID
                map(Type.STRING); // 1 - Window Type
                map(Type.STRING, Protocol1_9To1_8.FIX_JSON); // 2 - Window Title
                map(Type.UNSIGNED_BYTE); // 3 - Slot Count
                // There is a horse parameter after this, we don't handle it and let it passthrough
                // Inventory tracking
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        String inventory = wrapper.get(Type.STRING, 0);
                        InventoryTracker inventoryTracker = wrapper.user().get(InventoryTracker.class);
                        inventoryTracker.setInventory(inventory);
                    }
                });
                // Brewing patch
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        String inventory = wrapper.get(Type.STRING, 0);
                        if (inventory.equals("minecraft:brewing_stand")) {
                            wrapper.set(Type.UNSIGNED_BYTE, 1, (short) (wrapper.get(Type.UNSIGNED_BYTE, 1) + 1));
                        }
                    }
                });
            }
        });
        protocol.registerClientbound(ClientboundPackets1_8.SET_SLOT, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.UNSIGNED_BYTE); // 0 - Window ID
                map(Type.SHORT); // 1 - Slot ID
                map(Type.ITEM); // 2 - Slot Value
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        Item stack = wrapper.get(Type.ITEM, 0);

                        boolean showShieldWhenSwordInHand = Via.getConfig().isShowShieldWhenSwordInHand()
                                && Via.getConfig().isShieldBlocking();

                        // Check if it is the inventory of the player
                        if (showShieldWhenSwordInHand) {
                            InventoryTracker inventoryTracker = wrapper.user().get(InventoryTracker.class);
                            EntityTracker1_9 entityTracker = wrapper.user().getEntityTracker(Protocol1_9To1_8.class);

                            short slotID = wrapper.get(Type.SHORT, 0);
                            short windowId = wrapper.get(Type.UNSIGNED_BYTE, 0);

                            // Store item in slot
                            inventoryTracker.setItemId(windowId, slotID, stack == null ? 0 : stack.identifier());

                            // Sync shield item in offhand with main hand
                            entityTracker.syncShieldWithSword();
                        }

                        ItemRewriter.toClient(stack);
                    }
                });
                // Brewing patch
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        InventoryTracker inventoryTracker = wrapper.user().get(InventoryTracker.class);

                        short slotID = wrapper.get(Type.SHORT, 0);
                        if (inventoryTracker.getInventory() != null) {
                            if (inventoryTracker.getInventory().equals("minecraft:brewing_stand")) {
                                if (slotID >= 4) {
                                    wrapper.set(Type.SHORT, 0, (short) (slotID + 1));
                                }
                            }
                        }
                    }
                });
            }
        });
        protocol.registerClientbound(ClientboundPackets1_8.WINDOW_ITEMS, new PacketRemapper() {

            @Override
            public void registerMap() {
                map(Type.UNSIGNED_BYTE); // 0 - Window ID
                map(Type.ITEM_ARRAY); // 1 - Window Values

                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        Item[] stacks = wrapper.get(Type.ITEM_ARRAY, 0);
                        Short windowId = wrapper.get(Type.UNSIGNED_BYTE, 0);

                        InventoryTracker inventoryTracker = wrapper.user().get(InventoryTracker.class);
                        EntityTracker1_9 entityTracker = wrapper.user().getEntityTracker(Protocol1_9To1_8.class);

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
                    }
                });
                // Brewing Patch
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        InventoryTracker inventoryTracker = wrapper.user().get(InventoryTracker.class);
                        if (inventoryTracker.getInventory() != null) {
                            if (inventoryTracker.getInventory().equals("minecraft:brewing_stand")) {
                                Item[] oldStack = wrapper.get(Type.ITEM_ARRAY, 0);
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
                                wrapper.set(Type.ITEM_ARRAY, 0, newStack);
                            }
                        }
                    }
                });
            }
        });
        protocol.registerClientbound(ClientboundPackets1_8.CLOSE_WINDOW, new PacketRemapper() {

            @Override
            public void registerMap() {
                map(Type.UNSIGNED_BYTE); // 0 - Window ID
                // Inventory tracking
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        InventoryTracker inventoryTracker = wrapper.user().get(InventoryTracker.class);
                        inventoryTracker.setInventory(null);
                        inventoryTracker.resetInventory(wrapper.get(Type.UNSIGNED_BYTE, 0));
                    }
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_8.MAP_DATA, new PacketRemapper() {

            @Override
            public void registerMap() {
                map(Type.VAR_INT); // 0 - Map ID
                map(Type.BYTE); // 1 - Map Scale
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) {
                        wrapper.write(Type.BOOLEAN, true); // 2 - Show marker
                    }
                });
                // Everything else is passed through
            }
        });


        /* Incoming Packets */
        protocol.registerServerbound(ServerboundPackets1_9.CREATIVE_INVENTORY_ACTION, new PacketRemapper() {

            @Override
            public void registerMap() {
                map(Type.SHORT); // 0 - Slot ID
                map(Type.ITEM); // 1 - Item
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        Item stack = wrapper.get(Type.ITEM, 0);

                        boolean showShieldWhenSwordInHand = Via.getConfig().isShowShieldWhenSwordInHand()
                                && Via.getConfig().isShieldBlocking();

                        if (showShieldWhenSwordInHand) {
                            InventoryTracker inventoryTracker = wrapper.user().get(InventoryTracker.class);
                            EntityTracker1_9 entityTracker = wrapper.user().getEntityTracker(Protocol1_9To1_8.class);
                            short slotID = wrapper.get(Type.SHORT, 0);

                            // Update item in slot
                            inventoryTracker.setItemId((short) 0, slotID, stack == null ? 0 : stack.identifier());

                            // Sync shield item in offhand with main hand
                            entityTracker.syncShieldWithSword();
                        }

                        ItemRewriter.toServer(stack);
                    }
                });
                // Elytra throw patch
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        final short slot = wrapper.get(Type.SHORT, 0);
                        boolean throwItem = (slot == 45);
                        if (throwItem) {
                            // Send a packet wiping the slot
                            wrapper.create(0x16, new PacketHandler() {
                                @Override
                                public void handle(PacketWrapper wrapper) throws Exception {
                                    wrapper.write(Type.BYTE, (byte) 0);
                                    wrapper.write(Type.SHORT, slot);
                                    wrapper.write(Type.ITEM, null);
                                }
                            }).send(Protocol1_9To1_8.class);
                            // Finally reset to simulate throwing item
                            wrapper.set(Type.SHORT, 0, (short) -999); // Set slot to -999
                        }
                    }
                });
            }
        });

        protocol.registerServerbound(ServerboundPackets1_9.CLICK_WINDOW, new PacketRemapper() {

            @Override
            public void registerMap() {
                map(Type.UNSIGNED_BYTE); // 0 - Window ID
                map(Type.SHORT); // 1 - Slot ID
                map(Type.BYTE); // 2 - Button
                map(Type.SHORT); // 3 - Action
                map(Type.VAR_INT, Type.BYTE); // 4 - Mode
                map(Type.ITEM); // 5 - Clicked Item
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        Item stack = wrapper.get(Type.ITEM, 0);

                        if (Via.getConfig().isShowShieldWhenSwordInHand()) {
                            Short windowId = wrapper.get(Type.UNSIGNED_BYTE, 0);
                            byte mode = wrapper.get(Type.BYTE, 1);
                            short hoverSlot = wrapper.get(Type.SHORT, 0);
                            byte button = wrapper.get(Type.BYTE, 0);

                            // Move items in inventory to track the sword location
                            InventoryTracker inventoryTracker = wrapper.user().get(InventoryTracker.class);
                            inventoryTracker.handleWindowClick(wrapper.user(), windowId, mode, hoverSlot, button);
                        }

                        ItemRewriter.toServer(stack);
                    }
                });
                // Brewing patch and elytra throw patch
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        final short windowID = wrapper.get(Type.UNSIGNED_BYTE, 0);
                        final short slot = wrapper.get(Type.SHORT, 0);
                        boolean throwItem = (slot == 45 && windowID == 0);
                        InventoryTracker inventoryTracker = wrapper.user().get(InventoryTracker.class);
                        if (inventoryTracker.getInventory() != null) {
                            if (inventoryTracker.getInventory().equals("minecraft:brewing_stand")) {
                                if (slot == 4) {
                                    throwItem = true;
                                }
                                if (slot > 4) {
                                    wrapper.set(Type.SHORT, 0, (short) (slot - 1));
                                }
                            }
                        }

                        if (throwItem) {
                            // Send a packet wiping the slot
                            wrapper.create(0x16, new PacketHandler() {
                                @Override
                                public void handle(PacketWrapper wrapper) throws Exception {
                                    wrapper.write(Type.BYTE, (byte) windowID);
                                    wrapper.write(Type.SHORT, slot);
                                    wrapper.write(Type.ITEM, null);
                                }
                            }).scheduleSend(Protocol1_9To1_8.class);
                            // Finally reset to simulate throwing item
                            wrapper.set(Type.BYTE, 0, (byte) 0); // Set button to 0
                            wrapper.set(Type.BYTE, 1, (byte) 0); // Set mode to 0
                            wrapper.set(Type.SHORT, 0, (short) -999); // Set slot to -999
                        }
                    }
                });
            }
        });

        protocol.registerServerbound(ServerboundPackets1_9.CLOSE_WINDOW, new PacketRemapper() {

            @Override
            public void registerMap() {
                map(Type.UNSIGNED_BYTE); // 0 - Window ID

                // Inventory tracking
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        InventoryTracker inventoryTracker = wrapper.user().get(InventoryTracker.class);
                        inventoryTracker.setInventory(null);
                        inventoryTracker.resetInventory(wrapper.get(Type.UNSIGNED_BYTE, 0));
                    }
                });
            }
        });

        protocol.registerServerbound(ServerboundPackets1_9.HELD_ITEM_CHANGE, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.SHORT); // 0 - Slot id

                // Blocking patch
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        boolean showShieldWhenSwordInHand = Via.getConfig().isShowShieldWhenSwordInHand()
                                && Via.getConfig().isShieldBlocking();

                        EntityTracker1_9 entityTracker = wrapper.user().getEntityTracker(Protocol1_9To1_8.class);
                        if (entityTracker.isBlocking()) {
                            entityTracker.setBlocking(false);

                            if (!showShieldWhenSwordInHand) {
                                entityTracker.setSecondHand(null);
                            }
                        }

                        if (showShieldWhenSwordInHand) {
                            // Update current held item slot index
                            entityTracker.setHeldItemSlot(wrapper.get(Type.SHORT, 0));

                            // Sync shield item in offhand with main hand
                            entityTracker.syncShieldWithSword();
                        }
                    }
                });
            }
        });
    }
}

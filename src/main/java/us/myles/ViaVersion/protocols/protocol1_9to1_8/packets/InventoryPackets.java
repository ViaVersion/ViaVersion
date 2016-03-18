package us.myles.ViaVersion.protocols.protocol1_9to1_8.packets;

import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.minecraft.item.Item;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketHandler;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.remapper.ValueCreator;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.packets.State;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.ItemRewriter;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.Protocol1_9TO1_8;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.storage.EntityTracker;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.storage.InventoryTracker;

public class InventoryPackets {
    public static void register(Protocol protocol) {
        // Window Property Packet
        protocol.registerOutgoing(State.PLAY, 0x31, 0x15, new PacketRemapper() {

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
                                    wrapper.create(wrapper.getId(), new ValueCreator() {
                                        @Override
                                        public void write(PacketWrapper wrapper) throws Exception {
                                            wrapper.write(Type.UNSIGNED_BYTE, windowId);
                                            wrapper.write(Type.SHORT, property);
                                            wrapper.write(Type.SHORT, enchantID);
                                        }
                                    }).send();

                                    wrapper.set(Type.SHORT, 0, (short) (property + 3));
                                    wrapper.set(Type.SHORT, 1, level);
                                }
                            }
                        }
                    }
                });
            }
        });
        // Window Open Packet
        protocol.registerOutgoing(State.PLAY, 0x2D, 0x13, new PacketRemapper() {

            @Override
            public void registerMap() {
                map(Type.UNSIGNED_BYTE); // 0 - Window ID
                map(Type.STRING); // 1 - Window Type
                map(Type.STRING, Protocol1_9TO1_8.FIX_JSON); // 2 - Window Title
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
        // Window Set Slot Packet
        protocol.registerOutgoing(State.PLAY, 0x2F, 0x16, new PacketRemapper() {

            @Override
            public void registerMap() {

                map(Type.BYTE); // 0 - Window ID
                map(Type.SHORT); // 1 - Slot ID
                map(Type.ITEM); // 2 - Slot Value
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        Item stack = wrapper.get(Type.ITEM, 0);
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
        // Window Set Slots Packet
        protocol.registerOutgoing(State.PLAY, 0x30, 0x14, new PacketRemapper() {

            @Override
            public void registerMap() {
                map(Type.UNSIGNED_BYTE); // 0 - Window ID
                map(Type.ITEM_ARRAY); // 1 - Window Values

                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        Item[] stacks = wrapper.get(Type.ITEM_ARRAY, 0);
                        for (Item stack : stacks)
                            ItemRewriter.toClient(stack);
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
                                    if (i > 3) {
                                        newStack[i] = oldStack[i - 1];
                                    } else {
                                        if (i != 3) { // Leave index 3 blank
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
        // Close Window Packet
        protocol.registerOutgoing(State.PLAY, 0x2E, 0x12, new PacketRemapper() {

            @Override
            public void registerMap() {
                map(Type.UNSIGNED_BYTE); // 0 - Window ID
                // Inventory tracking
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        InventoryTracker inventoryTracker = wrapper.user().get(InventoryTracker.class);
                        inventoryTracker.setInventory(null);
                    }
                });
            }
        });

        // Map Packet
        protocol.registerOutgoing(State.PLAY, 0x34, 0x24, new PacketRemapper() {

            @Override
            public void registerMap() {
                map(Type.VAR_INT); // 0 - Map ID
                map(Type.BYTE); // 1 - Map Scale
                create(new ValueCreator() {
                    @Override
                    public void write(PacketWrapper wrapper) {
                        wrapper.write(Type.BOOLEAN, true); // 2 - Show marker
                    }
                });
                // Everything else is passed through
            }
        });

        /* Packets which do not have any field remapping or handlers */
        protocol.registerOutgoing(State.PLAY, 0x09, 0x37); // Held Item Change Packet
        protocol.registerOutgoing(State.PLAY, 0x32, 0x11); // Confirm Transaction Packet

        /* Incoming Packets */

        // Creative Inventory Slot Action Packet
        protocol.registerIncoming(State.PLAY, 0x10, 0x18, new PacketRemapper() {

            @Override
            public void registerMap() {
                map(Type.SHORT); // 0 - Slot ID
                map(Type.ITEM); // 1 - Item
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        Item stack = wrapper.get(Type.ITEM, 0);
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
                            wrapper.create(0x16, new ValueCreator() {
                                @Override
                                public void write(PacketWrapper wrapper) throws Exception {
                                    wrapper.write(Type.UNSIGNED_BYTE, (short) 0);
                                    wrapper.write(Type.SHORT, slot);
                                    wrapper.write(Type.SHORT, (short) -1);
                                }
                            }).send();
                            // Finally reset to simulate throwing item
                            wrapper.set(Type.SHORT, 0, (short) -999); // Set slot to -999
                        }
                    }
                });
            }
        });

        // Player Click Window Packet
        protocol.registerIncoming(State.PLAY, 0x0E, 0x07, new PacketRemapper() {

            @Override
            public void registerMap() {
                map(Type.UNSIGNED_BYTE); // 0 - Window ID
                map(Type.SHORT); // 1 - Slot ID
                map(Type.BYTE); // 2 - Button
                map(Type.SHORT); // 3 - Action
                map(Type.BYTE); // 4 - Mode
                map(Type.ITEM); // 5 - Clicked Item
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        Item stack = wrapper.get(Type.ITEM, 0);
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
                            wrapper.create(0x16, new ValueCreator() {
                                @Override
                                public void write(PacketWrapper wrapper) throws Exception {
                                    wrapper.write(Type.UNSIGNED_BYTE, windowID);
                                    wrapper.write(Type.SHORT, slot);
                                    wrapper.write(Type.SHORT, (short) -1);
                                }
                            }).send();
                            // Finally reset to simulate throwing item
                            wrapper.set(Type.BYTE, 0, (byte) 0); // Set button to 0
                            wrapper.set(Type.BYTE, 1, (byte) 0); // Set mode to 0
                            wrapper.set(Type.SHORT, 0, (short) -999); // Set slot to -999
                        }
                    }
                });
            }
        });

        // Close Window Incoming Packet
        protocol.registerIncoming(State.PLAY, 0x0D, 0x08, new PacketRemapper() {

            @Override
            public void registerMap() {
                // Inventory tracking
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        InventoryTracker inventoryTracker = wrapper.user().get(InventoryTracker.class);
                        inventoryTracker.setInventory(null);
                    }
                });
            }
        });

        // Held Item Change Packet
        protocol.registerIncoming(State.PLAY, 0x09, 0x17, new PacketRemapper() {
            @Override
            public void registerMap() {
                // Blocking patch
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        EntityTracker entityTracker = wrapper.user().get(EntityTracker.class);
                        if (entityTracker.isBlocking()) {
                            entityTracker.setBlocking(false);
                            entityTracker.setSecondHand(null);
                        }
                    }
                });
            }
        });

        /* Packets which do not have any field remapping or handlers */

        protocol.registerIncoming(State.PLAY, 0x0F, 0x05); // Confirm Transaction Packet
        protocol.registerIncoming(State.PLAY, 0x11, 0x06); // Enchant Item Packet


    }
}

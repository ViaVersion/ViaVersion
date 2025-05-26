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
package com.viaversion.viaversion.protocols.v1_8to1_9.rewriter;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_8to1_9.Protocol1_8To1_9;
import com.viaversion.viaversion.protocols.v1_8to1_9.data.EntityIds1_8;
import com.viaversion.viaversion.protocols.v1_8to1_9.data.PotionIdMappings1_9;
import com.viaversion.viaversion.protocols.v1_8to1_9.packet.ClientboundPackets1_8;
import com.viaversion.viaversion.protocols.v1_8to1_9.packet.ClientboundPackets1_9;
import com.viaversion.viaversion.protocols.v1_8to1_9.packet.ServerboundPackets1_9;
import com.viaversion.viaversion.protocols.v1_8to1_9.storage.EntityTracker1_9;
import com.viaversion.viaversion.protocols.v1_8to1_9.storage.InventoryTracker;
import com.viaversion.viaversion.rewriter.ItemRewriter;
import com.viaversion.viaversion.util.ComponentUtil;
import com.viaversion.viaversion.util.Key;
import com.viaversion.viaversion.util.SerializerVersion;
import java.util.Collections;
import org.checkerframework.checker.nullness.qual.Nullable;

public class ItemPacketRewriter1_9 extends ItemRewriter<ClientboundPackets1_8, ServerboundPackets1_9, Protocol1_8To1_9> {

    public ItemPacketRewriter1_9(final Protocol1_8To1_9 protocol) {
        super(protocol, Types.ITEM1_8, Types.ITEM1_8_SHORT_ARRAY);
    }

    @Override
    protected void registerPackets() {
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
                map(Types.BYTE); // 0 - Window ID
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
                        byte windowId = wrapper.get(Types.BYTE, 0);

                        // Store item in slot
                        inventoryTracker.setItemId(windowId, slotID, stack == null ? 0 : stack.identifier());

                        // Sync shield item in offhand with main hand
                        entityTracker.syncShieldWithSword();
                    }

                    handleItemToClient(wrapper.user(), stack);
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
                    short windowId = wrapper.get(Types.UNSIGNED_BYTE, 0);

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

                        handleItemToClient(wrapper.user(), stack);
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
                    short windowId = wrapper.get(Types.UNSIGNED_BYTE, 0);
                    inventoryTracker.resetInventory(windowId);
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
                        inventoryTracker.setItemId(0, slotID, stack == null ? 0 : stack.identifier());

                        // Sync shield item in offhand with main hand
                        entityTracker.syncShieldWithSword();
                    }

                    handleItemToServer(wrapper.user(), stack);
                });
                // Elytra throw patch
                handler(wrapper -> {
                    final short slot = wrapper.get(Types.SHORT, 0);
                    boolean throwItem = (slot == 45);
                    if (throwItem) {
                        // Send a packet wiping the slot
                        wrapper.create(ClientboundPackets1_9.CONTAINER_SET_SLOT, w -> {
                            w.write(Types.BYTE, (byte) 0);
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
                map(Types.BYTE); // 0 - Window ID
                map(Types.SHORT); // 1 - Slot ID
                map(Types.BYTE); // 2 - Button
                map(Types.SHORT); // 3 - Action
                map(Types.VAR_INT, Types.BYTE); // 4 - Mode
                map(Types.ITEM1_8); // 5 - Clicked Item
                handler(wrapper -> {
                    Item stack = wrapper.get(Types.ITEM1_8, 0);

                    if (Via.getConfig().isShowShieldWhenSwordInHand()) {
                        byte windowId = wrapper.get(Types.BYTE, 0);
                        byte mode = wrapper.get(Types.BYTE, 2);
                        short hoverSlot = wrapper.get(Types.SHORT, 0);
                        byte button = wrapper.get(Types.BYTE, 1);

                        // Move items in inventory to track the sword location
                        InventoryTracker inventoryTracker = wrapper.user().get(InventoryTracker.class);
                        inventoryTracker.handleWindowClick(wrapper.user(), windowId, mode, hoverSlot, button);
                    }

                    handleItemToServer(wrapper.user(), stack);
                });
                // Brewing patch and elytra throw patch
                handler(wrapper -> {
                    final byte windowID = wrapper.get(Types.BYTE, 0);
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
                            w.write(Types.BYTE, windowID);
                            w.write(Types.SHORT, slot);
                            w.write(Types.ITEM1_8, null);
                        }).scheduleSend(Protocol1_8To1_9.class);
                        // Finally reset to simulate throwing item
                        wrapper.set(Types.BYTE, 1, (byte) 0); // Set button to 0
                        wrapper.set(Types.BYTE, 2, (byte) 0); // Set mode to 0
                        wrapper.set(Types.SHORT, 0, (short) -999); // Set slot to -999
                    }
                });
            }
        });

        protocol.registerServerbound(ServerboundPackets1_9.CONTAINER_CLOSE, new

            PacketHandlers() {

                @Override
                public void register() {
                    map(Types.BYTE); // 0 - Window ID

                    // Inventory tracking
                    handler(wrapper -> {
                        InventoryTracker inventoryTracker = wrapper.user().get(InventoryTracker.class);
                        inventoryTracker.setInventory(null);
                        inventoryTracker.resetInventory(wrapper.get(Types.BYTE, 0));
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

    @Override
    public @Nullable Item handleItemToClient(final UserConnection connection, @Nullable final Item item) {
        if (item == null) return null;
        if (item.identifier() == 383 && item.data() != 0) { // Monster Egg
            CompoundTag tag = item.tag();
            if (tag == null) {
                tag = new CompoundTag();
            }
            CompoundTag entityTag = new CompoundTag();
            String entityName = EntityIds1_8.ENTITY_ID_TO_NAME.get((int) item.data());
            if (entityName != null) {
                StringTag id = new StringTag(entityName);
                entityTag.put("id", id);
                tag.put("EntityTag", entityTag);
            }
            item.setTag(tag);
            item.setData((short) 0);
        }
        if (item.identifier() == 373) { // Potion
            CompoundTag tag = item.tag();
            if (tag == null) {
                tag = new CompoundTag();
            }
            if (item.data() >= 16384) {
                item.setIdentifier(438); // splash id
                item.setData((short) (item.data() - 8192));
            }
            String name = PotionIdMappings1_9.potionNameFromDamage(item.data());
            StringTag potion = new StringTag(Key.namespaced(name));
            tag.put("Potion", potion);
            item.setTag(tag);
            item.setData((short) 0);
        }
        if (item.identifier() == 387) { // WRITTEN_BOOK
            CompoundTag tag = item.tag();
            if (tag == null) {
                tag = new CompoundTag();
            }

            ListTag<StringTag> pages = tag.getListTag("pages", StringTag.class);
            tag.put(nbtTagName("pages"), pages == null ? new ListTag<>(StringTag.class) : pages.copy());

            if (pages == null) {
                pages = new ListTag<>(Collections.singletonList(new StringTag(ComponentUtil.emptyJsonComponent().toString())));
                tag.put("pages", pages);
            } else {
                for (int i = 0; i < pages.size(); i++) {
                    final StringTag page = pages.get(i);
                    try {
                        page.setValue(ComponentUtil.convertJsonOrEmpty(page.getValue(), SerializerVersion.V1_8, SerializerVersion.V1_9).toString());
                    } catch (final Exception e) {
                        // Don't fail on invalid JSON as per Vanilla behaviour
                    }
                }
            }
            item.setTag(tag);
        }
        return item;
    }

    @Override
    public @Nullable Item handleItemToServer(final UserConnection connection, @Nullable final Item item) {
        if (item == null) return null;
        if (item.identifier() == 383 && item.data() == 0) { // Monster Egg
            CompoundTag tag = item.tag();
            int data = 0;
            if (tag != null && tag.getCompoundTag("EntityTag") != null) {
                CompoundTag entityTag = tag.getCompoundTag("EntityTag");
                StringTag id = entityTag.getStringTag("id");
                if (id != null) {
                    if (EntityIds1_8.ENTITY_NAME_TO_ID.containsKey(id.getValue())) {
                        data = EntityIds1_8.ENTITY_NAME_TO_ID.get(id.getValue());
                    }
                }
                tag.remove("EntityTag");
            }
            item.setTag(tag);
            item.setData((short) data);
        }
        if (item.identifier() == 373) { // Potion
            CompoundTag tag = item.tag();
            int data = 0;
            if (tag != null && tag.getStringTag("Potion") != null) {
                StringTag potion = tag.getStringTag("Potion");
                String potionName = Key.stripMinecraftNamespace(potion.getValue());
                if (PotionIdMappings1_9.POTION_NAME_TO_ID.containsKey(potionName)) {
                    data = PotionIdMappings1_9.POTION_NAME_TO_ID.get(potionName);
                }
                tag.remove("Potion");
            }
            item.setTag(tag);
            item.setData((short) data);
        }
        // Splash potion
        if (item.identifier() == 438) {
            CompoundTag tag = item.tag();
            int data = 0;
            item.setIdentifier(373); // Potion
            if (tag != null && tag.getStringTag("Potion") != null) {
                StringTag potion = tag.getStringTag("Potion");
                String potionName = Key.stripMinecraftNamespace(potion.getValue());
                if (PotionIdMappings1_9.POTION_NAME_TO_ID.containsKey(potionName)) {
                    data = PotionIdMappings1_9.POTION_NAME_TO_ID.get(potionName) + 8192;
                }
                tag.remove("Potion");
            }
            item.setTag(tag);
            item.setData((short) data);
        }
        if (item.identifier() == 387) { // WRITTEN_BOOK
            CompoundTag tag = item.tag();
            if (tag != null) {
                // Prefer saved pages since they are more likely to be accurate
                ListTag<StringTag> backup = tag.removeUnchecked(nbtTagName("pages"));
                if (backup != null) {
                    if (!backup.isEmpty()) {
                        tag.put("pages", backup);
                    } else {
                        tag.remove("pages");
                        if (tag.isEmpty()) {
                            item.setTag(null);
                        }
                    }
                } else {
                    // Fallback to normal pages tag
                    ListTag<StringTag> pages = tag.getListTag("pages", StringTag.class);
                    if (pages != null) {
                        for (int i = 0; i < pages.size(); i++) {
                            final StringTag page = pages.get(i);
                            page.setValue(ComponentUtil.convertJsonOrEmpty(page.getValue(), SerializerVersion.V1_9, SerializerVersion.V1_8).toString());
                        }
                    }
                }
            }
        }

        boolean newItem = item.identifier() >= 198 && item.identifier() <= 212;
        newItem |= item.identifier() == 397 && item.data() == 5;
        newItem |= item.identifier() >= 432 && item.identifier() <= 448;
        if (newItem) { // Replace server-side unknown items
            item.setIdentifier(1);
            item.setData((short) 0);
        }
        return item;
    }
}

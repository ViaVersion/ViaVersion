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
package com.viaversion.viaversion.protocols.protocol1_16to1_15_2.packets;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.IntArrayTag;
import com.github.steveice10.opennbt.tag.builtin.ListTag;
import com.github.steveice10.opennbt.tag.builtin.LongTag;
import com.github.steveice10.opennbt.tag.builtin.NumberTag;
import com.github.steveice10.opennbt.tag.builtin.StringTag;
import com.github.steveice10.opennbt.tag.builtin.Tag;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.remapper.PacketRemapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.UUIDIntArrayType;
import com.viaversion.viaversion.protocols.protocol1_14to1_13_2.data.RecipeRewriter1_14;
import com.viaversion.viaversion.protocols.protocol1_15to1_14_4.ClientboundPackets1_15;
import com.viaversion.viaversion.protocols.protocol1_16to1_15_2.Protocol1_16To1_15_2;
import com.viaversion.viaversion.protocols.protocol1_16to1_15_2.ServerboundPackets1_16;
import com.viaversion.viaversion.protocols.protocol1_16to1_15_2.storage.InventoryTracker1_16;
import com.viaversion.viaversion.rewriter.ItemRewriter;

import java.util.UUID;

public class InventoryPackets extends ItemRewriter<Protocol1_16To1_15_2> {

    public InventoryPackets(Protocol1_16To1_15_2 protocol) {
        super(protocol);
    }

    @Override
    public void registerPackets() {
        protocol.registerClientbound(ClientboundPackets1_15.OPEN_WINDOW, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // Window Id
                map(Type.VAR_INT); // Window Type
                map(Type.COMPONENT); // Window Title

                handler(wrapper -> {
                    InventoryTracker1_16 inventoryTracker = wrapper.user().get(InventoryTracker1_16.class);
                    int windowId = wrapper.get(Type.VAR_INT, 0);
                    int windowType = wrapper.get(Type.VAR_INT, 1);
                    if (windowType >= 20) { // smithing added with id 20
                        wrapper.set(Type.VAR_INT, 1, ++windowType);
                    }
                    inventoryTracker.setInventory((short) windowId);
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_15.CLOSE_WINDOW, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.UNSIGNED_BYTE);

                handler(wrapper -> {
                    InventoryTracker1_16 inventoryTracker = wrapper.user().get(InventoryTracker1_16.class);
                    inventoryTracker.setInventory((short) -1);
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_15.WINDOW_PROPERTY, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.UNSIGNED_BYTE); // Window Id
                map(Type.SHORT); // Property
                map(Type.SHORT); // Value

                handler(wrapper -> {
                    short property = wrapper.get(Type.SHORT, 0);
                    if (property >= 4 && property <= 6) { // Enchantment id
                        short enchantmentId = wrapper.get(Type.SHORT, 1);
                        if (enchantmentId >= 11) { // soul_speed added with id 11
                            wrapper.set(Type.SHORT, 1, ++enchantmentId);
                        }
                    }
                });
            }
        });

        registerSetCooldown(ClientboundPackets1_15.COOLDOWN);
        registerWindowItems(ClientboundPackets1_15.WINDOW_ITEMS, Type.FLAT_VAR_INT_ITEM_ARRAY);
        registerTradeList(ClientboundPackets1_15.TRADE_LIST, Type.FLAT_VAR_INT_ITEM);
        registerSetSlot(ClientboundPackets1_15.SET_SLOT, Type.FLAT_VAR_INT_ITEM);
        registerAdvancements(ClientboundPackets1_15.ADVANCEMENTS, Type.FLAT_VAR_INT_ITEM);

        protocol.registerClientbound(ClientboundPackets1_15.ENTITY_EQUIPMENT, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // 0 - Entity ID

                handler(wrapper -> {
                    int slot = wrapper.read(Type.VAR_INT);
                    wrapper.write(Type.BYTE, (byte) slot);
                    handleItemToClient(wrapper.passthrough(Type.FLAT_VAR_INT_ITEM));
                });
            }
        });

        new RecipeRewriter1_14(protocol).registerDefaultHandler(ClientboundPackets1_15.DECLARE_RECIPES);

        registerClickWindow(ServerboundPackets1_16.CLICK_WINDOW, Type.FLAT_VAR_INT_ITEM);
        registerCreativeInvAction(ServerboundPackets1_16.CREATIVE_INVENTORY_ACTION, Type.FLAT_VAR_INT_ITEM);

        protocol.registerServerbound(ServerboundPackets1_16.CLOSE_WINDOW, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.UNSIGNED_BYTE);

                handler(wrapper -> {
                    InventoryTracker1_16 inventoryTracker = wrapper.user().get(InventoryTracker1_16.class);
                    inventoryTracker.setInventory((short) -1);
                });
            }
        });

        protocol.registerServerbound(ServerboundPackets1_16.EDIT_BOOK, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> handleItemToServer(wrapper.passthrough(Type.FLAT_VAR_INT_ITEM)));
            }
        });

        registerSpawnParticle(ClientboundPackets1_15.SPAWN_PARTICLE, Type.FLAT_VAR_INT_ITEM, Type.DOUBLE);
    }

    @Override
    public Item handleItemToClient(Item item) {
        if (item == null) return null;

        if (item.identifier() == 771 && item.tag() != null) {
            CompoundTag tag = item.tag();
            Tag ownerTag = tag.get("SkullOwner");
            if (ownerTag instanceof CompoundTag) {
                CompoundTag ownerCompundTag = (CompoundTag) ownerTag;
                Tag idTag = ownerCompundTag.get("Id");
                if (idTag instanceof StringTag) {
                    UUID id = UUID.fromString((String) idTag.getValue());
                    ownerCompundTag.put("Id", new IntArrayTag(UUIDIntArrayType.uuidToIntArray(id)));
                }
            }
        }

        oldToNewAttributes(item);
        item.setIdentifier(Protocol1_16To1_15_2.MAPPINGS.getNewItemId(item.identifier()));
        return item;
    }

    @Override
    public Item handleItemToServer(Item item) {
        if (item == null) return null;

        item.setIdentifier(Protocol1_16To1_15_2.MAPPINGS.getOldItemId(item.identifier()));

        if (item.identifier() == 771 && item.tag() != null) {
            CompoundTag tag = item.tag();
            Tag ownerTag = tag.get("SkullOwner");
            if (ownerTag instanceof CompoundTag) {
                CompoundTag ownerCompundTag = (CompoundTag) ownerTag;
                Tag idTag = ownerCompundTag.get("Id");
                if (idTag instanceof IntArrayTag) {
                    UUID id = UUIDIntArrayType.uuidFromIntArray((int[]) idTag.getValue());
                    ownerCompundTag.put("Id", new StringTag(id.toString()));
                }
            }
        }

        newToOldAttributes(item);
        return item;
    }

    public static void oldToNewAttributes(Item item) {
        if (item.tag() == null) return;

        ListTag attributes = item.tag().get("AttributeModifiers");
        if (attributes == null) return;

        for (Tag tag : attributes) {
            CompoundTag attribute = (CompoundTag) tag;
            rewriteAttributeName(attribute, "AttributeName", false);
            rewriteAttributeName(attribute, "Name", false);
            Tag leastTag = attribute.get("UUIDLeast");
            if (leastTag != null) {
                Tag mostTag = attribute.get("UUIDMost");
                int[] uuidIntArray = UUIDIntArrayType.bitsToIntArray(((NumberTag) leastTag).asLong(), ((NumberTag) mostTag).asLong());
                attribute.put("UUID", new IntArrayTag(uuidIntArray));
            }
        }
    }

    public static void newToOldAttributes(Item item) {
        if (item.tag() == null) return;

        ListTag attributes = item.tag().get("AttributeModifiers");
        if (attributes == null) return;

        for (Tag tag : attributes) {
            CompoundTag attribute = (CompoundTag) tag;
            rewriteAttributeName(attribute, "AttributeName", true);
            rewriteAttributeName(attribute, "Name", true);
            IntArrayTag uuidTag = attribute.get("UUID");
            if (uuidTag != null && uuidTag.getValue().length == 4) {
                UUID uuid = UUIDIntArrayType.uuidFromIntArray(uuidTag.getValue());
                attribute.put("UUIDLeast", new LongTag(uuid.getLeastSignificantBits()));
                attribute.put("UUIDMost", new LongTag(uuid.getMostSignificantBits()));
            }
        }
    }

    public static void rewriteAttributeName(CompoundTag compoundTag, String entryName, boolean inverse) {
        StringTag attributeNameTag = compoundTag.get(entryName);
        if (attributeNameTag == null) return;

        String attributeName = attributeNameTag.getValue();
        if (inverse && !attributeName.startsWith("minecraft:")) {
            attributeName = "minecraft:" + attributeName;
        }

        String mappedAttribute = (inverse ? Protocol1_16To1_15_2.MAPPINGS.getAttributeMappings().inverse()
                : Protocol1_16To1_15_2.MAPPINGS.getAttributeMappings()).get(attributeName);
        if (mappedAttribute == null) return;

        attributeNameTag.setValue(mappedAttribute);
    }
}

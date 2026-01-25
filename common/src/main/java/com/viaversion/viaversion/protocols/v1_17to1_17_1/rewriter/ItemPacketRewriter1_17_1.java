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
package com.viaversion.viaversion.protocols.v1_17to1_17_1.rewriter;

import com.viaversion.nbt.tag.ByteTag;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_16_4to1_17.packet.ClientboundPackets1_17;
import com.viaversion.viaversion.protocols.v1_16_4to1_17.packet.ServerboundPackets1_17;
import com.viaversion.viaversion.protocols.v1_17to1_17_1.Protocol1_17To1_17_1;
import com.viaversion.viaversion.rewriter.ItemRewriter;
import com.viaversion.viaversion.rewriter.RecipeRewriter;
import com.viaversion.viaversion.util.Key;
import com.viaversion.viaversion.util.Limit;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class ItemPacketRewriter1_17_1 extends ItemRewriter<ClientboundPackets1_17, ServerboundPackets1_17, Protocol1_17To1_17_1> {

    public ItemPacketRewriter1_17_1(Protocol1_17To1_17_1 protocol) {
        super(protocol, Types.ITEM1_13_2, Types.ITEM1_13_2_SHORT_ARRAY);
    }

    @Override
    public void registerPackets() {
        registerCooldown(ClientboundPackets1_17.COOLDOWN);
        registerMerchantOffers(ClientboundPackets1_17.MERCHANT_OFFERS);
        registerSetEquipment(ClientboundPackets1_17.SET_EQUIPMENT);
        registerAdvancements(ClientboundPackets1_17.UPDATE_ADVANCEMENTS);

        new RecipeRewriter<>(protocol).register(ClientboundPackets1_17.UPDATE_RECIPES);

        registerSetCreativeModeSlot(ServerboundPackets1_17.SET_CREATIVE_MODE_SLOT);

        protocol.registerClientbound(ClientboundPackets1_17.CONTAINER_SET_SLOT, wrapper -> {
            wrapper.passthrough(Types.BYTE); // Container id
            wrapper.write(Types.VAR_INT, 0); // Add arbitrary state id
            wrapper.passthrough(Types.SHORT); // Slot id
            passthroughClientboundItem(wrapper);
        });

        protocol.registerClientbound(ClientboundPackets1_17.CONTAINER_SET_CONTENT, wrapper -> {
            wrapper.passthrough(Types.UNSIGNED_BYTE); // Container id
            wrapper.write(Types.VAR_INT, 0); // Add arbitrary state id
            // Length encoded as var int now
            Item[] items = wrapper.passthroughAndMap(Types.ITEM1_13_2_SHORT_ARRAY, Types.ITEM1_13_2_ARRAY);
            for (int i = 0; i < items.length; i++) {
                items[i] = handleItemToClient(wrapper.user(), items[i]);
            }

            // Carried item - should work like this
            wrapper.write(Types.ITEM1_13_2, null);
        });

        protocol.registerServerbound(ServerboundPackets1_17.CONTAINER_CLICK, wrapper -> {
            wrapper.passthrough(Types.BYTE); // Container id
            wrapper.read(Types.VAR_INT); // Remove state id
            wrapper.passthrough(Types.SHORT); // Slot
            wrapper.passthrough(Types.BYTE); // Button
            wrapper.passthrough(Types.VAR_INT); // Mode

            // Affected items
            final int length = Limit.max(wrapper.passthrough(Types.VAR_INT), 128);
            for (int i = 0; i < length; i++) {
                wrapper.passthrough(Types.SHORT); // Slot
                wrapper.write(Types.ITEM1_13_2, handleItemToServer(wrapper.user(), wrapper.read(Types.ITEM1_13_2)));
            }

            // Carried item
            wrapper.write(Types.ITEM1_13_2, handleItemToServer(wrapper.user(), wrapper.read(Types.ITEM1_13_2)));
        });
    }

    @Override
    public Item handleItemToClient(UserConnection connection, Item item) {
        if (item == null) return null;

        final CompoundTag tag = item.tag();
        if (tag == null) {
            return item;
        }

        int hideFlags = tag.getInt("HideFlags");
        if ((hideFlags & 1) == 0) {
            replaceInvalidEnchantments(tag, "Enchantments");
        }
        if ((hideFlags & 1 << 5) == 0) {
            replaceInvalidEnchantments(tag, "StoredEnchantments");
        }

        return item;
    }

    @Override
    public @Nullable Item handleItemToServer(final UserConnection connection, @Nullable final Item item) {
        if (item == null) return null;

        final CompoundTag tag = item.tag();
        if (tag == null) {
            return item;
        }

        restoreInvalidEnchantments(tag, "Enchantments");
        restoreInvalidEnchantments(tag, "StoredEnchantments");

        return item;
    }

    private void replaceInvalidEnchantments(CompoundTag tag, String tagName) {
        final ListTag<CompoundTag> enchantments = tag.getListTag(tagName, CompoundTag.class);
        if (enchantments == null) {
            return;
        }

        tag.put(nbtTagName(tagName), enchantments);

        boolean displayRestoreTagAdded = false;
        for (final CompoundTag enchantment : enchantments.getValue()) {
            final short lvl = enchantment.getShort("lvl");
            if (lvl >= 0 && lvl <= 255) {
                continue;
            }

            final String id = enchantment.getString("id");
            if (id == null) {
                continue;
            }

            CompoundTag display = tag.getCompoundTag("display");
            if (display == null) {
                tag.put("display", display = new CompoundTag());
            } else if (!displayRestoreTagAdded) {
                displayRestoreTagAdded = true;
                tag.put(nbtTagName("display"), display);
            }

            ListTag<StringTag> lore = display.getListTag("Lore", StringTag.class);
            if (lore == null) {
                display.put("Lore", lore = new ListTag<>(StringTag.class));
            }

            final Key key = Key.of(id);
            lore.getValue().add(0, new StringTag("{\"italic\":false,\"color\":\"gray\",\"translate\":\"enchantment." + key.namespace() + "." + key.path() + "\",\"extra\":[\" \",{\"translate\":\"enchantment.level." + lvl + "\"}]}"));
        }
    }

    private void restoreInvalidEnchantments(final CompoundTag tag, final String tagName) {
        final Tag enchantments = tag.remove(nbtTagName(tagName));
        if (enchantments == null) {
            return;
        }

        tag.put(tagName, enchantments);

        final Tag display = tag.remove(nbtTagName("display"));
        if (display == null) {
            tag.remove("display");
        } else {
            tag.put("display", display);
        }
    }
}

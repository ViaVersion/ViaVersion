/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2026 ViaVersion and contributors
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
package com.viaversion.viaversion.protocols.v1_10to1_11.rewriter;

import com.viaversion.nbt.tag.ByteTag;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_10to1_11.Protocol1_10To1_11;
import com.viaversion.viaversion.protocols.v1_10to1_11.data.EntityMappings1_11;
import com.viaversion.viaversion.protocols.v1_9_1to1_9_3.packet.ClientboundPackets1_9_3;
import com.viaversion.viaversion.protocols.v1_9_1to1_9_3.packet.ServerboundPackets1_9_3;
import com.viaversion.viaversion.rewriter.ItemRewriter;

public class ItemPacketRewriter1_11 extends ItemRewriter<ClientboundPackets1_9_3, ServerboundPackets1_9_3, Protocol1_10To1_11> {

    public ItemPacketRewriter1_11(Protocol1_10To1_11 protocol) {
        super(protocol, Types.ITEM1_8, Types.ITEM1_8_SHORT_ARRAY);
    }

    @Override
    public void registerPackets() {
        registerSetSlot(ClientboundPackets1_9_3.CONTAINER_SET_SLOT);
        registerSetContent(ClientboundPackets1_9_3.CONTAINER_SET_CONTENT);
        registerSetEquippedItem(ClientboundPackets1_9_3.SET_EQUIPPED_ITEM);
        registerCustomPayloadTradeList(ClientboundPackets1_9_3.CUSTOM_PAYLOAD);
        registerContainerClick(ServerboundPackets1_9_3.CONTAINER_CLICK);
        registerSetCreativeModeSlot(ServerboundPackets1_9_3.SET_CREATIVE_MODE_SLOT);
    }

    @Override
    public Item handleItemToClient(UserConnection connection, Item item) {
        if (item != null && item.amount() <= 0) {
            CompoundTag tag = item.tag();
            if (tag == null) {
                item.setTag(tag = new CompoundTag());
            }
            tag.putByte(nbtTagName(), (byte) item.amount());
            item.setAmount(1);
        }
        EntityMappings1_11.toClientItem(item);

        if (item == null || item.tag() == null) {
            return item;
        }
        CompoundTag tag = item.tag();

        // 1.10 will display item glint if the list tag is present, 1.11+ only if at least one element is present
        ListTag enchTag = tag.getListTag("ench");
        if (enchTag != null && enchTag.isEmpty()) {
            tag.putBoolean(nbtTagName("clearEnch"), true);

            enchTag.add(new CompoundTag());
        }
        return item;
    }

    @Override
    public Item handleItemToServer(UserConnection connection, Item item) {
        if (item == null) {
            return null;
        }
        CompoundTag tag = item.tag();
        if (tag != null) {
            if (tag.contains(nbtTagName())) {
                item.setAmount(tag.<ByteTag>removeUnchecked(nbtTagName()).asByte());
                if (tag.isEmpty()) {
                    item.setTag(null);
                }
            }
            if (tag.remove(nbtTagName("clearEnch")) != null) {
                tag.put("ench", new ListTag());
            }
        }
        EntityMappings1_11.toServerItem(item);
        boolean newItem = item.identifier() >= 218 && item.identifier() <= 234;
        newItem |= item.identifier() == 449 || item.identifier() == 450;
        if (newItem) { // Replace server-side unknown items
            item.setIdentifier(1);
            item.setData((short) 0);
        }
        return item;
    }
}

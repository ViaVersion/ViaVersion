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
package com.viaversion.viaversion.protocols.v1_17to1_17_1;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.viaversion.api.minecraft.item.DataItem;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.StringType;
import com.viaversion.viaversion.protocols.v1_16_4to1_17.packet.ClientboundPackets1_17;
import com.viaversion.viaversion.protocols.v1_16_4to1_17.packet.ServerboundPackets1_17;
import com.viaversion.viaversion.protocols.v1_17to1_17_1.packet.ClientboundPackets1_17_1;
import com.viaversion.viaversion.protocols.v1_17to1_17_1.rewriter.ItemPacketRewriter1_17_1;

public final class Protocol1_17To1_17_1 extends AbstractProtocol<ClientboundPackets1_17, ClientboundPackets1_17_1, ServerboundPackets1_17, ServerboundPackets1_17> {

    private static final StringType PAGE_STRING_TYPE = new StringType(8192);
    private static final StringType TITLE_STRING_TYPE = new StringType(128);
    private final ItemPacketRewriter1_17_1 itemRewriter = new ItemPacketRewriter1_17_1(this);

    public Protocol1_17To1_17_1() {
        super(ClientboundPackets1_17.class, ClientboundPackets1_17_1.class, ServerboundPackets1_17.class, ServerboundPackets1_17.class);
    }

    @Override
    protected void registerPackets() {
        super.registerPackets();

        registerClientbound(ClientboundPackets1_17.REMOVE_ENTITY, ClientboundPackets1_17_1.REMOVE_ENTITIES, wrapper -> {
            // Aaaaand back to an array again!
            int entityId = wrapper.read(Types.VAR_INT);
            wrapper.write(Types.VAR_INT_ARRAY_PRIMITIVE, new int[]{entityId});
        });

        registerServerbound(ServerboundPackets1_17.EDIT_BOOK, wrapper -> {
            CompoundTag tag = new CompoundTag();
            Item item = new DataItem(942, (byte) 1, tag); // Magic value for writable books

            // Write the item, edit the tag down the line
            wrapper.write(Types.ITEM1_13_2, item);

            int slot = wrapper.read(Types.VAR_INT);

            // Save pages to tag
            int pages = wrapper.read(Types.VAR_INT);
            ListTag<StringTag> pagesTag = new ListTag<>(StringTag.class);
            for (int i = 0; i < pages; i++) {
                String page = wrapper.read(PAGE_STRING_TYPE);
                if (i < 200) { // Apply network limit as per game code
                    pagesTag.add(new StringTag(page));
                }
            }

            // Legacy servers don't like an empty pages list
            if (pagesTag.isEmpty()) {
                pagesTag.add(new StringTag(""));
            }

            tag.put("pages", pagesTag);

            if (wrapper.read(Types.BOOLEAN)) {
                // Save the title to tag
                String title = wrapper.read(TITLE_STRING_TYPE);
                tag.put("title", new StringTag(title));

                // Even if unused, legacy servers check for the author tag
                tag.put("author", new StringTag(wrapper.user().getProtocolInfo().getUsername()));

                // Write signing
                wrapper.write(Types.BOOLEAN, true);
            } else {
                wrapper.write(Types.BOOLEAN, false);
            }

            // Write the slot
            wrapper.write(Types.VAR_INT, slot);
        });
    }

    @Override
    public ItemPacketRewriter1_17_1 getItemRewriter() {
        return itemRewriter;
    }
}

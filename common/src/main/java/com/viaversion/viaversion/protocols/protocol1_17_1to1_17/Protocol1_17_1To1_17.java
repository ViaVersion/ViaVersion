/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2023 ViaVersion and contributors
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
package com.viaversion.viaversion.protocols.protocol1_17_1to1_17;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.ListTag;
import com.github.steveice10.opennbt.tag.builtin.StringTag;
import com.viaversion.viaversion.api.minecraft.item.DataItem;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.StringType;
import com.viaversion.viaversion.protocols.protocol1_17to1_16_4.ClientboundPackets1_17;
import com.viaversion.viaversion.protocols.protocol1_17to1_16_4.ServerboundPackets1_17;

public final class Protocol1_17_1To1_17 extends AbstractProtocol<ClientboundPackets1_17, ClientboundPackets1_17_1, ServerboundPackets1_17, ServerboundPackets1_17> {

    private static final StringType PAGE_STRING_TYPE = new StringType(8192);
    private static final StringType TITLE_STRING_TYPE = new StringType(128);

    public Protocol1_17_1To1_17() {
        super(ClientboundPackets1_17.class, ClientboundPackets1_17_1.class, ServerboundPackets1_17.class, ServerboundPackets1_17.class);
    }

    @Override
    protected void registerPackets() {
        registerClientbound(ClientboundPackets1_17.REMOVE_ENTITY, ClientboundPackets1_17_1.REMOVE_ENTITIES, wrapper -> {
            // Aaaaand back to an array again!
            int entityId = wrapper.read(Type.VAR_INT);
            wrapper.write(Type.VAR_INT_ARRAY_PRIMITIVE, new int[]{entityId});
        });

        registerClientbound(ClientboundPackets1_17.SET_SLOT, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.UNSIGNED_BYTE); // Container id
                create(Type.VAR_INT, 0); // Add arbitrary state id
            }
        });

        registerClientbound(ClientboundPackets1_17.WINDOW_ITEMS, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.UNSIGNED_BYTE); // Container id
                create(Type.VAR_INT, 0); // Add arbitrary state id
                handler(wrapper -> {
                    // Length encoded as var int now
                    wrapper.write(Type.FLAT_VAR_INT_ITEM_ARRAY_VAR_INT, wrapper.read(Type.FLAT_VAR_INT_ITEM_ARRAY));

                    // Carried item - should work like this
                    wrapper.write(Type.FLAT_VAR_INT_ITEM, null);
                });
            }
        });

        registerServerbound(ServerboundPackets1_17.CLICK_WINDOW, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.UNSIGNED_BYTE); // Container id
                read(Type.VAR_INT); // Remove state id
            }
        });

        registerServerbound(ServerboundPackets1_17.EDIT_BOOK, wrapper -> {
            CompoundTag tag = new CompoundTag();
            Item item = new DataItem(942, (byte) 1, (short) 0, tag); // Magic value for writable books

            // Write the item, edit the tag down the line
            wrapper.write(Type.FLAT_VAR_INT_ITEM, item);

            int slot = wrapper.read(Type.VAR_INT);

            // Save pages to tag
            int pages = wrapper.read(Type.VAR_INT);
            ListTag pagesTag = new ListTag(StringTag.class);
            for (int i = 0; i < pages; i++) {
                String page = wrapper.read(PAGE_STRING_TYPE);
                pagesTag.add(new StringTag(page));
            }

            // Legacy servers don't like an empty pages list
            if (pagesTag.size() == 0) {
                pagesTag.add(new StringTag(""));
            }

            tag.put("pages", pagesTag);

            if (wrapper.read(Type.BOOLEAN)) {
                // Save the title to tag
                String title = wrapper.read(TITLE_STRING_TYPE);
                tag.put("title", new StringTag(title));

                // Even if unused, legacy servers check for the author tag
                tag.put("author", new StringTag(wrapper.user().getProtocolInfo().getUsername()));

                // Write signing
                wrapper.write(Type.BOOLEAN, true);
            } else {
                wrapper.write(Type.BOOLEAN, false);
            }

            // Write the slot
            wrapper.write(Type.VAR_INT, slot);
        });
    }
}

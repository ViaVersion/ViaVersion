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
package com.viaversion.viaversion.protocols.protocol1_14to1_13_2.packets;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.ListTag;
import com.github.steveice10.opennbt.tag.builtin.StringTag;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.minecraft.Position;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.ClientboundPackets1_13;
import com.viaversion.viaversion.protocols.protocol1_14to1_13_2.Protocol1_14To1_13_2;
import com.viaversion.viaversion.protocols.protocol1_14to1_13_2.ServerboundPackets1_14;
import java.util.Collections;

public class PlayerPackets {

    public static void register(Protocol1_14To1_13_2 protocol) {
        protocol.registerClientbound(ClientboundPackets1_13.OPEN_SIGN_EDITOR, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.POSITION1_8, Type.POSITION1_14);
            }
        });

        protocol.registerServerbound(ServerboundPackets1_14.QUERY_BLOCK_NBT, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT);
                map(Type.POSITION1_14, Type.POSITION1_8);
            }
        });

        protocol.registerServerbound(ServerboundPackets1_14.EDIT_BOOK, wrapper -> {
            Item item = wrapper.passthrough(Type.ITEM1_13_2);
            protocol.getItemRewriter().handleItemToServer(wrapper.user(), item);

            if (item == null) return;

            CompoundTag tag = item.tag();
            if (tag == null) return;

            ListTag<StringTag> pages = tag.getListTag("pages", StringTag.class);

            // Fix for https://github.com/ViaVersion/ViaVersion/issues/2660
            if (pages == null) {
                pages = new ListTag<>(StringTag.class);
                pages.add(new StringTag());
                tag.put("pages", pages);
                return;
            }

            // Client limit when editing a book was upped from 50 to 100 in 1.14, but some anti-exploit plugins ban with a size higher than the old client limit
            if (Via.getConfig().isTruncate1_14Books()) {
                if (pages.size() > 50) {
                    pages.setValue(pages.getValue().subList(0, 50));
                }
            }
        });

        protocol.registerServerbound(ServerboundPackets1_14.PLAYER_DIGGING, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT); // Action
                map(Type.POSITION1_14, Type.POSITION1_8); // Position
            }
        });

        protocol.registerServerbound(ServerboundPackets1_14.RECIPE_BOOK_DATA, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT);
                handler(wrapper -> {
                    int type = wrapper.get(Type.VAR_INT, 0);
                    if (type == 0) {
                        wrapper.passthrough(Type.STRING);
                    } else if (type == 1) {
                        wrapper.passthrough(Type.BOOLEAN); // Crafting Recipe Book Open
                        wrapper.passthrough(Type.BOOLEAN); // Crafting Recipe Filter Active
                        wrapper.passthrough(Type.BOOLEAN); // Smelting Recipe Book Open
                        wrapper.passthrough(Type.BOOLEAN); // Smelting Recipe Filter Active

                        // Unknown new booleans
                        wrapper.read(Type.BOOLEAN);
                        wrapper.read(Type.BOOLEAN);
                        wrapper.read(Type.BOOLEAN);
                        wrapper.read(Type.BOOLEAN);
                    }
                });
            }
        });

        protocol.registerServerbound(ServerboundPackets1_14.UPDATE_COMMAND_BLOCK, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.POSITION1_14, Type.POSITION1_8);
            }
        });
        protocol.registerServerbound(ServerboundPackets1_14.UPDATE_STRUCTURE_BLOCK, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.POSITION1_14, Type.POSITION1_8);
            }
        });
        protocol.registerServerbound(ServerboundPackets1_14.UPDATE_SIGN, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.POSITION1_14, Type.POSITION1_8);
            }
        });

        protocol.registerServerbound(ServerboundPackets1_14.PLAYER_BLOCK_PLACEMENT, wrapper -> {
            int hand = wrapper.read(Type.VAR_INT);
            Position position = wrapper.read(Type.POSITION1_14);
            int face = wrapper.read(Type.VAR_INT);
            float x = wrapper.read(Type.FLOAT);
            float y = wrapper.read(Type.FLOAT);
            float z = wrapper.read(Type.FLOAT);
            wrapper.read(Type.BOOLEAN);  // new unknown boolean

            wrapper.write(Type.POSITION1_8, position);
            wrapper.write(Type.VAR_INT, face);
            wrapper.write(Type.VAR_INT, hand);
            wrapper.write(Type.FLOAT, x);
            wrapper.write(Type.FLOAT, y);
            wrapper.write(Type.FLOAT, z);
        });
    }
}

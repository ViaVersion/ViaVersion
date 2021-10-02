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
package com.viaversion.viaversion.protocols.protocol1_14to1_13_2.packets;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.ListTag;
import com.github.steveice10.opennbt.tag.builtin.Tag;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.minecraft.Position;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.protocol.remapper.PacketRemapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.ClientboundPackets1_13;
import com.viaversion.viaversion.protocols.protocol1_14to1_13_2.ServerboundPackets1_14;

public class PlayerPackets {

    public static void register(Protocol protocol) {
        protocol.registerClientbound(ClientboundPackets1_13.OPEN_SIGN_EDITOR, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.POSITION, Type.POSITION1_14);
            }
        });

        protocol.registerServerbound(ServerboundPackets1_14.QUERY_BLOCK_NBT, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT);
                map(Type.POSITION1_14, Type.POSITION);
            }
        });

        protocol.registerServerbound(ServerboundPackets1_14.EDIT_BOOK, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        Item item = wrapper.passthrough(Type.FLAT_VAR_INT_ITEM);
                        protocol.getItemRewriter().handleItemToServer(item);

                        // Client limit when editing a book was upped from 50 to 100 in 1.14, but some anti-exploit plugins ban with a size higher than the old client limit
                        if (Via.getConfig().isTruncate1_14Books()) {
                            if (item == null) return;
                            CompoundTag tag = item.tag();

                            if (tag == null) return;
                            Tag pages = tag.get("pages");

                            if (!(pages instanceof ListTag)) return;

                            ListTag listTag = (ListTag) pages;
                            if (listTag.size() <= 50) return;
                            listTag.setValue(listTag.getValue().subList(0, 50));
                        }
                    }
                });
            }
        });

        protocol.registerServerbound(ServerboundPackets1_14.PLAYER_DIGGING, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT);
                map(Type.POSITION1_14, Type.POSITION);
                map(Type.BYTE);
            }
        });

        protocol.registerServerbound(ServerboundPackets1_14.RECIPE_BOOK_DATA, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT);
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
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
                    }
                });
            }
        });

        protocol.registerServerbound(ServerboundPackets1_14.UPDATE_COMMAND_BLOCK, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.POSITION1_14, Type.POSITION);
            }
        });
        protocol.registerServerbound(ServerboundPackets1_14.UPDATE_STRUCTURE_BLOCK, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.POSITION1_14, Type.POSITION);
            }
        });
        protocol.registerServerbound(ServerboundPackets1_14.UPDATE_SIGN, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.POSITION1_14, Type.POSITION);
            }
        });

        protocol.registerServerbound(ServerboundPackets1_14.PLAYER_BLOCK_PLACEMENT, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int hand = wrapper.read(Type.VAR_INT);
                        Position position = wrapper.read(Type.POSITION1_14);
                        int face = wrapper.read(Type.VAR_INT);
                        float x = wrapper.read(Type.FLOAT);
                        float y = wrapper.read(Type.FLOAT);
                        float z = wrapper.read(Type.FLOAT);
                        wrapper.read(Type.BOOLEAN);  // new unknown boolean

                        wrapper.write(Type.POSITION, position);
                        wrapper.write(Type.VAR_INT, face);
                        wrapper.write(Type.VAR_INT, hand);
                        wrapper.write(Type.FLOAT, x);
                        wrapper.write(Type.FLOAT, y);
                        wrapper.write(Type.FLOAT, z);
                    }
                });
            }
        });
    }
}

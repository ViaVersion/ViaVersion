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
package com.viaversion.viaversion.protocols.v1_13_1to1_13_2.rewriter;

import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.packet.ClientboundPackets1_13;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.packet.ServerboundPackets1_13;
import com.viaversion.viaversion.protocols.v1_13_1to1_13_2.Protocol1_13_1To1_13_2;
import com.viaversion.viaversion.util.Key;

public class ItemPacketRewriter1_13_2 {

    public static void register(Protocol1_13_1To1_13_2 protocol) {
        protocol.registerClientbound(ClientboundPackets1_13.CONTAINER_SET_SLOT, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.BYTE); // 0 - Window ID
                map(Types.SHORT); // 1 - Slot ID
                map(Types.ITEM1_13, Types.ITEM1_13_2); // 2 - Slot Value
            }
        });
        protocol.registerClientbound(ClientboundPackets1_13.CONTAINER_SET_CONTENT, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.UNSIGNED_BYTE); // 0 - Window ID
                map(Types.ITEM1_13_SHORT_ARRAY, Types.ITEM1_13_2_SHORT_ARRAY); // 1 - Window Values
            }
        });

        protocol.registerClientbound(ClientboundPackets1_13.CUSTOM_PAYLOAD, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.STRING); // Channel
                handlerSoftFail(wrapper -> {
                    String channel = Key.namespaced(wrapper.get(Types.STRING, 0));
                    if (channel.equals("minecraft:trader_list")) {
                        wrapper.passthrough(Types.INT); // Passthrough Window ID

                        int size = wrapper.passthrough(Types.UNSIGNED_BYTE);
                        for (int i = 0; i < size; i++) {
                            // Input Item
                            wrapper.write(Types.ITEM1_13_2, wrapper.read(Types.ITEM1_13));
                            // Output Item
                            wrapper.write(Types.ITEM1_13_2, wrapper.read(Types.ITEM1_13));

                            boolean secondItem = wrapper.passthrough(Types.BOOLEAN); // Has second item
                            if (secondItem) {
                                wrapper.write(Types.ITEM1_13_2, wrapper.read(Types.ITEM1_13));
                            }

                            wrapper.passthrough(Types.BOOLEAN); // Trade disabled
                            wrapper.passthrough(Types.INT); // Number of tools uses
                            wrapper.passthrough(Types.INT); // Maximum number of trade uses
                        }
                    }
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_13.SET_EQUIPPED_ITEM, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); // 0 - Entity ID
                map(Types.VAR_INT); // 1 - Slot ID
                map(Types.ITEM1_13, Types.ITEM1_13_2); // 2 - Item
            }
        });

        protocol.registerClientbound(ClientboundPackets1_13.UPDATE_RECIPES, wrapper -> {
            int recipesNo = wrapper.passthrough(Types.VAR_INT);
            for (int i = 0; i < recipesNo; i++) {
                wrapper.passthrough(Types.STRING); // Id
                String type = wrapper.passthrough(Types.STRING);
                if (type.equals("crafting_shapeless")) {
                    wrapper.passthrough(Types.STRING); // Group
                    int ingredientsNo = wrapper.passthrough(Types.VAR_INT);
                    for (int i1 = 0; i1 < ingredientsNo; i1++) {
                        wrapper.write(Types.ITEM1_13_2_ARRAY, wrapper.read(Types.ITEM1_13_ARRAY));
                    }
                    wrapper.write(Types.ITEM1_13_2, wrapper.read(Types.ITEM1_13));
                } else if (type.equals("crafting_shaped")) {
                    int ingredientsNo = wrapper.passthrough(Types.VAR_INT) * wrapper.passthrough(Types.VAR_INT);
                    wrapper.passthrough(Types.STRING); // Group
                    for (int i1 = 0; i1 < ingredientsNo; i1++) {
                        wrapper.write(Types.ITEM1_13_2_ARRAY, wrapper.read(Types.ITEM1_13_ARRAY));
                    }
                    wrapper.write(Types.ITEM1_13_2, wrapper.read(Types.ITEM1_13));
                } else if (type.equals("smelting")) {
                    wrapper.passthrough(Types.STRING); // Group
                    // Ingredient start
                    wrapper.write(Types.ITEM1_13_2_ARRAY, wrapper.read(Types.ITEM1_13_ARRAY));
                    // Ingredient end
                    wrapper.write(Types.ITEM1_13_2, wrapper.read(Types.ITEM1_13));
                    wrapper.passthrough(Types.FLOAT); // EXP
                    wrapper.passthrough(Types.VAR_INT); // Cooking time
                }
            }
        });

        protocol.registerServerbound(ServerboundPackets1_13.CONTAINER_CLICK, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.BYTE); // 0 - Window ID
                map(Types.SHORT); // 1 - Slot
                map(Types.BYTE); // 2 - Button
                map(Types.SHORT); // 3 - Action number
                map(Types.VAR_INT); // 4 - Mode
                map(Types.ITEM1_13_2, Types.ITEM1_13); // 5 - Clicked Item
            }
        });
        protocol.registerServerbound(ServerboundPackets1_13.SET_CREATIVE_MODE_SLOT, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.SHORT); // 0 - Slot
                map(Types.ITEM1_13_2, Types.ITEM1_13); // 1 - Clicked Item
            }
        });
    }
}

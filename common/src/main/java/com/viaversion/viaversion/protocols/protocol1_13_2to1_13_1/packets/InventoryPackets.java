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
package com.viaversion.viaversion.protocols.protocol1_13_2to1_13_1.packets;

import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_13_2to1_13_1.Protocol1_13_2To1_13_1;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.ClientboundPackets1_13;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.ServerboundPackets1_13;
import com.viaversion.viaversion.util.Key;

public class InventoryPackets {

    public static void register(Protocol1_13_2To1_13_1 protocol) {
        protocol.registerClientbound(ClientboundPackets1_13.SET_SLOT, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.UNSIGNED_BYTE); // 0 - Window ID
                map(Type.SHORT); // 1 - Slot ID
                map(Type.ITEM1_13, Type.ITEM1_13_2); // 2 - Slot Value
            }
        });
        protocol.registerClientbound(ClientboundPackets1_13.WINDOW_ITEMS, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.UNSIGNED_BYTE); // 0 - Window ID
                map(Type.ITEM1_13_SHORT_ARRAY, Type.ITEM1_13_2_SHORT_ARRAY); // 1 - Window Values
            }
        });

        protocol.registerClientbound(ClientboundPackets1_13.PLUGIN_MESSAGE, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.STRING); // Channel
                handlerSoftFail(wrapper -> {
                    String channel = Key.namespaced(wrapper.get(Type.STRING, 0));
                    if (channel.equals("minecraft:trader_list")) {
                        wrapper.passthrough(Type.INT); // Passthrough Window ID

                        int size = wrapper.passthrough(Type.UNSIGNED_BYTE);
                        for (int i = 0; i < size; i++) {
                            // Input Item
                            wrapper.write(Type.ITEM1_13_2, wrapper.read(Type.ITEM1_13));
                            // Output Item
                            wrapper.write(Type.ITEM1_13_2, wrapper.read(Type.ITEM1_13));

                            boolean secondItem = wrapper.passthrough(Type.BOOLEAN); // Has second item
                            if (secondItem) {
                                wrapper.write(Type.ITEM1_13_2, wrapper.read(Type.ITEM1_13));
                            }

                            wrapper.passthrough(Type.BOOLEAN); // Trade disabled
                            wrapper.passthrough(Type.INT); // Number of tools uses
                            wrapper.passthrough(Type.INT); // Maximum number of trade uses
                        }
                    }
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_13.ENTITY_EQUIPMENT, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT); // 0 - Entity ID
                map(Type.VAR_INT); // 1 - Slot ID
                map(Type.ITEM1_13, Type.ITEM1_13_2); // 2 - Item
            }
        });

        protocol.registerClientbound(ClientboundPackets1_13.DECLARE_RECIPES, wrapper -> {
            int recipesNo = wrapper.passthrough(Type.VAR_INT);
            for (int i = 0; i < recipesNo; i++) {
                wrapper.passthrough(Type.STRING); // Id
                String type = wrapper.passthrough(Type.STRING);
                if (type.equals("crafting_shapeless")) {
                    wrapper.passthrough(Type.STRING); // Group
                    int ingredientsNo = wrapper.passthrough(Type.VAR_INT);
                    for (int i1 = 0; i1 < ingredientsNo; i1++) {
                        wrapper.write(Type.ITEM1_13_2_ARRAY, wrapper.read(Type.ITEM1_13_ARRAY));
                    }
                    wrapper.write(Type.ITEM1_13_2, wrapper.read(Type.ITEM1_13));
                } else if (type.equals("crafting_shaped")) {
                    int ingredientsNo = wrapper.passthrough(Type.VAR_INT) * wrapper.passthrough(Type.VAR_INT);
                    wrapper.passthrough(Type.STRING); // Group
                    for (int i1 = 0; i1 < ingredientsNo; i1++) {
                        wrapper.write(Type.ITEM1_13_2_ARRAY, wrapper.read(Type.ITEM1_13_ARRAY));
                    }
                    wrapper.write(Type.ITEM1_13_2, wrapper.read(Type.ITEM1_13));
                } else if (type.equals("smelting")) {
                    wrapper.passthrough(Type.STRING); // Group
                    // Ingredient start
                    wrapper.write(Type.ITEM1_13_2_ARRAY, wrapper.read(Type.ITEM1_13_ARRAY));
                    // Ingredient end
                    wrapper.write(Type.ITEM1_13_2, wrapper.read(Type.ITEM1_13));
                    wrapper.passthrough(Type.FLOAT); // EXP
                    wrapper.passthrough(Type.VAR_INT); // Cooking time
                }
            }
        });

        protocol.registerServerbound(ServerboundPackets1_13.CLICK_WINDOW, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.UNSIGNED_BYTE); // 0 - Window ID
                map(Type.SHORT); // 1 - Slot
                map(Type.BYTE); // 2 - Button
                map(Type.SHORT); // 3 - Action number
                map(Type.VAR_INT); // 4 - Mode
                map(Type.ITEM1_13_2, Type.ITEM1_13); // 5 - Clicked Item
            }
        });
        protocol.registerServerbound(ServerboundPackets1_13.CREATIVE_INVENTORY_ACTION, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.SHORT); // 0 - Slot
                map(Type.ITEM1_13_2, Type.ITEM1_13); // 1 - Clicked Item
            }
        });
    }
}

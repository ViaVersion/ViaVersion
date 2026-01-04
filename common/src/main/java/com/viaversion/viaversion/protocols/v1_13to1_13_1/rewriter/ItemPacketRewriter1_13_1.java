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
package com.viaversion.viaversion.protocols.v1_13to1_13_1.rewriter;

import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.packet.ClientboundPackets1_13;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.packet.ServerboundPackets1_13;
import com.viaversion.viaversion.protocols.v1_13to1_13_1.Protocol1_13To1_13_1;
import com.viaversion.viaversion.rewriter.ItemRewriter;
import com.viaversion.viaversion.rewriter.RecipeRewriter;
import com.viaversion.viaversion.util.Key;

public class ItemPacketRewriter1_13_1 extends ItemRewriter<ClientboundPackets1_13, ServerboundPackets1_13, Protocol1_13To1_13_1> {

    public ItemPacketRewriter1_13_1(Protocol1_13To1_13_1 protocol) {
        super(protocol, Types.ITEM1_13, Types.ITEM1_13_SHORT_ARRAY);
    }

    @Override
    public void registerPackets() {
        registerSetSlot(ClientboundPackets1_13.CONTAINER_SET_SLOT);
        registerSetContent(ClientboundPackets1_13.CONTAINER_SET_CONTENT);
        registerAdvancements(ClientboundPackets1_13.UPDATE_ADVANCEMENTS);
        registerCooldown(ClientboundPackets1_13.COOLDOWN);

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
                            handleItemToClient(wrapper.user(), wrapper.passthrough(Types.ITEM1_13));
                            // Output Item
                            handleItemToClient(wrapper.user(), wrapper.passthrough(Types.ITEM1_13));

                            boolean secondItem = wrapper.passthrough(Types.BOOLEAN); // Has second item
                            if (secondItem) {
                                // Second Item
                                handleItemToClient(wrapper.user(), wrapper.passthrough(Types.ITEM1_13));
                            }

                            wrapper.passthrough(Types.BOOLEAN); // Trade disabled
                            wrapper.passthrough(Types.INT); // Number of tools uses
                            wrapper.passthrough(Types.INT); // Maximum number of trade uses
                        }
                    }
                });
            }
        });

        registerSetEquippedItem(ClientboundPackets1_13.SET_EQUIPPED_ITEM);

        RecipeRewriter<ClientboundPackets1_13> recipeRewriter = new RecipeRewriter<>(protocol) {
            @Override
            protected Type<Item> itemType() {
                return Types.ITEM1_13;
            }

            @Override
            protected Type<Item[]> itemArrayType() {
                return Types.ITEM1_13_ARRAY;
            }
        };
        protocol.registerClientbound(ClientboundPackets1_13.UPDATE_RECIPES, wrapper -> {
            int size = wrapper.passthrough(Types.VAR_INT);
            for (int i = 0; i < size; i++) {
                // First id, then type
                wrapper.passthrough(Types.STRING); // Id
                String type = Key.stripMinecraftNamespace(wrapper.passthrough(Types.STRING));
                recipeRewriter.handleRecipeType(wrapper, type);
            }
        });

        registerContainerClick(ServerboundPackets1_13.CONTAINER_CLICK);
        registerSetCreativeModeSlot(ServerboundPackets1_13.SET_CREATIVE_MODE_SLOT);
    }
}

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
package com.viaversion.viaversion.protocols.protocol1_16_2to1_16_1.packets;

import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_16_2to1_16_1.Protocol1_16_2To1_16_1;
import com.viaversion.viaversion.protocols.protocol1_16_2to1_16_1.ServerboundPackets1_16_2;
import com.viaversion.viaversion.protocols.protocol1_16to1_15_2.ClientboundPackets1_16;
import com.viaversion.viaversion.rewriter.ItemRewriter;
import com.viaversion.viaversion.rewriter.RecipeRewriter;

public class InventoryPackets extends ItemRewriter<ClientboundPackets1_16, ServerboundPackets1_16_2, Protocol1_16_2To1_16_1> {

    public InventoryPackets(Protocol1_16_2To1_16_1 protocol) {
        super(protocol, Type.ITEM1_13_2, Type.ITEM1_13_2_SHORT_ARRAY);
    }

    @Override
    public void registerPackets() {
        registerSetCooldown(ClientboundPackets1_16.COOLDOWN);
        registerWindowItems(ClientboundPackets1_16.WINDOW_ITEMS);
        registerTradeList(ClientboundPackets1_16.TRADE_LIST);
        registerSetSlot(ClientboundPackets1_16.SET_SLOT);
        registerEntityEquipmentArray(ClientboundPackets1_16.ENTITY_EQUIPMENT);
        registerAdvancements(ClientboundPackets1_16.ADVANCEMENTS);

        protocol.registerClientbound(ClientboundPackets1_16.UNLOCK_RECIPES, wrapper -> {
            wrapper.passthrough(Type.VAR_INT);
            wrapper.passthrough(Type.BOOLEAN); // Open
            wrapper.passthrough(Type.BOOLEAN); // Filter
            wrapper.passthrough(Type.BOOLEAN); // Furnace
            wrapper.passthrough(Type.BOOLEAN); // Filter furnace
            // Blast furnace / smoker
            wrapper.write(Type.BOOLEAN, false);
            wrapper.write(Type.BOOLEAN, false);
            wrapper.write(Type.BOOLEAN, false);
            wrapper.write(Type.BOOLEAN, false);
        });

        new RecipeRewriter<>(protocol).register(ClientboundPackets1_16.DECLARE_RECIPES);

        registerClickWindow(ServerboundPackets1_16_2.CLICK_WINDOW);
        registerCreativeInvAction(ServerboundPackets1_16_2.CREATIVE_INVENTORY_ACTION);
        protocol.registerServerbound(ServerboundPackets1_16_2.EDIT_BOOK, wrapper -> handleItemToServer(wrapper.user(), wrapper.passthrough(Type.ITEM1_13_2)));

        registerSpawnParticle(ClientboundPackets1_16.SPAWN_PARTICLE, Type.DOUBLE);
    }
}

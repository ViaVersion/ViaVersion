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
package com.viaversion.viaversion.protocols.protocol1_15to1_14_4.packets;

import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_14_4to1_14_3.ClientboundPackets1_14_4;
import com.viaversion.viaversion.protocols.protocol1_14to1_13_2.ServerboundPackets1_14;
import com.viaversion.viaversion.protocols.protocol1_15to1_14_4.Protocol1_15To1_14_4;
import com.viaversion.viaversion.rewriter.ItemRewriter;
import com.viaversion.viaversion.rewriter.RecipeRewriter;

public class InventoryPackets extends ItemRewriter<ClientboundPackets1_14_4, ServerboundPackets1_14, Protocol1_15To1_14_4> {

    public InventoryPackets(Protocol1_15To1_14_4 protocol) {
        super(protocol, Type.ITEM1_13_2, Type.ITEM1_13_2_SHORT_ARRAY);
    }

    @Override
    public void registerPackets() {
        registerSetCooldown(ClientboundPackets1_14_4.COOLDOWN);
        registerWindowItems(ClientboundPackets1_14_4.WINDOW_ITEMS);
        registerTradeList(ClientboundPackets1_14_4.TRADE_LIST);
        registerSetSlot(ClientboundPackets1_14_4.SET_SLOT);
        registerEntityEquipment(ClientboundPackets1_14_4.ENTITY_EQUIPMENT);
        registerAdvancements(ClientboundPackets1_14_4.ADVANCEMENTS);

        new RecipeRewriter<>(protocol).register(ClientboundPackets1_14_4.DECLARE_RECIPES);

        registerClickWindow(ServerboundPackets1_14.CLICK_WINDOW);
        registerCreativeInvAction(ServerboundPackets1_14.CREATIVE_INVENTORY_ACTION);
    }
}

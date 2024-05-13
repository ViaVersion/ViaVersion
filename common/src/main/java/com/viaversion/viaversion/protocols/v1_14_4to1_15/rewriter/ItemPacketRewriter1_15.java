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
package com.viaversion.viaversion.protocols.v1_14_4to1_15.rewriter;

import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_13_2to1_14.packet.ServerboundPackets1_14;
import com.viaversion.viaversion.protocols.v1_14_3to1_14_4.packet.ClientboundPackets1_14_4;
import com.viaversion.viaversion.protocols.v1_14_4to1_15.Protocol1_14_4To1_15;
import com.viaversion.viaversion.rewriter.ItemRewriter;
import com.viaversion.viaversion.rewriter.RecipeRewriter;

public class ItemPacketRewriter1_15 extends ItemRewriter<ClientboundPackets1_14_4, ServerboundPackets1_14, Protocol1_14_4To1_15> {

    public ItemPacketRewriter1_15(Protocol1_14_4To1_15 protocol) {
        super(protocol, Types.ITEM1_13_2, Types.ITEM1_13_2_SHORT_ARRAY);
    }

    @Override
    public void registerPackets() {
        registerCooldown(ClientboundPackets1_14_4.COOLDOWN);
        registerSetContent(ClientboundPackets1_14_4.CONTAINER_SET_CONTENT);
        registerMerchantOffers(ClientboundPackets1_14_4.MERCHANT_OFFERS);
        registerSetSlot(ClientboundPackets1_14_4.CONTAINER_SET_SLOT);
        registerSetEquippedItem(ClientboundPackets1_14_4.SET_EQUIPPED_ITEM);
        registerAdvancements(ClientboundPackets1_14_4.UPDATE_ADVANCEMENTS);

        new RecipeRewriter<>(protocol).register(ClientboundPackets1_14_4.UPDATE_RECIPES);

        registerContainerClick(ServerboundPackets1_14.CONTAINER_CLICK);
        registerSetCreativeModeSlot(ServerboundPackets1_14.SET_CREATIVE_MODE_SLOT);
    }
}

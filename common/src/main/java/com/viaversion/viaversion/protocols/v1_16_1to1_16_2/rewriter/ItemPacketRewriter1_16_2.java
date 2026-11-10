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
package com.viaversion.viaversion.protocols.v1_16_1to1_16_2.rewriter;

import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_15_2to1_16.packet.ClientboundPackets1_16;
import com.viaversion.viaversion.protocols.v1_16_1to1_16_2.Protocol1_16_1To1_16_2;
import com.viaversion.viaversion.protocols.v1_16_1to1_16_2.packet.ServerboundPackets1_16_2;
import com.viaversion.viaversion.rewriter.ItemRewriter;
import com.viaversion.viaversion.rewriter.RecipeRewriter;

public class ItemPacketRewriter1_16_2 extends ItemRewriter<ClientboundPackets1_16, ServerboundPackets1_16_2, Protocol1_16_1To1_16_2> {

    public ItemPacketRewriter1_16_2(Protocol1_16_1To1_16_2 protocol) {
        super(protocol, Types.ITEM1_13_2, Types.ITEM1_13_2_SHORT_ARRAY);
    }

    @Override
    public void registerPackets() {
        protocol.registerClientbound(ClientboundPackets1_16.RECIPE, wrapper -> {
            wrapper.passthrough(Types.VAR_INT);
            wrapper.passthrough(Types.BOOLEAN); // Open
            wrapper.passthrough(Types.BOOLEAN); // Filter
            wrapper.passthrough(Types.BOOLEAN); // Furnace
            wrapper.passthrough(Types.BOOLEAN); // Filter furnace
            // Blast furnace / smoker
            wrapper.write(Types.BOOLEAN, false);
            wrapper.write(Types.BOOLEAN, false);
            wrapper.write(Types.BOOLEAN, false);
            wrapper.write(Types.BOOLEAN, false);
        });

        new RecipeRewriter<>(protocol).register(ClientboundPackets1_16.UPDATE_RECIPES);

        protocol.registerServerbound(ServerboundPackets1_16_2.EDIT_BOOK, wrapper -> handleItemToServer(wrapper.user(), wrapper.passthrough(Types.ITEM1_13_2)));
    }
}

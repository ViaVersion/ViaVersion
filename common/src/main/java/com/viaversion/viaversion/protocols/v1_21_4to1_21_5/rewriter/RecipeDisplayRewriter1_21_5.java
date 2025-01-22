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
package com.viaversion.viaversion.protocols.v1_21_4to1_21_5.rewriter;

import com.viaversion.viaversion.api.minecraft.item.data.ArmorTrimPattern;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.rewriter.RecipeDisplayRewriter;

public class RecipeDisplayRewriter1_21_5<C extends ClientboundPacketType> extends RecipeDisplayRewriter<C> {

    public RecipeDisplayRewriter1_21_5(final Protocol<C, ?, ?, ?> protocol) {
        super(protocol);
    }

    @Override
    protected void handleSmithingTrimSlotDisplay(final PacketWrapper wrapper) {
        handleSlotDisplay(wrapper); // Base
        handleSlotDisplay(wrapper); // Material
        wrapper.passthrough(ArmorTrimPattern.TYPE1_21_5); // Pattern
    }
}

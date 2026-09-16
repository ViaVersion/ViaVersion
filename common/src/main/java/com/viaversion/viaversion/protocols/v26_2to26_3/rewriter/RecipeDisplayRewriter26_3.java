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
package com.viaversion.viaversion.protocols.v26_2to26_3.rewriter;

import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_21_4to1_21_5.rewriter.RecipeDisplayRewriter1_21_5;

public final class RecipeDisplayRewriter26_3<C extends ClientboundPacketType> extends RecipeDisplayRewriter1_21_5<C> {

    public RecipeDisplayRewriter26_3(final Protocol<C, ?, ?, ?> protocol) {
        super(protocol);
        // Tag slot displays hold a holder set now, of which a tag is the zero sized form
        slotDisplayHandlers.put("tag", wrapper -> {
            final String tag = wrapper.read(Types.STRING);
            wrapper.write(Types.VAR_INT, 0);
            wrapper.write(Types.STRING, tag);
        });
    }
}

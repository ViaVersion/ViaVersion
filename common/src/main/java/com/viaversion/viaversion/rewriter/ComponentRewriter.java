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
package com.viaversion.viaversion.rewriter;

import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.rewriter.text.ComponentRewriterBase;
import com.viaversion.viaversion.rewriter.text.JsonNBTComponentRewriter;
import com.viaversion.viaversion.rewriter.text.NBTComponentRewriter;

/**
 * @deprecated use {@link JsonNBTComponentRewriter} or {@link NBTComponentRewriter}
 */
@Deprecated(forRemoval = true)
public abstract class ComponentRewriter<C extends ClientboundPacketType> extends JsonNBTComponentRewriter<C> {

    protected ComponentRewriter(final Protocol<C, ?, ?, ?> protocol, final ReadType type) {
        super(protocol, type == ReadType.JSON ? ComponentRewriterBase.ReadType.JSON : ComponentRewriterBase.ReadType.NBT);
    }

    public void registerOpenScreen(final C packetType) {
        super.registerOpenScreen1_14(packetType);
    }

    public enum ReadType {

        JSON,
        NBT
    }
}

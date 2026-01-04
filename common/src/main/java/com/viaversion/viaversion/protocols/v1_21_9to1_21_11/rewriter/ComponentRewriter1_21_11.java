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
package com.viaversion.viaversion.protocols.v1_21_9to1_21_11.rewriter;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.packet.ClientboundPacket1_21_9;
import com.viaversion.viaversion.protocols.v1_21_9to1_21_11.Protocol1_21_9To1_21_11;
import com.viaversion.viaversion.rewriter.text.NBTComponentRewriter;
import com.viaversion.viaversion.util.Key;

public final class ComponentRewriter1_21_11 extends NBTComponentRewriter<ClientboundPacket1_21_9> {

    public ComponentRewriter1_21_11(final Protocol1_21_9To1_21_11 protocol) {
        super(protocol);
    }

    @Override
    protected void processCompoundTag(final UserConnection connection, final CompoundTag tag) {
        super.processCompoundTag(connection, tag);

        final StringTag sprite = tag.getStringTag("sprite");
        if (sprite != null) {
            final String strippedSprite = Key.stripNamespace(sprite.getValue());
            if (strippedSprite.startsWith("item/")) {
                tag.putString("atlas", "items");
            } else if (strippedSprite.startsWith("block/")) {
                tag.putString("atlas", "blocks");
            }
        }
    }
}

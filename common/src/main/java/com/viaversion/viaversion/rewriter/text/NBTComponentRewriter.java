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
package com.viaversion.viaversion.rewriter.text;

import com.google.gson.JsonObject;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.item.data.ChatType;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.util.TagUtil;

/**
 * Rewrites nbt serialized components from 1.21.5 upwards. Can also handle json components outside of hover events.
 *
 * @param <C> clientbound packet type
 */
public class NBTComponentRewriter<C extends ClientboundPacketType> extends ComponentRewriterBase<C> {

    public NBTComponentRewriter(final Protocol<C, ?, ?, ?> protocol) {
        super(protocol, ReadType.NBT);
    }

    @Override
    protected void handleHoverEvent(final UserConnection connection, final JsonObject hoverEvent) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    protected void handleHoverEvent(final UserConnection connection, final CompoundTag hoverEventTag) {
        // To override if needed (don't forget to call super)
        final StringTag actionTag = hoverEventTag.getStringTag("action");
        if (actionTag == null) {
            return;
        }

        final String action = actionTag.getValue();
        if (action.equals("show_text")) {
            processTag(connection, hoverEventTag.get("value"));
        } else if (action.equals("show_entity")) {
            processTag(connection, hoverEventTag.get("name"));

            final StringTag idTag = hoverEventTag.getStringTag("id");
            if (idTag != null && protocol.getEntityRewriter() != null) {
                idTag.setValue(protocol.getEntityRewriter().mappedEntityIdentifier(idTag.getValue()));
            }
        } else if (action.equals("show_item")) {
            final CompoundTag componentsTag = hoverEventTag.getCompoundTag("components");
            handleShowItem(connection, hoverEventTag, componentsTag);
        }
    }

    @Override
    protected void handleNestedComponent(final UserConnection connection, final CompoundTag parent, final String key) {
        final Tag tag = parent.get(key);
        if (tag != null) {
            processTag(connection, tag);
        }
    }

    @Override
    protected String hoverEventKey() {
        return "hover_event";
    }

    public void registerPlayerChat1_21_5(final C packetType) {
        protocol.registerClientbound(packetType, wrapper -> {
            wrapper.passthrough(Types.VAR_INT); // Index
            wrapper.passthrough(Types.UUID); // Sender
            wrapper.passthrough(Types.VAR_INT); // Index
            wrapper.passthrough(Types.OPTIONAL_SIGNATURE_BYTES); // Signature
            wrapper.passthrough(Types.STRING); // Plain content
            wrapper.passthrough(Types.LONG); // Timestamp
            wrapper.passthrough(Types.LONG); // Salt

            final int lastSeen = wrapper.passthrough(Types.VAR_INT);
            for (int i = 0; i < lastSeen; i++) {
                final int index = wrapper.passthrough(Types.VAR_INT);
                if (index == 0) {
                    wrapper.passthrough(Types.SIGNATURE_BYTES);
                }
            }

            processTag(wrapper.user(), wrapper.passthrough(Types.OPTIONAL_TAG)); // Unsigned content

            final int filterMaskType = wrapper.passthrough(Types.VAR_INT);
            if (filterMaskType == 2) { // Partially filtered
                wrapper.passthrough(Types.LONG_ARRAY_PRIMITIVE); // Mask
            }

            wrapper.passthrough(ChatType.TYPE); // Chat Type
            processTag(wrapper.user(), wrapper.passthrough(Types.TAG)); // Name
            processTag(wrapper.user(), wrapper.passthrough(Types.OPTIONAL_TAG)); // Target Name
        });
    }
}

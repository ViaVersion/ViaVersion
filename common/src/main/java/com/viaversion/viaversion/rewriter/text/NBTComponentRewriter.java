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
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
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

            final StringTag typeTag = hoverEventTag.getStringTag("id");
            if (typeTag != null && protocol.getEntityRewriter() != null) {
                typeTag.setValue(protocol.getEntityRewriter().mappedEntityIdentifier(typeTag.getValue()));
            }
        } else if (action.equals("show_item")) {
            final CompoundTag componentsTag = hoverEventTag.getCompoundTag("components");
            handleShowItem(connection, hoverEventTag, componentsTag);
            if (componentsTag == null) {
                return;
            }

            handleWrittenBookContents(connection, componentsTag);
            handleContainerContents(connection, componentsTag);
            handleItemArrayContents(connection, componentsTag, "bundle_contents");
            handleItemArrayContents(connection, componentsTag, "charged_projectiles");
            final CompoundTag useRemainder = TagUtil.getNamespacedCompoundTag(componentsTag, "use_remainder");
            if (useRemainder != null) {
                handleShowItem(connection, useRemainder);
            }
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
}

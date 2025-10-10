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
package com.viaversion.viaversion.protocols.v1_13_2to1_14.rewriter;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.NumberTag;
import com.viaversion.nbt.tag.ShortTag;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.item.DataItem;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.protocols.v1_13_2to1_14.Protocol1_13_2To1_14;
import com.viaversion.viaversion.rewriter.text.JsonNBTComponentRewriter;
import com.viaversion.viaversion.util.ComponentUtil;
import com.viaversion.viaversion.util.SerializerVersion;
import com.viaversion.viaversion.util.StringUtil;
import java.util.logging.Level;

public final class ComponentRewriter1_14<C extends ClientboundPacketType> extends JsonNBTComponentRewriter<C> {

    public ComponentRewriter1_14(final Protocol<C, ?, ?, ?> protocol) {
        super(protocol, ReadType.JSON);
    }

    @Override
    protected void handleHoverEvent(UserConnection connection, JsonObject hoverEvent) {
        super.handleHoverEvent(connection, hoverEvent);
        final String action = hoverEvent.getAsJsonPrimitive("action").getAsString();
        if (!action.equals("show_item")) {
            return;
        }

        final JsonElement value = hoverEvent.get("value");
        if (value == null) {
            return;
        }

        CompoundTag tag;
        try {
            tag = ComponentUtil.deserializeLegacyShowItem(value, SerializerVersion.V1_13);
        } catch (Exception e) {
            if (!Via.getConfig().isSuppressTextComponentConversionWarnings()) {
                Protocol1_13_2To1_14.LOGGER.log(Level.WARNING, "Error reading NBT in show_item: " + StringUtil.forLogging(value), e);
            }
            return;
        }

        final CompoundTag itemTag = tag.getCompoundTag("tag");

        // Call item converter
        final Item item = new DataItem();
        item.setTag(itemTag);
        protocol.getItemRewriter().handleItemToClient(null, item);

        // Serialize again
        if (itemTag != null) {
            tag.put("tag", itemTag);
        }

        final JsonArray newValue = new JsonArray();
        final JsonObject showItem = new JsonObject();
        newValue.add(showItem);
        try {
            showItem.addProperty("text", SerializerVersion.V1_14.toSNBT(tag));
            hoverEvent.add("value", newValue);
        } catch (Exception e) {
            if (!Via.getConfig().isSuppressTextComponentConversionWarnings()) {
                Protocol1_13_2To1_14.LOGGER.log(Level.WARNING, "Error writing NBT in show_item: " + StringUtil.forLogging(value), e);
            }
        }
    }
}

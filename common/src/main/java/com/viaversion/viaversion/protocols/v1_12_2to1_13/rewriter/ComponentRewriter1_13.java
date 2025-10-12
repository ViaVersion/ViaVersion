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
package com.viaversion.viaversion.protocols.v1_12_2to1_13.rewriter;

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
import com.viaversion.viaversion.protocols.v1_12_2to1_13.Protocol1_12_2To1_13;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.data.ItemIds1_12_2;
import com.viaversion.viaversion.protocols.v1_13to1_13_1.Protocol1_13To1_13_1;
import com.viaversion.viaversion.rewriter.text.JsonNBTComponentRewriter;
import com.viaversion.viaversion.util.ComponentUtil;
import com.viaversion.viaversion.util.SerializerVersion;
import com.viaversion.viaversion.util.StringUtil;
import java.util.logging.Level;

public class ComponentRewriter1_13<C extends ClientboundPacketType> extends JsonNBTComponentRewriter<C> {

    public ComponentRewriter1_13(Protocol<C, ?, ?, ?> protocol) {
        super(protocol, ReadType.JSON);
    }

    @Override
    protected void handleHoverEvent(UserConnection connection, JsonObject hoverEvent) {
        super.handleHoverEvent(connection, hoverEvent);
        final String action = hoverEvent.getAsJsonPrimitive("action").getAsString();
        if (!action.equals("show_item")) return;

        final JsonElement value = hoverEvent.get("value");
        if (value == null) return;

        CompoundTag tag;
        try {
            tag = ComponentUtil.deserializeLegacyShowItem(value, SerializerVersion.V1_12);
        } catch (Exception e) {
            if (!Via.getConfig().isSuppressTextComponentConversionWarnings()) {
                Protocol1_12_2To1_13.LOGGER.log(Level.WARNING, "Error reading NBT in show_item: " + StringUtil.forLogging(value), e);
            }
            return;
        }

        final String idTag = tag.getString("id", "");
        final CompoundTag itemTag = tag.getCompoundTag("tag");
        final NumberTag damageTag = tag.getNumberTag("Damage");

        // Call item converter
        final int id = ItemIds1_12_2.getId(idTag, 1);

        final short damage = damageTag != null ? damageTag.asShort() : 0;

        final Item item = new DataItem();
        item.setIdentifier(id);
        item.setData(damage);
        item.setTag(itemTag);
        protocol.getItemRewriter().handleItemToClient(null, item);

        // Serialize again
        if (id != item.identifier()) {
            tag.putString("id", Protocol1_13To1_13_1.MAPPINGS.getFullItemMappings().identifier(item.identifier()));
        }
        if (damage != item.data()) {
            tag.put("Damage", new ShortTag(item.data()));
        }
        if (itemTag != null) {
            tag.put("tag", itemTag);
        }

        final JsonArray newValue = new JsonArray();
        final JsonObject showItem = new JsonObject();
        newValue.add(showItem);
        try {
            showItem.addProperty("text", SerializerVersion.V1_13.toSNBT(tag));
            hoverEvent.add("value", newValue);
        } catch (Exception e) {
            if (!Via.getConfig().isSuppressTextComponentConversionWarnings()) {
                Protocol1_12_2To1_13.LOGGER.log(Level.WARNING, "Error writing NBT in show_item: " + StringUtil.forLogging(value), e);
            }
        }
    }

    @Override
    protected void handleTranslate(JsonObject object, String translate) {
        super.handleTranslate(object, translate);
        String newTranslate;
        newTranslate = Protocol1_12_2To1_13.MAPPINGS.getTranslateMapping().get(translate);
        if (newTranslate == null) {
            newTranslate = Protocol1_12_2To1_13.MAPPINGS.getMojangTranslation().get(translate);
        }
        if (newTranslate != null) {
            object.addProperty("translate", newTranslate);
        }
    }
}

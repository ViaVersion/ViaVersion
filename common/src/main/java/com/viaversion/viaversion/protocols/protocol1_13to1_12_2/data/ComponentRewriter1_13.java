/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2021 ViaVersion and contributors
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
package com.viaversion.viaversion.protocols.protocol1_13to1_12_2.data;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.ShortTag;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.minecraft.item.DataItem;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.nbt.BinaryTagIO;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.Protocol1_13To1_12_2;
import com.viaversion.viaversion.rewriter.ComponentRewriter;

import java.io.IOException;

public class ComponentRewriter1_13 extends ComponentRewriter {

    public ComponentRewriter1_13(Protocol protocol) {
        super(protocol);
    }

    public ComponentRewriter1_13() {
        super(Via.getManager().getProtocolManager().getProtocol(Protocol1_13To1_12_2.class));
    }

    @Override
    protected void handleHoverEvent(JsonObject hoverEvent) {
        super.handleHoverEvent(hoverEvent);
        String action = hoverEvent.getAsJsonPrimitive("action").getAsString();
        if (!action.equals("show_item")) return;

        JsonElement value = hoverEvent.get("value");
        if (value == null) return;

        String text = findItemNBT(value);
        if (text == null) return;

        CompoundTag tag;
        try {
            tag = BinaryTagIO.readString(text);
        } catch (Exception e) {
            if (!Via.getConfig().isSuppressConversionWarnings() || Via.getManager().isDebug()) {
                Via.getPlatform().getLogger().warning("Error reading NBT in show_item:" + text);
                e.printStackTrace();
            }
            return;
        }

        CompoundTag itemTag = tag.get("tag");
        ShortTag damageTag = tag.get("Damage");

        // Call item converter
        short damage = damageTag != null ? damageTag.asShort() : 0;
        Item item = new DataItem();
        item.setData(damage);
        item.setTag(itemTag);
        protocol.getItemRewriter().handleItemToClient(item);

        // Serialize again
        if (damage != item.data()) {
            tag.put("Damage", new ShortTag(item.data()));
        }
        if (itemTag != null) {
            tag.put("tag", itemTag);
        }

        JsonArray array = new JsonArray();
        JsonObject object = new JsonObject();
        array.add(object);
        String serializedNBT;
        try {
            serializedNBT = BinaryTagIO.writeString(tag);
            object.addProperty("text", serializedNBT);
            hoverEvent.add("value", array);
        } catch (IOException e) {
            Via.getPlatform().getLogger().warning("Error writing NBT in show_item:" + text);
            e.printStackTrace();
        }
    }

    protected String findItemNBT(JsonElement element) {
        if (element.isJsonArray()) {
            for (JsonElement jsonElement : element.getAsJsonArray()) {
                String value = findItemNBT(jsonElement);
                if (value != null) {
                    return value;
                }
            }
        } else if (element.isJsonObject()) {
            JsonPrimitive text = element.getAsJsonObject().getAsJsonPrimitive("text");
            if (text != null) {
                return text.getAsString();
            }
        } else if (element.isJsonPrimitive()) {
            return element.getAsJsonPrimitive().getAsString();
        }
        return null;
    }

    @Override
    protected void handleTranslate(JsonObject object, String translate) {
        super.handleTranslate(object, translate);
        String newTranslate;
        newTranslate = Protocol1_13To1_12_2.MAPPINGS.getTranslateMapping().get(translate);
        if (newTranslate == null) {
            newTranslate = Protocol1_13To1_12_2.MAPPINGS.getMojangTranslation().get(translate);
        }
        if (newTranslate != null) {
            object.addProperty("translate", newTranslate);
        }
    }
}

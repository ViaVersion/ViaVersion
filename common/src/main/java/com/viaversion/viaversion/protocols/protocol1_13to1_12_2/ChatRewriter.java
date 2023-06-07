/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2023 ViaVersion and contributors
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
package com.viaversion.viaversion.protocols.protocol1_13to1_12_2;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.libs.kyori.adventure.text.Component;
import com.viaversion.viaversion.libs.kyori.adventure.text.format.TextDecoration;
import com.viaversion.viaversion.libs.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import com.viaversion.viaversion.libs.kyori.adventure.text.serializer.gson.legacyimpl.NBTLegacyHoverEventSerializer;
import com.viaversion.viaversion.libs.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public final class ChatRewriter {
    public static final GsonComponentSerializer HOVER_GSON_SERIALIZER = GsonComponentSerializer.builder().emitLegacyHoverEvent().legacyHoverEventSerializer(NBTLegacyHoverEventSerializer.get()).build();

    public static JsonObject emptyComponent() {
        final JsonObject object = new JsonObject();
        object.addProperty("text", "");
        return object;
    }

    public static String emptyComponentString() {
        return "{\"text\":\"\"}";
    }

    public static String legacyTextToJsonString(String message, boolean itemData) {
        // Not used for chat messages, so no need for url extraction
        Component component = LegacyComponentSerializer.legacySection().deserialize(message);
        if (itemData) {
            component = Component.text().decoration(TextDecoration.ITALIC, false).append(component).build();
        }
        return GsonComponentSerializer.gson().serialize(component);
    }

    public static String legacyTextToJsonString(String legacyText) {
        return legacyTextToJsonString(legacyText, false);
    }

    public static JsonElement legacyTextToJson(String legacyText) {
        return JsonParser.parseString(legacyTextToJsonString(legacyText, false));
    }

    public static String jsonToLegacyText(String value) {
        try {
            Component component = HOVER_GSON_SERIALIZER.deserialize(value);
            return LegacyComponentSerializer.legacySection().serialize(component);
        } catch (Exception e) {
            Via.getPlatform().getLogger().warning("Error converting json text to legacy: " + value);
            e.printStackTrace();
            return "";
        }
    }

    @Deprecated/*(forRemoval = true)*/
    public static void processTranslate(JsonElement value) {
        Via.getManager().getProtocolManager().getProtocol(Protocol1_13To1_12_2.class).getComponentRewriter().processText(value);
    }
}

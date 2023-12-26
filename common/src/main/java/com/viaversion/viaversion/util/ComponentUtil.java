/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2023 ViaVersion and contributors
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
package com.viaversion.viaversion.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.lenni0451.mcstructs.text.ATextComponent;
import net.lenni0451.mcstructs.text.Style;
import net.lenni0451.mcstructs.text.serializer.LegacyStringDeserializer;
import net.lenni0451.mcstructs.text.serializer.TextComponentSerializer;

public final class ComponentUtil {

    public static JsonObject emptyJsonComponent() {
        return plainTextToJson("");
    }

    public static String emptyJsonComponentString() {
        return "{\"text\":\"\"}";
    }

    public static JsonObject plainTextToJson(final String message) {
        final JsonObject object = new JsonObject();
        object.addProperty("text", message);
        return object;
    }

    public static JsonElement legacyToJson(final String message) {
        return TextComponentSerializer.V1_12.serializeJson(LegacyStringDeserializer.parse(message, true));
    }

    public static String legacyToJsonString(final String legacyText) {
        return legacyToJsonString(legacyText, false);
    }

    public static String legacyToJsonString(final String message, final boolean itemData) {
        final ATextComponent component = LegacyStringDeserializer.parse(message, true);
        if (itemData) {
            component.setParentStyle(new Style().setItalic(false));
        }
        return TextComponentSerializer.V1_12.serialize(component);
    }

    public static String jsonToLegacy(final String value) {
        return TextComponentSerializer.LATEST.deserialize(value).asLegacyFormatString();
    }

    public static String jsonToLegacy(final JsonElement value) {
        return TextComponentSerializer.LATEST.deserialize(value).asLegacyFormatString();
    }
}

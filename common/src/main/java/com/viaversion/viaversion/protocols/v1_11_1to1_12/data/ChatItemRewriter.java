/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2024 ViaVersion and contributors
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
package com.viaversion.viaversion.protocols.v1_11_1to1_12.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viaversion.util.ComponentUtil;
import com.viaversion.viaversion.util.SerializerVersion;

public final class ChatItemRewriter {

    public static void toClient(JsonElement element) {
        if (element instanceof final JsonObject obj) {
            if (obj.has("hoverEvent")) {
                if (!(obj.get("hoverEvent") instanceof final JsonObject hoverEvent)) {
                    return;
                }

                if (!hoverEvent.has("action") || !hoverEvent.has("value")) {
                    return;
                }

                final String type = hoverEvent.get("action").getAsString();
                final JsonElement value = hoverEvent.get("value");

                if (type.equals("show_item")) {
                    final CompoundTag compound = ComponentUtil.deserializeLegacyShowItem(value, SerializerVersion.V1_8);
                    hoverEvent.addProperty("value", SerializerVersion.V1_12.toSNBT(compound));
                }
            } else if (obj.has("extra")) {
                toClient(obj.get("extra"));
            } else if (obj.has("translate") && obj.has("with")) {
                toClient(obj.get("with"));
            }
        } else if (element instanceof final JsonArray array) {
            for (JsonElement value : array) {
                toClient(value);
            }
        }
    }
}

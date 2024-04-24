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
package com.viaversion.viaversion.util;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.StringTag;
import com.github.steveice10.opennbt.tag.builtin.Tag;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.viaversion.viaversion.api.Via;
import java.util.logging.Level;
import net.lenni0451.mcstructs.text.ATextComponent;
import net.lenni0451.mcstructs.text.Style;
import net.lenni0451.mcstructs.text.events.hover.AHoverEvent;
import net.lenni0451.mcstructs.text.events.hover.impl.TextHoverEvent;
import net.lenni0451.mcstructs.text.serializer.LegacyStringDeserializer;
import net.lenni0451.mcstructs.text.serializer.TextComponentSerializer;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Component conversion utility, trying to divert most calls to the component library to this class instead for easy replacement.
 */
public final class ComponentUtil {

    public static JsonObject emptyJsonComponent() {
        return plainToJson("");
    }

    public static String emptyJsonComponentString() {
        return "{\"text\":\"\"}";
    }

    public static JsonObject plainToJson(final String message) {
        final JsonObject object = new JsonObject();
        object.addProperty("text", message);
        return object;
    }

    public static @Nullable JsonElement tagToJson(@Nullable final Tag tag) {
        try {
            final ATextComponent component = SerializerVersion.V1_20_3.toComponent(tag);
            return component != null ? SerializerVersion.V1_19_4.toJson(component) : null;
        } catch (final Exception e) {
            Via.getPlatform().getLogger().log(Level.SEVERE, "Error converting tag: " + tag, e);
            return plainToJson("<error>");
        }
    }

    public static @Nullable Tag jsonToTag(@Nullable final JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return null;
        }

        try {
            final ATextComponent component = SerializerVersion.V1_19_4.toComponent(element);
            return trimStrings(SerializerVersion.V1_20_3.toTag(component));
        } catch (final Exception e) {
            Via.getPlatform().getLogger().log(Level.SEVERE, "Error converting component: " + element, e);
            return new StringTag("<error>");
        }
    }

    private static Tag trimStrings(final Tag input) {
        // Dirty fix for https://github.com/ViaVersion/ViaVersion/issues/3650
        // Usually tripped by hover event data being too long, e.g. book or shulker box contents, which have to be held in full as SNBT
        if (input == null) {
            return null;
        }
        return TagUtil.handleDeep(input, (key, tag) -> {
            if (tag instanceof StringTag) {
                final String value = ((StringTag) tag).getValue();
                if (value.length() > Short.MAX_VALUE) {
                    ((StringTag) tag).setValue("{}");
                }
            }
            return tag;
        });
    }

    public static @Nullable String tagToJsonString(@Nullable final Tag tag) {
        try {
            final ATextComponent component = SerializerVersion.V1_20_5.toComponent(tag);
            return component != null ? SerializerVersion.V1_20_3.toString(component) : null;
        } catch (final Exception e) {
            Via.getPlatform().getLogger().log(Level.SEVERE, "Error converting tag: " + tag, e);
            return plainToJson("<error>").toString();
        }
    }

    public static @Nullable Tag jsonStringToTag(@Nullable final String json) {
        return jsonStringToTag(json, SerializerVersion.V1_20_3, SerializerVersion.V1_20_5);
    }

    public static @Nullable Tag jsonStringToTag(@Nullable final String json, final SerializerVersion from, final SerializerVersion to) {
        if (json == null) {
            return null;
        }
        return to.toTag(from.jsonSerializer.deserialize(json));
    }

    public static @Nullable JsonElement convertJson(@Nullable final JsonElement element, final SerializerVersion from, final SerializerVersion to) {
        return element != null ? convert(from, to, from.toComponent(element)) : null;
    }

    public static @Nullable JsonElement convertJson(@Nullable final String json, final SerializerVersion from, final SerializerVersion to) {
        return json != null ? convert(from, to, from.toComponent(json)) : null;
    }

    public static @Nullable JsonElement convertJsonOrEmpty(@Nullable final String json, final SerializerVersion from, final SerializerVersion to) {
        final ATextComponent component = from.toComponent(json);
        if (component == null) {
            return emptyJsonComponent();
        }
        return to.toJson(component);
    }

    private static JsonElement convert(final SerializerVersion from, final SerializerVersion to, final ATextComponent component) {
        if (from.ordinal() >= SerializerVersion.V1_16.ordinal() && to.ordinal() < SerializerVersion.V1_16.ordinal()) {
            // Convert hover event to legacy format
            final Style style = component.getStyle();
            final AHoverEvent hoverEvent = style.getHoverEvent();
            if (hoverEvent != null && !(hoverEvent instanceof TextHoverEvent)) {
                style.setHoverEvent(hoverEvent.toLegacy(to.jsonSerializer, to.snbtSerializer));
            }
        }
        return to.toJson(component);
    }

    public static JsonElement legacyToJson(final String message) {
        return SerializerVersion.V1_12.toJson(LegacyStringDeserializer.parse(message, true));
    }

    public static String legacyToJsonString(final String message) {
        return legacyToJsonString(message, false);
    }

    public static String legacyToJsonString(final String message, final boolean itemData) {
        final ATextComponent component = LegacyStringDeserializer.parse(message, true);
        if (itemData) {
            component.setParentStyle(new Style().setItalic(false));
        }
        return SerializerVersion.V1_12.toString(component);
    }

    public static String jsonToLegacy(final String value) {
        return TextComponentSerializer.V1_12.deserializeReader(value).asLegacyFormatString();
    }

    public static String jsonToLegacy(final JsonElement value) {
        return SerializerVersion.V1_12.toComponent(value).asLegacyFormatString();
    }

    public static CompoundTag deserializeLegacyShowItem(final JsonElement element, final SerializerVersion version) {
        return (CompoundTag) version.toTag(version.toComponent(element).asUnformattedString());
    }

    public static CompoundTag deserializeShowItem(final Tag value, final SerializerVersion version) {
        return (CompoundTag) version.toTag(version.toComponent(value).asUnformattedString());
    }
}

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

import com.github.steveice10.opennbt.tag.builtin.StringTag;
import com.github.steveice10.opennbt.tag.builtin.Tag;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.viaversion.viaversion.api.Via;
import java.util.logging.Level;
import net.lenni0451.mcstructs.snbt.SNbtSerializer;
import net.lenni0451.mcstructs.text.ATextComponent;
import net.lenni0451.mcstructs.text.Style;
import net.lenni0451.mcstructs.text.events.hover.AHoverEvent;
import net.lenni0451.mcstructs.text.events.hover.impl.TextHoverEvent;
import net.lenni0451.mcstructs.text.serializer.LegacyStringDeserializer;
import net.lenni0451.mcstructs.text.serializer.TextComponentCodec;
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
            final ATextComponent component = TextComponentCodec.V1_20_3.deserializeNbtTree(tag);
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
            final ATextComponent component = TextComponentSerializer.V1_19_4.deserialize(element);
            return trimStrings(TextComponentCodec.V1_20_3.serializeNbt(component));
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

    public static @Nullable JsonElement convertJson(@Nullable final JsonElement element, final SerializerVersion from, final SerializerVersion to) {
        return element != null ? convert(from, to, from.jsonSerializer.deserialize(element)) : null;
    }

    public static @Nullable JsonElement convertJson(@Nullable final String json, final SerializerVersion from, final SerializerVersion to) {
        return json != null ? convert(from, to, from.jsonSerializer.deserializeReader(json)) : null;
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
        return TextComponentSerializer.V1_12.serialize(component);
    }

    public static String jsonToLegacy(final String value) {
        return TextComponentSerializer.V1_12.deserializeReader(value).asLegacyFormatString();
    }

    public static String jsonToLegacy(final JsonElement value) {
        return TextComponentSerializer.V1_12.deserialize(value).asLegacyFormatString();
    }

    public enum SerializerVersion {
        V1_8(TextComponentSerializer.V1_8, SNbtSerializer.V1_8),
        V1_9(TextComponentSerializer.V1_9, SNbtSerializer.V1_8),
        V1_12(TextComponentSerializer.V1_12, SNbtSerializer.V1_12),
        V1_14(TextComponentSerializer.V1_14, SNbtSerializer.V1_14),
        V1_15(TextComponentSerializer.V1_15, SNbtSerializer.V1_14),
        V1_16(TextComponentSerializer.V1_16, SNbtSerializer.V1_14),
        V1_17(TextComponentSerializer.V1_17, SNbtSerializer.V1_14),
        V1_18(TextComponentSerializer.V1_18, SNbtSerializer.V1_14),
        V1_19_4(TextComponentSerializer.V1_19_4, SNbtSerializer.V1_14),
        V1_20_3(TextComponentCodec.V1_20_3, SNbtSerializer.V1_14);

        private final TextComponentSerializer jsonSerializer;
        private final SNbtSerializer<?> snbtSerializer;
        private final TextComponentCodec codec;

        SerializerVersion(final TextComponentSerializer jsonSerializer, final SNbtSerializer<?> snbtSerializer) {
            this.jsonSerializer = jsonSerializer;
            this.snbtSerializer = snbtSerializer;
            this.codec = null;
        }

        SerializerVersion(final TextComponentCodec codec, final SNbtSerializer<?> snbtSerializer) {
            this.codec = codec;
            this.jsonSerializer = codec.asSerializer();
            this.snbtSerializer = snbtSerializer;
        }

        public JsonElement toJson(final ATextComponent component) {
            return jsonSerializer.serializeJson(component);
        }
    }
}

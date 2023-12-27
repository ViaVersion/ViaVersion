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

import com.github.steveice10.opennbt.tag.builtin.Tag;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.lenni0451.mcstructs.text.ATextComponent;
import net.lenni0451.mcstructs.text.Style;
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
        final ATextComponent component = TextComponentCodec.V1_20_3.deserializeNbtTree(NBTConverter.viaToMcStructs(tag));
        return component != null ? TextComponentSerializer.V1_19_4.serializeJson(component) : null;
    }

    public static @Nullable Tag jsonToTag(@Nullable final JsonElement element) {
        final ATextComponent component = TextComponentSerializer.V1_19_4.deserialize(element);
        return component != null ? NBTConverter.mcStructsToVia(TextComponentCodec.V1_20_3.serializeNbt(component)) : null;
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
        return TextComponentSerializer.V1_12.deserialize(value).asLegacyFormatString();
    }

    public static String jsonToLegacy(final JsonElement value) {
        return TextComponentSerializer.V1_12.deserialize(value).asLegacyFormatString();
    }

    public enum SerializerVersion {
        V1_8(TextComponentSerializer.V1_8),
        V1_9(TextComponentSerializer.V1_9),
        V1_12(TextComponentSerializer.V1_12),
        V1_14(TextComponentSerializer.V1_14),
        V1_15(TextComponentSerializer.V1_15),
        V1_16(TextComponentSerializer.V1_16),
        V1_17(TextComponentSerializer.V1_17),
        V1_18(TextComponentSerializer.V1_18),
        V1_19_4(TextComponentSerializer.V1_19_4),
        V1_20_3(TextComponentCodec.V1_20_3);

        private final TextComponentSerializer serializer;
        private final TextComponentCodec codec;

        SerializerVersion(final TextComponentSerializer serializer) {
            this.serializer = serializer;
            this.codec = null;
        }

        SerializerVersion(final TextComponentCodec codec) {
            this.serializer = codec.asSerializer();
            this.codec = codec;
        }

        public TextComponentSerializer serializer() {
            return serializer;
        }

        public @Nullable TextComponentCodec codec() {
            return codec;
        }
    }
}

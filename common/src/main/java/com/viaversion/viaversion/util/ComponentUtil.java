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
package com.viaversion.viaversion.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.Via;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import net.lenni0451.mcstructs.text.Style;
import net.lenni0451.mcstructs.text.TextComponent;
import net.lenni0451.mcstructs.text.components.StringComponent;
import net.lenni0451.mcstructs.text.events.hover.HoverEvent;
import net.lenni0451.mcstructs.text.events.hover.impl.EntityHoverEvent;
import net.lenni0451.mcstructs.text.events.hover.impl.ItemHoverEvent;
import net.lenni0451.mcstructs.text.serializer.TextComponentSerializer;
import net.lenni0451.mcstructs.text.stringformat.StringFormat;
import net.lenni0451.mcstructs.text.stringformat.handling.ColorHandling;
import net.lenni0451.mcstructs.text.stringformat.handling.DeserializerUnknownHandling;
import net.lenni0451.mcstructs.text.utils.TextUtils;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Component conversion utility, trying to divert most calls to the component library to this class instead for easy replacement.
 */
public final class ComponentUtil {

    private static final int MAX_UNSIGNED_SHORT = 65535;

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
            final TextComponent component = SerializerVersion.V1_20_3.toComponent(tag);
            return component != null ? SerializerVersion.V1_19_4.toJson(component) : null;
        } catch (final Exception e) {
            if (!Via.getConfig().isSuppressConversionWarnings()) {
                Via.getPlatform().getLogger().log(Level.SEVERE, "Error converting tag: " + tag, e);
            }
            return plainToJson("<error>");
        }
    }

    public static @Nullable Tag jsonToTag(@Nullable final JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return null;
        }

        try {
            final TextComponent component = SerializerVersion.V1_19_4.toComponent(element);
            return trimStrings(SerializerVersion.V1_20_3.toTag(component));
        } catch (final Exception e) {
            if (!Via.getConfig().isSuppressConversionWarnings()) {
                Via.getPlatform().getLogger().log(Level.SEVERE, "Error converting component: " + element, e);
            }
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
            if (tag instanceof StringTag stringTag) {
                final byte[] value = stringTag.getValue().getBytes(StandardCharsets.UTF_8);
                if (value.length > MAX_UNSIGNED_SHORT) {
                    stringTag.setValue("{}");
                }
            }
            return tag;
        });
    }

    public static @Nullable String tagToJsonString(@Nullable final Tag tag) {
        try {
            final TextComponent component = SerializerVersion.V1_20_5.toComponent(tag);
            return component != null ? SerializerVersion.V1_20_3.toString(component) : null;
        } catch (final Exception e) {
            if (!Via.getConfig().isSuppressConversionWarnings()) {
                Via.getPlatform().getLogger().log(Level.SEVERE, "Error converting tag: " + tag, e);
            }
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
        final TextComponent component = from.toComponent(json);
        if (component == null) {
            return emptyJsonComponent();
        }
        return to.toJson(component);
    }

    private static JsonElement convert(final SerializerVersion from, final SerializerVersion to, final TextComponent component) {
        if (from.ordinal() >= SerializerVersion.V1_16.ordinal() && to.ordinal() < SerializerVersion.V1_16.ordinal()) {
            // Convert hover event to legacy format
            final Style style = component.getStyle();
            final HoverEvent hoverEvent = style.getHoverEvent();
            if (hoverEvent instanceof EntityHoverEvent entityHoverEvent && entityHoverEvent.isModern()) {
                final EntityHoverEvent.ModernHolder entityData = entityHoverEvent.asModern();
                final CompoundTag tag = new CompoundTag();
                tag.putString("type", entityData.getType().get());
                tag.putString("id", entityData.getUuid().toString());
                tag.putString("name", to.toString(entityData.getName() != null ? entityData.getName() : new StringComponent("")));
                entityHoverEvent.setLegacyData(new StringComponent(to.toSNBT(tag)));
            } else if (hoverEvent instanceof ItemHoverEvent itemHoverEvent && itemHoverEvent.isModern()) {
                final ItemHoverEvent.ModernHolder itemData = itemHoverEvent.asModern();
                final CompoundTag tag = new CompoundTag();
                tag.putString("id", itemData.getId().get());
                tag.putByte("Count", (byte) itemData.getCount());
                if (itemData.getTag() != null) {
                    tag.put("tag", itemData.getTag());
                }
                itemHoverEvent.setLegacyData(to.toSNBT(tag));
            }
        }
        return to.toJson(component);
    }

    public static JsonElement legacyToJson(final String message) {
        return SerializerVersion.V1_12.toJson(StringFormat.vanilla().fromString(message, ColorHandling.RESET, DeserializerUnknownHandling.WHITE));
    }

    public static String legacyToJsonString(final String message) {
        return legacyToJsonString(message, false);
    }

    public static String legacyToJsonString(final String message, final boolean itemData) {
        final TextComponent component = StringFormat.vanilla().fromString(message, ColorHandling.RESET, DeserializerUnknownHandling.WHITE);
        if (itemData) {
            TextUtils.iterateAll(component, c -> {
                if (!c.getStyle().isEmpty()) {
                    c.setParentStyle(new Style().setItalic(false));
                }
            });
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

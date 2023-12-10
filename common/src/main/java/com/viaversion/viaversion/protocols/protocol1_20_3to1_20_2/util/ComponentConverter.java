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
package com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2.util;

import com.github.steveice10.opennbt.tag.builtin.ByteArrayTag;
import com.github.steveice10.opennbt.tag.builtin.ByteTag;
import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.DoubleTag;
import com.github.steveice10.opennbt.tag.builtin.FloatTag;
import com.github.steveice10.opennbt.tag.builtin.IntArrayTag;
import com.github.steveice10.opennbt.tag.builtin.IntTag;
import com.github.steveice10.opennbt.tag.builtin.ListTag;
import com.github.steveice10.opennbt.tag.builtin.LongArrayTag;
import com.github.steveice10.opennbt.tag.builtin.LongTag;
import com.github.steveice10.opennbt.tag.builtin.NumberTag;
import com.github.steveice10.opennbt.tag.builtin.ShortTag;
import com.github.steveice10.opennbt.tag.builtin.StringTag;
import com.github.steveice10.opennbt.tag.builtin.Tag;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.internal.LazilyParsedNumber;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.util.Pair;
import com.viaversion.viaversion.util.UUIDUtil;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class ComponentConverter {

    private static final Set<String> BOOLEAN_TYPES = new HashSet<>(Arrays.asList(
            "interpret",
            "bold",
            "italic",
            "underlined",
            "strikethrough",
            "obfuscated"
    ));
    // Order is important
    private static final List<Pair<String, String>> COMPONENT_TYPES = Arrays.asList(
            new Pair<>("text", "text"),
            new Pair<>("translatable", "translate"),
            new Pair<>("score", "score"),
            new Pair<>("selector", "selector"),
            new Pair<>("keybind", "keybind"),
            new Pair<>("nbt", "nbt")
    );

    public static @Nullable JsonElement tagComponentToJson(@Nullable final Tag tag) {
        try {
            return convertToJson(null, tag);
        } catch (final Exception e) {
            Via.getPlatform().getLogger().log(Level.SEVERE, "Error converting component: " + tag, e);
            return new JsonPrimitive("<error>");
        }
    }

    public static @Nullable Tag jsonComponentToTag(@Nullable final JsonElement component) {
        try {
            return convertToTag(component);
        } catch (final Exception e) {
            Via.getPlatform().getLogger().log(Level.SEVERE, "Error converting component: " + component, e);
            return new StringTag("<error>");
        }
    }

    private static @Nullable Tag convertToTag(final @Nullable JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return null;
        } else if (element.isJsonObject()) {
            final CompoundTag tag = new CompoundTag();
            final JsonObject jsonObject = element.getAsJsonObject();
            for (final Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                convertObjectEntry(entry.getKey(), entry.getValue(), tag);
            }

            addComponentType(jsonObject, tag);
            return tag;
        } else if (element.isJsonArray()) {
            return convertJsonArray(element.getAsJsonArray());
        } else if (element.isJsonPrimitive()) {
            final JsonPrimitive primitive = element.getAsJsonPrimitive();
            if (primitive.isString()) {
                return new StringTag(primitive.getAsString());
            } else if (primitive.isBoolean()) {
                return new ByteTag((byte) (primitive.getAsBoolean() ? 1 : 0));
            }

            final Number number = primitive.getAsNumber();
            if (number instanceof Integer) {
                return new IntTag(number.intValue());
            } else if (number instanceof Byte) {
                return new ByteTag(number.byteValue());
            } else if (number instanceof Short) {
                return new ShortTag(number.shortValue());
            } else if (number instanceof Long) {
                return new LongTag(number.longValue());
            } else if (number instanceof Double) {
                return new DoubleTag(number.doubleValue());
            } else if (number instanceof Float) {
                return new FloatTag(number.floatValue());
            } else if (number instanceof LazilyParsedNumber) {
                // TODO: This might need better handling
                return new IntTag(number.intValue());
            }
            return new IntTag(number.intValue()); // ???
        }
        throw new IllegalArgumentException("Unhandled json type " + element.getClass().getSimpleName() + " with value " + element.getAsString());
    }

    private static ListTag convertJsonArray(final JsonArray array) {
        // TODO Number arrays?
        final ListTag listTag = new ListTag();
        boolean singleType = true;
        for (final JsonElement entry : array) {
            final Tag convertedEntryTag = convertToTag(entry);
            if (listTag.getElementType() != null && listTag.getElementType() != convertedEntryTag.getClass()) {
                singleType = false;
                break;
            }

            listTag.add(convertedEntryTag);
        }

        if (singleType) {
            return listTag;
        }

        // Generally, vanilla-esque serializers should not produce this format, so it should be rare
        // Lists are only used for lists of components ("extra" and "with")
        final ListTag processedListTag = new ListTag();
        for (final JsonElement entry : array) {
            final Tag convertedTag = convertToTag(entry);
            if (convertedTag instanceof CompoundTag) {
                processedListTag.add(convertedTag);
                continue;
            }

            // Wrap all entries in compound tags, as lists can only consist of one type of tag
            final CompoundTag compoundTag = new CompoundTag();
            compoundTag.put("type", new StringTag("text"));
            if (convertedTag instanceof ListTag) {
                compoundTag.put("text", new StringTag());
                compoundTag.put("extra", convertedTag);
            } else {
                compoundTag.put("text", new StringTag(convertedTag.asRawString()));
            }
            processedListTag.add(compoundTag);
        }
        return processedListTag;
    }

    /**
     * Converts a json object entry to a tag entry.
     *
     * @param key   key of the entry
     * @param value value of the entry
     * @param tag   the resulting compound tag
     */
    private static void convertObjectEntry(final String key, final JsonElement value, final CompoundTag tag) {
        if ((key.equals("contents")) && value.isJsonObject()) {
            // Store show_entity id as int array instead of uuid string
            // Not really required, but we might as well make it more compact
            final JsonObject hoverEvent = value.getAsJsonObject();
            final JsonElement id = hoverEvent.get("id");
            final UUID uuid;
            if (id != null && id.isJsonPrimitive() && (uuid = UUIDUtil.parseUUID(id.getAsString())) != null) {
                hoverEvent.remove("id");

                final CompoundTag convertedTag = (CompoundTag) convertToTag(value);
                convertedTag.put("id", new IntArrayTag(UUIDUtil.toIntArray(uuid)));
                tag.put(key, convertedTag);
                return;
            }
        }

        tag.put(key, convertToTag(value));
    }

    private static void addComponentType(final JsonObject object, final CompoundTag tag) {
        if (object.has("type")) {
            return;
        }

        // Add the type to speed up deserialization and make DFU errors slightly more useful
        for (final Pair<String, String> pair : COMPONENT_TYPES) {
            if (object.has(pair.value())) {
                tag.put("type", new StringTag(pair.key()));
                return;
            }
        }
    }

    private static @Nullable JsonElement convertToJson(final @Nullable String key, final @Nullable Tag tag) {
        if (tag == null) {
            return null;
        } else if (tag instanceof CompoundTag) {
            final JsonObject object = new JsonObject();
            if (!"value".equals(key)) {
                removeComponentType(object);
            }

            for (final Map.Entry<String, Tag> entry : ((CompoundTag) tag).entrySet()) {
                convertCompoundTagEntry(entry.getKey(), entry.getValue(), object);
            }
            return object;
        } else if (tag instanceof ListTag) {
            final ListTag list = (ListTag) tag;
            final JsonArray array = new JsonArray();
            for (final Tag listEntry : list) {
                array.add(convertToJson(null, listEntry));
            }
            return array;
        } else if (tag instanceof NumberTag) {
            final NumberTag numberTag = (NumberTag) tag;
            if (key != null && BOOLEAN_TYPES.contains(key)) {
                // Booleans don't have a direct representation in nbt
                return new JsonPrimitive(numberTag.asBoolean());
            }
            return new JsonPrimitive(numberTag.getValue());
        } else if (tag instanceof StringTag) {
            return new JsonPrimitive(((StringTag) tag).getValue());
        } else if (tag instanceof ByteArrayTag) {
            final ByteArrayTag arrayTag = (ByteArrayTag) tag;
            final JsonArray array = new JsonArray();
            for (final byte num : arrayTag.getValue()) {
                array.add(num);
            }
            return array;
        } else if (tag instanceof IntArrayTag) {
            final IntArrayTag arrayTag = (IntArrayTag) tag;
            final JsonArray array = new JsonArray();
            for (final int num : arrayTag.getValue()) {
                array.add(num);
            }
            return array;
        } else if (tag instanceof LongArrayTag) {
            final LongArrayTag arrayTag = (LongArrayTag) tag;
            final JsonArray array = new JsonArray();
            for (final long num : arrayTag.getValue()) {
                array.add(num);
            }
            return array;
        }
        throw new IllegalArgumentException("Unhandled tag type " + tag.getClass().getSimpleName());
    }

    private static void convertCompoundTagEntry(final String key, final Tag tag, final JsonObject object) {
        if ((key.equals("contents")) && tag instanceof CompoundTag) {
            // Back to a UUID string
            final CompoundTag showEntity = (CompoundTag) tag;
            final Tag idTag = showEntity.get("id");
            if (idTag instanceof IntArrayTag) {
                showEntity.remove("id");

                final JsonObject convertedElement = (JsonObject) convertToJson(key, tag);
                final UUID uuid = UUIDUtil.fromIntArray(((IntArrayTag) idTag).getValue());
                convertedElement.addProperty("id", uuid.toString());
                object.add(key, convertedElement);
                return;
            }
        }

        // "":1 is a valid tag, but not a valid json component
        object.add(key.isEmpty() ? "text" : key, convertToJson(key, tag));
    }

    private static void removeComponentType(final JsonObject object) {
        final JsonElement type = object.remove("type");
        if (!type.isJsonPrimitive()) {
            return;
        }

        // Remove the other fields
        final String typeString = type.getAsString();
        for (final Pair<String, String> pair : COMPONENT_TYPES) {
            if (!pair.key().equals(typeString)) {
                object.remove(pair.value());
            }
        }
    }
}

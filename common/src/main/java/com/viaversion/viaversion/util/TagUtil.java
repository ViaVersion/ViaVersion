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

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.NumberTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.nbt.tag.Tag;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class TagUtil {

    public static ListTag<CompoundTag> getRegistryEntries(final CompoundTag tag, final String key) {
        return getRegistryEntries(tag, key, null);
    }

    public static ListTag<CompoundTag> getRegistryEntries(final CompoundTag tag, final String key, final @Nullable ListTag<CompoundTag> defaultValue) {
        CompoundTag registry = getNamespacedCompoundTag(tag, key);
        if (registry == null) {
            return defaultValue;
        }
        return registry.getListTag("value", CompoundTag.class);
    }

    public static ListTag<CompoundTag> removeRegistryEntries(final CompoundTag tag, final String key) {
        return removeRegistryEntries(tag, key, null);
    }

    public static ListTag<CompoundTag> removeRegistryEntries(final CompoundTag tag, final String key, final @Nullable ListTag<CompoundTag> defaultValue) {
        final String actualKey = getNamespacedTagKey(tag, key);
        final CompoundTag registry = tag.getCompoundTag(actualKey);
        if (registry == null) {
            return defaultValue;
        }
        tag.remove(actualKey);
        return registry.getListTag("value", CompoundTag.class);
    }

    public static boolean removeNamespaced(final CompoundTag tag, final String key) {
        return tag.remove(Key.namespaced(key)) != null || tag.remove(Key.stripMinecraftNamespace(key)) != null;
    }

    public static boolean containsNamespaced(final CompoundTag tag, final String key) {
        return tag.contains(Key.namespaced(key)) || tag.contains(Key.stripMinecraftNamespace(key));
    }

    public static String getNamespacedTagKey(final CompoundTag tag, final String name) {
        return tag.contains(Key.namespaced(name)) ? Key.namespaced(name) : Key.stripMinecraftNamespace(name);
    }

    public static @Nullable Tag getNamespacedTag(final CompoundTag tag, final String key) {
        final Tag value = tag.get(Key.namespaced(key));
        return value != null ? value : tag.get(Key.stripMinecraftNamespace(key));
    }

    public static @Nullable CompoundTag getNamespacedCompoundTag(final CompoundTag tag, final String key) {
        final CompoundTag compoundTag = tag.getCompoundTag(Key.namespaced(key));
        return compoundTag != null ? compoundTag : tag.getCompoundTag(Key.stripMinecraftNamespace(key));
    }

    public static @Nullable ListTag<CompoundTag> getNamespacedCompoundTagList(final CompoundTag tag, final String key) {
        final ListTag<CompoundTag> listTag = tag.getListTag(Key.namespaced(key), CompoundTag.class);
        return listTag != null ? listTag : tag.getListTag(Key.stripMinecraftNamespace(key), CompoundTag.class);
    }

    public static @Nullable StringTag getNamespacedStringTag(final CompoundTag tag, final String key) {
        final StringTag stringTag = tag.getStringTag(Key.namespaced(key));
        return stringTag != null ? stringTag : tag.getStringTag(Key.stripMinecraftNamespace(key));
    }

    public static @Nullable NumberTag getNamespacedNumberTag(final CompoundTag tag, final String key) {
        final NumberTag numberTag = tag.getNumberTag(Key.namespaced(key));
        return numberTag != null ? numberTag : tag.getNumberTag(Key.stripMinecraftNamespace(key));
    }

    public static Tag handleDeep(final Tag tag, final TagUpdater consumer) {
        return handleDeep(null, tag, consumer);
    }

    private static Tag handleDeep(@Nullable final String key, final Tag tag, final TagUpdater consumer) {
        if (tag instanceof final CompoundTag compoundTag) {
            for (final Map.Entry<String, Tag> entry : compoundTag.entrySet()) {
                final Tag updatedTag = handleDeep(entry.getKey(), entry.getValue(), consumer);
                entry.setValue(updatedTag);
            }
        } else if (tag instanceof ListTag<?> listTag) {
            handleListTag(listTag, consumer);
        }
        return consumer.update(key, tag);
    }

    private static <T extends Tag> void handleListTag(final ListTag<T> listTag, final TagUpdater consumer) {
        listTag.getValue().replaceAll(t -> (T) handleDeep(null, t, consumer));
    }

    @FunctionalInterface
    public interface TagUpdater {

        /**
         * Updates the given tag.
         *
         * @param key key of the tag if inside a CompoundTag
         * @param tag the tag to update
         * @return the updated tag, can be the original one
         */
        Tag update(@Nullable String key, Tag tag);
    }
}

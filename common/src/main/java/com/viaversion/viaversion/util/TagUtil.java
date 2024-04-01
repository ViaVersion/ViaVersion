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
import com.github.steveice10.opennbt.tag.builtin.ListTag;
import com.github.steveice10.opennbt.tag.builtin.Tag;
import java.util.Map;
import net.lenni0451.mcstructs.snbt.exceptions.SNbtDeserializeException;
import net.lenni0451.mcstructs.snbt.exceptions.SNbtSerializeException;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class TagUtil {

    public static Tag handleDeep(final Tag tag, final TagUpdater consumer) {
        return handleDeep(null, tag, consumer);
    }

    private static Tag handleDeep(@Nullable final String key, final Tag tag, final TagUpdater consumer) {
        if (tag instanceof CompoundTag) {
            final CompoundTag compoundTag = (CompoundTag) tag;
            for (final Map.Entry<String, Tag> entry : compoundTag.entrySet()) {
                final Tag updatedTag = handleDeep(entry.getKey(), entry.getValue(), consumer);
                entry.setValue(updatedTag);
            }
        } else if (tag instanceof ListTag) {
            handleListTag((ListTag<?>) tag, consumer);
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

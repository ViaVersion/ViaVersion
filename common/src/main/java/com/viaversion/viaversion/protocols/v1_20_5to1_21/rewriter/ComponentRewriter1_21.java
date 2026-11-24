/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2026 ViaVersion and contributors
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
package com.viaversion.viaversion.protocols.v1_20_5to1_21.rewriter;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.IntArrayTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.NumberArrayTag;
import com.viaversion.nbt.tag.NumberTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ClientboundPacket1_20_5;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.Protocol1_20_5To1_21;
import com.viaversion.viaversion.rewriter.text.JsonNBTComponentRewriter;
import com.viaversion.viaversion.util.Key;
import com.viaversion.viaversion.util.SerializerVersion;
import com.viaversion.viaversion.util.TagUtil;
import com.viaversion.viaversion.util.UUIDUtil;
import java.util.UUID;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class ComponentRewriter1_21 extends JsonNBTComponentRewriter<ClientboundPacket1_20_5> {

    private static final UUID ZERO_UUID = new UUID(0, 0);

    public ComponentRewriter1_21(final Protocol1_20_5To1_21 protocol) {
        super(protocol, ReadType.NBT);
    }

    @Override
    protected void handleShowItem(final UserConnection connection, final CompoundTag itemTag, CompoundTag componentsTag) {
        super.handleShowItem(connection, itemTag, componentsTag);
        final String identifier = Key.stripMinecraftNamespace(itemTag.getString("id"));
        if (identifier.equals("trident") || identifier.equals("piglin_banner_pattern")) {
            if (componentsTag == null) {
                itemTag.put("components", componentsTag = new CompoundTag());
            }
            if (!TagUtil.containsNamespaced(componentsTag, "rarity")) {
                componentsTag.put("minecraft:rarity", new StringTag("common"));
            }
        }

        if (componentsTag == null) {
            return;
        }

        final CompoundTag attributeModifiers = TagUtil.getNamespacedCompoundTag(componentsTag, "attribute_modifiers");
        if (attributeModifiers == null) {
            return;
        }
        final ListTag<CompoundTag> modifiers = attributeModifiers.getListTag("modifiers", CompoundTag.class);
        if (modifiers == null) {
            return;
        }

        for (final CompoundTag modifier : modifiers) {
            final String name = modifier.getString("name");
            final UUID uuid = modifierUuid(modifier.get("uuid"));
            final String id = Protocol1_20_5To1_21.mapAttributeUUID(uuid, name);
            modifier.putString("id", id);
        }
    }

    private static UUID modifierUuid(@Nullable final Tag tag) {
        if (tag instanceof final IntArrayTag intArrayTag && intArrayTag.length() == 4) {
            return UUIDUtil.fromIntArray(intArrayTag.getValue());
        }

        // Components converted from json can have the values in other number arrays or lists, possibly wrapped in compounds
        final ListTag<?> values = tag instanceof final NumberArrayTag arrayTag ? arrayTag.toListTag() : tag instanceof final ListTag<?> listTag ? listTag : null;
        if (values == null || values.size() != 4) {
            return ZERO_UUID;
        }

        final int[] parts = new int[4];
        for (int i = 0; i < parts.length; i++) {
            final Tag value = values.get(i) instanceof final CompoundTag wrapper ? wrapper.get("") : values.get(i);
            if (!(value instanceof final NumberTag numberTag)) {
                return ZERO_UUID;
            }
            parts[i] = numberTag.asInt();
        }
        return UUIDUtil.fromIntArray(parts);
    }

    @Override
    protected SerializerVersion inputSerializerVersion() {
        return SerializerVersion.V1_20_5;
    }
}

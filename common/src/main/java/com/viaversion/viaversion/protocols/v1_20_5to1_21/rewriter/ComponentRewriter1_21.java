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
package com.viaversion.viaversion.protocols.v1_20_5to1_21.rewriter;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ClientboundPacket1_20_5;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.Protocol1_20_5To1_21;
import com.viaversion.viaversion.rewriter.ComponentRewriter;
import com.viaversion.viaversion.util.Key;
import com.viaversion.viaversion.util.SerializerVersion;
import com.viaversion.viaversion.util.TagUtil;
import com.viaversion.viaversion.util.UUIDUtil;
import java.util.UUID;

public final class ComponentRewriter1_21 extends ComponentRewriter<ClientboundPacket1_20_5> {

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
        for (final CompoundTag modifier : modifiers) {
            final String name = modifier.getString("name");
            final UUID uuid = UUIDUtil.fromIntArray(modifier.getIntArrayTag("uuid").getValue());
            final String id = Protocol1_20_5To1_21.mapAttributeUUID(uuid, name);
            modifier.putString("id", id);
        }
    }

    @Override
    protected SerializerVersion inputSerializerVersion() {
        return SerializerVersion.V1_20_5;
    }
}

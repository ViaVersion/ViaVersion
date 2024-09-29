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
package com.viaversion.viaversion.protocols.v1_21to1_21_2.rewriter;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.FullMappings;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundPacket1_21;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.Protocol1_21To1_21_2;
import com.viaversion.viaversion.rewriter.ComponentRewriter;
import com.viaversion.viaversion.util.SerializerVersion;
import com.viaversion.viaversion.util.TagUtil;
import java.util.Iterator;

public final class ComponentRewriter1_21_2 extends ComponentRewriter<ClientboundPacket1_21> {

    public ComponentRewriter1_21_2(final Protocol1_21To1_21_2 protocol) {
        super(protocol, ReadType.NBT);
    }

    @Override
    protected void handleShowItem(final UserConnection connection, final CompoundTag itemTag, final CompoundTag componentsTag) {
        super.handleShowItem(connection, itemTag, componentsTag);
        if (componentsTag == null) {
            return;
        }

        convertAttributes(componentsTag, protocol.getMappingData().getAttributeMappings());

        final CompoundTag instrument = TagUtil.getNamespacedCompoundTag(componentsTag, "instrument");
        if (instrument != null) {
            instrument.putString("description", "");
        }

        final CompoundTag food = TagUtil.getNamespacedCompoundTag(componentsTag, "food");
        if (food != null) {
            final CompoundTag convertsTo = food.getCompoundTag("using_converts_to");
            if (convertsTo != null) {
                food.remove("using_converts_to");
                componentsTag.put("minecraft:use_remainder", convertsTo);
            }
            food.remove("eat_seconds");
            food.remove("effects");
        }

        TagUtil.removeNamespaced(componentsTag, "fire_resistant");
        TagUtil.removeNamespaced(componentsTag, "lock");
    }

    public static void convertAttributes(final CompoundTag componentsTag, final FullMappings mappings) {
        final CompoundTag attributeModifiers = TagUtil.getNamespacedCompoundTag(componentsTag, "attribute_modifiers");
        if (attributeModifiers == null) {
            return;
        }

        final ListTag<CompoundTag> modifiers = attributeModifiers.getListTag("modifiers", CompoundTag.class);
        final Iterator<CompoundTag> iterator = modifiers.iterator();
        while (iterator.hasNext()) {
            final CompoundTag modifier = iterator.next();
            final StringTag attribute = modifier.getStringTag("type");
            final String mappedAttribute = mappings.mappedIdentifier(attribute.getValue());
            if (mappedAttribute != null) {
                attribute.setValue(mappedAttribute);
            } else {
                iterator.remove();
            }
        }
    }

    @Override
    protected SerializerVersion inputSerializerVersion() {
        return SerializerVersion.V1_20_5;
    }

    // Only cosmetic changes in the 1.21.2 serializer
}

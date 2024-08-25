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
package com.viaversion.viaversion.protocols.v1_20to1_20_2.storage;

import com.viaversion.viaversion.api.connection.StorableObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.TagData;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_20to1_20_2.Protocol1_20To1_20_2;
import com.viaversion.viaversion.protocols.v1_20to1_20_2.packet.ClientboundConfigurationPackets1_20_2;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LastTags implements StorableObject {

    private final List<RegistryTags> registryTags = new ArrayList<>();

    public LastTags(final PacketWrapper wrapper) {
        final int length = wrapper.passthrough(Types.VAR_INT);
        for (int i = 0; i < length; i++) {
            final List<TagData> tags = new ArrayList<>();
            final String registryKey = wrapper.passthrough(Types.STRING);
            final int tagsSize = wrapper.passthrough(Types.VAR_INT);
            for (int j = 0; j < tagsSize; j++) {
                final String key = wrapper.passthrough(Types.STRING);
                final int[] ids = wrapper.passthrough(Types.VAR_INT_ARRAY_PRIMITIVE);
                tags.add(new TagData(key, ids));
            }
            this.registryTags.add(new RegistryTags(registryKey, tags));
        }
    }

    public void sendLastTags(final UserConnection connection) {
        if (registryTags.isEmpty()) {
            return;
        }

        final PacketWrapper packet = PacketWrapper.create(ClientboundConfigurationPackets1_20_2.UPDATE_TAGS, connection);
        packet.write(Types.VAR_INT, registryTags.size());
        for (final RegistryTags registryTag : registryTags) {
            packet.write(Types.STRING, registryTag.registryKey);
            packet.write(Types.VAR_INT, registryTag.tags.size());
            for (final TagData tag : registryTag.tags) {
                packet.write(Types.STRING, tag.identifier());
                packet.write(Types.VAR_INT_ARRAY_PRIMITIVE, Arrays.copyOf(tag.entries(), tag.entries().length));
            }
        }
        packet.send(Protocol1_20To1_20_2.class);
    }

    private record RegistryTags(String registryKey, List<TagData> tags) {
    }
}

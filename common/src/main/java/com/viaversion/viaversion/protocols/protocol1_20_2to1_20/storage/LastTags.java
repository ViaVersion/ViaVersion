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
package com.viaversion.viaversion.protocols.protocol1_20_2to1_20.storage;

import com.viaversion.viaversion.api.connection.StorableObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_20_2to1_20.Protocol1_20_2To1_20;
import com.viaversion.viaversion.protocols.protocol1_20_2to1_20.packet.ClientboundConfigurationPackets1_20_2;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LastTags implements StorableObject {

    private final List<RegistryTags> registryTags = new ArrayList<>();

    public LastTags(final PacketWrapper wrapper) throws Exception {
        final int length = wrapper.passthrough(Type.VAR_INT);
        for (int i = 0; i < length; i++) {
            final List<Tag> tags = new ArrayList<>();
            final String registryKey = wrapper.passthrough(Type.STRING);
            final int tagsSize = wrapper.passthrough(Type.VAR_INT);
            for (int j = 0; j < tagsSize; j++) {
                final String key = wrapper.passthrough(Type.STRING);
                final int[] ids = wrapper.passthrough(Type.VAR_INT_ARRAY_PRIMITIVE);
                tags.add(new Tag(key, ids));
            }

            this.registryTags.add(new RegistryTags(registryKey, tags));
        }
    }

    public void sendLastTags(final UserConnection connection) throws Exception {
        if (registryTags.isEmpty()) {
            return;
        }

        final PacketWrapper packet = PacketWrapper.create(ClientboundConfigurationPackets1_20_2.UPDATE_TAGS, connection);
        packet.write(Type.VAR_INT, registryTags.size());
        for (final RegistryTags registryTag : registryTags) {
            packet.write(Type.STRING, registryTag.registryKey);
            packet.write(Type.VAR_INT, registryTag.tags.size());
            for (final Tag tag : registryTag.tags) {
                packet.write(Type.STRING, tag.key);
                packet.write(Type.VAR_INT_ARRAY_PRIMITIVE, Arrays.copyOf(tag.ids, tag.ids.length));
            }
        }
        packet.send(Protocol1_20_2To1_20.class);
    }

    private static final class RegistryTags {
        private final String registryKey;
        private final List<Tag> tags;

        private RegistryTags(final String registryKey, final List<Tag> tags) {
            this.registryKey = registryKey;
            this.tags = tags;
        }
    }

    private static final class Tag {
        private final String key;
        private final int[] ids;

        private Tag(final String key, final int[] ids) {
            this.key = key;
            this.ids = ids;
        }
    }
}

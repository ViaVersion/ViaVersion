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
package com.viaversion.viaversion.protocols.v1_20_3to1_20_5.storage;

import com.viaversion.viaversion.api.connection.StorableObject;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.util.Key;
import java.util.HashSet;
import java.util.Set;

public final class TagKeys implements StorableObject {

    private final Set<String> identifiers = new HashSet<>();

    public TagKeys(final PacketWrapper wrapper) {
        final int length = wrapper.passthrough(Types.VAR_INT);
        for (int i = 0; i < length; i++) {
            wrapper.passthrough(Types.STRING); // Registry key
            final int tagsSize = wrapper.passthrough(Types.VAR_INT);
            for (int j = 0; j < tagsSize; j++) {
                final String key = wrapper.passthrough(Types.STRING);
                wrapper.passthrough(Types.VAR_INT_ARRAY_PRIMITIVE); // Ids
                identifiers.add(Key.stripMinecraftNamespace(key));
            }
        }
    }

    public boolean isValidIdentifier(final String identifier) {
        return identifiers.contains(Key.stripMinecraftNamespace(identifier));
    }

}

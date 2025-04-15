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

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.FullMappings;
import com.viaversion.viaversion.api.protocol.Protocol;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface Rewritable {

    /**
     * Rewrites the object to a different version, may return self or a new object.
     *
     * @param connection  user connection
     * @param protocol    protocol
     * @param clientbound whether it should be rewritten client- or serverbound
     * @return rewritten object, may be (modified or unmodified) self or a new object
     */
    Object rewrite(UserConnection connection, Protocol<?, ?, ?, ?> protocol, boolean clientbound);

    static int rewriteDataComponentType(final Protocol<?, ?, ?, ?> protocol, final boolean clientbound, final int typeId) {
        final FullMappings mappings = protocol.getMappingData().getDataComponentSerializerMappings();
        return mappings == null ? typeId : (clientbound ? mappings.getNewId(typeId) : mappings.inverse().getNewId(typeId));
    }

    static int rewriteSound(final Protocol<?, ?, ?, ?> protocol, final boolean clientbound, final int soundId) {
        return protocol.getMappingData().getSoundMappings() == null ? soundId
            : (clientbound ? protocol.getMappingData().getNewSoundId(soundId) : protocol.getMappingData().getOldSoundId(soundId));
    }

    static int rewriteItem(final Protocol<?, ?, ?, ?> protocol, final boolean clientbound, final int itemId) {
        return protocol.getMappingData().getItemMappings() == null ? itemId
            : (clientbound ? protocol.getMappingData().getNewItemId(itemId) : protocol.getMappingData().getOldItemId(itemId));
    }

    static String rewriteItem(final Protocol<?, ?, ?, ?> protocol, final boolean clientbound, final String itemId) {
        final FullMappings mappings = protocol.getMappingData().getFullItemMappings();
        return mappings == null ? itemId : (clientbound ? mappedIdentifier(mappings, itemId) : unmappedIdentifier(mappings, itemId));
    }

    static Int2IntFunction itemRewriteFunction(final Protocol<?, ?, ?, ?> protocol, final boolean clientbound) {
        return protocol.getMappingData().getItemMappings() == null ? Int2IntFunction.identity()
            : (clientbound ? protocol.getMappingData()::getNewItemId : protocol.getMappingData()::getOldItemId);
    }

    static Int2IntFunction blockRewriteFunction(final Protocol<?, ?, ?, ?> protocol, final boolean clientbound) {
        return protocol.getMappingData().getBlockMappings() == null ? Int2IntFunction.identity()
            : (clientbound ? protocol.getMappingData()::getNewBlockId : protocol.getMappingData()::getOldBlockId);
    }

    static Int2IntFunction soundRewriteFunction(final Protocol<?, ?, ?, ?> protocol, final boolean clientbound) {
        return protocol.getMappingData().getSoundMappings() == null ? Int2IntFunction.identity()
            : (clientbound ? protocol.getMappingData()::getNewSoundId : protocol.getMappingData()::getOldSoundId);
    }

    static @Nullable String mappedIdentifier(final FullMappings mappings, final String identifier) {
        // Check if the original exists before mapping
        if (mappings.id(identifier) == -1) {
            return identifier;
        }
        return mappings.mappedIdentifier(identifier);
    }

    static @Nullable String unmappedIdentifier(final FullMappings mappings, final String mappedIdentifier) {
        if (mappings.mappedId(mappedIdentifier) == -1) {
            return mappedIdentifier;
        }
        return mappings.identifier(mappedIdentifier);
    }
}

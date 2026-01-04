/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2026 ViaVersion and contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
            : (clientbound ? protocol.getMappingData().getSoundMappings()::getNewId : protocol.getMappingData()::getOldSoundId);
    }

    static Int2IntFunction entityRewriteFunction(final Protocol<?, ?, ?, ?> protocol, final boolean clientbound) {
        return protocol.getMappingData().getEntityMappings() == null ? Int2IntFunction.identity()
            : (clientbound ? protocol.getMappingData().getEntityMappings()::getNewId : protocol.getMappingData().getEntityMappings().inverse()::getNewId);
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

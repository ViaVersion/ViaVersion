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
package com.viaversion.viaversion.codec;

import com.viaversion.viaversion.api.minecraft.codec.CodecContext;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.type.types.version.VersionedTypesHolder;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.List;
import java.util.Set;

public record CodecRegistryContext(Protocol<?, ?, ?, ?> protocol, RegistryAccess registryAccess,
                                   boolean mapped) implements CodecContext {

    // Generally from hardcoded, but highly variable client data
    private static final Set<StructuredDataKey<?>> NOT_IMPLEMENTED = new ReferenceOpenHashSet<>(List.of(
        StructuredDataKey.DAMAGE_TYPE
    ));

    public CodecRegistryContext(final Protocol<?, ?, ?, ?> protocol, final RegistryAccess registryAccess, final boolean mapped) {
        this.protocol = protocol;
        this.registryAccess = registryAccess.withMapped(mapped);
        this.mapped = mapped;
    }

    @Override
    public boolean isSupported(final StructuredDataKey<?> key) {
        if (protocol == null) { // For testing
            return !NOT_IMPLEMENTED.contains(key);
        }

        final VersionedTypesHolder types = mapped ? protocol.mappedTypes() : protocol.types();
        return (NOT_IMPLEMENTED.isEmpty() || !NOT_IMPLEMENTED.contains(key)) && types.structuredDataKeys().supportsOps(key);
    }
}

/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2025 ViaVersion and contributors
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
package com.viaversion.viaversion.api.minecraft.codec;

import com.google.common.base.Preconditions;
import com.viaversion.viaversion.api.data.FullMappings;
import com.viaversion.viaversion.api.data.MappingData;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.rewriter.RegistryDataRewriter;
import com.viaversion.viaversion.util.Key;
import com.viaversion.viaversion.util.KeyMappings;
import org.checkerframework.checker.nullness.qual.Nullable;

final class RegistryAccessImpl implements CodecContext.RegistryAccess {
    private final RegistryDataRewriter registryDataRewriter;
    private final MappingData mappingData;
    private final boolean mapped;

    RegistryAccessImpl(final Protocol<?, ?, ?, ?> protocol) {
        this.mappingData = protocol.getMappingData();
        this.registryDataRewriter = protocol.getRegistryDataRewriter();
        this.mapped = false;
    }

    private RegistryAccessImpl(final MappingData mappingData, final RegistryDataRewriter registryDataRewriter, final boolean mapped) {
        this.mappingData = mappingData;
        this.registryDataRewriter = registryDataRewriter;
        this.mapped = mapped;
    }

    @Override
    public Key item(final int id) {
        return key(mappingData.getFullItemMappings(), id);
    }

    @Override
    public Key attributeModifier(final int id) {
        return key(mappingData.getAttributeMappings(), id);
    }

    @Override
    public Key dataComponentType(final int id) {
        return key(mappingData.getDataComponentSerializerMappings(), id);
    }

    @Override
    public Key entity(final int id) {
        return key(mappingData.getEntityMappings(), id);
    }

    @Override
    public Key blockEntity(final int id) {
        return key(mappingData.getBlockEntityMappings(), id);
    }

    @Override
    public Key sound(final int id) {
        return key(mappingData.getFullSoundMappings(), id);
    }

    @Override
    public Key key(final MappingData.MappingType mappingType, final int id) {
        return key(mappingData.getFullMappings(mappingType), id);
    }

    @Override
    public int id(final MappingData.MappingType mappingType, final String identifier) {
        final FullMappings mappings = mappingData.getFullMappings(mappingType);
        return mapped ? mappings.mappedId(identifier) : mappings.id(identifier);
    }

    @Override
    public Key registryKey(final String registry, final int id) {
        final KeyMappings mappings = registryDataRewriter.getMappings(registry);
        Preconditions.checkNotNull(mappings, "No registry mappings for registry: " + registry);
        final String identifier = id >= 0 && id < mappings.size() ? mappings.idToKey(id) : null;
        return key(identifier, id);
    }

    private Key key(final FullMappings mappings, final int id) {
        final String identifier = mapped ? mappings.mappedIdentifier(id) : mappings.identifier(id);
        return key(identifier, id);
    }

    private Key key(@Nullable final String identifier, final int id) {
        return identifier != null ? Key.of(identifier) : Key.of("viaversion", "unknown/" + id);
    }

    @Override
    public CodecContext.RegistryAccess withMapped(final boolean mapped) {
        return this.mapped == mapped ? this : new RegistryAccessImpl(this.mappingData, this.registryDataRewriter, mapped);
    }
}

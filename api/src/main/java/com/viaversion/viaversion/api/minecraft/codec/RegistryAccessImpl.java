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

import com.viaversion.viaversion.api.data.FullMappings;
import com.viaversion.viaversion.api.data.MappingData;
import com.viaversion.viaversion.util.Key;
import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;

final class RegistryAccessImpl implements CodecContext.RegistryAccess {
    private final List<String> enchantments;
    private final MappingData mappingData;
    private final boolean mapped;

    RegistryAccessImpl(final List<String> enchantments, final MappingData mappingData) {
        this.enchantments = enchantments;
        this.mappingData = mappingData;
        this.mapped = false;
    }

    private RegistryAccessImpl(final List<String> enchantments, final MappingData mappingData, final boolean mapped) {
        this.enchantments = enchantments;
        this.mappingData = mappingData;
        this.mapped = mapped;
    }

    @Override
    public Key item(final int id) {
        return key(mappingData.getFullItemMappings(), id);
    }

    @Override
    public Key enchantment(final int id) {
        final String identifier = id >= 0 && id < this.enchantments.size() ? this.enchantments.get(id) : null;
        return key(identifier, id);
    }

    @Override
    public Key attributeModifier(final int id) {
        return key(mappingData.getAttributeMappings(), id);
    }

    @Override
    public Key dataComponentType(final int id) {
        return key(mappingData.getDataComponentSerializerMappings(), id);
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
        return this.mapped == mapped ? this : new RegistryAccessImpl(this.enchantments, this.mappingData, mapped);
    }
}

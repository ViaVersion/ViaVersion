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
package com.viaversion.viaversion.rewriter.item;

import com.viaversion.viaversion.api.data.FullMappings;
import com.viaversion.viaversion.api.data.MappingData;
import java.util.List;
import net.lenni0451.mcstructs.core.Identifier;
import net.lenni0451.mcstructs.itemcomponents.impl.Registries;
import net.lenni0451.mcstructs.itemcomponents.registry.Registry;
import net.lenni0451.mcstructs.itemcomponents.registry.RegistryEntry;
import org.checkerframework.checker.nullness.qual.Nullable;

final class RegistryAccessImpl implements ItemDataComponentConverter.RegistryAccess {
    private final List<String> enchantments;
    private final Registries registries;
    private final MappingData mappingData;

    RegistryAccessImpl(final List<String> enchantments, final Registries registries, final MappingData mappingData) {
        this.enchantments = enchantments;
        this.registries = registries;
        this.mappingData = mappingData;
    }

    @Override
    public RegistryEntry item(final int id, final boolean mapped) {
        return registryEntry(registries.item, mappingData.getFullItemMappings(), id, mapped);
    }

    @Override
    public RegistryEntry enchantment(final int id) {
        final String identifier = id >= 0 && id < this.enchantments.size() ? this.enchantments.get(id) : null;
        return new RegistryEntry(this.registries.enchantment, identifier(identifier, id));
    }

    @Override
    public RegistryEntry attributeModifier(final int id, final boolean mapped) {
        return registryEntry(registries.attributeModifier, mappingData.getAttributeMappings(), id, mapped);
    }

    @Override
    public String dataComponentType(final int id, final boolean mapped) {
        final FullMappings mappings = mappingData.getDataComponentSerializerMappings();
        return mapped ? mappings.mappedIdentifier(id) : mappings.identifier(id);
    }

    private RegistryEntry registryEntry(final Registry registry, final FullMappings mappings, final int id, final boolean mapped) {
        final String identifier = mapped ? mappings.mappedIdentifier(id) : mappings.identifier(id);
        return new RegistryEntry(registry, identifier(identifier, id));
    }

    private Identifier identifier(@Nullable final String identifier, final int id) {
        return identifier != null ? Identifier.of(identifier) : Identifier.of("viaversion", "unknown/" + id);
    }
}

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

import com.viaversion.viaversion.api.data.MappingData;
import java.util.List;
import net.lenni0451.mcstructs.core.Identifier;
import net.lenni0451.mcstructs.itemcomponents.impl.Registries;
import net.lenni0451.mcstructs.itemcomponents.registry.Registry;
import net.lenni0451.mcstructs.itemcomponents.registry.RegistryEntry;

final class RegistryAccessImpl implements ItemDataComponentConverter.RegistryAccess {
    private final List<Identifier> enchantments;
    private final Registries registries;
    private final MappingData mappingData;

    RegistryAccessImpl(final List<Identifier> enchantments, final Registries registries, final MappingData mappingData) {
        this.enchantments = enchantments;
        this.registries = registries;
        this.mappingData = mappingData;
    }

    @Override
    public RegistryEntry getItem(final int networkId) {
        final String identifier = this.mappingData.getFullItemMappings().mappedIdentifier(networkId);
        final Registry registry = this.registries.item;
        if (identifier != null) {
            return new RegistryEntry(registry, Identifier.of(identifier));
        } else {
            return new RegistryEntry(registry, Identifier.of("viaversion", "unknown/item/" + networkId));
        }
    }

    @Override
    public RegistryEntry getEnchantment(final int networkId) {
        final Registry registry = this.registries.enchantment;
        if (networkId < 0 || networkId >= this.enchantments.size()) {
            return new RegistryEntry(registry, Identifier.of("viaversion", "unknown/enchantment/" + networkId));
        }
        return new RegistryEntry(registry, this.enchantments.get(networkId));
    }
}

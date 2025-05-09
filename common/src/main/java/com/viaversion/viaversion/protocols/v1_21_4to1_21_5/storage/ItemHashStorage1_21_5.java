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
package com.viaversion.viaversion.protocols.v1_21_4to1_21_5.storage;

import com.google.common.cache.CacheBuilder;
import com.viaversion.viaversion.api.connection.StoredObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.MappingData;
import com.viaversion.viaversion.api.minecraft.data.StructuredData;
import com.viaversion.viaversion.rewriter.item.ItemDataComponentConverter;
import com.viaversion.viaversion.rewriter.item.RegistryAccess;
import com.viaversion.viaversion.util.Pair;
import net.lenni0451.mcstructs.converter.impl.v1_21_5.HashConverter_v1_21_5;
import net.lenni0451.mcstructs.core.Identifier;
import net.lenni0451.mcstructs.itemcomponents.ItemComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ItemHashStorage1_21_5 extends StoredObject {

    private final Map<Long, StructuredData<?>> hashToStructuredData = CacheBuilder.newBuilder().concurrencyLevel(1).maximumSize(256).<Long, StructuredData<?>>build().asMap();
    private final List<Identifier> enchantmentRegistry = new ArrayList<>();
    private final ItemDataComponentConverter itemComponentConverter;

    public ItemHashStorage1_21_5(final UserConnection user, final MappingData mappingData) {
        super(user);
        this.itemComponentConverter = new ItemDataComponentConverter(new RegistryAccess() {
            @Override
            public Identifier getItem(final int networkId) {
                final String identifier = mappingData.getFullItemMappings().mappedIdentifier(networkId);
                if (identifier != null) {
                    return Identifier.of(identifier);
                } else {
                    return Identifier.of("viaproxy", "unknown/item/" + networkId);
                }
            }

            @Override
            public Identifier getEnchantment(final int networkId) {
                if (networkId >= 0 && networkId < ItemHashStorage1_21_5.this.enchantmentRegistry.size()) {
                    return ItemHashStorage1_21_5.this.enchantmentRegistry.get(networkId);
                } else {
                    return Identifier.of("viaproxy", "unknown/enchantment/" + networkId);
                }
            }
        });
    }

    public void setEnchantmentRegistry(final List<Identifier> enchantmentRegistry) {
        this.enchantmentRegistry.clear();
        this.enchantmentRegistry.addAll(enchantmentRegistry);
    }

    public void trackStructuredData(final StructuredData<?> structuredData) {
        if (structuredData.isEmpty()) {
            return;
        }

        final Pair<ItemComponent<?>, Object> itemComponent = this.itemComponentConverter.viaToMcStructs(structuredData);
        if (itemComponent == null) {
            return;
        }

        final int hash = itemComponent.key().getCodec().serialize(HashConverter_v1_21_5.CRC32C, cast(itemComponent.value())).getOrThrow().asInt();
        final long key = (long) structuredData.id() << 32 | hash;
        if (!this.hashToStructuredData.containsKey(key)) {
            this.hashToStructuredData.put(key, structuredData.copy());
        }
    }

    public StructuredData<?> getStructuredData(final int componentId, final int hash) {
        final long key = (long) componentId << 32 | hash;
        if (this.hashToStructuredData.containsKey(key)) {
            return this.hashToStructuredData.get(key).copy();
        } else {
            return null;
        }
    }

    private static <T> T cast(final Object o) {
        return (T) o;
    }

}

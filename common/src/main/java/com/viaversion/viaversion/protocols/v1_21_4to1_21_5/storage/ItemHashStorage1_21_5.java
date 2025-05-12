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
import com.viaversion.viaversion.api.data.MappingData;
import com.viaversion.viaversion.api.data.item.ItemHasher;
import com.viaversion.viaversion.api.minecraft.data.StructuredData;
import com.viaversion.viaversion.rewriter.item.ItemDataComponentConverter;
import com.viaversion.viaversion.rewriter.item.ItemDataComponentConverter.RegistryAccess;
import com.viaversion.viaversion.util.SerializerVersion;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.lenni0451.mcstructs.converter.impl.v1_21_5.HashConverter_v1_21_5;
import net.lenni0451.mcstructs.itemcomponents.ItemComponentRegistry;

public class ItemHashStorage1_21_5 implements ItemHasher {

    private final Map<Long, StructuredData<?>> hashToStructuredData = CacheBuilder.newBuilder().concurrencyLevel(1).maximumSize(512).<Long, StructuredData<?>>build().asMap();
    private final List<String> enchantmentRegistry = new ArrayList<>();
    private final ItemDataComponentConverter itemComponentConverter;
    private boolean processingClientboundInventoryPacket;

    public ItemHashStorage1_21_5(final MappingData mappingData) {
        final RegistryAccess registryAccess = RegistryAccess.of(this.enchantmentRegistry, ItemComponentRegistry.V1_21_5.getRegistries(), mappingData);
        this.itemComponentConverter = new ItemDataComponentConverter(SerializerVersion.V1_21_5, SerializerVersion.V1_21_5, registryAccess); // always using 1.21.5 items as input
    }

    @Override
    public void setEnchantments(final List<String> enchantments) {
        this.enchantmentRegistry.clear();
        this.enchantmentRegistry.addAll(enchantments);
    }

    public void trackStructuredData(final StructuredData<?> structuredData) {
        if (structuredData.isEmpty()) {
            return;
        }

        final ItemDataComponentConverter.Result<?> result = this.itemComponentConverter.viaToMcStructs(structuredData, true);
        if (result == null) {
            return;
        }

        final long key = (long) structuredData.id() << 32 | hash(result);
        this.hashToStructuredData.computeIfAbsent(key, $ -> structuredData.copy());
    }

    public StructuredData<?> dataFromHash(final int dataComponentId, final int hash) {
        final long key = (long) dataComponentId << 32 | hash;
        final StructuredData<?> data = this.hashToStructuredData.get(key);
        return data != null ? data.copy() : null;
    }

    private static <T> int hash(final ItemDataComponentConverter.Result<T> result) {
        return result.type().getCodec().serialize(HashConverter_v1_21_5.CRC32C, result.value()).getOrThrow().asInt();
    }

    @Override
    public boolean isProcessingClientboundInventoryPacket() {
        return this.processingClientboundInventoryPacket;
    }

    @Override
    public void setProcessingClientboundInventoryPacket(final boolean processingClientboundInventoryPacket) {
        this.processingClientboundInventoryPacket = processingClientboundInventoryPacket;
    }
}

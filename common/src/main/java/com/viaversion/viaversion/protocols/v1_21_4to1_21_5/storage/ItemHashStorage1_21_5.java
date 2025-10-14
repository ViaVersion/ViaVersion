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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.viaversion.viaversion.api.data.item.ItemHasher;
import com.viaversion.viaversion.api.minecraft.codec.CodecContext;
import com.viaversion.viaversion.api.minecraft.codec.CodecContext.RegistryAccess;
import com.viaversion.viaversion.api.minecraft.codec.hash.Hasher;
import com.viaversion.viaversion.api.minecraft.data.StructuredData;
import com.viaversion.viaversion.codec.CodecRegistryContext;
import com.viaversion.viaversion.codec.hash.HashFunction;
import com.viaversion.viaversion.codec.hash.HashOps;
import com.viaversion.viaversion.data.item.ItemHasherBase;
import com.viaversion.viaversion.protocols.v1_21_4to1_21_5.Protocol1_21_4To1_21_5;
import java.util.concurrent.ExecutionException;

public class ItemHashStorage1_21_5 implements ItemHasher {

    private final Cache<Long, StructuredData<?>> hashToStructuredData = CacheBuilder.newBuilder().concurrencyLevel(1).maximumSize(512).build();
    private boolean processingClientboundInventoryPacket;
    private final CodecContext context;

    public ItemHashStorage1_21_5(final Protocol1_21_4To1_21_5 protocol) {
        final RegistryAccess registryAccess = RegistryAccess.of(protocol);
        this.context = new CodecRegistryContext(protocol, registryAccess, true); // always using 1.21.5 items as input
    }

    public void trackStructuredData(final StructuredData<?> structuredData) {
        if (structuredData.isEmpty()) {
            return;
        }

        final HashOps hasher = new HashOps(context, HashFunction.CRC32C);
        final int hash = hasher.context().isSupported(structuredData.key()) ? hash(hasher, structuredData) : ItemHasherBase.UNKNOWN_HASH;
        if (hash == ItemHasherBase.UNKNOWN_HASH) {
            return;
        }

        final long key = (long) structuredData.id() << 32 | hash;
        try {
            this.hashToStructuredData.get(key, structuredData::copy);
        } catch (final ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public StructuredData<?> dataFromHash(final int dataComponentId, final int hash) {
        final long key = (long) dataComponentId << 32 | hash;
        final StructuredData<?> data = this.hashToStructuredData.getIfPresent(key);
        return data != null ? data.copy() : null;
    }

    private static <T> int hash(final Hasher hasher, final StructuredData<T> data) {
        hasher.write(data.key().type(), data.value());
        return hasher.hash();
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

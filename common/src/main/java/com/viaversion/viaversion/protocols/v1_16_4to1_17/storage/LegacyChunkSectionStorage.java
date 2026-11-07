/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2026 ViaVersion and contributors
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
package com.viaversion.viaversion.protocols.v1_16_4to1_17.storage;

import com.viaversion.viaversion.api.minecraft.BlockChangeRecord;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.api.minecraft.ChunkPosition;
import com.viaversion.viaversion.api.minecraft.chunks.Chunk;
import com.viaversion.viaversion.api.minecraft.chunks.ChunkSection;
import com.viaversion.viaversion.api.minecraft.chunks.DataPalette;
import com.viaversion.viaversion.api.minecraft.chunks.PaletteType;
import com.viaversion.viaversion.util.MathUtil;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Keeps a bounded, client-side view of chunk sections at the 1.17 protocol boundary.
 *
 * <p>The cache is needed to turn legacy non-full chunk packets into real section diffs. It is
 * deliberately bounded and uses palette-packed section snapshots, so old servers cannot make
 * the cache grow without limit by touching chunks while a player moves around.</p>
 */
public final class LegacyChunkSectionStorage {

    private static final int SECTION_COUNT = 16;
    private static final long CHUNK_OVERHEAD_BYTES = 64L + SECTION_COUNT * Long.BYTES;

    private final LinkedHashMap<Long, CachedChunk> chunks = new LinkedHashMap<>(128, 0.75F, true);
    private final long maxCacheBytes;
    private long cacheBytes;
    private boolean active;

    LegacyChunkSectionStorage(final long maxCacheBytes) {
        this.maxCacheBytes = maxCacheBytes;
    }

    public boolean isActive() {
        return active;
    }

    public void markActive() {
        active = true;
    }

    /**
     * Replaces a complete chunk snapshot. Null sections are represented as uniform air sections,
     * making every section in the chunk a known snapshot for future partial packets.
     */
    public void storeFullChunk(final Chunk chunk, final int airBlockStateId) {
        final SectionState[] sections = new SectionState[SECTION_COUNT];
        final ChunkSection[] chunkSections = chunk.getSections();
        for (int sectionY = 0; sectionY < SECTION_COUNT; sectionY++) {
            final ChunkSection section = sectionY < chunkSections.length ? chunkSections[sectionY] : null;
            sections[sectionY] = section != null ? SectionState.fromPalette(section.palette(PaletteType.BLOCKS)) : SectionState.uniform(airBlockStateId);
        }

        putChunk(chunk.getX(), chunk.getZ(), new CachedChunk(sections));
    }

    /**
     * Returns the known state of a section, or null when no complete snapshot exists for it.
     */
    public @Nullable SectionState section(final int chunkX, final int sectionY, final int chunkZ) {
        if (!validSectionY(sectionY)) {
            return null;
        }

        final CachedChunk chunk = chunks.get(ChunkPosition.chunkKey(chunkX, chunkZ));
        return chunk != null ? chunk.sections[sectionY] : null;
    }

    /**
     * Stores a complete section received through a legacy partial chunk packet.
     */
    public void storeSection(final int chunkX, final int sectionY, final int chunkZ, final SectionState state) {
        if (!validSectionY(sectionY)) {
            return;
        }

        final long chunkKey = ChunkPosition.chunkKey(chunkX, chunkZ);
        CachedChunk chunk = chunks.get(chunkKey);
        if (chunk == null) {
            chunk = new CachedChunk(new SectionState[SECTION_COUNT]);
            chunks.put(chunkKey, chunk);
            cacheBytes += chunk.estimatedBytes;
        }

        final SectionState previous = chunk.sections[sectionY];
        if (previous != null) {
            final long previousBytes = previous.estimatedBytes();
            chunk.estimatedBytes -= previousBytes;
            cacheBytes -= previousBytes;
        }

        chunk.sections[sectionY] = state;
        final long stateBytes = state.estimatedBytes();
        chunk.estimatedBytes += stateBytes;
        cacheBytes += stateBytes;
        trim();
    }

    /**
     * Applies a mapped single-block update to an already known section.
     */
    public void updateBlock(final BlockPosition position, final int blockStateId) {
        updateBlock(position.x(), position.y(), position.z(), blockStateId);
    }

    public void updateBlock(final int blockX, final int blockY, final int blockZ, final int blockStateId) {
        final int sectionY = blockY >> 4;
        if (!validSectionY(sectionY)) {
            return;
        }

        final CachedChunk chunk = chunks.get(ChunkPosition.chunkKeyForBlock(blockX, blockZ));
        if (chunk == null) {
            return;
        }

        final SectionState section = chunk.sections[sectionY];
        if (section == null) {
            // The chunk was created from a partial packet and this section has no known snapshot.
            return;
        }

        final long previousBytes = section.estimatedBytes();
        section.setIdAt(ChunkSection.index(blockX & 15, blockY & 15, blockZ & 15), blockStateId);
        updateSizeAfterMutation(chunk, section, previousBytes);
    }

    /**
     * Applies a mapped 1.16.2+ section block update to an already known section.
     */
    public void updateSection(final long sectionPosition, final BlockChangeRecord[] records) {
        final int chunkX = (int) (sectionPosition >> 42);
        final int sectionY = (int) (sectionPosition << 44 >> 44);
        final int chunkZ = (int) (sectionPosition << 22 >> 42);
        if (!validSectionY(sectionY)) {
            return;
        }

        final CachedChunk chunk = chunks.get(ChunkPosition.chunkKey(chunkX, chunkZ));
        if (chunk == null) {
            return;
        }

        final SectionState section = chunk.sections[sectionY];
        if (section == null) {
            return;
        }

        final long previousBytes = section.estimatedBytes();
        for (final BlockChangeRecord record : records) {
            section.setIdAt(ChunkSection.index(
                    record.getSectionX() & 15,
                    record.getSectionY() & 15,
                    record.getSectionZ() & 15
            ), record.getBlockId());
        }
        updateSizeAfterMutation(chunk, section, previousBytes);
    }

    public void unloadChunk(final int chunkX, final int chunkZ) {
        final CachedChunk removed = chunks.remove(ChunkPosition.chunkKey(chunkX, chunkZ));
        if (removed != null) {
            cacheBytes -= removed.estimatedBytes;
        }
    }

    public void clear() {
        chunks.clear();
        cacheBytes = 0;
        active = false;
    }

    private void putChunk(final int chunkX, final int chunkZ, final CachedChunk chunk) {
        final long chunkKey = ChunkPosition.chunkKey(chunkX, chunkZ);
        final CachedChunk previous = chunks.remove(chunkKey);
        if (previous != null) {
            cacheBytes -= previous.estimatedBytes;
        }

        chunks.put(chunkKey, chunk);
        cacheBytes += chunk.estimatedBytes;
        trim();
    }

    private void updateSizeAfterMutation(final CachedChunk chunk, final SectionState section, final long previousBytes) {
        final long sizeDelta = section.estimatedBytes() - previousBytes;
        if (sizeDelta != 0) {
            chunk.estimatedBytes += sizeDelta;
            cacheBytes += sizeDelta;
        }
        trim();
    }

    private void trim() {
        if (cacheBytes <= maxCacheBytes) {
            return;
        }

        final Iterator<Map.Entry<Long, CachedChunk>> iterator = chunks.entrySet().iterator();
        while (cacheBytes > maxCacheBytes && iterator.hasNext()) {
            final CachedChunk removed = iterator.next().getValue();
            // Keep a single oversized hot chunk rather than immediately forgetting the snapshot
            // we just built and retransmitting all 4096 records on the next packet.
            if (chunks.size() == 1) {
                break;
            }
            iterator.remove();
            cacheBytes -= removed.estimatedBytes;
        }
    }

    private static boolean validSectionY(final int sectionY) {
        return sectionY >= 0 && sectionY < SECTION_COUNT;
    }

    private static final class CachedChunk {
        private final SectionState[] sections;
        private long estimatedBytes;

        private CachedChunk(final SectionState[] sections) {
            this.sections = sections;
            this.estimatedBytes = CHUNK_OVERHEAD_BYTES;
            for (final SectionState section : sections) {
                if (section != null) {
                    this.estimatedBytes += section.estimatedBytes();
                }
            }
        }
    }

    /**
     * Simple mutable palette-indexed snapshot of one 16x16x16 block section.
     */
    public static final class SectionState {
        private static final long[] EMPTY_DATA = new long[0];
        private static final long OBJECT_OVERHEAD_BYTES = 40L;
        private static final int MIN_PALETTE_CAPACITY = 4;

        private int[] palette;
        private int paletteSize;
        private @Nullable short[] paletteUsages;
        private int bitsPerValue;
        private int valuesPerLong;
        private long valueMask;
        private long[] data;

        private SectionState(final int[] palette, final int bitsPerValue, final long[] data) {
            this.palette = palette;
            this.paletteSize = palette.length;
            setFormat(bitsPerValue, data);
        }

        public static SectionState uniform(final int blockStateId) {
            return new SectionState(new int[]{blockStateId}, 0, EMPTY_DATA);
        }

        public static SectionState fromPalette(final DataPalette palette) {
            final int paletteSize = palette.size();
            if (paletteSize <= 0) {
                throw new IllegalArgumentException("Cannot cache an empty block palette");
            }

            final int[] ids = new int[paletteSize];
            for (int i = 0; i < paletteSize; i++) {
                ids[i] = palette.idByIndex(i);
            }

            final int bitsPerValue = paletteSize == 1 ? 0 : MathUtil.ceilLog2(paletteSize);
            final long[] data = bitsPerValue == 0
                    ? EMPTY_DATA
                    : palette.createPackedValues(bitsPerValue, ChunkSection.SIZE, false).clone();
            return new SectionState(ids, bitsPerValue, data);
        }

        public int idAt(final int sectionIndex) {
            return palette[paletteIndexAt(sectionIndex)];
        }

        public void setIdAt(final int sectionIndex, final int blockStateId) {
            final int oldPaletteIndex = paletteIndexAt(sectionIndex);
            if (palette[oldPaletteIndex] == blockStateId) {
                return;
            }

            short[] usages = paletteUsages();
            if (usages[oldPaletteIndex] <= 0) {
                throw new IllegalStateException("Invalid palette usage count at index " + oldPaletteIndex);
            }
            usages[oldPaletteIndex]--;

            int paletteIndex = findPaletteIndex(blockStateId);
            if (paletteIndex == -1) {
                paletteIndex = findReusablePaletteIndex(usages);
                if (paletteIndex == -1) {
                    paletteIndex = paletteSize;
                    ensurePaletteCapacity(paletteSize + 1);
                    usages = paletteUsages();
                    paletteSize++;

                    final int requiredBits = MathUtil.ceilLog2(paletteSize);
                    if (requiredBits > bitsPerValue) {
                        repack(requiredBits);
                    }
                }
                palette[paletteIndex] = blockStateId;
            }

            usages[paletteIndex]++;
            setPaletteIndexAt(sectionIndex, paletteIndex);
        }

        public long estimatedBytes() {
            final long usageBytes = paletteUsages != null ? (long) paletteUsages.length * Short.BYTES : 0L;
            return OBJECT_OVERHEAD_BYTES + (long) palette.length * Integer.BYTES + usageBytes + (long) data.length * Long.BYTES;
        }

        private int findPaletteIndex(final int blockStateId) {
            for (int i = 0; i < paletteSize; i++) {
                if (palette[i] == blockStateId) {
                    return i;
                }
            }
            return -1;
        }

        private int findReusablePaletteIndex(final short[] usages) {
            for (int i = 0; i < paletteSize; i++) {
                if (usages[i] == 0) {
                    return i;
                }
            }
            return -1;
        }

        private short[] paletteUsages() {
            short[] usages = paletteUsages;
            if (usages == null) {
                usages = new short[palette.length];
                for (int sectionIndex = 0; sectionIndex < ChunkSection.SIZE; sectionIndex++) {
                    final int paletteIndex = paletteIndexAt(sectionIndex);
                    if (paletteIndex < 0 || paletteIndex >= paletteSize) {
                        throw new IllegalStateException("Invalid palette index " + paletteIndex + " at section index " + sectionIndex);
                    }
                    usages[paletteIndex]++;
                }
                paletteUsages = usages;
            }
            return usages;
        }

        private void ensurePaletteCapacity(final int requiredCapacity) {
            if (requiredCapacity <= palette.length) {
                return;
            }

            final int doubledCapacity = palette.length < MIN_PALETTE_CAPACITY ? MIN_PALETTE_CAPACITY : palette.length << 1;
            final int newCapacity = Math.max(requiredCapacity, doubledCapacity);
            final short[] usages = paletteUsages;
            if (usages == null) {
                throw new IllegalStateException("Palette usages were not initialized");
            }
            palette = Arrays.copyOf(palette, newCapacity);
            paletteUsages = Arrays.copyOf(usages, newCapacity);
        }

        private int paletteIndexAt(final int sectionIndex) {
            if (bitsPerValue == 0) {
                return 0;
            }

            final int cellIndex = sectionIndex / valuesPerLong;
            final int bitIndex = (sectionIndex - cellIndex * valuesPerLong) * bitsPerValue;
            return (int) (data[cellIndex] >>> bitIndex & valueMask);
        }

        private void setPaletteIndexAt(final int sectionIndex, final int paletteIndex) {
            if (bitsPerValue == 0) {
                if (paletteIndex != 0) {
                    throw new IllegalStateException("Cannot set a non-zero index to a uniform section");
                }
                return;
            }

            final int cellIndex = sectionIndex / valuesPerLong;
            final int bitIndex = (sectionIndex - cellIndex * valuesPerLong) * bitsPerValue;
            final long shiftedMask = valueMask << bitIndex;
            data[cellIndex] = (data[cellIndex] & ~shiftedMask) | ((long) paletteIndex << bitIndex);
        }

        private void repack(final int newBitsPerValue) {
            final int oldBitsPerValue = bitsPerValue;
            final int oldValuesPerLong = valuesPerLong;
            final long oldValueMask = valueMask;
            final long[] oldData = data;

            final int newValuesPerLong = 64 / newBitsPerValue;
            final long[] newData = new long[(ChunkSection.SIZE + newValuesPerLong - 1) / newValuesPerLong];
            setFormat(newBitsPerValue, newData);

            if (oldBitsPerValue == 0) {
                return;
            }

            for (int sectionIndex = 0; sectionIndex < ChunkSection.SIZE; sectionIndex++) {
                final int oldCellIndex = sectionIndex / oldValuesPerLong;
                final int oldBitIndex = (sectionIndex - oldCellIndex * oldValuesPerLong) * oldBitsPerValue;
                final int paletteIndex = (int) (oldData[oldCellIndex] >>> oldBitIndex & oldValueMask);
                setPaletteIndexAt(sectionIndex, paletteIndex);
            }
        }

        private void setFormat(final int bitsPerValue, final long[] data) {
            this.bitsPerValue = bitsPerValue;
            this.valuesPerLong = bitsPerValue == 0 ? 0 : 64 / bitsPerValue;
            this.valueMask = bitsPerValue == 0 ? 0 : (1L << bitsPerValue) - 1L;
            this.data = data;
        }
    }
}

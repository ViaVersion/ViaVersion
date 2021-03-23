/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2021 ViaVersion and contributors
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
package us.myles.ViaVersion.api.minecraft.chunks;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.jetbrains.annotations.Nullable;

public class ChunkSection {

    /**
     * Size (dimensions) of blocks in a chunks section.
     */
    public static final int SIZE = 16 * 16 * 16; // width * depth * height
    /**
     * Length of the sky and block light nibble arrays.
     */
    public static final int LIGHT_LENGTH = 16 * 16 * 16 / 2; // size * size * size / 2 (nibble bit count)
    private final IntList palette;
    private final Int2IntMap inversePalette;
    private final int[] blocks;
    private NibbleArray blockLight;
    private NibbleArray skyLight;
    private int nonAirBlocksCount;

    public ChunkSection() {
        this.blocks = new int[SIZE];
        this.blockLight = new NibbleArray(SIZE);
        palette = new IntArrayList();
        inversePalette = new Int2IntOpenHashMap();
        inversePalette.defaultReturnValue(-1);
    }

    public ChunkSection(int expectedPaletteLength) {
        this.blocks = new int[SIZE];
        this.blockLight = new NibbleArray(SIZE);

        // Pre-size the palette array/map
        palette = new IntArrayList(expectedPaletteLength);
        inversePalette = new Int2IntOpenHashMap(expectedPaletteLength);
        inversePalette.defaultReturnValue(-1);
    }

    /**
     * Set a block in the chunks
     * This method will not update non-air blocks count
     *
     * @param x    Block X
     * @param y    Block Y
     * @param z    Block Z
     * @param type The type of the block
     * @param data The data value of the block
     */
    public void setBlock(int x, int y, int z, int type, int data) {
        setFlatBlock(index(x, y, z), type << 4 | (data & 0xF));
    }

    public void setFlatBlock(int x, int y, int z, int type) {
        setFlatBlock(index(x, y, z), type);
    }

    public int getBlockId(int x, int y, int z) {
        return getFlatBlock(x, y, z) >> 4;
    }

    public int getBlockData(int x, int y, int z) {
        return getFlatBlock(x, y, z) & 0xF;
    }

    public int getFlatBlock(int x, int y, int z) {
        int index = blocks[index(x, y, z)];
        return palette.getInt(index);
    }

    public int getFlatBlock(int idx) {
        int index = blocks[idx];
        return palette.getInt(index);
    }

    public void setBlock(int idx, int type, int data) {
        setFlatBlock(idx, type << 4 | (data & 0xF));
    }

    public void setPaletteIndex(int idx, int index) {
        blocks[idx] = index;
    }

    public int getPaletteIndex(int idx) {
        return blocks[idx];
    }

    public int getPaletteSize() {
        return palette.size();
    }

    public int getPaletteEntry(int index) {
        if (index < 0 || index >= palette.size()) throw new IndexOutOfBoundsException();
        return palette.getInt(index);
    }

    public void setPaletteEntry(int index, int id) {
        if (index < 0 || index >= palette.size()) throw new IndexOutOfBoundsException();

        int oldId = palette.set(index, id);
        if (oldId == id) return;

        inversePalette.put(id, index);
        if (inversePalette.get(oldId) == index) {
            inversePalette.remove(oldId);
            for (int i = 0; i < palette.size(); i++) {
                if (palette.getInt(i) == oldId) {
                    inversePalette.put(oldId, i);
                    break;
                }
            }
        }
    }

    public void replacePaletteEntry(int oldId, int newId) {
        int index = inversePalette.remove(oldId);
        if (index == -1) return;

        inversePalette.put(newId, index);
        for (int i = 0; i < palette.size(); i++) {
            if (palette.getInt(i) == oldId) {
                palette.set(i, newId);
            }
        }
    }

    public void addPaletteEntry(int id) {
        inversePalette.put(id, palette.size());
        palette.add(id);
    }

    public void clearPalette() {
        palette.clear();
        inversePalette.clear();
    }

    /**
     * Set a block state in the chunk
     * This method will not update non-air blocks count
     *
     * @param idx Index
     * @param id  The raw or flat id of the block
     */
    public void setFlatBlock(int idx, int id) {
        int index = inversePalette.get(id);
        if (index == -1) {
            index = palette.size();
            palette.add(id);
            inversePalette.put(id, index);
        }

        blocks[idx] = index;
    }

    /**
     * Set the block light array
     *
     * @param data The value to set the block light to
     */
    public void setBlockLight(@Nullable byte[] data) {
        if (data.length != LIGHT_LENGTH) throw new IllegalArgumentException("Data length != " + LIGHT_LENGTH);
        if (this.blockLight == null) {
            this.blockLight = new NibbleArray(data);
        } else {
            this.blockLight.setHandle(data);
        }
    }

    /**
     * Set the sky light array
     *
     * @param data The value to set the sky light to
     */
    public void setSkyLight(@Nullable byte[] data) {
        if (data.length != LIGHT_LENGTH) throw new IllegalArgumentException("Data length != " + LIGHT_LENGTH);
        if (this.skyLight == null) {
            this.skyLight = new NibbleArray(data);
        } else {
            this.skyLight.setHandle(data);
        }
    }

    @Nullable
    public byte[] getBlockLight() {
        return blockLight == null ? null : blockLight.getHandle();
    }

    @Nullable
    public NibbleArray getBlockLightNibbleArray() {
        return blockLight;
    }

    @Nullable
    public byte[] getSkyLight() {
        return skyLight == null ? null : skyLight.getHandle();
    }

    @Nullable
    public NibbleArray getSkyLightNibbleArray() {
        return skyLight;
    }

    public void readBlockLight(ByteBuf input) {
        if (this.blockLight == null) {
            this.blockLight = new NibbleArray(LIGHT_LENGTH * 2);
        }
        input.readBytes(this.blockLight.getHandle());
    }

    public void readSkyLight(ByteBuf input) {
        if (this.skyLight == null) {
            this.skyLight = new NibbleArray(LIGHT_LENGTH * 2);
        }
        input.readBytes(this.skyLight.getHandle());
    }

    public static int index(int x, int y, int z) {
        return y << 8 | z << 4 | x;
    }

    /**
     * Write the block light to a buffer
     *
     * @param output The buffer to write to
     */
    public void writeBlockLight(ByteBuf output) {
        output.writeBytes(blockLight.getHandle());
    }

    /**
     * Write the sky light to a buffer
     *
     * @param output The buffer to write to
     */
    public void writeSkyLight(ByteBuf output) {
        output.writeBytes(skyLight.getHandle());
    }

    /**
     * Check if sky light is present
     *
     * @return True if skylight is present
     */
    public boolean hasSkyLight() {
        return skyLight != null;
    }

    public boolean hasBlockLight() {
        return blockLight != null;
    }

    public int getNonAirBlocksCount() {
        return nonAirBlocksCount;
    }

    public void setNonAirBlocksCount(int nonAirBlocksCount) {
        this.nonAirBlocksCount = nonAirBlocksCount;
    }
}

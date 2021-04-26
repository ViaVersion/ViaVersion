/*
 * This file is part of adventure, licensed under the MIT License.
 *
 * Copyright (c) 2017-2020 KyoriPowered
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
package com.viaversion.viaversion.api.minecraft.nbt;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.BufferedInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * See https://github.com/KyoriPowered/adventure.
 */
public final class BinaryTagIO {
    private BinaryTagIO() {
    }

    /**
     * Reads a compound tag from {@code path}.
     *
     * @param path the path
     * @return the compound tag
     * @throws IOException if an exception was encountered while reading a compound tag
     */
    public static @NonNull CompoundTag readPath(final @NonNull Path path) throws IOException {
        return readInputStream(Files.newInputStream(path));
    }

    /**
     * Reads a compound tag from an input stream.
     *
     * @param input the input stream
     * @return the compound tag
     * @throws IOException if an exception was encountered while reading a compound tag
     */
    public static @NonNull CompoundTag readInputStream(final @NonNull InputStream input) throws IOException {
        try (final DataInputStream dis = new DataInputStream(input)) {
            return readDataInput(dis);
        }
    }

    /**
     * Reads a compound tag from {@code path} using GZIP decompression.
     *
     * @param path the path
     * @return the compound tag
     * @throws IOException if an exception was encountered while reading a compound tag
     */
    public static @NonNull CompoundTag readCompressedPath(final @NonNull Path path) throws IOException {
        return readCompressedInputStream(Files.newInputStream(path));
    }

    /**
     * Reads a compound tag from an input stream using GZIP decompression.
     *
     * @param input the input stream
     * @return the compound tag
     * @throws IOException if an exception was encountered while reading a compound tag
     */
    public static @NonNull CompoundTag readCompressedInputStream(final @NonNull InputStream input) throws IOException {
        try (final DataInputStream dis = new DataInputStream(new BufferedInputStream(new GZIPInputStream(input)))) {
            return readDataInput(dis);
        }
    }

    /**
     * Reads a compound tag from {@code input}.
     *
     * @param input the input
     * @return the compound tag
     * @throws IOException if an exception was encountered while reading a compound tag
     */
    public static @NonNull CompoundTag readDataInput(final @NonNull DataInput input) throws IOException {
        byte type = input.readByte();
        if (type != CompoundTag.ID) {
            throw new IOException(String.format("Expected root tag to be a CompoundTag, was %s", type));
        }
        input.skipBytes(input.readUnsignedShort()); // read empty name

        final CompoundTag compoundTag = new CompoundTag();
        compoundTag.read(input);
        return compoundTag;
    }

    /**
     * Writes a compound tag to {@code path}.
     *
     * @param tag  the compound tag
     * @param path the path
     * @throws IOException if an exception was encountered while writing the compound tag
     */
    public static void writePath(final @NonNull CompoundTag tag, final @NonNull Path path) throws IOException {
        writeOutputStream(tag, Files.newOutputStream(path));
    }

    /**
     * Writes a compound tag to an output stream.
     *
     * @param tag    the compound tag
     * @param output the output stream
     * @throws IOException if an exception was encountered while writing the compound tag
     */
    public static void writeOutputStream(final @NonNull CompoundTag tag, final @NonNull OutputStream output) throws IOException {
        try (final DataOutputStream dos = new DataOutputStream(output)) {
            writeDataOutput(tag, dos);
        }
    }

    /**
     * Writes a compound tag to {@code path} using GZIP compression.
     *
     * @param tag  the compound tag
     * @param path the path
     * @throws IOException if an exception was encountered while writing the compound tag
     */
    public static void writeCompressedPath(final @NonNull CompoundTag tag, final @NonNull Path path) throws IOException {
        writeCompressedOutputStream(tag, Files.newOutputStream(path));
    }

    /**
     * Writes a compound tag to an output stream using GZIP compression.
     *
     * @param tag    the compound tag
     * @param output the output stream
     * @throws IOException if an exception was encountered while writing the compound tag
     */
    public static void writeCompressedOutputStream(final @NonNull CompoundTag tag, final @NonNull OutputStream output) throws IOException {
        try (final DataOutputStream dos = new DataOutputStream(new GZIPOutputStream(output))) {
            writeDataOutput(tag, dos);
        }
    }

    /**
     * Writes a compound tag to {@code output}.
     *
     * @param tag    the compound tag
     * @param output the output
     * @throws IOException if an exception was encountered while writing the compound tag
     */
    public static void writeDataOutput(final @NonNull CompoundTag tag, final @NonNull DataOutput output) throws IOException {
        output.writeByte(CompoundTag.ID);
        output.writeUTF(""); // write empty name
        tag.write(output);
    }

    /**
     * Reads a compound tag from a {@link String}.
     *
     * @param input the string
     * @return the compound tag
     * @throws IOException if an exception was encountered while reading a compound tag
     */
    public static @NonNull CompoundTag readString(final @NonNull String input) throws IOException {
        try {
            final CharBuffer buffer = new CharBuffer(input);
            final TagStringReader parser = new TagStringReader(buffer);
            final CompoundTag tag = parser.compound();
            if (buffer.skipWhitespace().hasMore()) {
                throw new IOException("Document had trailing content after first CompoundTag");
            }
            return tag;
        } catch (final StringTagParseException ex) {
            throw new IOException(ex);
        }
    }

    /**
     * Writes a compound tag to a {@link String}.
     *
     * @param tag the compound tag
     * @return the string
     * @throws IOException if an exception was encountered while writing the compound tag
     */
    public static @NonNull String writeString(final @NonNull CompoundTag tag) throws IOException {
        final StringBuilder sb = new StringBuilder();
        try (final TagStringWriter emit = new TagStringWriter(sb)) {
            emit.writeTag(tag);
        }
        return sb.toString();
    }
}
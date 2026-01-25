/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2026 ViaVersion and contributors
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
package com.viaversion.viaversion.api.type.types.misc;

import com.viaversion.nbt.io.TagRegistry;
import com.viaversion.nbt.limiter.TagLimiter;
import com.viaversion.nbt.tag.ByteArrayTag;
import com.viaversion.nbt.tag.ByteTag;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.DoubleTag;
import com.viaversion.nbt.tag.FloatTag;
import com.viaversion.nbt.tag.IntArrayTag;
import com.viaversion.nbt.tag.IntTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.LongArrayTag;
import com.viaversion.nbt.tag.LongTag;
import com.viaversion.nbt.tag.NumberTag;
import com.viaversion.nbt.tag.ShortTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.OptionalType;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import java.io.IOException;
import java.util.Map;

public class TagType extends Type<Tag> {

    private final int maxBytes;

    public TagType() {
        this(true);
    }

    public TagType(final boolean limitBytes) {
        super(Tag.class);
        this.maxBytes = limitBytes ? NamedCompoundTagType.MAX_NBT_BYTES : Integer.MAX_VALUE;
    }

    @Override
    public Tag read(final ByteBuf buffer) {
        final byte id = buffer.readByte();
        if (id == 0) {
            return null;
        }

        final TagLimiter tagLimiter = TagLimiter.create(this.maxBytes, NamedCompoundTagType.MAX_NESTING_LEVEL);
        try {
            return TagRegistry.read(id, new ByteBufInputStream(buffer), tagLimiter, 0);
        } catch (final IOException e) {
            if (Via.getManager().isDebug()) {
                throw new RuntimeException(e);
            } else {
                throw new RuntimeException("Error reading tag :" + e.getMessage());
            }
        }
    }

    @Override
    public void write(final ByteBuf buffer, final Tag tag) {
        try {
            NamedCompoundTagType.write(buffer, tag, null);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void write(final Ops ops, final Tag value) {
        if (value == null) {
            throw new IllegalArgumentException("Cannot write null tag"); // no 'empty'
        } else if (value instanceof StringTag stringTag) {
            ops.writeString(stringTag.getValue());
        } else if (value instanceof NumberTag) {
            if (value instanceof IntTag intTag) {
                ops.writeInt(intTag.asInt());
            } else if (value instanceof ByteTag byteTag) {
                ops.writeByte(byteTag.asByte());
            } else if (value instanceof FloatTag floatTag) {
                ops.writeFloat(floatTag.asFloat());
            } else if (value instanceof DoubleTag doubleTag) {
                ops.writeDouble(doubleTag.asDouble());
            } else if (value instanceof ShortTag shortTag) {
                ops.writeShort(shortTag.asShort());
            } else if (value instanceof LongTag longTag) {
                ops.writeLong(longTag.asLong());
            }
        } else if (value instanceof CompoundTag compoundTag) {
            ops.writeMap(map -> {
                for (final Map.Entry<String, Tag> entry : compoundTag.entrySet()) {
                    map.write(entry.getKey(), this, entry.getValue());
                }
            });
        } else if (value instanceof ListTag<?> listTag) {
            ops.writeList(list -> {
                for (final Tag tag : listTag) {
                    list.write(this, tag);
                }
            });
        } else if (value instanceof IntArrayTag intArrayTag) {
            ops.writeInts(intArrayTag.getValue());
        } else if (value instanceof ByteArrayTag byteArrayTag) {
            ops.writeBytes(byteArrayTag.getValue());
        } else if (value instanceof LongArrayTag longArrayTag) {
            ops.writeLongs(longArrayTag.getValue());
        } else {
            throw new IllegalArgumentException("Unknown tag type: " + value);
        }
    }

    public static final class OptionalTagType extends OptionalType<Tag> {

        private OptionalTagType(final Type<Tag> tagType) {
            super(tagType);
        }

        public static OptionalTagType type() {
            return new OptionalTagType(Types.TAG);
        }

        public static OptionalTagType trustedType() {
            return new OptionalTagType(Types.TRUSTED_TAG);
        }
    }
}

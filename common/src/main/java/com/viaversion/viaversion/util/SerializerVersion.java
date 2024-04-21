/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2024 ViaVersion and contributors
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
package com.viaversion.viaversion.util;

import com.github.steveice10.opennbt.tag.builtin.Tag;
import com.google.gson.JsonElement;
import net.lenni0451.mcstructs.snbt.SNbtSerializer;
import net.lenni0451.mcstructs.snbt.exceptions.SNbtDeserializeException;
import net.lenni0451.mcstructs.snbt.exceptions.SNbtSerializeException;
import net.lenni0451.mcstructs.text.ATextComponent;
import net.lenni0451.mcstructs.text.serializer.TextComponentCodec;
import net.lenni0451.mcstructs.text.serializer.TextComponentSerializer;

public enum SerializerVersion {
    V1_6(TextComponentSerializer.V1_6, null),
    V1_7(TextComponentSerializer.V1_7, SNbtSerializer.V1_7),
    V1_8(TextComponentSerializer.V1_8, SNbtSerializer.V1_8),
    V1_9(TextComponentSerializer.V1_9, SNbtSerializer.V1_8),
    V1_12(TextComponentSerializer.V1_12, SNbtSerializer.V1_12),
    V1_13(TextComponentSerializer.V1_12, SNbtSerializer.V1_13),
    V1_14(TextComponentSerializer.V1_14, SNbtSerializer.V1_14),
    V1_15(TextComponentSerializer.V1_15, SNbtSerializer.V1_14),
    V1_16(TextComponentSerializer.V1_16, SNbtSerializer.V1_14),
    V1_17(TextComponentSerializer.V1_17, SNbtSerializer.V1_14),
    V1_18(TextComponentSerializer.V1_18, SNbtSerializer.V1_14),
    V1_19_4(TextComponentSerializer.V1_19_4, SNbtSerializer.V1_14),
    V1_20_3(TextComponentCodec.V1_20_3, SNbtSerializer.V1_14),
    V1_20_5(TextComponentCodec.V1_20_5, SNbtSerializer.V1_14);

    final TextComponentSerializer jsonSerializer;
    final SNbtSerializer<? extends Tag> snbtSerializer;
    final TextComponentCodec codec;

    SerializerVersion(final TextComponentSerializer jsonSerializer, final SNbtSerializer<? extends Tag> snbtSerializer) {
        this.jsonSerializer = jsonSerializer;
        this.snbtSerializer = snbtSerializer;
        this.codec = null;
    }

    SerializerVersion(final TextComponentCodec codec, final SNbtSerializer<? extends Tag> snbtSerializer) {
        this.codec = codec;
        this.jsonSerializer = codec.asSerializer();
        this.snbtSerializer = snbtSerializer;
    }

    public String toString(final ATextComponent component) {
        return jsonSerializer.serialize(component);
    }

    public JsonElement toJson(final ATextComponent component) {
        return jsonSerializer.serializeJson(component);
    }

    public Tag toTag(final ATextComponent component) {
        if (codec == null) {
            throw new IllegalStateException("Cannot convert component to NBT with this version");
        }
        return codec.serializeNbt(component);
    }

    public ATextComponent toComponent(final JsonElement json) {
        return jsonSerializer.deserialize(json);
    }

    public ATextComponent toComponent(final String json) {
        if (ordinal() >= SerializerVersion.V1_20_3.ordinal()) {
            return jsonSerializer.deserializeParser(json);
        } else if (ordinal() >= SerializerVersion.V1_9.ordinal()) {
            return jsonSerializer.deserializeReader(json);
        } else {
            return jsonSerializer.deserialize(json);
        }
    }

    public ATextComponent toComponent(final Tag tag) {
        if (codec == null) {
            throw new IllegalStateException("Cannot convert NBT to component with this version");
        }
        return codec.deserializeNbtTree(tag);
    }

    public Tag toTag(final String snbt) {
        if (snbtSerializer == null) {
            throw new IllegalStateException("Cannot convert SNBT to NBT with this version");
        }
        try {
            return snbtSerializer.deserialize(snbt);
        } catch (SNbtDeserializeException e) {
            throw new RuntimeException(e);
        }
    }

    public String toSNBT(final Tag tag) {
        if (snbtSerializer == null) {
            throw new IllegalStateException("Cannot convert SNBT to NBT with this version");
        }
        try {
            return snbtSerializer.serialize(tag);
        } catch (SNbtSerializeException e) {
            throw new RuntimeException(e);
        }
    }
}
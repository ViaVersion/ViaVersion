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
package com.viaversion.viaversion.util;

import com.google.gson.JsonElement;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.rewriter.text.JsonNBTComponentRewriter;
import net.lenni0451.mcstructs.snbt.SNbt;
import net.lenni0451.mcstructs.snbt.exceptions.SNbtDeserializeException;
import net.lenni0451.mcstructs.snbt.exceptions.SNbtSerializeException;
import net.lenni0451.mcstructs.text.TextComponent;
import net.lenni0451.mcstructs.text.serializer.TextComponentCodec;
import net.lenni0451.mcstructs.text.serializer.TextComponentSerializer;

/**
 * Wrapper enum to unify text component and string nbt serialization functions across different versions.
 * Only contains versions which have actual changes we handle.
 *
 * @see ComponentUtil
 * @see JsonNBTComponentRewriter
 */
public enum SerializerVersion {
    V1_6(TextComponentSerializer.V1_6, null),
    V1_7(TextComponentSerializer.V1_7, SNbt.V1_7),
    V1_8(TextComponentSerializer.V1_8, SNbt.V1_8),
    V1_9(TextComponentSerializer.V1_9, SNbt.V1_8),
    V1_12(TextComponentSerializer.V1_12, SNbt.V1_12),
    V1_13(TextComponentSerializer.V1_12, SNbt.V1_13),
    V1_14(TextComponentSerializer.V1_14, SNbt.V1_14),
    V1_15(TextComponentSerializer.V1_15, SNbt.V1_14),
    V1_16(TextComponentSerializer.V1_16, SNbt.V1_14),
    V1_17(TextComponentSerializer.V1_17, SNbt.V1_14),
    V1_18(TextComponentSerializer.V1_18, SNbt.V1_14),
    V1_19_4(TextComponentSerializer.V1_19_4, SNbt.V1_14),
    V1_20_3(TextComponentCodec.V1_20_3, SNbt.V1_14),
    V1_20_5(TextComponentCodec.V1_20_5, SNbt.V1_14),
    V1_21_4(TextComponentCodec.V1_21_4, SNbt.V1_14),
    V1_21_5(TextComponentCodec.V1_21_5, null/*Currently not needed and also not implemented 100% in MCStructs*/);

    final TextComponentSerializer jsonSerializer;
    final SNbt<? extends Tag> sNbt;
    final TextComponentCodec codec;

    SerializerVersion(final TextComponentSerializer jsonSerializer, final SNbt<? extends Tag> sNbt) {
        this.jsonSerializer = jsonSerializer;
        this.sNbt = sNbt;
        this.codec = null;
    }

    SerializerVersion(final TextComponentCodec codec, final SNbt<? extends Tag> sNbt) {
        this.codec = codec;
        this.jsonSerializer = codec.asSerializer();
        this.sNbt = sNbt;
    }

    public String toString(final TextComponent component) {
        return jsonSerializer.serialize(component);
    }

    public JsonElement toJson(final TextComponent component) {
        return jsonSerializer.serializeJson(component);
    }

    public Tag toTag(final TextComponent component) {
        if (codec == null) {
            throw new IllegalStateException("Cannot convert component to NBT with this version");
        }

        return codec.serializeNbtTree(component);
    }

    public TextComponent toComponent(final JsonElement json) {
        return jsonSerializer.deserialize(json);
    }

    public TextComponent toComponent(final String json) {
        if (ordinal() >= SerializerVersion.V1_20_3.ordinal()) {
            return jsonSerializer.deserializeParser(json);
        } else if (ordinal() >= SerializerVersion.V1_9.ordinal()) {
            return jsonSerializer.deserializeReader(json);
        } else {
            return jsonSerializer.deserialize(json);
        }
    }

    public TextComponent toComponent(final Tag tag) {
        if (codec == null) {
            throw new IllegalStateException("Cannot convert NBT to component with this version");
        }
        return codec.deserializeNbtTree(tag);
    }

    public Tag toTag(final String snbt) {
        if (sNbt == null) {
            throw new IllegalStateException("Cannot convert SNBT to NBT with this version");
        }

        try {
            return sNbt.deserialize(snbt);
        } catch (SNbtDeserializeException e) {
            throw new RuntimeException(e);
        }
    }

    public String toSNBT(final Tag tag) {
        if (sNbt == null) {
            throw new IllegalStateException("Cannot convert SNBT to NBT with this version");
        }

        try {
            return sNbt.serialize(tag);
        } catch (SNbtSerializeException e) {
            throw new RuntimeException(e);
        }
    }
}

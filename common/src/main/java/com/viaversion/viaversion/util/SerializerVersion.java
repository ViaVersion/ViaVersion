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

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.google.gson.JsonElement;
import net.lenni0451.mcstructs.snbt.SNbtSerializer;
import net.lenni0451.mcstructs.text.ATextComponent;
import net.lenni0451.mcstructs.text.serializer.TextComponentCodec;
import net.lenni0451.mcstructs.text.serializer.TextComponentSerializer;

public enum SerializerVersion {
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
    V1_20_3(TextComponentCodec.V1_20_3, SNbtSerializer.V1_14);

    final TextComponentSerializer jsonSerializer;
    final SNbtSerializer<CompoundTag> snbtSerializer;

    SerializerVersion(final TextComponentSerializer jsonSerializer, final SNbtSerializer<CompoundTag> snbtSerializer) {
        this.jsonSerializer = jsonSerializer;
        this.snbtSerializer = snbtSerializer;
    }

    SerializerVersion(final TextComponentCodec codec, final SNbtSerializer<CompoundTag> snbtSerializer) {
        this.jsonSerializer = codec.asSerializer();
        this.snbtSerializer = snbtSerializer;
    }

    public JsonElement toJson(final ATextComponent component) {
        return jsonSerializer.serializeJson(component);
    }
}
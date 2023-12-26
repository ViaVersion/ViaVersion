/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2023 ViaVersion and contributors
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
package com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2.util;

import com.github.steveice10.opennbt.tag.builtin.Tag;
import com.google.gson.JsonElement;
import com.viaversion.viaversion.api.Via;
import net.lenni0451.mcstructs.text.ATextComponent;
import net.lenni0451.mcstructs.text.serializer.TextComponentCodec;
import net.lenni0451.mcstructs.text.serializer.TextComponentSerializer;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class ComponentConverter {

    private static final boolean DEBUG = Boolean.getBoolean("viaversion.debug.components");

    public static @Nullable JsonElement tagComponentToJson(@Nullable final Tag tag) {
        if (DEBUG) {
            Via.getPlatform().getLogger().info("Converting tag to json: " + tag);
        }

        final ATextComponent component = TextComponentCodec.V1_20_3.deserializeNbtTree(NBTConverter.viaToMcStructs(tag));
        return component != null ? TextComponentSerializer.V1_19_4.serializeJson(component) : null;
    }

    public static @Nullable Tag jsonComponentToTag(@Nullable final JsonElement element) {
        if (DEBUG) {
            Via.getPlatform().getLogger().info("Converting json to tag: " + element);
        }

        final ATextComponent component = TextComponentSerializer.V1_19_4.deserialize(element);
        return component != null ? NBTConverter.mcStructsToVia(TextComponentCodec.V1_20_3.serializeNbt(component)) : null;
    }
}

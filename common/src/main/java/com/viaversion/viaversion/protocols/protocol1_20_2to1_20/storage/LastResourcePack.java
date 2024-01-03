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
package com.viaversion.viaversion.protocols.protocol1_20_2to1_20.storage;

import com.google.gson.JsonElement;
import com.viaversion.viaversion.api.connection.StorableObject;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class LastResourcePack implements StorableObject {

    private final String url;
    private final String hash;
    private final boolean required;
    private final JsonElement prompt;

    public LastResourcePack(final String url, final String hash, final boolean required, @Nullable final JsonElement prompt) {
        this.url = url;
        this.hash = hash;
        this.required = required;
        this.prompt = prompt;
    }

    public String url() {
        return url;
    }

    public String hash() {
        return hash;
    }

    public boolean required() {
        return required;
    }

    public @Nullable JsonElement prompt() {
        return prompt;
    }

    @Override
    public boolean clearOnServerSwitch() {
        return false;
    }
}

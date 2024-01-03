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
package com.viaversion.viaversion.unsupported;

import com.google.common.base.Preconditions;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.platform.UnsupportedSoftware;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class UnsupportedPlugin implements UnsupportedSoftware {

    private final String name;
    private final List<String> identifiers;
    private final String reason;

    public UnsupportedPlugin(final String name, final List<String> identifiers, final String reason) {
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(reason);
        Preconditions.checkArgument(!identifiers.isEmpty());
        this.name = name;
        this.identifiers = Collections.unmodifiableList(identifiers);
        this.reason = reason;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getReason() {
        return reason;
    }

    @Override
    public final @Nullable String match() {
        for (final String identifier : identifiers) {
            if (Via.getPlatform().hasPlugin(identifier)) {
                return identifier;
            }
        }
        return null;
    }

    public static final class Builder {

        private final List<String> identifiers = new ArrayList<>();
        private String name;
        private String reason;

        public Builder name(final String name) {
            this.name = name;
            return this;
        }

        public Builder reason(final String reason) {
            this.reason = reason;
            return this;
        }

        public Builder addPlugin(final String identifier) {
            identifiers.add(identifier);
            return this;
        }

        public UnsupportedPlugin build() {
            return new UnsupportedPlugin(name, identifiers, reason);
        }
    }

    public static final class Reason {

        public static final String SECURE_CHAT_BYPASS = "Instead of doing the obvious (or nothing at all), these kinds of plugins completely break chat message handling, usually then also breaking other plugins.";
    }
}

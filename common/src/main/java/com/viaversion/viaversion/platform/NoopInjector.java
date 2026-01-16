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
package com.viaversion.viaversion.platform;

import com.google.gson.JsonObject;
import com.viaversion.viaversion.api.platform.ViaInjector;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import java.util.SortedSet;

/**
 * Default implementation for {@link ViaInjector} on standalone proxies/platforms supporting all {@link ProtocolVersion}s and not requiring additional injection.
 */
public class NoopInjector implements ViaInjector {

    @Override
    public void inject() {
    }

    @Override
    public void uninject() {
    }

    @Override
    public ProtocolVersion getServerProtocolVersion() throws Exception {
        return getServerProtocolVersions().first();
    }

    @Override
    public SortedSet<ProtocolVersion> getServerProtocolVersions() {
        // Collect all registered protocol versions
        final SortedSet<ProtocolVersion> versions = new ObjectLinkedOpenHashSet<>();
        versions.addAll(ProtocolVersion.getProtocols());
        return versions;
    }

    @Override
    public JsonObject getDump() {
        return new JsonObject();
    }
}

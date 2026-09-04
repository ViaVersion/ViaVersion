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
package com.viaversion.viaversion.common.hash;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.minecraft.codec.CodecContext;
import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.minecraft.item.data.DyedColor;
import com.viaversion.viaversion.api.minecraft.item.data.ToolProperties;
import com.viaversion.viaversion.api.minecraft.item.data.ToolRule;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.version.VersionedTypesHolder;
import com.viaversion.viaversion.codec.CodecRegistryContext;
import com.viaversion.viaversion.codec.hash.HashFunction;
import com.viaversion.viaversion.codec.hash.HashOps;
import com.viaversion.viaversion.common.PlatformTestBase;
import com.viaversion.viaversion.connection.UserConnectionImpl;
import com.viaversion.viaversion.protocols.v1_21_6to1_21_7.Protocol1_21_6To1_21_7;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StructuredDataOpsTest extends PlatformTestBase {

    /**
     * Protocols storing an item hasher hash items in both their own and their mapped component versions,
     * so every component key of either side has to be writable via ops or declared unsupported.
     */
    @Test
    void testHashableComponents() {
        final List<String> missing = new ArrayList<>();
        final List<String> checked = new ArrayList<>();
        final UserConnectionImpl connection = new UserConnectionImpl(null);
        for (final Protocol<?, ?, ?, ?> protocol : Via.getManager().getProtocolManager().getProtocols()) {
            protocol.init(connection);
            if (connection.getItemHasher(protocol) == null) {
                continue;
            }
            checked.add(protocol.getClass().getSimpleName());
            collectMissing(protocol, protocol.types(), missing);
            collectMissing(protocol, protocol.mappedTypes(), missing);
        }
        Assertions.assertFalse(checked.isEmpty(), "No protocol with an item hasher found");
        Assertions.assertTrue(missing.isEmpty(), "Components without an ops writer: " + missing);
    }

    private void collectMissing(final Protocol<?, ?, ?, ?> protocol, final VersionedTypesHolder types, final List<String> missing) {
        if (types == null) {
            return;
        }

        for (final StructuredDataKey<?> key : types.structuredDataKeys().keys()) {
            if (types.structuredDataKeys().supportsOps(key) && !writesOps(key.type())) {
                missing.add(protocol.getClass().getSimpleName() + "/" + key.identifier());
            }
        }
    }

    /**
     * A component the backwards conversion left alone has to hash the same in either version,
     * or every item carrying it gets backed up into custom_data for nothing.
     */
    @Test
    void testLegacyHashesMatch() {
        final ToolProperties tool = new ToolProperties(new ToolRule[0], 1F, 1, true);
        Assertions.assertEquals(hash(ToolProperties.TYPE1_21_5, tool), hash(ToolProperties.TYPE1_20_5, tool), "tool hash mismatch");

        // show_in_tooltip lives in tooltip_display from 1.21.5 on and is hashed there, not here
        Assertions.assertEquals(hash(DyedColor.TYPE1_21_5, new DyedColor(0xFF0000)), hash(DyedColor.TYPE1_20_5, new DyedColor(0xFF0000, false)), "dyed_color hash mismatch");
    }

    private <T> int hash(final Type<T> type, final T value) {
        final UserConnectionImpl connection = new UserConnectionImpl(null);
        final Protocol<?, ?, ?, ?> protocol = Via.getManager().getProtocolManager().getProtocol(Protocol1_21_6To1_21_7.class);
        protocol.init(connection);
        final HashOps hasher = new HashOps(new CodecRegistryContext(null, CodecContext.RegistryAccess.of(protocol, connection), false), HashFunction.crc32c());
        hasher.write(type, value);
        return hasher.hash();
    }

    private static boolean writesOps(final Type<?> type) {
        for (Class<?> c = type.getClass(); c != null && c != Type.class; c = c.getSuperclass()) {
            for (final Method method : c.getDeclaredMethods()) {
                if (method.getName().equals("write") && method.getParameterCount() == 2
                    && method.getParameterTypes()[0] == Ops.class && !method.isSynthetic()) {
                    return true;
                }
            }
        }
        return false;
    }
}

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
package com.viaversion.viaversion.common.type;

import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;

/**
 * Hands arbitrary bytes to every reader in {@link Types}.
 *
 * <p>These readers are the first thing a packet meets, before anything has had the chance to
 * decide it is nonsense, and the bytes they are given come from whoever is on the other end of
 * the connection. Rejecting a malformed packet is the correct outcome and the catch below treats
 * it as a pass. What it does not swallow is the JVM giving up: an allocation sized from the wire,
 * a recursion with no floor, a loop that does not end. Those reach Jazzer and fail the run.
 *
 * <p>Without a corpus this runs as an ordinary regression test and costs nothing in CI. To
 * actually fuzz it:
 *
 * <pre>{@code ./gradlew :viaversion-common:test --tests '*TypeReadFuzzTest*' -Djazzer.fuzz=true}</pre>
 */
final class TypeReadFuzzTest {

    private static final List<Type<?>> TYPES = collectTypes();

    /**
     * Every {@link Type} constant on {@link Types}, read reflectively so a type added later is
     * covered without anyone remembering to add it here.
     */
    private static List<Type<?>> collectTypes() {
        final List<Type<?>> types = new ArrayList<>();
        for (final Field field : Types.class.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()) || !Type.class.isAssignableFrom(field.getType())) {
                continue;
            }
            try {
                field.setAccessible(true);
                if (field.get(null) instanceof final Type<?> type) {
                    types.add(type);
                }
            } catch (final ReflectiveOperationException e) {
                throw new IllegalStateException("Could not read " + field.getName(), e);
            }
        }
        // An empty list would make the fuzz target silently test nothing and pass forever.
        Assertions.assertFalse(types.isEmpty(), "No types collected from Types");
        return types;
    }

    @FuzzTest(maxDuration = "10s")
    void readsArbitraryBytes(final FuzzedDataProvider data) {
        final Type<?> type = TYPES.get(data.consumeInt(0, TYPES.size() - 1));
        final ByteBuf buffer = Unpooled.wrappedBuffer(data.consumeRemainingAsBytes());
        try {
            type.read(buffer);
        } catch (final RuntimeException expected) {
            // Refusing input it cannot make sense of is what a reader is supposed to do.
        }
    }
}

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
package com.viaversion.viaversion.protocol.shared_registration;

import com.google.common.base.Preconditions;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.ServerboundPacketType;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.protocol.version.VersionType;
import com.viaversion.viaversion.protocol.shared_registration.SharedRegistrations.RegistrationAction;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public final class RegistrationBuilder {
    private final List<VersionedTemplateGroup> groups = new ArrayList<>();
    private final SharedRegistrations registrations;
    private VersionType versionType;

    RegistrationBuilder(final SharedRegistrations registrations) {
        this.registrations = registrations;
    }

    /**
     * Adds a registration for an open-ended version range.
     *
     * @param min    minimum version, inclusive
     * @param action registration action
     * @return this
     */
    public RegistrationBuilder since(final ProtocolVersion min, final RegistrationAction<?, ?> action) {
        checkVersionType(min);
        groups.add(new VersionedTemplateGroup(action, min, null));
        return this;
    }

    /**
     * Adds a registration for a version range.
     *
     * @param min    minimum version, inclusive
     * @param max    maximum version, exclusive
     * @param action registration action
     * @return this
     */
    public RegistrationBuilder range(final ProtocolVersion min, final ProtocolVersion max, final RegistrationAction<?, ?> action) {
        checkVersionType(min);
        checkVersionType(max);
        groups.add(new VersionedTemplateGroup(action, min, max));
        return this;
    }

    public <CU extends ClientboundPacketType, SU extends ServerboundPacketType> RegistrationBuilder ranges(
        final ProtocolVersion min, final Consumer<RangesBuilder<CU, SU>> consumer
    ) {
        consumer.accept(new RangesBuilder<>(min));
        return this;
    }

    public <CU extends ClientboundPacketType, SU extends ServerboundPacketType, R> RegistrationBuilder ranges(
        final Function<RegistrationContext<CU, SU>, R> adapter, final ProtocolVersion min,
        final Consumer<TypedRangesBuilder<CU, SU, R>> consumer
    ) {
        consumer.accept(new TypedRangesBuilder<>(adapter, min));
        return this;
    }

    public void register() {
        registrations.register(versionType, groups);
    }

    private void checkVersionType(final ProtocolVersion version) {
        if (versionType == null) {
            versionType = version.getVersionType();
        } else if (versionType != version.getVersionType()) {
            throw new IllegalArgumentException("Cannot mix different version types in the same registration builder");
        }
    }

    public class RangesBuilder<CU extends ClientboundPacketType, SU extends ServerboundPacketType> {
        private ProtocolVersion currentMin;
        private boolean completed;

        private RangesBuilder(final ProtocolVersion min) {
            this.currentMin = min;
        }

        public RangesBuilder<CU, SU> to(final ProtocolVersion max, final RegistrationAction<CU, SU> action) {
            Preconditions.checkState(!completed, "Range chain already completed");
            RegistrationBuilder.this.range(currentMin, max, action);
            currentMin = max;
            return this;
        }

        public RegistrationBuilder since(final RegistrationAction<CU, SU> action) {
            Preconditions.checkState(!completed, "Range chain already completed");
            completed = true;
            return RegistrationBuilder.this.since(currentMin, action);
        }
    }

    public final class TypedRangesBuilder<CU extends ClientboundPacketType, SU extends ServerboundPacketType, R> extends RangesBuilder<CU, SU> {
        private final Function<RegistrationContext<CU, SU>, R> adapter;

        private TypedRangesBuilder(final Function<RegistrationContext<CU, SU>, R> adapter, final ProtocolVersion min) {
            super(min);
            this.adapter = adapter;
        }

        public TypedRangesBuilder<CU, SU, R> to(final ProtocolVersion max, final TypedRegistrationAction<CU, SU, R> action) {
            to(max, wrapAction(action));
            return this;
        }

        public TypedRangesBuilder<CU, SU, R> since(final TypedRegistrationAction<CU, SU, R> action) {
            since(wrapAction(action));
            return this;
        }

        private RegistrationAction<CU, SU> wrapAction(final TypedRegistrationAction<CU, SU, R> action) {
            return ctx -> {
                final R rewriter = adapter.apply(ctx);
                if (rewriter != null) {
                    action.accept(ctx, rewriter);
                }
            };
        }
    }

    @FunctionalInterface
    public interface TypedRegistrationAction<CU extends ClientboundPacketType, SU extends ServerboundPacketType, R> {

        void accept(RegistrationContext<CU, SU> ctx, R r);
    }

}

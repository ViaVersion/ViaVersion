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
package com.viaversion.viaversion.protocol.shared_registration.def;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.protocol.shared_registration.SharedRegistrations;

public final class DefaultRegistrations {

    public static void apply() {
        SharedRegistrations.defaultRegistrations().registrations()
            .ranges(EntityRegistrations::entity, ProtocolVersion.v1_13, b -> b
                .to(ProtocolVersion.v1_14_4, EntityRegistrations::registerEntityPackets1_13)
                .to(ProtocolVersion.v1_16_2, EntityRegistrations::registerEntityPackets1_14_4)
                .to(ProtocolVersion.v1_17_1, EntityRegistrations::registerEntityPackets1_16_2)
                .to(ProtocolVersion.v1_19, EntityRegistrations::registerEntityPackets1_17_1)
                .to(ProtocolVersion.v1_20_5, EntityRegistrations::registerEntityPackets1_19)
                .to(ProtocolVersion.v1_21_4, EntityRegistrations::registerEntityPackets1_20_5)
                .to(ProtocolVersion.v1_21_9, EntityRegistrations::registerEntityPackets1_21_4)
                .since(EntityRegistrations::registerEntityPackets1_21_9)
            )

            .ranges(ItemRegistrations::item, ProtocolVersion.v1_13, b -> b
                .to(ProtocolVersion.v1_14, ItemRegistrations::registerItemPackets1_13)
                .to(ProtocolVersion.v1_14_4, ItemRegistrations::registerItemPackets1_14)
                .to(ProtocolVersion.v1_16, ItemRegistrations::registerItemPackets1_14_4)
                .to(ProtocolVersion.v1_17_1, ItemRegistrations::registerItemPackets1_16)
                .to(ProtocolVersion.v1_19_1, ItemRegistrations::registerItemPackets1_17_1)
                .to(ProtocolVersion.v1_20_3, ItemRegistrations::registerItemPackets1_19_1)
                .to(ProtocolVersion.v1_20_5, ItemRegistrations::registerItemPackets1_20_3)
                .to(ProtocolVersion.v1_21, ItemRegistrations::registerItemPackets1_20_5)
                .to(ProtocolVersion.v1_21_2, ItemRegistrations::registerItemPackets1_21)
                .to(ProtocolVersion.v1_21_5, ItemRegistrations::registerItemPackets1_21_2)
            )
            .ranges(ItemRegistrations::structuredItem, ProtocolVersion.v1_21_5, b -> b
                .to(ProtocolVersion.v1_21_6, ItemRegistrations::registerItemPackets1_21_5)
                .since(ItemRegistrations::registerItemPackets1_21_6)
            )

            .ranges(BlockRegistrations::block, ProtocolVersion.v1_13, steps -> steps
                .to(ProtocolVersion.v1_14_4, BlockRegistrations::registerBlockPackets1_13)
                .to(ProtocolVersion.v1_16_2, BlockRegistrations::registerBlockPackets1_14_4)
                .to(ProtocolVersion.v1_18, BlockRegistrations::registerBlockPackets1_16_2)
                .to(ProtocolVersion.v1_19, BlockRegistrations::registerBlockPackets1_18)
                .to(ProtocolVersion.v1_20, BlockRegistrations::registerBlockPackets1_19)
                .to(ProtocolVersion.v1_21, BlockRegistrations::registerBlockPackets1_20)
                .since(BlockRegistrations::registerBlockPackets1_21)
            )

            .ranges(ParticleRegistrations::particle, ProtocolVersion.v1_13, steps -> steps
                .to(ProtocolVersion.v1_15_2, ParticleRegistrations::registerParticlePackets1_13)
                .to(ProtocolVersion.v1_19, ParticleRegistrations::registerParticlePackets1_15_2)
                .to(ProtocolVersion.v1_20, ParticleRegistrations::registerParticlePackets1_19)
                .to(ProtocolVersion.v1_20_5, ParticleRegistrations::registerParticlePackets1_20)
                .to(ProtocolVersion.v1_21_2, ParticleRegistrations::registerParticlePackets1_20_5)
                .to(ProtocolVersion.v1_21_4, ParticleRegistrations::registerParticlePackets1_21_2)
                .to(ProtocolVersion.v1_21_9, ParticleRegistrations::registerParticlePackets1_21_4)
                .since(ParticleRegistrations::registerParticlePackets1_21_9)
            )

            .ranges(TextComponentRegistrations::text, ProtocolVersion.v1_12_2, b -> b
                .to(ProtocolVersion.v1_14, TextComponentRegistrations::registerComponents1_12_2)
                .to(ProtocolVersion.v1_16_2, TextComponentRegistrations::registerComponents1_14)
                .to(ProtocolVersion.v1_17, TextComponentRegistrations::registerComponents1_16_2)
                .to(ProtocolVersion.v1_19, TextComponentRegistrations::registerComponents1_17)
                .to(ProtocolVersion.v1_20_3, TextComponentRegistrations::registerComponents1_19)
                .to(ProtocolVersion.v1_20_5, TextComponentRegistrations::registerComponents1_20_3)
                .to(ProtocolVersion.v1_21, TextComponentRegistrations::registerComponents1_20_5)
                .to(ProtocolVersion.v1_21_4, TextComponentRegistrations::registerComponents1_21)
                .to(ProtocolVersion.v1_21_5, TextComponentRegistrations::registerComponents1_21_4)
            )
            .ranges(TextComponentRegistrations::nbtText, ProtocolVersion.v1_21_5, b -> b
                .since(TextComponentRegistrations::registerComponents1_21_5)
            )

            .ranges(ProtocolVersion.v1_17_1, sbeps -> sbeps
                .to(ProtocolVersion.v1_20_2, RegistryRegistrations::registerTags1_17_1)
                .since(RegistryRegistrations::registerTags1_20_2)
            )

            .ranges(ProtocolVersion.v1_10, b -> b
                .to(ProtocolVersion.v1_14, RegistryRegistrations::registerSounds1_10)
                .to(ProtocolVersion.v1_19_3, RegistryRegistrations::registerSounds1_14)
                .since(RegistryRegistrations::registerSounds1_19_3)
            )

            .since(ProtocolVersion.v1_13, RegistryRegistrations::registerStatistics1_13)
            .since(ProtocolVersion.v1_19, RegistryRegistrations::registerCommands1_19)
            .since(ProtocolVersion.v1_21, RegistryRegistrations::registerRegistryData1_21)
            .since(ProtocolVersion.v1_21, RegistryRegistrations::registerAttributes1_21)
            .since(ProtocolVersion.v1_21_2, RegistryRegistrations::registerRecipePackets1_21_2)

            .register();
    }
}

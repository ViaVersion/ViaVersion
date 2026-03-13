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

import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.ServerboundPacketType;
import com.viaversion.viaversion.protocol.shared_registration.PacketBound;
import com.viaversion.viaversion.protocol.shared_registration.RegistrationContext;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.packet.ClientboundPackets1_13;
import com.viaversion.viaversion.protocols.v1_14_3to1_14_4.packet.ClientboundPackets1_14_4;
import com.viaversion.viaversion.protocols.v1_16_1to1_16_2.packet.ClientboundPackets1_16_2;
import com.viaversion.viaversion.protocols.v1_17_1to1_18.packet.ClientboundPackets1_18;
import com.viaversion.viaversion.protocols.v1_18_2to1_19.packet.ClientboundPackets1_19;
import com.viaversion.viaversion.protocols.v1_19_3to1_19_4.packet.ClientboundPackets1_19_4;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundPackets1_21;
import com.viaversion.viaversion.rewriter.BlockRewriter;
import org.checkerframework.checker.nullness.qual.Nullable;

final class BlockRegistrations {

    static <CU extends ClientboundPacketType> void registerBlockPackets1_13(final RegistrationContext<CU, ?> ctx, final BlockRewriter<CU> br) {
        common1_13(ctx, br);
        ctx.clientbound(ClientboundPackets1_13.CHUNK_BLOCKS_UPDATE, br::registerChunkBlocksUpdate);
    }

    private static <CU extends ClientboundPacketType> void common1_13(RegistrationContext<CU, ?> ctx, BlockRewriter<CU> br) {
        ctx.clientbound(ClientboundPackets1_13.BLOCK_EVENT, br::registerBlockEvent);
        ctx.clientbound(ClientboundPackets1_13.BLOCK_UPDATE, br::registerBlockUpdate);
        ctx.clientbound(ClientboundPackets1_13.LEVEL_EVENT, br::registerLevelEvent1_13);
    }

    static <CU extends ClientboundPacketType> void registerBlockPackets1_14_4(final RegistrationContext<CU, ?> ctx, final BlockRewriter<CU> br) {
        common1_13(ctx, br);
        ctx.clientbound(ClientboundPackets1_14_4.CHUNK_BLOCKS_UPDATE, br::registerChunkBlocksUpdate, PacketBound.REMOVED_AT_MAX);
        ctx.clientbound(ClientboundPackets1_14_4.BLOCK_BREAK_ACK, br::registerBlockBreakAck, PacketBound.ADDED_AT_MIN);
    }

    static <CU extends ClientboundPacketType> void registerBlockPackets1_16_2(final RegistrationContext<CU, ?> ctx, final BlockRewriter<CU> br) {
        common1_13(ctx, br);
        ctx.clientbound(ClientboundPackets1_16_2.BLOCK_BREAK_ACK, br::registerBlockBreakAck);
        ctx.clientbound(ClientboundPackets1_16_2.SECTION_BLOCKS_UPDATE, br::registerSectionBlocksUpdate, PacketBound.ADDED_AT_MIN);
    }

    static <CU extends ClientboundPacketType> void registerBlockPackets1_18(final RegistrationContext<CU, ?> ctx, final BlockRewriter<CU> br) {
        common1_13(ctx, br);
        ctx.clientbound(ClientboundPackets1_18.BLOCK_BREAK_ACK, br::registerBlockBreakAck, PacketBound.REMOVED_AT_MAX);
        ctx.clientbound(ClientboundPackets1_18.LEVEL_CHUNK_WITH_LIGHT, br::registerLevelChunk1_18, PacketBound.ADDED_AT_MIN);
        ctx.clientbound(ClientboundPackets1_18.SECTION_BLOCKS_UPDATE, br::registerSectionBlocksUpdate);
        ctx.clientbound(ClientboundPackets1_18.BLOCK_ENTITY_DATA, br::registerBlockEntityData1_18);
    }

    static <CU extends ClientboundPacketType> void registerBlockPackets1_19(final RegistrationContext<CU, ?> ctx, final BlockRewriter<CU> br) {
        common1_19(ctx, br);
        ctx.clientbound(ClientboundPackets1_19.LEVEL_EVENT, br::registerLevelEvent1_13);
        ctx.clientbound(ClientboundPackets1_19.SECTION_BLOCKS_UPDATE, br::registerSectionBlocksUpdate);
    }

    private static <CU extends ClientboundPacketType> void common1_19(RegistrationContext<CU, ?> ctx, BlockRewriter<CU> br) {
        ctx.clientbound(ClientboundPackets1_19.BLOCK_EVENT, br::registerBlockEvent);
        ctx.clientbound(ClientboundPackets1_19.BLOCK_UPDATE, br::registerBlockUpdate);
        ctx.clientbound(ClientboundPackets1_19.LEVEL_CHUNK_WITH_LIGHT, br::registerLevelChunk1_18);
        ctx.clientbound(ClientboundPackets1_19.BLOCK_ENTITY_DATA, br::registerBlockEntityData1_18);
    }

    static <CU extends ClientboundPacketType> void registerBlockPackets1_20(final RegistrationContext<CU, ?> ctx, final BlockRewriter<CU> br) {
        common1_19(ctx, br);
        ctx.clientbound(ClientboundPackets1_19_4.LEVEL_EVENT, br::registerLevelEvent1_13);
        ctx.clientbound(ClientboundPackets1_19_4.SECTION_BLOCKS_UPDATE, br::registerSectionBlocksUpdate1_20);
    }

    static <CU extends ClientboundPacketType> void registerBlockPackets1_21(final RegistrationContext<CU, ?> ctx, final BlockRewriter<CU> br) {
        common1_19(ctx, br);
        ctx.clientbound(ClientboundPackets1_21.LEVEL_EVENT, br::registerLevelEvent1_21);
        ctx.clientbound(ClientboundPackets1_21.SECTION_BLOCKS_UPDATE, br::registerSectionBlocksUpdate1_20);
    }

    static @Nullable <CU extends ClientboundPacketType, SU extends ServerboundPacketType> BlockRewriter<CU> block(final RegistrationContext<CU, SU> ctx) {
        return ctx.protocol().getBlockRewriter();
    }
}

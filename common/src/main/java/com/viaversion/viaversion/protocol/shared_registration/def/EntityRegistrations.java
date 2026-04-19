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
import com.viaversion.viaversion.protocols.v1_18_2to1_19.packet.ClientboundPackets1_19;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ClientboundPackets1_20_5;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.packet.ClientboundPackets1_21_9;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ClientboundPackets1_21_2;
import com.viaversion.viaversion.protocols.v1_8to1_9.packet.ClientboundPackets1_9;
import com.viaversion.viaversion.rewriter.EntityRewriter;
import org.checkerframework.checker.nullness.qual.Nullable;

final class EntityRegistrations {

    static <CU extends ClientboundPacketType> void registerEntityPackets1_13(final RegistrationContext<CU, ?> ctx, final EntityRewriter<CU, ?> er) {
        ctx.clientbound(ClientboundPackets1_13.REMOVE_ENTITIES, er::registerRemoveEntities);
    }

    static <CU extends ClientboundPacketType> void registerEntityPackets1_14_4(final RegistrationContext<CU, ?> ctx, final EntityRewriter<CU, ?> er) {
        ctx.clientbound(ClientboundPackets1_14_4.REMOVE_ENTITIES, er::registerRemoveEntities);
        ctx.clientbound(ClientboundPackets1_14_4.ADD_ENTITY, er::registerTrackerWithData);
        ctx.clientbound(ClientboundPackets1_14_4.ADD_MOB, er::registerTracker);
    }

    static <CU extends ClientboundPacketType> void registerEntityPackets1_16_2(final RegistrationContext<CU, ?> ctx, final EntityRewriter<CU, ?> er) {
        // remove_entities got temporarily changed to only take a single entity...
        ctx.clientbound(ClientboundPackets1_14_4.ADD_ENTITY, er::registerTrackerWithData);
        ctx.clientbound(ClientboundPackets1_14_4.ADD_MOB, er::registerTracker);
    }

    static <CU extends ClientboundPacketType> void registerEntityPackets1_17_1(final RegistrationContext<CU, ?> ctx, final EntityRewriter<CU, ?> er) {
        ctx.clientbound(ClientboundPackets1_14_4.REMOVE_ENTITIES, er::registerRemoveEntities);
        ctx.clientbound(ClientboundPackets1_14_4.ADD_ENTITY, er::registerTrackerWithData);
        ctx.clientbound(ClientboundPackets1_14_4.ADD_MOB, er::registerTracker, PacketBound.REMOVED_AT_MAX);
    }

    static <CU extends ClientboundPacketType> void registerEntityPackets1_19(final RegistrationContext<CU, ?> ctx, final EntityRewriter<CU, ?> er) {
        ctx.clientbound(ClientboundPackets1_9.REMOVE_ENTITIES, er::registerRemoveEntities);
        ctx.clientbound(ClientboundPackets1_19.ADD_ENTITY, er::registerTrackerWithData1_19);
    }

    static <CU extends ClientboundPacketType> void registerEntityPackets1_20_5(final RegistrationContext<CU, ?> ctx, final EntityRewriter<CU, ?> er) {
        common1_20_5(ctx, er);
        ctx.clientbound(ClientboundPackets1_19.ADD_ENTITY, er::registerTrackerWithData1_19);
    }

    static <CU extends ClientboundPacketType> void registerEntityPackets1_21_4(final RegistrationContext<CU, ?> ctx, final EntityRewriter<CU, ?> er) {
        common1_20_5(ctx, er);
        registerGameModeTrackers1_21_4(ctx, er);
        ctx.clientbound(ClientboundPackets1_21_2.ADD_ENTITY, er::registerTrackerWithData1_19);
    }

    static <CU extends ClientboundPacketType> void registerEntityPackets1_21_9(final RegistrationContext<CU, ?> ctx, final EntityRewriter<CU, ?> er) {
        common1_20_5(ctx, er);
        registerGameModeTrackers1_21_4(ctx, er);
        ctx.clientbound(ClientboundPackets1_21_9.ADD_ENTITY, er::registerTrackerWithData1_21_9);
    }

    private static <CU extends ClientboundPacketType> void common1_20_5(final RegistrationContext<CU, ?> ctx, final EntityRewriter<CU, ?> er) {
        ctx.clientbound(ClientboundPackets1_20_5.SET_ENTITY_DATA, er::registerSetEntityData);
        ctx.clientbound(ClientboundPackets1_20_5.REMOVE_ENTITIES, er::registerRemoveEntities);
        ctx.clientbound(ClientboundPackets1_20_5.LOGIN, er::registerLogin1_20_5);
        ctx.clientbound(ClientboundPackets1_20_5.RESPAWN, er::registerRespawn1_20_5);
    }

    private static <CU extends ClientboundPacketType> void registerGameModeTrackers1_21_4(final RegistrationContext<CU, ?> ctx, final EntityRewriter<CU, ?> er) {
        ctx.clientbound(ClientboundPackets1_21_2.GAME_EVENT, er::registerGameEvent);
        ctx.clientbound(ClientboundPackets1_21_2.PLAYER_ABILITIES, er::registerPlayerAbilities);
    }


    static @Nullable <CU extends ClientboundPacketType, SU extends ServerboundPacketType> EntityRewriter<CU, ?> entity(final RegistrationContext<CU, SU> ctx) {
        return (EntityRewriter<CU, ?>) ctx.protocol().getEntityRewriter();
    }
}

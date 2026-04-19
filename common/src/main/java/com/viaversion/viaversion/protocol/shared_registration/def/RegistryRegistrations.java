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

import com.viaversion.viaversion.api.data.Mappings;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.protocol.shared_registration.PacketBound;
import com.viaversion.viaversion.protocol.shared_registration.RegistrationContext;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.packet.ClientboundPackets1_13;
import com.viaversion.viaversion.protocols.v1_13_2to1_14.packet.ClientboundPackets1_14;
import com.viaversion.viaversion.protocols.v1_17to1_17_1.packet.ClientboundPackets1_17_1;
import com.viaversion.viaversion.protocols.v1_19_1to1_19_3.packet.ClientboundPackets1_19_3;
import com.viaversion.viaversion.protocols.v1_19_3to1_19_4.rewriter.CommandRewriter1_19_4;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundConfigurationPackets1_21;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundPackets1_21;
import com.viaversion.viaversion.protocols.v1_20to1_20_2.packet.ClientboundConfigurationPackets1_20_2;
import com.viaversion.viaversion.protocols.v1_20to1_20_2.packet.ClientboundPackets1_20_2;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ClientboundPackets1_21_2;
import com.viaversion.viaversion.protocols.v1_9_1to1_9_3.packet.ClientboundPackets1_9_3;
import com.viaversion.viaversion.rewriter.AttributeRewriter;
import com.viaversion.viaversion.rewriter.RecipeDisplayRewriter;
import com.viaversion.viaversion.rewriter.SoundRewriter;
import com.viaversion.viaversion.rewriter.StatisticsRewriter;
import com.viaversion.viaversion.rewriter.TagRewriter;

final class RegistryRegistrations {

    static <CU extends ClientboundPacketType> void registerTags1_17_1(final RegistrationContext<CU, ?> ctx) {
        if (ctx.protocol().getTagRewriter() instanceof final TagRewriter tagRewriter) {
            ctx.clientbound(ClientboundPackets1_17_1.UPDATE_TAGS, tagRewriter::registerGeneric);
        }
    }

    static <CU extends ClientboundPacketType> void registerTags1_20_2(final RegistrationContext<CU, ?> ctx) {
        if (ctx.protocol().getTagRewriter() instanceof final TagRewriter tagRewriter) {
            ctx.clientbound(ClientboundPackets1_20_2.UPDATE_TAGS, tagRewriter::registerGeneric);
            ctx.clientbound(ClientboundConfigurationPackets1_20_2.UPDATE_TAGS, tagRewriter::registerGeneric, PacketBound.ADDED_AT_MIN);
        }
    }


    static <CU extends ClientboundPacketType> void registerRegistryData1_21(final RegistrationContext<CU, ?> ctx) {
        if (ctx.protocol().getRegistryDataRewriter() == null) return;
        ctx.clientboundHandler(ClientboundConfigurationPackets1_21.REGISTRY_DATA, ctx.protocol().getRegistryDataRewriter()::handle);
    }


    static <CU extends ClientboundPacketType> void registerRecipePackets1_21_2(final RegistrationContext<CU, ?> ctx) {
        final RecipeDisplayRewriter<CU> rr = ctx.protocol().getRecipeRewriter();
        if (rr == null) return;
        ctx.clientbound(ClientboundPackets1_21_2.UPDATE_RECIPES, rr::registerUpdateRecipes);
        ctx.clientbound(ClientboundPackets1_21_2.RECIPE_BOOK_ADD, rr::registerRecipeBookAdd, PacketBound.ADDED_AT_MIN);
        ctx.clientbound(ClientboundPackets1_21_2.PLACE_GHOST_RECIPE, rr::registerPlaceGhostRecipe);
    }


    static <CU extends ClientboundPacketType> void registerStatistics1_13(final RegistrationContext<CU, ?> ctx) {
        ctx.clientbound(ClientboundPackets1_13.AWARD_STATS, new StatisticsRewriter<>(ctx.protocol())::register);
    }


    static <CU extends ClientboundPacketType> void registerSounds1_10(final RegistrationContext<CU, ?> ctx) {
        ctx.clientbound(ClientboundPackets1_9_3.SOUND, new SoundRewriter<>(ctx.protocol())::registerSound);
    }

    static <CU extends ClientboundPacketType> void registerSounds1_14(final RegistrationContext<CU, ?> ctx) {
        final SoundRewriter<CU> sr = new SoundRewriter<>(ctx.protocol());
        ctx.clientbound(ClientboundPackets1_14.SOUND, sr::registerSound);
        ctx.clientbound(ClientboundPackets1_14.SOUND_ENTITY, sr::registerSound, PacketBound.ADDED_AT_MIN);
    }

    static <CU extends ClientboundPacketType> void registerSounds1_19_3(final RegistrationContext<CU, ?> ctx) {
        final SoundRewriter<CU> sr = new SoundRewriter<>(ctx.protocol());
        ctx.clientbound(ClientboundPackets1_19_3.SOUND, sr::registerSound1_19_3);
        ctx.clientbound(ClientboundPackets1_19_3.SOUND_ENTITY, sr::registerSound1_19_3);
    }


    static <CU extends ClientboundPacketType> void registerCommands1_19(final RegistrationContext<CU, ?> ctx) {
        if ((ctx.protocol().getMappingData() != null && !Mappings.isIntIdIdentity(ctx.protocol().getMappingData().getArgumentTypeMappings()))
            || ctx.protocol().getRegistryDataRewriter() != null) {
            ctx.clientbound(ClientboundPackets1_19_3.COMMANDS, new CommandRewriter1_19_4<>(ctx.protocol())::registerDeclareCommands1_19);
        }
    }


    static <CU extends ClientboundPacketType> void registerAttributes1_21(final RegistrationContext<CU, ?> ctx) {
        ctx.clientbound(ClientboundPackets1_21.UPDATE_ATTRIBUTES, new AttributeRewriter<>(ctx.protocol())::register1_21);
    }
}

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
package com.viaversion.viaversion.protocols.template;

import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_20_2;
import com.viaversion.viaversion.api.type.types.version.Types1_20_5;
import com.viaversion.viaversion.protocols.v1_20_2to1_20_3.rewriter.RecipeRewriter1_20_3;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ServerboundPacket1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ServerboundPackets1_20_5;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundPacket1_21;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundPackets1_21;
import com.viaversion.viaversion.rewriter.BlockRewriter;
import com.viaversion.viaversion.rewriter.StructuredItemRewriter;

// To replace if needed:
//   ChunkType1_20_2
//   RecipeRewriter1_20_3
final class BlockItemPacketRewriter1_99 extends StructuredItemRewriter<ClientboundPacket1_21, ServerboundPacket1_20_5, Protocol1_99To_98> {

    public BlockItemPacketRewriter1_99(final Protocol1_99To_98 protocol) {
        super(protocol, /*TypesOLD.ITEM, TypesOLD.ITEM_ARRAY, */Types1_20_5.ITEM, Types1_20_5.ITEM_ARRAY);
    }

    @Override
    public void registerPackets() {
        // Register block and block state id changes
        // Other places using block state id mappings: Spawn particle, entity data, entity spawn (falling blocks)
        // Tags and statistics use block (!) ids
        final BlockRewriter<ClientboundPacket1_21> blockRewriter = BlockRewriter.for1_20_2(protocol);
        blockRewriter.registerBlockEvent(ClientboundPackets1_21.BLOCK_EVENT);
        blockRewriter.registerBlockUpdate(ClientboundPackets1_21.BLOCK_UPDATE);
        blockRewriter.registerSectionBlocksUpdate1_20(ClientboundPackets1_21.SECTION_BLOCKS_UPDATE);
        blockRewriter.registerLevelEvent1_21(ClientboundPackets1_21.LEVEL_EVENT, 2001);
        blockRewriter.registerLevelChunk1_19(ClientboundPackets1_21.LEVEL_CHUNK_WITH_LIGHT, ChunkType1_20_2::new);
        blockRewriter.registerBlockEntityData(ClientboundPackets1_21.BLOCK_ENTITY_DATA);

        // Registers item id changes
        // Other places using item ids are: Entity data, tags, statistics, effect
        // registerOpenWindow(ClientboundPackets1_21.OPEN_WINDOW); - If a new container type was added
        registerCooldown(ClientboundPackets1_21.COOLDOWN);
        registerSetContent1_17_1(ClientboundPackets1_21.CONTAINER_SET_CONTENT);
        registerSetSlot1_17_1(ClientboundPackets1_21.CONTAINER_SET_SLOT);
        registerAdvancements1_20_3(ClientboundPackets1_21.UPDATE_ADVANCEMENTS);
        registerSetEquipment(ClientboundPackets1_21.SET_EQUIPMENT);
        registerContainerClick1_17_1(ServerboundPackets1_20_5.CONTAINER_CLICK);
        registerMerchantOffers1_20_5(ClientboundPackets1_21.MERCHANT_OFFERS, Types1_20_5.ITEM_COST, Types1_20_5.ITEM_COST, Types1_20_5.OPTIONAL_ITEM_COST, Types1_20_5.OPTIONAL_ITEM_COST);
        registerSetCreativeModeSlot(ServerboundPackets1_20_5.SET_CREATIVE_MODE_SLOT);
        registerLevelParticles1_20_5(ClientboundPackets1_21.LEVEL_PARTICLES, Types1_20_5.PARTICLE, Types1_20_5.PARTICLE);
        registerExplosion(ClientboundPackets1_21.EXPLODE, Types1_20_5.PARTICLE, Types1_20_5.PARTICLE); // Rewrites the included sound and particles

        new RecipeRewriter1_20_3<>(protocol).register1_20_5(ClientboundPackets1_21.UPDATE_RECIPES);
        // OR do this if serialization of recipes changed and override the relevant method
        // Add new serializers to RecipeRewriter, or extend the last one for changes
        // new RecipeRewriter1_20_3<ClientboundPacket1_21>(this) {}.register1_20_5(ClientboundPackets1_21.DECLARE_RECIPES);
    }
}

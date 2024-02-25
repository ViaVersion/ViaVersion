/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2023 ViaVersion and contributors
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
package com.viaversion.viaversion.protocols.protocol1_20_5to1_20_3.rewriter;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.viaversion.viaversion.api.data.ParticleMappings;
import com.viaversion.viaversion.api.minecraft.Particle;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_20_2;
import com.viaversion.viaversion.api.type.types.version.Types1_20_3;
import com.viaversion.viaversion.api.type.types.version.Types1_20_5;
import com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2.packet.ClientboundPacket1_20_3;
import com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2.packet.ClientboundPackets1_20_3;
import com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2.rewriter.RecipeRewriter1_20_3;
import com.viaversion.viaversion.protocols.protocol1_20_5to1_20_3.Protocol1_20_5To1_20_3;
import com.viaversion.viaversion.protocols.protocol1_20_5to1_20_3.packet.ServerboundPacket1_20_5;
import com.viaversion.viaversion.protocols.protocol1_20_5to1_20_3.packet.ServerboundPackets1_20_5;
import com.viaversion.viaversion.rewriter.BlockRewriter;
import com.viaversion.viaversion.rewriter.ItemRewriter;
import com.viaversion.viaversion.util.Key;

public final class BlockItemPacketRewriter1_20_5 extends ItemRewriter<ClientboundPacket1_20_3, ServerboundPacket1_20_5, Protocol1_20_5To1_20_3> {

    public BlockItemPacketRewriter1_20_5(final Protocol1_20_5To1_20_3 protocol) {
        super(protocol, Type.ITEM1_20_2, Type.ITEM1_20_2_ARRAY);
    }

    @Override
    public void registerPackets() {
        final BlockRewriter<ClientboundPacket1_20_3> blockRewriter = BlockRewriter.for1_20_2(protocol);
        blockRewriter.registerBlockAction(ClientboundPackets1_20_3.BLOCK_ACTION);
        blockRewriter.registerBlockChange(ClientboundPackets1_20_3.BLOCK_CHANGE);
        blockRewriter.registerVarLongMultiBlockChange1_20(ClientboundPackets1_20_3.MULTI_BLOCK_CHANGE);
        blockRewriter.registerEffect(ClientboundPackets1_20_3.EFFECT, 1010, 2001);
        blockRewriter.registerChunkData1_19(ClientboundPackets1_20_3.CHUNK_DATA, ChunkType1_20_2::new);
        protocol.registerClientbound(ClientboundPackets1_20_3.BLOCK_ENTITY_DATA, wrapper -> {
            wrapper.passthrough(Type.POSITION1_14); // Position
            wrapper.passthrough(Type.VAR_INT); // Block entity type

            // No longer nullable
            final CompoundTag tag = wrapper.read(Type.COMPOUND_TAG);
            wrapper.write(Type.COMPOUND_TAG, tag != null ? tag : new CompoundTag());
        });

        registerSetCooldown(ClientboundPackets1_20_3.COOLDOWN);
        registerWindowItems1_17_1(ClientboundPackets1_20_3.WINDOW_ITEMS);
        registerSetSlot1_17_1(ClientboundPackets1_20_3.SET_SLOT);
        registerAdvancements1_20_3(ClientboundPackets1_20_3.ADVANCEMENTS);
        registerEntityEquipmentArray(ClientboundPackets1_20_3.ENTITY_EQUIPMENT);
        registerClickWindow1_17_1(ServerboundPackets1_20_5.CLICK_WINDOW);
        registerCreativeInvAction(ServerboundPackets1_20_5.CREATIVE_INVENTORY_ACTION);
        registerWindowPropertyEnchantmentHandler(ClientboundPackets1_20_3.WINDOW_PROPERTY);

        protocol.registerClientbound(ClientboundPackets1_20_3.SPAWN_PARTICLE, wrapper -> {
            final int particleId = wrapper.read(Type.VAR_INT);

            wrapper.passthrough(Type.BOOLEAN); // Long Distance
            wrapper.passthrough(Type.DOUBLE); // X
            wrapper.passthrough(Type.DOUBLE); // Y
            wrapper.passthrough(Type.DOUBLE); // Z
            wrapper.passthrough(Type.FLOAT); // Offset X
            wrapper.passthrough(Type.FLOAT); // Offset Y
            wrapper.passthrough(Type.FLOAT); // Offset Z
            wrapper.passthrough(Type.FLOAT); // Particle Data
            wrapper.passthrough(Type.INT); // Particle Count

            // Read data and add it to Particle
            final ParticleMappings mappings = protocol.getMappingData().getParticleMappings();
            final Particle particle = new Particle(mappings.getNewId(particleId));
            if (mappings.isBlockParticle(particleId)) {
                final int blockStateId = wrapper.read(Type.VAR_INT);
                particle.add(Type.VAR_INT, protocol.getMappingData().getNewBlockStateId(blockStateId));
            } else if (mappings.isItemParticle(particleId)) {
                final Item item = handleItemToClient(wrapper.read(Type.ITEM1_20_2));
                particle.add(Type.ITEM1_20_2, item);
            }

            wrapper.write(Types1_20_5.PARTICLE, particle);
        });

        protocol.registerClientbound(ClientboundPackets1_20_3.EXPLOSION, wrapper -> {
            wrapper.passthrough(Type.DOUBLE); // X
            wrapper.passthrough(Type.DOUBLE); // Y
            wrapper.passthrough(Type.DOUBLE); // Z
            wrapper.passthrough(Type.FLOAT); // Power
            final int blocks = wrapper.passthrough(Type.VAR_INT);
            for (int i = 0; i < blocks; i++) {
                wrapper.passthrough(Type.BYTE); // Relative X
                wrapper.passthrough(Type.BYTE); // Relative Y
                wrapper.passthrough(Type.BYTE); // Relative Z
            }
            wrapper.passthrough(Type.FLOAT); // Knockback X
            wrapper.passthrough(Type.FLOAT); // Knockback Y
            wrapper.passthrough(Type.FLOAT); // Knockback Z
            wrapper.passthrough(Type.VAR_INT); // Block interaction type

            protocol.getEntityRewriter().rewriteParticle(wrapper, Types1_20_3.PARTICLE, Types1_20_5.PARTICLE); // Small explosion particle
            protocol.getEntityRewriter().rewriteParticle(wrapper, Types1_20_3.PARTICLE, Types1_20_5.PARTICLE); // Large explosion particle

            wrapper.write(Type.VAR_INT, 0); // "Empty" registry id to instead use the resource location that follows after
        });

        protocol.registerClientbound(ClientboundPackets1_20_3.TRADE_LIST, wrapper -> {
            wrapper.passthrough(Type.VAR_INT); // Container id
            final int size = wrapper.passthrough(Type.VAR_INT);
            for (int i = 0; i < size; i++) {
                handleItemToClient(wrapper.passthrough(Type.ITEM1_20_2)); // Input
                handleItemToClient(wrapper.passthrough(Type.ITEM1_20_2)); // Output
                handleItemToClient(wrapper.passthrough(Type.ITEM1_20_2)); // Second Item
                wrapper.passthrough(Type.BOOLEAN); // Trade disabled
                wrapper.passthrough(Type.INT); // Number of tools uses
                wrapper.passthrough(Type.INT); // Maximum number of trade uses
                wrapper.passthrough(Type.INT); // XP
                wrapper.passthrough(Type.INT); // Special price
                wrapper.passthrough(Type.FLOAT); // Price multiplier
                wrapper.passthrough(Type.INT); // Demand

                wrapper.write(Type.BOOLEAN, false); // Ignore tags
            }
        });

        final RecipeRewriter1_20_3<ClientboundPacket1_20_3> recipeRewriter = new RecipeRewriter1_20_3<>(protocol);
        protocol.registerClientbound(ClientboundPackets1_20_3.DECLARE_RECIPES, wrapper -> {
            final int size = wrapper.passthrough(Type.VAR_INT);
            for (int i = 0; i < size; i++) {
                // Change order and write the type as an int
                final String type = wrapper.read(Type.STRING);
                wrapper.passthrough(Type.STRING); // Recipe Identifier

                wrapper.write(Type.VAR_INT, protocol.getMappingData().getRecipeSerializerMappings().mappedId(type));
                recipeRewriter.handleRecipeType(wrapper, Key.stripMinecraftNamespace(type));
            }
        });
    }
}
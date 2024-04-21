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
package com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2.rewriter;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.ListTag;
import com.github.steveice10.opennbt.tag.builtin.StringTag;
import com.github.steveice10.opennbt.tag.builtin.Tag;
import com.google.gson.JsonElement;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.ParticleMappings;
import com.viaversion.viaversion.api.minecraft.Particle;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_20_2;
import com.viaversion.viaversion.api.type.types.version.Types1_20_3;
import com.viaversion.viaversion.protocols.protocol1_20_2to1_20.packet.ClientboundPacket1_20_2;
import com.viaversion.viaversion.protocols.protocol1_20_2to1_20.packet.ClientboundPackets1_20_2;
import com.viaversion.viaversion.protocols.protocol1_20_2to1_20.rewriter.RecipeRewriter1_20_2;
import com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2.Protocol1_20_3To1_20_2;
import com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2.packet.ServerboundPacket1_20_3;
import com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2.packet.ServerboundPackets1_20_3;
import com.viaversion.viaversion.rewriter.BlockRewriter;
import com.viaversion.viaversion.rewriter.ItemRewriter;
import com.viaversion.viaversion.util.ComponentUtil;
import com.viaversion.viaversion.util.Key;
import com.viaversion.viaversion.util.SerializerVersion;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class BlockItemPacketRewriter1_20_3 extends ItemRewriter<ClientboundPacket1_20_2, ServerboundPacket1_20_3, Protocol1_20_3To1_20_2> {

    public BlockItemPacketRewriter1_20_3(final Protocol1_20_3To1_20_2 protocol) {
        super(protocol, Type.ITEM1_20_2, Type.ITEM1_20_2_ARRAY);
    }

    @Override
    public void registerPackets() {
        final BlockRewriter<ClientboundPacket1_20_2> blockRewriter = BlockRewriter.for1_20_2(protocol);
        blockRewriter.registerBlockAction(ClientboundPackets1_20_2.BLOCK_ACTION);
        blockRewriter.registerBlockChange(ClientboundPackets1_20_2.BLOCK_CHANGE);
        blockRewriter.registerVarLongMultiBlockChange1_20(ClientboundPackets1_20_2.MULTI_BLOCK_CHANGE);
        blockRewriter.registerEffect(ClientboundPackets1_20_2.EFFECT, 1010, 2001);
        blockRewriter.registerChunkData1_19(ClientboundPackets1_20_2.CHUNK_DATA, ChunkType1_20_2::new);
        blockRewriter.registerBlockEntityData(ClientboundPackets1_20_2.BLOCK_ENTITY_DATA);

        registerSetCooldown(ClientboundPackets1_20_2.COOLDOWN);
        registerWindowItems1_17_1(ClientboundPackets1_20_2.WINDOW_ITEMS);
        registerSetSlot1_17_1(ClientboundPackets1_20_2.SET_SLOT);
        registerEntityEquipmentArray(ClientboundPackets1_20_2.ENTITY_EQUIPMENT);
        registerClickWindow1_17_1(ServerboundPackets1_20_3.CLICK_WINDOW);
        registerTradeList1_19(ClientboundPackets1_20_2.TRADE_LIST);
        registerCreativeInvAction(ServerboundPackets1_20_3.CREATIVE_INVENTORY_ACTION);
        registerWindowPropertyEnchantmentHandler(ClientboundPackets1_20_2.WINDOW_PROPERTY);

        protocol.registerClientbound(ClientboundPackets1_20_2.SPAWN_PARTICLE, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT); // 0 - Particle ID
                map(Type.BOOLEAN); // 1 - Long Distance
                map(Type.DOUBLE); // 2 - X
                map(Type.DOUBLE); // 3 - Y
                map(Type.DOUBLE); // 4 - Z
                map(Type.FLOAT); // 5 - Offset X
                map(Type.FLOAT); // 6 - Offset Y
                map(Type.FLOAT); // 7 - Offset Z
                map(Type.FLOAT); // 8 - Particle Data
                map(Type.INT); // 9 - Particle Count
                handler(wrapper -> {
                    final int id = wrapper.get(Type.VAR_INT, 0);
                    final ParticleMappings particleMappings = protocol.getMappingData().getParticleMappings();
                    if (id == particleMappings.id("vibration")) {
                        final String resourceLocation = Key.stripMinecraftNamespace(wrapper.read(Type.STRING));
                        wrapper.write(Type.VAR_INT, resourceLocation.equals("block") ? 0 : 1);
                    }
                });
                handler(getSpawnParticleHandler(Type.VAR_INT));
            }
        });

        new RecipeRewriter1_20_2<ClientboundPacket1_20_2>(protocol) {
            @Override
            public void handleCraftingShaped(final PacketWrapper wrapper) throws Exception {
                // Move width and height down
                final int width = wrapper.read(Type.VAR_INT);
                final int height = wrapper.read(Type.VAR_INT);

                wrapper.passthrough(Type.STRING); // Group
                wrapper.passthrough(Type.VAR_INT); // Crafting book category

                wrapper.write(Type.VAR_INT, width);
                wrapper.write(Type.VAR_INT, height);
                final int ingredients = height * width;
                for (int i = 0; i < ingredients; i++) {
                    handleIngredient(wrapper);
                }
                rewrite(wrapper.user(), wrapper.passthrough(itemType())); // Result
                wrapper.passthrough(Type.BOOLEAN); // Show notification
            }
        }.register(ClientboundPackets1_20_2.DECLARE_RECIPES);

        protocol.registerClientbound(ClientboundPackets1_20_2.EXPLOSION, wrapper -> {
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

            wrapper.write(Type.VAR_INT, 1); // Block interaction type - Destroy
            wrapper.write(Types1_20_3.PARTICLE, new Particle(protocol.getMappingData().getParticleMappings().mappedId("explosion"))); // Small explosion particle
            wrapper.write(Types1_20_3.PARTICLE, new Particle(protocol.getMappingData().getParticleMappings().mappedId("explosion_emitter"))); // Large explosion particle
            wrapper.write(Type.STRING, "minecraft:entity.generic.explode"); // Explosion sound
            wrapper.write(Type.OPTIONAL_FLOAT, null); // Sound range
        });
    }

    @Override
    public @Nullable Item handleItemToClient(final UserConnection connection, @Nullable final Item item) {
        if (item == null) {
            return null;
        }

        final CompoundTag tag = item.tag();
        if (tag != null && item.identifier() == 1047) { // Written book
            updatePages(tag, "pages");
            updatePages(tag, "filtered_pages"); // TODO This isn't a list
        }
        return super.handleItemToClient(connection, item);
    }

    private void updatePages(final CompoundTag tag, final String key) {
        final ListTag<StringTag> pages = tag.getListTag(key, StringTag.class);
        if (pages == null) {
            return;
        }

        for (final StringTag pageTag : pages) {
            try {
                final JsonElement updatedComponent = ComponentUtil.convertJson(pageTag.getValue(), SerializerVersion.V1_19_4, SerializerVersion.V1_20_3);
                pageTag.setValue(updatedComponent.toString());
            } catch (final Exception e) {
                Via.getManager().debugHandler().error("Error during book conversion", e);
            }
        }
    }
}
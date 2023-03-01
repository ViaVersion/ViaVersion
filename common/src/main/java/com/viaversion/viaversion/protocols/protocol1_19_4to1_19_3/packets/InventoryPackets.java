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
package com.viaversion.viaversion.protocols.protocol1_19_4to1_19_3.packets;

import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_18to1_17_1.types.Chunk1_18Type;
import com.viaversion.viaversion.protocols.protocol1_19_3to1_19_1.ClientboundPackets1_19_3;
import com.viaversion.viaversion.protocols.protocol1_19_3to1_19_1.rewriter.RecipeRewriter1_19_3;
import com.viaversion.viaversion.protocols.protocol1_19_4to1_19_3.Protocol1_19_4To1_19_3;
import com.viaversion.viaversion.protocols.protocol1_19_4to1_19_3.ServerboundPackets1_19_4;
import com.viaversion.viaversion.rewriter.BlockRewriter;
import com.viaversion.viaversion.rewriter.ItemRewriter;

public final class InventoryPackets extends ItemRewriter<ClientboundPackets1_19_3, ServerboundPackets1_19_4, Protocol1_19_4To1_19_3> {

    public InventoryPackets(final Protocol1_19_4To1_19_3 protocol) {
        super(protocol);
    }

    @Override
    public void registerPackets() {
        final BlockRewriter<ClientboundPackets1_19_3> blockRewriter = new BlockRewriter<>(protocol, Type.POSITION1_14);
        blockRewriter.registerBlockAction(ClientboundPackets1_19_3.BLOCK_ACTION);
        blockRewriter.registerBlockChange(ClientboundPackets1_19_3.BLOCK_CHANGE);
        blockRewriter.registerVarLongMultiBlockChange(ClientboundPackets1_19_3.MULTI_BLOCK_CHANGE);
        blockRewriter.registerEffect(ClientboundPackets1_19_3.EFFECT, 1010, 2001);
        blockRewriter.registerChunkData1_19(ClientboundPackets1_19_3.CHUNK_DATA, Chunk1_18Type::new);
        blockRewriter.registerBlockEntityData(ClientboundPackets1_19_3.BLOCK_ENTITY_DATA);

        protocol.registerClientbound(ClientboundPackets1_19_3.OPEN_WINDOW, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT); // Container id
                map(Type.VAR_INT); // Container type
                map(Type.COMPONENT); // Title
                handler(wrapper -> {
                    final int windowType = wrapper.get(Type.VAR_INT, 1);
                    if (windowType >= 21) { // New smithing menu
                        wrapper.set(Type.VAR_INT, 1, windowType + 1);
                    }
                });
            }
        });

        registerSetCooldown(ClientboundPackets1_19_3.COOLDOWN);
        registerWindowItems1_17_1(ClientboundPackets1_19_3.WINDOW_ITEMS);
        registerSetSlot1_17_1(ClientboundPackets1_19_3.SET_SLOT);
        registerAdvancements(ClientboundPackets1_19_3.ADVANCEMENTS, Type.FLAT_VAR_INT_ITEM);
        registerEntityEquipmentArray(ClientboundPackets1_19_3.ENTITY_EQUIPMENT);
        registerTradeList1_19(ClientboundPackets1_19_3.TRADE_LIST);
        registerWindowPropertyEnchantmentHandler(ClientboundPackets1_19_3.WINDOW_PROPERTY);
        registerSpawnParticle1_19(ClientboundPackets1_19_3.SPAWN_PARTICLE);
        registerCreativeInvAction(ServerboundPackets1_19_4.CREATIVE_INVENTORY_ACTION, Type.FLAT_VAR_INT_ITEM);
        registerClickWindow1_17_1(ServerboundPackets1_19_4.CLICK_WINDOW);

        new RecipeRewriter1_19_3<ClientboundPackets1_19_3>(protocol) {
            @Override
            public void handleCraftingShaped(final PacketWrapper wrapper) throws Exception {
                super.handleCraftingShaped(wrapper);
                wrapper.write(Type.BOOLEAN, true); // Show notification
            }
        }.register(ClientboundPackets1_19_3.DECLARE_RECIPES);
    }
}
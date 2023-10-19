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
package com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2.rewriter;

import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_20_2to1_20.packet.ClientboundPackets1_20_2;
import com.viaversion.viaversion.protocols.protocol1_20_2to1_20.rewriter.RecipeRewriter1_20_2;
import com.viaversion.viaversion.protocols.protocol1_20_2to1_20.type.ChunkType1_20_2;
import com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2.Protocol1_20_3To1_20_2;
import com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2.packet.ServerboundPackets1_20_3;
import com.viaversion.viaversion.rewriter.BlockRewriter;
import com.viaversion.viaversion.rewriter.ItemRewriter;

public final class BlockItemPacketRewriter1_20_3 extends ItemRewriter<ClientboundPackets1_20_2, ServerboundPackets1_20_3, Protocol1_20_3To1_20_2> {

    public BlockItemPacketRewriter1_20_3(final Protocol1_20_3To1_20_2 protocol) {
        super(protocol, Type.ITEM1_20_2, Type.ITEM1_20_2_ARRAY);
    }

    @Override
    public void registerPackets() {
        final BlockRewriter<ClientboundPackets1_20_2> blockRewriter = BlockRewriter.for1_20_2(protocol);
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
        registerSpawnParticle1_19(ClientboundPackets1_20_2.SPAWN_PARTICLE);

        new RecipeRewriter1_20_2<>(protocol).register(ClientboundPackets1_20_2.DECLARE_RECIPES);
    }
}
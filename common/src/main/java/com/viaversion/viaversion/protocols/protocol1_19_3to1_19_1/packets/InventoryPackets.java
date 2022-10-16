/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2022 ViaVersion and contributors
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
package com.viaversion.viaversion.protocols.protocol1_19_3to1_19_1.packets;

import com.viaversion.viaversion.api.protocol.remapper.PacketRemapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_16to1_15_2.data.RecipeRewriter1_16;
import com.viaversion.viaversion.protocols.protocol1_18to1_17_1.types.Chunk1_18Type;
import com.viaversion.viaversion.protocols.protocol1_19_1to1_19.ClientboundPackets1_19_1;
import com.viaversion.viaversion.protocols.protocol1_19_3to1_19_1.Protocol1_19_3To1_19_1;
import com.viaversion.viaversion.protocols.protocol1_19_3to1_19_1.ServerboundPackets1_19_3;
import com.viaversion.viaversion.rewriter.BlockRewriter;
import com.viaversion.viaversion.rewriter.ItemRewriter;

public final class InventoryPackets extends ItemRewriter<Protocol1_19_3To1_19_1> {

    public InventoryPackets(Protocol1_19_3To1_19_1 protocol) {
        super(protocol);
    }

    @Override
    public void registerPackets() {
        final BlockRewriter blockRewriter = new BlockRewriter(protocol, Type.POSITION1_14);
        blockRewriter.registerBlockAction(ClientboundPackets1_19_1.BLOCK_ACTION);
        blockRewriter.registerBlockChange(ClientboundPackets1_19_1.BLOCK_CHANGE);
        blockRewriter.registerVarLongMultiBlockChange(ClientboundPackets1_19_1.MULTI_BLOCK_CHANGE);
        blockRewriter.registerEffect(ClientboundPackets1_19_1.EFFECT, 1010, 2001);
        blockRewriter.registerChunkData1_19(ClientboundPackets1_19_1.CHUNK_DATA, Chunk1_18Type::new);
        blockRewriter.registerBlockEntityData(ClientboundPackets1_19_1.BLOCK_ENTITY_DATA);

        registerSetCooldown(ClientboundPackets1_19_1.COOLDOWN);
        registerWindowItems1_17_1(ClientboundPackets1_19_1.WINDOW_ITEMS);
        registerSetSlot1_17_1(ClientboundPackets1_19_1.SET_SLOT);
        registerAdvancements(ClientboundPackets1_19_1.ADVANCEMENTS, Type.FLAT_VAR_INT_ITEM);
        registerEntityEquipmentArray(ClientboundPackets1_19_1.ENTITY_EQUIPMENT);
        registerClickWindow1_17_1(ServerboundPackets1_19_3.CLICK_WINDOW);
        registerTradeList1_19(ClientboundPackets1_19_1.TRADE_LIST);
        registerCreativeInvAction(ServerboundPackets1_19_3.CREATIVE_INVENTORY_ACTION, Type.FLAT_VAR_INT_ITEM);
        registerWindowPropertyEnchantmentHandler(ClientboundPackets1_19_1.WINDOW_PROPERTY);
        registerSpawnParticle1_19(ClientboundPackets1_19_1.SPAWN_PARTICLE);

        new RecipeRewriter1_16(protocol).registerDefaultHandler(ClientboundPackets1_19_1.DECLARE_RECIPES);

        protocol.registerClientbound(ClientboundPackets1_19_1.EXPLOSION, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.FLOAT, Type.DOUBLE); // X
                map(Type.FLOAT, Type.DOUBLE); // Y
                map(Type.FLOAT, Type.DOUBLE); // Z
            }
        });
    }
}

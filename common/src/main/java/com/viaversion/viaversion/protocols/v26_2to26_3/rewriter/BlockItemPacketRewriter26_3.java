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
package com.viaversion.viaversion.protocols.v26_2to26_3.rewriter;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.Holder;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataContainer;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.item.StructuredItemTemplate;
import com.viaversion.viaversion.api.minecraft.item.data.ArmorTrimMaterial1_20_5;
import com.viaversion.viaversion.api.minecraft.item.data.ArmorTrimMaterial26_3;
import com.viaversion.viaversion.api.minecraft.item.data.PotDecorations1_20_5;
import com.viaversion.viaversion.api.minecraft.item.data.PotDecorations26_3;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.VersionedTypes;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ClientboundPacket26_1;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ClientboundPackets26_1;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ServerboundPacket26_1;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ServerboundPackets26_1;
import com.viaversion.viaversion.protocols.v26_2to26_3.Protocol26_2To26_3;
import com.viaversion.viaversion.rewriter.StructuredItemRewriter;
import java.util.BitSet;
import java.util.HashMap;

public final class BlockItemPacketRewriter26_3 extends StructuredItemRewriter<ClientboundPacket26_1, ServerboundPacket26_1, Protocol26_2To26_3> {

    public BlockItemPacketRewriter26_3(final Protocol26_2To26_3 protocol) {
        super(protocol);
    }

    @Override
    protected void registerPackets() {
        protocol.appendClientbound(ClientboundPackets26_1.EXPLODE, wrapper -> {
            wrapper.write(Types.BOOLEAN, true); // Play sound
        });

        protocol.registerClientbound(ClientboundPackets26_1.OPEN_SIGN_EDITOR, wrapper -> {
            wrapper.passthrough(Types.BLOCK_POSITION1_14);
            final int signTextSlot = wrapper.read(Types.BOOLEAN) ? 1 : 0;
            wrapper.write(Types.VAR_INT, signTextSlot);
        });

        protocol.registerServerbound(ServerboundPackets26_1.SIGN_UPDATE, wrapper -> {
            wrapper.passthrough(Types.BLOCK_POSITION1_14);

            wrapper.write(Types.BOOLEAN, true); // Front text - replace below if needed

            for (int i = 0; i < 4; i++) {
                wrapper.passthrough(Types.STRING); // Line
            }

            final int signTextSlot = wrapper.read(Types.VAR_INT);
            if (signTextSlot == 0) {
                wrapper.set(Types.BOOLEAN, 0, false);
            }
        });

        protocol.registerClientbound(ClientboundPackets26_1.LIGHT_UPDATE, wrapper -> {
            wrapper.passthrough(Types.VAR_INT); // X
            wrapper.passthrough(Types.VAR_INT); // Y
            handleLightMasks(wrapper);
        });
        protocol.appendClientbound(ClientboundPackets26_1.LEVEL_CHUNK_WITH_LIGHT, this::handleLightMasks);
    }

    private void handleLightMasks(final PacketWrapper wrapper) {
        for (int i = 0; i < 4; i++) {
            final long[] mask = wrapper.read(Types.LONG_ARRAY_PRIMITIVE);
            wrapper.write(Types.BIT_SET, BitSet.valueOf(mask));
        }
    }

    @Override
    protected void backupInconvertibleData(final UserConnection connection, final Item item, final StructuredDataContainer dataContainer, final CompoundTag backupTag) {
        super.backupInconvertibleData(connection, item, dataContainer, backupTag);
    }

    @Override
    protected void handleItemDataComponentsToClient(final UserConnection connection, final Item item, final StructuredDataContainer container) {
        upgradeData(container);
        super.handleItemDataComponentsToClient(connection, item, container);
    }

    @Override
    protected void handleItemDataComponentsToServer(final UserConnection connection, final Item item, final StructuredDataContainer container) {
        downgradeData(container);
        super.handleItemDataComponentsToServer(connection, item, container);
    }

    public static void upgradeData(final StructuredDataContainer container) {
        container.replaceKey(StructuredDataKey.INSTRUMENT26_1, StructuredDataKey.INSTRUMENT26_3);
        container.replace(StructuredDataKey.PROVIDES_TRIM_MATERIAL26_1, StructuredDataKey.PROVIDES_TRIM_MATERIAL26_3, holder -> {
            if (holder.hasId()) {
                return Holder.of(holder.id());
            }

            final ArmorTrimMaterial1_20_5 trim = holder.value();
            return Holder.of(new ArmorTrimMaterial26_3(trim.assetName(), trim.description()));
        });
        container.replace(StructuredDataKey.POT_DECORATIONS1_20_5, VersionedTypes.V26_3.structuredDataKeys().potDecorations, decorations -> {
            return new PotDecorations26_3(
                new StructuredItemTemplate(decorations.backItem(), 1),
                new StructuredItemTemplate(decorations.leftItem(), 1),
                new StructuredItemTemplate(decorations.rightItem(), 1),
                new StructuredItemTemplate(decorations.frontItem(), 1)
            );
        });
    }

    public static void downgradeData(final StructuredDataContainer container) {
        container.remove(StructuredDataKey.PROVIDES_POTTERY_PATTERN);
        container.remove(StructuredDataKey.BLOCK_TRANSFORMER);
        container.remove(StructuredDataKey.VILLAGER_FOOD);
        container.remove(StructuredDataKey.COOKING_FUEL);
        container.remove(StructuredDataKey.BREWING_FUEL);
        container.remove(StructuredDataKey.MOB_VISIBILITY);
        container.remove(StructuredDataKey.SIGN_TEXT_FRONT);
        container.remove(StructuredDataKey.SIGN_TEXT_BACK);
        container.remove(StructuredDataKey.WAXED);
        container.remove(StructuredDataKey.CUSHION_COLOR);

        container.replaceKey(StructuredDataKey.INSTRUMENT26_3, StructuredDataKey.INSTRUMENT26_1);
        container.replace(StructuredDataKey.PROVIDES_TRIM_MATERIAL26_3, StructuredDataKey.PROVIDES_TRIM_MATERIAL26_1, holder -> {
            if (holder.hasId()) {
                return Holder.of(holder.id());
            }

            final ArmorTrimMaterial26_3 trim = holder.value();
            return Holder.of(new ArmorTrimMaterial1_20_5(trim.paletteId(), new HashMap<>(), trim.description()));
        });
        container.replace(VersionedTypes.V26_3.structuredDataKeys().potDecorations, StructuredDataKey.POT_DECORATIONS1_20_5, decorations -> {
            return new PotDecorations1_20_5(new int[]{decorations.back().identifier(), decorations.left().identifier(), decorations.right().identifier(), decorations.front().identifier()});
        });
    }

    @Override
    protected void restoreBackupData(final Item item, final StructuredDataContainer container, final CompoundTag customData) {
        super.restoreBackupData(item, container, customData);
    }
}

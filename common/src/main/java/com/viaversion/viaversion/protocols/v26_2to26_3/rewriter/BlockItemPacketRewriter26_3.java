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

import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.FullMappings;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataContainer;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.item.StructuredItemTemplate;
import com.viaversion.viaversion.api.minecraft.item.data.PotDecorations26_3;
import com.viaversion.viaversion.api.minecraft.item.data.PotDecorations;
import com.viaversion.viaversion.api.rewriter.ComponentRewriter;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ClientboundPacket26_1;
import com.viaversion.viaversion.protocols.v26_2to26_3.Protocol26_2To26_3;
import com.viaversion.viaversion.protocols.v26_2to26_3.packet.ServerboundPacket26_3;
import com.viaversion.viaversion.rewriter.StructuredItemRewriter;
import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class BlockItemPacketRewriter26_3 extends StructuredItemRewriter<ClientboundPacket26_1, ServerboundPacket26_3, Protocol26_2To26_3> {

    private static final String BRICK = "minecraft:brick";

    public BlockItemPacketRewriter26_3(final Protocol26_2To26_3 protocol) {
        super(protocol);
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

    private void upgradeData(final StructuredDataContainer container) {
        container.replaceKey(StructuredDataKey.SWING_ANIMATION, StructuredDataKey.ATTACK_ANIMATION);
        container.remove(StructuredDataKey.MAP_COLOR);

        container.replace(StructuredDataKey.POT_DECORATIONS, protocol.mappedTypes().structuredDataKeys().potDecorations, decorations -> {
            final int emptySherd = emptySherdId(true);
            return new PotDecorations26_3(sherd(decorations.backItem(), emptySherd), sherd(decorations.leftItem(), emptySherd),
                sherd(decorations.rightItem(), emptySherd), sherd(decorations.frontItem(), emptySherd));
        });
    }

    private void downgradeData(final StructuredDataContainer container) {
        container.replace(protocol.mappedTypes().structuredDataKeys().potDecorations, StructuredDataKey.POT_DECORATIONS, decorations -> {
            final int emptySherd = emptySherdId(false);
            return new PotDecorations(new int[]{
                sherdId(decorations.back(), emptySherd),
                sherdId(decorations.left(), emptySherd),
                sherdId(decorations.right(), emptySherd),
                sherdId(decorations.front(), emptySherd)
            });
        });

        container.replaceKey(StructuredDataKey.ATTACK_ANIMATION, StructuredDataKey.SWING_ANIMATION);
        container.remove(List.of(StructuredDataKey.INTERACT_ANIMATION, StructuredDataKey.BLOCK_TRANSFORMER, StructuredDataKey.VILLAGER_FOOD,
            StructuredDataKey.COMPOSTABLE, StructuredDataKey.COOKING_FUEL, StructuredDataKey.BREWING_FUEL, StructuredDataKey.MOB_VISIBILITY,
            StructuredDataKey.PROVIDES_POTTERY_PATTERN, StructuredDataKey.SIGN_TEXT_FRONT, StructuredDataKey.SIGN_TEXT_BACK,
            StructuredDataKey.WAXED, StructuredDataKey.CUSHION_COLOR));
    }

    private static @Nullable Item sherd(final int id, final int emptySherd) {
        return id == -1 || id == emptySherd ? null : new StructuredItemTemplate(id, 1, new StructuredDataContainer());
    }

    private static int sherdId(@Nullable final Item sherd, final int emptySherd) {
        return sherd != null ? sherd.identifier() : emptySherd;
    }

    /**
     * Empty slots used to be filled with bricks rather than being nullable.
     */
    private int emptySherdId(final boolean mapped) {
        final FullMappings itemMappings = protocol.getMappingData().getFullItemMappings();
        if (itemMappings == null) {
            return -1;
        }
        return mapped ? itemMappings.mappedId(BRICK) : itemMappings.id(BRICK);
    }

    public void registerAdvancements26_3(final ClientboundPacket26_1 packetType) {
        protocol.replaceClientbound(packetType, wrapper -> {
            wrapper.passthrough(Types.BOOLEAN); // Reset/clear
            final int size = wrapper.passthrough(Types.VAR_INT);
            for (int i = 0; i < size; i++) {
                wrapper.passthrough(Types.STRING); // Identifier
                wrapper.passthrough(Types.OPTIONAL_STRING); // Parent

                float x = 0;
                float y = 0;
                if (wrapper.passthrough(Types.BOOLEAN)) { // Display data
                    final Tag title = wrapper.passthrough(Types.TRUSTED_TAG);
                    final Tag description = wrapper.passthrough(Types.TRUSTED_TAG);
                    final ComponentRewriter componentRewriter = protocol.getComponentRewriter();
                    if (componentRewriter != null) {
                        componentRewriter.processTag(wrapper.user(), title);
                        componentRewriter.processTag(wrapper.user(), description);
                    }

                    passthroughClientboundItemTemplate(wrapper); // Icon
                    wrapper.passthrough(Types.VAR_INT); // Frame type
                    final int flags = wrapper.passthrough(Types.INT);
                    if ((flags & 1) != 0) {
                        wrapper.passthrough(Types.STRING); // Background texture
                    }
                    x = wrapper.read(Types.FLOAT);
                    y = wrapper.read(Types.FLOAT);
                }

                final int requirements = wrapper.passthrough(Types.VAR_INT);
                for (int array = 0; array < requirements; array++) {
                    wrapper.passthrough(Types.STRING_ARRAY);
                }

                wrapper.passthrough(Types.BOOLEAN); // Send telemetry

                // The tree layout is sent by the server now instead of being computed by the client
                wrapper.write(Types.FLOAT, x);
                wrapper.write(Types.FLOAT, y);
            }
        });
    }
}

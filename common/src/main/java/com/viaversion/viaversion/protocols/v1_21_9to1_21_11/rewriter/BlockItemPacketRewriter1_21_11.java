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
package com.viaversion.viaversion.protocols.v1_21_9to1_21_11.rewriter;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataContainer;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.item.data.AttackRange;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_21_5;
import com.viaversion.viaversion.protocols.v1_21_4to1_21_5.rewriter.RecipeDisplayRewriter1_21_5;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ServerboundPackets1_21_6;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.packet.ClientboundPacket1_21_9;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.packet.ClientboundPackets1_21_9;
import com.viaversion.viaversion.protocols.v1_21_7to1_21_9.packet.ServerboundPacket1_21_9;
import com.viaversion.viaversion.protocols.v1_21_9to1_21_11.Protocol1_21_9To1_21_11;
import com.viaversion.viaversion.protocols.v1_21_9to1_21_11.storage.GameTimeStorage;
import com.viaversion.viaversion.rewriter.BlockRewriter;
import com.viaversion.viaversion.rewriter.RecipeDisplayRewriter;
import com.viaversion.viaversion.rewriter.StructuredItemRewriter;
import com.viaversion.viaversion.rewriter.block.BlockRewriter1_21_5;

public final class BlockItemPacketRewriter1_21_11 extends StructuredItemRewriter<ClientboundPacket1_21_9, ServerboundPacket1_21_9, Protocol1_21_9To1_21_11> {

    public BlockItemPacketRewriter1_21_11(final Protocol1_21_9To1_21_11 protocol) {
        super(protocol);
    }

    @Override
    public void registerPackets() {
        final BlockRewriter<ClientboundPacket1_21_9> blockRewriter = new BlockRewriter1_21_5<>(protocol);
        blockRewriter.registerBlockEvent(ClientboundPackets1_21_9.BLOCK_EVENT);
        blockRewriter.registerBlockUpdate(ClientboundPackets1_21_9.BLOCK_UPDATE);
        blockRewriter.registerSectionBlocksUpdate1_20(ClientboundPackets1_21_9.SECTION_BLOCKS_UPDATE);
        blockRewriter.registerLevelEvent1_21(ClientboundPackets1_21_9.LEVEL_EVENT, 2001);
        blockRewriter.registerLevelChunk1_19(ClientboundPackets1_21_9.LEVEL_CHUNK_WITH_LIGHT, ChunkType1_21_5::new);
        blockRewriter.registerBlockEntityData(ClientboundPackets1_21_9.BLOCK_ENTITY_DATA);

        registerSetCursorItem(ClientboundPackets1_21_9.SET_CURSOR_ITEM);
        registerSetPlayerInventory(ClientboundPackets1_21_9.SET_PLAYER_INVENTORY);
        registerCooldown1_21_2(ClientboundPackets1_21_9.COOLDOWN);
        registerSetContent1_21_2(ClientboundPackets1_21_9.CONTAINER_SET_CONTENT);
        registerSetSlot1_21_2(ClientboundPackets1_21_9.CONTAINER_SET_SLOT);
        registerAdvancements1_20_3(ClientboundPackets1_21_9.UPDATE_ADVANCEMENTS);
        registerSetEquipment(ClientboundPackets1_21_9.SET_EQUIPMENT);
        registerMerchantOffers1_20_5(ClientboundPackets1_21_9.MERCHANT_OFFERS);
        registerContainerClick1_21_5(ServerboundPackets1_21_6.CONTAINER_CLICK);
        registerSetCreativeModeSlot1_21_5(ServerboundPackets1_21_6.SET_CREATIVE_MODE_SLOT);

        final RecipeDisplayRewriter<ClientboundPacket1_21_9> recipeRewriter = new RecipeDisplayRewriter1_21_5<>(protocol);
        recipeRewriter.registerUpdateRecipes(ClientboundPackets1_21_9.UPDATE_RECIPES);
        recipeRewriter.registerRecipeBookAdd(ClientboundPackets1_21_9.RECIPE_BOOK_ADD);
        recipeRewriter.registerPlaceGhostRecipe(ClientboundPackets1_21_9.PLACE_GHOST_RECIPE);

        protocol.registerClientbound(ClientboundPackets1_21_9.SET_BORDER_LERP_SIZE, wrapper -> {
            wrapper.passthrough(Types.DOUBLE); // oldSize
            wrapper.passthrough(Types.DOUBLE); // newSize
            wrapper.write(Types.VAR_LONG, wrapper.read(Types.VAR_LONG) / 50); // lerpTime
        });
        protocol.registerClientbound(ClientboundPackets1_21_9.INITIALIZE_BORDER, wrapper -> {
            wrapper.passthrough(Types.DOUBLE); // newCenterX
            wrapper.passthrough(Types.DOUBLE); // newCenterZ
            wrapper.passthrough(Types.DOUBLE); // oldSize
            wrapper.passthrough(Types.DOUBLE); // newSize
            wrapper.write(Types.VAR_LONG, wrapper.read(Types.VAR_LONG) / 50); // lerpTime
        });
        protocol.registerClientbound(ClientboundPackets1_21_9.SET_TIME, wrapper -> {
            final long gameTime = wrapper.passthrough(Types.LONG);
            wrapper.user().get(GameTimeStorage.class).setGameTime(gameTime);
        });
        protocol.registerServerbound(ServerboundPackets1_21_6.CLIENT_TICK_END, wrapper -> {
            wrapper.user().get(GameTimeStorage.class).incrementGameTime();
        });
    }

    @Override
    protected void handleItemDataComponentsToClient(final UserConnection connection, final Item item, final StructuredDataContainer container) {
        upgradeData(item, container);

        // Add data components to fix issues in older protocols
        appendItemDataFixComponents(connection, item);

        super.handleItemDataComponentsToClient(connection, item, container);
    }

    @Override
    protected void handleItemDataComponentsToServer(final UserConnection connection, final Item item, final StructuredDataContainer container) {
        downgradeData(item, container);
        super.handleItemDataComponentsToServer(connection, item, container);
    }

    private void appendItemDataFixComponents(final UserConnection connection, final Item item) {
        final ProtocolVersion serverVersion = connection.getProtocolInfo().serverProtocolVersion();
        if (Via.getConfig().use1_8HitboxMargin() && serverVersion.olderThanOrEqualTo(ProtocolVersion.v1_8)) {
            // Set 0.1 hitbox margin like in 1.8. Creative range is 4F instead of default 5F as measured empirically.
            item.dataContainer().set(StructuredDataKey.ATTACK_RANGE, new AttackRange(0F, 3F, 0F, 4F, 0.1F, 1F));
        }
    }

    public static void upgradeData(final Item item, final StructuredDataContainer container) {
    }

    public static void downgradeData(final Item item, final StructuredDataContainer container) {
        container.remove(StructuredDataKey.SWING_ANIMATION);
        container.remove(StructuredDataKey.KINETIC_WEAPON);
        container.remove(StructuredDataKey.PIERCING_WEAPON);
        container.remove(StructuredDataKey.DAMAGE_TYPE);
        container.remove(StructuredDataKey.MINIMUM_ATTACK_CHARGE);
        container.remove(StructuredDataKey.USE_EFFECTS);
        container.remove(StructuredDataKey.ZOMBIE_NAUTILUS_VARIANT);
        container.remove(StructuredDataKey.ATTACK_RANGE);
    }
}

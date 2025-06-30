/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2025 ViaVersion and contributors
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
package com.viaversion.viaversion.protocols.v1_20_5to1_21.rewriter;

import com.viaversion.nbt.tag.ByteTag;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataContainer;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.item.data.AttributeModifiers1_20_5;
import com.viaversion.viaversion.api.minecraft.item.data.AttributeModifiers1_21;
import com.viaversion.viaversion.api.minecraft.item.data.Enchantments;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_20_2;
import com.viaversion.viaversion.protocols.v1_20_2to1_20_3.rewriter.RecipeRewriter1_20_3;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ClientboundPacket1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ClientboundPackets1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ServerboundPacket1_20_5;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.packet.ServerboundPackets1_20_5;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.Protocol1_20_5To1_21;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.data.AttributeModifierMappings1_21;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.storage.EfficiencyAttributeStorage;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.storage.PlayerPositionStorage;
import com.viaversion.viaversion.rewriter.BlockRewriter;
import com.viaversion.viaversion.rewriter.StructuredItemRewriter;
import java.util.Arrays;
import java.util.List;

public final class BlockItemPacketRewriter1_21 extends StructuredItemRewriter<ClientboundPacket1_20_5, ServerboundPacket1_20_5, Protocol1_20_5To1_21> {

    private static final List<String> DISCS = List.of("11", "13", "5", "blocks", "cat", "chirp", "far", "mall", "mellohi", "otherside", "pigstep", "relic", "stal", "strad", "wait", "ward");
    private static final int HELMET_SLOT = 5;
    private static final int CHESTPLATE_SLOT = 6;
    private static final int LEGGINGS_SLOT = 7;
    private static final int BOOTS_SLOT = 8;

    private static final int AQUA_AFFINITY_ID = 6;
    private static final int DEPTH_STRIDER_ID = 8;
    private static final int SWIFT_SNEAK_ID = 12;

    public BlockItemPacketRewriter1_21(final Protocol1_20_5To1_21 protocol) {
        super(protocol);
    }

    @Override
    public void registerPackets() {
        final BlockRewriter<ClientboundPacket1_20_5> blockRewriter = BlockRewriter.for1_20_2(protocol);
        blockRewriter.registerBlockEvent(ClientboundPackets1_20_5.BLOCK_EVENT);
        blockRewriter.registerBlockUpdate(ClientboundPackets1_20_5.BLOCK_UPDATE);
        blockRewriter.registerSectionBlocksUpdate1_20(ClientboundPackets1_20_5.SECTION_BLOCKS_UPDATE);
        blockRewriter.registerLevelChunk1_19(ClientboundPackets1_20_5.LEVEL_CHUNK_WITH_LIGHT, ChunkType1_20_2::new);
        blockRewriter.registerBlockEntityData(ClientboundPackets1_20_5.BLOCK_ENTITY_DATA);

        registerCooldown(ClientboundPackets1_20_5.COOLDOWN);
        registerSetContent1_17_1(ClientboundPackets1_20_5.CONTAINER_SET_CONTENT);
        protocol.registerClientbound(ClientboundPackets1_20_5.CONTAINER_SET_SLOT, wrapper -> {
            final byte containerId = wrapper.passthrough(Types.BYTE);
            wrapper.passthrough(Types.VAR_INT); // State id
            final short slotId = wrapper.passthrough(Types.SHORT);
            final Item item = handleItemToClient(wrapper.user(), wrapper.read(itemType()));
            wrapper.write(mappedItemType(), item);

            // When a players' armor is set, update their attributes
            if (containerId != 0 || slotId > BOOTS_SLOT || slotId < HELMET_SLOT || slotId == CHESTPLATE_SLOT) {
                return;
            }

            final EfficiencyAttributeStorage storage = wrapper.user().get(EfficiencyAttributeStorage.class);
            Enchantments enchants = item.dataContainer().get(StructuredDataKey.ENCHANTMENTS1_20_5);
            EfficiencyAttributeStorage.ActiveEnchants active = storage.activeEnchants();
            active = switch (slotId) {
                case HELMET_SLOT -> active.aquaAffinity(enchants == null ? 0 : enchants.getLevel(AQUA_AFFINITY_ID));
                case LEGGINGS_SLOT -> active.swiftSneak(enchants == null ? 0 : enchants.getLevel(SWIFT_SNEAK_ID));
                case BOOTS_SLOT -> active.depthStrider(enchants == null ? 0 : enchants.getLevel(DEPTH_STRIDER_ID));
                default -> active;
            };
            storage.setEnchants(-1, wrapper.user(), active);
        });
        registerAdvancements1_20_3(ClientboundPackets1_20_5.UPDATE_ADVANCEMENTS);
        registerSetEquipment(ClientboundPackets1_20_5.SET_EQUIPMENT);
        registerContainerClick1_17_1(ServerboundPackets1_20_5.CONTAINER_CLICK);
        registerMerchantOffers1_20_5(ClientboundPackets1_20_5.MERCHANT_OFFERS);
        registerSetCreativeModeSlot(ServerboundPackets1_20_5.SET_CREATIVE_MODE_SLOT);

        protocol.registerClientbound(ClientboundPackets1_20_5.HORSE_SCREEN_OPEN, wrapper -> {
            wrapper.passthrough(Types.UNSIGNED_BYTE); // Container id

            // Now written as columns
            final int size = wrapper.read(Types.VAR_INT);
            wrapper.write(Types.VAR_INT, Math.max(0, (size - 1) / 3));
        });

        protocol.registerClientbound(ClientboundPackets1_20_5.LEVEL_EVENT, wrapper -> {
            final int id = wrapper.passthrough(Types.INT);
            wrapper.passthrough(Types.BLOCK_POSITION1_14);

            final int data = wrapper.read(Types.INT);
            if (id == 1010) {
                final int jukeboxSong = itemToJubeboxSong(data);
                if (jukeboxSong == -1) {
                    wrapper.cancel();
                    return;
                }

                wrapper.write(Types.INT, jukeboxSong);
            } else if (id == 2001) {
                wrapper.write(Types.INT, protocol.getMappingData().getNewBlockStateId(data));
            } else {
                wrapper.write(Types.INT, data);
            }
        });

        protocol.registerServerbound(ServerboundPackets1_20_5.USE_ITEM, wrapper -> {
            wrapper.passthrough(Types.VAR_INT); // Hand
            wrapper.passthrough(Types.VAR_INT); // Sequence
            final float yaw = wrapper.read(Types.FLOAT); // Y rotation
            final float pitch = wrapper.read(Types.FLOAT); // X rotation

            if (!Via.getConfig().fix1_21PlacementRotation()) {
                return;
            }
            final PlayerPositionStorage storage = wrapper.user().get(PlayerPositionStorage.class);

            // Not correct but *enough* for vanilla/normal servers to have block placement synchronized
            final PacketWrapper playerRotation = wrapper.create(ServerboundPackets1_20_5.MOVE_PLAYER_POS_ROT);
            playerRotation.write(Types.DOUBLE, storage.x());
            playerRotation.write(Types.DOUBLE, storage.y());
            playerRotation.write(Types.DOUBLE, storage.z());
            playerRotation.write(Types.FLOAT, yaw);
            playerRotation.write(Types.FLOAT, pitch);
            playerRotation.write(Types.BOOLEAN, storage.onGround());

            playerRotation.sendToServer(Protocol1_20_5To1_21.class);
            wrapper.sendToServer(Protocol1_20_5To1_21.class);
            wrapper.cancel();
        });

        new RecipeRewriter1_20_3<>(protocol).register1_20_5(ClientboundPackets1_20_5.UPDATE_RECIPES);
    }

    @Override
    public Item handleItemToClient(final UserConnection connection, final Item item) {
        if (item.isEmpty()) {
            return item;
        }

        super.handleItemToClient(connection, item);
        updateItemData(item);

        final StructuredDataContainer data = item.dataContainer();
        if (data.has(StructuredDataKey.RARITY)) {
            return item;
        }
        // Change rarity of trident and piglin banner pattern
        if (item.identifier() == 1188 || item.identifier() == 1200) {
            data.set(StructuredDataKey.RARITY, 0); // Common
            saveTag(createCustomTag(item), new ByteTag(true), "rarity");
        }
        return item;
    }

    public static void updateItemData(final Item item) {
        final StructuredDataContainer dataContainer = item.dataContainer();
        dataContainer.replaceKey(StructuredDataKey.FOOD1_20_5, StructuredDataKey.FOOD1_21);
        dataContainer.replace(StructuredDataKey.ATTRIBUTE_MODIFIERS1_20_5, StructuredDataKey.ATTRIBUTE_MODIFIERS1_21, attributeModifiers -> {
            final AttributeModifiers1_21.AttributeModifier[] modifiers = Arrays.stream(attributeModifiers.modifiers()).map(modifier -> {
                final AttributeModifiers1_20_5.ModifierData modData = modifier.modifier();
                final AttributeModifiers1_21.ModifierData updatedModData = new AttributeModifiers1_21.ModifierData(Protocol1_20_5To1_21.mapAttributeUUID(modData.uuid(), modData.name()), modData.amount(), modData.operation());
                return new AttributeModifiers1_21.AttributeModifier(modifier.attribute(), updatedModData, modifier.slotType());
            }).toArray(AttributeModifiers1_21.AttributeModifier[]::new);
            return new AttributeModifiers1_21(modifiers, attributeModifiers.showInTooltip());
        });
    }

    @Override
    public Item handleItemToServer(final UserConnection connection, final Item item) {
        if (item.isEmpty()) {
            return item;
        }

        super.handleItemToServer(connection, item);
        downgradeItemData(item);

        final StructuredDataContainer data = item.dataContainer();
        final CompoundTag customData = data.get(StructuredDataKey.CUSTOM_DATA);
        if (customData == null) {
            return item;
        }
        if (customData.remove(nbtTagName("rarity")) != null) {
            data.remove(StructuredDataKey.RARITY);
            removeCustomTag(data, customData);
        }
        return item;
    }

    public static void downgradeItemData(final Item item) {
        final StructuredDataContainer dataContainer = item.dataContainer();
        dataContainer.replaceKey(StructuredDataKey.FOOD1_21, StructuredDataKey.FOOD1_20_5);
        dataContainer.remove(StructuredDataKey.JUKEBOX_PLAYABLE1_21);
        dataContainer.replace(StructuredDataKey.ATTRIBUTE_MODIFIERS1_21, StructuredDataKey.ATTRIBUTE_MODIFIERS1_20_5, attributeModifiers -> {
            final AttributeModifiers1_20_5.AttributeModifier[] modifiers = Arrays.stream(attributeModifiers.modifiers()).map(modifier -> {
                final AttributeModifiers1_21.ModifierData modData = modifier.modifier();
                final String name = AttributeModifierMappings1_21.idToName(modData.id());
                final AttributeModifiers1_20_5.ModifierData updatedModData = new AttributeModifiers1_20_5.ModifierData(
                    Protocol1_20_5To1_21.mapAttributeId(modData.id()),
                    name != null ? name : modData.id(),
                    modData.amount(),
                    modData.operation()
                );
                return new AttributeModifiers1_20_5.AttributeModifier(modifier.attribute(), updatedModData, modifier.slotType());
            }).toArray(AttributeModifiers1_20_5.AttributeModifier[]::new);
            return new AttributeModifiers1_20_5(modifiers, attributeModifiers.showInTooltip());
        });
    }

    private int itemToJubeboxSong(final int id) {
        String identifier = Protocol1_20_5To1_21.MAPPINGS.getFullItemMappings().identifier(id);
        if (!identifier.contains("music_disc_")) {
            return -1;
        }

        identifier = identifier.substring("minecraft:music_disc_".length());
        return DISCS.indexOf(identifier);
    }
}

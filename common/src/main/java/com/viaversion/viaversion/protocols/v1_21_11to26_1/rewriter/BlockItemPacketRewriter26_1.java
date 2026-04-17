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
package com.viaversion.viaversion.protocols.v1_21_11to26_1.rewriter;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.EitherHolder;
import com.viaversion.viaversion.api.minecraft.Holder;
import com.viaversion.viaversion.api.minecraft.HolderSet;
import com.viaversion.viaversion.api.minecraft.chunks.Chunk;
import com.viaversion.viaversion.api.minecraft.chunks.ChunkSection;
import com.viaversion.viaversion.api.minecraft.chunks.DataPalette;
import com.viaversion.viaversion.api.minecraft.chunks.PaletteType;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataContainer;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.api.minecraft.data.version.StructuredDataKeys1_21_11;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.item.StructuredItem;
import com.viaversion.viaversion.api.minecraft.item.data.BlocksAttacks;
import com.viaversion.viaversion.api.minecraft.item.data.DamageResistant1_21_2;
import com.viaversion.viaversion.api.minecraft.item.data.DamageResistant26_1;
import com.viaversion.viaversion.api.minecraft.item.data.DamageType;
import com.viaversion.viaversion.api.minecraft.item.data.JukeboxPlayable;
import com.viaversion.viaversion.api.minecraft.item.data.ProvidesBannerPatterns;
import com.viaversion.viaversion.api.minecraft.item.data.ProvidesTrimMaterial;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.Protocol1_21_11To26_1;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ServerboundPacket26_1;
import com.viaversion.viaversion.protocols.v1_21_9to1_21_11.packet.ClientboundPacket1_21_11;
import com.viaversion.viaversion.protocols.v1_21_9to1_21_11.packet.ClientboundPackets1_21_11;
import com.viaversion.viaversion.rewriter.StructuredItemRewriter;
import com.viaversion.viaversion.util.Either;
import com.viaversion.viaversion.util.Key;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class BlockItemPacketRewriter26_1 extends StructuredItemRewriter<ClientboundPacket1_21_11, ServerboundPacket26_1, Protocol1_21_11To26_1> {

    public BlockItemPacketRewriter26_1(final Protocol1_21_11To26_1 protocol) {
        super(protocol);
    }

    @Override
    public void registerPackets() {
        protocol.replaceClientbound(ClientboundPackets1_21_11.LEVEL_CHUNK_WITH_LIGHT, wrapper -> {
            final Chunk chunk = protocol.getBlockRewriter().handleChunk1_18(wrapper);
            for (final ChunkSection section : chunk.getSections()) {
                if (section.getNonAirBlocksCount() == 0) {
                    continue;
                }

                final DataPalette blockPalette = section.palette(PaletteType.BLOCKS);
                for (int idx = 0; idx < ChunkSection.SIZE; idx++) {
                    final int id = blockPalette.idAt(idx);
                    if (Protocol1_21_11To26_1.MAPPINGS.fluidBlockStates().contains(id)) {
                        // Needed for certain client-side fluid interactions
                        section.setFluidCount(section.getFluidCount() + 1);
                    }
                }
            }
            protocol.getBlockRewriter().handleBlockEntities(chunk, wrapper.user());
        });
    }

    @Override
    protected void handleItemDataComponentsToClient(final UserConnection connection, final Item item, final StructuredDataContainer container) {
        upgradeData(protocol, protocol.types().structuredDataKeys(), container);
        super.handleItemDataComponentsToClient(connection, item, container);
    }

    public static void upgradeData(final Protocol<?, ?, ?, ?> protocol, final StructuredDataKeys1_21_11 dataKeys, final StructuredDataContainer container) {
        container.replace(StructuredDataKey.JUKEBOX_PLAYABLE1_21_5, StructuredDataKey.JUKEBOX_PLAYABLE26_1, jukeboxPlayable -> upgradeHolder(protocol, jukeboxPlayable.song(), "jukebox_song"));
        container.replace(StructuredDataKey.INSTRUMENT1_21_5, StructuredDataKey.INSTRUMENT26_1, instrument -> upgradeHolder(protocol, instrument, "instrument"));
        container.replace(StructuredDataKey.PROVIDES_TRIM_MATERIAL1_21_5, StructuredDataKey.PROVIDES_TRIM_MATERIAL26_1, providesTrimMaterial -> upgradeHolder(protocol, providesTrimMaterial.material(), "trim_material"));
        container.replace(StructuredDataKey.CHICKEN_VARIANT1_21_5, StructuredDataKey.CHICKEN_VARIANT26_1, chickenVariant -> upgradeEitherVariant(protocol, chickenVariant, "chicken_variant"));
        container.replace(StructuredDataKey.ZOMBIE_NAUTILUS_VARIANT1_21_11, StructuredDataKey.ZOMBIE_NAUTILUS_VARIANT26_1, nautilusVariant -> upgradeEitherVariant(protocol, nautilusVariant, "zombie_nautilus_variant"));
        container.replace(StructuredDataKey.DAMAGE_TYPE1_21_11, StructuredDataKey.DAMAGE_TYPE26_1, damageType -> upgradeEitherVariant(protocol, damageType.id(), "damage_type"));
        container.replace(StructuredDataKey.PROVIDES_BANNER_PATTERNS1_21_5, StructuredDataKey.PROVIDES_BANNER_PATTERNS26_1, key -> new ProvidesBannerPatterns(HolderSet.of(key.original())));
        container.replace(StructuredDataKey.DAMAGE_RESISTANT1_21_2, StructuredDataKey.DAMAGE_RESISTANT26_1, damageResistant -> new DamageResistant26_1(HolderSet.of(damageResistant.typesTagKey().original())));
        container.replaceKey(StructuredDataKey.BLOCKS_ATTACKS1_21_5, StructuredDataKey.BLOCKS_ATTACKS26_1);

        // Uses null instead of empty items now
        final Item[] containerData = container.get(dataKeys.container);
        if (containerData != null) {
            for (int i = 0; i < containerData.length; i++) {
                if (containerData[i].isEmpty()) {
                    containerData[i] = null;
                }
            }
        }
    }

    private static @Nullable Integer upgradeEitherVariant(final Protocol<?, ?, ?, ?> protocol, final Either<Integer, String> eitherHolder, final String registry) {
        return eitherHolder.isLeft() ? eitherHolder.left() : registryIdOrNull(protocol, registry, eitherHolder.right());
    }

    private static <T> @Nullable Holder<T> upgradeHolder(final Protocol<?, ?, ?, ?> protocol, final EitherHolder<T> eitherHolder, final String registry) {
        if (eitherHolder.hasHolder()) {
            return eitherHolder.holder();
        }
        final Integer id = registryIdOrNull(protocol, registry, eitherHolder.key());
        return id != null ? Holder.of((int) id) : null;
    }

    private static @Nullable Integer registryIdOrNull(final Protocol<?, ?, ?, ?> protocol, final String registry, final String key) {
        final int id = protocol.getRegistryDataRewriter().getMappings(registry).keyToId(key);
        return id != -1 ? id : null;
    }

    @Override
    protected void handleItemDataComponentsToServer(final UserConnection connection, final Item item, final StructuredDataContainer container) {
        downgradeData(protocol.mappedTypes().structuredDataKeys(), container);
        super.handleItemDataComponentsToServer(connection, item, container);
    }

    public static void downgradeData(final StructuredDataKeys1_21_11 dataKeys, final StructuredDataContainer container) {
        container.replace(StructuredDataKey.JUKEBOX_PLAYABLE26_1, StructuredDataKey.JUKEBOX_PLAYABLE1_21_5, jukeboxPlayable -> new JukeboxPlayable(jukeboxPlayable, true));
        container.replace(StructuredDataKey.INSTRUMENT26_1, StructuredDataKey.INSTRUMENT1_21_5, EitherHolder::of);
        container.replace(StructuredDataKey.PROVIDES_TRIM_MATERIAL26_1, StructuredDataKey.PROVIDES_TRIM_MATERIAL1_21_5, providesTrimMaterial -> new ProvidesTrimMaterial(EitherHolder.of(providesTrimMaterial)));
        container.replace(StructuredDataKey.CHICKEN_VARIANT26_1, StructuredDataKey.CHICKEN_VARIANT1_21_5, Either::left);
        container.replace(StructuredDataKey.ZOMBIE_NAUTILUS_VARIANT26_1, StructuredDataKey.ZOMBIE_NAUTILUS_VARIANT1_21_11, Either::left);
        container.replace(StructuredDataKey.DAMAGE_TYPE26_1, StructuredDataKey.DAMAGE_TYPE1_21_11, damageType -> new DamageType(Either.left(damageType)));
        container.replace(StructuredDataKey.PROVIDES_BANNER_PATTERNS26_1, StructuredDataKey.PROVIDES_BANNER_PATTERNS1_21_5, patterns -> tagOrNull(patterns.patterns()));
        container.replace(StructuredDataKey.DAMAGE_RESISTANT26_1, StructuredDataKey.DAMAGE_RESISTANT1_21_2, damageResistant -> new DamageResistant1_21_2(tagOrNull(damageResistant.types())));
        container.replace(StructuredDataKey.BLOCKS_ATTACKS26_1, StructuredDataKey.BLOCKS_ATTACKS1_21_5, blocksAttacks -> {
            if (blocksAttacks.bypassedBy() == null) {
                return blocksAttacks;
            }
            // Remove bypassed by ids
            return new BlocksAttacks(blocksAttacks.blockDelaySeconds(), blocksAttacks.disableCooldownScale(), blocksAttacks.damageReductions(), blocksAttacks.itemDamage(), null, blocksAttacks.blockSound(), blocksAttacks.disableSound());
        });

        container.remove(StructuredDataKey.ADDITIONAL_TRADE_COST);
        container.remove(StructuredDataKey.DYE);
        container.remove(StructuredDataKey.CAT_SOUND_VARIANT);
        container.remove(StructuredDataKey.CHICKEN_SOUND_VARIANT);
        container.remove(StructuredDataKey.COW_SOUND_VARIANT);
        container.remove(StructuredDataKey.PIG_SOUND_VARIANT);

        final Item[] containerData = container.get(dataKeys.container);
        if (containerData != null) {
            for (int i = 0; i < containerData.length; i++) {
                if (containerData[i] == null) {
                    containerData[i] = StructuredItem.empty();
                }
            }
        }
    }

    private static @Nullable Key tagOrNull(final HolderSet holderSet) {
        return holderSet.hasTagKey() ? Key.of(holderSet.tagKey()) : null;
    }
}

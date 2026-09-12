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
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.Holder;
import com.viaversion.viaversion.api.minecraft.Particle;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataContainer;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.item.StructuredItemTemplate;
import com.viaversion.viaversion.api.minecraft.item.data.PotDecorations1_20_5;
import com.viaversion.viaversion.api.minecraft.item.data.PotDecorations26_3;
import com.viaversion.viaversion.api.minecraft.item.data.consumable.ConsumeEffect;
import com.viaversion.viaversion.api.minecraft.item.data.consumable.TeleportRandomlyConsumeEffect;
import com.viaversion.viaversion.api.minecraft.item.data.trim.ArmorTrim1_20_5;
import com.viaversion.viaversion.api.minecraft.item.data.trim.ArmorTrim26_3;
import com.viaversion.viaversion.api.minecraft.item.data.trim.ArmorTrimMaterial1_20_5;
import com.viaversion.viaversion.api.minecraft.item.data.trim.ArmorTrimMaterial26_3;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.VersionedTypes;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ClientboundPacket26_1;
import com.viaversion.viaversion.protocols.v1_21_11to26_1.packet.ClientboundPackets26_1;
import com.viaversion.viaversion.protocols.v26_2to26_3.Protocol26_2To26_3;
import com.viaversion.viaversion.protocols.v26_2to26_3.packet.ServerboundPacket26_3;
import com.viaversion.viaversion.protocols.v26_2to26_3.packet.ServerboundPackets26_3;
import com.viaversion.viaversion.rewriter.StructuredItemRewriter;
import com.viaversion.viaversion.rewriter.text.NBTComponentRewriter;
import com.viaversion.viaversion.util.Key;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class BlockItemPacketRewriter26_3 extends StructuredItemRewriter<ClientboundPacket26_1, ServerboundPacket26_3, Protocol26_2To26_3> {

    private static final int TELEPORT_RANDOMLY_EFFECT = 3;
    private static final String TRIM_PALETTE_PREFIX = "trim/";

    public BlockItemPacketRewriter26_3(final Protocol26_2To26_3 protocol) {
        super(protocol);
    }

    @Override
    protected void registerPackets() {
        protocol.replaceClientbound(ClientboundPackets26_1.LEVEL_PARTICLES, wrapper -> {
            wrapper.write(protocol.getParticleRewriter().mappedParticleType(), new Particle(0)); // Replace later

            wrapper.passthrough(Types.BOOLEAN); // Override limiter
            wrapper.passthrough(Types.BOOLEAN); // Always show
            wrapper.passthrough(Types.DOUBLE); // X
            wrapper.passthrough(Types.DOUBLE); // Y
            wrapper.passthrough(Types.DOUBLE); // Z
            wrapper.passthrough(Types.FLOAT); // Offset X
            wrapper.passthrough(Types.FLOAT); // Offset Y
            wrapper.passthrough(Types.FLOAT); // Offset Z

            final float maxSpeed = wrapper.read(Types.FLOAT);
            wrapper.write(Types.FLOAT, maxSpeed); // Max x speed
            wrapper.write(Types.FLOAT, maxSpeed); // Max y speed
            wrapper.write(Types.FLOAT, maxSpeed); // Max z speed

            wrapper.passthroughAndMap(Types.INT, Types.VAR_INT); // Particle Count
            wrapper.write(Types.VAR_INT, 0); // Default randomization type

            final Particle particle = wrapper.read(protocol.getParticleRewriter().particleType());
            protocol.getParticleRewriter().rewriteParticle(wrapper.user(), particle);
            wrapper.set(protocol.getParticleRewriter().mappedParticleType(), 0, particle);
        });


        protocol.appendClientbound(ClientboundPackets26_1.EXPLODE, wrapper -> {
            wrapper.write(Types.BOOLEAN, true); // Play sound
        });

        protocol.registerClientbound(ClientboundPackets26_1.OPEN_SIGN_EDITOR, wrapper -> {
            wrapper.passthrough(Types.BLOCK_POSITION1_14);
            final int signTextSlot = wrapper.read(Types.BOOLEAN) ? 1 : 0;
            wrapper.write(Types.VAR_INT, signTextSlot);
        });

        protocol.registerServerbound(ServerboundPackets26_3.SIGN_UPDATE, wrapper -> {
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

        protocol.replaceClientbound(ClientboundPackets26_1.UPDATE_ADVANCEMENTS, wrapper -> {
            float x = 0;
            float y = 0;

            wrapper.passthrough(Types.BOOLEAN); // Reset/clear
            final int size = wrapper.passthrough(Types.VAR_INT); // Mapping size
            for (int i = 0; i < size; i++) {
                wrapper.passthrough(Types.STRING); // Identifier
                wrapper.passthrough(Types.OPTIONAL_STRING); // Parent

                // Display data
                if (wrapper.passthrough(Types.BOOLEAN)) {
                    final Tag title = wrapper.passthrough(Types.TRUSTED_TAG);
                    final Tag description = wrapper.passthrough(Types.TRUSTED_TAG);
                    final NBTComponentRewriter<ClientboundPacket26_1> componentRewriter = protocol.getComponentRewriter();
                    componentRewriter.processTag(wrapper.user(), title);
                    componentRewriter.processTag(wrapper.user(), description);

                    passthroughClientboundItemTemplate(wrapper); // Icon
                    wrapper.passthrough(Types.VAR_INT); // Frame type
                    final int flags = wrapper.passthrough(Types.INT); // Flags
                    if ((flags & 1) != 0) {
                        wrapper.passthrough(Types.STRING); // Background texture
                    }

                    // Moved outside display data
                    x = wrapper.read(Types.FLOAT);
                    y = wrapper.read(Types.FLOAT);
                }

                final int requirements = wrapper.passthrough(Types.VAR_INT);
                for (int array = 0; array < requirements; array++) {
                    wrapper.passthrough(Types.STRING_ARRAY);
                }

                wrapper.passthrough(Types.BOOLEAN); // Send telemetry
                wrapper.write(Types.FLOAT, x);
                wrapper.write(Types.FLOAT, y);
            }
        });
    }

    private void handleLightMasks(final PacketWrapper wrapper) {
        for (int i = 0; i < 4; i++) {
            final long[] mask = wrapper.read(Types.LONG_ARRAY_PRIMITIVE);
            wrapper.write(Types.BIT_SET, BitSet.valueOf(mask));
        }
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
        container.remove(StructuredDataKey.MAP_COLOR);

        container.replaceKey(StructuredDataKey.INSTRUMENT26_1, StructuredDataKey.INSTRUMENT26_3);
        container.replace(StructuredDataKey.SWING_ANIMATION, animation -> {
            container.set(StructuredDataKey.ATTACK_ANIMATION, animation);
            container.set(StructuredDataKey.INTERACT_ANIMATION, animation);
            return null;
        });
        container.replace(StructuredDataKey.PROVIDES_TRIM_MATERIAL26_1, StructuredDataKey.PROVIDES_TRIM_MATERIAL26_3, holder -> {
            if (holder.hasId()) {
                return Holder.of(holder.id());
            }

            final ArmorTrimMaterial1_20_5 material = holder.value();
            return Holder.of(new ArmorTrimMaterial26_3(trimPaletteId(material.assetName()), material.description()));
        });
        container.replace(StructuredDataKey.TRIM1_21_5, StructuredDataKey.TRIM26_3, trim -> {
            if (trim.material().hasId()) {
                return new ArmorTrim26_3(Holder.of(trim.material().id()), trim.pattern());
            }

            final ArmorTrimMaterial1_20_5 material = trim.material().value();
            return new ArmorTrim26_3(Holder.of(new ArmorTrimMaterial26_3(trimPaletteId(material.assetName()), material.description())), trim.pattern());
        });
        container.replace(StructuredDataKey.POT_DECORATIONS1_20_5, VersionedTypes.V26_3.structuredDataKeys().potDecorations, decorations -> {
            return new PotDecorations26_3(
                new StructuredItemTemplate(decorations.backItem(), 1),
                new StructuredItemTemplate(decorations.leftItem(), 1),
                new StructuredItemTemplate(decorations.rightItem(), 1),
                new StructuredItemTemplate(decorations.frontItem(), 1)
            );
        });
        container.replace(StructuredDataKey.CONSUMABLE1_21_2, StructuredDataKey.CONSUMABLE26_3, consumable -> {
            upgradeConsumeEffects(consumable.consumeEffects());
            return consumable;
        });
        container.replace(StructuredDataKey.DEATH_PROTECTION1_21_2, StructuredDataKey.DEATH_PROTECTION26_3, deathProtection -> {
            upgradeConsumeEffects(deathProtection.deathEffects());
            return deathProtection;
        });
    }

    public static void downgradeData(final StructuredDataContainer container) {
        container.remove(StructuredDataKey.PROVIDES_POTTERY_PATTERN);
        container.remove(StructuredDataKey.BLOCK_TRANSFORMER);
        container.remove(StructuredDataKey.COMPOSTABLE);
        container.remove(StructuredDataKey.VILLAGER_FOOD);
        container.remove(StructuredDataKey.COOKING_FUEL);
        container.remove(StructuredDataKey.BREWING_FUEL);
        container.remove(StructuredDataKey.MOB_VISIBILITY);
        container.remove(StructuredDataKey.SIGN_TEXT_FRONT);
        container.remove(StructuredDataKey.SIGN_TEXT_BACK);
        container.remove(StructuredDataKey.WAXED);
        container.remove(StructuredDataKey.CUSHION_COLOR);

        container.replace(StructuredDataKey.CONSUMABLE26_3, StructuredDataKey.CONSUMABLE1_21_2, consumable -> {
            downgradeConsumeEffects(consumable.consumeEffects());
            return consumable;
        });
        container.replace(StructuredDataKey.DEATH_PROTECTION26_3, StructuredDataKey.DEATH_PROTECTION1_21_2, deathProtection -> {
            downgradeConsumeEffects(deathProtection.deathEffects());
            return deathProtection;
        });
        container.replaceKey(StructuredDataKey.ATTACK_ANIMATION, StructuredDataKey.SWING_ANIMATION);
        container.replaceKey(StructuredDataKey.INTERACT_ANIMATION, StructuredDataKey.SWING_ANIMATION);
        container.replaceKey(StructuredDataKey.INSTRUMENT26_3, StructuredDataKey.INSTRUMENT26_1);
        container.replace(StructuredDataKey.PROVIDES_TRIM_MATERIAL26_3, StructuredDataKey.PROVIDES_TRIM_MATERIAL26_1, holder -> {
            if (holder.hasId()) {
                return Holder.of(holder.id());
            }

            final ArmorTrimMaterial26_3 trim = holder.value();
            return Holder.of(new ArmorTrimMaterial1_20_5(trimAssetName(trim.paletteId()), new HashMap<>(), trim.description()));
        });
        container.replace(StructuredDataKey.TRIM26_3, StructuredDataKey.TRIM1_21_5, trim -> {
            if (trim.material().hasId()) {
                return new ArmorTrim1_20_5(Holder.of(trim.material().id()), trim.pattern());
            }

            final ArmorTrimMaterial26_3 material = trim.material().value();
            return new ArmorTrim1_20_5(Holder.of(new ArmorTrimMaterial1_20_5(
                trimAssetName(material.paletteId()),
                new HashMap<>(),
                material.description()
            )), trim.pattern());
        });
        container.replace(VersionedTypes.V26_3.structuredDataKeys().potDecorations, StructuredDataKey.POT_DECORATIONS1_20_5, decorations -> {
            return new PotDecorations1_20_5(new int[]{potDecorationId(decorations.back()), potDecorationId(decorations.left()), potDecorationId(decorations.right()), potDecorationId(decorations.front())});
        });
    }

    public static String trimPaletteId(final String assetName) {
        return Key.namespaced(TRIM_PALETTE_PREFIX + assetName);
    }

    public static String trimAssetName(final String paletteId) {
        final String path = Key.stripNamespace(paletteId);
        return path.startsWith(TRIM_PALETTE_PREFIX) ? path.substring(TRIM_PALETTE_PREFIX.length()) : path;
    }

    private static void upgradeConsumeEffects(final ConsumeEffect<?>[] effects) {
        for (int i = 0; i < effects.length; i++) {
            if (effects[i].value() instanceof final Float diameter) {
                effects[i] = new ConsumeEffect<>(TELEPORT_RANDOMLY_EFFECT, ConsumeEffect.TELEPORT_RANDOMLY_TYPE26_3, new TeleportRandomlyConsumeEffect(diameter, false));
            }
        }
    }

    private static void downgradeConsumeEffects(final ConsumeEffect<?>[] effects) {
        for (int i = 0; i < effects.length; i++) {
            if (effects[i].value() instanceof final TeleportRandomlyConsumeEffect effect) {
                effects[i] = new ConsumeEffect<>(TELEPORT_RANDOMLY_EFFECT, ConsumeEffect.TELEPORT_RANDOMLY_TYPE1_21_2, effect.diameter());
            }
        }
    }

    private static int potDecorationId(final @Nullable Item decoration) {
        return decoration != null ? decoration.identifier() : Protocol26_2To26_3.MAPPINGS.getFullItemMappings().id("brick");
    }

    @Override
    protected void backupInconvertibleData(final UserConnection connection, final Item item, final StructuredDataContainer container, final CompoundTag backupTag) {
        super.backupInconvertibleData(connection, item, container, backupTag);

        final ArmorTrim1_20_5 trim = container.get(StructuredDataKey.TRIM1_21_5);
        if (trim != null) {
            backupTrimMaterial(backupTag, "trim", trim.material());
        }

        backupTrimMaterial(backupTag, "provides_trim_material", container.get(StructuredDataKey.PROVIDES_TRIM_MATERIAL26_1));
    }

    private void backupTrimMaterial(final CompoundTag tag, final String key, @Nullable final Holder<ArmorTrimMaterial1_20_5> materialHolder) {
        if (materialHolder == null || materialHolder.hasId()) {
            return;
        }

        final CompoundTag trimTag = new CompoundTag();
        final ArmorTrimMaterial1_20_5 material = materialHolder.value();
        final CompoundTag overrides = new CompoundTag();
        for (final Map.Entry<String, String> entry : material.overrideArmorMaterials().entrySet()) {
            overrides.putString(entry.getKey(), entry.getValue());
        }
        trimTag.put("overrides", overrides);
        trimTag.putInt("item", material.itemId());
        tag.put(key, trimTag);
    }

    @Override
    protected void restoreBackupData(final Item item, final StructuredDataContainer container, final CompoundTag customData) {
        super.restoreBackupData(item, container, customData);
        if (!(customData.remove(nbtTagName("backup")) instanceof final CompoundTag backupTag)) {
            return;
        }

        final CompoundTag trimTag = backupTag.getCompoundTag("trim");
        if (trimTag != null) {
            container.replace(StructuredDataKey.TRIM26_3, StructuredDataKey.TRIM1_21_5, trim -> new ArmorTrim1_20_5(restoreTrimMaterial(trim.material(), trimTag), trim.pattern()));
        }

        final CompoundTag providesTrimMaterialTag = backupTag.getCompoundTag("provides_trim_material");
        if (providesTrimMaterialTag != null) {
            container.replace(StructuredDataKey.PROVIDES_TRIM_MATERIAL26_3, StructuredDataKey.PROVIDES_TRIM_MATERIAL26_1, material -> restoreTrimMaterial(material, providesTrimMaterialTag));
        }
    }

    private Holder<ArmorTrimMaterial1_20_5> restoreTrimMaterial(final Holder<ArmorTrimMaterial26_3> holder, final CompoundTag tag) {
        if (holder.hasId()) {
            return Holder.of(holder.id());
        }

        final ArmorTrimMaterial26_3 material = holder.value();
        final Map<String, String> overrides = new HashMap<>();
        final CompoundTag overridesTag = tag.getCompoundTag("overrides");
        if (overridesTag != null) {
            for (final Map.Entry<String, Tag> entry : overridesTag.entrySet()) {
                if (entry.getValue() instanceof final StringTag value) {
                    overrides.put(entry.getKey(), value.getValue());
                }
            }
        }
        return Holder.of(new ArmorTrimMaterial1_20_5(
            trimAssetName(material.paletteId()),
            tag.getInt("item"),
            overrides,
            material.description()
        ));
    }
}

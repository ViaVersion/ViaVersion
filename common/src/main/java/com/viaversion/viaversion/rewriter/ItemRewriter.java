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
package com.viaversion.viaversion.rewriter;

import com.google.gson.JsonElement;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.Mappings;
import com.viaversion.viaversion.api.data.ParticleMappings;
import com.viaversion.viaversion.api.minecraft.Particle;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.ServerboundPacketType;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.rewriter.ComponentRewriter;
import com.viaversion.viaversion.api.rewriter.RewriterBase;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import org.checkerframework.checker.nullness.qual.Nullable;

public class ItemRewriter<C extends ClientboundPacketType, S extends ServerboundPacketType,
    T extends Protocol<C, ?, ?, S>> extends RewriterBase<T> implements com.viaversion.viaversion.api.rewriter.ItemRewriter<T> {
    private final Type<Item> itemType;
    private final Type<Item> mappedItemType;
    private final Type<Item[]> itemArrayType;
    private final Type<Item[]> mappedItemArrayType;
    private final Type<Item> itemCostType;
    private final Type<Item> mappedItemCostType;
    private final Type<Item> optionalItemCostType;
    private final Type<Item> mappedOptionalItemCostType;
    private final Type<Particle> particleType;
    private final Type<Particle> mappedParticleType;

    public ItemRewriter(
        T protocol,
        Type<Item> itemType, Type<Item[]> itemArrayType, Type<Item> mappedItemType, Type<Item[]> mappedItemArrayType,
        Type<Item> itemCostType, Type<Item> optionalItemCostType, Type<Item> mappedItemCostType, Type<Item> mappedOptionalItemCostType,
        Type<Particle> particleType, Type<Particle> mappedParticleType
    ) {
        super(protocol);
        this.itemType = itemType;
        this.itemArrayType = itemArrayType;
        this.mappedItemType = mappedItemType;
        this.mappedItemArrayType = mappedItemArrayType;
        this.itemCostType = itemCostType;
        this.mappedItemCostType = mappedItemCostType;
        this.optionalItemCostType = optionalItemCostType;
        this.mappedOptionalItemCostType = mappedOptionalItemCostType;
        this.particleType = particleType;
        this.mappedParticleType = mappedParticleType;
    }

    public ItemRewriter(T protocol, Type<Item> itemType, Type<Item[]> itemArrayType, Type<Item> mappedItemType, Type<Item[]> mappedItemArrayType) {
        this(protocol, itemType, itemArrayType, mappedItemType, mappedItemArrayType, null, null, null, null, null, null);
    }

    public ItemRewriter(T protocol, Type<Item> itemType, Type<Item[]> itemArrayType) {
        this(protocol, itemType, itemArrayType, itemType, itemArrayType);
    }

    @Override
    public @Nullable Item handleItemToClient(final UserConnection connection, @Nullable Item item) {
        if (item == null) return null;
        if (protocol.getMappingData() != null && protocol.getMappingData().getItemMappings() != null) {
            item.setIdentifier(protocol.getMappingData().getNewItemId(item.identifier()));
        }
        return item;
    }

    @Override
    public @Nullable Item handleItemToServer(final UserConnection connection, @Nullable Item item) {
        if (item == null) return null;
        if (protocol.getMappingData() != null && protocol.getMappingData().getItemMappings() != null) {
            item.setIdentifier(protocol.getMappingData().getOldItemId(item.identifier()));
        }
        return item;
    }

    public void registerSetContent(C packetType) {
        protocol.registerClientbound(packetType, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.UNSIGNED_BYTE); // Window id
                handler(wrapper -> {
                    Item[] items = wrapper.read(itemArrayType);
                    wrapper.write(mappedItemArrayType, items);
                    for (int i = 0; i < items.length; i++) {
                        items[i] = handleItemToClient(wrapper.user(), items[i]);
                    }
                });
            }
        });
    }

    public void registerSetContent1_17_1(C packetType) {
        protocol.registerClientbound(packetType, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.UNSIGNED_BYTE); // Window id
                map(Types.VAR_INT); // State id
                handler(wrapper -> {
                    Item[] items = wrapper.read(itemArrayType);
                    wrapper.write(mappedItemArrayType, items);
                    for (int i = 0; i < items.length; i++) {
                        items[i] = handleItemToClient(wrapper.user(), items[i]);
                    }

                    handleClientboundItem(wrapper);
                });
            }
        });
    }

    public void registerOpenScreen(C packetType) {
        protocol.registerClientbound(packetType, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); // Container id
                handler(wrapper -> handleMenuType(wrapper));
            }
        });
    }

    public void handleMenuType(final PacketWrapper wrapper) {
        final int windowType = wrapper.read(Types.VAR_INT);
        final int mappedId = protocol.getMappingData().getMenuMappings().getNewId(windowType);
        if (mappedId == -1) {
            wrapper.cancel();
            return;
        }

        wrapper.write(Types.VAR_INT, mappedId);
    }

    public void registerSetSlot(C packetType) {
        protocol.registerClientbound(packetType, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.UNSIGNED_BYTE); // Window id
                map(Types.SHORT); // Slot id
                handler(wrapper -> handleClientboundItem(wrapper));
            }
        });
    }

    public void registerSetSlot1_17_1(C packetType) {
        protocol.registerClientbound(packetType, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.UNSIGNED_BYTE); // Window id
                map(Types.VAR_INT); // State id
                map(Types.SHORT); // Slot id
                handler(wrapper -> handleClientboundItem(wrapper));
            }
        });
    }

    // Sub 1.16
    public void registerSetEquippedItem(C packetType) {
        protocol.registerClientbound(packetType, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); // Entity ID
                map(Types.VAR_INT); // Slot ID
                handler(wrapper -> handleClientboundItem(wrapper));
            }
        });
    }

    // 1.16+
    public void registerSetEquipment(C packetType) {
        protocol.registerClientbound(packetType, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); // 0 - Entity ID

                handler(wrapper -> {
                    byte slot;
                    do {
                        slot = wrapper.passthrough(Types.BYTE);
                        // & 0x7F into an extra variable if slot is needed
                        handleClientboundItem(wrapper);
                    } while (slot < 0);
                });
            }
        });
    }

    public void registerSetCreativeModeSlot(S packetType) {
        protocol.registerServerbound(packetType, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.SHORT); // 0 - Slot
                handler(wrapper -> handleServerboundItem(wrapper));
            }
        });
    }

    public void registerContainerClick(S packetType) {
        protocol.registerServerbound(packetType, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.UNSIGNED_BYTE); // 0 - Window ID
                map(Types.SHORT); // 1 - Slot
                map(Types.BYTE); // 2 - Button
                map(Types.SHORT); // 3 - Action number
                map(Types.VAR_INT); // 4 - Mode
                handler(wrapper -> handleServerboundItem(wrapper));
            }
        });
    }

    public void registerContainerClick1_17_1(S packetType) {
        protocol.registerServerbound(packetType, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.UNSIGNED_BYTE); // Window Id
                map(Types.VAR_INT); // State id
                map(Types.SHORT); // Slot
                map(Types.BYTE); // Button
                map(Types.VAR_INT); // Mode

                handler(wrapper -> {
                    // Affected items
                    int length = wrapper.passthrough(Types.VAR_INT);
                    for (int i = 0; i < length; i++) {
                        wrapper.passthrough(Types.SHORT); // Slot
                        handleServerboundItem(wrapper);
                    }

                    // Carried item
                    handleServerboundItem(wrapper);
                });
            }
        });
    }

    public void registerCooldown(C packetType) {
        protocol.registerClientbound(packetType, wrapper -> {
            int itemId = wrapper.read(Types.VAR_INT);
            wrapper.write(Types.VAR_INT, protocol.getMappingData().getNewItemId(itemId));
        });
    }


    public void registerCustomPayloadTradeList(C packetType) {
        protocol.registerClientbound(packetType, new PacketHandlers() {
            @Override
            protected void register() {
                map(Types.STRING); // 0 - Channel
                handlerSoftFail(wrapper -> {
                    final String channel = wrapper.get(Types.STRING, 0);
                    if (channel.equals("MC|TrList")) {
                        handleTradeList(wrapper);
                    }
                });
            }
        });
    }

    public void handleTradeList(final PacketWrapper wrapper) {
        wrapper.passthrough(Types.INT); // Window ID

        final int size = wrapper.passthrough(Types.UNSIGNED_BYTE);
        for (int i = 0; i < size; i++) {
            handleClientboundItem(wrapper); // Input Item
            handleClientboundItem(wrapper); // Output Item

            if (wrapper.passthrough(Types.BOOLEAN)) {
                handleClientboundItem(wrapper); // Second Item
            }

            wrapper.passthrough(Types.BOOLEAN); // Trade disabled
            wrapper.passthrough(Types.INT); // Number of tools uses
            wrapper.passthrough(Types.INT); // Maximum number of trade uses
        }
    }


    // 1.14.4+
    public void registerMerchantOffers(C packetType) {
        protocol.registerClientbound(packetType, wrapper -> {
            wrapper.passthrough(Types.VAR_INT);
            int size = wrapper.passthrough(Types.UNSIGNED_BYTE);
            for (int i = 0; i < size; i++) {
                handleClientboundItem(wrapper); // Input
                handleClientboundItem(wrapper); // Output

                if (wrapper.passthrough(Types.BOOLEAN)) { // Has second item
                    handleClientboundItem(wrapper); // Second item
                }

                wrapper.passthrough(Types.BOOLEAN); // Trade disabled
                wrapper.passthrough(Types.INT); // Number of tools uses
                wrapper.passthrough(Types.INT); // Maximum number of trade uses

                wrapper.passthrough(Types.INT); // XP
                wrapper.passthrough(Types.INT); // Special price
                wrapper.passthrough(Types.FLOAT); // Price multiplier
                wrapper.passthrough(Types.INT); // Demand
            }
            //...
        });
    }

    public void registerMerchantOffers1_19(C packetType) {
        protocol.registerClientbound(packetType, wrapper -> {
            wrapper.passthrough(Types.VAR_INT); // Container id
            int size = wrapper.passthrough(Types.VAR_INT);
            for (int i = 0; i < size; i++) {
                handleClientboundItem(wrapper); // Input
                handleClientboundItem(wrapper); // Output
                handleClientboundItem(wrapper); // Second item

                wrapper.passthrough(Types.BOOLEAN); // Trade disabled
                wrapper.passthrough(Types.INT); // Number of tools uses
                wrapper.passthrough(Types.INT); // Maximum number of trade uses

                wrapper.passthrough(Types.INT); // XP
                wrapper.passthrough(Types.INT); // Special price
                wrapper.passthrough(Types.FLOAT); // Price multiplier
                wrapper.passthrough(Types.INT); // Demand
            }
        });
    }

    // Hopefully the item cost weirdness is temporary
    public void registerMerchantOffers1_20_5(final C packetType) {
        protocol.registerClientbound(packetType, wrapper -> {
            wrapper.passthrough(Types.VAR_INT); // Container id
            int size = wrapper.passthrough(Types.VAR_INT);
            for (int i = 0; i < size; i++) {
                final Item input = wrapper.read(itemCostType);
                wrapper.write(mappedItemCostType, handleItemToClient(wrapper.user(), input));

                handleClientboundItem(wrapper); // Result

                Item secondInput = wrapper.read(optionalItemCostType);
                if (secondInput != null) {
                    handleItemToClient(wrapper.user(), secondInput);
                }
                wrapper.write(mappedOptionalItemCostType, secondInput);

                wrapper.passthrough(Types.BOOLEAN); // Out of stock
                wrapper.passthrough(Types.INT); // Number of trade uses
                wrapper.passthrough(Types.INT); // Maximum number of trade uses

                wrapper.passthrough(Types.INT); // XP
                wrapper.passthrough(Types.INT); // Special price
                wrapper.passthrough(Types.FLOAT); // Price multiplier
                wrapper.passthrough(Types.INT); // Demand
            }
        });
    }

    public void registerAdvancements(C packetType) {
        protocol.registerClientbound(packetType, wrapper -> {
            wrapper.passthrough(Types.BOOLEAN); // Reset/clear
            int size = wrapper.passthrough(Types.VAR_INT); // Mapping size
            for (int i = 0; i < size; i++) {
                wrapper.passthrough(Types.STRING); // Identifier
                wrapper.passthrough(Types.OPTIONAL_STRING); // Parent

                // Display data
                if (wrapper.passthrough(Types.BOOLEAN)) {
                    final JsonElement title = wrapper.passthrough(Types.COMPONENT); // Title
                    final JsonElement description = wrapper.passthrough(Types.COMPONENT); // Description
                    final ComponentRewriter componentRewriter = protocol.getComponentRewriter();
                    if (componentRewriter != null) {
                        componentRewriter.processText(wrapper.user(), title);
                        componentRewriter.processText(wrapper.user(), description);
                    }

                    handleClientboundItem(wrapper); // Icon
                    wrapper.passthrough(Types.VAR_INT); // Frame type
                    int flags = wrapper.passthrough(Types.INT); // Flags
                    if ((flags & 1) != 0) {
                        wrapper.passthrough(Types.STRING); // Background texture
                    }
                    wrapper.passthrough(Types.FLOAT); // X
                    wrapper.passthrough(Types.FLOAT); // Y
                }

                wrapper.passthrough(Types.STRING_ARRAY); // Criteria

                int arrayLength = wrapper.passthrough(Types.VAR_INT);
                for (int array = 0; array < arrayLength; array++) {
                    wrapper.passthrough(Types.STRING_ARRAY); // String array
                }
            }
        });
    }

    public void registerAdvancements1_20_3(C packetType) {
        protocol.registerClientbound(packetType, wrapper -> {
            wrapper.passthrough(Types.BOOLEAN); // Reset/clear
            int size = wrapper.passthrough(Types.VAR_INT); // Mapping size
            for (int i = 0; i < size; i++) {
                wrapper.passthrough(Types.STRING); // Identifier
                wrapper.passthrough(Types.OPTIONAL_STRING); // Parent

                // Display data
                if (wrapper.passthrough(Types.BOOLEAN)) {
                    final Tag title = wrapper.passthrough(Types.TAG);
                    final Tag description = wrapper.passthrough(Types.TAG);
                    final ComponentRewriter componentRewriter = protocol.getComponentRewriter();
                    if (componentRewriter != null) {
                        componentRewriter.processTag(wrapper.user(), title);
                        componentRewriter.processTag(wrapper.user(), description);
                    }

                    handleClientboundItem(wrapper); // Icon
                    wrapper.passthrough(Types.VAR_INT); // Frame type
                    int flags = wrapper.passthrough(Types.INT); // Flags
                    if ((flags & 1) != 0) {
                        wrapper.passthrough(Types.STRING); // Background texture
                    }
                    wrapper.passthrough(Types.FLOAT); // X
                    wrapper.passthrough(Types.FLOAT); // Y
                }

                int requirements = wrapper.passthrough(Types.VAR_INT);
                for (int array = 0; array < requirements; array++) {
                    wrapper.passthrough(Types.STRING_ARRAY);
                }

                wrapper.passthrough(Types.BOOLEAN); // Send telemetry
            }
        });
    }

    // Pre 1.21 for enchantments
    public void registerContainerSetData(C packetType) {
        protocol.registerClientbound(packetType, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.UNSIGNED_BYTE); // Container id
                handler(wrapper -> {
                    Mappings mappings = protocol.getMappingData().getEnchantmentMappings();
                    if (mappings == null) {
                        return;
                    }

                    short property = wrapper.passthrough(Types.SHORT);
                    if (property >= 4 && property <= 6) { // Enchantment id
                        short enchantmentId = (short) mappings.getNewId(wrapper.read(Types.SHORT));
                        wrapper.write(Types.SHORT, enchantmentId);
                    }
                });
            }
        });
    }

    // Not the very best place for this, but has to stay here until *everything* is abstracted
    public void registerLevelParticles(C packetType, Type<?> coordType) {
        protocol.registerClientbound(packetType, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.INT); // 0 - Particle ID
                map(Types.BOOLEAN); // 1 - Long Distance
                map(coordType); // 2 - X
                map(coordType); // 3 - Y
                map(coordType); // 4 - Z
                map(Types.FLOAT); // 5 - Offset X
                map(Types.FLOAT); // 6 - Offset Y
                map(Types.FLOAT); // 7 - Offset Z
                map(Types.FLOAT); // 8 - Particle Data
                map(Types.INT); // 9 - Particle Count
                handler(levelParticlesHandler());
            }
        });
    }

    public void registerLevelParticles1_19(C packetType) {
        protocol.registerClientbound(packetType, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); // 0 - Particle ID
                map(Types.BOOLEAN); // 1 - Long Distance
                map(Types.DOUBLE); // 2 - X
                map(Types.DOUBLE); // 3 - Y
                map(Types.DOUBLE); // 4 - Z
                map(Types.FLOAT); // 5 - Offset X
                map(Types.FLOAT); // 6 - Offset Y
                map(Types.FLOAT); // 7 - Offset Z
                map(Types.FLOAT); // 8 - Particle Data
                map(Types.INT); // 9 - Particle Count
                handler(levelParticlesHandler(Types.VAR_INT));
            }
        });
    }

    public void registerLevelParticles1_20_5(C packetType) {
        protocol.registerClientbound(packetType, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.BOOLEAN); // Long Distance
                map(Types.DOUBLE); // X
                map(Types.DOUBLE); // Y
                map(Types.DOUBLE); // Z
                map(Types.FLOAT); // Offset X
                map(Types.FLOAT); // Offset Y
                map(Types.FLOAT); // Offset Z
                map(Types.FLOAT); // Particle Data
                map(Types.INT); // Particle Count
                handler(wrapper -> {
                    final Particle particle = wrapper.read(particleType);
                    rewriteParticle(wrapper.user(), particle);
                    wrapper.write(mappedParticleType, particle);
                });
            }
        });
    }

    public void registerExplosion(C packetType) {
        final SoundRewriter<C> soundRewriter = new SoundRewriter<>(protocol);
        protocol.registerClientbound(packetType, wrapper -> {
            wrapper.passthrough(Types.DOUBLE); // X
            wrapper.passthrough(Types.DOUBLE); // Y
            wrapper.passthrough(Types.DOUBLE); // Z
            wrapper.passthrough(Types.FLOAT); // Power
            final int blocks = wrapper.passthrough(Types.VAR_INT);
            for (int i = 0; i < blocks; i++) {
                wrapper.passthrough(Types.BYTE); // Relative X
                wrapper.passthrough(Types.BYTE); // Relative Y
                wrapper.passthrough(Types.BYTE); // Relative Z
            }
            wrapper.passthrough(Types.FLOAT); // Knockback X
            wrapper.passthrough(Types.FLOAT); // Knockback Y
            wrapper.passthrough(Types.FLOAT); // Knockback Z
            wrapper.passthrough(Types.VAR_INT); // Block interaction type

            final Particle smallExplosionParticle = wrapper.read(particleType);
            final Particle largeExplosionParticle = wrapper.read(particleType);
            wrapper.write(mappedParticleType, smallExplosionParticle);
            wrapper.write(mappedParticleType, largeExplosionParticle);
            rewriteParticle(wrapper.user(), smallExplosionParticle);
            rewriteParticle(wrapper.user(), largeExplosionParticle);

            soundRewriter.soundHolderHandler().handle(wrapper);
        });
    }

    public PacketHandler levelParticlesHandler() {
        return levelParticlesHandler(Types.INT);
    }

    public PacketHandler levelParticlesHandler(Type<Integer> idType) {
        return wrapper -> {
            int id = wrapper.get(idType, 0);
            if (id == -1) {
                return;
            }

            ParticleMappings mappings = protocol.getMappingData().getParticleMappings();
            if (mappings.isBlockParticle(id)) {
                int data = wrapper.read(Types.VAR_INT);
                wrapper.write(Types.VAR_INT, protocol.getMappingData().getNewBlockStateId(data));
            } else if (mappings.isItemParticle(id)) {
                handleClientboundItem(wrapper);
            }

            int mappedId = protocol.getMappingData().getNewParticleId(id);
            if (mappedId != id) {
                wrapper.set(idType, 0, mappedId);
            }
        };
    }

    private void handleClientboundItem(final PacketWrapper wrapper) {
        final Item item = handleItemToClient(wrapper.user(), wrapper.read(itemType));
        wrapper.write(mappedItemType, item);
    }

    private void handleServerboundItem(final PacketWrapper wrapper) {
        final Item item = handleItemToServer(wrapper.user(), wrapper.read(mappedItemType));
        wrapper.write(itemType, item);
    }

    protected void rewriteParticle(UserConnection connection, Particle particle) {
        ParticleMappings mappings = protocol.getMappingData().getParticleMappings();
        int id = particle.id();
        if (mappings.isBlockParticle(id)) {
            Particle.ParticleData<Integer> data = particle.getArgument(0);
            data.setValue(protocol.getMappingData().getNewBlockStateId(data.getValue()));
        } else if (mappings.isItemParticle(id)) {
            Particle.ParticleData<Item> data = particle.getArgument(0);
            Item item = handleItemToClient(connection, data.getValue());
            if (mappedItemType() != null && itemType() != mappedItemType()) {
                // Replace the type
                particle.set(0, mappedItemType(), item);
            } else {
                data.setValue(item);
            }
        }

        particle.setId(protocol.getMappingData().getNewParticleId(id));
    }

    @Override
    public Type<Item> itemType() {
        return itemType;
    }

    @Override
    public Type<Item[]> itemArrayType() {
        return itemArrayType;
    }

    @Override
    public Type<Item> mappedItemType() {
        return mappedItemType;
    }

    @Override
    public Type<Item[]> mappedItemArrayType() {
        return mappedItemArrayType;
    }
}

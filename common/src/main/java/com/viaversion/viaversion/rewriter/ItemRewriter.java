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

    public ItemRewriter(T protocol, Type<Item> itemType, Type<Item[]> itemArrayType, Type<Item> mappedItemType, Type<Item[]> mappedItemArrayType) {
        super(protocol);
        this.itemType = itemType;
        this.itemArrayType = itemArrayType;
        this.mappedItemType = mappedItemType;
        this.mappedItemArrayType = mappedItemArrayType;
    }

    public ItemRewriter(T protocol, Type<Item> itemType, Type<Item[]> itemArrayType) {
        this(protocol, itemType, itemArrayType, itemType, itemArrayType);
    }

    @Override
    public @Nullable Item handleItemToClient(final UserConnection connection,  @Nullable Item item) {
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

    public void registerWindowItems(C packetType) {
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

    public void registerWindowItems1_17_1(C packetType) {
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

    public void registerOpenWindow(C packetType) {
        protocol.registerClientbound(packetType, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); // Container id
                handler(wrapper -> {
                    final int windowType = wrapper.read(Types.VAR_INT);
                    final int mappedId = protocol.getMappingData().getMenuMappings().getNewId(windowType);
                    if (mappedId == -1) {
                        wrapper.cancel();
                        return;
                    }

                    wrapper.write(Types.VAR_INT, mappedId);
                });
            }
        });
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
    public void registerEntityEquipment(C packetType) {
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
    public void registerEntityEquipmentArray(C packetType) {
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

    public void registerCreativeInvAction(S packetType) {
        protocol.registerServerbound(packetType, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.SHORT); // 0 - Slot
                handler(wrapper -> handleServerboundItem(wrapper));
            }
        });
    }

    public void registerClickWindow(S packetType) {
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

    public void registerClickWindow1_17_1(S packetType) {
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

    public void registerSetCooldown(C packetType) {
        protocol.registerClientbound(packetType, wrapper -> {
            int itemId = wrapper.read(Types.VAR_INT);
            wrapper.write(Types.VAR_INT, protocol.getMappingData().getNewItemId(itemId));
        });
    }

    // 1.14.4+
    public void registerTradeList(C packetType) {
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

    public void registerTradeList1_19(C packetType) {
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
    public void registerTradeList1_20_5(
        final C packetType,
        final Type<Item> costType, final Type<Item> mappedCostType,
        final Type<Item> optionalCostType, final Type<Item> mappedOptionalCostType
    ) {
        protocol.registerClientbound(packetType, wrapper -> {
            wrapper.passthrough(Types.VAR_INT); // Container id
            int size = wrapper.passthrough(Types.VAR_INT);
            for (int i = 0; i < size; i++) {
                final Item input = wrapper.read(costType);
                wrapper.write(mappedCostType, handleItemToClient(wrapper.user(), input));

                handleClientboundItem(wrapper); // Result

                final Item secondInput = wrapper.read(optionalCostType);
                wrapper.write(mappedOptionalCostType, handleItemToClient(wrapper.user(), secondInput));

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
                    wrapper.passthrough(Types.COMPONENT); // Title
                    wrapper.passthrough(Types.COMPONENT); // Description
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

    public void registerAdvancements1_20_2(C packetType) {
        registerAdvancements1_20_2(packetType, Types.COMPONENT);
    }

    public void registerAdvancements1_20_3(C packetType) {
        registerAdvancements1_20_2(packetType, Types.TAG);
    }

    private void registerAdvancements1_20_2(C packetType, Type<?> componentType) {
        protocol.registerClientbound(packetType, wrapper -> {
            wrapper.passthrough(Types.BOOLEAN); // Reset/clear
            int size = wrapper.passthrough(Types.VAR_INT); // Mapping size
            for (int i = 0; i < size; i++) {
                wrapper.passthrough(Types.STRING); // Identifier
                wrapper.passthrough(Types.OPTIONAL_STRING); // Parent

                // Display data
                if (wrapper.passthrough(Types.BOOLEAN)) {
                    wrapper.passthrough(componentType); // Title
                    wrapper.passthrough(componentType); // Description
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

    public void registerWindowPropertyEnchantmentHandler(C packetType) {
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
    public void registerSpawnParticle(C packetType, Type<?> coordType) {
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
                handler(getSpawnParticleHandler());
            }
        });
    }

    public void registerSpawnParticle1_19(C packetType) {
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
                handler(getSpawnParticleHandler(Types.VAR_INT));
            }
        });
    }

    public void registerSpawnParticle1_20_5(C packetType, Type<Particle> unmappedParticleType, Type<Particle> mappedParticleType) {
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
                    final Particle particle = wrapper.read(unmappedParticleType);
                    rewriteParticle(wrapper.user(), particle);
                    wrapper.write(mappedParticleType, particle);
                });
            }
        });
    }

    public void registerExplosion(C packetType, Type<Particle> unmappedParticleType, Type<Particle> mappedParticleType) {
        final SoundRewriter<C> cSoundRewriter = new SoundRewriter<>(protocol);
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

            final Particle smallExplosionParticle = wrapper.read(unmappedParticleType);
            final Particle largeExplosionParticle = wrapper.read(unmappedParticleType);
            wrapper.write(mappedParticleType, smallExplosionParticle);
            wrapper.write(mappedParticleType, largeExplosionParticle);
            rewriteParticle(wrapper.user(), smallExplosionParticle);
            rewriteParticle(wrapper.user(), largeExplosionParticle);

            cSoundRewriter.soundHolderHandler().handle(wrapper);
        });
    }

    public PacketHandler getSpawnParticleHandler() {
        return getSpawnParticleHandler(Types.INT);
    }

    public PacketHandler getSpawnParticleHandler(Type<Integer> idType) {
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

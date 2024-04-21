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
                map(Type.UNSIGNED_BYTE); // Window id
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
                map(Type.UNSIGNED_BYTE); // Window id
                map(Type.VAR_INT); // State id
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
                map(Type.VAR_INT); // Container id
                handler(wrapper -> {
                    final int windowType = wrapper.read(Type.VAR_INT);
                    final int mappedId = protocol.getMappingData().getMenuMappings().getNewId(windowType);
                    if (mappedId == -1) {
                        wrapper.cancel();
                        return;
                    }

                    wrapper.write(Type.VAR_INT, mappedId);
                });
            }
        });
    }

    public void registerSetSlot(C packetType) {
        protocol.registerClientbound(packetType, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.UNSIGNED_BYTE); // Window id
                map(Type.SHORT); // Slot id
                handler(wrapper -> handleClientboundItem(wrapper));
            }
        });
    }

    public void registerSetSlot1_17_1(C packetType) {
        protocol.registerClientbound(packetType, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.UNSIGNED_BYTE); // Window id
                map(Type.VAR_INT); // State id
                map(Type.SHORT); // Slot id
                handler(wrapper -> handleClientboundItem(wrapper));
            }
        });
    }

    // Sub 1.16
    public void registerEntityEquipment(C packetType) {
        protocol.registerClientbound(packetType, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT); // Entity ID
                map(Type.VAR_INT); // Slot ID
                handler(wrapper -> handleClientboundItem(wrapper));
            }
        });
    }

    // 1.16+
    public void registerEntityEquipmentArray(C packetType) {
        protocol.registerClientbound(packetType, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT); // 0 - Entity ID

                handler(wrapper -> {
                    byte slot;
                    do {
                        slot = wrapper.passthrough(Type.BYTE);
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
                map(Type.SHORT); // 0 - Slot
                handler(wrapper -> handleServerboundItem(wrapper));
            }
        });
    }

    public void registerClickWindow(S packetType) {
        protocol.registerServerbound(packetType, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.UNSIGNED_BYTE); // 0 - Window ID
                map(Type.SHORT); // 1 - Slot
                map(Type.BYTE); // 2 - Button
                map(Type.SHORT); // 3 - Action number
                map(Type.VAR_INT); // 4 - Mode
                handler(wrapper -> handleServerboundItem(wrapper));
            }
        });
    }

    public void registerClickWindow1_17_1(S packetType) {
        protocol.registerServerbound(packetType, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.UNSIGNED_BYTE); // Window Id
                map(Type.VAR_INT); // State id
                map(Type.SHORT); // Slot
                map(Type.BYTE); // Button
                map(Type.VAR_INT); // Mode

                handler(wrapper -> {
                    // Affected items
                    int length = wrapper.passthrough(Type.VAR_INT);
                    for (int i = 0; i < length; i++) {
                        wrapper.passthrough(Type.SHORT); // Slot
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
            int itemId = wrapper.read(Type.VAR_INT);
            wrapper.write(Type.VAR_INT, protocol.getMappingData().getNewItemId(itemId));
        });
    }

    // 1.14.4+
    public void registerTradeList(C packetType) {
        protocol.registerClientbound(packetType, wrapper -> {
            wrapper.passthrough(Type.VAR_INT);
            int size = wrapper.passthrough(Type.UNSIGNED_BYTE);
            for (int i = 0; i < size; i++) {
                handleClientboundItem(wrapper); // Input
                handleClientboundItem(wrapper); // Output

                if (wrapper.passthrough(Type.BOOLEAN)) { // Has second item
                    handleClientboundItem(wrapper); // Second item
                }

                wrapper.passthrough(Type.BOOLEAN); // Trade disabled
                wrapper.passthrough(Type.INT); // Number of tools uses
                wrapper.passthrough(Type.INT); // Maximum number of trade uses

                wrapper.passthrough(Type.INT); // XP
                wrapper.passthrough(Type.INT); // Special price
                wrapper.passthrough(Type.FLOAT); // Price multiplier
                wrapper.passthrough(Type.INT); // Demand
            }
            //...
        });
    }

    public void registerTradeList1_19(C packetType) {
        protocol.registerClientbound(packetType, wrapper -> {
            wrapper.passthrough(Type.VAR_INT); // Container id
            int size = wrapper.passthrough(Type.VAR_INT);
            for (int i = 0; i < size; i++) {
                handleClientboundItem(wrapper); // Input
                handleClientboundItem(wrapper); // Output
                handleClientboundItem(wrapper); // Second item

                wrapper.passthrough(Type.BOOLEAN); // Trade disabled
                wrapper.passthrough(Type.INT); // Number of tools uses
                wrapper.passthrough(Type.INT); // Maximum number of trade uses

                wrapper.passthrough(Type.INT); // XP
                wrapper.passthrough(Type.INT); // Special price
                wrapper.passthrough(Type.FLOAT); // Price multiplier
                wrapper.passthrough(Type.INT); // Demand
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
            wrapper.passthrough(Type.VAR_INT); // Container id
            int size = wrapper.passthrough(Type.VAR_INT);
            for (int i = 0; i < size; i++) {
                final Item input = wrapper.read(costType);
                wrapper.write(mappedCostType, handleItemToClient(wrapper.user(), input));

                handleClientboundItem(wrapper); // Result

                final Item secondInput = wrapper.read(optionalCostType);
                wrapper.write(mappedOptionalCostType, handleItemToClient(wrapper.user(), secondInput));

                wrapper.passthrough(Type.BOOLEAN); // Out of stock
                wrapper.passthrough(Type.INT); // Number of trade uses
                wrapper.passthrough(Type.INT); // Maximum number of trade uses

                wrapper.passthrough(Type.INT); // XP
                wrapper.passthrough(Type.INT); // Special price
                wrapper.passthrough(Type.FLOAT); // Price multiplier
                wrapper.passthrough(Type.INT); // Demand
            }
        });
    }

    public void registerAdvancements(C packetType) {
        protocol.registerClientbound(packetType, wrapper -> {
            wrapper.passthrough(Type.BOOLEAN); // Reset/clear
            int size = wrapper.passthrough(Type.VAR_INT); // Mapping size
            for (int i = 0; i < size; i++) {
                wrapper.passthrough(Type.STRING); // Identifier
                wrapper.passthrough(Type.OPTIONAL_STRING); // Parent

                // Display data
                if (wrapper.passthrough(Type.BOOLEAN)) {
                    wrapper.passthrough(Type.COMPONENT); // Title
                    wrapper.passthrough(Type.COMPONENT); // Description
                    handleClientboundItem(wrapper); // Icon
                    wrapper.passthrough(Type.VAR_INT); // Frame type
                    int flags = wrapper.passthrough(Type.INT); // Flags
                    if ((flags & 1) != 0) {
                        wrapper.passthrough(Type.STRING); // Background texture
                    }
                    wrapper.passthrough(Type.FLOAT); // X
                    wrapper.passthrough(Type.FLOAT); // Y
                }

                wrapper.passthrough(Type.STRING_ARRAY); // Criteria

                int arrayLength = wrapper.passthrough(Type.VAR_INT);
                for (int array = 0; array < arrayLength; array++) {
                    wrapper.passthrough(Type.STRING_ARRAY); // String array
                }
            }
        });
    }

    public void registerAdvancements1_20_2(C packetType) {
        registerAdvancements1_20_2(packetType, Type.COMPONENT);
    }

    public void registerAdvancements1_20_3(C packetType) {
        registerAdvancements1_20_2(packetType, Type.TAG);
    }

    private void registerAdvancements1_20_2(C packetType, Type<?> componentType) {
        protocol.registerClientbound(packetType, wrapper -> {
            wrapper.passthrough(Type.BOOLEAN); // Reset/clear
            int size = wrapper.passthrough(Type.VAR_INT); // Mapping size
            for (int i = 0; i < size; i++) {
                wrapper.passthrough(Type.STRING); // Identifier
                wrapper.passthrough(Type.OPTIONAL_STRING); // Parent

                // Display data
                if (wrapper.passthrough(Type.BOOLEAN)) {
                    wrapper.passthrough(componentType); // Title
                    wrapper.passthrough(componentType); // Description
                    handleClientboundItem(wrapper); // Icon
                    wrapper.passthrough(Type.VAR_INT); // Frame type
                    int flags = wrapper.passthrough(Type.INT); // Flags
                    if ((flags & 1) != 0) {
                        wrapper.passthrough(Type.STRING); // Background texture
                    }
                    wrapper.passthrough(Type.FLOAT); // X
                    wrapper.passthrough(Type.FLOAT); // Y
                }

                int requirements = wrapper.passthrough(Type.VAR_INT);
                for (int array = 0; array < requirements; array++) {
                    wrapper.passthrough(Type.STRING_ARRAY);
                }

                wrapper.passthrough(Type.BOOLEAN); // Send telemetry
            }
        });
    }

    public void registerWindowPropertyEnchantmentHandler(C packetType) {
        protocol.registerClientbound(packetType, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.UNSIGNED_BYTE); // Container id
                handler(wrapper -> {
                    Mappings mappings = protocol.getMappingData().getEnchantmentMappings();
                    if (mappings == null) {
                        return;
                    }

                    short property = wrapper.passthrough(Type.SHORT);
                    if (property >= 4 && property <= 6) { // Enchantment id
                        short enchantmentId = (short) mappings.getNewId(wrapper.read(Type.SHORT));
                        wrapper.write(Type.SHORT, enchantmentId);
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
                map(Type.INT); // 0 - Particle ID
                map(Type.BOOLEAN); // 1 - Long Distance
                map(coordType); // 2 - X
                map(coordType); // 3 - Y
                map(coordType); // 4 - Z
                map(Type.FLOAT); // 5 - Offset X
                map(Type.FLOAT); // 6 - Offset Y
                map(Type.FLOAT); // 7 - Offset Z
                map(Type.FLOAT); // 8 - Particle Data
                map(Type.INT); // 9 - Particle Count
                handler(getSpawnParticleHandler());
            }
        });
    }

    public void registerSpawnParticle1_19(C packetType) {
        protocol.registerClientbound(packetType, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT); // 0 - Particle ID
                map(Type.BOOLEAN); // 1 - Long Distance
                map(Type.DOUBLE); // 2 - X
                map(Type.DOUBLE); // 3 - Y
                map(Type.DOUBLE); // 4 - Z
                map(Type.FLOAT); // 5 - Offset X
                map(Type.FLOAT); // 6 - Offset Y
                map(Type.FLOAT); // 7 - Offset Z
                map(Type.FLOAT); // 8 - Particle Data
                map(Type.INT); // 9 - Particle Count
                handler(getSpawnParticleHandler(Type.VAR_INT));
            }
        });
    }

    public void registerSpawnParticle1_20_5(C packetType, Type<Particle> unmappedParticleType, Type<Particle> mappedParticleType) {
        protocol.registerClientbound(packetType, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.BOOLEAN); // Long Distance
                map(Type.DOUBLE); // X
                map(Type.DOUBLE); // Y
                map(Type.DOUBLE); // Z
                map(Type.FLOAT); // Offset X
                map(Type.FLOAT); // Offset Y
                map(Type.FLOAT); // Offset Z
                map(Type.FLOAT); // Particle Data
                map(Type.INT); // Particle Count
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
            wrapper.passthrough(Type.DOUBLE); // X
            wrapper.passthrough(Type.DOUBLE); // Y
            wrapper.passthrough(Type.DOUBLE); // Z
            wrapper.passthrough(Type.FLOAT); // Power
            final int blocks = wrapper.passthrough(Type.VAR_INT);
            for (int i = 0; i < blocks; i++) {
                wrapper.passthrough(Type.BYTE); // Relative X
                wrapper.passthrough(Type.BYTE); // Relative Y
                wrapper.passthrough(Type.BYTE); // Relative Z
            }
            wrapper.passthrough(Type.FLOAT); // Knockback X
            wrapper.passthrough(Type.FLOAT); // Knockback Y
            wrapper.passthrough(Type.FLOAT); // Knockback Z
            wrapper.passthrough(Type.VAR_INT); // Block interaction type

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
        return getSpawnParticleHandler(Type.INT);
    }

    public PacketHandler getSpawnParticleHandler(Type<Integer> idType) {
        return wrapper -> {
            int id = wrapper.get(idType, 0);
            if (id == -1) {
                return;
            }

            ParticleMappings mappings = protocol.getMappingData().getParticleMappings();
            if (mappings.isBlockParticle(id)) {
                int data = wrapper.read(Type.VAR_INT);
                wrapper.write(Type.VAR_INT, protocol.getMappingData().getNewBlockStateId(data));
            } else if (mappings.isItemParticle(id)) {
                handleClientboundItem(wrapper);
            }

            int mappedId = protocol.getMappingData().getNewParticleId(id);
            if (mappedId != id) {
                wrapper.set(idType, 0, mappedId);
            }
        };
    }

    private void handleClientboundItem(final PacketWrapper wrapper) throws Exception {
        final Item item = handleItemToClient(wrapper.user(), wrapper.read(itemType));
        wrapper.write(mappedItemType, item);
    }

    private void handleServerboundItem(final PacketWrapper wrapper) throws Exception {
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

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
package com.viaversion.viaversion.protocols.protocol1_9to1_8.packets;

import com.google.common.collect.ImmutableList;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.metadata.Metadata;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.protocol.remapper.ValueTransformer;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.version.Types1_8;
import com.viaversion.viaversion.api.type.types.version.Types1_9;
import com.viaversion.viaversion.protocols.protocol1_8.ClientboundPackets1_8;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.ClientboundPackets1_9;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.ItemRewriter;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.Protocol1_9To1_8;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.ServerboundPackets1_9;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.metadata.MetadataRewriter1_9To1_8;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.storage.EntityTracker1_9;
import com.viaversion.viaversion.util.Pair;
import com.viaversion.viaversion.util.Triple;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EntityPackets {
    public static final ValueTransformer<Byte, Short> toNewShort = new ValueTransformer<Byte, Short>(Type.SHORT) {
        @Override
        public Short transform(PacketWrapper wrapper, Byte inputValue) {
            return (short) (inputValue * 128);
        }
    };

    public static void register(Protocol1_9To1_8 protocol) {
        // Attach Entity Packet
        protocol.registerClientbound(ClientboundPackets1_8.ATTACH_ENTITY, new PacketHandlers() {

            @Override
            public void register() {
                map(Type.INT); // 0 - Entity ID
                map(Type.INT); // 1 - Vehicle

                handler(wrapper -> {
                    final short leashState = wrapper.read(Type.UNSIGNED_BYTE);
                    EntityTracker1_9 tracker = wrapper.user().getEntityTracker(Protocol1_9To1_8.class);
                    if (leashState == 0) {
                        int passenger = wrapper.get(Type.INT, 0);
                        int vehicle = wrapper.get(Type.INT, 1);

                        wrapper.cancel(); // Don't send current packet

                        PacketWrapper passengerPacket = wrapper.create(ClientboundPackets1_9.SET_PASSENGERS);
                        if (vehicle == -1) {
                            if (!tracker.getVehicleMap().containsKey(passenger)) {
                                return; // Cancel
                            }

                            passengerPacket.write(Type.VAR_INT, tracker.getVehicleMap().remove(passenger));
                            passengerPacket.write(Type.VAR_INT_ARRAY_PRIMITIVE, new int[]{});
                        } else {
                            passengerPacket.write(Type.VAR_INT, vehicle);
                            passengerPacket.write(Type.VAR_INT_ARRAY_PRIMITIVE, new int[]{passenger});
                            tracker.getVehicleMap().put(passenger, vehicle);
                        }
                        passengerPacket.send(Protocol1_9To1_8.class); // Send the packet
                    }
                });
            }
        });
        protocol.registerClientbound(ClientboundPackets1_8.ENTITY_TELEPORT, new PacketHandlers() {

            @Override
            public void register() {
                map(Type.VAR_INT); // 0 - Entity ID
                map(Type.INT, SpawnPackets.toNewDouble); // 1 - X - Needs to be divided by 32
                map(Type.INT, SpawnPackets.toNewDouble); // 2 - Y - Needs to be divided by 32
                map(Type.INT, SpawnPackets.toNewDouble); // 3 - Z - Needs to be divided by 32

                map(Type.BYTE); // 4 - Pitch
                map(Type.BYTE); // 5 - Yaw

                map(Type.BOOLEAN); // 6 - On Ground

                handler(wrapper -> {
                    int entityID = wrapper.get(Type.VAR_INT, 0);
                    if (Via.getConfig().isHologramPatch()) {
                        EntityTracker1_9 tracker = wrapper.user().getEntityTracker(Protocol1_9To1_8.class);
                        if (tracker.getKnownHolograms().contains(entityID)) {
                            Double newValue = wrapper.get(Type.DOUBLE, 1);
                            newValue += (Via.getConfig().getHologramYOffset());
                            wrapper.set(Type.DOUBLE, 1, newValue);
                        }
                    }
                });


            }
        });
        protocol.registerClientbound(ClientboundPackets1_8.ENTITY_POSITION_AND_ROTATION, new PacketHandlers() {

            @Override
            public void register() {
                map(Type.VAR_INT); // 0 - Entity ID
                map(Type.BYTE, toNewShort); // 1 - X
                map(Type.BYTE, toNewShort); // 2 - Y
                map(Type.BYTE, toNewShort); // 3 - Z

                map(Type.BYTE); // 4 - Yaw
                map(Type.BYTE); // 5 - Pitch

                map(Type.BOOLEAN); // 6 - On Ground
            }
        });
        protocol.registerClientbound(ClientboundPackets1_8.ENTITY_POSITION, new PacketHandlers() {

            @Override
            public void register() {
                map(Type.VAR_INT); // 0 - Entity ID
                map(Type.BYTE, toNewShort); // 1 - X
                map(Type.BYTE, toNewShort); // 2 - Y
                map(Type.BYTE, toNewShort); // 3 - Z

                map(Type.BOOLEAN); // 4 - On Ground
            }
        });
        protocol.registerClientbound(ClientboundPackets1_8.ENTITY_EQUIPMENT, new PacketHandlers() {

            @Override
            public void register() {
                map(Type.VAR_INT); // 0 - Entity ID
                // 1 - Slot ID
                map(Type.SHORT, new ValueTransformer<Short, Integer>(Type.VAR_INT) {
                    @Override
                    public Integer transform(PacketWrapper wrapper, Short slot) throws Exception {
                        int entityId = wrapper.get(Type.VAR_INT, 0);
                        int receiverId = wrapper.user().getEntityTracker(Protocol1_9To1_8.class).clientEntityId();

                        // Cancel invalid slots as they would cause a packet read error in 1.9
                        // 1.8 handled invalid slots gracefully, but 1.9 does not
                        if (slot < 0 || slot > 4 || (entityId == receiverId && slot > 3)) {
                            wrapper.cancel();
                            return 0;
                        }

                        // Normally, 0 = hand and 1-4 = armor
                        // ... but if the sent id is equal to the receiver's id, 0-3 will instead mark the armor slots
                        // (In 1.9+, every client treats the received the same: 0=hand, 1=offhand, 2-5=armor)
                        if (entityId == receiverId) {
                            return slot.intValue() + 2;
                        }
                        return slot > 0 ? slot.intValue() + 1 : slot.intValue();
                    }
                });
                map(Type.ITEM1_8); // 2 - Item
                // Item Rewriter
                handler(wrapper -> {
                    Item stack = wrapper.get(Type.ITEM1_8, 0);
                    ItemRewriter.toClient(stack);
                });
                // Blocking
                handler(wrapper -> {
                    EntityTracker1_9 entityTracker = wrapper.user().getEntityTracker(Protocol1_9To1_8.class);
                    int entityID = wrapper.get(Type.VAR_INT, 0);
                    Item stack = wrapper.get(Type.ITEM1_8, 0);

                    if (stack != null && Protocol1_9To1_8.isSword(stack.identifier())) {
                        entityTracker.getValidBlocking().add(entityID);
                        return;
                    }

                    entityTracker.getValidBlocking().remove(entityID);
                });
            }
        });
        protocol.registerClientbound(ClientboundPackets1_8.ENTITY_METADATA, new PacketHandlers() {

            @Override
            public void register() {
                map(Type.VAR_INT); // 0 - Entity ID
                map(Types1_8.METADATA_LIST, Types1_9.METADATA_LIST); // 1 - Metadata List
                handler(wrapper -> {
                    List<Metadata> metadataList = wrapper.get(Types1_9.METADATA_LIST, 0);
                    int entityId = wrapper.get(Type.VAR_INT, 0);
                    EntityTracker1_9 tracker = wrapper.user().getEntityTracker(Protocol1_9To1_8.class);
                    if (tracker.hasEntity(entityId)) {
                        protocol.get(MetadataRewriter1_9To1_8.class).handleMetadata(entityId, metadataList, wrapper.user());
                    } else {
                        wrapper.cancel();
                    }
                });

                // Handler for meta data
                handler(wrapper -> {
                    List<Metadata> metadataList = wrapper.get(Types1_9.METADATA_LIST, 0);
                    int entityID = wrapper.get(Type.VAR_INT, 0);
                    EntityTracker1_9 tracker = wrapper.user().getEntityTracker(Protocol1_9To1_8.class);
                    tracker.handleMetadata(entityID, metadataList);
                });

                // Cancel packet if list empty
                handler(wrapper -> {
                    List<Metadata> metadataList = wrapper.get(Types1_9.METADATA_LIST, 0);
                    if (metadataList.isEmpty()) {
                        wrapper.cancel();
                    }
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_8.ENTITY_EFFECT, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT); // 0 - Entity ID
                map(Type.BYTE); // 1 - Effect ID
                map(Type.BYTE); // 2 - Amplifier
                map(Type.VAR_INT); // 3 - Duration
                //Handle effect indicator
                handler(wrapper -> {
                    boolean showParticles = wrapper.read(Type.BOOLEAN); //In 1.8 = true->Show particles : false->Hide particles
                    boolean newEffect = Via.getConfig().isNewEffectIndicator();
                    //0: hide, 1: shown without indictator, 2: shown with indicator, 3: hide with beacon indicator, but we don't use it.
                    wrapper.write(Type.BYTE, (byte) (showParticles ? newEffect ? 2 : 1 : 0));
                });
            }
        });

        protocol.cancelClientbound(ClientboundPackets1_8.UPDATE_ENTITY_NBT);

        protocol.registerClientbound(ClientboundPackets1_8.COMBAT_EVENT, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT); //Event id
                handler(wrapper -> {
                    if (wrapper.get(Type.VAR_INT, 0) == 2) { // entity dead
                        wrapper.passthrough(Type.VAR_INT); //Player id
                        wrapper.passthrough(Type.INT); //Entity id
                        Protocol1_9To1_8.STRING_TO_JSON.write(wrapper, wrapper.read(Type.STRING));
                    }
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_8.ENTITY_PROPERTIES, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT);
                handler(wrapper -> {
                    if (!Via.getConfig().isMinimizeCooldown()) return;

                    EntityTracker1_9 tracker = wrapper.user().getEntityTracker(Protocol1_9To1_8.class);
                    if (wrapper.get(Type.VAR_INT, 0) != tracker.getProvidedEntityId()) {
                        return;
                    }
                    int propertiesToRead = wrapper.read(Type.INT);
                    Map<String, Pair<Double, List<Triple<UUID, Double, Byte>>>> properties = new HashMap<>(propertiesToRead);
                    for (int i = 0; i < propertiesToRead; i++) {
                        String key = wrapper.read(Type.STRING);
                        Double value = wrapper.read(Type.DOUBLE);
                        int modifiersToRead = wrapper.read(Type.VAR_INT);
                        List<Triple<UUID, Double, Byte>> modifiers = new ArrayList<>(modifiersToRead);
                        for (int j = 0; j < modifiersToRead; j++) {
                            modifiers.add(
                                    new Triple<>(
                                            wrapper.read(Type.UUID),
                                            wrapper.read(Type.DOUBLE), // Amount
                                            wrapper.read(Type.BYTE) // Operation
                                    )
                            );
                        }
                        properties.put(key, new Pair<>(value, modifiers));
                    }

                    // == Why 15.9? ==
                    // Higher values hides the cooldown but it bugs visual animation on hand
                    // when removing item from hand with inventory gui
                    properties.put("generic.attackSpeed", new Pair<>(15.9, ImmutableList.of( // Neutralize modifiers
                            new Triple<>(UUID.fromString("FA233E1C-4180-4865-B01B-BCCE9785ACA3"), 0.0, (byte) 0), // Tool and weapon modifier
                            new Triple<>(UUID.fromString("AF8B6E3F-3328-4C0A-AA36-5BA2BB9DBEF3"), 0.0, (byte) 2), // Dig speed
                            new Triple<>(UUID.fromString("55FCED67-E92A-486E-9800-B47F202C4386"), 0.0, (byte) 2) // Dig slow down
                    )));

                    wrapper.write(Type.INT, properties.size());
                    for (Map.Entry<String, Pair<Double, List<Triple<UUID, Double, Byte>>>> entry : properties.entrySet()) {
                        wrapper.write(Type.STRING, entry.getKey()); // Key
                        wrapper.write(Type.DOUBLE, entry.getValue().key()); // Value
                        wrapper.write(Type.VAR_INT, entry.getValue().value().size());
                        for (Triple<UUID, Double, Byte> modifier : entry.getValue().value()) {
                            wrapper.write(Type.UUID, modifier.first());
                            wrapper.write(Type.DOUBLE, modifier.second()); // Amount
                            wrapper.write(Type.BYTE, modifier.third()); // Operation
                        }
                    }
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_8.ENTITY_ANIMATION, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT); // 0 - Entity ID
                map(Type.UNSIGNED_BYTE); // 1 - Animation
                handler(wrapper -> {
                    if (wrapper.get(Type.UNSIGNED_BYTE, 0) == 3) {
                        wrapper.cancel();
                    }
                });
            }
        });


        /* Incoming Packets */
        protocol.registerServerbound(ServerboundPackets1_9.ENTITY_ACTION, new PacketHandlers() {

            @Override
            public void register() {
                map(Type.VAR_INT); // 0 - Player ID
                map(Type.VAR_INT); // 1 - Action
                map(Type.VAR_INT); // 2 - Jump
                handler(wrapper -> {
                    int action = wrapper.get(Type.VAR_INT, 1);
                    if (action == 6 || action == 8)
                        wrapper.cancel();
                    if (action == 7) {
                        wrapper.set(Type.VAR_INT, 1, 6);
                    }
                });
            }
        });

        protocol.registerServerbound(ServerboundPackets1_9.INTERACT_ENTITY, new PacketHandlers() {

            @Override
            public void register() {
                map(Type.VAR_INT); // 0 - Entity ID (Target)
                map(Type.VAR_INT); // 1 - Action Type

                // Cancel second hand to prevent double interact
                handler(wrapper -> {
                    int type = wrapper.get(Type.VAR_INT, 1);
                    if (type == 2) {
                        wrapper.passthrough(Type.FLOAT); // 2 - X
                        wrapper.passthrough(Type.FLOAT); // 3 - Y
                        wrapper.passthrough(Type.FLOAT); // 4 - Z
                    }
                    if (type == 0 || type == 2) {
                        int hand = wrapper.read(Type.VAR_INT); // 2/5 - Hand

                        if (hand == 1)
                            wrapper.cancel();
                    }
                });
            }
        });
    }
}

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
package com.viaversion.viaversion.protocols.v1_10to1_11.rewriter;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_11;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_11.EntityType;
import com.viaversion.viaversion.api.minecraft.item.DataItem;
import com.viaversion.viaversion.api.minecraft.metadata.Metadata;
import com.viaversion.viaversion.api.minecraft.metadata.types.MetaType1_9;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.Types1_9;
import com.viaversion.viaversion.protocols.v1_10to1_11.Protocol1_10To1_11;
import com.viaversion.viaversion.protocols.v1_10to1_11.data.BlockEntityNames1_11;
import com.viaversion.viaversion.protocols.v1_10to1_11.data.EntityNames1_11;
import com.viaversion.viaversion.protocols.v1_10to1_11.storage.EntityTracker1_11;
import com.viaversion.viaversion.protocols.v1_9_1to1_9_3.packet.ClientboundPackets1_9_3;
import com.viaversion.viaversion.rewriter.EntityRewriter;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

public class EntityPacketRewriter1_11 extends EntityRewriter<ClientboundPackets1_9_3, Protocol1_10To1_11> {

    public EntityPacketRewriter1_11(Protocol1_10To1_11 protocol) {
        super(protocol);
    }

    @Override
    protected void registerPackets() {
        protocol.registerClientbound(ClientboundPackets1_9_3.ADD_ENTITY, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); // 0 - Entity id
                map(Types.UUID); // 1 - UUID
                map(Types.BYTE); // 2 - Type

                // Track Entity
                handler(objectTrackerHandler());
            }
        });

        protocol.registerClientbound(ClientboundPackets1_9_3.ADD_MOB, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); // 0 - Entity ID
                map(Types.UUID); // 1 - Entity UUID
                map(Types.UNSIGNED_BYTE, Types.VAR_INT); // 2 - Entity Type
                map(Types.DOUBLE); // 3 - X
                map(Types.DOUBLE); // 4 - Y
                map(Types.DOUBLE); // 5 - Z
                map(Types.BYTE); // 6 - Yaw
                map(Types.BYTE); // 7 - Pitch
                map(Types.BYTE); // 8 - Head Pitch
                map(Types.SHORT); // 9 - Velocity X
                map(Types.SHORT); // 10 - Velocity Y
                map(Types.SHORT); // 11 - Velocity Z
                map(Types1_9.METADATA_LIST); // 12 - Metadata

                handler(wrapper -> {
                    int entityId = wrapper.get(Types.VAR_INT, 0);
                    // Change Type :)
                    int type = wrapper.get(Types.VAR_INT, 1);

                    EntityTypes1_11.EntityType entType = rewriteEntityType(type, wrapper.get(Types1_9.METADATA_LIST, 0));
                    if (entType != null) {
                        wrapper.set(Types.VAR_INT, 1, entType.getId());

                        // Register Type ID
                        wrapper.user().getEntityTracker(Protocol1_10To1_11.class).addEntity(entityId, entType);
                        handleMetadata(entityId, wrapper.get(Types1_9.METADATA_LIST, 0), wrapper.user());
                    }
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_9_3.TAKE_ITEM_ENTITY, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); // 0 - Collected entity id
                map(Types.VAR_INT); // 1 - Collector entity id

                handler(wrapper -> {
                    wrapper.write(Types.VAR_INT, 1); // 2 - Pickup Count
                });
            }
        });

        registerMetadataRewriter(ClientboundPackets1_9_3.SET_ENTITY_DATA, Types1_9.METADATA_LIST);

        protocol.registerClientbound(ClientboundPackets1_9_3.TELEPORT_ENTITY, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.VAR_INT); // 0 - Entity id
                map(Types.DOUBLE); // 1 - x
                map(Types.DOUBLE); // 2 - y
                map(Types.DOUBLE); // 3 - z
                map(Types.BYTE); // 4 - yaw
                map(Types.BYTE); // 5 - pitch
                map(Types.BOOLEAN); // 6 - onGround

                handler(wrapper -> {
                    int entityID = wrapper.get(Types.VAR_INT, 0);
                    if (Via.getConfig().isHologramPatch()) {
                        EntityTracker1_11 tracker = wrapper.user().getEntityTracker(Protocol1_10To1_11.class);
                        if (tracker.isHologram(entityID)) {
                            Double newValue = wrapper.get(Types.DOUBLE, 1);
                            newValue -= (Via.getConfig().getHologramYOffset());
                            wrapper.set(Types.DOUBLE, 1, newValue);
                        }
                    }
                });
            }
        });

        registerRemoveEntities(ClientboundPackets1_9_3.REMOVE_ENTITIES);

        protocol.registerClientbound(ClientboundPackets1_9_3.BLOCK_ENTITY_DATA, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.BLOCK_POSITION1_8); // 0 - Position
                map(Types.UNSIGNED_BYTE); // 1 - Action
                map(Types.NAMED_COMPOUND_TAG); // 2 - NBT data

                handler(wrapper -> {
                    CompoundTag tag = wrapper.get(Types.NAMED_COMPOUND_TAG, 0);
                    if (wrapper.get(Types.UNSIGNED_BYTE, 0) == 1) {
                        EntityNames1_11.toClientSpawner(tag);
                    }

                    StringTag idTag = tag.getStringTag("id");
                    if (idTag != null) {
                        // Handle new identifier
                        idTag.setValue(BlockEntityNames1_11.toNewIdentifier(idTag.getValue()));
                    }
                });
            }
        });
    }

    @Override
    protected void registerRewrites() {
        filter().handler((event, meta) -> {
            if (meta.getValue() instanceof DataItem) {
                // Apply rewrite
                EntityNames1_11.toClientItem(meta.value());
            }
        });

        filter().type(EntityType.GUARDIAN).index(12).handler((event, meta) -> {
            boolean value = (((byte) meta.getValue()) & 0x02) == 0x02;
            meta.setTypeAndValue(MetaType1_9.BOOLEAN, value);
        });

        filter().type(EntityType.ABSTRACT_SKELETON).removeIndex(12);

        filter().type(EntityType.ZOMBIE).handler((event, meta) -> {
            if ((event.entityType() == EntityType.ZOMBIE || event.entityType() == EntityType.HUSK) && meta.id() == 14) {
                event.cancel();
            } else if (meta.id() == 15) {
                meta.setId(14);
            }
        });

        filter().type(EntityType.ABSTRACT_HORSE).handler((event, metadata) -> {
            final com.viaversion.viaversion.api.minecraft.entities.EntityType type = event.entityType();
            int id = metadata.id();
            if (id == 14) { // Type
                event.cancel();
                return;
            }

            if (id == 16) { // Owner
                metadata.setId(14);
            } else if (id == 17) { // Armor
                metadata.setId(16);
            }

            // Process per type
            if (!type.is(EntityType.HORSE) && metadata.id() == 15 || metadata.id() == 16) {
                event.cancel();
                return;
            }

            if ((type == EntityType.DONKEY || type == EntityType.MULE) && metadata.id() == 13) {
                if ((((byte) metadata.getValue()) & 0x08) == 0x08) {
                    event.createExtraMeta(new Metadata(15, MetaType1_9.BOOLEAN, true));
                } else {
                    event.createExtraMeta(new Metadata(15, MetaType1_9.BOOLEAN, false));
                }
            }
        });

        filter().type(EntityType.ARMOR_STAND).index(0).handler((event, meta) -> {
            if (!Via.getConfig().isHologramPatch()) {
                return;
            }

            Metadata flags = event.metaAtIndex(11);
            Metadata customName = event.metaAtIndex(2);
            Metadata customNameVisible = event.metaAtIndex(3);
            if (flags == null || customName == null || customNameVisible == null) {
                return;
            }

            byte data = meta.value();
            // Check invisible | Check small | Check if custom name is empty | Check if custom name visible is true
            if ((data & 0x20) == 0x20 && ((byte) flags.getValue() & 0x01) == 0x01
                && !((String) customName.getValue()).isEmpty() && (boolean) customNameVisible.getValue()) {
                EntityTracker1_11 tracker = tracker(event.user());
                int entityId = event.entityId();
                if (tracker.addHologram(entityId)) {
                    // Send movement
                    PacketWrapper wrapper = PacketWrapper.create(ClientboundPackets1_9_3.MOVE_ENTITY_POS, null, event.user());
                    wrapper.write(Types.VAR_INT, entityId);
                    wrapper.write(Types.SHORT, (short) 0);
                    wrapper.write(Types.SHORT, (short) (128D * (-Via.getConfig().getHologramYOffset() * 32D)));
                    wrapper.write(Types.SHORT, (short) 0);
                    wrapper.write(Types.BOOLEAN, true);

                    wrapper.send(Protocol1_10To1_11.class);
                }
            }
        });
    }

    @Override
    public com.viaversion.viaversion.api.minecraft.entities.EntityType typeFromId(int type) {
        return EntityTypes1_11.getTypeFromId(type, false);
    }

    @Override
    public com.viaversion.viaversion.api.minecraft.entities.EntityType objectTypeFromId(int type) {
        return EntityTypes1_11.getTypeFromId(type, true);
    }

    public EntityType rewriteEntityType(int numType, List<Metadata> metadata) {
        Optional<EntityType> optType = EntityType.findById(numType);
        if (optType.isEmpty()) {
            Via.getManager().getPlatform().getLogger().severe("Error: could not find Entity type " + numType + " with metadata: " + metadata);
            return null;
        }

        EntityType type = optType.get();

        try {
            if (type.is(EntityType.GUARDIAN)) {
                // ElderGuardian - 4
                Optional<Metadata> options = getById(metadata, 12);
                if (options.isPresent()) {
                    if ((((byte) options.get().getValue()) & 0x04) == 0x04) {
                        return EntityType.ELDER_GUARDIAN;
                    }
                }
            }
            if (type.is(EntityType.SKELETON)) {
                // WitherSkeleton - 5
                // Stray - 6
                Optional<Metadata> options = getById(metadata, 12);
                if (options.isPresent()) {
                    if (((int) options.get().getValue()) == 1) {
                        return EntityType.WITHER_SKELETON;
                    }
                    if (((int) options.get().getValue()) == 2) {
                        return EntityType.STRAY;
                    }
                }
            }
            if (type.is(EntityType.ZOMBIE)) {
                // ZombieVillager - 27
                // Husk - 23
                Optional<Metadata> options = getById(metadata, 13);
                if (options.isPresent()) {
                    int value = (int) options.get().getValue();
                    if (value > 0 && value < 6) {
                        metadata.add(new Metadata(16, MetaType1_9.VAR_INT, value - 1)); // Add profession type to new metadata
                        return EntityType.ZOMBIE_VILLAGER;
                    }
                    if (value == 6) {
                        return EntityType.HUSK;
                    }
                }
            }
            if (type.is(EntityType.HORSE)) {
                // SkeletonHorse - 28
                // ZombieHorse - 29
                // Donkey - 31
                // Mule - 32
                Optional<Metadata> options = getById(metadata, 14);
                if (options.isPresent()) {
                    if (((int) options.get().getValue()) == 0) {
                        return EntityType.HORSE;
                    }
                    if (((int) options.get().getValue()) == 1) {
                        return EntityType.DONKEY;
                    }
                    if (((int) options.get().getValue()) == 2) {
                        return EntityType.MULE;
                    }
                    if (((int) options.get().getValue()) == 3) {
                        return EntityType.ZOMBIE_HORSE;
                    }
                    if (((int) options.get().getValue()) == 4) {
                        return EntityType.SKELETON_HORSE;
                    }
                }
            }
        } catch (Exception e) {
            if (!Via.getConfig().isSuppressMetadataErrors() || Via.getManager().isDebug()) {
                protocol.getLogger().warning("An error occurred with entity type rewriter");
                protocol.getLogger().warning("Metadata: " + metadata);
                protocol.getLogger().log(Level.WARNING, "Error: ", e);
            }
        }

        return type;
    }

    public Optional<Metadata> getById(List<Metadata> metadatas, int id) {
        for (Metadata metadata : metadatas) {
            if (metadata.id() == id) return Optional.of(metadata);
        }
        return Optional.empty();
    }
}

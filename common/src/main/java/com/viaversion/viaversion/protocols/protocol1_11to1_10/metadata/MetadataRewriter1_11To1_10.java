/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2021 ViaVersion and contributors
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
package com.viaversion.viaversion.protocols.protocol1_11to1_10.metadata;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.entities.Entity1_11Types;
import com.viaversion.viaversion.api.minecraft.entities.Entity1_11Types.EntityType;
import com.viaversion.viaversion.api.minecraft.item.DataItem;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.metadata.Metadata;
import com.viaversion.viaversion.api.minecraft.metadata.types.MetaType1_9;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_11to1_10.EntityIdRewriter;
import com.viaversion.viaversion.protocols.protocol1_11to1_10.Protocol1_11To1_10;
import com.viaversion.viaversion.protocols.protocol1_11to1_10.storage.EntityTracker1_11;
import com.viaversion.viaversion.rewriter.EntityRewriter;

import java.util.List;
import java.util.Optional;

public class MetadataRewriter1_11To1_10 extends EntityRewriter<Protocol1_11To1_10> {

    public MetadataRewriter1_11To1_10(Protocol1_11To1_10 protocol) {
        super(protocol);
    }

    @Override
    protected void handleMetadata(int entityId, com.viaversion.viaversion.api.minecraft.entities.EntityType type, Metadata metadata, List<Metadata> metadatas, UserConnection connection) {
        if (metadata.getValue() instanceof DataItem) {
            // Apply rewrite
            EntityIdRewriter.toClientItem((Item) metadata.getValue());
        }

        if (type == null) return;
        if (type.is(EntityType.ELDER_GUARDIAN) || type.is(EntityType.GUARDIAN)) { // Guardians
            int oldid = metadata.id();
            if (oldid == 12) {
                boolean val = (((byte) metadata.getValue()) & 0x02) == 0x02;
                metadata.setTypeAndValue(MetaType1_9.Boolean, val);
            }
        }

        if (type.isOrHasParent(EntityType.ABSTRACT_SKELETON)) { // Skeletons
            int oldid = metadata.id();
            if (oldid == 12) {
                metadatas.remove(metadata);
            }
            if (oldid == 13) {
                metadata.setId(12);
            }
        }

        if (type.isOrHasParent(EntityType.ZOMBIE)) { // Zombie | Zombie Villager | Husk
            if (type.is(EntityType.ZOMBIE, EntityType.HUSK) && metadata.id() == 14) {
                metadatas.remove(metadata);
            } else {
                if (metadata.id() == 15) {
                    metadata.setId(14);
                } else {
                    if (metadata.id() == 14) {
                        metadata.setId(15);
                    }
                }
            }
        }

        if (type.isOrHasParent(EntityType.ABSTRACT_HORSE)) { // Horses
            // Remap metadata id
            int oldid = metadata.id();
            if (oldid == 14) { // Type
                metadatas.remove(metadata);
            }
            if (oldid == 16) { // Owner
                metadata.setId(14);
            }
            if (oldid == 17) { // Armor
                metadata.setId(16);
            }

            // Process per type
            if (type.is(EntityType.HORSE)) {
                // Normal Horse
            } else {
                // Remove 15, 16
                if (metadata.id() == 15 || metadata.id() == 16) {
                    metadatas.remove(metadata);
                }
            }
            if (type.is(EntityType.DONKEY, EntityType.MULE)) {
                // Chested Horse
                if (metadata.id() == 13) {
                    if ((((byte) metadata.getValue()) & 0x08) == 0x08) {
                        metadatas.add(new Metadata(15, MetaType1_9.Boolean, true));
                    } else {
                        metadatas.add(new Metadata(15, MetaType1_9.Boolean, false));
                    }
                }
            }
        }

        if (type.is(EntityType.ARMOR_STAND) && Via.getConfig().isHologramPatch()) {
            Metadata flags = metaByIndex(11, metadatas);
            Metadata customName = metaByIndex(2, metadatas);
            Metadata customNameVisible = metaByIndex(3, metadatas);
            if (metadata.id() == 0 && flags != null && customName != null && customNameVisible != null) {
                byte data = (byte) metadata.getValue();
                // Check invisible | Check small | Check if custom name is empty | Check if custom name visible is true
                if ((data & 0x20) == 0x20 && ((byte) flags.getValue() & 0x01) == 0x01
                        && !((String) customName.getValue()).isEmpty() && (boolean) customNameVisible.getValue()) {
                    EntityTracker1_11 tracker = tracker(connection);
                    if (!tracker.isHologram(entityId)) {
                        tracker.addHologram(entityId);
                        try {
                            // Send movement
                            PacketWrapper wrapper = PacketWrapper.create(0x25, null, connection);
                            wrapper.write(Type.VAR_INT, entityId);
                            wrapper.write(Type.SHORT, (short) 0);
                            wrapper.write(Type.SHORT, (short) (128D * (-Via.getConfig().getHologramYOffset() * 32D)));
                            wrapper.write(Type.SHORT, (short) 0);
                            wrapper.write(Type.BOOLEAN, true);

                            wrapper.send(Protocol1_11To1_10.class);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    @Override
    public com.viaversion.viaversion.api.minecraft.entities.EntityType typeFromId(int type) {
        return Entity1_11Types.getTypeFromId(type, false);
    }

    @Override
    public com.viaversion.viaversion.api.minecraft.entities.EntityType objectTypeFromId(int type) {
        return Entity1_11Types.getTypeFromId(type, true);
    }

    public static EntityType rewriteEntityType(int numType, List<Metadata> metadata) {
        Optional<EntityType> optType = EntityType.findById(numType);
        if (!optType.isPresent()) {
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
                        metadata.add(new Metadata(16, MetaType1_9.VarInt, value - 1)); // Add profession type to new metadata
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
                Via.getPlatform().getLogger().warning("An error occurred with entity type rewriter");
                Via.getPlatform().getLogger().warning("Metadata: " + metadata);
                e.printStackTrace();
            }
        }

        return type;
    }

    public static Optional<Metadata> getById(List<Metadata> metadatas, int id) {
        for (Metadata metadata : metadatas) {
            if (metadata.id() == id) return Optional.of(metadata);
        }
        return Optional.empty();
    }
}

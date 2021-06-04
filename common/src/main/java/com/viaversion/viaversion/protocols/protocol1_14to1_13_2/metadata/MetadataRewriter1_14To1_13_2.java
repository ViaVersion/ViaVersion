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
package com.viaversion.viaversion.protocols.protocol1_14to1_13_2.metadata;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.VillagerData;
import com.viaversion.viaversion.api.minecraft.entities.Entity1_13Types;
import com.viaversion.viaversion.api.minecraft.entities.Entity1_14Types;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.item.DataItem;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.metadata.Metadata;
import com.viaversion.viaversion.api.minecraft.metadata.types.MetaType1_14;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.Particle;
import com.viaversion.viaversion.protocols.protocol1_14to1_13_2.Protocol1_14To1_13_2;
import com.viaversion.viaversion.protocols.protocol1_14to1_13_2.storage.EntityTracker1_14;
import com.viaversion.viaversion.rewriter.EntityRewriter;

import java.util.List;

public class MetadataRewriter1_14To1_13_2 extends EntityRewriter<Protocol1_14To1_13_2> {

    public MetadataRewriter1_14To1_13_2(Protocol1_14To1_13_2 protocol) {
        super(protocol);
        mapTypes(Entity1_13Types.EntityType.values(), Entity1_14Types.class);
        mapEntityType(Entity1_13Types.EntityType.OCELOT, Entity1_14Types.CAT); //TODO remap untamed ocelots to ocelots?
    }

    @Override
    protected void handleMetadata(int entityId, EntityType type, Metadata metadata, List<Metadata> metadatas, UserConnection connection) throws Exception {
        metadata.setMetaType(MetaType1_14.byId(metadata.metaType().typeId()));

        EntityTracker1_14 tracker = tracker(connection);

        if (metadata.metaType() == MetaType1_14.Slot) {
            protocol.getItemRewriter().handleItemToClient((Item) metadata.getValue());
        } else if (metadata.metaType() == MetaType1_14.BlockID) {
            // Convert to new block id
            int data = (int) metadata.getValue();
            metadata.setValue(protocol.getMappingData().getNewBlockStateId(data));
        } else if (metadata.metaType() == MetaType1_14.PARTICLE) {
            rewriteParticle((Particle) metadata.getValue());
        }

        if (type == null) return;

        //Metadata 6 added to abstract_entity
        if (metadata.id() > 5) {
            metadata.setId(metadata.id() + 1);
        }
        if (metadata.id() == 8 && type.isOrHasParent(Entity1_14Types.LIVINGENTITY)) {
            final float v = ((Number) metadata.getValue()).floatValue();
            if (Float.isNaN(v) && Via.getConfig().is1_14HealthNaNFix()) {
                metadata.setValue(1F);
            }
        }

        //Metadata 12 added to living_entity
        if (metadata.id() > 11 && type.isOrHasParent(Entity1_14Types.LIVINGENTITY)) {
            metadata.setId(metadata.id() + 1);
        }

        if (type.isOrHasParent(Entity1_14Types.ABSTRACT_INSENTIENT)) {
            if (metadata.id() == 13) {
                tracker.setInsentientData(entityId, (byte) ((((Number) metadata.getValue()).byteValue() & ~0x4)
                        | (tracker.getInsentientData(entityId) & 0x4))); // New attacking metadata
                metadata.setValue(tracker.getInsentientData(entityId));
            }
        }

        if (type.isOrHasParent(Entity1_14Types.PLAYER)) {
            if (entityId != tracker.clientEntityId()) {
                if (metadata.id() == 0) {
                    byte flags = ((Number) metadata.getValue()).byteValue();
                    // Mojang overrides the client-side pose updater, see OtherPlayerEntity#updateSize
                    tracker.setEntityFlags(entityId, flags);
                } else if (metadata.id() == 7) {
                    tracker.setRiptide(entityId, (((Number) metadata.getValue()).byteValue() & 0x4) != 0);
                }
                if (metadata.id() == 0 || metadata.id() == 7) {
                    metadatas.add(new Metadata(6, MetaType1_14.Pose, recalculatePlayerPose(entityId, tracker)));
                }
            }
        } else if (type.isOrHasParent(Entity1_14Types.ZOMBIE)) {
            if (metadata.id() == 16) {
                tracker.setInsentientData(entityId, (byte) ((tracker.getInsentientData(entityId) & ~0x4)
                        | ((boolean) metadata.getValue() ? 0x4 : 0))); // New attacking
                metadatas.remove(metadata);  // "Are hands held up"
                metadatas.add(new Metadata(13, MetaType1_14.Byte, tracker.getInsentientData(entityId)));
            } else if (metadata.id() > 16) {
                metadata.setId(metadata.id() - 1);
            }
        }

        if (type.isOrHasParent(Entity1_14Types.MINECART_ABSTRACT)) {
            if (metadata.id() == 10) {
                // New block format
                int data = (int) metadata.getValue();
                metadata.setValue(protocol.getMappingData().getNewBlockStateId(data));
            }
        } else if (type.is(Entity1_14Types.HORSE)) {
            if (metadata.id() == 18) {
                metadatas.remove(metadata);

                int armorType = (int) metadata.getValue();
                Item armorItem = null;
                if (armorType == 1) {  //iron armor
                    armorItem = new DataItem(protocol.getMappingData().getNewItemId(727), (byte) 1, (short) 0, null);
                } else if (armorType == 2) {  //gold armor
                    armorItem = new DataItem(protocol.getMappingData().getNewItemId(728), (byte) 1, (short) 0, null);
                } else if (armorType == 3) {  //diamond armor
                    armorItem = new DataItem(protocol.getMappingData().getNewItemId(729), (byte) 1, (short) 0, null);
                }

                PacketWrapper equipmentPacket = PacketWrapper.create(0x46, null, connection);
                equipmentPacket.write(Type.VAR_INT, entityId);
                equipmentPacket.write(Type.VAR_INT, 4);
                equipmentPacket.write(Type.FLAT_VAR_INT_ITEM, armorItem);
                equipmentPacket.scheduleSend(Protocol1_14To1_13_2.class);
            }
        } else if (type.is(Entity1_14Types.VILLAGER)) {
            if (metadata.id() == 15) {
                // plains
                metadata.setTypeAndValue(MetaType1_14.VillagerData, new VillagerData(2, getNewProfessionId((int) metadata.getValue()), 0));
            }
        } else if (type.is(Entity1_14Types.ZOMBIE_VILLAGER)) {
            if (metadata.id() == 18) {
                // plains
                metadata.setTypeAndValue(MetaType1_14.VillagerData, new VillagerData(2, getNewProfessionId((int) metadata.getValue()), 0));
            }
        } else if (type.isOrHasParent(Entity1_14Types.ABSTRACT_ARROW)) {
            if (metadata.id() >= 9) { // New piercing
                metadata.setId(metadata.id() + 1);
            }
        } else if (type.is(Entity1_14Types.FIREWORK_ROCKET)) {
            if (metadata.id() == 8) {
                metadata.setMetaType(MetaType1_14.OptVarInt);
                if (metadata.getValue().equals(0)) {
                    metadata.setValue(null); // https://bugs.mojang.com/browse/MC-111480
                }
            }
        } else if (type.isOrHasParent(Entity1_14Types.ABSTRACT_SKELETON)) {
            if (metadata.id() == 14) {
                tracker.setInsentientData(entityId, (byte) ((tracker.getInsentientData(entityId) & ~0x4)
                        | ((boolean) metadata.getValue() ? 0x4 : 0))); // New attacking
                metadatas.remove(metadata);  // "Is swinging arms"
                metadatas.add(new Metadata(13, MetaType1_14.Byte, tracker.getInsentientData(entityId)));
            }
        }

        if (type.isOrHasParent(Entity1_14Types.ABSTRACT_ILLAGER_BASE)) {
            if (metadata.id() == 14) {
                tracker.setInsentientData(entityId, (byte) ((tracker.getInsentientData(entityId) & ~0x4)
                        | (((Number) metadata.getValue()).byteValue() != 0 ? 0x4 : 0))); // New attacking
                metadatas.remove(metadata);  // "Has target (aggressive state)"
                metadatas.add(new Metadata(13, MetaType1_14.Byte, tracker.getInsentientData(entityId)));
            }
        }

        // TODO Are witch and ravager also abstract illagers? They all inherit the new metadata 14 added in 19w13a
        if (type.is(Entity1_14Types.WITCH) || type.is(Entity1_14Types.RAVAGER) || type.isOrHasParent(Entity1_14Types.ABSTRACT_ILLAGER_BASE)) {
            if (metadata.id() >= 14) {  // TODO 19w13 added a new boolean (raid participant - is celebrating) with id 14
                metadata.setId(metadata.id() + 1);
            }
        }
    }

    @Override
    public EntityType typeFromId(int type) {
        return Entity1_14Types.getTypeFromId(type);
    }

    private static boolean isSneaking(byte flags) {
        return (flags & 0x2) != 0;
    }

    private static boolean isSwimming(byte flags) {
        return (flags & 0x10) != 0;
    }

    private static int getNewProfessionId(int old) {
        // profession -> career
        switch (old) {
            case 0: // farmer
                return 5;
            case 1: // librarian
                return 9;
            case 2: // priest
                return 4; // cleric
            case 3: // blacksmith
                return 1; // armorer
            case 4: // butcher
                return 2;
            case 5: // nitwit
                return 11;
            default:
                return 0; // none
        }
    }

    private static boolean isFallFlying(int entityFlags) {
        return (entityFlags & 0x80) != 0;
    }

    public static int recalculatePlayerPose(int entityId, EntityTracker1_14 tracker) {
        byte flags = tracker.getEntityFlags(entityId);
        // Mojang overrides the client-side pose updater, see OtherPlayerEntity#updateSize
        int pose = 0; // standing
        if (isFallFlying(flags)) {
            pose = 1;
        } else if (tracker.isSleeping(entityId)) {
            pose = 2;
        } else if (isSwimming(flags)) {
            pose = 3;
        } else if (tracker.isRiptide(entityId)) {
            pose = 4;
        } else if (isSneaking(flags)) {
            pose = 5;
        }
        return pose;
    }
}

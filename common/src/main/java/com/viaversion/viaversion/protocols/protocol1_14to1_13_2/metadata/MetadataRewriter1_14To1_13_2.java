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
package com.viaversion.viaversion.protocols.protocol1_14to1_13_2.metadata;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.minecraft.VillagerData;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_13;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_14;
import com.viaversion.viaversion.api.minecraft.item.DataItem;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.metadata.Metadata;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.version.Types1_14;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.ClientboundPackets1_13;
import com.viaversion.viaversion.protocols.protocol1_14to1_13_2.ClientboundPackets1_14;
import com.viaversion.viaversion.protocols.protocol1_14to1_13_2.Protocol1_14To1_13_2;
import com.viaversion.viaversion.protocols.protocol1_14to1_13_2.storage.EntityTracker1_14;
import com.viaversion.viaversion.rewriter.EntityRewriter;

public class MetadataRewriter1_14To1_13_2 extends EntityRewriter<ClientboundPackets1_13, Protocol1_14To1_13_2> {

    public MetadataRewriter1_14To1_13_2(Protocol1_14To1_13_2 protocol) {
        super(protocol);
        mapTypes(EntityTypes1_13.EntityType.values(), EntityTypes1_14.class);

        if (Via.getConfig().translateOcelotToCat()) {
            // A better solution for this would be to despawn the ocelot and spawn a cat in its place, but that would
            // require a lot of data tracking and is not worth the effort.
            mapEntityType(EntityTypes1_13.EntityType.OCELOT, EntityTypes1_14.CAT);
        }
    }

    @Override
    protected void registerRewrites() {
        filter().mapMetaType(Types1_14.META_TYPES::byId);
        registerMetaTypeHandler(Types1_14.META_TYPES.itemType, Types1_14.META_TYPES.blockStateType, Types1_14.META_TYPES.particleType);

        filter().type(EntityTypes1_14.ENTITY).addIndex(6);
        filter().type(EntityTypes1_14.LIVINGENTITY).addIndex(12);

        filter().type(EntityTypes1_14.LIVINGENTITY).index(8).handler((event, meta) -> {
            float value = ((Number) meta.getValue()).floatValue();
            if (Float.isNaN(value) && Via.getConfig().is1_14HealthNaNFix()) {
                meta.setValue(1F);
            }
        });

        filter().type(EntityTypes1_14.ABSTRACT_INSENTIENT).index(13).handler((event, meta) -> {
            EntityTracker1_14 tracker = tracker(event.user());
            int entityId = event.entityId();
            tracker.setInsentientData(entityId, (byte) ((((Number) meta.getValue()).byteValue() & ~0x4)
                    | (tracker.getInsentientData(entityId) & 0x4))); // New attacking metadata
            meta.setValue(tracker.getInsentientData(entityId));
        });

        filter().type(EntityTypes1_14.PLAYER).handler((event, meta) -> {
            EntityTracker1_14 tracker = tracker(event.user());
            int entityId = event.entityId();
            if (entityId != tracker.clientEntityId()) {
                if (meta.id() == 0) {
                    byte flags = ((Number) meta.getValue()).byteValue();
                    // Mojang overrides the client-side pose updater, see OtherPlayerEntity#updateSize
                    tracker.setEntityFlags(entityId, flags);
                } else if (meta.id() == 7) {
                    tracker.setRiptide(entityId, (((Number) meta.getValue()).byteValue() & 0x4) != 0);
                }
                if (meta.id() == 0 || meta.id() == 7) {
                    event.createExtraMeta(new Metadata(6, Types1_14.META_TYPES.poseType, recalculatePlayerPose(entityId, tracker)));
                }
            }
        });

        filter().type(EntityTypes1_14.ZOMBIE).handler((event, meta) -> {
            if (meta.id() == 16) {
                EntityTracker1_14 tracker = tracker(event.user());
                int entityId = event.entityId();
                tracker.setInsentientData(entityId, (byte) ((tracker.getInsentientData(entityId) & ~0x4)
                        | ((boolean) meta.getValue() ? 0x4 : 0))); // New attacking
                event.createExtraMeta(new Metadata(13, Types1_14.META_TYPES.byteType, tracker.getInsentientData(entityId)));
                event.cancel(); // "Are hands held up"
            } else if (meta.id() > 16) {
                meta.setId(meta.id() - 1);
            }
        });

        filter().type(EntityTypes1_14.MINECART_ABSTRACT).index(10).handler((event, meta) -> {
            int data = meta.value();
            meta.setValue(protocol.getMappingData().getNewBlockStateId(data));
        });

        filter().type(EntityTypes1_14.HORSE).index(18).handler((event, meta) -> {
            event.cancel();

            int armorType = meta.value();
            Item armorItem = null;
            if (armorType == 1) {  //iron armor
                armorItem = new DataItem(protocol.getMappingData().getNewItemId(727), (byte) 1, (short) 0, null);
            } else if (armorType == 2) {  //gold armor
                armorItem = new DataItem(protocol.getMappingData().getNewItemId(728), (byte) 1, (short) 0, null);
            } else if (armorType == 3) {  //diamond armor
                armorItem = new DataItem(protocol.getMappingData().getNewItemId(729), (byte) 1, (short) 0, null);
            }

            PacketWrapper equipmentPacket = PacketWrapper.create(ClientboundPackets1_14.ENTITY_EQUIPMENT, null, event.user());
            equipmentPacket.write(Type.VAR_INT, event.entityId());
            equipmentPacket.write(Type.VAR_INT, 4);
            equipmentPacket.write(Type.ITEM1_13_2, armorItem);
            try {
                equipmentPacket.scheduleSend(Protocol1_14To1_13_2.class);
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        });

        filter().type(EntityTypes1_14.VILLAGER).index(15).handler((event, meta) -> {
            meta.setTypeAndValue(Types1_14.META_TYPES.villagerDatatType, new VillagerData(2, getNewProfessionId(meta.value()), 0));
        });

        filter().type(EntityTypes1_14.ZOMBIE_VILLAGER).index(18).handler((event, meta) -> {
            meta.setTypeAndValue(Types1_14.META_TYPES.villagerDatatType, new VillagerData(2, getNewProfessionId(meta.value()), 0));
        });

        filter().type(EntityTypes1_14.ABSTRACT_ARROW).addIndex(9); // Piercing level added

        filter().type(EntityTypes1_14.FIREWORK_ROCKET).index(8).handler((event, meta) -> {
            meta.setMetaType(Types1_14.META_TYPES.optionalVarIntType);
            if (meta.getValue().equals(0)) {
                meta.setValue(null); // https://bugs.mojang.com/browse/MC-111480
            }
        });

        filter().type(EntityTypes1_14.ABSTRACT_SKELETON).index(14).handler((event, meta) -> {
            EntityTracker1_14 tracker = tracker(event.user());
            int entityId = event.entityId();
            tracker.setInsentientData(entityId, (byte) ((tracker.getInsentientData(entityId) & ~0x4)
                    | ((boolean) meta.getValue() ? 0x4 : 0))); // New attacking
            event.createExtraMeta(new Metadata(13, Types1_14.META_TYPES.byteType, tracker.getInsentientData(entityId)));
            event.cancel();  // "Is swinging arms"
        });

        filter().type(EntityTypes1_14.ABSTRACT_ILLAGER_BASE).handler((event, meta) -> {
            if (event.index() == 14) {
                EntityTracker1_14 tracker = tracker(event.user());
                int entityId = event.entityId();
                tracker.setInsentientData(entityId, (byte) ((tracker.getInsentientData(entityId) & ~0x4)
                    | (((Number) meta.getValue()).byteValue() != 0 ? 0x4 : 0))); // New attacking
                event.createExtraMeta(new Metadata(13, Types1_14.META_TYPES.byteType, tracker.getInsentientData(entityId)));
                event.cancel(); // "Has target (aggressive state)"
            } else if (event.index() > 14) {
                meta.setId(meta.id() - 1);
            }
        });

        filter().type(EntityTypes1_14.OCELOT).removeIndex(17); // variant

        // Ocelot is not tamable anymore
        filter().type(EntityTypes1_14.OCELOT).removeIndex(16); // owner uuid
        filter().type(EntityTypes1_14.OCELOT).removeIndex(15); // data

        filter().handler((event, meta) -> {
            EntityType type = event.entityType();
            if (type.is(EntityTypes1_14.WITCH) || type.is(EntityTypes1_14.RAVAGER) || type.isOrHasParent(EntityTypes1_14.ABSTRACT_ILLAGER_BASE)) {
                if (meta.id() >= 14) {  // 19w13 added a new boolean (raid participant - is celebrating) with id 14
                    meta.setId(meta.id() + 1);
                }
            }
        });
    }

    @Override
    public EntityType typeFromId(int type) {
        return EntityTypes1_14.getTypeFromId(type);
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

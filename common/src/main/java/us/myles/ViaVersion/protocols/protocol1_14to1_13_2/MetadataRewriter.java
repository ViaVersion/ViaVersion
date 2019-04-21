package us.myles.ViaVersion.protocols.protocol1_14to1_13_2;

import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.entities.Entity1_14Types;
import us.myles.ViaVersion.api.minecraft.VillagerData;
import us.myles.ViaVersion.api.minecraft.item.Item;
import us.myles.ViaVersion.api.minecraft.metadata.Metadata;
import us.myles.ViaVersion.api.minecraft.metadata.types.MetaType1_14;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.data.Particle;
import us.myles.ViaVersion.protocols.protocol1_14to1_13_2.packets.InventoryPackets;
import us.myles.ViaVersion.protocols.protocol1_14to1_13_2.storage.EntityTracker;

import java.util.ArrayList;
import java.util.List;

public class MetadataRewriter {

    public static void handleMetadata(int entityId, Entity1_14Types.EntityType type, List<Metadata> metadatas, UserConnection connection) {
        for (Metadata metadata : new ArrayList<>(metadatas)) {
            try {
                metadata.setMetaType(MetaType1_14.byId(metadata.getMetaType().getTypeID()));

                EntityTracker tracker = connection.get(EntityTracker.class);

                if (metadata.getMetaType() == MetaType1_14.Slot) {
                    InventoryPackets.toClient((Item) metadata.getValue());
                } else if (metadata.getMetaType() == MetaType1_14.BlockID) {
                    // Convert to new block id
                    int data = (int) metadata.getValue();
                    metadata.setValue(Protocol1_14To1_13_2.getNewBlockStateId(data));
                }

                if (type == null) continue;

                //Metadata 6 added to abstract_entity
                if (metadata.getId() > 5) {
                    metadata.setId(metadata.getId() + 1);
                }

                //Metadata 12 added to living_entity
                if (metadata.getId() > 11 && type.isOrHasParent(Entity1_14Types.EntityType.LIVINGENTITY)) {
                    metadata.setId(metadata.getId() + 1);
                }

                if (type.isOrHasParent(Entity1_14Types.EntityType.ABSTRACT_INSENTIENT)) {
                    if (metadata.getId() == 13) {
                        tracker.setInsentientData(entityId, (byte) ((((Number)metadata.getValue()).byteValue() & ~0x4)
                                | (tracker.getInsentientData(entityId) & 0x4))); // New attacking metadata
                        metadata.setValue(tracker.getInsentientData(entityId));
                    }
                }

                if (type.isOrHasParent(Entity1_14Types.EntityType.MINECART_ABSTRACT)) {
                    if (metadata.getId() == 10) {
                        // New block format
                        int data = (int) metadata.getValue();
                        metadata.setValue(Protocol1_14To1_13_2.getNewBlockStateId(data));
                    }
                } else if (type.is(Entity1_14Types.EntityType.HORSE)) {
                    if (metadata.getId() == 18) {
                        metadatas.remove(metadata);

                        int armorType = (int) metadata.getValue();
                        Item armorItem = null;
                        if (armorType == 1) {  //iron armor
                            armorItem = new Item(InventoryPackets.getNewItemId(727), (byte) 1, (short) 0, null);
                        } else if (armorType == 2) {  //gold armor
                            armorItem = new Item(InventoryPackets.getNewItemId(728), (byte) 1, (short) 0, null);
                        } else if (armorType == 3) {  //diamond armor
                            armorItem = new Item(InventoryPackets.getNewItemId(729), (byte) 1, (short) 0, null);
                        }

                        PacketWrapper equipmentPacket = new PacketWrapper(0x46, null, connection);
                        equipmentPacket.write(Type.VAR_INT, entityId);
                        equipmentPacket.write(Type.VAR_INT, 4);
                        equipmentPacket.write(Type.FLAT_VAR_INT_ITEM, armorItem);
                        equipmentPacket.send(Protocol1_14To1_13_2.class);
                    }
                } else if (type.is(Entity1_14Types.EntityType.VILLAGER)) {
                    if (metadata.getId() == 15) {
                        // plains
                        metadata.setValue(new VillagerData(2, getNewProfessionId((int) metadata.getValue()), 0));
                        metadata.setMetaType(MetaType1_14.VillagerData);
                    }
                } else if (type.is(Entity1_14Types.EntityType.ZOMBIE_VILLAGER)) {
                    if (metadata.getId() == 19) {
                        // plains
                        metadata.setValue(new VillagerData(2, getNewProfessionId((int) metadata.getValue()), 0));
                        metadata.setMetaType(MetaType1_14.VillagerData);
                    }
                } else if (type.isOrHasParent(Entity1_14Types.EntityType.ARROW)) {
                    if (metadata.getId() >= 9) {
                        metadata.setId(metadata.getId() + 1);
                    }
                } else if (type.is(Entity1_14Types.EntityType.FIREWORKS_ROCKET)) {
                    if (metadata.getId() == 8) {
                        if (metadata.getValue().equals(0)) metadata.setValue(null); // https://bugs.mojang.com/browse/MC-111480
                        metadata.setMetaType(MetaType1_14.OptVarInt);
                    }
                } else if (type.isOrHasParent(Entity1_14Types.EntityType.ABSTRACT_SKELETON)) {
                    if (metadata.getId() == 12) {
                        tracker.setInsentientData(entityId, (byte) ((tracker.getInsentientData(entityId) & ~0x4)
                                | ((boolean) metadata.getValue() ? 0x4 : 0))); // New attacking
                        metadatas.remove(metadata);  // "Is swinging arms"
                        metadatas.add(new Metadata(13, MetaType1_14.Byte, tracker.getInsentientData(entityId)));
                    }
                } else if (type.isOrHasParent(Entity1_14Types.EntityType.ZOMBIE)) {
                    if (metadata.getId() == 16) {
                        tracker.setInsentientData(entityId, (byte) ((tracker.getInsentientData(entityId) & ~0x4)
                                | ((boolean) metadata.getValue() ? 0x4 : 0))); // New attacking
                        metadatas.remove(metadata);  // "Are hands held up"
                        metadatas.add(new Metadata(13, MetaType1_14.Byte, tracker.getInsentientData(entityId)));
                    } else if (metadata.getId() > 16) {
                        metadata.setId(metadata.getId() - 1);
                    }
                } else if (type.is(Entity1_14Types.EntityType.AREA_EFFECT_CLOUD)) {
                    if (metadata.getId() == 10) {
                        Particle particle = (Particle) metadata.getValue();
                        particle.setId(getNewParticleId(particle.getId()));
                    }
                }

                if (type.isOrHasParent(Entity1_14Types.EntityType.ABSTRACT_ILLAGER_BASE)) {
                    if (metadata.getId() == 14) {
                        tracker.setInsentientData(entityId, (byte) ((tracker.getInsentientData(entityId) & ~0x4)
                                | ((boolean) metadata.getValue() ? 0x4 : 0))); // New attacking
                        metadatas.remove(metadata);  // "Has target (aggressive state)"
                        metadatas.add(new Metadata(13, MetaType1_14.Byte, tracker.getInsentientData(entityId)));
                    }
                }

                // TODO Are witch and ravager also abstract illagers? They all inherit the new metadata 14 added in 19w13a
                if (type.is(Entity1_14Types.EntityType.WITCH) || type.is(Entity1_14Types.EntityType.RAVAGER) || type.isOrHasParent(Entity1_14Types.EntityType.ABSTRACT_ILLAGER_BASE)) {
                    if (metadata.getId() >= 14) {  // TODO 19w13 added a new boolean with id 14
                        metadata.setId(metadata.getId() + 1);
                    }
                }
            } catch (Exception e) {
                metadatas.remove(metadata);
                if (!Via.getConfig().isSuppressMetadataErrors() || Via.getManager().isDebug()) {
                    Via.getPlatform().getLogger().warning("An error occurred with entity metadata handler");
                    Via.getPlatform().getLogger().warning("Metadata: " + metadata);
                    e.printStackTrace();
                }
            }
        }
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

    public static int getNewParticleId(int id) {
        if (id >= 10) {
            id += 2; // new lava drips 10, 11
        }
        if (id >= 13) {
            id += 1; // new water drip 11 -> 13
        }
        if (id >= 27) {
            id += 1; // new 24 -> 27
        }
        if (id >= 29) {
            id += 1; // skip new short happy villager
        }
        if (id >= 44) {
            id += 1; // new 39 -> 44
        }
        return id;
    }
}

package us.myles.ViaVersion.protocols.protocolsnapshotto1_10;

import com.google.common.base.Optional;
import us.myles.ViaVersion.api.ViaVersion;
import us.myles.ViaVersion.api.minecraft.metadata.Metadata;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.metadata.NewType;

import java.util.ArrayList;
import java.util.List;

public class MetadataRewriter {
    public static int rewriteEntityType(int currentType, List<Metadata> metadata) {
        try {
            if (currentType == 68) {
                // ElderGuardian - 4
                Optional<Metadata> options = getById(metadata, 12);
                if (options.isPresent()) {
                    if ((((byte) options.get().getValue()) & 0x04) == 0x04) {
                        return 4;
                    }
                }
            }
            if (currentType == 51) {
                // WitherSkeleton - 5
                // Stray - 6
                Optional<Metadata> options = getById(metadata, 12);
                if (options.isPresent()) {
                    if (((int) options.get().getValue()) == 1) {
                        return 5;
                    }
                    if (((int) options.get().getValue()) == 2) {
                        return 6;
                    }
                }
            }
            if (currentType == 54) {
                // ZombieVillager - 27
                // Husk - 23
                Optional<Metadata> options = getById(metadata, 13);
                if (options.isPresent()) {
                    int value = (int) options.get().getValue();
                    if (value > 0 && value < 6) {
                        metadata.add(new Metadata(16, NewType.VarInt.getTypeID(), Type.VAR_INT, value - 1)); // Add profession type to new metadata
                        return 27;
                    }
                    if (value == 6) {
                        return 23;
                    }
                }
            }
            if (currentType == 100) {
                // SkeletonHorse - 28
                // ZombieHorse - 29
                // Donkey - 31
                // Mule - 32
                Optional<Metadata> options = getById(metadata, 14);
                if (options.isPresent()) {
                    if (((int) options.get().getValue()) == 0) {
                        return currentType;
                    }
                    if (((int) options.get().getValue()) == 1) {
                        return 31;
                    }
                    if (((int) options.get().getValue()) == 2) {
                        return 32;
                    }
                    if (((int) options.get().getValue()) == 3) {
                        return 29;
                    }
                    if (((int) options.get().getValue()) == 4) {
                        return 28;
                    }
                }
            }
            return currentType;
        } catch (Exception e) {
            ;
            if (!ViaVersion.getConfig().isSuppressMetadataErrors() || ViaVersion.getInstance().isDebug()) {
                System.out.println("An error occurred with entity type rewriter");
                System.out.println("Metadata: " + metadata);
                e.printStackTrace();
            }
        }
        return currentType;
    }

    public static void handleMetadata(int type, List<Metadata> metadatas) {
        for (Metadata metadata : new ArrayList<>(metadatas)) {
            try {
                if (type == 4 || type == 68) { // Guardians
                    int oldid = metadata.getId();
                    if (oldid == 12) {
                        metadata.setType(Type.BOOLEAN);
                        metadata.setTypeID(NewType.Boolean.getTypeID());
                        boolean val = (((byte) metadata.getValue()) & 0x02) == 0x02;
                        metadata.setValue(val);
                    }
                }
                if (type == 51 || type == 5 || type == 6) { // Skeletons
                    int oldid = metadata.getId();
                    if (oldid == 12) {
                        metadatas.remove(metadata);
                    }
                    if (oldid == 13) {
                        metadata.setId(12);
                    }
                }
                if (type == 54 || type == 27) { // Zombie | Zombie Villager
                    if (type == 54 && metadata.getId() == 14) {
                        metadatas.remove(metadata);
                    } else {
                        if (metadata.getId() == 15) {
                            metadata.setId(14);
                        } else {
                            if (metadata.getId() == 14) {
                                metadata.setId(15);
                            }
                        }
                    }
                }
                if (type == 100 || type == 31 || type == 32 || type == 29 || type == 28) { // Horses
                    // Remap metadata id
                    int oldid = metadata.getId();
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
                    if (type == 100) {
                        // Normal Horse
                    } else {
                        // Remove 15, 16
                        if (metadata.getId() == 15 || metadata.getId() == 16) {
                            metadatas.remove(metadata);
                        }
                    }
                    if (type == 31 || type == 32) {
                        // Chested Horse
                        if (metadata.getId() == 13) {
                            if ((((byte) metadata.getValue()) & 0x08) == 0x08) {
                                metadatas.add(new Metadata(15, NewType.Boolean.getTypeID(), Type.BOOLEAN, true));
                            } else {
                                metadatas.add(new Metadata(15, NewType.Boolean.getTypeID(), Type.BOOLEAN, false));
                            }
                        }
                    }
                }
            } catch (Exception e) {
                ;
                metadatas.remove(metadata);
                if (!ViaVersion.getConfig().isSuppressMetadataErrors() || ViaVersion.getInstance().isDebug()) {
                    System.out.println("An error occurred with entity metadata handler");
                    System.out.println("Metadata: " + metadata);
                    e.printStackTrace();
                }
            }
        }
    }

    public static Optional<Metadata> getById(List<Metadata> metadatas, int id) {
        for (Metadata metadata : metadatas) {
            if (metadata.getId() == id) return Optional.of(metadata);
        }
        return Optional.absent();
    }
}

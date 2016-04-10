package us.myles.ViaVersion.protocols.protocol1_9to1_8.metadata;

import org.bukkit.entity.EntityType;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;
import us.myles.ViaVersion.api.ViaVersion;
import us.myles.ViaVersion.api.minecraft.item.Item;
import us.myles.ViaVersion.api.minecraft.metadata.Metadata;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.ItemRewriter;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.Protocol1_9TO1_8;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MetadataRewriter {
    public static void transform(EntityType type, List<Metadata> list) {
        short id = -1;
        int data = -1;
        for (Metadata entry : new ArrayList<>(list)) {
            MetaIndex metaIndex = MetaIndex.getIndex(type, entry.getId());
            try {
                if (metaIndex != null) {
                    if (metaIndex.getNewType() != NewType.Discontinued) {
                        if (metaIndex.getNewType() != NewType.BlockID || id != -1 && data == -1 || id == -1 && data != -1) { // block ID is only written if we have both parts
                            entry.setId(metaIndex.getNewIndex());
                            entry.setTypeID(metaIndex.getNewType().getTypeID());
                        }
                        Object value = entry.getValue();
                        switch (metaIndex.getNewType()) {
                            case Byte:
                                entry.setType(us.myles.ViaVersion.api.type.Type.BYTE);
                                // convert from int, byte
                                if (metaIndex.getOldType() == Type.Byte) {
                                    entry.setValue(value);
                                }
                                if (metaIndex.getOldType() == Type.Int) {
                                    entry.setValue(((Integer) value).byteValue());
                                }
                                // After writing the last one
                                if (metaIndex == MetaIndex.ENTITY_STATUS && type == EntityType.PLAYER) {
                                    Byte val = 0;
                                    if ((((Byte) value) & 0x10) == 0x10) { // Player eating/aiming/drinking
                                        val = 1;
                                    }
                                    int newIndex = MetaIndex.PLAYER_HAND.getNewIndex();
                                    int typeID = MetaIndex.PLAYER_HAND.getNewType().getTypeID();
                                    Metadata metadata = new Metadata(newIndex, typeID, us.myles.ViaVersion.api.type.Type.BYTE, val);
                                    list.add(metadata);
                                }
                                break;
                            case OptUUID:
                                entry.setType(us.myles.ViaVersion.api.type.Type.OPTIONAL_UUID);
                                String owner = (String) value;
                                UUID toWrite = null;
                                if (owner.length() != 0) {
                                    try {
                                        toWrite = UUID.fromString(owner);
                                    } catch (Exception ignored) {
                                    }
                                }
                                entry.setValue(toWrite);
                                break;
                            case BlockID:
                                entry.setType(us.myles.ViaVersion.api.type.Type.VAR_INT);
                                // if we have both sources :))
                                if (metaIndex.getOldType() == Type.Byte) {
                                    data = (Byte) value;
                                }
                                if (metaIndex.getOldType() == Type.Short) {
                                    id = (Short) value;
                                }
                                if (id != -1 && data != -1) {
                                    int combined = id << 4 | data;
                                    data = -1;
                                    id = -1;
                                    entry.setValue(combined);
                                } else {
                                    list.remove(entry);
                                }
                                break;
                            case VarInt:
                                entry.setType(us.myles.ViaVersion.api.type.Type.VAR_INT);
                                // convert from int, short, byte
                                if (metaIndex.getOldType() == Type.Byte) {
                                    entry.setValue(((Byte) value).intValue());
                                }
                                if (metaIndex.getOldType() == Type.Short) {
                                    entry.setValue(((Short) value).intValue());
                                }
                                if (metaIndex.getOldType() == Type.Int) {
                                    entry.setValue(value);
                                }
                                break;
                            case Float:
                                entry.setType(us.myles.ViaVersion.api.type.Type.FLOAT);
                                entry.setValue(value);
                                break;
                            case String:
                                entry.setType(us.myles.ViaVersion.api.type.Type.STRING);
                                entry.setValue(value);
                                break;
                            case Boolean:
                                entry.setType(us.myles.ViaVersion.api.type.Type.BOOLEAN);
                                if (metaIndex == MetaIndex.AGEABLE_AGE)
                                    entry.setValue((Byte) value < 0);
                                else
                                    entry.setValue((Byte) value != 0);
                                break;
                            case Slot:
                                entry.setType(us.myles.ViaVersion.api.type.Type.ITEM);
                                entry.setValue(value);
                                ItemRewriter.toClient((Item) entry.getValue());
                                break;
                            case Position:
                                entry.setType(us.myles.ViaVersion.api.type.Type.VECTOR);
                                Vector vector = (Vector) value;
                                entry.setValue(vector);
                                break;
                            case Vector3F:
                                entry.setType(us.myles.ViaVersion.api.type.Type.ROTATION);
                                EulerAngle angle = (EulerAngle) value;
                                entry.setValue(angle);
                                break;
                            case Chat:
                                entry.setType(us.myles.ViaVersion.api.type.Type.STRING);
                                value = Protocol1_9TO1_8.fixJson((String) value);
                                entry.setValue(value);
                                break;
                            default:
                                System.out.println("[Out] Unhandled MetaDataType: " + metaIndex.getNewType());
                                list.remove(entry);
                                break;
                        }
                    } else {
                        list.remove(entry);
                    }
                } else {
                    throw new Exception("Could not find valid metadata");
                }
            } catch (Exception e) {
                list.remove(entry);
                if (!ViaVersion.getConfig().isSuppressMetadataErrors() || ViaVersion.getInstance().isDebug()) {
                    System.out.println("INCLUDE THIS IN YOUR ERROR LOG!");
                    if (type != null)
                        System.out.println("An error occurred with entity meta data for " + type + " OldID: " + entry.getId());
                    else
                        System.out.println("An error occurred with entity meta data for UNKNOWN_ENTITY OldID: " + entry.getId());
                    if (metaIndex != null) {
                        System.out.println("Value: " + entry.getValue());
                        System.out.println("Old ID: " + metaIndex.getIndex() + " New ID: " + metaIndex.getNewIndex());
                        System.out.println("Old Type: " + metaIndex.getOldType() + " New Type: " + metaIndex.getNewType());
                    }
                    e.printStackTrace();
                }
            }
        }
    }

}

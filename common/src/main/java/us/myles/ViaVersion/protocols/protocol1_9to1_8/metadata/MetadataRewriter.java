package us.myles.ViaVersion.protocols.protocol1_9to1_8.metadata;

import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.entities.Entity1_10Types;
import us.myles.ViaVersion.api.minecraft.EulerAngle;
import us.myles.ViaVersion.api.minecraft.Vector;
import us.myles.ViaVersion.api.minecraft.item.Item;
import us.myles.ViaVersion.api.minecraft.metadata.MetaType;
import us.myles.ViaVersion.api.minecraft.metadata.Metadata;
import us.myles.ViaVersion.api.minecraft.metadata.types.MetaType1_8;
import us.myles.ViaVersion.api.minecraft.metadata.types.MetaType1_9;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.ItemRewriter;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.Protocol1_9TO1_8;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public class MetadataRewriter {
    public static void transform(Entity1_10Types.EntityType type, List<Metadata> list) {
        short id = -1;
        int data = -1;
        for (Metadata entry : new ArrayList<>(list)) {
            MetaIndex metaIndex = MetaIndex.searchIndex(type, entry.getId());
            try {
                if (metaIndex != null) {
                    if (metaIndex.getNewType() != MetaType1_9.Discontinued) {
                        if (metaIndex.getNewType() != MetaType1_9.BlockID || id != -1 && data == -1 || id == -1 && data != -1) { // block ID is only written if we have both parts
                            entry.setId(metaIndex.getNewIndex());
                            entry.setMetaType(metaIndex.getNewType());
                        }
                        Object value = entry.getValue();
                        switch (metaIndex.getNewType()) {
                            case Byte:
                                // convert from int, byte
                                if (metaIndex.getOldType() == MetaType1_8.Byte) {
                                    entry.setValue(value);
                                }
                                if (metaIndex.getOldType() == MetaType1_8.Int) {
                                    entry.setValue(((Integer) value).byteValue());
                                }
                                // After writing the last one
                                if (metaIndex == MetaIndex.ENTITY_STATUS && type == Entity1_10Types.EntityType.PLAYER) {
                                    Byte val = 0;
                                    if ((((Byte) value) & 0x10) == 0x10) { // Player eating/aiming/drinking
                                        val = 1;
                                    }
                                    int newIndex = MetaIndex.PLAYER_HAND.getNewIndex();
                                    MetaType metaType = MetaIndex.PLAYER_HAND.getNewType();
                                    Metadata metadata = new Metadata(newIndex, metaType, val);
                                    list.add(metadata);
                                }
                                break;
                            case OptUUID:
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
                                // if we have both sources :))
                                if (metaIndex.getOldType() == MetaType1_8.Byte) {
                                    data = (Byte) value;
                                }
                                if (metaIndex.getOldType() == MetaType1_8.Short) {
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
                                // convert from int, short, byte
                                if (metaIndex.getOldType() == MetaType1_8.Byte) {
                                    entry.setValue(((Byte) value).intValue());
                                }
                                if (metaIndex.getOldType() == MetaType1_8.Short) {
                                    entry.setValue(((Short) value).intValue());
                                }
                                if (metaIndex.getOldType() == MetaType1_8.Int) {
                                    entry.setValue(value);
                                }
                                break;
                            case Float:
                                entry.setValue(value);
                                break;
                            case String:
                                entry.setValue(value);
                                break;
                            case Boolean:
                                if (metaIndex == MetaIndex.AGEABLE_AGE)
                                    entry.setValue((Byte) value < 0);
                                else
                                    entry.setValue((Byte) value != 0);
                                break;
                            case Slot:
                                entry.setValue(value);
                                ItemRewriter.toClient((Item) entry.getValue());
                                break;
                            case Position:
                                Vector vector = (Vector) value;
                                entry.setValue(vector);
                                break;
                            case Vector3F:
                                EulerAngle angle = (EulerAngle) value;
                                entry.setValue(angle);
                                break;
                            case Chat:
                                value = Protocol1_9TO1_8.fixJson((String) value);
                                entry.setValue(value);
                                break;
                            default:
                                Via.getPlatform().getLogger().warning("[Out] Unhandled MetaDataType: " + metaIndex.getNewType());
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
                if (!Via.getConfig().isSuppressMetadataErrors() || Via.getManager().isDebug()) {
                    Logger log = Via.getPlatform().getLogger();

                    log.warning("This is most likely down to one of your plugins sending bad datawatchers. Please test if this occurs without any plugins except ViaVersion before reporting it on GitHub");
                    log.warning("Also make sure that all your plugins are compatible with your server version.");
                    if (type != null)
                        log.severe("An error occurred with entity meta data for " + type + " OldID: " + entry.getId());
                    else
                        log.severe("An error occurred with entity meta data for UNKNOWN_ENTITY OldID: " + entry.getId());
                    if (metaIndex != null) {
                        log.severe("Value: " + entry.getValue());
                        log.severe("Old ID: " + metaIndex.getIndex() + " New ID: " + metaIndex.getNewIndex());
                        log.severe("Old Type: " + metaIndex.getOldType() + " New Type: " + metaIndex.getNewType());
                    }
                    e.printStackTrace();
                }
            }
        }
    }

}

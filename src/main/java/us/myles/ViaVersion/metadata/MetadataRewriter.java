package us.myles.ViaVersion.metadata;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.EntityType;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;
import us.myles.ViaVersion.slot.ItemSlotRewriter;
import us.myles.ViaVersion.slot.ItemSlotRewriter.ItemStack;
import us.myles.ViaVersion.transformers.OutgoingTransformer;
import us.myles.ViaVersion.util.PacketUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MetadataRewriter {

    public static void writeMetadata1_9(EntityType type, List<Entry> list, ByteBuf output) {
        short id = -1;
        int data = -1;
        for (Entry entry : list) {
            MetaIndex metaIndex = entry.index;
            try {
                if (metaIndex.getNewType() != NewType.Discontinued) {
                    if (metaIndex.getNewType() != NewType.BlockID || id != -1 && data == -1 || id == -1 && data != -1) { // block ID is only written if we have both parts
                        output.writeByte(metaIndex.getNewIndex());
                        output.writeByte(metaIndex.getNewType().getTypeID());
                    }
                    Object value = entry.value;
                    switch (metaIndex.getNewType()) {
                        case Byte:
                            // convert from int, byte
                            if (metaIndex.getOldType() == Type.Byte) {
                                output.writeByte((Byte) value);
                            }
                            if (metaIndex.getOldType() == Type.Int) {
                                output.writeByte(((Integer) value).byteValue());
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
                            output.writeBoolean(toWrite != null);
                            if (toWrite != null)
                                PacketUtil.writeUUID(toWrite, output);
                            break;
                        case BlockID:
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
                                PacketUtil.writeVarInt(combined, output);
                            }
                            break;
                        case VarInt:
                            // convert from int, short, byte
                            if (metaIndex.getOldType() == Type.Byte) {
                                PacketUtil.writeVarInt(((Byte) value).intValue(), output);
                            }
                            if (metaIndex.getOldType() == Type.Short) {
                                PacketUtil.writeVarInt(((Short) value).intValue(), output);
                            }
                            if (metaIndex.getOldType() == Type.Int) {
                                PacketUtil.writeVarInt((Integer) value, output);
                            }
                            break;
                        case Float:
                            output.writeFloat((Float) value);
                            break;
                        case String:
                            PacketUtil.writeString((String) value, output);
                            break;
                        case Boolean:
                            if (metaIndex == MetaIndex.AGEABLE_AGE)
                                output.writeBoolean((Byte) value < 0);
                            else
                                output.writeBoolean((Byte) value != 0);
                            break;
                        case Slot:
                            ItemStack item = (ItemStack) value;
                            ItemSlotRewriter.fixIdsFrom1_8To1_9(item);
                            ItemSlotRewriter.writeItemStack(item, output);
                            break;
                        case Position:
                            Vector vector = (Vector) value;
                            output.writeInt((int) vector.getX());
                            output.writeInt((int) vector.getY());
                            output.writeInt((int) vector.getZ());
                            break;
                        case Vector3F:
                            EulerAngle angle = (EulerAngle) value;
                            output.writeFloat((float) angle.getX());
                            output.writeFloat((float) angle.getY());
                            output.writeFloat((float) angle.getZ());
                            break;
                        case Chat:
                            PacketUtil.writeString(OutgoingTransformer.fixJson((String) value), output);
                            break;
                        default:
                            System.out.println("[Out] Unhandled MetaDataType: " + metaIndex.getNewType());
                            break;
                    }
                }
            } catch (Exception e) {
                System.out.println("INCLUDE THIS IN YOUR ERROR LOG!");
                if (type != null)
                    System.out.println("An error occurred with entity meta data for " + type + " OldID: " + entry.oldID);
                else
                    System.out.println("An error occurred with entity meta data for UNKOWN_ENTITY OldID: " + entry.oldID);
                if (metaIndex != null) {
                    System.out.println("Old ID: " + metaIndex.getIndex() + " New ID: " + metaIndex.getNewIndex());
                    System.out.println("Old Type: " + metaIndex.getOldType() + " New Type: " + metaIndex.getNewType());
                }
                e.printStackTrace();
            }
        }
        output.writeByte(255);
    }

    public static List<Entry> readMetadata1_8(EntityType entityType, ByteBuf buf) {
        List<Entry> entries = new ArrayList<>();
        byte item;
        while ((item = buf.readByte()) != 127) {
            Type type = Type.byId((item & 0xE0) >> 5);
            int id = item & 0x1F;
            MetaIndex index = MetaIndex.getIndex(entityType, id);
            switch (type) {
                case Byte:
                    entries.add(new Entry(index, buf.readByte(), id));
                    break;
                case Short:
                    entries.add(new Entry(index, buf.readShort(), id));
                    break;
                case Int:
                    entries.add(new Entry(index, buf.readInt(), id));
                    break;
                case Float:
                    entries.add(new Entry(index, buf.readFloat(), id));
                    break;
                case String:
                    entries.add(new Entry(index, PacketUtil.readString(buf), id));
                    break;
                case Slot: {
                    try {
                        entries.add(new Entry(index, ItemSlotRewriter.readItemStack(buf), id));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
                case Position: {
                    int x = buf.readInt();
                    int y = buf.readInt();
                    int z = buf.readInt();
                    entries.add(new Entry(index, new Vector(x, y, z), id));
                    break;
                }
                case Rotation: {
                    float x = buf.readFloat();
                    float y = buf.readFloat();
                    float z = buf.readFloat();
                    entries.add(new Entry(index, new EulerAngle(x, y, z), id));
                    break;
                }
                default:
                    System.out.println("[Out] Unhandled MetaDataType: " + type);
                    break;
            }
        }
        return entries;
    }

    @Getter
    @Setter
    public static final class Entry {

        private final int oldID;
        private MetaIndex index;
        private Object value;

        private Entry(MetaIndex index, Object value, int id) {
            this.index = index;
            this.value = value;
            this.oldID = id;
        }
    }
}

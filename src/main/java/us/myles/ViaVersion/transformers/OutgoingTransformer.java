package us.myles.ViaVersion.transformers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import net.minecraft.server.v1_8_R3.*;
import net.minecraft.server.v1_8_R3.ItemStack;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.inventory.*;
import org.spacehq.mc.protocol.data.game.chunk.Column;
import org.spacehq.mc.protocol.util.NetUtil;
import us.myles.ViaVersion.CancelException;
import us.myles.ViaVersion.ConnectionInfo;
import us.myles.ViaVersion.Core;
import us.myles.ViaVersion.PacketUtil;
import us.myles.ViaVersion.handlers.ViaVersionInitializer;
import us.myles.ViaVersion.metadata.MetaIndex;
import us.myles.ViaVersion.metadata.NewType;
import us.myles.ViaVersion.metadata.Type;
import us.myles.ViaVersion.packets.PacketType;
import us.myles.ViaVersion.packets.State;

import java.io.IOException;
import java.util.*;

public class OutgoingTransformer {
    private static Gson gson = new Gson();
    private final Channel channel;
    private final ConnectionInfo info;
    private final ViaVersionInitializer init;
    private boolean cancel = false;
    private Map<Integer, UUID> uuidMap = new HashMap<Integer, UUID>();

    public OutgoingTransformer(Channel channel, ConnectionInfo info, ViaVersionInitializer init) {
        this.channel = channel;
        this.info = info;
        this.init = init;
    }

    public void transform(int packetID, ByteBuf input, ByteBuf output) throws CancelException {
        if (cancel) {
            throw new CancelException();
        }

        PacketType packet = PacketType.getOutgoingPacket(info.getState(), packetID);

        if (packet == null) {
            throw new RuntimeException("Outgoing Packet not found? " + packetID + " State: " + info.getState() + " Version: " + info.getProtocol());
        }
        if (packet != PacketType.PLAY_CHUNK_DATA && packet != PacketType.PLAY_KEEP_ALIVE && packet != PacketType.PLAY_TIME_UPDATE && !packet.name().toLowerCase().contains("entity"))
            System.out.println("Packet Type: " + packet + " Original ID: " + packetID + " State:" + info.getState());
        if (packet.getPacketID() != -1) {
            packetID = packet.getNewPacketID();
        }

        // By default no transform
        PacketUtil.writeVarInt(packetID, output);
        if (packet == PacketType.PLAY_NAMED_SOUND_EFFECT) {
            // TODO: Port this over
            throw new CancelException();
        }
        if (packet == PacketType.PLAY_ATTACH_ENTITY) {
            int id = input.readInt();
            output.writeInt(id);
            int target = input.readInt();
            output.writeInt(target);
            return;
        }
        if (packet == PacketType.PLAY_ENTITY_TELEPORT) {
            // Port this so that it relative moves :P
            int id = PacketUtil.readVarInt(input);
            PacketUtil.writeVarInt(id, output);

            int x = input.readInt();
            output.writeDouble(x / 32D);
            int y = input.readInt();
            output.writeDouble(y / 32D);
            int z = input.readInt();
            output.writeDouble(z / 32D);

            byte yaw = input.readByte();
            output.writeByte(yaw);
            byte pitch = input.readByte();
            output.writeByte(pitch);

            boolean onGround = input.readBoolean();
            output.writeBoolean(onGround);
            return;

        }
        if (packet == PacketType.PLAY_ENTITY_LOOK_MOVE) {
            int id = PacketUtil.readVarInt(input);
            PacketUtil.writeVarInt(id, output);
            int x = input.readByte();
            output.writeShort(x);
            int y = input.readByte();
            output.writeShort(y);
            int z = input.readByte();
            output.writeShort(z);

            byte yaw = input.readByte();
            output.writeByte(yaw);
            byte pitch = input.readByte();
            output.writeByte(pitch);

            boolean onGround = input.readBoolean();
            output.writeBoolean(onGround);
            return;

        }
        if (packet == PacketType.PLAY_ENTITY_RELATIVE_MOVE) {
            int id = PacketUtil.readVarInt(input);
            PacketUtil.writeVarInt(id, output);
            short x = (short) (input.readByte());
            output.writeShort(x);
            short y = (short) (input.readByte());
            output.writeShort(y);
            short z = (short) (input.readByte());
            output.writeShort(z);

            boolean onGround = input.readBoolean();
            output.writeBoolean(onGround);
            return;

        }
        // If login success
        if (packet == PacketType.LOGIN_SUCCESS) {
            info.setState(State.PLAY);
        }
        if (packet == PacketType.LOGIN_SETCOMPRESSION) {
            int factor = PacketUtil.readVarInt(input);
            info.setCompression(factor);
            PacketUtil.writeVarInt(factor, output);
            return;
        }

        if (packet == PacketType.STATUS_RESPONSE) {
            String original = PacketUtil.readString(input);
            JsonObject object = gson.fromJson(original, JsonObject.class);
            object.get("version").getAsJsonObject().addProperty("protocol", info.getProtocol());
            PacketUtil.writeString(gson.toJson(object), output);
            return;
        }
        if (packet == PacketType.LOGIN_SUCCESS) {
            String uu = PacketUtil.readString(input);
            PacketUtil.writeString(uu, output);
            info.setUUID(UUID.fromString(uu));
            output.writeBytes(input);
            return;
        }

        if (packet == PacketType.PLAY_PLAYER_POSITION_LOOK) {
            output.writeBytes(input);
            PacketUtil.writeVarInt(0, output);
            return;
        }
        if (packet == PacketType.PLAY_ENTITY_EQUIPMENT) {
            int id = PacketUtil.readVarInt(input);
            PacketUtil.writeVarInt(id, output);
            short slot = input.readShort();

            if (slot > 1) {
                slot += 1; // add 1 so it's now 2-5
            }
            PacketUtil.writeVarInt(slot, output);
            output.writeBytes(input);
        }
        if (packet == PacketType.PLAY_ENTITY_METADATA) {
            int id = PacketUtil.readVarInt(input);
            PacketUtil.writeVarInt(id, output);

            try {
                List dw = Core.getPrivateField(info.getLastPacket(), "b", List.class);
                // get entity via entityID, not preferred but we need it.
                Entity entity = Core.getEntity(info.getUUID(), id);
                if (entity != null) {
                    transformMetadata(entity, dw, output);
                } else {
                    // Died before we could get to it. rip
                    throw new CancelException();
                }
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            return;
        }
        if (packet == PacketType.PLAY_SPAWN_OBJECT) {
            int id = PacketUtil.readVarInt(input);
            PacketUtil.writeVarInt(id, output);

            PacketUtil.writeUUID(getUUID(id), output);

            byte type = input.readByte();
            output.writeByte(type);

            double x = input.readInt();
            output.writeDouble(x / 32D);
            double y = input.readInt();
            output.writeDouble(y / 32D);
            double z = input.readInt();
            output.writeDouble(z / 32D);
            byte pitch = input.readByte();
            output.writeByte(pitch);
            byte yaw = input.readByte();
            output.writeByte(yaw);

            int data = input.readInt();
            output.writeInt(data);

            short vX = input.readableBytes() >= 16 ? input.readShort() : 0;
            output.writeShort(vX);
            short vY = input.readableBytes() >= 16 ? input.readShort() : 0;
            output.writeShort(vY);
            short vZ = input.readableBytes() >= 16 ? input.readShort() : 0;
            output.writeShort(vZ);

            return;
        }
        if (packet == PacketType.PLAY_SPAWN_XP_ORB) { // TODO: Verify
            int id = PacketUtil.readVarInt(input);
            PacketUtil.writeVarInt(id, output);

            double x = input.readInt();
            output.writeDouble(x / 32D);
            double y = input.readInt();
            output.writeDouble(y / 32D);
            double z = input.readInt();
            output.writeDouble(z / 32D);

            short data = input.readShort();
            output.writeShort(data);

            return;
        }
        if (packet == PacketType.PLAY_SPAWN_MOB) {
            int id = PacketUtil.readVarInt(input);
            PacketUtil.writeVarInt(id, output);

            PacketUtil.writeUUID(getUUID(id), output);
            short type = input.readUnsignedByte();
            output.writeByte(type);
            double x = input.readInt();
            output.writeDouble(x / 32D);
            double y = input.readInt();
            output.writeDouble(y / 32D);
            double z = input.readInt();
            output.writeDouble(z / 32D);
            byte yaw = input.readByte();
            output.writeByte(yaw);
            byte pitch = input.readByte();
            output.writeByte(pitch);
            byte headPitch = input.readByte();
            output.writeByte(headPitch);

            short vX = input.readShort();
            output.writeShort(vX);
            short vY = input.readShort();
            output.writeShort(vY);
            short vZ = input.readShort();
            output.writeShort(vZ);
            try {
                DataWatcher dw = Core.getPrivateField(info.getLastPacket(), "l", DataWatcher.class);
                transformMetadata(dw, output);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            return;
        }
        if (packet == PacketType.PLAY_SPAWN_PLAYER) {
            int id = PacketUtil.readVarInt(input);
            PacketUtil.writeVarInt(id, output);

            UUID playerUUID = PacketUtil.readUUID(input);
            PacketUtil.writeUUID(playerUUID, output);

            double x = input.readInt();
            output.writeDouble(x / 32D);
            double y = input.readInt();
            output.writeDouble(y / 32D);
            double z = input.readInt();
            output.writeDouble(z / 32D);

            byte pitch = input.readByte();
            output.writeByte(pitch);
            byte yaw = input.readByte();
            output.writeByte(yaw);
            try {
                DataWatcher dw = Core.getPrivateField(info.getLastPacket(), "i", DataWatcher.class);
                transformMetadata(dw, output);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

            return;
        }
        if (packet == PacketType.PLAY_CHUNK_DATA) {
            // We need to catch unloading chunk packets as defined by wiki.vg
            // To unload chunks, send this packet with Ground-Up Continuous=true and no 16^3 chunks (eg. Primary Bit Mask=0)
            int chunkX = input.readInt();
            int chunkZ = input.readInt();
            output.writeInt(chunkX);
            output.writeInt(chunkZ);


            boolean groundUp = input.readBoolean();
            output.writeBoolean(groundUp);

            int bitMask = input.readUnsignedShort();

            if (bitMask == 0) {
                output.clear();
                PacketUtil.writeVarInt(PacketType.PLAY_UNLOAD_CHUNK.getNewPacketID(), output);
                output.writeInt(chunkX);
                output.writeInt(chunkZ);
                return;
            }
            int size = PacketUtil.readVarInt(input);

            byte[] data = new byte[size];
            input.readBytes(data);
            boolean sk = false;
            if (info.getLastPacket() instanceof PacketPlayOutMapChunkBulk) {
                try {
                    sk = Core.getPrivateField(info.getLastPacket(), "d", boolean.class);
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            Column read = NetUtil.readOldChunkData(chunkX, chunkZ, groundUp, bitMask, data, true, sk);
            // Write chunk section array :((
            ByteBuf temp = output.alloc().buffer();
            try {
                int bitmask = NetUtil.writeNewColumn(temp, read, groundUp, sk);
                PacketUtil.writeVarInt(bitmask, output);
                PacketUtil.writeVarInt(temp.readableBytes(), output);
                output.writeBytes(temp);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        output.writeBytes(input);
    }

    private void transformMetadata(DataWatcher dw, ByteBuf output) {
        // get entity
        try {
            transformMetadata(Core.getPrivateField(dw, "a", Entity.class), dw.b(), output);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void transformMetadata(Entity entity, List<DataWatcher.WatchableObject> dw, ByteBuf output) {
        PacketDataSerializer packetdataserializer = new PacketDataSerializer(output);

        if (dw != null) {
            short id = -1;
            int data = -1;

            Iterator<DataWatcher.WatchableObject> iterator = dw.iterator();
            while (iterator.hasNext()) {
                DataWatcher.WatchableObject obj = iterator.next();
                MetaIndex metaIndex = MetaIndex.getIndex(entity, obj.a());
                if (metaIndex.getNewType() != NewType.Discontinued) {
                    if (metaIndex.getNewType() != NewType.BlockID || id != -1 && data == -1 || id == -1 && data != -1) { // block ID is only written if we have both parts
                        output.writeByte(metaIndex.getNewIndex());
                        output.writeByte(metaIndex.getNewType().getTypeID());
                    }
                    switch (metaIndex.getNewType()) {
                        case Byte:
                            // convert from int, byte
                            if (metaIndex.getOldType() == Type.Byte) {
                                packetdataserializer.writeByte(((Byte) obj.b()).byteValue());
                            }
                            if (metaIndex.getOldType() == Type.Int) {
                                packetdataserializer.writeByte(((Integer) obj.b()).byteValue());
                            }
                            break;
                        case OptUUID:
                            String owner = (String) obj.b();
                            UUID toWrite = null;
                            if (owner.length() != 0) {
                                try {
                                    toWrite = UUID.fromString(owner);
                                } catch (Exception ignored) {
                                }
                            }
                            packetdataserializer.writeBoolean(toWrite != null);
                            if (toWrite != null)
                                packetdataserializer.a(toWrite);
                            break;
                        case BlockID:
                            // if we have both sources :))
                            if (metaIndex.getOldType() == Type.Byte) {
                                data = ((Byte) obj.b()).byteValue();
                            }
                            if (metaIndex.getOldType() == Type.Short) {
                                id = ((Short) obj.b()).shortValue();
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
                                PacketUtil.writeVarInt(((Byte) obj.b()).intValue(), output);
                            }
                            if (metaIndex.getOldType() == Type.Short) {
                                PacketUtil.writeVarInt(((Short) obj.b()).intValue(), output);
                            }
                            if (metaIndex.getOldType() == Type.Int) {
                                PacketUtil.writeVarInt(((Integer) obj.b()).intValue(), output);
                            }
                            break;
                        case Float:
                            packetdataserializer.writeFloat(((Float) obj.b()).floatValue());
                            break;
                        case String:
                            packetdataserializer.a((String) obj.b());
                            break;
                        case Boolean:
                            packetdataserializer.writeBoolean(((Byte) obj.b()).byteValue() != 0);
                            break;
                        case Slot:
                            ItemStack itemstack = (ItemStack) obj.b();
                            packetdataserializer.a(itemstack);
                            break;
                        case Position:
                            BlockPosition blockposition = (BlockPosition) obj.b();
                            packetdataserializer.writeInt(blockposition.getX());
                            packetdataserializer.writeInt(blockposition.getY());
                            packetdataserializer.writeInt(blockposition.getZ());
                            break;
                        case Vector3F:
                            Vector3f vector3f = (Vector3f) obj.b();
                            packetdataserializer.writeFloat(vector3f.getX());
                            packetdataserializer.writeFloat(vector3f.getY());
                            packetdataserializer.writeFloat(vector3f.getZ());
                    }
                }
            }
        }
        output.writeByte(255);


    }


    private UUID getUUID(int id) {
        if (uuidMap.containsKey(id)) {
            return uuidMap.get(id);
        } else {
            UUID uuid = UUID.randomUUID();
            uuidMap.put(id, uuid);
            return uuid;
        }
    }

}

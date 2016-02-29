package us.myles.ViaVersion.transformers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.spacehq.mc.protocol.data.game.chunk.Column;
import org.spacehq.mc.protocol.util.NetUtil;
import us.myles.ViaVersion.*;
import us.myles.ViaVersion.handlers.ViaVersionInitializer;
import us.myles.ViaVersion.metadata.MetaIndex;
import us.myles.ViaVersion.metadata.NewType;
import us.myles.ViaVersion.metadata.Type;
import us.myles.ViaVersion.packets.PacketType;
import us.myles.ViaVersion.packets.State;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
//        if (packet != PacketType.PLAY_CHUNK_DATA && packet != PacketType.PLAY_KEEP_ALIVE && packet != PacketType.PLAY_TIME_UPDATE && !packet.name().toLowerCase().contains("entity"))
//            System.out.println("Packet Type: " + packet + " Original ID: " + packetID + " State:" + info.getState());
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
            output.writeShort(x * 128);
            int y = input.readByte();
            output.writeShort(y * 128);
            int z = input.readByte();
            output.writeShort(z * 128);

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
            output.writeShort(x * 128);
            short y = (short) (input.readByte());
            output.writeShort(y * 128);
            short z = (short) (input.readByte());
            output.writeShort(z * 128);

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
                List dw = ReflectionUtil.get(info.getLastPacket(), "b", List.class);
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
                Object dataWatcher = ReflectionUtil.get(info.getLastPacket(), "l", ReflectionUtil.nms("DataWatcher"));
                transformMetadata(dataWatcher, output);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            return;
        }
        if(packet == PacketType.PLAY_UPDATE_SIGN){
            Long location = input.readLong();
            output.writeLong(location);
            for(int i = 0;i<4;i++){
                String line = PacketUtil.readString(input);
                if(line.startsWith("\"")){
                    line = "{\"text\":" + line + "}";
                }
                PacketUtil.writeString(line, output);
            }
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
                Object dataWatcher = ReflectionUtil.get(info.getLastPacket(), "i", ReflectionUtil.nms("DataWatcher"));
                transformMetadata(dataWatcher, output);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            return;
        }
        if(packet == PacketType.PLAY_MAP) {
            int damage = PacketUtil.readVarInt(input);
            PacketUtil.writeVarInt(damage, output);
            byte scale = input.readByte();
            output.writeByte(scale);
            output.writeBoolean(true);
            output.writeBytes(input);
            return;
        }
        if(packet == PacketType.PLAY_TEAM) {
            String teamName = PacketUtil.readString(input);
            PacketUtil.writeString(teamName, output);
            byte mode = input.readByte();
            output.writeByte(mode);
            if(mode == 0 || mode == 2){
                PacketUtil.writeString(PacketUtil.readString(input), output);
                PacketUtil.writeString(PacketUtil.readString(input), output);
                PacketUtil.writeString(PacketUtil.readString(input), output);

                output.writeByte(input.readByte());
                PacketUtil.writeString(PacketUtil.readString(input), output);

                PacketUtil.readString(input); // collission rule :)
            }
            output.writeBytes(input);
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
            if (info.getLastPacket().getClass().getName().endsWith("PacketPlayOutMapChunkBulk")) {
                try {
                    sk = ReflectionUtil.get(info.getLastPacket(), "d", boolean.class);
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

    private void transformMetadata(Object dw, ByteBuf output) {
        // get entity
        try {
            Class<?> nmsClass = ReflectionUtil.nms("Entity");
            Object nmsEntity = ReflectionUtil.get(dw, "a", nmsClass);
            Class<?> craftClass = ReflectionUtil.obc("entity.CraftEntity");
            Method bukkitMethod = craftClass.getDeclaredMethod("getEntity", ReflectionUtil.obc("CraftServer"), nmsClass);

            Object entity = bukkitMethod.invoke(null, Bukkit.getServer(), nmsEntity);
            transformMetadata((Entity) entity, (List) ReflectionUtil.invoke(dw, "b"), output);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void transformMetadata(Entity entity, List dw, ByteBuf output) {
        try {
            if (dw != null) {
                short id = -1;
                int data = -1;

                Iterator iterator = dw.iterator();
                while (iterator.hasNext()) {
                    Object watchableObj = iterator.next(); //
                    MetaIndex metaIndex = MetaIndex.getIndex(entity, (int) ReflectionUtil.invoke(watchableObj, "a"));
                    if (metaIndex.getNewType() != NewType.Discontinued) {
                        if (metaIndex.getNewType() != NewType.BlockID || id != -1 && data == -1 || id == -1 && data != -1) { // block ID is only written if we have both parts
                            output.writeByte(metaIndex.getNewIndex());
                            output.writeByte(metaIndex.getNewType().getTypeID());
                        }
                        Object value = ReflectionUtil.invoke(watchableObj, "b");
                        switch (metaIndex.getNewType()) {
                            case Byte:
                                // convert from int, byte
                                if (metaIndex.getOldType() == Type.Byte) {
                                    output.writeByte(((Byte) value).byteValue());
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
                                    PacketUtil.writeUUID((UUID) value, output);
                                break;
                            case BlockID:
                                // if we have both sources :))
                                if (metaIndex.getOldType() == Type.Byte) {
                                    data = ((Byte) value).byteValue();
                                }
                                if (metaIndex.getOldType() == Type.Short) {
                                    id = ((Short) value).shortValue();
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
                                    PacketUtil.writeVarInt(((Integer) value).intValue(), output);
                                }
                                break;
                            case Float:
                                output.writeFloat(((Float) value).floatValue());
                                break;
                            case String:
                                PacketUtil.writeString((String) value, output);
                                break;
                            case Boolean:
                                output.writeBoolean(((Byte) value).byteValue() != 0);
                                break;
                            case Slot:
                                PacketUtil.writeItem(value, output);
                                break;
                            case Position:
                                output.writeInt((int) ReflectionUtil.invoke(value, "getX"));
                                output.writeInt((int) ReflectionUtil.invoke(value, "getY"));
                                output.writeInt((int) ReflectionUtil.invoke(value, "getZ"));
                                break;
                            case Vector3F:
                                output.writeFloat((float) ReflectionUtil.invoke(value, "getX"));
                                output.writeFloat((float) ReflectionUtil.invoke(value, "getY"));
                                output.writeFloat((float) ReflectionUtil.invoke(value, "getZ"));
                        }
                    }
                }
            }
            output.writeByte(255);
        }catch(Exception e){
            e.printStackTrace();
        }

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

package us.myles.ViaVersion.transformers;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;

import org.bukkit.entity.EntityType;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.spacehq.mc.protocol.data.game.chunk.Column;
import org.spacehq.mc.protocol.util.NetUtil;
import org.spacehq.opennbt.NBTIO;
import org.spacehq.opennbt.tag.builtin.CompoundTag;
import org.spacehq.opennbt.tag.builtin.StringTag;

import us.myles.ViaVersion.CancelException;
import us.myles.ViaVersion.ConnectionInfo;
import us.myles.ViaVersion.ViaVersionPlugin;
import us.myles.ViaVersion.api.ViaVersion;
import us.myles.ViaVersion.metadata.MetadataRewriter;
import us.myles.ViaVersion.packets.PacketType;
import us.myles.ViaVersion.packets.State;
import us.myles.ViaVersion.slot.ItemSlotRewriter;
import us.myles.ViaVersion.sounds.SoundEffect;
import us.myles.ViaVersion.util.EntityUtil;
import us.myles.ViaVersion.util.PacketUtil;
import us.myles.ViaVersion.util.ReflectionUtil;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

import static us.myles.ViaVersion.util.PacketUtil.*;

public class OutgoingTransformer {
    private final ConnectionInfo info;
    private final ViaVersionPlugin plugin = (ViaVersionPlugin) ViaVersion.getInstance();
    private boolean cancel = false;

    private Map<Integer, UUID> uuidMap = new HashMap<Integer, UUID>();
    private Map<Integer, EntityType> clientEntityTypes = new HashMap<Integer, EntityType>();
    private Map<Integer, Integer> vehicleMap = new HashMap<>();

    public OutgoingTransformer(ConnectionInfo info) {
        this.info = info;
    }

    public void transform(int packetID, ByteBuf input, ByteBuf output) throws CancelException {
        if (cancel) {
            throw new CancelException();
        }

        PacketType packet = PacketType.getOutgoingPacket(info.getState(), packetID);

        if (packet == null) {
            throw new RuntimeException("Outgoing Packet not found? " + packetID + " State: " + info.getState() + " Version: " + info.getProtocol());
        }
//        if (packet != PacketType.PLAY_CHUNK_DATA && packet != PacketType.PLAY_KEEP_ALIVE && packet != PacketType.PLAY_TIME_UPDATE && (!packet.name().toLowerCase().contains("move") && !packet.name().toLowerCase().contains("look")))
//            System.out.println("Packet Type: " + packet + " Original ID: " + packetID + " State:" + info.getState());
        if (packet.getPacketID() != -1) {
            packetID = packet.getNewPacketID();
        }

        // By default no transform
        PacketUtil.writeVarInt(packetID, output);
        if (packet == PacketType.PLAY_NAMED_SOUND_EFFECT) {
            String name = PacketUtil.readString(input);

            SoundEffect effect = SoundEffect.getByName(name);
            int catid = 0;
            String newname = name;
            if (effect != null) {
                if (effect.isBreakPlaceSound()) {
                    throw new CancelException();
                }
                catid = effect.getCategory().getId();
                newname = effect.getNewName();
            }
            PacketUtil.writeString(newname, output);
            PacketUtil.writeVarInt(catid, output);
            output.writeBytes(input);
        }
        if (packet == PacketType.PLAY_ATTACH_ENTITY) {
            int passenger = input.readInt();
            int vehicle = input.readInt();
            boolean lead = input.readBoolean();
            if (!lead) {
                output.clear();
                writeVarInt(PacketType.PLAY_SET_PASSENGERS.getNewPacketID(), output);
                if (vehicle == -1) {
                    if (!vehicleMap.containsKey(passenger))
                        throw new CancelException();
                    vehicle = vehicleMap.remove(passenger);
                    writeVarInt(vehicle, output);
                    writeVarIntArray(Collections.<Integer>emptyList(), output);
                } else {
                    writeVarInt(vehicle, output);
                    writeVarIntArray(Collections.singletonList(passenger), output);
                    vehicleMap.put(passenger, vehicle);
                }
                return;
            }
            output.writeInt(passenger);
            output.writeInt(vehicle);
            return;
        }
        if (packet == PacketType.PLAY_PLUGIN_MESSAGE) {
            String name = PacketUtil.readString(input);
            PacketUtil.writeString(name, output);
            byte[] b = new byte[input.readableBytes()];
            input.readBytes(b);
            // patch books
            if (name.equals("MC|BOpen")) {
                PacketUtil.writeVarInt(0, output);
            }
            output.writeBytes(b);
        }
        if (packet == PacketType.PLAY_DISCONNECT) {
            String reason = readString(input);
            writeString(fixJson(reason), output);
            return;
        }
        if (packet == PacketType.PLAY_TITLE) {
            int action = PacketUtil.readVarInt(input);
            PacketUtil.writeVarInt(action, output);
            if (action == 0 || action == 1) {
                String text = PacketUtil.readString(input);
                PacketUtil.writeString(fixJson(text), output);
            }
            output.writeBytes(input);
            return;
        }
        if (packet == PacketType.PLAY_PLAYER_LIST_ITEM) {
            int action = readVarInt(input);
            writeVarInt(action, output);
            int players = readVarInt(input);
            writeVarInt(players, output);

            // loop through players
            for (int i = 0; i < players; i++) {
                UUID uuid = readUUID(input);
                writeUUID(uuid, output);
                if (action == 0) { // add player
                    writeString(readString(input), output); // name

                    int properties = readVarInt(input);
                    writeVarInt(properties, output);

                    // loop through properties
                    for (int j = 0; j < properties; j++) {
                        writeString(readString(input), output); // name
                        writeString(readString(input), output); // value
                        boolean isSigned = input.readBoolean();
                        output.writeBoolean(isSigned);
                        if (isSigned) {
                            writeString(readString(input), output); // signature
                        }
                    }

                    writeVarInt(readVarInt(input), output); // gamemode
                    writeVarInt(readVarInt(input), output); // ping
                    boolean hasDisplayName = input.readBoolean();
                    output.writeBoolean(hasDisplayName);
                    if (hasDisplayName) {
                        writeString(fixJson(readString(input)), output); // display name
                    }
                } else if ((action == 1) || (action == 2)) { // update gamemode || update latency
                    writeVarInt(readVarInt(input), output);
                } else if (action == 3) { // update display name
                    boolean hasDisplayName = input.readBoolean();
                    output.writeBoolean(hasDisplayName);
                    if (hasDisplayName) {
                        writeString(fixJson(readString(input)), output); // display name
                    }
                } else if (action == 4) { // remove player
                    // no fields
                }
            }

            return;
        }
        if (packet == PacketType.PLAY_PLAYER_LIST_HEADER_FOOTER) {
            String header = readString(input);
            String footer = readString(input);
            writeString(fixJson(header), output);
            writeString(fixJson(footer), output);
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
            try {
                JSONObject json = (JSONObject) new JSONParser().parse(original);
                JSONObject version = (JSONObject) json.get("version");
                version.put("protocol", info.getProtocol());
                PacketUtil.writeString(json.toJSONString(), output);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return;
        }
        if (packet == PacketType.LOGIN_SUCCESS) {
            String uu = PacketUtil.readString(input);
            PacketUtil.writeString(uu, output);
            UUID uniqueId = UUID.fromString(uu);
            info.setUUID(uniqueId);
            plugin.addPortedClient(info);
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

            if (slot > 0) {
                slot += 1; // add 1 so it's now 2-5
            }
            PacketUtil.writeVarInt(slot, output);

            ItemSlotRewriter.rewrite1_8To1_9(input, output);
            return;
        }
        if (packet == PacketType.PLAY_ENTITY_METADATA) {
            int id = PacketUtil.readVarInt(input);
            PacketUtil.writeVarInt(id, output);

            transformMetadata(id, input, output);
            return;
        }

        if (packet == PacketType.PLAY_SPAWN_GLOBAL_ENTITY) {
            int id = PacketUtil.readVarInt(input);
            PacketUtil.writeVarInt(id, output);

            // only used for lightning
            byte type = input.readByte();
            clientEntityTypes.put(id, EntityType.LIGHTNING);
            output.writeByte(type);

            double x = input.readInt();
            output.writeDouble(x / 32D);
            double y = input.readInt();
            output.writeDouble(y / 32D);
            double z = input.readInt();
            output.writeDouble(z / 32D);
            return;
        }
        if (packet == PacketType.PLAY_DESTROY_ENTITIES) {
            int count = PacketUtil.readVarInt(input);
            PacketUtil.writeVarInt(count, output);

            int[] toDestroy = PacketUtil.readVarInts(count, input);
            for (int entityID : toDestroy) {
                clientEntityTypes.remove(entityID);
                PacketUtil.writeVarInt(entityID, output);
            }
            return;
        }
        if (packet == PacketType.PLAY_SPAWN_OBJECT) {
            int id = PacketUtil.readVarInt(input);
            PacketUtil.writeVarInt(id, output);
            PacketUtil.writeUUID(getUUID(id), output);

            byte type = input.readByte();
            clientEntityTypes.put(id, EntityUtil.getTypeFromID(type, true));
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

            short vX = 0, vY = 0, vZ = 0;
            if (data > 0) {
                vX = input.readShort();
                vY = input.readShort();
                vZ = input.readShort();
            }
            output.writeShort(vX);
            output.writeShort(vY);
            output.writeShort(vZ);

            return;
        }
        if (packet == PacketType.PLAY_SPAWN_XP_ORB) {
            int id = PacketUtil.readVarInt(input);
            clientEntityTypes.put(id, EntityType.EXPERIENCE_ORB);
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
        if (packet == PacketType.PLAY_SPAWN_PAINTING) {
            int id = PacketUtil.readVarInt(input);
            clientEntityTypes.put(id, EntityType.PAINTING);
            PacketUtil.writeVarInt(id, output);

            PacketUtil.writeUUID(getUUID(id), output);
            String title = PacketUtil.readString(input);
            PacketUtil.writeString(title, output);

            long[] position = PacketUtil.readBlockPosition(input);
            PacketUtil.writeBlockPosition(output, position[0], position[1], position[2]);

            byte direction = input.readByte();
            output.writeByte(direction);

            return;
        }
        if (packet == PacketType.PLAY_OPEN_WINDOW) {
            int windowId = input.readUnsignedByte();
            String type = readString(input);
            String windowTitle = readString(input);

            output.writeByte(windowId);
            writeString(type, output);
            writeString(fixJson(windowTitle), output);
            output.writeBytes(input);
            return;
        }
        if (packet == PacketType.PLAY_SET_SLOT) {
            int windowId = input.readUnsignedByte();
            output.writeByte(windowId);

            short slot = input.readShort();
            output.writeShort(slot);
            ItemSlotRewriter.rewrite1_8To1_9(input, output);
            return;
        }
        if (packet == PacketType.PLAY_WINDOW_ITEMS) {
            int windowId = input.readUnsignedByte();
            output.writeByte(windowId);

            short count = input.readShort();
            output.writeShort(count);

            for (int i = 0; i < count; i++) {
                ItemSlotRewriter.rewrite1_8To1_9(input, output);
            }
            return;
        }
        if (packet == PacketType.PLAY_SPAWN_MOB) {
            int id = PacketUtil.readVarInt(input);
            PacketUtil.writeVarInt(id, output);

            PacketUtil.writeUUID(getUUID(id), output);
            short type = input.readUnsignedByte();
            clientEntityTypes.put(id, EntityUtil.getTypeFromID(type, false));
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

            transformMetadata(id, input, output);
            return;
        }
        if (packet == PacketType.PLAY_UPDATE_SIGN) {
            Long location = input.readLong();
            output.writeLong(location);
            for (int i = 0; i < 4; i++) {
                String line = PacketUtil.readString(input);
                PacketUtil.writeString(fixJson(line), output);
            }
        }
        if (packet == PacketType.PLAY_CHAT_MESSAGE) {
            String chat = PacketUtil.readString(input);
            PacketUtil.writeString(fixJson(chat), output);

            byte pos = input.readByte();
            output.writeByte(pos);
            return;
        }
        if (packet == PacketType.PLAY_JOIN_GAME) {
            int id = input.readInt();
            clientEntityTypes.put(id, EntityType.PLAYER);
            output.writeInt(id);
            output.writeBytes(input);
            return;
        }
        if (packet == PacketType.PLAY_SPAWN_PLAYER) {
            int id = PacketUtil.readVarInt(input);
            PacketUtil.writeVarInt(id, output);
            clientEntityTypes.put(id, EntityType.PLAYER);
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

            // next field is Current Item, this was removed in 1.9 so we'll ignore it
            input.readShort();

            transformMetadata(id, input, output);

            return;
        }
        if (packet == PacketType.PLAY_MAP) {
            int damage = PacketUtil.readVarInt(input);
            PacketUtil.writeVarInt(damage, output);
            byte scale = input.readByte();
            output.writeByte(scale);
            output.writeBoolean(true);
            output.writeBytes(input);
            return;
        }
        if (packet == PacketType.PLAY_ENTITY_EFFECT) {
            int id = PacketUtil.readVarInt(input);
            PacketUtil.writeVarInt(id, output);
            byte effectID = input.readByte();
            output.writeByte(effectID);
            byte amplifier = input.readByte();
            output.writeByte(amplifier);
            int duration = PacketUtil.readVarInt(input);
            PacketUtil.writeVarInt(duration, output);
            // we need to write as a byte instead of boolean
            boolean hideParticles = input.readBoolean();
            output.writeByte(hideParticles ? 1 : 0);
            return;
        }
        if (packet == PacketType.PLAY_TEAM) {
            String teamName = PacketUtil.readString(input);
            PacketUtil.writeString(teamName, output);
            byte mode = input.readByte();
            output.writeByte(mode);
            if (mode == 0 || mode == 2) {
                PacketUtil.writeString(PacketUtil.readString(input), output);
                PacketUtil.writeString(PacketUtil.readString(input), output);
                PacketUtil.writeString(PacketUtil.readString(input), output);

                output.writeByte(input.readByte());
                PacketUtil.writeString(PacketUtil.readString(input), output);

                PacketUtil.writeString("", output); // collission rule :)
            }
            output.writeBytes(input);
            return;
        }
        if (packet == PacketType.PLAY_UPDATE_BLOCK_ENTITY) {
        	long[] pos = PacketUtil.readBlockPosition(input);
        	int action = input.readUnsignedByte();
        	if(action == 1) { // update spawner
        		try {
        			DataInputStream stream = new DataInputStream(new ByteBufInputStream(input));
					CompoundTag tag = (CompoundTag) NBTIO.readTag(stream);
					String entity = (String) tag.get("EntityId").getValue();
					CompoundTag spawn = new CompoundTag("SpawnData");
					spawn.put(new StringTag("id", entity));
					tag.put(spawn);
					PacketUtil.writeBlockPosition(output, pos[0], pos[1], pos[2]);
					output.writeByte(action);
					DataOutputStream out = new DataOutputStream(new ByteBufOutputStream(output));
					NBTIO.writeTag(out, tag);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        		return;
        	}
        	PacketUtil.writeBlockPosition(output, pos[0], pos[1], pos[2]);
			output.writeByte(action);
			output.writeBytes(input, input.readableBytes());
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
            int size = PacketUtil.readVarInt(input);
            byte[] data = new byte[size];
            input.readBytes(data);
//            if (bitMask == 0 && groundUp) {
//                // if 256
//                output.clear();
//                PacketUtil.writeVarInt(PacketType.PLAY_UNLOAD_CHUNK.getNewPacketID(), output);
//                output.writeInt(chunkX);
//                output.writeInt(chunkZ);
//                System.out.println("Sending unload chunk " + chunkX + " " + chunkZ + " - " + size + " bulk: " + bulk);
//                return;
//            }
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

    public static String fixJson(String line) {
        if (line == null || line.equalsIgnoreCase("null")) {
            line = "{\"text\":\"\"}";
        } else {
            if (!line.startsWith("\"") && !line.startsWith("{"))
                line = "\"" + line + "\"";
            if (line.startsWith("\""))
                line = "{\"text\":" + line + "}";
        }
        try {
            new JSONParser().parse(line);
        } catch (Exception e) {
            System.out.println("Invalid JSON String: \"" + line + "\" Please report this issue to the ViaVersion Github: " + e.getMessage());
            return "{\"text\":\"\"}";
        }
        return line;
    }

    private void transformMetadata(int entityID, ByteBuf input, ByteBuf output) throws CancelException {
        EntityType type = clientEntityTypes.get(entityID);
        if (type == null) {
            System.out.println("Unable to get entity for ID: " + entityID);
            output.writeByte(255);
            return;
        }
        List<MetadataRewriter.Entry> list = MetadataRewriter.readMetadata1_8(type, input);
        MetadataRewriter.writeMetadata1_9(type, list, output);
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

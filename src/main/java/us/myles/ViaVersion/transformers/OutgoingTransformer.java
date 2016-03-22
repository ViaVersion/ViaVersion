package us.myles.ViaVersion.transformers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.spacehq.opennbt.tag.builtin.CompoundTag;
import org.spacehq.opennbt.tag.builtin.StringTag;
import us.myles.ViaVersion.CancelException;
import us.myles.ViaVersion.ConnectionInfo;
import us.myles.ViaVersion.ViaVersionPlugin;
import us.myles.ViaVersion.api.ViaVersion;
import us.myles.ViaVersion.api.boss.BossBar;
import us.myles.ViaVersion.api.boss.BossColor;
import us.myles.ViaVersion.api.boss.BossStyle;
import us.myles.ViaVersion.chunks.Chunk;
import us.myles.ViaVersion.chunks.ChunkManager;
import us.myles.ViaVersion.metadata.MetaIndex;
import us.myles.ViaVersion.metadata.MetadataRewriter;
import us.myles.ViaVersion.metadata.MetadataRewriter.Entry;
import us.myles.ViaVersion.packets.PacketType;
import us.myles.ViaVersion.packets.State;
import us.myles.ViaVersion.slot.ItemSlotRewriter;
import us.myles.ViaVersion.sounds.SoundEffect;
import us.myles.ViaVersion.util.EntityUtil;
import us.myles.ViaVersion.util.PacketUtil;

import java.io.IOException;
import java.util.*;

import static us.myles.ViaVersion.util.PacketUtil.*;


public class OutgoingTransformer {
    private static Gson gson = new GsonBuilder().create();

    private final ViaVersionPlugin plugin = (ViaVersionPlugin) ViaVersion.getInstance();

    private final ConnectionInfo info;
    private final Map<Integer, UUID> uuidMap = new HashMap<>();
    private final Map<Integer, EntityType> clientEntityTypes = new HashMap<>();
    private final Map<Integer, Integer> vehicleMap = new HashMap<>();
    private final Set<Integer> validBlocking = new HashSet<>();
    private final Set<Integer> knownHolograms = new HashSet<>();
    private final Map<Integer, BossBar> bossBarMap = new HashMap<>();
    private boolean autoTeam = false;
    private boolean teamExists = false;

    public OutgoingTransformer(ConnectionInfo info) {
        this.info = info;
    }

    public static String fixJson(String line) {
        if (line == null || line.equalsIgnoreCase("null")) {
            line = "{\"text\":\"\"}";
        } else {
            if ((!line.startsWith("\"") || !line.endsWith("\"")) && (!line.startsWith("{") || !line.endsWith("}"))) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("text", line);
                return gson.toJson(jsonObject);
            }
            if (line.startsWith("\"") && line.endsWith("\"")) {
                line = "{\"text\":" + line + "}";
            }
        }
        try {
            gson.fromJson(line, JsonObject.class);
        } catch (Exception e) {
            System.out.println("Invalid JSON String: \"" + line + "\" Please report this issue to the ViaVersion Github: " + e.getMessage());
            return "{\"text\":\"\"}";
        }
        return line;
    }

    public void transform(int packetID, ByteBuf input, ByteBuf output) throws CancelException {
        PacketType packet = PacketType.getOutgoingPacket(info.getState(), packetID);
        int original = packetID;
        if (packet == null) {
            throw new RuntimeException("Outgoing Packet not found? " + packetID + " State: " + info.getState() + " Version: " + info.getProtocol());
        }
        if (packet.getPacketID() != -1) {
            packetID = packet.getNewPacketID();
        }
        if (ViaVersion.getInstance().isDebug()) {
            if (packet != PacketType.PLAY_CHUNK_DATA && packet != PacketType.PLAY_KEEP_ALIVE && packet != PacketType.PLAY_TIME_UPDATE && (!packet.name().toLowerCase().contains("move") && !packet.name().toLowerCase().contains("look"))) {
                System.out.println("Direction " + packet.getDirection().name() + " Packet Type: " + packet + " New ID: " + packetID + " Original: " + original + " Size: " + input.readableBytes());
            }
        }

        // By default no transform
        PacketUtil.writeVarInt(packetID, output);
        if (packet == PacketType.PLAY_NAMED_SOUND_EFFECT) {
            String name = PacketUtil.readString(input);

            SoundEffect effect = SoundEffect.getByName(name);
            int catid = 0;
            String newname = name;
            if (effect != null) {
                if (effect.isBreaksound()) {
                    throw new CancelException();
                }
                catid = effect.getCategory().getId();
                newname = effect.getNewName();
            }
            PacketUtil.writeString(newname, output);
            PacketUtil.writeVarInt(catid, output);
            output.writeBytes(input);
        }
        if (packet == PacketType.PLAY_EFFECT) {
            int effectid = input.readInt();
            if (effectid >= 1000 && effectid < 2000 && effectid != 1005) //Sound effect
                throw new CancelException();
            if (effectid == 1005) //Fix jukebox
                effectid = 1010;
            output.writeInt(effectid);
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
            int id = PacketUtil.readVarInt(input);
            PacketUtil.writeVarInt(id, output);

            int x = input.readInt();
            output.writeDouble(x / 32D);
            int y = input.readInt();
            if (plugin.isHologramPatch() & knownHolograms.contains(id)) {
                y = (int) ((y) + (plugin.getHologramYOffset() * 32D));
            }
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
        if (packet == PacketType.LOGIN_SETCOMPRESSION) {
            int factor = PacketUtil.readVarInt(input);
            info.setCompression(factor);
            PacketUtil.writeVarInt(factor, output);
            return;
        }

        if (packet == PacketType.STATUS_RESPONSE) {
            String originalStatus = PacketUtil.readString(input);
            try {
                JsonObject jsonObject = gson.fromJson(originalStatus, JsonObject.class);
                JsonObject version = jsonObject.get("version").getAsJsonObject();
                if (version.get("protocol").getAsInt() != 9999) //Fix ServerListPlus custom outdated message
                    version.addProperty("protocol", info.getProtocol());
                PacketUtil.writeString(gson.toJson(jsonObject), output);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
        if (packet == PacketType.LOGIN_SUCCESS) {
            info.setState(State.PLAY);

            String uuid = PacketUtil.readString(input);
            PacketUtil.writeString(uuid, output);
            UUID uniqueId = UUID.fromString(uuid);
            info.setUUID(uniqueId);
            plugin.addPortedClient(info);
            String username = PacketUtil.readString(input);
            info.setUsername(username);
            PacketUtil.writeString(username, output);
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
            if (slot == 0) {
                // Read aheat for sword
                input.markReaderIndex();
                short itemID = input.readShort();
                if (itemID != -1) {
                    Material m = Material.getMaterial(itemID);
                    if (m != null) {
                        if (m.name().endsWith("SWORD")) {
                            validBlocking.add(id);
                        } else {
                            validBlocking.remove(id);
                        }
                    }
                } else {
                    validBlocking.remove(id);
                }
                input.resetReaderIndex();
            }
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
                knownHolograms.remove(entityID);
                PacketUtil.writeVarInt(entityID, output);

                // Remvoe boss bar
                BossBar bar = bossBarMap.remove(entityID);
                if (bar != null) {
                    bar.hide();
                }
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
        if (packet == PacketType.PLAY_WINDOW_PROPERTY) {
            int windowId = input.readUnsignedByte();
            output.writeByte(windowId);
            short property = input.readShort();
            short value = input.readShort();

            if (info.getOpenWindow() != null) {
                if (info.getOpenWindow().equalsIgnoreCase("minecraft:enchanting_table")) {
                    if (property > 3 && property < 7) {
                        short level = (short) (value >> 8);
                        short enchantID = (short) (value & 0xFF);
                        // Property 1
                        ByteBuf buf1 = info.getChannel().alloc().buffer();
                        PacketUtil.writeVarInt(PacketType.PLAY_WINDOW_PROPERTY.getNewPacketID(), buf1);
                        buf1.writeByte(windowId);
                        buf1.writeShort(property);
                        buf1.writeShort(enchantID);

                        info.sendRawPacket(buf1);

                        property = (short) (property + 3);
                        value = level;
                    }
                }
            }
            output.writeShort(property);
            output.writeShort(value);
        }
        if (packet == PacketType.PLAY_OPEN_WINDOW) {
            int windowId = input.readUnsignedByte();
            String type = readString(input);
            info.setOpenWindow(type);
            String windowTitle = readString(input);

            output.writeByte(windowId);
            writeString(type, output);
            writeString(fixJson(windowTitle), output);
            int slots = input.readUnsignedByte();
            if (type.equals("minecraft:brewing_stand")) {
                slots = slots + 1; // new slot
            }
            output.writeByte(slots);
            output.writeBytes(input);
            return;
        }
        if (packet == PacketType.PLAY_CLOSE_WINDOW) {
            info.closeWindow();
        }
        if (packet == PacketType.PLAY_SET_SLOT) {
            int windowId = input.readUnsignedByte();
            output.writeByte(windowId);

            short slot = input.readShort();
            if (info.getOpenWindow() != null) {
                if (info.getOpenWindow().equals("minecraft:brewing_stand")) {
                    if (slot >= 4)
                        slot = (short) (slot + 1);
                }
            }
            output.writeShort(slot);
            ItemSlotRewriter.rewrite1_8To1_9(input, output);
            return;
        }
        if (packet == PacketType.PLAY_WINDOW_ITEMS) {
            int windowId = input.readUnsignedByte();
            output.writeByte(windowId);

            short count = input.readShort();
            boolean brewing = false;
            if (info.getOpenWindow() != null && windowId > 0) {
                if (info.getOpenWindow().equals("minecraft:brewing_stand")) {
                    brewing = true;
                }
            }
            output.writeShort(brewing ? (count + 1) : count);

            for (int i = 0; i < count; i++) {

                ItemSlotRewriter.rewrite1_8To1_9(input, output);

                // write "fuel" slot
                if (brewing && i == 3) {
                    output.writeShort(-1); // empty slot
                }
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
            info.setEntityID(id);
            output.writeInt(id);

            int gamemode = input.readUnsignedByte();
            output.writeByte(gamemode);
            int dimension = input.readByte();
            output.writeByte(dimension);
            int difficulty = input.readUnsignedByte();
            if (info.getProtocol() >= 108) {
                // 1.8.1 Pre 2
                output.writeInt(difficulty);
            } else {
                output.writeByte(difficulty);
            }
            int maxPlayers = input.readUnsignedByte();
            output.writeByte(maxPlayers);
            String level = PacketUtil.readString(input);
            PacketUtil.writeString(level, output);
            boolean reducedDebug = input.readBoolean();
            output.writeBoolean(reducedDebug);
            return;
        }
        if (packet == PacketType.PLAY_SERVER_DIFFICULTY) {
            if (plugin.isAutoTeam()) {
                autoTeam = true;
                sendTeamPacket(true);
            }
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
            output.writeByte(hideParticles ? plugin.isNewEffectIndicator() ? 2 : 1 : 0);
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

                PacketUtil.writeString(plugin.isPreventCollision() ? "never" : "", output); // collission rule :)

                output.writeByte(input.readByte());
            }
            if (mode == 0 || mode == 3 || mode == 4) {
                // add players
                int count = PacketUtil.readVarInt(input);
                PacketUtil.writeVarInt(count, output);

                for (int i = 0; i < count; i++) {
                    String name = PacketUtil.readString(input);
                    if (autoTeam && name.equalsIgnoreCase(info.getUsername())) {
                        if (mode == 4) {
                            // since removing add to auto team
                            plugin.run(new Runnable() {
                                @Override
                                public void run() {
                                    sendTeamPacket(true);
                                }
                            }, false);
                        } else {
                            // since adding remove from auto team
                            sendTeamPacket(false);
                        }
                    }
                    PacketUtil.writeString(name, output);
                }
            }
            output.writeBytes(input);
            return;
        }
        if (packet == PacketType.PLAY_UPDATE_BLOCK_ENTITY) {
            long[] pos = PacketUtil.readBlockPosition(input);
            PacketUtil.writeBlockPosition(output, pos[0], pos[1], pos[2]);
            int action = input.readUnsignedByte();
            output.writeByte(action);
            if (action == 1) { // update spawner
                try {
                    int index = input.readerIndex();
                    CompoundTag tag = PacketUtil.readNBT(input);
                    if (tag != null && tag.contains("EntityId")) {
                        String entity = (String) tag.get("EntityId").getValue();
                        CompoundTag spawn = new CompoundTag("SpawnData");
                        spawn.put(new StringTag("id", entity));
                        tag.put(spawn);
                        PacketUtil.writeNBT(output, tag);
                    } else if (tag != null) { // EntityID does not exist
                        CompoundTag spawn = new CompoundTag("SpawnData");
                        spawn.put(new StringTag("id", "AreaEffectCloud")); //Make spawners show up as empty when no EntityId is given.
                        tag.put(spawn);
                        PacketUtil.writeNBT(output, spawn);
                    } else { //There doesn't exist any NBT tag
                        input.readerIndex(index);
                        output.writeBytes(input, input.readableBytes());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }
            if (action == 2) { //Update commandblock
                throw new CancelException(); //Only update if player interact with commandblock (The commandblock window will update every time this packet is sent, this would prevent you from change things that update every tick)
            }
            output.writeBytes(input, input.readableBytes());
            return;
        }
        if (packet == PacketType.PLAY_CHUNK_DATA) {
            // Read chunk
            ChunkManager chunkManager = info.getChunkManager();
            Chunk chunk = chunkManager.readChunk(input);
            if (chunk == null) {
                throw new CancelException();
            }

            // Write chunk
            chunkManager.writeChunk(chunk, output);
            return;
        }
        output.writeBytes(input);
    }

    private void sendTeamPacket(boolean b) {
        ByteBuf buf = info.getChannel().alloc().buffer();
        PacketUtil.writeVarInt(PacketType.PLAY_TEAM.getNewPacketID(), buf);
        PacketUtil.writeString("viaversion", buf); // Use viaversion as name
        if (b) {
            // add
            if (!teamExists) {
                buf.writeByte(0); // make team
                PacketUtil.writeString("viaversion", buf);
                PacketUtil.writeString("", buf); // prefix
                PacketUtil.writeString("", buf); // suffix
                buf.writeByte(0); // friendly fire
                PacketUtil.writeString("", buf); // nametags
                PacketUtil.writeString("never", buf); // collision rule :)
                buf.writeByte(0); // color
            } else
                buf.writeByte(3);
            PacketUtil.writeVarInt(1, buf); // player count
            PacketUtil.writeString(info.getUsername(), buf); // us
        } else {
            buf.writeByte(1); // remove team
        }
        teamExists = b;
        info.sendRawPacket(buf);
    }

    private void transformMetadata(int entityID, ByteBuf input, ByteBuf output) throws CancelException {
        EntityType type = clientEntityTypes.get(entityID);
        if (type == null) {
            System.out.println("Unable to get entity for ID: " + entityID);
            output.writeByte(255);
            return;
        }
        List<MetadataRewriter.Entry> list = MetadataRewriter.readMetadata1_8(type, input);
        for (MetadataRewriter.Entry entry : list) {
            handleMetadata(entityID, entry, type);
        }
        // Fix: wither (crash fix)
        if (type == EntityType.WITHER) {
            // Remove custom value if already exist
            Iterator<Entry> it = list.iterator();
            while (it.hasNext()) {
                Entry e = it.next();
                if (e.getOldID() == 10) {
                    it.remove();
                }
            }

            list.add(new Entry(MetaIndex.WITHER_PROPERTIES, (byte) 0, 10));
        }
        // Fix: Dragon (crash fix)
        if (type == EntityType.ENDER_DRAGON) {
            // Remove custom value if already exist
            Iterator<Entry> it = list.iterator();
            while (it.hasNext()) {
                Entry e = it.next();
                if (e.getOldID() == 11) {
                    it.remove();
                }
            }

            list.add(new Entry(MetaIndex.ENDERDRAGON_PHASE, (byte) 0, 11));
        }
        MetadataRewriter.writeMetadata1_9(type, list, output);
    }

    private void handleMetadata(int entityID, MetadataRewriter.Entry entry, EntityType type) {
        // This handles old IDs
        if (type == EntityType.PLAYER) {
            if (entry.getOldID() == 0) {
                // Byte
                byte data = (byte) entry.getValue();
                if (entityID != info.getEntityID() && plugin.isShieldBlocking()) {
                    if ((data & 0x10) == 0x10) {
                        if (validBlocking.contains(entityID)) {
                            ItemSlotRewriter.ItemStack shield = new ItemSlotRewriter.ItemStack();
                            shield.id = 442;
                            shield.amount = 1;
                            shield.data = 0;
                            sendSecondHandItem(entityID, shield);
                        }
                    } else {
                        sendSecondHandItem(entityID, null);
                    }
                }
            }
        }
        if (type == EntityType.ARMOR_STAND && plugin.isHologramPatch()) {
            if (entry.getOldID() == 0) {
                byte data = (byte) entry.getValue();
                if ((data & 0x20) == 0x20) {
                    if (!knownHolograms.contains(entityID)) {
                        knownHolograms.add(entityID);
                        // Send movement
                        ByteBuf buf = info.getChannel().alloc().buffer();
                        PacketUtil.writeVarInt(PacketType.PLAY_ENTITY_RELATIVE_MOVE.getNewPacketID(), buf);
                        PacketUtil.writeVarInt(entityID, buf);
                        buf.writeShort(0);
                        buf.writeShort((short) (128D * (plugin.getHologramYOffset() * 32D)));
                        buf.writeShort(0);
                        buf.writeBoolean(true);
                        info.sendRawPacket(buf, false);
                    }
                }
            }
        }

        // Boss bar
        if (plugin.isBossbarPatch()) {
            if (type == EntityType.ENDER_DRAGON || type == EntityType.WITHER) {
                if (entry.getOldID() == 2) {
                    BossBar bar = bossBarMap.get(entityID);
                    String title = (String) entry.getValue();
                    title = title.isEmpty() ? (type == EntityType.ENDER_DRAGON ? "Ender Dragon" : "Wither") : title;
                    if (bar == null) {
                        bar = ViaVersion.getInstance().createBossBar(title, BossColor.PINK, BossStyle.SOLID);
                        bossBarMap.put(entityID, bar);
                        bar.addPlayer(info.getPlayer());
                        bar.show();
                    } else {
                        bar.setTitle(title);
                    }
                } else if (entry.getOldID() == 6 && !plugin.isBossbarAntiflicker()) { // If anti flicker is enabled, don't update health
                    BossBar bar = bossBarMap.get(entityID);
                    // Make health range between 0 and 1
                    float maxHealth = type == EntityType.ENDER_DRAGON ? 200.0f : 300.0f;
                    float health = Math.max(0.0f, Math.min(((float) entry.getValue()) / maxHealth, 1.0f));
                    if (bar == null) {
                        String title = type == EntityType.ENDER_DRAGON ? "Ender Dragon" : "Wither";
                        bar = ViaVersion.getInstance().createBossBar(title, health, BossColor.PINK, BossStyle.SOLID);
                        bossBarMap.put(entityID, bar);
                        bar.addPlayer(info.getPlayer());
                        bar.show();
                    } else {
                        bar.setHealth(health);
                    }
                }
            }
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

    private void sendSecondHandItem(int entityID, ItemSlotRewriter.ItemStack o) {

        ByteBuf buf = info.getChannel().alloc().buffer();
        PacketUtil.writeVarInt(PacketType.PLAY_ENTITY_EQUIPMENT.getNewPacketID(), buf);
        PacketUtil.writeVarInt(entityID, buf);
        PacketUtil.writeVarInt(1, buf); // slot
        // write shield
        try {
            ItemSlotRewriter.writeItemStack(o, buf);
        } catch (IOException e) {
            e.printStackTrace();
        }
        info.sendRawPacket(buf, true);
    }
}
package us.myles.ViaVersion.protocols.protocol1_9to1_8.packets;

import org.spacehq.opennbt.tag.builtin.CompoundTag;
import org.spacehq.opennbt.tag.builtin.StringTag;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.ViaVersion;
import us.myles.ViaVersion.api.minecraft.chunks.Chunk;
import us.myles.ViaVersion.api.minecraft.item.Item;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketHandler;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.remapper.ValueCreator;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.packets.State;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.Protocol1_9TO1_8;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.sounds.SoundEffect;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.storage.ClientChunks;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.storage.EntityTracker;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.types.ChunkType;

public class WorldPackets {
    public static void register(Protocol protocol) {
        // Sign Update Packet
        protocol.registerOutgoing(State.PLAY, 0x33, 0x46, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.POSITION); // 0 - Sign Position
                map(Type.STRING, Protocol1_9TO1_8.FIX_JSON); // 1 - Sign Line (json)
                map(Type.STRING, Protocol1_9TO1_8.FIX_JSON); // 2 - Sign Line (json)
                map(Type.STRING, Protocol1_9TO1_8.FIX_JSON); // 3 - Sign Line (json)
                map(Type.STRING, Protocol1_9TO1_8.FIX_JSON); // 4 - Sign Line (json)
            }
        });

        // Play Effect Packet
        protocol.registerOutgoing(State.PLAY, 0x28, 0x21, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.INT); // 0 - Effect ID
                // Everything else get's written through

                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int id = wrapper.get(Type.INT, 0);
                        if (id >= 1000 && id < 2000 && id != 1005) { // Sound Effect
                            wrapper.cancel();
                        }
                        if (id == 1005) { // Fix jukebox
                            id = 1010;
                        }
                        wrapper.set(Type.INT, 0, id);
                    }
                });
            }
        });

        // Play Named Sound Effect Packet
        protocol.registerOutgoing(State.PLAY, 0x29, 0x19, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.STRING); // 0 - Sound Name
                // 1 - Sound Category ID
                // Everything else get's written through

                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        String name = wrapper.get(Type.STRING, 0);

                        SoundEffect effect = SoundEffect.getByName(name);
                        int catid = 0;
                        String newname = name;
                        if (effect != null) {
                            if (effect.isBreaksound()) {
                                wrapper.cancel();
                                return;
                            }
                            catid = effect.getCategory().getId();
                            newname = effect.getNewName();
                        }
                        wrapper.set(Type.STRING, 0, newname);
                        wrapper.write(Type.VAR_INT, catid); // Write Category ID
                    }
                });
            }
        });

        // Chunk Packet
        protocol.registerOutgoing(State.PLAY, 0x21, 0x20, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        ClientChunks clientChunks = wrapper.user().get(ClientChunks.class);
                        Chunk chunk = wrapper.passthrough(new ChunkType(clientChunks));
                        if (chunk.isUnloadPacket()) {
                            PacketWrapper unload = wrapper.create(0x1D);
                            unload.write(Type.INT, chunk.getX());
                            unload.write(Type.INT, chunk.getZ());
                            unload.send();
                            wrapper.cancel();
                        }
                    }
                });
            }
        });

        // Update Block Entity Packet
        protocol.registerOutgoing(State.PLAY, 0x35, 0x09, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.POSITION); // 0 - Block Position
                map(Type.UNSIGNED_BYTE); // 1 - Action
                map(Type.NBT); // 2 - NBT (Might not be present)
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int action = wrapper.get(Type.UNSIGNED_BYTE, 0);
                        if (action == 1) { // Update Spawner
                            CompoundTag tag = wrapper.get(Type.NBT, 0);
                            if (tag != null) {
                                if (tag.contains("EntityId")) {
                                    String entity = (String) tag.get("EntityId").getValue();
                                    CompoundTag spawn = new CompoundTag("SpawnData");
                                    spawn.put(new StringTag("id", entity));
                                    tag.put(spawn);
                                } else { // EntityID does not exist
                                    CompoundTag spawn = new CompoundTag("SpawnData");
                                    spawn.put(new StringTag("id", "AreaEffectCloud")); //Make spawners show up as empty when no EntityId is given.
                                    tag.put(spawn);
                                }
                            }
                        }
                        if (action == 2) { // Update Command Block
                            // To prevent window issues don't send updates
                            wrapper.cancel();
                        }
                    }
                });
            }
        });

        // Server Difficulty Packet
        protocol.registerOutgoing(State.PLAY, 0x41, 0x0D, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        if (ViaVersion.getConfig().isAutoTeam()) {
                            EntityTracker entityTracker = wrapper.user().get(EntityTracker.class);
                            entityTracker.setAutoTeam(true);
                            entityTracker.sendTeamPacket(true);
                        }
                    }
                });
            }
        });
        /* Packets which do not have any field remapping or handlers */

        protocol.registerOutgoing(State.PLAY, 0x25, 0x08); // Block Break Animation Packet
        protocol.registerOutgoing(State.PLAY, 0x24, 0x0A); // Block Action Packet
        protocol.registerOutgoing(State.PLAY, 0x23, 0x0B); // Block Change Packet
        protocol.registerOutgoing(State.PLAY, 0x22, 0x10); // Multi Block Change Packet
        protocol.registerOutgoing(State.PLAY, 0x27, 0x1C); // Explosion Packet
        protocol.registerOutgoing(State.PLAY, 0x2A, 0x22); // Particle Packet
        protocol.registerOutgoing(State.PLAY, 0x03, 0x44); // Update Time Packet
        protocol.registerOutgoing(State.PLAY, 0x44, 0x35); // World Border Packet

        /* Incoming Packets */

        // Sign Update Request Packet
        protocol.registerIncoming(State.PLAY, 0x12, 0x19, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.POSITION); // 0 - Sign Position
                map(Type.STRING, Protocol1_9TO1_8.FIX_JSON); // 1 - Sign Line (json)
                map(Type.STRING, Protocol1_9TO1_8.FIX_JSON); // 2 - Sign Line (json)
                map(Type.STRING, Protocol1_9TO1_8.FIX_JSON); // 3 - Sign Line (json)
                map(Type.STRING, Protocol1_9TO1_8.FIX_JSON); // 4 - Sign Line (json)
            }
        });

        // Player Digging Packet
        protocol.registerIncoming(State.PLAY, 0x07, 0x13, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.UNSIGNED_BYTE); // 0 - Status
                map(Type.POSITION); // 1 - Position
                map(Type.UNSIGNED_BYTE); // 2 - Face
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int status = wrapper.get(Type.UNSIGNED_BYTE, 0);
                        if (status == 6)
                            wrapper.cancel();
                    }
                });
                // Blocking
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int status = wrapper.get(Type.UNSIGNED_BYTE, 0);
                        if (status == 5) {
                            EntityTracker entityTracker = wrapper.user().get(EntityTracker.class);
                            if (entityTracker.isBlocking()) {
                                entityTracker.setBlocking(false);
                                entityTracker.setSecondHand(null);
                            }
                        }
                    }
                });
            }
        });

        // Block Placement Packet
        protocol.registerIncoming(State.PLAY, 0x08, 0x1C, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.POSITION); // 0 - Position
                map(Type.VAR_INT, Type.BYTE); // 1 - Block Face
                map(Type.VAR_INT, Type.NOTHING); // 2 - Hand
                create(new ValueCreator() {
                    @Override
                    public void write(PacketWrapper wrapper) throws Exception {
                        Item item = Item.getItem(Protocol1_9TO1_8.getHandItem(wrapper.user()));
                        wrapper.write(Type.ITEM, item);
                    }
                });
                map(Type.UNSIGNED_BYTE); // 4 - X
                map(Type.UNSIGNED_BYTE); // 5 - Y
                map(Type.UNSIGNED_BYTE); // 6 - Z

            }
        });
    }
}

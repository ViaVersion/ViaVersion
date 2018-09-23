package us.myles.ViaVersion.protocols.protocol1_9to1_8.packets;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.StringTag;
import com.google.common.base.Optional;
import io.netty.buffer.ByteBuf;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.minecraft.Position;
import us.myles.ViaVersion.api.minecraft.item.Item;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketHandler;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.remapper.ValueCreator;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.packets.State;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.ItemRewriter;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.Protocol1_9TO1_8;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.chunks.Chunk1_9to1_8;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.providers.BulkChunkTranslatorProvider;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.providers.CommandBlockProvider;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.sounds.Effect;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.sounds.SoundEffect;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.storage.ClientChunks;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.storage.EntityTracker;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.storage.PlaceBlockTracker;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.types.ChunkType;

import java.io.IOException;
import java.util.List;

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
                map(Type.POSITION); // 1 - Position
                map(Type.INT); // 2 - Data
                map(Type.BOOLEAN); // 3 - Disable relative volume

                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int id = wrapper.get(Type.INT, 0);

                        id = Effect.getNewId(id);
                        wrapper.set(Type.INT, 0, id);
                    }
                });
                // Rewrite potion effect as it changed to use a dynamic registry
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int id = wrapper.get(Type.INT, 0);
                        if (id == 2002) {
                            int data = wrapper.get(Type.INT, 1);
                            int newData = ItemRewriter.getNewEffectID(data);
                            wrapper.set(Type.INT, 1, newData);
                        }
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
                            catid = effect.getCategory().getId();
                            newname = effect.getNewName();
                        }
                        wrapper.set(Type.STRING, 0, newname);
                        wrapper.write(Type.VAR_INT, catid); // Write Category ID
                        if (effect != null && effect.isBreaksound()) {
                            EntityTracker tracker = wrapper.user().get(EntityTracker.class);
                            int x = wrapper.passthrough(Type.INT); //Position X
                            int y = wrapper.passthrough(Type.INT); //Position Y
                            int z = wrapper.passthrough(Type.INT); //Position Z
                            if (tracker.interactedBlockRecently((int) Math.floor(x / 8.0), (int) Math.floor(y / 8.0), (int) Math.floor(z / 8.0))) {
                                wrapper.cancel();
                                return;
                            }
                        }
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
                        Chunk1_9to1_8 chunk = (Chunk1_9to1_8) wrapper.passthrough(new ChunkType(clientChunks));
                        if (chunk.isUnloadPacket()) {
                            wrapper.setId(0x1D);

                            // Remove commandBlocks on chunk unload
                            CommandBlockProvider provider = Via.getManager().getProviders().get(CommandBlockProvider.class);
                            provider.unloadChunk(wrapper.user(), chunk.getX(), chunk.getZ());
                        }

                        // eat any other data (Usually happens with unload packets)
                        wrapper.read(Type.REMAINING_BYTES);
                    }
                });
            }
        });

        // Bulk Chunk Packet
        protocol.registerOutgoing(State.PLAY, 0x26, -1, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        wrapper.cancel(); // Cancel the packet from being sent
                        BulkChunkTranslatorProvider provider = Via.getManager().getProviders().get(BulkChunkTranslatorProvider.class);

                        // Don't read the packet
                        if (!provider.isPacketLevel())
                            return;

                        List<Object> list = provider.transformMapChunkBulk(wrapper, wrapper.user().get(ClientChunks.class));
                        for (Object obj : list) {
                            if (!(obj instanceof PacketWrapper))
                                throw new IOException("transformMapChunkBulk returned the wrong object type");

                            PacketWrapper output = (PacketWrapper) obj;
                            ByteBuf buffer = wrapper.user().getChannel().alloc().buffer();
                            output.setId(-1); // -1 for no writing of id
                            output.writeToBuffer(buffer);
                            PacketWrapper chunkPacket = new PacketWrapper(0x21, buffer, wrapper.user());
                            chunkPacket.send(Protocol1_9TO1_8.class, false, true);
                            buffer.release();
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
                            CommandBlockProvider provider = Via.getManager().getProviders().get(CommandBlockProvider.class);
                            provider.addOrUpdateBlock(wrapper.user(), wrapper.get(Type.POSITION, 0), wrapper.get(Type.NBT, 0));

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
                        if (Via.getConfig().isAutoTeam()) {
                            EntityTracker entityTracker = wrapper.user().get(EntityTracker.class);
                            entityTracker.setAutoTeam(true);
                            entityTracker.sendTeamPacket(true, true);
                        }
                    }
                });
            }
        });

        // Block Change Packet
        protocol.registerOutgoing(State.PLAY, 0x23, 0x0B, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.POSITION);
                map(Type.VAR_INT);
            }
        });
        /* Packets which do not have any field remapping or handlers */

        protocol.registerOutgoing(State.PLAY, 0x25, 0x08); // Block Break Animation Packet
        protocol.registerOutgoing(State.PLAY, 0x24, 0x0A); // Block Action Packet
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
                        if (status == 5 || status == 4 || status == 3) {
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

        // Use Item Packet
        protocol.registerIncoming(State.PLAY, -1, 0x1D, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int hand = wrapper.read(Type.VAR_INT);
                        // Wipe the input buffer
                        wrapper.clearInputBuffer();
                        // First set this packet ID to Block placement
                        wrapper.setId(0x08);
                        wrapper.write(Type.LONG, -1L);
                        wrapper.write(Type.BYTE, (byte) 255);
                        // Write item in hand
                        Item item = Protocol1_9TO1_8.getHandItem(wrapper.user());
                        // Blocking patch
                        if (Via.getConfig().isShieldBlocking()) {
                            EntityTracker tracker = wrapper.user().get(EntityTracker.class);
                            
                            if (item != null && Protocol1_9TO1_8.isSword(item.getId())) {
                                if (hand == 0) {
                                    if (!tracker.isBlocking()) {
                                        tracker.setBlocking(true);
                                        Item shield = new Item((short) 442, (byte) 1, (short) 0, null);
                                        tracker.setSecondHand(shield);
                                    }
                                    wrapper.cancel();
                                }
                            } else {
                                tracker.setSecondHand(null);
                                tracker.setBlocking(false);
                            }
                        }
                        wrapper.write(Type.ITEM, item);

                        wrapper.write(Type.BYTE, (byte) 0);
                        wrapper.write(Type.BYTE, (byte) 0);
                        wrapper.write(Type.BYTE, (byte) 0);
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
                        Item item = Protocol1_9TO1_8.getHandItem(wrapper.user());
                        wrapper.write(Type.ITEM, item); // 3 - Item
                    }
                });
                map(Type.UNSIGNED_BYTE); // 4 - X
                map(Type.UNSIGNED_BYTE); // 5 - Y
                map(Type.UNSIGNED_BYTE); // 6 - Z

                // Cancel if item as 1.9 uses Use_Item packet
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        Position position = wrapper.get(Type.POSITION, 0);
                        PlaceBlockTracker tracker = wrapper.user().get(PlaceBlockTracker.class);
                        if (tracker.getLastPlacedPosition() != null && tracker.getLastPlacedPosition().equals(position) && !tracker.isExpired(50))
                            wrapper.cancel();
                        tracker.updateTime();
                        tracker.setLastPlacedPosition(position);
                    }
                });

                //Register block place to fix sounds
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int face = wrapper.get(Type.BYTE, 0);
                        if (face == 255)
                            return;
                        Position p = wrapper.get(Type.POSITION, 0);
                        long x = p.getX();
                        long y = p.getY();
                        long z = p.getZ();
                        switch (face) {
                            case 0:
                                y--;
                                break;
                            case 1:
                                y++;
                                break;
                            case 2:
                                z--;
                                break;
                            case 3:
                                z++;
                                break;
                            case 4:
                                x--;
                                break;
                            case 5:
                                x++;
                                break;
                        }
                        EntityTracker tracker = wrapper.user().get(EntityTracker.class);
                        tracker.addBlockInteraction(new Position(x, y, z));
                    }
                });

                // Handle CommandBlocks
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        CommandBlockProvider provider = Via.getManager().getProviders().get(CommandBlockProvider.class);

                        Position pos = wrapper.get(Type.POSITION, 0);
                        Optional<CompoundTag> tag = provider.get(wrapper.user(), pos);
                        // Send the Update Block Entity packet if present
                        if (tag.isPresent()) {
                            PacketWrapper updateBlockEntity = new PacketWrapper(0x09, null, wrapper.user());

                            updateBlockEntity.write(Type.POSITION, pos);
                            updateBlockEntity.write(Type.UNSIGNED_BYTE, (short) 2);
                            updateBlockEntity.write(Type.NBT, tag.get());

                            updateBlockEntity.send(Protocol1_9TO1_8.class);
                        }
                    }
                });

            }
        });
    }
}

package us.myles.ViaVersion.protocols.protocol1_11to1_10;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.StringTag;
import com.google.common.base.Optional;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.entities.Entity1_11Types;
import us.myles.ViaVersion.api.minecraft.chunks.Chunk;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketHandler;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.remapper.ValueCreator;
import us.myles.ViaVersion.api.remapper.ValueTransformer;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.api.type.types.version.Types1_9;
import us.myles.ViaVersion.packets.State;
import us.myles.ViaVersion.protocols.protocol1_11to1_10.packets.InventoryPackets;
import us.myles.ViaVersion.protocols.protocol1_11to1_10.storage.EntityTracker;
import us.myles.ViaVersion.protocols.protocol1_11to1_10.storage.FMLTracker;
import us.myles.ViaVersion.protocols.protocol1_9_1_2to1_9_3_4.types.Chunk1_9_3_4Type;
import us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2.storage.ClientWorld;

import java.util.*;

public class Protocol1_11To1_10 extends Protocol {
    private static final ValueTransformer<Float, Short> toOldByte = new ValueTransformer<Float, Short>(Type.UNSIGNED_BYTE) {
        @Override
        public Short transform(PacketWrapper wrapper, Float inputValue) throws Exception {
            return (short) (inputValue * 16);
        }
    };

    @Override
    protected void registerPackets() {
        InventoryPackets.register(this);

        // Spawn Object
        registerOutgoing(State.PLAY, 0x00, 0x00, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // 0 - Entity id
                map(Type.UUID); // 1 - UUID
                map(Type.BYTE); // 2 - Type

                // Track Entity
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {

                        int entityId = wrapper.get(Type.VAR_INT, 0);
                        byte type = wrapper.get(Type.BYTE, 0);

                        Entity1_11Types.EntityType entType = Entity1_11Types.getTypeFromId(type, true);

                        // Register Type ID
                        wrapper.user().get(EntityTracker.class).addEntity(entityId, entType);
                    }
                });
            }
        });

        // Spawn mob packet
        registerOutgoing(State.PLAY, 0x03, 0x03, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // 0 - Entity ID
                map(Type.UUID); // 1 - Entity UUID
                map(Type.UNSIGNED_BYTE, Type.VAR_INT); // 2 - Entity Type
                map(Type.DOUBLE); // 3 - X
                map(Type.DOUBLE); // 4 - Y
                map(Type.DOUBLE); // 5 - Z
                map(Type.BYTE); // 6 - Yaw
                map(Type.BYTE); // 7 - Pitch
                map(Type.BYTE); // 8 - Head Pitch
                map(Type.SHORT); // 9 - Velocity X
                map(Type.SHORT); // 10 - Velocity Y
                map(Type.SHORT); // 11 - Velocity Z
                map(Types1_9.METADATA_LIST); // 12 - Metadata

                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int entityId = wrapper.get(Type.VAR_INT, 0);
                        // Change Type :)
                        int type = wrapper.get(Type.VAR_INT, 1);

                        Entity1_11Types.EntityType entType = MetadataRewriter.rewriteEntityType(type, wrapper.get(Types1_9.METADATA_LIST, 0));
                        if (entType != null)
                            wrapper.set(Type.VAR_INT, 1, entType.getId());

                        // Register Type ID
                        wrapper.user().get(EntityTracker.class).addEntity(entityId, entType);
                        MetadataRewriter.handleMetadata(entityId, entType, wrapper.get(Types1_9.METADATA_LIST, 0), wrapper.user());
                    }
                });
            }
        });

        // Plugin message FML Handshake
        registerOutgoing(State.PLAY, 0x18, 0x18, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        String channel = wrapper.passthrough(Type.STRING);
                        if (Via.getConfig().isForgeFix()
                                && channel.equals("FML|HS")) {
                            FMLTracker fmlTracker = wrapper.user().get(FMLTracker.class);
                            byte discriminator = wrapper.passthrough(Type.BYTE);
                            if (discriminator == 3) { // Registry Data
                                wrapper.passthrough(Type.BOOLEAN); // Has more
                                String name = wrapper.passthrough(Type.STRING);
                                if (name.equals("minecraft:soundevents")) {
                                    int numberOfIds = wrapper.read(Type.VAR_INT);
                                    Map<String, Integer> ids = new HashMap<>(numberOfIds);
                                    for (int i = 0; i < numberOfIds; i++) {
                                        ids.put(
                                                wrapper.read(Type.STRING),
                                                wrapper.read(Type.VAR_INT)
                                        );
                                    }
                                    fmlTracker.getRemovedSoundIds().add(
                                            ids.remove("minecraft:entity.experience_orb.touch")
                                    );
                                    wrapper.write(Type.VAR_INT, ids.size());
                                    for (Map.Entry<String, Integer> id : ids.entrySet()) {
                                        wrapper.write(Type.STRING, id.getKey());
                                        wrapper.write(Type.VAR_INT, id.getValue());
                                    }
                                }
                            } else if (discriminator == -1) { // ack
                                byte phase = wrapper.passthrough(Type.BYTE);
                                if (phase == 3) fmlTracker.setServerHandshakeComplete(true);
                            } else if (discriminator == -2) { // reset
                                fmlTracker.reset();
                            }
                        }
                        wrapper.passthroughAll();
                    }
                });
            }
        });

        // Sound effect
        registerOutgoing(State.PLAY, 0x46, 0x46, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // 0 - Sound name
                map(Type.VAR_INT); // 1 - Sound Category
                map(Type.INT); // 2 - x
                map(Type.INT); // 3 - y
                map(Type.INT); // 4 - z
                map(Type.FLOAT); // 5 - Volume
                map(Type.FLOAT); // 6 - Pitch

                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int id = wrapper.get(Type.VAR_INT, 0);
                        FMLTracker fmlTracker = wrapper.user().get(FMLTracker.class);
                        if (Via.getConfig().isForgeFix()
                                && fmlTracker.isHandshakeComplete()){
                            if (fmlTracker.getRemovedSoundIds().contains(id))
                                id = -1;
                            // Forge will handle sound ids
                        } else {
                            id = getNewSoundId(id);
                        }

                        if (id == -1) // Removed
                            wrapper.cancel();
                        wrapper.set(Type.VAR_INT, 0, id);
                    }
                });
            }
        });

        // Collect item packet
        registerOutgoing(State.PLAY, 0x48, 0x48, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // 0 - Collected entity id
                map(Type.VAR_INT); // 1 - Collector entity id

                create(new ValueCreator() {
                    @Override
                    public void write(PacketWrapper wrapper) throws Exception {
                        wrapper.write(Type.VAR_INT, 1); // 2 - Pickup Count
                    }
                });
            }
        });

        // Metadata packet
        registerOutgoing(State.PLAY, 0x39, 0x39, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // 0 - Entity ID
                map(Types1_9.METADATA_LIST); // 1 - Metadata list
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int entityId = wrapper.get(Type.VAR_INT, 0);

                        Optional<Entity1_11Types.EntityType> type = wrapper.user().get(EntityTracker.class).get(entityId);
                        if (!type.isPresent())
                            return;

                        MetadataRewriter.handleMetadata(entityId, type.get(), wrapper.get(Types1_9.METADATA_LIST, 0), wrapper.user());
                    }
                });
            }
        });

        // Entity teleport
        registerOutgoing(State.PLAY, 0x49, 0x49, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // 0 - Entity id
                map(Type.DOUBLE); // 1 - x
                map(Type.DOUBLE); // 2 - y
                map(Type.DOUBLE); // 3 - z
                map(Type.BYTE); // 4 - yaw
                map(Type.BYTE); // 5 - pitch
                map(Type.BOOLEAN); // 6 - onGround

                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int entityID = wrapper.get(Type.VAR_INT, 0);
                        if (Via.getConfig().isHologramPatch()) {
                            EntityTracker tracker = wrapper.user().get(EntityTracker.class);
                            if (tracker.isHologram(entityID)) {
                                Double newValue = wrapper.get(Type.DOUBLE, 1);
                                newValue -= (Via.getConfig().getHologramYOffset());
                                wrapper.set(Type.DOUBLE, 1, newValue);
                            }
                        }
                    }
                });
            }
        });

        // Destroy entities
        registerOutgoing(State.PLAY, 0x30, 0x30, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT_ARRAY); // 0 - Entity IDS

                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        for (int entity : wrapper.get(Type.VAR_INT_ARRAY, 0))
                            wrapper.user().get(EntityTracker.class).removeEntity(entity);
                    }
                });
            }
        });

        // Title packet
        registerOutgoing(State.PLAY, 0x45, 0x45, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // 0 - Action

                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int action = wrapper.get(Type.VAR_INT, 0);

                        // Handle the new ActionBar
                        if (action >= 2)
                            wrapper.set(Type.VAR_INT, 0, action + 1);
                    }
                });


            }
        });

        // Block action packet
        registerOutgoing(State.PLAY, 0x0A, 0x0A, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.POSITION); // 0 - Position
                map(Type.UNSIGNED_BYTE); // 1 - Action ID
                map(Type.UNSIGNED_BYTE); // 2 - Action Param
                map(Type.VAR_INT); // 3 - Block Type

                // Cheap hack to ensure it's always right block
                handler(new PacketHandler() {
                    @Override
                    public void handle(final PacketWrapper actionWrapper) throws Exception {
                        if (Via.getConfig().isPistonAnimationPatch()) {
                            int id = actionWrapper.get(Type.VAR_INT, 0);
                            if (id == 33 || id == 29) {
                                actionWrapper.cancel();
                            }
                        }
                    }
                });
            }
        });

        // Update Block Entity
        registerOutgoing(State.PLAY, 0x09, 0x09, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.POSITION); // 0 - Position
                map(Type.UNSIGNED_BYTE); // 1 - Action
                map(Type.NBT); // 2 - NBT data

                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        if (wrapper.get(Type.UNSIGNED_BYTE, 0) == 1) {
                            CompoundTag tag = wrapper.get(Type.NBT, 0);
                            EntityIdRewriter.toClientSpawner(tag);
                        }
                    }
                });
            }
        });

        // Chunk Data
        registerOutgoing(State.PLAY, 0x20, 0x20, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        ClientWorld clientWorld = wrapper.user().get(ClientWorld.class);

                        Chunk1_9_3_4Type type = new Chunk1_9_3_4Type(clientWorld);
                        Chunk chunk = wrapper.passthrough(type);

                        // Clear any other bytes (This is a workaround for a issue with 1.9.2 encoder adding nbt list)
                        wrapper.clearInputBuffer();

                        if (chunk.getBlockEntities() == null) return;
                        for (CompoundTag tag : chunk.getBlockEntities()) {
                            if (tag.contains("id") &&
                                    ((StringTag) tag.get("id")).getValue().equals("MobSpawner")) {
                                EntityIdRewriter.toClientSpawner(tag);
                            }
                        }
                    }
                });
            }
        });

        // Join (save dimension id)
        registerOutgoing(State.PLAY, 0x23, 0x23, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.INT); // 0 - Entity ID
                map(Type.UNSIGNED_BYTE); // 1 - Gamemode
                map(Type.INT); // 2 - Dimension

                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        ClientWorld clientChunks = wrapper.user().get(ClientWorld.class);

                        int dimensionId = wrapper.get(Type.INT, 1);
                        clientChunks.setEnvironment(dimensionId);
                    }
                });
            }
        });

        // Respawn (save dimension id)
        registerOutgoing(State.PLAY, 0x33, 0x33, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.INT); // 0 - Dimension ID

                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        ClientWorld clientWorld = wrapper.user().get(ClientWorld.class);

                        int dimensionId = wrapper.get(Type.INT, 0);
                        clientWorld.setEnvironment(dimensionId);
                    }
                });
            }
        });

        /*
            INCOMING PACKETS
         */

        // Block placement
        registerIncoming(State.PLAY, 0x1C, 0x1C, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.POSITION); // 0 - Location
                map(Type.VAR_INT); // 1 - Face
                map(Type.VAR_INT); // 2 - Hand

                map(Type.FLOAT, toOldByte);
                map(Type.FLOAT, toOldByte);
                map(Type.FLOAT, toOldByte);
            }
        });

        // Plugin Message FML Handshake
        registerIncoming(State.PLAY, 0x09, 0x09, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        String channel = wrapper.passthrough(Type.STRING);
                        if (Via.getConfig().isForgeFix()
                                && channel.equals("FML|HS")) {
                            FMLTracker fmlTracker = wrapper.user().get(FMLTracker.class);
                            byte discriminator = wrapper.passthrough(Type.BYTE);
                            if (discriminator == -1) { // ack
                                byte phase = wrapper.passthrough(Type.BYTE);
                                if (fmlTracker.isServerHandshakeComplete() && phase == 5)
                                    fmlTracker.setClientHandshakeComplete(true);
                            }
                        }
                        wrapper.passthroughAll();
                    }
                });
            }
        });

        // Chat Message Incoming
        registerIncoming(State.PLAY, 0x02, 0x02, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.STRING); // 0 - Message
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        // 100 character limit on older servers
                        String msg = wrapper.get(Type.STRING, 0);
                        if (msg.length() > 100) {
                            wrapper.set(Type.STRING, 0, msg.substring(0, 100));
                        }
                    }
                });
            }
        });
    }

    private int getNewSoundId(int id) { //TODO Make it better, suggestions are welcome. It's ugly and hardcoded now.
        if (id == 196) // Experience orb sound got removed
            return -1;

        int newId = id;
        if (id >= 85) // Hello shulker boxes
            newId += 2;
        if (id >= 174) // Hello Guardian flop
            newId += 1;
        if (id >= 194) // Hello evocation things
            newId += 8;
        if (id >= 196) // Rip the Experience orb touch sound :'(
            newId -= 1;
        if (id >= 269) // Hello Liama's
            newId += 9;
        if (id >= 277) // Hello Mule chest
            newId += 1;
        if (id >= 370) // Hello Vex
            newId += 4;
        if (id >= 376) // Hello vindication
            newId += 3;
        if (id >= 423) // Equip Elytra
            newId += 1;
        if (id >= 427) // Hello empty bottle
            newId += 1;
        if (id >= 441) // Hello item totem use
            newId += 1;
        return newId;
    }


    @Override
    public void init(UserConnection userConnection) {
        userConnection.put(new EntityTracker(userConnection));
        if (!userConnection.has(ClientWorld.class))
            userConnection.put(new ClientWorld(userConnection));
        if (!userConnection.has(FMLTracker.class))
            userConnection.put(new FMLTracker(userConnection));
    }
}

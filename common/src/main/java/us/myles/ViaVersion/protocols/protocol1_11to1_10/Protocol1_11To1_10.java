package us.myles.ViaVersion.protocols.protocol1_11to1_10;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.StringTag;
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
import us.myles.ViaVersion.protocols.protocol1_11to1_10.metadata.MetadataRewriter1_11To1_10;
import us.myles.ViaVersion.protocols.protocol1_11to1_10.packets.InventoryPackets;
import us.myles.ViaVersion.protocols.protocol1_11to1_10.storage.EntityTracker1_11;
import us.myles.ViaVersion.protocols.protocol1_9_1_2to1_9_3_4.types.Chunk1_9_3_4Type;
import us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2.storage.ClientWorld;

public class Protocol1_11To1_10 extends Protocol {
    private static final ValueTransformer<Float, Short> toOldByte = new ValueTransformer<Float, Short>(Type.UNSIGNED_BYTE) {
        @Override
        public Short transform(PacketWrapper wrapper, Float inputValue) throws Exception {
            return (short) (inputValue * 16);
        }
    };

    @Override
    protected void registerPackets() {
        MetadataRewriter1_11To1_10 metadataRewriter = new MetadataRewriter1_11To1_10(this);

        InventoryPackets.register(this);

        // Spawn Object
        registerOutgoing(State.PLAY, 0x00, 0x00, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // 0 - Entity id
                map(Type.UUID); // 1 - UUID
                map(Type.BYTE); // 2 - Type

                // Track Entity
                handler(metadataRewriter.getObjectTracker());
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

                        Entity1_11Types.EntityType entType = MetadataRewriter1_11To1_10.rewriteEntityType(type, wrapper.get(Types1_9.METADATA_LIST, 0));
                        if (entType != null) {
                            wrapper.set(Type.VAR_INT, 1, entType.getId());

                            // Register Type ID
                            wrapper.user().get(EntityTracker1_11.class).addEntity(entityId, entType);
                            metadataRewriter.handleMetadata(entityId, wrapper.get(Types1_9.METADATA_LIST, 0), wrapper.user());
                        }
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
                        id = getNewSoundId(id);

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
        metadataRewriter.registerMetadataRewriter(0x39, 0x39, Types1_9.METADATA_LIST);

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
                            EntityTracker1_11 tracker = wrapper.user().get(EntityTracker1_11.class);
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
        metadataRewriter.registerEntityDestroy(0x30, 0x30);

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
                        CompoundTag tag = wrapper.get(Type.NBT, 0);
                        if (wrapper.get(Type.UNSIGNED_BYTE, 0) == 1)
                            EntityIdRewriter.toClientSpawner(tag);

                        if (tag.contains("id"))
                            // Handle new identifier
                            ((StringTag) tag.get("id")).setValue(BlockEntityRewriter.toNewIdentifier((String) tag.get("id").getValue()));

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
                            if (tag.contains("id")) {
                                String identifier = ((StringTag) tag.get("id")).getValue();
                                if (identifier.equals("MobSpawner"))
                                    EntityIdRewriter.toClientSpawner(tag);

                                // Handle new identifier
                                ((StringTag) tag.get("id")).setValue(BlockEntityRewriter.toNewIdentifier(identifier));
                            }
                        }
                    }
                });
            }
        });

        // Join (save dimension id)
        metadataRewriter.registerJoinGame(0x23, 0x23, null);

        // Respawn (save dimension id)
        metadataRewriter.registerRespawn(0x33, 0x33);

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

    private int getNewSoundId(int id) {
        if (id == 196) // Experience orb sound got removed
            return -1;

        if (id >= 85) // Shulker boxes
            id += 2;
        if (id >= 176) // Guardian flop
            id += 1;
        if (id >= 197) // evocation things
            id += 8;
        if (id >= 196) // Rip the Experience orb touch sound :'(
            id -= 1;
        if (id >= 279) // Liama's
            id += 9;
        if (id >= 296) // Mule chest
            id += 1;
        if (id >= 390) // Vex
            id += 4;
        if (id >= 400) // vindication
            id += 3;
        if (id >= 450) // Elytra
            id += 1;
        if (id >= 455) // Empty bottle
            id += 1;
        if (id >= 470) // Totem use
            id += 1;
        return id;
    }


    @Override
    public void init(UserConnection userConnection) {
        // Entity tracker
        userConnection.put(new EntityTracker1_11(userConnection));

        if (!userConnection.has(ClientWorld.class))
            userConnection.put(new ClientWorld(userConnection));
    }
}

package us.myles.ViaVersion.protocols.protocol1_12to1_11_1;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.IntTag;
import com.github.steveice10.opennbt.tag.builtin.StringTag;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.minecraft.chunks.Chunk;
import us.myles.ViaVersion.api.minecraft.chunks.ChunkSection;
import us.myles.ViaVersion.api.platform.providers.ViaProviders;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketHandler;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.api.type.types.version.Types1_12;
import us.myles.ViaVersion.packets.State;
import us.myles.ViaVersion.protocols.protocol1_12to1_11_1.metadata.MetadataRewriter1_12To1_11_1;
import us.myles.ViaVersion.protocols.protocol1_12to1_11_1.packets.InventoryPackets;
import us.myles.ViaVersion.protocols.protocol1_12to1_11_1.providers.InventoryQuickMoveProvider;
import us.myles.ViaVersion.protocols.protocol1_12to1_11_1.storage.EntityTracker1_12;
import us.myles.ViaVersion.protocols.protocol1_9_1_2to1_9_3_4.types.Chunk1_9_3_4Type;
import us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2.storage.ClientWorld;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.Protocol1_9To1_8;

public class Protocol1_12To1_11_1 extends Protocol {

    @Override
    protected void registerPackets() {
        MetadataRewriter1_12To1_11_1 metadataRewriter = new MetadataRewriter1_12To1_11_1(this);

        InventoryPackets.register(this);
        // Outgoing
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
                map(Type.VAR_INT); // 2 - Entity Type
                map(Type.DOUBLE); // 3 - X
                map(Type.DOUBLE); // 4 - Y
                map(Type.DOUBLE); // 5 - Z
                map(Type.BYTE); // 6 - Yaw
                map(Type.BYTE); // 7 - Pitch
                map(Type.BYTE); // 8 - Head Pitch
                map(Type.SHORT); // 9 - Velocity X
                map(Type.SHORT); // 10 - Velocity Y
                map(Type.SHORT); // 11 - Velocity Z
                map(Types1_12.METADATA_LIST); // 12 - Metadata

                // Track mob and rewrite metadata
                handler(metadataRewriter.getTrackerAndRewriter(Types1_12.METADATA_LIST));
            }
        });

        // Chat message packet
        registerOutgoing(State.PLAY, 0x0F, 0x0F, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.STRING, Protocol1_9To1_8.FIX_JSON); // 0 - Chat Message (json)
                map(Type.BYTE); // 1 - Chat Positon

                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        if (!Via.getConfig().is1_12NBTArrayFix()) return;
                        try {
                            JsonElement obj = new JsonParser().parse(wrapper.get(Type.STRING, 0));
                            if (!TranslateRewriter.toClient(obj, wrapper.user())) {
                                wrapper.cancel();
                                return;
                            }
                            ChatItemRewriter.toClient(obj, wrapper.user());
                            wrapper.set(Type.STRING, 0, obj.toString());
                        } catch (Exception e) {
                            e.printStackTrace();
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

                        for (int i = 0; i < chunk.getSections().length; i++) {
                            ChunkSection section = chunk.getSections()[i];
                            if (section == null)
                                continue;

                            for (int y = 0; y < 16; y++) {
                                for (int z = 0; z < 16; z++) {
                                    for (int x = 0; x < 16; x++) {
                                        int block = section.getBlockId(x, y, z);
                                        // Is this a bed?
                                        if (block == 26) {
                                            //  NBT -> { color:14, x:132, y:64, z:222, id:"minecraft:bed" } (Debug output)
                                            CompoundTag tag = new CompoundTag("");
                                            tag.put(new IntTag("color", 14)); // Set color to red (Default in previous versions)
                                            tag.put(new IntTag("x", x + (chunk.getX() << 4)));
                                            tag.put(new IntTag("y", y + (i << 4)));
                                            tag.put(new IntTag("z", z + (chunk.getZ() << 4)));
                                            tag.put(new StringTag("id", "minecraft:bed"));

                                            // Add a fake block entity
                                            chunk.getBlockEntities().add(tag);
                                        }
                                    }
                                }
                            }
                        }

                    }
                });
            }
        });

        // Join Packet
        metadataRewriter.registerJoinGame(0x23, 0x23, null);

        // 0x28 moved to 0x25
        registerOutgoing(State.PLAY, 0x28, 0x25);
        registerOutgoing(State.PLAY, 0x25, 0x26);
        registerOutgoing(State.PLAY, 0x26, 0x27);
        registerOutgoing(State.PLAY, 0x27, 0x28);

        // New packet at 0x30
        // Destroy entities
        metadataRewriter.registerEntityDestroy(0x30, 0x31);

        registerOutgoing(State.PLAY, 0x31, 0x32);
        registerOutgoing(State.PLAY, 0x32, 0x33);

        // Respawn Packet
        metadataRewriter.registerRespawn(0x33, 0x34);

        registerOutgoing(State.PLAY, 0x34, 0x35);
        // New packet at 0x36
        registerOutgoing(State.PLAY, 0x35, 0x37);
        registerOutgoing(State.PLAY, 0x36, 0x38);
        registerOutgoing(State.PLAY, 0x37, 0x39);
        registerOutgoing(State.PLAY, 0x38, 0x3a);

        // Metadata packet
        metadataRewriter.registerMetadataRewriter(0x39, 0x3b, Types1_12.METADATA_LIST);

        registerOutgoing(State.PLAY, 0x3a, 0x3c);
        registerOutgoing(State.PLAY, 0x3b, 0x3d);
        // registerOutgoing(State.PLAY, 0x3c, 0x3e); - Handled in InventoryPackets
        registerOutgoing(State.PLAY, 0x3d, 0x3f);
        registerOutgoing(State.PLAY, 0x3e, 0x40);
        registerOutgoing(State.PLAY, 0x3f, 0x41);
        registerOutgoing(State.PLAY, 0x40, 0x42);
        registerOutgoing(State.PLAY, 0x41, 0x43);
        registerOutgoing(State.PLAY, 0x42, 0x44);
        registerOutgoing(State.PLAY, 0x43, 0x45);
        registerOutgoing(State.PLAY, 0x44, 0x46);
        registerOutgoing(State.PLAY, 0x45, 0x47);

        // Sound effect
        registerOutgoing(State.PLAY, 0x46, 0x48, new PacketRemapper() {
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

        registerOutgoing(State.PLAY, 0x47, 0x49);
        registerOutgoing(State.PLAY, 0x48, 0x4a);
        registerOutgoing(State.PLAY, 0x49, 0x4b);
        // New packet at 0x4c
        registerOutgoing(State.PLAY, 0x4a, 0x4d);
        registerOutgoing(State.PLAY, 0x4b, 0x4e);

        // Incoming
        // New packet at 0x01
        cancelIncoming(State.PLAY, 0x01, 0x01);

        registerIncoming(State.PLAY, 0x01, 0x02);
        registerIncoming(State.PLAY, 0x02, 0x03);
        registerIncoming(State.PLAY, 0x03, 0x04);
        // Client Settings (max length changed)
        registerIncoming(State.PLAY, 0x04, 0x05, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.STRING); // 0 - Locale
                map(Type.BYTE); // 1 - view distance
                map(Type.VAR_INT); // 2 - chat mode
                map(Type.BOOLEAN); // 3 - chat colors
                map(Type.UNSIGNED_BYTE); // 4 - chat flags
                map(Type.VAR_INT); // 5 - main hand
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        // As part of the fix for MC-111054, the max length of
                        // the locale was raised to 16 (from 7), and the client
                        // now makes sure that resource packs have names in that
                        // length.  However, for older servers, it is still 7,
                        // and thus the server will reject it (and the client
                        // won't know that the pack's invalid).
                        // The fix is to just silently lower the length.  The
                        // server doesn't actually use the locale anywhere, so
                        // this is fine.
                        String locale = wrapper.get(Type.STRING, 0);
                        if (locale.length() > 7) {
                            wrapper.set(Type.STRING, 0, locale.substring(0, 7));
                        }
                    }
                });
            }
        });
        registerIncoming(State.PLAY, 0x05, 0x06);
        registerIncoming(State.PLAY, 0x06, 0x07);
        // registerIncoming(State.PLAY, 0x07, 0x08); - Handled in InventoryPackets
        registerIncoming(State.PLAY, 0x08, 0x09);
        registerIncoming(State.PLAY, 0x09, 0x0a);
        registerIncoming(State.PLAY, 0x0a, 0x0b);
        registerIncoming(State.PLAY, 0x0b, 0x0c);
        // Mojang swapped 0x0F to 0x0D
        registerIncoming(State.PLAY, 0x0f, 0x0d);
        registerIncoming(State.PLAY, 0x0c, 0x0e);
        // Mojang swapped 0x0F to 0x0D
        registerIncoming(State.PLAY, 0x0d, 0x0f);
        registerIncoming(State.PLAY, 0x0e, 0x10);
        registerIncoming(State.PLAY, 0x10, 0x11);
        registerIncoming(State.PLAY, 0x11, 0x12);
        registerIncoming(State.PLAY, 0x12, 0x13);
        registerIncoming(State.PLAY, 0x13, 0x14);
        registerIncoming(State.PLAY, 0x14, 0x15);
        registerIncoming(State.PLAY, 0x15, 0x16);

        // New packet at 0x17
        cancelIncoming(State.PLAY, 0x17, 0x17);

        registerIncoming(State.PLAY, 0x16, 0x18);

        // New packet 0x19
        cancelIncoming(State.PLAY, 0x19, 0x19);

        registerIncoming(State.PLAY, 0x17, 0x1a);
        // registerIncoming(State.PLAY, 0x18, 0x1b); - Handled in InventoryPackets
        registerIncoming(State.PLAY, 0x19, 0x1c);
        registerIncoming(State.PLAY, 0x1a, 0x1d);
        registerIncoming(State.PLAY, 0x1b, 0x1e);
        registerIncoming(State.PLAY, 0x1c, 0x1f);
        registerIncoming(State.PLAY, 0x1d, 0x20);
    }

    private int getNewSoundId(int id) { //TODO Make it better, suggestions are welcome. It's ugly and hardcoded now.
        int newId = id;
        if (id >= 26) // End Portal Sounds
            newId += 2;
        if (id >= 70) // New Block Notes
            newId += 4;
        if (id >= 74) // New Block Note 2
            newId += 1;
        if (id >= 143) // Boat Sounds
            newId += 3;
        if (id >= 185) // Endereye death
            newId += 1;
        if (id >= 263) // Illagers
            newId += 7;
        if (id >= 301) // Parrots
            newId += 33;
        if (id >= 317) // Player Sounds
            newId += 2;
        if (id >= 491) // UI toast sound
            newId += 3;
        return newId;
    }

    @Override
    protected void register(ViaProviders providers) {
        providers.register(InventoryQuickMoveProvider.class, new InventoryQuickMoveProvider());
    }

    @Override
    public void init(UserConnection userConnection) {
        userConnection.put(new EntityTracker1_12(userConnection));
        if (!userConnection.has(ClientWorld.class))
            userConnection.put(new ClientWorld(userConnection));
    }
}

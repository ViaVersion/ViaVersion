package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.packets;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.google.common.base.Optional;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.minecraft.BlockChangeRecord;
import us.myles.ViaVersion.api.minecraft.Position;
import us.myles.ViaVersion.api.minecraft.chunks.Chunk;
import us.myles.ViaVersion.api.minecraft.chunks.ChunkSection;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketHandler;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.packets.State;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.Protocol1_13To1_12_2;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.data.MappingData;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.data.NamedSoundRewriter;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.data.Particle;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.data.ParticleRewriter;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.providers.BlockEntityProvider;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.providers.PaintingProvider;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.storage.BlockStorage;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.types.Chunk1_13Type;
import us.myles.ViaVersion.protocols.protocol1_9_1_2to1_9_3_4.types.Chunk1_9_3_4Type;
import us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2.storage.ClientWorld;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WorldPackets {
    private static final Set<Integer> validBiomes = new HashSet<>();

    static {
        // Client will crash if it receives a invalid biome id
        for (int i = 0; i < 50; i++) {
            validBiomes.add(i);
        }
        validBiomes.add(127);
        for (int i = 129; i <= 134; i++) {
            validBiomes.add(i);
        }
        validBiomes.add(140);
        validBiomes.add(149);
        validBiomes.add(151);
        for (int i = 155; i <= 158; i++) {
            validBiomes.add(i);
        }
        for (int i = 160; i <= 167; i++) {
            validBiomes.add(i);
        }
    }

    public static void register(Protocol protocol) {
        // Outgoing packets

        // Spawn Painting
        protocol.registerOutgoing(State.PLAY, 0x04, 0x04, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // 0 - Entity ID
                map(Type.UUID); // 1 - Entity UUID

                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        PaintingProvider provider = Via.getManager().getProviders().get(PaintingProvider.class);
                        String motive = wrapper.read(Type.STRING);

                        Optional<Integer> id = provider.getIntByIdentifier(motive);

                        if (!id.isPresent() && (!Via.getConfig().isSuppress1_13ConversionErrors() || Via.getManager().isDebug())) {
                            Via.getPlatform().getLogger().warning("Could not find painting motive: " + motive + " falling back to default (0)");
                        }
                        wrapper.write(Type.VAR_INT, id.or(0));
                    }
                });
            }
        });

        // Update Block Entity
        protocol.registerOutgoing(State.PLAY, 0x09, 0x09, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.POSITION); // 0 - Location
                map(Type.UNSIGNED_BYTE); // 1 - Action
                map(Type.NBT); // 2 - NBT data

                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        Position position = wrapper.get(Type.POSITION, 0);
                        short action = wrapper.get(Type.UNSIGNED_BYTE, 0);
                        CompoundTag tag = wrapper.get(Type.NBT, 0);

                        BlockEntityProvider provider = Via.getManager().getProviders().get(BlockEntityProvider.class);
                        int newId = provider.transform(wrapper.user(), position, tag, true);

                        if (newId != -1) {
                            BlockStorage storage = wrapper.user().get(BlockStorage.class);
                            if (storage.contains(position))
                                storage.get(position).setReplacement(newId);
                        }

                        if (action == 5) // Set type of flower in flower pot
                            wrapper.cancel(); // Removed
                    }
                });
            }
        });

        // Block action
        protocol.registerOutgoing(State.PLAY, 0xA, 0xA, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.POSITION); // Location
                map(Type.UNSIGNED_BYTE); // Action Id
                map(Type.UNSIGNED_BYTE); // Action param
                map(Type.VAR_INT); // Block Id - /!\ NOT BLOCK STATE ID
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        Position pos = wrapper.get(Type.POSITION, 0);
                        short action = wrapper.get(Type.UNSIGNED_BYTE, 0);
                        short param = wrapper.get(Type.UNSIGNED_BYTE, 1);
                        int blockId = wrapper.get(Type.VAR_INT, 0);

                        if (blockId == 25)
                            blockId = 73;
                        else if (blockId == 33)
                            blockId = 99;
                        else if (blockId == 29)
                            blockId = 92;
                        else if (blockId == 54)
                            blockId = 142;
                        else if (blockId == 146)
                            blockId = 305;
                        else if (blockId == 130)
                            blockId = 249;
                        else if (blockId == 138)
                            blockId = 257;
                        else if (blockId == 52)
                            blockId = 140;
                        else if (blockId == 209)
                            blockId = 472;
                        else if (blockId >= 219 && blockId <= 234)
                            blockId = blockId - 219 + 483;

                        if (blockId == 73) { // Note block
                            PacketWrapper blockChange = wrapper.create(0x0B); // block change
                            blockChange.write(Type.POSITION, new Position(pos.getX(), pos.getY(), pos.getZ())); // Clone because position is mutable
                            blockChange.write(Type.VAR_INT, 249 + (action * 24 * 2) + (param * 2));
                            blockChange.send(Protocol1_13To1_12_2.class, true, true);
                        }
                        wrapper.set(Type.VAR_INT, 0, blockId);
                    }
                });
            }
        });

        // Block Change
        protocol.registerOutgoing(State.PLAY, 0xB, 0xB, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.POSITION);
                map(Type.VAR_INT);
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        Position position = wrapper.get(Type.POSITION, 0);
                        int newId = toNewId(wrapper.get(Type.VAR_INT, 0));

                        wrapper.set(Type.VAR_INT, 0, checkStorage(wrapper.user(), position, newId));
                    }
                });
            }
        });

        // Multi Block Change
        protocol.registerOutgoing(State.PLAY, 0x10, 0xF, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.INT); // 0 - Chunk X
                map(Type.INT); // 1 - Chunk Z
                map(Type.BLOCK_CHANGE_RECORD_ARRAY); // 2 - Records
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int chunkX = wrapper.get(Type.INT, 0);
                        int chunkZ = wrapper.get(Type.INT, 1);
                        // Convert ids
                        for (BlockChangeRecord record : wrapper.get(Type.BLOCK_CHANGE_RECORD_ARRAY, 0)) {
                            int newBlock = toNewId(record.getBlockId());
                            Position position = new Position(
                                    (long) (record.getHorizontal() >> 4 & 15) + (chunkX * 16),
                                    (long) record.getY(),
                                    (long) (record.getHorizontal() & 15) + (chunkZ * 16));
                            record.setBlockId(checkStorage(wrapper.user(), position, newBlock));
                        }
                    }
                });
            }
        });

        // Named Sound Effect TODO String -> Identifier? Check if identifier is present?
        protocol.registerOutgoing(State.PLAY, 0x19, 0x1A, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.STRING);
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        String newSoundId = NamedSoundRewriter.getNewId(wrapper.get(Type.STRING, 0));
                        wrapper.set(Type.STRING, 0, newSoundId);
                    }
                });
            }
        });

        // Chunk Data
        protocol.registerOutgoing(State.PLAY, 0x20, 0x22, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        ClientWorld clientWorld = wrapper.user().get(ClientWorld.class);
                        BlockStorage storage = wrapper.user().get(BlockStorage.class);

                        Chunk1_9_3_4Type type = new Chunk1_9_3_4Type(clientWorld);
                        Chunk1_13Type type1_13 = new Chunk1_13Type(clientWorld);
                        Chunk chunk = wrapper.read(type);
                        wrapper.write(type1_13, chunk);

                        for (int i = 0; i < chunk.getSections().length; i++) {
                            ChunkSection section = chunk.getSections()[i];
                            if (section == null)
                                continue;

                            boolean willStoreAnyBlock = false;

                            for (int p = 0; p < section.getPalette().size(); p++) {
                                int old = section.getPalette().get(p);
                                int newId = toNewId(old);
                                if (storage.isWelcome(newId)) {
                                    willStoreAnyBlock = true;
                                }
                                section.getPalette().set(p, newId);
                            }

                            if (willStoreAnyBlock) {
                                for (int x = 0; x < 16; x++) {
                                    for (int y = 0; y < 16; y++) {
                                        for (int z = 0; z < 16; z++) {
                                            int block = section.getBlock(x, y, z);
                                            if (storage.isWelcome(block)) {
                                                storage.store(new Position(
                                                        (long) (x + (chunk.getX() << 4)),
                                                        (long) (y + (i << 4)),
                                                        (long) (z + (chunk.getZ() << 4))
                                                ), block);
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Rewrite biome id 255 to plains
                        if (chunk.isBiomeData()) {
                            int latestBiomeWarn = Integer.MIN_VALUE;
                            for (int i = 0; i < 256; i++) {
                                int biome = chunk.getBiomeData()[i] & 0xFF;
                                if (!validBiomes.contains(biome)) {
                                    if (biome != 255 // is it generated naturally? *shrug*
                                            && latestBiomeWarn != biome) {
                                        if (!Via.getConfig().isSuppress1_13ConversionErrors() || Via.getManager().isDebug()) {
                                            Via.getPlatform().getLogger().warning("Received invalid biome id " + biome);
                                        }
                                        latestBiomeWarn = biome;
                                    }
                                    chunk.getBiomeData()[i] = 1; // Plains
                                }
                            }
                        }

                        // Rewrite BlockEntities to normal blocks
                        BlockEntityProvider provider = Via.getManager().getProviders().get(BlockEntityProvider.class);
                        for (CompoundTag tag : chunk.getBlockEntities()) {
                            int newId = provider.transform(wrapper.user(), null, tag, false);
                            if (newId != -1) {
                                int x = (int) tag.get("x").getValue();
                                int y = (int) tag.get("y").getValue();
                                int z = (int) tag.get("z").getValue();

                                Position position = new Position((long) x, (long) y, (long) z);
                                // Store the replacement blocks for blockupdates
                                if (storage.contains(position))
                                    storage.get(position).setReplacement(newId);

                                chunk.getSections()[y >> 4].setFlatBlock(x & 0xF, y & 0xF, z & 0xF, newId);
                            }
                        }
                    }
                });
            }
        });

        // Particle
        protocol.registerOutgoing(State.PLAY, 0x22, 0x24, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.INT); // 0 - Particle ID
                map(Type.BOOLEAN); // 1 - Long Distance
                map(Type.FLOAT); // 2 - X
                map(Type.FLOAT); // 3 - Y
                map(Type.FLOAT); // 4 - Z
                map(Type.FLOAT); // 5 - Offset X
                map(Type.FLOAT); // 6 - Offset Y
                map(Type.FLOAT); // 7 - Offset Z
                map(Type.FLOAT); // 8 - Particle Data
                map(Type.INT); // 9 - Particle Count

                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int particleId = wrapper.get(Type.INT, 0);

                        // Get the data (Arrays are overrated)
                        int dataCount = 0;
                        // Particles with 1 data [BlockCrack,BlockDust,FallingDust]
                        if (particleId == 37 || particleId == 38 || particleId == 46)
                            dataCount = 1;
                            // Particles with 2 data [IconCrack]
                        else if (particleId == 36)
                            dataCount = 2;

                        Integer[] data = new Integer[dataCount];
                        for (int i = 0; i < data.length; i++)
                            data[i] = wrapper.read(Type.VAR_INT);

                        Particle particle = ParticleRewriter.rewriteParticle(particleId, data);

                        // Cancel if null or completely removed
                        if (particle == null || particle.getId() == -1) {
                            wrapper.cancel();
                            return;
                        }

                        //Handle reddust particle color
                        if (particle.getId() == 11) {
                            int count = wrapper.get(Type.INT, 1);
                            float speed = wrapper.get(Type.FLOAT, 6);
                            // Only handle for count = 0 & speed = 1
                            if (count == 0 && speed == 1) {
                                wrapper.set(Type.INT, 1, 1);
                                wrapper.set(Type.FLOAT, 6, 0f);

                                List<Particle.ParticleData> arguments = particle.getArguments();
                                for (int i = 0; i < 3; i++) {
                                    //RGB values are represented by the X/Y/Z offset
                                    arguments.get(i).setValue(wrapper.get(Type.FLOAT, i + 3));
                                    wrapper.set(Type.FLOAT, i + 3, 0f);
                                }
                            }
                        }

                        wrapper.set(Type.INT, 0, particle.getId());
                        for (Particle.ParticleData particleData : particle.getArguments())
                            wrapper.write(particleData.getType(), particleData.getValue());

                    }
                });
            }
        });
    }

    public static int toNewId(int oldId) {
        if (oldId < 0) {
            oldId = 0; // Some plugins use negative numbers to clear blocks, remap them to air.
        }
        int newId = MappingData.blockMappings.getNewBlock(oldId);
        if (newId != -1) {
            return newId;
        }
        newId = MappingData.blockMappings.getNewBlock(oldId & ~0xF); // Remove data
        if (newId != -1) {
            if (!Via.getConfig().isSuppress1_13ConversionErrors() || Via.getManager().isDebug()) {
                Via.getPlatform().getLogger().warning("Missing block " + oldId);
            }
            return newId;
        }
        if (!Via.getConfig().isSuppress1_13ConversionErrors() || Via.getManager().isDebug()) {
            Via.getPlatform().getLogger().warning("Missing block completely " + oldId);
        }
        // Default stone
        return 1;
    }

    private static int checkStorage(UserConnection user, Position position, int newId) {
        BlockStorage storage = user.get(BlockStorage.class);
        if (storage.contains(position)) {
            BlockStorage.ReplacementData data = storage.get(position);

            if (data.getOriginal() == newId) {
                if (data.getReplacement() != -1) {
                    return data.getReplacement();
                }
            } else {
                storage.remove(position);
                // Check if the new id has to be stored
                if (storage.isWelcome(newId))
                    storage.store(position, newId);
            }
        } else if (storage.isWelcome(newId))
            storage.store(position, newId);
        return newId;
    }
}

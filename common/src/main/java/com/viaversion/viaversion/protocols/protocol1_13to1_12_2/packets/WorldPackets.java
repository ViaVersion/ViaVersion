/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2024 ViaVersion and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.viaversion.viaversion.protocols.protocol1_13to1_12_2.packets;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.StringTag;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.BlockChangeRecord;
import com.viaversion.viaversion.api.minecraft.ClientWorld;
import com.viaversion.viaversion.api.minecraft.Particle;
import com.viaversion.viaversion.api.minecraft.Position;
import com.viaversion.viaversion.api.minecraft.chunks.Chunk;
import com.viaversion.viaversion.api.minecraft.chunks.ChunkSection;
import com.viaversion.viaversion.api.minecraft.chunks.DataPalette;
import com.viaversion.viaversion.api.minecraft.chunks.PaletteType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_13;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_9_3;
import com.viaversion.viaversion.protocols.protocol1_12_1to1_12.ClientboundPackets1_12_1;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.ClientboundPackets1_13;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.Protocol1_13To1_12_2;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.ServerboundPackets1_13;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.blockconnections.ConnectionData;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.blockconnections.ConnectionHandler;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.data.NamedSoundRewriter;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.data.ParticleRewriter;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.providers.BlockEntityProvider;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.providers.PaintingProvider;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.storage.BlockStorage;
import com.viaversion.viaversion.util.IdAndData;
import com.viaversion.viaversion.util.Key;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.Iterator;
import java.util.Optional;

public class WorldPackets {
    private static final IntSet VALID_BIOMES = new IntOpenHashSet(70, .99F);

    static {
        // Client will crash if it receives an invalid biome id
        for (int i = 0; i < 50; i++) {
            VALID_BIOMES.add(i);
        }
        VALID_BIOMES.add(127);
        for (int i = 129; i <= 134; i++) {
            VALID_BIOMES.add(i);
        }
        VALID_BIOMES.add(140);
        VALID_BIOMES.add(149);
        VALID_BIOMES.add(151);
        for (int i = 155; i <= 158; i++) {
            VALID_BIOMES.add(i);
        }
        for (int i = 160; i <= 167; i++) {
            VALID_BIOMES.add(i);
        }
    }

    public static void register(Protocol1_13To1_12_2 protocol) {
        // Outgoing packets
        protocol.registerClientbound(ClientboundPackets1_12_1.SPAWN_PAINTING, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT); // 0 - Entity ID
                map(Type.UUID); // 1 - Entity UUID

                handler(wrapper -> {
                    PaintingProvider provider = Via.getManager().getProviders().get(PaintingProvider.class);
                    String motive = wrapper.read(Type.STRING);

                    Optional<Integer> id = provider.getIntByIdentifier(motive);

                    if (!id.isPresent() && (!Via.getConfig().isSuppressConversionWarnings() || Via.getManager().isDebug())) {
                        Via.getPlatform().getLogger().warning("Could not find painting motive: " + motive + " falling back to default (0)");
                    }
                    wrapper.write(Type.VAR_INT, id.orElse(0));
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_12_1.BLOCK_ENTITY_DATA, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.POSITION1_8); // 0 - Location
                map(Type.UNSIGNED_BYTE); // 1 - Action
                map(Type.NAMED_COMPOUND_TAG); // 2 - NBT data

                handler(wrapper -> {
                    Position position = wrapper.get(Type.POSITION1_8, 0);
                    short action = wrapper.get(Type.UNSIGNED_BYTE, 0);
                    CompoundTag tag = wrapper.get(Type.NAMED_COMPOUND_TAG, 0);

                    BlockEntityProvider provider = Via.getManager().getProviders().get(BlockEntityProvider.class);
                    int newId = provider.transform(wrapper.user(), position, tag, true);

                    if (newId != -1) {
                        BlockStorage storage = wrapper.user().get(BlockStorage.class);
                        BlockStorage.ReplacementData replacementData = storage.get(position);
                        if (replacementData != null) {
                            replacementData.setReplacement(newId);
                        }
                    }

                    if (action == 5) { // Set type of flower in flower pot
                        wrapper.cancel(); // Removed
                    }
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_12_1.BLOCK_ACTION, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.POSITION1_8); // Location
                map(Type.UNSIGNED_BYTE); // Action Id
                map(Type.UNSIGNED_BYTE); // Action param
                map(Type.VAR_INT); // Block Id - /!\ NOT BLOCK STATE ID
                handler(wrapper -> {
                    Position pos = wrapper.get(Type.POSITION1_8, 0);
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
                        PacketWrapper blockChange = wrapper.create(ClientboundPackets1_13.BLOCK_CHANGE);
                        blockChange.write(Type.POSITION1_8, pos);
                        blockChange.write(Type.VAR_INT, 249 + (action * 24 * 2) + (param * 2));
                        blockChange.send(Protocol1_13To1_12_2.class);
                    }
                    wrapper.set(Type.VAR_INT, 0, blockId);
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_12_1.BLOCK_CHANGE, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.POSITION1_8);
                map(Type.VAR_INT);
                handler(wrapper -> {
                    Position position = wrapper.get(Type.POSITION1_8, 0);
                    int newId = toNewId(wrapper.get(Type.VAR_INT, 0));

                    UserConnection userConnection = wrapper.user();
                    if (Via.getConfig().isServersideBlockConnections()) {
                        newId = ConnectionData.connect(userConnection, position, newId);
                        ConnectionData.updateBlockStorage(userConnection, position.x(), position.y(), position.z(), newId);
                    }

                    wrapper.set(Type.VAR_INT, 0, checkStorage(wrapper.user(), position, newId));

                    if (Via.getConfig().isServersideBlockConnections()) {
                        // Workaround for packet order issue
                        wrapper.send(Protocol1_13To1_12_2.class);
                        wrapper.cancel();
                        ConnectionData.update(userConnection, position);
                    }
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_12_1.MULTI_BLOCK_CHANGE, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.INT); // 0 - Chunk X
                map(Type.INT); // 1 - Chunk Z
                map(Type.BLOCK_CHANGE_RECORD_ARRAY); // 2 - Records
                handler(wrapper -> {
                    int chunkX = wrapper.get(Type.INT, 0);
                    int chunkZ = wrapper.get(Type.INT, 1);
                    UserConnection userConnection = wrapper.user();
                    BlockChangeRecord[] records = wrapper.get(Type.BLOCK_CHANGE_RECORD_ARRAY, 0);
                    // Convert ids
                    for (BlockChangeRecord record : records) {
                        int newBlock = toNewId(record.getBlockId());
                        Position position = new Position(
                                record.getSectionX() + (chunkX << 4),
                                record.getY(),
                                record.getSectionZ() + (chunkZ << 4));

                        record.setBlockId(checkStorage(wrapper.user(), position, newBlock));
                        if (Via.getConfig().isServersideBlockConnections()) {
                            ConnectionData.updateBlockStorage(userConnection, position.x(), position.y(), position.z(), newBlock);
                        }
                    }

                    if (Via.getConfig().isServersideBlockConnections()) {
                        for (BlockChangeRecord record : records) {
                            int blockState = record.getBlockId();

                            Position position = new Position(
                                    record.getSectionX() + (chunkX * 16),
                                    record.getY(),
                                    record.getSectionZ() + (chunkZ * 16));

                            ConnectionHandler handler = ConnectionData.getConnectionHandler(blockState);
                            if (handler != null) {
                                blockState = handler.connect(userConnection, position, blockState);
                                record.setBlockId(blockState);
                                ConnectionData.updateBlockStorage(userConnection, position.x(), position.y(), position.z(), blockState);
                            }
                        }

                        // Workaround for packet order issue
                        wrapper.send(Protocol1_13To1_12_2.class);
                        wrapper.cancel();

                        for (BlockChangeRecord record : records) {
                            Position position = new Position(
                                    record.getSectionX() + (chunkX * 16),
                                    record.getY(),
                                    record.getSectionZ() + (chunkZ * 16));
                            ConnectionData.update(userConnection, position);
                        }
                    }
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_12_1.EXPLOSION, new PacketHandlers() {
            @Override
            public void register() {
                if (!Via.getConfig().isServersideBlockConnections()) {
                    return;
                }

                map(Type.FLOAT); // X
                map(Type.FLOAT); // Y
                map(Type.FLOAT); // Z
                map(Type.FLOAT); // Radius
                map(Type.INT); // Record Count

                handler(wrapper -> {
                    UserConnection userConnection = wrapper.user();
                    int x = (int) Math.floor(wrapper.get(Type.FLOAT, 0));
                    int y = (int) Math.floor(wrapper.get(Type.FLOAT, 1));
                    int z = (int) Math.floor(wrapper.get(Type.FLOAT, 2));
                    int recordCount = wrapper.get(Type.INT, 0);
                    Position[] records = new Position[recordCount];

                    for (int i = 0; i < recordCount; i++) {
                        Position position = new Position(
                                x + wrapper.passthrough(Type.BYTE),
                                (short) (y + wrapper.passthrough(Type.BYTE)),
                                z + wrapper.passthrough(Type.BYTE));
                        records[i] = position;

                        // Set to air
                        ConnectionData.updateBlockStorage(userConnection, position.x(), position.y(), position.z(), 0);
                    }

                    // Workaround for packet order issue
                    wrapper.send(Protocol1_13To1_12_2.class);
                    wrapper.cancel();

                    for (int i = 0; i < recordCount; i++) {
                        ConnectionData.update(userConnection, records[i]);
                    }
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_12_1.UNLOAD_CHUNK, new PacketHandlers() {
            @Override
            public void register() {
                if (Via.getConfig().isServersideBlockConnections()) {
                    handler(wrapper -> {
                        int x = wrapper.passthrough(Type.INT);
                        int z = wrapper.passthrough(Type.INT);
                        ConnectionData.blockConnectionProvider.unloadChunk(wrapper.user(), x, z);
                    });
                }
            }
        });

        protocol.registerClientbound(ClientboundPackets1_12_1.NAMED_SOUND, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.STRING);
                handler(wrapper -> {
                    String sound = Key.stripMinecraftNamespace(wrapper.get(Type.STRING, 0));
                    String newSoundId = NamedSoundRewriter.getNewId(sound);
                    wrapper.set(Type.STRING, 0, newSoundId);
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_12_1.CHUNK_DATA, wrapper -> {
            ClientWorld clientWorld = wrapper.user().get(ClientWorld.class);
            BlockStorage storage = wrapper.user().get(BlockStorage.class);

            ChunkType1_9_3 type = ChunkType1_9_3.forEnvironment(clientWorld.getEnvironment());
            ChunkType1_13 type1_13 = ChunkType1_13.forEnvironment(clientWorld.getEnvironment());
            Chunk chunk = wrapper.read(type);
            wrapper.write(type1_13, chunk);

            for (int s = 0; s < chunk.getSections().length; s++) {
                ChunkSection section = chunk.getSections()[s];
                if (section == null) continue;
                DataPalette blocks = section.palette(PaletteType.BLOCKS);

                for (int p = 0; p < blocks.size(); p++) {
                    int old = blocks.idByIndex(p);
                    int newId = toNewId(old);
                    blocks.setIdByIndex(p, newId);
                }

                storage:
                {
                    if (chunk.isFullChunk()) {
                        boolean willSave = false;
                        for (int p = 0; p < blocks.size(); p++) {
                            if (storage.isWelcome(blocks.idByIndex(p))) {
                                willSave = true;
                                break;
                            }
                        }
                        if (!willSave) break storage;
                    }
                    for (int idx = 0; idx < ChunkSection.SIZE; idx++) {
                        int id = blocks.idAt(idx);
                        Position position = new Position(ChunkSection.xFromIndex(idx) + (chunk.getX() << 4), ChunkSection.yFromIndex(idx) + (s << 4), ChunkSection.zFromIndex(idx) + (chunk.getZ() << 4));
                        if (storage.isWelcome(id)) {
                            storage.store(position, id);
                        } else if (!chunk.isFullChunk()) { // Update
                            storage.remove(position);
                        }
                    }
                }

                save_connections:
                {
                    if (!Via.getConfig().isServersideBlockConnections() || !ConnectionData.needStoreBlocks()) {
                        break save_connections;
                    }

                    if (!chunk.isFullChunk()) { // Update
                        ConnectionData.blockConnectionProvider.unloadChunkSection(wrapper.user(), chunk.getX(), s, chunk.getZ());
                    }

                    boolean willSave = false;
                    for (int p = 0; p < blocks.size(); p++) {
                        if (ConnectionData.isWelcome(blocks.idByIndex(p))) {
                            willSave = true;
                            break;
                        }
                    }
                    if (!willSave) {
                        break save_connections;
                    }

                    for (int idx = 0; idx < ChunkSection.SIZE; idx++) {
                        int id = blocks.idAt(idx);
                        if (ConnectionData.isWelcome(id)) {
                            int globalX = ChunkSection.xFromIndex(idx) + (chunk.getX() << 4);
                            int globalY = ChunkSection.yFromIndex(idx) + (s << 4);
                            int globalZ = ChunkSection.zFromIndex(idx) + (chunk.getZ() << 4);
                            ConnectionData.blockConnectionProvider.storeBlock(wrapper.user(), globalX, globalY, globalZ, id);
                        }
                    }
                }
            }

            // Rewrite biome id 255 to plains
            if (chunk.isBiomeData()) {
                int latestBiomeWarn = Integer.MIN_VALUE;
                for (int i = 0; i < 256; i++) {
                    int biome = chunk.getBiomeData()[i];
                    if (!VALID_BIOMES.contains(biome)) {
                        if (biome != 255 // is it generated naturally? *shrug*
                                && latestBiomeWarn != biome) {
                            if (!Via.getConfig().isSuppressConversionWarnings() || Via.getManager().isDebug()) {
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
            final Iterator<CompoundTag> iterator = chunk.getBlockEntities().iterator();
            while (iterator.hasNext()) {
                CompoundTag tag = iterator.next();
                int newId = provider.transform(wrapper.user(), null, tag, false);
                if (newId != -1) {
                    int x = tag.getNumberTag("x").asInt();
                    int y = tag.getNumberTag("y").asInt();
                    int z = tag.getNumberTag("z").asInt();

                    Position position = new Position(x, (short) y, z);
                    // Store the replacement blocks for blockupdates
                    BlockStorage.ReplacementData replacementData = storage.get(position);
                    if (replacementData != null) {
                        replacementData.setReplacement(newId);
                    }

                    chunk.getSections()[y >> 4].palette(PaletteType.BLOCKS).setIdAt(x & 0xF, y & 0xF, z & 0xF, newId);
                }

                final StringTag idTag = tag.getStringTag("id");
                if (idTag != null) {
                    // No longer block entities
                    final String id = Key.namespaced(idTag.getValue());
                    if (id.equals("minecraft:noteblock") || id.equals("minecraft:flower_pot")) {
                        iterator.remove();
                    }
                }
            }

            if (Via.getConfig().isServersideBlockConnections()) {
                ConnectionData.connectBlocks(wrapper.user(), chunk);
                // Workaround for packet order issue
                wrapper.send(Protocol1_13To1_12_2.class);
                wrapper.cancel();
                ConnectionData.NeighbourUpdater updater = new ConnectionData.NeighbourUpdater(wrapper.user());
                for (int i = 0; i < chunk.getSections().length; i++) {
                    ChunkSection section = chunk.getSections()[i];
                    if (section == null) {
                        continue;
                    }

                    updater.updateChunkSectionNeighbours(chunk.getX(), chunk.getZ(), i);
                }
            }
        });

        protocol.registerClientbound(ClientboundPackets1_12_1.SPAWN_PARTICLE, new PacketHandlers() {
            @Override
            public void register() {
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

                handler(wrapper -> {
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

                    // Handle reddust particle color
                    if (particle.getId() == 11) {
                        int count = wrapper.get(Type.INT, 1);
                        float speed = wrapper.get(Type.FLOAT, 6);
                        // Only handle for count = 0
                        if (count == 0) {
                            wrapper.set(Type.INT, 1, 1);
                            wrapper.set(Type.FLOAT, 6, 0f);

                            for (int i = 0; i < 3; i++) {
                                //RGB values are represented by the X/Y/Z offset
                                float colorValue = wrapper.get(Type.FLOAT, i + 3) * speed;
                                if (colorValue == 0 && i == 0) {
                                    // https://minecraft.gamepedia.com/User:Alphappy/reddust
                                    colorValue = 1;
                                }
                                particle.<Float>getArgument(i).setValue(colorValue);
                                wrapper.set(Type.FLOAT, i + 3, 0f);
                            }
                        }
                    }

                    wrapper.set(Type.INT, 0, particle.getId());
                    for (Particle.ParticleData<?> particleData : particle.getArguments())
                        particleData.write(wrapper);

                });
            }
        });

        // Incoming Packets
        protocol.registerServerbound(ServerboundPackets1_13.PLAYER_BLOCK_PLACEMENT, wrapper -> {
            Position pos = wrapper.passthrough(Type.POSITION1_8);
            wrapper.passthrough(Type.VAR_INT); // block face
            wrapper.passthrough(Type.VAR_INT); // hand
            wrapper.passthrough(Type.FLOAT); // cursor x
            wrapper.passthrough(Type.FLOAT); // cursor y
            wrapper.passthrough(Type.FLOAT); // cursor z

            if (Via.getConfig().isServersideBlockConnections() && ConnectionData.needStoreBlocks()) {
                ConnectionData.markModified(wrapper.user(), pos);
            }
        });
        protocol.registerServerbound(ServerboundPackets1_13.PLAYER_DIGGING, wrapper -> {
            int status = wrapper.passthrough(Type.VAR_INT); // Status
            Position pos = wrapper.passthrough(Type.POSITION1_8); // Location
            wrapper.passthrough(Type.UNSIGNED_BYTE); // block face

            // 0 = Started digging: if in creative this causes the block to break directly
            // There's no point in storing the finished digging as it may never show-up (creative)
            if (status == 0 && Via.getConfig().isServersideBlockConnections() && ConnectionData.needStoreBlocks()) {
                ConnectionData.markModified(wrapper.user(), pos);
            }
        });

    }

    public static int toNewId(int oldId) {
        if (oldId < 0) {
            oldId = 0; // Some plugins use negative numbers to clear blocks, remap them to air.
        }
        int newId = Protocol1_13To1_12_2.MAPPINGS.getBlockMappings().getNewId(oldId);
        if (newId != -1) {
            return newId;
        }
        newId = Protocol1_13To1_12_2.MAPPINGS.getBlockMappings().getNewId(IdAndData.removeData(oldId)); // Remove data
        if (newId != -1) {
            if (!Via.getConfig().isSuppressConversionWarnings() || Via.getManager().isDebug()) {
                Via.getPlatform().getLogger().warning("Missing block " + oldId);
            }
            return newId;
        }
        if (!Via.getConfig().isSuppressConversionWarnings() || Via.getManager().isDebug()) {
            Via.getPlatform().getLogger().warning("Missing block completely " + oldId);
        }
        // Default air
        return 0;
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

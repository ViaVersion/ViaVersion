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
package com.viaversion.viaversion.protocols.v1_12_2to1_13.blockconnections;

import com.viaversion.nbt.tag.ByteArrayTag;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.IntArrayTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.NumberTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.MappingDataLoader;
import com.viaversion.viaversion.api.minecraft.BlockChangeRecord1_8;
import com.viaversion.viaversion.api.minecraft.BlockFace;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.api.minecraft.chunks.Chunk;
import com.viaversion.viaversion.api.minecraft.chunks.ChunkSection;
import com.viaversion.viaversion.api.minecraft.chunks.DataPalette;
import com.viaversion.viaversion.api.minecraft.chunks.PaletteType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.Protocol1_12_2To1_13;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.blockconnections.providers.BlockConnectionProvider;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.blockconnections.providers.PacketBlockConnectionProvider;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.blockconnections.providers.UserBlockData;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.packet.ClientboundPackets1_13;
import com.viaversion.viaversion.util.Key;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

public final class ConnectionData {
    public static BlockConnectionProvider blockConnectionProvider;
    static final Object2IntMap<String> KEY_TO_ID = new Object2IntOpenHashMap<>(8582);
    static final IntSet OCCLUDING_STATES = new IntOpenHashSet(377);
    static Int2ObjectMap<ConnectionHandler> connectionHandlerMap = new Int2ObjectOpenHashMap<>();
    static Int2ObjectMap<BlockData> blockConnectionData = new Int2ObjectOpenHashMap<>();
    private static final BlockChangeRecord1_8[] EMPTY_RECORDS = new BlockChangeRecord1_8[0];

    static {
        KEY_TO_ID.defaultReturnValue(-1);
    }

    public static void update(UserConnection user, BlockPosition position) {
        Boolean inSync = null;

        for (BlockFace face : BlockFace.values()) {
            BlockPosition pos = position.getRelative(face);
            int blockState = blockConnectionProvider.getBlockData(user, pos.x(), pos.y(), pos.z());
            ConnectionHandler handler = connectionHandlerMap.get(blockState);
            if (handler == null) {
                continue;
            }

            int newBlockState = handler.connect(user, pos, blockState);
            if (newBlockState == blockState) {
                if (inSync == null) {
                    inSync = blockConnectionProvider.storesBlocks(user, position);
                }
                // Blocks-states are the same, and known to be stored and not de-synced, skip update
                if (inSync) {
                    continue;
                }
            }

            updateBlockStorage(user, pos.x(), pos.y(), pos.z(), newBlockState);

            PacketWrapper blockUpdatePacket = PacketWrapper.create(ClientboundPackets1_13.BLOCK_UPDATE, null, user);
            blockUpdatePacket.write(Types.BLOCK_POSITION1_8, pos);
            blockUpdatePacket.write(Types.VAR_INT, newBlockState);
            blockUpdatePacket.send(Protocol1_12_2To1_13.class);
        }
    }

    public static void updateBlockStorage(UserConnection userConnection, int x, int y, int z, int blockState) {
        if (!needStoreBlocks()) return;
        if (ConnectionData.isWelcome(blockState)) {
            blockConnectionProvider.storeBlock(userConnection, x, y, z, blockState);
        } else {
            blockConnectionProvider.removeBlock(userConnection, x, y, z);
        }
    }

    public static void clearBlockStorage(UserConnection connection) {
        if (!needStoreBlocks()) return;
        blockConnectionProvider.clearStorage(connection);
    }

    public static void markModified(UserConnection connection, BlockPosition pos) {
        if (!needStoreBlocks()) return;
        blockConnectionProvider.modifiedBlock(connection, pos);
    }

    public static boolean needStoreBlocks() {
        return blockConnectionProvider.storesBlocks(null, null);
    }

    public static void connectBlocks(UserConnection user, Chunk chunk) {
        int xOff = chunk.getX() << 4;
        int zOff = chunk.getZ() << 4;

        for (int s = 0; s < chunk.getSections().length; s++) {
            ChunkSection section = chunk.getSections()[s];
            if (section == null) {
                continue;
            }

            DataPalette blocks = section.palette(PaletteType.BLOCKS);

            boolean willConnect = false;
            for (int p = 0; p < blocks.size(); p++) {
                int id = blocks.idByIndex(p);
                if (ConnectionData.connects(id)) {
                    willConnect = true;
                    break;
                }
            }
            if (!willConnect) {
                continue;
            }

            int yOff = s << 4;

            for (int idx = 0; idx < ChunkSection.SIZE; idx++) {
                int id = blocks.idAt(idx);
                ConnectionHandler handler = ConnectionData.getConnectionHandler(id);
                if (handler == null) {
                    continue;
                }

                BlockPosition position = new BlockPosition(xOff + ChunkSection.xFromIndex(idx), yOff + ChunkSection.yFromIndex(idx), zOff + ChunkSection.zFromIndex(idx));
                int connectedId = handler.connect(user, position, id);
                if (connectedId != id) {
                    blocks.setIdAt(idx, connectedId);
                    updateBlockStorage(user, position.x(), position.y(), position.z(), connectedId);
                }
            }
        }
    }

    public static void init() {
        if (!Via.getConfig().isServersideBlockConnections()) {
            return;
        }

        Via.getPlatform().getLogger().info("Loading block connection mappings ...");
        ListTag<StringTag> blockStates = MappingDataLoader.INSTANCE.loadNBT("blockstates-1.13.nbt").getListTag("blockstates", StringTag.class);
        for (int id = 0; id < blockStates.size(); id++) {
            String key = blockStates.get(id).getValue();
            KEY_TO_ID.put(key, id);
        }

        connectionHandlerMap = new Int2ObjectOpenHashMap<>(3650);

        if (!Via.getConfig().isReduceBlockStorageMemory()) {
            blockConnectionData = new Int2ObjectOpenHashMap<>(2048);

            CompoundTag data = MappingDataLoader.INSTANCE.loadNBT("blockConnections.nbt");
            ListTag<CompoundTag> blockConnectionMappings = data.getListTag("data", CompoundTag.class);
            for (CompoundTag blockTag : blockConnectionMappings) {
                BlockData blockData = new BlockData();
                for (Entry<String, Tag> entry : blockTag.entrySet()) {
                    String key = entry.getKey();
                    if (key.equals("id") || key.equals("ids")) {
                        continue;
                    }


                    boolean[] attachingFaces = new boolean[4];
                    ByteArrayTag connections = (ByteArrayTag) entry.getValue();
                    for (byte blockFaceId : connections.getValue()) {
                        attachingFaces[blockFaceId] = true;
                    }

                    int connectionTypeId = Integer.parseInt(key);
                    blockData.put(connectionTypeId, attachingFaces);
                }

                NumberTag idTag = blockTag.getNumberTag("id");
                if (idTag != null) {
                    blockConnectionData.put(idTag.asInt(), blockData);
                } else {
                    IntArrayTag idsTag = blockTag.getIntArrayTag("ids");
                    for (int id : idsTag.getValue()) {
                        blockConnectionData.put(id, blockData);
                    }
                }
            }

            IntArrayTag occludingStatesArray = data.getIntArrayTag("occluding-states");
            for (final int blockStateId : occludingStatesArray.getValue()) {
                OCCLUDING_STATES.add(blockStateId);
            }
        }

        List<ConnectorInitAction> initActions = new ArrayList<>();
        initActions.add(PumpkinConnectionHandler.init());
        initActions.addAll(BasicFenceConnectionHandler.init());
        initActions.add(NetherFenceConnectionHandler.init());
        initActions.addAll(WallConnectionHandler.init());
        initActions.add(MelonConnectionHandler.init());
        initActions.addAll(GlassConnectionHandler.init());
        initActions.add(ChestConnectionHandler.init());
        initActions.add(DoorConnectionHandler.init());
        initActions.add(RedstoneConnectionHandler.init());
        initActions.add(StairConnectionHandler.init());
        initActions.add(FlowerConnectionHandler.init());
        initActions.addAll(ChorusPlantConnectionHandler.init());
        initActions.add(TripwireConnectionHandler.init());
        initActions.add(SnowyGrassConnectionHandler.init());
        initActions.add(FireConnectionHandler.init());
        if (Via.getConfig().isVineClimbFix()) {
            initActions.add(VineConnectionHandler.init());
        }

        for (String key : KEY_TO_ID.keySet()) {
            WrappedBlockData wrappedBlockData = WrappedBlockData.fromString(key);
            for (ConnectorInitAction action : initActions) {
                action.check(wrappedBlockData);
            }
        }

        if (Via.getConfig().getBlockConnectionMethod().equalsIgnoreCase("packet")) {
            blockConnectionProvider = new PacketBlockConnectionProvider();
            Via.getManager().getProviders().register(BlockConnectionProvider.class, blockConnectionProvider);
        }
    }

    public static boolean isWelcome(int blockState) {
        return blockConnectionData.containsKey(blockState) || connectionHandlerMap.containsKey(blockState);
    }

    public static boolean connects(int blockState) {
        return connectionHandlerMap.containsKey(blockState);
    }

    public static int connect(UserConnection user, BlockPosition position, int blockState) {
        ConnectionHandler handler = connectionHandlerMap.get(blockState);
        return handler != null ? handler.connect(user, position, blockState) : blockState;
    }

    public static ConnectionHandler getConnectionHandler(int blockstate) {
        return connectionHandlerMap.get(blockstate);
    }

    public static int getId(String key) {
        return KEY_TO_ID.getOrDefault(Key.stripMinecraftNamespace(key), -1);
    }

    @FunctionalInterface
    interface ConnectorInitAction {

        void check(WrappedBlockData blockData);
    }

    public static Object2IntMap<String> getKeyToId() {
        return KEY_TO_ID;
    }

    public static final class NeighbourUpdater {
        private final UserConnection user;
        private final UserBlockData userBlockData;

        public NeighbourUpdater(UserConnection user) {
            this.user = user;
            this.userBlockData = blockConnectionProvider.forUser(user);
        }

        public void updateChunkSectionNeighbours(int chunkX, int chunkZ, int chunkSectionY) {
            int chunkMinY = chunkSectionY << 4;
            List<BlockChangeRecord1_8> updates = new ArrayList<>();
            for (int chunkDeltaX = -1; chunkDeltaX <= 1; chunkDeltaX++) {
                for (int chunkDeltaZ = -1; chunkDeltaZ <= 1; chunkDeltaZ++) {
                    int distance = Math.abs(chunkDeltaX) + Math.abs(chunkDeltaZ);
                    if (distance == 0) continue;

                    int chunkMinX = (chunkX + chunkDeltaX) << 4;
                    int chunkMinZ = (chunkZ + chunkDeltaZ) << 4;
                    if (distance == 2) { // Corner
                        for (int blockY = chunkMinY; blockY < chunkMinY + 16; blockY++) {
                            int blockPosX = chunkDeltaX == 1 ? 0 : 15;
                            int blockPosZ = chunkDeltaZ == 1 ? 0 : 15;
                            updateBlock(chunkMinX + blockPosX, blockY, chunkMinZ + blockPosZ, updates);
                        }
                    } else {
                        for (int blockY = chunkMinY; blockY < chunkMinY + 16; blockY++) {
                            int xStart, xEnd;
                            int zStart, zEnd;
                            if (chunkDeltaX == 1) {
                                xStart = 0;
                                xEnd = 2;
                                zStart = 0;
                                zEnd = 16;
                            } else if (chunkDeltaX == -1) {
                                xStart = 14;
                                xEnd = 16;
                                zStart = 0;
                                zEnd = 16;
                            } else if (chunkDeltaZ == 1) {
                                xStart = 0;
                                xEnd = 16;
                                zStart = 0;
                                zEnd = 2;
                            } else {
                                xStart = 0;
                                xEnd = 16;
                                zStart = 14;
                                zEnd = 16;
                            }
                            for (int blockX = xStart; blockX < xEnd; blockX++) {
                                for (int blockZ = zStart; blockZ < zEnd; blockZ++) {
                                    updateBlock(chunkMinX + blockX, blockY, chunkMinZ + blockZ, updates);
                                }
                            }
                        }
                    }

                    if (!updates.isEmpty()) {
                        PacketWrapper wrapper = PacketWrapper.create(ClientboundPackets1_13.CHUNK_BLOCKS_UPDATE, null, user);
                        wrapper.write(Types.INT, chunkX + chunkDeltaX);
                        wrapper.write(Types.INT, chunkZ + chunkDeltaZ);
                        wrapper.write(Types.BLOCK_CHANGE_ARRAY, updates.toArray(EMPTY_RECORDS));
                        wrapper.send(Protocol1_12_2To1_13.class);
                        updates.clear();
                    }
                }
            }
        }

        private void updateBlock(int x, int y, int z, List<BlockChangeRecord1_8> records) {
            int blockState = userBlockData.getBlockData(x, y, z);
            ConnectionHandler handler = getConnectionHandler(blockState);
            if (handler == null) {
                return;
            }

            BlockPosition pos = new BlockPosition(x, y, z);
            int newBlockState = handler.connect(user, pos, blockState);
            if (blockState != newBlockState || !blockConnectionProvider.storesBlocks(user, null)) {
                records.add(new BlockChangeRecord1_8(x & 0xF, y, z & 0xF, newBlockState));
                updateBlockStorage(user, x, y, z, newBlockState);
            }
        }
    }
}

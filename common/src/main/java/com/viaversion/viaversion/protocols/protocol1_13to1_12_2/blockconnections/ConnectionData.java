/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2022 ViaVersion and contributors
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
package com.viaversion.viaversion.protocols.protocol1_13to1_12_2.blockconnections;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.MappingDataLoader;
import com.viaversion.viaversion.api.minecraft.BlockChangeRecord1_8;
import com.viaversion.viaversion.api.minecraft.BlockFace;
import com.viaversion.viaversion.api.minecraft.Position;
import com.viaversion.viaversion.api.minecraft.chunks.Chunk;
import com.viaversion.viaversion.api.minecraft.chunks.ChunkSection;
import com.viaversion.viaversion.api.minecraft.chunks.DataPalette;
import com.viaversion.viaversion.api.minecraft.chunks.PaletteType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.ClientboundPackets1_13;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.Protocol1_13To1_12_2;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.blockconnections.providers.BlockConnectionProvider;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.blockconnections.providers.PacketBlockConnectionProvider;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;

public class ConnectionData {
    private static final BlockChangeRecord1_8[] EMPTY_RECORDS = new BlockChangeRecord1_8[0];
    public static BlockConnectionProvider blockConnectionProvider;
    static Int2ObjectMap<String> idToKey = new Int2ObjectOpenHashMap<>(8582, .99F);
    static Object2IntMap<String> keyToId = new Object2IntOpenHashMap<>(8582, .99F);
    static Int2ObjectMap<ConnectionHandler> connectionHandlerMap = new Int2ObjectOpenHashMap<>(1);
    static Int2ObjectMap<BlockData> blockConnectionData = new Int2ObjectOpenHashMap<>(1);
    static IntSet occludingStates = new IntOpenHashSet(377, .99F);

    public static void update(UserConnection user, Position position) {
        for (BlockFace face : BlockFace.values()) {
            Position pos = position.getRelative(face);
            int blockState = blockConnectionProvider.getBlockData(user, pos.x(), pos.y(), pos.z());
            ConnectionHandler handler = connectionHandlerMap.get(blockState);
            if (handler == null) continue;

            int newBlockState = handler.connect(user, pos, blockState);
            PacketWrapper blockUpdatePacket = PacketWrapper.create(ClientboundPackets1_13.BLOCK_CHANGE, null, user);
            blockUpdatePacket.write(Type.POSITION, pos);
            blockUpdatePacket.write(Type.VAR_INT, newBlockState);
            try {
                blockUpdatePacket.send(Protocol1_13To1_12_2.class);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void updateChunkSectionNeighbours(UserConnection user, int chunkX, int chunkZ, int chunkSectionY) {
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
                        updateBlock(user, new Position(chunkMinX + blockPosX, blockY, chunkMinZ + blockPosZ), updates);
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
                                updateBlock(user, new Position(chunkMinX + blockX, blockY, chunkMinZ + blockZ), updates);
                            }
                        }
                    }
                }

                if (!updates.isEmpty()) {
                    PacketWrapper wrapper = PacketWrapper.create(ClientboundPackets1_13.MULTI_BLOCK_CHANGE, null, user);
                    wrapper.write(Type.INT, chunkX + chunkDeltaX);
                    wrapper.write(Type.INT, chunkZ + chunkDeltaZ);
                    wrapper.write(Type.BLOCK_CHANGE_RECORD_ARRAY, updates.toArray(EMPTY_RECORDS));
                    try {
                        wrapper.send(Protocol1_13To1_12_2.class);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    updates.clear();
                }
            }
        }
    }

    public static void updateBlock(UserConnection user, Position pos, List<BlockChangeRecord1_8> records) {
        int blockState = blockConnectionProvider.getBlockData(user, pos.x(), pos.y(), pos.z());
        ConnectionHandler handler = getConnectionHandler(blockState);
        if (handler == null) return;

        int newBlockState = handler.connect(user, pos, blockState);
        records.add(new BlockChangeRecord1_8(pos.x() & 0xF, pos.y(), pos.z() & 0xF, newBlockState));
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

    public static boolean needStoreBlocks() {
        return blockConnectionProvider.storesBlocks();
    }

    public static void connectBlocks(UserConnection user, Chunk chunk) {
        int xOff = chunk.getX() << 4;
        int zOff = chunk.getZ() << 4;

        for (int s = 0; s < chunk.getSections().length; s++) {
            ChunkSection section = chunk.getSections()[s];
            if (section == null) continue;
            DataPalette blocks = section.palette(PaletteType.BLOCKS);

            boolean willConnect = false;
            for (int p = 0; p < blocks.size(); p++) {
                int id = blocks.idByIndex(p);
                if (ConnectionData.connects(id)) {
                    willConnect = true;
                    break;
                }
            }
            if (!willConnect) continue;

            int yOff = s << 4;

            for (int idx = 0; idx < ChunkSection.SIZE; idx++) {
                int id = blocks.idAt(idx);
                ConnectionHandler handler = ConnectionData.getConnectionHandler(id);
                if (handler == null) continue;
                id = handler.connect(user, new Position(xOff + (idx & 0xF), yOff + (idx >> 8 & 0xF), zOff + (idx >> 4 & 0xF)), id);
                blocks.setIdAt(idx, id);
            }
        }
    }

    public static void init() {
        if (!Via.getConfig().isServersideBlockConnections()) return;

        Via.getPlatform().getLogger().info("Loading block connection mappings ...");
        JsonObject mapping1_13 = MappingDataLoader.loadData("mapping-1.13.json", true);
        JsonObject blocks1_13 = mapping1_13.getAsJsonObject("blockstates");
        for (Entry<String, JsonElement> blockState : blocks1_13.entrySet()) {
            int id = Integer.parseInt(blockState.getKey());
            String key = blockState.getValue().getAsString();
            idToKey.put(id, key);
            keyToId.put(key, id);
        }

        connectionHandlerMap = new Int2ObjectOpenHashMap<>(3650, .99F);

        if (!Via.getConfig().isReduceBlockStorageMemory()) {
            blockConnectionData = new Int2ObjectOpenHashMap<>(1146, .99F);
            JsonObject mappingBlockConnections = MappingDataLoader.loadData("blockConnections.json");
            for (Entry<String, JsonElement> entry : mappingBlockConnections.entrySet()) {
                int id = keyToId.get(entry.getKey());
                BlockData blockData = new BlockData();
                for (Entry<String, JsonElement> type : entry.getValue().getAsJsonObject().entrySet()) {
                    String name = type.getKey();
                    JsonObject object = type.getValue().getAsJsonObject();
                    boolean[] data = new boolean[6];
                    for (BlockFace value : BlockFace.values()) {
                        String face = value.toString().toLowerCase(Locale.ROOT);
                        if (object.has(face)) {
                            data[value.ordinal()] = object.getAsJsonPrimitive(face).getAsBoolean();
                        }
                    }
                    blockData.put(name, data);
                }
                if (entry.getKey().contains("stairs")) {
                    blockData.put("allFalseIfStairPre1_12", new boolean[6]);
                }
                blockConnectionData.put(id, blockData);
            }
        }

        JsonObject blockData = MappingDataLoader.loadData("blockData.json");
        JsonArray occluding = blockData.getAsJsonArray("occluding");
        for (JsonElement jsonElement : occluding) {
            occludingStates.add(keyToId.get(jsonElement.getAsString()).intValue());
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

        for (String key : keyToId.keySet()) {
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

    public static int connect(UserConnection user, Position position, int blockState) {
        ConnectionHandler handler = connectionHandlerMap.get(blockState);
        return handler != null ? handler.connect(user, position, blockState) : blockState;
    }

    public static ConnectionHandler getConnectionHandler(int blockstate) {
        return connectionHandlerMap.get(blockstate);
    }

    public static int getId(String key) {
        return keyToId.getOrDefault(key, -1);
    }

    public static String getKey(int id) {
        return idToKey.get(id);
    }

    @FunctionalInterface
    interface ConnectorInitAction {

        void check(WrappedBlockData blockData);
    }
}

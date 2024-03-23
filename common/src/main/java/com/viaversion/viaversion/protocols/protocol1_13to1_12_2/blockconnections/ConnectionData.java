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
package com.viaversion.viaversion.protocols.protocol1_13to1_12_2.blockconnections;

import com.github.steveice10.opennbt.tag.builtin.ByteArrayTag;
import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.IntArrayTag;
import com.github.steveice10.opennbt.tag.builtin.ListTag;
import com.github.steveice10.opennbt.tag.builtin.NumberTag;
import com.github.steveice10.opennbt.tag.builtin.StringTag;
import com.github.steveice10.opennbt.tag.builtin.Tag;
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
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.blockconnections.providers.UserBlockData;
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
    static final Object2IntMap<String> KEY_TO_ID = new Object2IntOpenHashMap<>(8582, .99F);
    static final IntSet OCCLUDING_STATES = new IntOpenHashSet(377, .99F);
    static Int2ObjectMap<ConnectionHandler> connectionHandlerMap = new Int2ObjectOpenHashMap<>();
    static Int2ObjectMap<BlockData> blockConnectionData = new Int2ObjectOpenHashMap<>();
    private static final BlockChangeRecord1_8[] EMPTY_RECORDS = new BlockChangeRecord1_8[0];

    static {
        KEY_TO_ID.defaultReturnValue(-1);
    }

    public static void update(UserConnection user, Position position) throws Exception {
        Boolean inSync = null;

        for (BlockFace face : BlockFace.values()) {
            Position pos = position.getRelative(face);
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

            PacketWrapper blockUpdatePacket = PacketWrapper.create(ClientboundPackets1_13.BLOCK_CHANGE, null, user);
            blockUpdatePacket.write(Type.POSITION1_8, pos);
            blockUpdatePacket.write(Type.VAR_INT, newBlockState);
            blockUpdatePacket.send(Protocol1_13To1_12_2.class);
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

    public static void markModified(UserConnection connection, Position pos) {
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

                Position position = new Position(xOff + ChunkSection.xFromIndex(idx), yOff + ChunkSection.yFromIndex(idx), zOff + ChunkSection.zFromIndex(idx));
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

        connectionHandlerMap = new Int2ObjectOpenHashMap<>(3650, .99F);

        if (!Via.getConfig().isReduceBlockStorageMemory()) {
            blockConnectionData = new Int2ObjectOpenHashMap<>(2048);

            ListTag<CompoundTag> blockConnectionMappings = MappingDataLoader.INSTANCE.loadNBT("blockConnections.nbt").getListTag("data", CompoundTag.class);
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
        }

        for (String state : occludingBlockStates()) {
            OCCLUDING_STATES.add(KEY_TO_ID.getInt(state));
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

    public static int connect(UserConnection user, Position position, int blockState) {
        ConnectionHandler handler = connectionHandlerMap.get(blockState);
        return handler != null ? handler.connect(user, position, blockState) : blockState;
    }

    public static ConnectionHandler getConnectionHandler(int blockstate) {
        return connectionHandlerMap.get(blockstate);
    }

    public static int getId(String key) {
        return KEY_TO_ID.getOrDefault(Key.stripMinecraftNamespace(key), -1);
    }

    private static String[] occludingBlockStates() {
        return new String[]{
                "stone",
                "granite",
                "polished_granite",
                "diorite",
                "polished_diorite",
                "andesite",
                "polished_andesite",
                "grass_block[snowy=false]",
                "dirt",
                "coarse_dirt",
                "podzol[snowy=false]",
                "cobblestone",
                "oak_planks",
                "spruce_planks",
                "birch_planks",
                "jungle_planks",
                "acacia_planks",
                "dark_oak_planks",
                "bedrock",
                "sand",
                "red_sand",
                "gravel",
                "gold_ore",
                "iron_ore",
                "coal_ore",
                "oak_log[axis=x]",
                "oak_log[axis=y]",
                "oak_log[axis=z]",
                "spruce_log[axis=x]",
                "spruce_log[axis=y]",
                "spruce_log[axis=z]",
                "birch_log[axis=x]",
                "birch_log[axis=y]",
                "birch_log[axis=z]",
                "jungle_log[axis=x]",
                "jungle_log[axis=y]",
                "jungle_log[axis=z]",
                "acacia_log[axis=x]",
                "acacia_log[axis=y]",
                "acacia_log[axis=z]",
                "dark_oak_log[axis=x]",
                "dark_oak_log[axis=y]",
                "dark_oak_log[axis=z]",
                "oak_wood[axis=y]",
                "spruce_wood[axis=y]",
                "birch_wood[axis=y]",
                "jungle_wood[axis=y]",
                "acacia_wood[axis=y]",
                "dark_oak_wood[axis=y]",
                "sponge",
                "wet_sponge",
                "lapis_ore",
                "lapis_block",
                "dispenser[facing=north,triggered=true]",
                "dispenser[facing=north,triggered=false]",
                "dispenser[facing=east,triggered=true]",
                "dispenser[facing=east,triggered=false]",
                "dispenser[facing=south,triggered=true]",
                "dispenser[facing=south,triggered=false]",
                "dispenser[facing=west,triggered=true]",
                "dispenser[facing=west,triggered=false]",
                "dispenser[facing=up,triggered=true]",
                "dispenser[facing=up,triggered=false]",
                "dispenser[facing=down,triggered=true]",
                "dispenser[facing=down,triggered=false]",
                "sandstone",
                "chiseled_sandstone",
                "cut_sandstone",
                "note_block[instrument=harp,note=0,powered=false]",
                "white_wool",
                "orange_wool",
                "magenta_wool",
                "light_blue_wool",
                "yellow_wool",
                "lime_wool",
                "pink_wool",
                "gray_wool",
                "light_gray_wool",
                "cyan_wool",
                "purple_wool",
                "blue_wool",
                "brown_wool",
                "green_wool",
                "red_wool",
                "black_wool",
                "gold_block",
                "iron_block",
                "bricks",
                "bookshelf",
                "mossy_cobblestone",
                "obsidian",
                "spawner",
                "diamond_ore",
                "diamond_block",
                "crafting_table",
                "furnace[facing=north,lit=true]",
                "furnace[facing=north,lit=false]",
                "furnace[facing=south,lit=true]",
                "furnace[facing=south,lit=false]",
                "furnace[facing=west,lit=true]",
                "furnace[facing=west,lit=false]",
                "furnace[facing=east,lit=true]",
                "furnace[facing=east,lit=false]",
                "redstone_ore[lit=true]",
                "redstone_ore[lit=false]",
                "snow_block",
                "clay",
                "jukebox[has_record=true]",
                "jukebox[has_record=false]",
                "netherrack",
                "soul_sand",
                "carved_pumpkin[facing=north]",
                "carved_pumpkin[facing=south]",
                "carved_pumpkin[facing=west]",
                "carved_pumpkin[facing=east]",
                "jack_o_lantern[facing=north]",
                "jack_o_lantern[facing=south]",
                "jack_o_lantern[facing=west]",
                "jack_o_lantern[facing=east]",
                "infested_stone",
                "infested_cobblestone",
                "infested_stone_bricks",
                "infested_mossy_stone_bricks",
                "infested_cracked_stone_bricks",
                "infested_chiseled_stone_bricks",
                "stone_bricks",
                "mossy_stone_bricks",
                "cracked_stone_bricks",
                "chiseled_stone_bricks",
                "brown_mushroom_block[down=true,east=true,north=true,south=true,up=true,west=true]",
                "brown_mushroom_block[down=false,east=true,north=true,south=false,up=true,west=false]",
                "brown_mushroom_block[down=false,east=true,north=false,south=true,up=true,west=false]",
                "brown_mushroom_block[down=false,east=true,north=false,south=false,up=true,west=false]",
                "brown_mushroom_block[down=false,east=false,north=true,south=false,up=true,west=true]",
                "brown_mushroom_block[down=false,east=false,north=true,south=false,up=true,west=false]",
                "brown_mushroom_block[down=false,east=false,north=false,south=true,up=true,west=true]",
                "brown_mushroom_block[down=false,east=false,north=false,south=true,up=true,west=false]",
                "brown_mushroom_block[down=false,east=false,north=false,south=false,up=true,west=true]",
                "brown_mushroom_block[down=false,east=false,north=false,south=false,up=true,west=false]",
                "brown_mushroom_block[down=false,east=false,north=false,south=false,up=false,west=false]",
                "red_mushroom_block[down=true,east=true,north=true,south=true,up=true,west=true]",
                "red_mushroom_block[down=false,east=true,north=true,south=false,up=true,west=false]",
                "red_mushroom_block[down=false,east=true,north=false,south=true,up=true,west=false]",
                "red_mushroom_block[down=false,east=true,north=false,south=false,up=true,west=false]",
                "red_mushroom_block[down=false,east=false,north=true,south=false,up=true,west=true]",
                "red_mushroom_block[down=false,east=false,north=true,south=false,up=true,west=false]",
                "red_mushroom_block[down=false,east=false,north=false,south=true,up=true,west=true]",
                "red_mushroom_block[down=false,east=false,north=false,south=true,up=true,west=false]",
                "red_mushroom_block[down=false,east=false,north=false,south=false,up=true,west=true]",
                "red_mushroom_block[down=false,east=false,north=false,south=false,up=true,west=false]",
                "red_mushroom_block[down=false,east=false,north=false,south=false,up=false,west=false]",
                "mushroom_stem[down=true,east=true,north=true,south=true,up=true,west=true]",
                "mushroom_stem[down=false,east=true,north=true,south=true,up=false,west=true]",
                "melon",
                "mycelium[snowy=false]",
                "nether_bricks",
                "end_stone",
                "redstone_lamp[lit=true]",
                "redstone_lamp[lit=false]",
                "emerald_ore",
                "emerald_block",
                "command_block[conditional=true,facing=north]",
                "command_block[conditional=true,facing=east]",
                "command_block[conditional=true,facing=south]",
                "command_block[conditional=true,facing=west]",
                "command_block[conditional=true,facing=up]",
                "command_block[conditional=true,facing=down]",
                "command_block[conditional=false,facing=north]",
                "command_block[conditional=false,facing=east]",
                "command_block[conditional=false,facing=south]",
                "command_block[conditional=false,facing=west]",
                "command_block[conditional=false,facing=up]",
                "command_block[conditional=false,facing=down]",
                "nether_quartz_ore",
                "quartz_block",
                "chiseled_quartz_block",
                "quartz_pillar[axis=x]",
                "quartz_pillar[axis=y]",
                "quartz_pillar[axis=z]",
                "dropper[facing=north,triggered=true]",
                "dropper[facing=north,triggered=false]",
                "dropper[facing=east,triggered=true]",
                "dropper[facing=east,triggered=false]",
                "dropper[facing=south,triggered=true]",
                "dropper[facing=south,triggered=false]",
                "dropper[facing=west,triggered=true]",
                "dropper[facing=west,triggered=false]",
                "dropper[facing=up,triggered=true]",
                "dropper[facing=up,triggered=false]",
                "dropper[facing=down,triggered=true]",
                "dropper[facing=down,triggered=false]",
                "white_terracotta",
                "orange_terracotta",
                "magenta_terracotta",
                "light_blue_terracotta",
                "yellow_terracotta",
                "lime_terracotta",
                "pink_terracotta",
                "gray_terracotta",
                "light_gray_terracotta",
                "cyan_terracotta",
                "purple_terracotta",
                "blue_terracotta",
                "brown_terracotta",
                "green_terracotta",
                "red_terracotta",
                "black_terracotta",
                "slime_block",
                "barrier",
                "prismarine",
                "prismarine_bricks",
                "dark_prismarine",
                "hay_block[axis=x]",
                "hay_block[axis=y]",
                "hay_block[axis=z]",
                "terracotta",
                "coal_block",
                "packed_ice",
                "red_sandstone",
                "chiseled_red_sandstone",
                "cut_red_sandstone",
                "oak_slab[type=double,waterlogged=false]",
                "spruce_slab[type=double,waterlogged=false]",
                "birch_slab[type=double,waterlogged=false]",
                "jungle_slab[type=double,waterlogged=false]",
                "acacia_slab[type=double,waterlogged=false]",
                "dark_oak_slab[type=double,waterlogged=false]",
                "stone_slab[type=double,waterlogged=false]",
                "sandstone_slab[type=double,waterlogged=false]",
                "petrified_oak_slab[type=double,waterlogged=false]",
                "cobblestone_slab[type=double,waterlogged=false]",
                "brick_slab[type=double,waterlogged=false]",
                "stone_brick_slab[type=double,waterlogged=false]",
                "nether_brick_slab[type=double,waterlogged=false]",
                "quartz_slab[type=double,waterlogged=false]",
                "red_sandstone_slab[type=double,waterlogged=false]",
                "purpur_slab[type=double,waterlogged=false]",
                "smooth_stone",
                "smooth_sandstone",
                "smooth_quartz",
                "smooth_red_sandstone",
                "purpur_block",
                "purpur_pillar[axis=x]",
                "purpur_pillar[axis=y]",
                "purpur_pillar[axis=z]",
                "end_stone_bricks",
                "repeating_command_block[conditional=true,facing=north]",
                "repeating_command_block[conditional=true,facing=east]",
                "repeating_command_block[conditional=true,facing=south]",
                "repeating_command_block[conditional=true,facing=west]",
                "repeating_command_block[conditional=true,facing=up]",
                "repeating_command_block[conditional=true,facing=down]",
                "repeating_command_block[conditional=false,facing=north]",
                "repeating_command_block[conditional=false,facing=east]",
                "repeating_command_block[conditional=false,facing=south]",
                "repeating_command_block[conditional=false,facing=west]",
                "repeating_command_block[conditional=false,facing=up]",
                "repeating_command_block[conditional=false,facing=down]",
                "chain_command_block[conditional=true,facing=north]",
                "chain_command_block[conditional=true,facing=east]",
                "chain_command_block[conditional=true,facing=south]",
                "chain_command_block[conditional=true,facing=west]",
                "chain_command_block[conditional=true,facing=up]",
                "chain_command_block[conditional=true,facing=down]",
                "chain_command_block[conditional=false,facing=north]",
                "chain_command_block[conditional=false,facing=east]",
                "chain_command_block[conditional=false,facing=south]",
                "chain_command_block[conditional=false,facing=west]",
                "chain_command_block[conditional=false,facing=up]",
                "chain_command_block[conditional=false,facing=down]",
                "magma_block",
                "nether_wart_block",
                "red_nether_bricks",
                "bone_block[axis=x]",
                "bone_block[axis=y]",
                "bone_block[axis=z]",
                "white_glazed_terracotta[facing=north]",
                "white_glazed_terracotta[facing=south]",
                "white_glazed_terracotta[facing=west]",
                "white_glazed_terracotta[facing=east]",
                "orange_glazed_terracotta[facing=north]",
                "orange_glazed_terracotta[facing=south]",
                "orange_glazed_terracotta[facing=west]",
                "orange_glazed_terracotta[facing=east]",
                "magenta_glazed_terracotta[facing=north]",
                "magenta_glazed_terracotta[facing=south]",
                "magenta_glazed_terracotta[facing=west]",
                "magenta_glazed_terracotta[facing=east]",
                "light_blue_glazed_terracotta[facing=north]",
                "light_blue_glazed_terracotta[facing=south]",
                "light_blue_glazed_terracotta[facing=west]",
                "light_blue_glazed_terracotta[facing=east]",
                "yellow_glazed_terracotta[facing=north]",
                "yellow_glazed_terracotta[facing=south]",
                "yellow_glazed_terracotta[facing=west]",
                "yellow_glazed_terracotta[facing=east]",
                "lime_glazed_terracotta[facing=north]",
                "lime_glazed_terracotta[facing=south]",
                "lime_glazed_terracotta[facing=west]",
                "lime_glazed_terracotta[facing=east]",
                "pink_glazed_terracotta[facing=north]",
                "pink_glazed_terracotta[facing=south]",
                "pink_glazed_terracotta[facing=west]",
                "pink_glazed_terracotta[facing=east]",
                "gray_glazed_terracotta[facing=north]",
                "gray_glazed_terracotta[facing=south]",
                "gray_glazed_terracotta[facing=west]",
                "gray_glazed_terracotta[facing=east]",
                "light_gray_glazed_terracotta[facing=north]",
                "light_gray_glazed_terracotta[facing=south]",
                "light_gray_glazed_terracotta[facing=west]",
                "light_gray_glazed_terracotta[facing=east]",
                "cyan_glazed_terracotta[facing=north]",
                "cyan_glazed_terracotta[facing=south]",
                "cyan_glazed_terracotta[facing=west]",
                "cyan_glazed_terracotta[facing=east]",
                "purple_glazed_terracotta[facing=north]",
                "purple_glazed_terracotta[facing=south]",
                "purple_glazed_terracotta[facing=west]",
                "purple_glazed_terracotta[facing=east]",
                "blue_glazed_terracotta[facing=north]",
                "blue_glazed_terracotta[facing=south]",
                "blue_glazed_terracotta[facing=west]",
                "blue_glazed_terracotta[facing=east]",
                "brown_glazed_terracotta[facing=north]",
                "brown_glazed_terracotta[facing=south]",
                "brown_glazed_terracotta[facing=west]",
                "brown_glazed_terracotta[facing=east]",
                "green_glazed_terracotta[facing=north]",
                "green_glazed_terracotta[facing=south]",
                "green_glazed_terracotta[facing=west]",
                "green_glazed_terracotta[facing=east]",
                "red_glazed_terracotta[facing=north]",
                "red_glazed_terracotta[facing=south]",
                "red_glazed_terracotta[facing=west]",
                "red_glazed_terracotta[facing=east]",
                "black_glazed_terracotta[facing=north]",
                "black_glazed_terracotta[facing=south]",
                "black_glazed_terracotta[facing=west]",
                "black_glazed_terracotta[facing=east]",
                "white_concrete",
                "orange_concrete",
                "magenta_concrete",
                "light_blue_concrete",
                "yellow_concrete",
                "lime_concrete",
                "pink_concrete",
                "gray_concrete",
                "light_gray_concrete",
                "cyan_concrete",
                "purple_concrete",
                "blue_concrete",
                "brown_concrete",
                "green_concrete",
                "red_concrete",
                "black_concrete",
                "white_concrete_powder",
                "orange_concrete_powder",
                "magenta_concrete_powder",
                "light_blue_concrete_powder",
                "yellow_concrete_powder",
                "lime_concrete_powder",
                "pink_concrete_powder",
                "gray_concrete_powder",
                "light_gray_concrete_powder",
                "cyan_concrete_powder",
                "purple_concrete_powder",
                "blue_concrete_powder",
                "brown_concrete_powder",
                "green_concrete_powder",
                "red_concrete_powder",
                "black_concrete_powder",
                "structure_block[mode=save]",
                "structure_block[mode=load]",
                "structure_block[mode=corner]",
                "structure_block[mode=data]",
                "glowstone"
        };
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

        public void updateChunkSectionNeighbours(int chunkX, int chunkZ, int chunkSectionY) throws Exception {
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
                        PacketWrapper wrapper = PacketWrapper.create(ClientboundPackets1_13.MULTI_BLOCK_CHANGE, null, user);
                        wrapper.write(Type.INT, chunkX + chunkDeltaX);
                        wrapper.write(Type.INT, chunkZ + chunkDeltaZ);
                        wrapper.write(Type.BLOCK_CHANGE_RECORD_ARRAY, updates.toArray(EMPTY_RECORDS));
                        wrapper.send(Protocol1_13To1_12_2.class);
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

            Position pos = new Position(x, y, z);
            int newBlockState = handler.connect(user, pos, blockState);
            if (blockState != newBlockState || !blockConnectionProvider.storesBlocks(user, null)) {
                records.add(new BlockChangeRecord1_8(x & 0xF, y, z & 0xF, newBlockState));
                updateBlockStorage(user, x, y, z, newBlockState);
            }
        }
    }
}

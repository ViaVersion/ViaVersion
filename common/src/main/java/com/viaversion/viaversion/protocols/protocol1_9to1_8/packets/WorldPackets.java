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
package com.viaversion.viaversion.protocols.protocol1_9to1_8.packets;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.StringTag;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.minecraft.BlockFace;
import com.viaversion.viaversion.api.minecraft.ClientWorld;
import com.viaversion.viaversion.api.minecraft.Position;
import com.viaversion.viaversion.api.minecraft.chunks.BaseChunk;
import com.viaversion.viaversion.api.minecraft.chunks.Chunk;
import com.viaversion.viaversion.api.minecraft.chunks.ChunkSection;
import com.viaversion.viaversion.api.minecraft.item.DataItem;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.chunk.BulkChunkType1_8;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_8;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_9_1;
import com.viaversion.viaversion.protocols.protocol1_8.ClientboundPackets1_8;
import com.viaversion.viaversion.protocols.protocol1_8.ServerboundPackets1_8;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.ClientboundPackets1_9;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.ItemRewriter;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.Protocol1_9To1_8;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.ServerboundPackets1_9;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.providers.CommandBlockProvider;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.sounds.Effect;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.sounds.SoundEffect;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.storage.ClientChunks;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.storage.EntityTracker1_9;
import com.viaversion.viaversion.util.ComponentUtil;
import com.viaversion.viaversion.util.Key;
import java.util.ArrayList;
import java.util.Optional;

public class WorldPackets {
    public static void register(Protocol1_9To1_8 protocol) {
        protocol.registerClientbound(ClientboundPackets1_8.UPDATE_SIGN, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.POSITION1_8); // 0 - Sign Position
                handler(wrapper -> {
                    for (int i = 0; i < 4; i++) {
                        final String line = wrapper.read(Type.STRING); // Should be Type.COMPONENT but would break in some cases
                        Protocol1_9To1_8.STRING_TO_JSON.write(wrapper, line);
                    }
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_8.EFFECT, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.INT); // 0 - Effect ID
                map(Type.POSITION1_8); // 1 - Position
                map(Type.INT); // 2 - Data
                map(Type.BOOLEAN); // 3 - Disable relative volume

                handler(wrapper -> {
                    int id = wrapper.get(Type.INT, 0);

                    id = Effect.getNewId(id);
                    wrapper.set(Type.INT, 0, id);
                });
                // Rewrite potion effect as it changed to use a dynamic registry
                handler(wrapper -> {
                    int id = wrapper.get(Type.INT, 0);
                    if (id == 2002) {
                        int data = wrapper.get(Type.INT, 1);
                        int newData = ItemRewriter.getNewEffectID(data);
                        wrapper.set(Type.INT, 1, newData);
                    }
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_8.NAMED_SOUND, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.STRING); // 0 - Sound Name
                // 1 - Sound Category ID
                // Everything else gets written through

                handler(wrapper -> {
                    String name = Key.stripMinecraftNamespace(wrapper.get(Type.STRING, 0));

                    SoundEffect effect = SoundEffect.getByName(name);
                    int catid = 0;
                    String newname = name;
                    if (effect != null) {
                        catid = effect.getCategory().getId();
                        newname = effect.getNewName();
                    }
                    wrapper.set(Type.STRING, 0, newname);
                    wrapper.write(Type.VAR_INT, catid); // Write Category ID
                    if (effect != null && effect.isBreakSound()) {
                        EntityTracker1_9 tracker = wrapper.user().getEntityTracker(Protocol1_9To1_8.class);
                        int x = wrapper.passthrough(Type.INT); //Position X
                        int y = wrapper.passthrough(Type.INT); //Position Y
                        int z = wrapper.passthrough(Type.INT); //Position Z
                        if (tracker.interactedBlockRecently((int) Math.floor(x / 8.0), (int) Math.floor(y / 8.0), (int) Math.floor(z / 8.0))) {
                            wrapper.cancel();
                        }
                    }
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_8.CHUNK_DATA, wrapper -> {
            ClientWorld clientWorld = wrapper.user().get(ClientWorld.class);
            ClientChunks clientChunks = wrapper.user().get(ClientChunks.class);
            Chunk chunk = wrapper.read(ChunkType1_8.forEnvironment(clientWorld.getEnvironment()));

            long chunkHash = ClientChunks.toLong(chunk.getX(), chunk.getZ());

            // Check if the chunk should be handled as an unload packet
            if (chunk.isFullChunk() && chunk.getBitmask() == 0) {
                wrapper.setPacketType(ClientboundPackets1_9.UNLOAD_CHUNK);
                wrapper.write(Type.INT, chunk.getX());
                wrapper.write(Type.INT, chunk.getZ());

                // Remove commandBlocks on chunk unload
                CommandBlockProvider provider = Via.getManager().getProviders().get(CommandBlockProvider.class);
                provider.unloadChunk(wrapper.user(), chunk.getX(), chunk.getZ());

                clientChunks.getLoadedChunks().remove(chunkHash);

                // Unload the empty chunks
                if (Via.getConfig().isChunkBorderFix()) {
                    for (BlockFace face : BlockFace.HORIZONTAL) {
                        int chunkX = chunk.getX() + face.modX();
                        int chunkZ = chunk.getZ() + face.modZ();
                        if (!clientChunks.getLoadedChunks().contains(ClientChunks.toLong(chunkX, chunkZ))) {
                            PacketWrapper unloadChunk = wrapper.create(ClientboundPackets1_9.UNLOAD_CHUNK);
                            unloadChunk.write(Type.INT, chunkX);
                            unloadChunk.write(Type.INT, chunkZ);
                            unloadChunk.send(Protocol1_9To1_8.class);
                        }
                    }
                }
            } else {
                Type<Chunk> chunkType = ChunkType1_9_1.forEnvironment(clientWorld.getEnvironment());
                wrapper.write(chunkType, chunk);

                clientChunks.getLoadedChunks().add(chunkHash);

                // Send empty chunks surrounding the loaded chunk to force 1.9+ clients to render the new chunk
                if (Via.getConfig().isChunkBorderFix()) {
                    for (BlockFace face : BlockFace.HORIZONTAL) {
                        int chunkX = chunk.getX() + face.modX();
                        int chunkZ = chunk.getZ() + face.modZ();
                        if (!clientChunks.getLoadedChunks().contains(ClientChunks.toLong(chunkX, chunkZ))) {
                            PacketWrapper emptyChunk = wrapper.create(ClientboundPackets1_9.CHUNK_DATA);
                            Chunk c = new BaseChunk(chunkX, chunkZ, true, false, 0, new ChunkSection[16], new int[256], new ArrayList<>());
                            emptyChunk.write(chunkType, c);
                            emptyChunk.send(Protocol1_9To1_8.class);
                        }
                    }
                }
            }
        });

        protocol.registerClientbound(ClientboundPackets1_8.MAP_BULK_CHUNK, null, wrapper -> {
            wrapper.cancel(); // Cancel the packet from being sent
            ClientWorld clientWorld = wrapper.user().get(ClientWorld.class);
            ClientChunks clientChunks = wrapper.user().get(ClientChunks.class);
            Chunk[] chunks = wrapper.read(BulkChunkType1_8.TYPE);

            Type<Chunk> chunkType = ChunkType1_9_1.forEnvironment(clientWorld.getEnvironment());
            // Split into multiple chunk packets
            for (Chunk chunk : chunks) {
                PacketWrapper chunkData = wrapper.create(ClientboundPackets1_9.CHUNK_DATA);
                chunkData.write(chunkType, chunk);
                chunkData.send(Protocol1_9To1_8.class);

                clientChunks.getLoadedChunks().add(ClientChunks.toLong(chunk.getX(), chunk.getZ()));

                // Send empty chunks surrounding the loaded chunk to force 1.9+ clients to render the new chunk
                if (Via.getConfig().isChunkBorderFix()) {
                    for (BlockFace face : BlockFace.HORIZONTAL) {
                        int chunkX = chunk.getX() + face.modX();
                        int chunkZ = chunk.getZ() + face.modZ();
                        if (!clientChunks.getLoadedChunks().contains(ClientChunks.toLong(chunkX, chunkZ))) {
                            PacketWrapper emptyChunk = wrapper.create(ClientboundPackets1_9.CHUNK_DATA);
                            Chunk c = new BaseChunk(chunkX, chunkZ, true, false, 0, new ChunkSection[16], new int[256], new ArrayList<>());
                            emptyChunk.write(chunkType, c);
                            emptyChunk.send(Protocol1_9To1_8.class);
                        }
                    }
                }
            }
        });

        protocol.registerClientbound(ClientboundPackets1_8.BLOCK_ENTITY_DATA, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.POSITION1_8); // 0 - Block Position
                map(Type.UNSIGNED_BYTE); // 1 - Action
                map(Type.NAMED_COMPOUND_TAG); // 2 - NBT (Might not be present)
                handler(wrapper -> {
                    int action = wrapper.get(Type.UNSIGNED_BYTE, 0);
                    if (action == 1) { // Update Spawner
                        CompoundTag tag = wrapper.get(Type.NAMED_COMPOUND_TAG, 0);
                        if (tag != null) {
                            StringTag entityId = tag.getStringTag("EntityId");
                            if (entityId != null) {
                                String entity = entityId.getValue();
                                CompoundTag spawn = new CompoundTag();
                                spawn.putString("id", entity);
                                tag.put("SpawnData", spawn);
                            } else { // EntityID does not exist
                                CompoundTag spawn = new CompoundTag();
                                spawn.putString("id", "AreaEffectCloud"); //Make spawners show up as empty when no EntityId is given.
                                tag.put("SpawnData", spawn);
                            }
                        }
                    }
                    if (action == 2) { // Update Command Block
                        CommandBlockProvider provider = Via.getManager().getProviders().get(CommandBlockProvider.class);
                        provider.addOrUpdateBlock(wrapper.user(), wrapper.get(Type.POSITION1_8, 0), wrapper.get(Type.NAMED_COMPOUND_TAG, 0));

                        // To prevent window issues don't send updates
                        wrapper.cancel();
                    }
                });
            }
        });


        /* Incoming Packets */
        protocol.registerServerbound(ServerboundPackets1_9.UPDATE_SIGN, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.POSITION1_8); // 0 - Sign Position
                handler(wrapper -> {
                    for (int i = 0; i < 4; i++) {
                        final String line = wrapper.read(Type.STRING);
                        wrapper.write(Type.COMPONENT, ComponentUtil.plainToJson(line));
                    }
                });
            }
        });

        protocol.registerServerbound(ServerboundPackets1_9.PLAYER_DIGGING, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT); // Action
                map(Type.POSITION1_8); // Position
                handler(wrapper -> {
                    int status = wrapper.get(Type.VAR_INT, 0);
                    if (status == 6)
                        wrapper.cancel();
                });
                // Blocking
                handler(wrapper -> {
                    int status = wrapper.get(Type.VAR_INT, 0);
                    if (status == 5 || status == 4 || status == 3) {
                        EntityTracker1_9 entityTracker = wrapper.user().getEntityTracker(Protocol1_9To1_8.class);
                        if (entityTracker.isBlocking()) {
                            entityTracker.setBlocking(false);
                            if (!Via.getConfig().isShowShieldWhenSwordInHand()) {
                                entityTracker.setSecondHand(null);
                            }
                        }
                    }
                });
            }
        });

        protocol.registerServerbound(ServerboundPackets1_9.USE_ITEM, null, wrapper -> {
            int hand = wrapper.read(Type.VAR_INT);
            // Wipe the input buffer
            wrapper.clearInputBuffer();
            wrapper.setPacketType(ServerboundPackets1_8.PLAYER_BLOCK_PLACEMENT);
            wrapper.write(Type.POSITION1_8, new Position(-1, (short) -1, -1));
            wrapper.write(Type.UNSIGNED_BYTE, (short) 255);
            // Write item in hand
            Item item = Protocol1_9To1_8.getHandItem(wrapper.user());
            // Blocking patch
            if (Via.getConfig().isShieldBlocking()) {
                EntityTracker1_9 tracker = wrapper.user().getEntityTracker(Protocol1_9To1_8.class);

                // Check if the shield is already there or if we have to give it here
                boolean showShieldWhenSwordInHand = Via.getConfig().isShowShieldWhenSwordInHand();

                // Method to identify the sword in hand
                boolean isSword = showShieldWhenSwordInHand ? tracker.hasSwordInHand()
                        : item != null && Protocol1_9To1_8.isSword(item.identifier());

                if (isSword) {
                    if (hand == 0 && !tracker.isBlocking()) {
                        tracker.setBlocking(true);

                        // Check if the shield is already in the offhand
                        if (!showShieldWhenSwordInHand && tracker.getItemInSecondHand() == null) {

                            // Set shield in offhand when interacting with main hand
                            Item shield = new DataItem(442, (byte) 1, (short) 0, null);
                            tracker.setSecondHand(shield);
                        }
                    }

                    // Use the main hand to trigger the blocking
                    boolean blockUsingMainHand = Via.getConfig().isNoDelayShieldBlocking()
                            && !showShieldWhenSwordInHand;

                    if (blockUsingMainHand && hand == 1 || !blockUsingMainHand && hand == 0) {
                        wrapper.cancel();
                    }
                } else {
                    if (!showShieldWhenSwordInHand) {
                        // Remove the shield from the offhand
                        tracker.setSecondHand(null);
                    }
                    tracker.setBlocking(false);
                }
            }
            wrapper.write(Type.ITEM1_8, item);

            wrapper.write(Type.UNSIGNED_BYTE, (short) 0);
            wrapper.write(Type.UNSIGNED_BYTE, (short) 0);
            wrapper.write(Type.UNSIGNED_BYTE, (short) 0);
        });

        protocol.registerServerbound(ServerboundPackets1_9.PLAYER_BLOCK_PLACEMENT, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.POSITION1_8); // 0 - Position
                map(Type.VAR_INT, Type.UNSIGNED_BYTE); // 1 - Block Face
                handler(wrapper -> {
                    final int hand = wrapper.read(Type.VAR_INT); // 2 - Hand
                    if (hand != 0) wrapper.cancel();
                });
                handler(wrapper -> {
                    Item item = Protocol1_9To1_8.getHandItem(wrapper.user());
                    wrapper.write(Type.ITEM1_8, item); // 3 - Item
                });
                map(Type.UNSIGNED_BYTE); // 4 - X
                map(Type.UNSIGNED_BYTE); // 5 - Y
                map(Type.UNSIGNED_BYTE); // 6 - Z

                //Register block place to fix sounds
                handler(wrapper -> {
                    int face = wrapper.get(Type.UNSIGNED_BYTE, 0);
                    if (face == 255)
                        return;
                    Position p = wrapper.get(Type.POSITION1_8, 0);
                    int x = p.x();
                    int y = p.y();
                    int z = p.z();
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
                    EntityTracker1_9 tracker = wrapper.user().getEntityTracker(Protocol1_9To1_8.class);
                    tracker.addBlockInteraction(new Position(x, y, z));
                });

                // Handle CommandBlocks
                handler(wrapper -> {
                    CommandBlockProvider provider = Via.getManager().getProviders().get(CommandBlockProvider.class);

                    Position pos = wrapper.get(Type.POSITION1_8, 0);
                    Optional<CompoundTag> tag = provider.get(wrapper.user(), pos);
                    // Send the Update Block Entity packet if present
                    if (tag.isPresent()) {
                        PacketWrapper updateBlockEntity = PacketWrapper.create(ClientboundPackets1_9.BLOCK_ENTITY_DATA, null, wrapper.user());

                        updateBlockEntity.write(Type.POSITION1_8, pos);
                        updateBlockEntity.write(Type.UNSIGNED_BYTE, (short) 2);
                        updateBlockEntity.write(Type.NAMED_COMPOUND_TAG, tag.get());

                        updateBlockEntity.scheduleSend(Protocol1_9To1_8.class);
                    }
                });

            }
        });
    }

}

/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2021 ViaVersion and contributors
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
import com.viaversion.viaversion.api.minecraft.Position;
import com.viaversion.viaversion.api.minecraft.chunks.Chunk1_8;
import com.viaversion.viaversion.api.minecraft.item.DataItem;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.protocol.remapper.PacketRemapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.CustomByteType;
import com.viaversion.viaversion.protocols.protocol1_8.ClientboundPackets1_8;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.ClientboundPackets1_9;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.ItemRewriter;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.Protocol1_9To1_8;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.ServerboundPackets1_9;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.providers.CommandBlockProvider;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.sounds.Effect;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.sounds.SoundEffect;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.storage.ClientChunks;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.storage.EntityTracker1_9;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.types.Chunk1_9to1_8Type;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Optional;

public class WorldPackets {
    public static void register(Protocol protocol) {
        protocol.registerClientbound(ClientboundPackets1_8.UPDATE_SIGN, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.POSITION); // 0 - Sign Position
                map(Type.STRING, Protocol1_9To1_8.FIX_JSON); // 1 - Sign Line (json)
                map(Type.STRING, Protocol1_9To1_8.FIX_JSON); // 2 - Sign Line (json)
                map(Type.STRING, Protocol1_9To1_8.FIX_JSON); // 3 - Sign Line (json)
                map(Type.STRING, Protocol1_9To1_8.FIX_JSON); // 4 - Sign Line (json)
            }
        });

        protocol.registerClientbound(ClientboundPackets1_8.EFFECT, new PacketRemapper() {
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

        protocol.registerClientbound(ClientboundPackets1_8.NAMED_SOUND, new PacketRemapper() {
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
                            EntityTracker1_9 tracker = wrapper.user().getEntityTracker(Protocol1_9To1_8.class);
                            int x = wrapper.passthrough(Type.INT); //Position X
                            int y = wrapper.passthrough(Type.INT); //Position Y
                            int z = wrapper.passthrough(Type.INT); //Position Z
                            if (tracker.interactedBlockRecently((int) Math.floor(x / 8.0), (int) Math.floor(y / 8.0), (int) Math.floor(z / 8.0))) {
                                wrapper.cancel();
                            }
                        }
                    }
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_8.CHUNK_DATA, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        ClientChunks clientChunks = wrapper.user().get(ClientChunks.class);
                        Chunk1_9to1_8Type type = new Chunk1_9to1_8Type(clientChunks);
                        Chunk1_8 chunk = (Chunk1_8) wrapper.read(type);
                        if (chunk.isUnloadPacket()) {
                            wrapper.setId(ClientboundPackets1_9.UNLOAD_CHUNK);

                            wrapper.write(Type.INT, chunk.getX());
                            wrapper.write(Type.INT, chunk.getZ());
                            // Remove commandBlocks on chunk unload
                            CommandBlockProvider provider = Via.getManager().getProviders().get(CommandBlockProvider.class);
                            provider.unloadChunk(wrapper.user(), chunk.getX(), chunk.getZ());
                        } else {
                            wrapper.write(type, chunk);
                            // eat any other data (Usually happens with unload packets)
                        }
                        wrapper.read(Type.REMAINING_BYTES);
                    }
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_8.MAP_BULK_CHUNK, null, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> {
                    wrapper.cancel(); // Cancel the packet from being sent

                    boolean skyLight = wrapper.read(Type.BOOLEAN);
                    int count = wrapper.read(Type.VAR_INT);

                    ChunkBulkSection[] chunks = new ChunkBulkSection[count];
                    for (int i = 0; i < count; i++) {
                        chunks[i] = new ChunkBulkSection(wrapper, skyLight);
                    }

                    ClientChunks clientChunks = wrapper.user().get(ClientChunks.class);
                    for (ChunkBulkSection chunk : chunks) {
                        // Data is at the end
                        CustomByteType customByteType = new CustomByteType(chunk.getLength());
                        chunk.setData(wrapper.read(customByteType));

                        clientChunks.getBulkChunks().add(ClientChunks.toLong(chunk.getX(), chunk.getZ())); // Store for later

                        // Construct chunk packet
                        ByteBuf buffer = null;
                        try {
                            buffer = wrapper.user().getChannel().alloc().buffer();

                            Type.INT.write(buffer, chunk.getX());
                            Type.INT.write(buffer, chunk.getZ());
                            Type.BOOLEAN.write(buffer, true); // Always ground-up
                            Type.UNSIGNED_SHORT.write(buffer, chunk.getBitMask());
                            Type.VAR_INT.writePrimitive(buffer, chunk.getLength());
                            customByteType.write(buffer, chunk.getData());

                            // Send through this protocol again
                            PacketWrapper chunkPacket = PacketWrapper.create(ClientboundPackets1_8.CHUNK_DATA, buffer, wrapper.user());
                            chunkPacket.send(Protocol1_9To1_8.class, false);
                        } finally {
                            if (buffer != null) {
                                buffer.release();
                            }
                        }
                    }
                });
            }
        });

        protocol.registerClientbound(ClientboundPackets1_8.BLOCK_ENTITY_DATA, new PacketRemapper() {
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
                                    CompoundTag spawn = new CompoundTag();
                                    spawn.put("id", new StringTag(entity));
                                    tag.put("SpawnData", spawn);
                                } else { // EntityID does not exist
                                    CompoundTag spawn = new CompoundTag();
                                    spawn.put("id", new StringTag("AreaEffectCloud")); //Make spawners show up as empty when no EntityId is given.
                                    tag.put("SpawnData", spawn);
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

        protocol.registerClientbound(ClientboundPackets1_8.BLOCK_CHANGE, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.POSITION);
                map(Type.VAR_INT);
            }
        });


        /* Incoming Packets */
        protocol.registerServerbound(ServerboundPackets1_9.UPDATE_SIGN, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.POSITION); // 0 - Sign Position
                map(Type.STRING, Protocol1_9To1_8.FIX_JSON); // 1 - Sign Line (json)
                map(Type.STRING, Protocol1_9To1_8.FIX_JSON); // 2 - Sign Line (json)
                map(Type.STRING, Protocol1_9To1_8.FIX_JSON); // 3 - Sign Line (json)
                map(Type.STRING, Protocol1_9To1_8.FIX_JSON); // 4 - Sign Line (json)
            }
        });

        protocol.registerServerbound(ServerboundPackets1_9.PLAYER_DIGGING, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT, Type.UNSIGNED_BYTE); // 0 - Status
                map(Type.POSITION); // 1 - Position
                map(Type.BYTE); // 2 - Face
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
                            EntityTracker1_9 entityTracker = wrapper.user().getEntityTracker(Protocol1_9To1_8.class);
                            if (entityTracker.isBlocking()) {
                                entityTracker.setBlocking(false);
                                if (!Via.getConfig().isShowShieldWhenSwordInHand()) {
                                    entityTracker.setSecondHand(null);
                                }
                            }
                        }
                    }
                });
            }
        });

        protocol.registerServerbound(ServerboundPackets1_9.USE_ITEM, null, new PacketRemapper() {
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
                        wrapper.write(Type.POSITION, new Position(-1, (short) -1, -1));
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
                                if (hand == 0) {
                                    if (!tracker.isBlocking()) {
                                        tracker.setBlocking(true);

                                        // Check if the shield is already in the offhand
                                        if (!showShieldWhenSwordInHand && tracker.getItemInSecondHand() == null) {

                                            // Set shield in offhand when interacting with main hand
                                            Item shield = new DataItem(442, (byte) 1, (short) 0, null);
                                            tracker.setSecondHand(shield);
                                        }
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
                        wrapper.write(Type.ITEM, item);

                        wrapper.write(Type.UNSIGNED_BYTE, (short) 0);
                        wrapper.write(Type.UNSIGNED_BYTE, (short) 0);
                        wrapper.write(Type.UNSIGNED_BYTE, (short) 0);
                    }
                });

            }
        });

        protocol.registerServerbound(ServerboundPackets1_9.PLAYER_BLOCK_PLACEMENT, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.POSITION); // 0 - Position
                map(Type.VAR_INT, Type.UNSIGNED_BYTE); // 1 - Block Face
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        final int hand = wrapper.read(Type.VAR_INT); // 2 - Hand
                        if (hand != 0) wrapper.cancel();
                    }
                });
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        Item item = Protocol1_9To1_8.getHandItem(wrapper.user());
                        wrapper.write(Type.ITEM, item); // 3 - Item
                    }
                });
                map(Type.UNSIGNED_BYTE); // 4 - X
                map(Type.UNSIGNED_BYTE); // 5 - Y
                map(Type.UNSIGNED_BYTE); // 6 - Z

                //Register block place to fix sounds
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int face = wrapper.get(Type.UNSIGNED_BYTE, 0);
                        if (face == 255)
                            return;
                        Position p = wrapper.get(Type.POSITION, 0);
                        int x = p.getX();
                        int y = p.getY();
                        int z = p.getZ();
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
                            PacketWrapper updateBlockEntity = PacketWrapper.create(0x09, null, wrapper.user());

                            updateBlockEntity.write(Type.POSITION, pos);
                            updateBlockEntity.write(Type.UNSIGNED_BYTE, (short) 2);
                            updateBlockEntity.write(Type.NBT, tag.get());

                            updateBlockEntity.scheduleSend(Protocol1_9To1_8.class);
                        }
                    }
                });

            }
        });
    }

    public static final class ChunkBulkSection {
        private final int x;
        private final int z;
        private final int bitMask;
        private final int length;
        private byte[] data;

        public ChunkBulkSection(PacketWrapper wrapper, boolean skylight) throws Exception {
            x = wrapper.read(Type.INT);
            z = wrapper.read(Type.INT);
            bitMask = wrapper.read(Type.UNSIGNED_SHORT);

            int bitCount = Integer.bitCount(bitMask);
            length = (bitCount * ((4096 * 2) + 2048)) + (skylight ? bitCount * 2048 : 0) + 256; // Thanks MCProtocolLib
        }

        public int getX() {
            return x;
        }

        public int getZ() {
            return z;
        }

        public int getBitMask() {
            return bitMask;
        }

        public int getLength() {
            return length;
        }

        public byte @Nullable [] getData() {
            return data;
        }

        public void setData(byte[] data) {
            this.data = data;
        }
    }
}

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
package us.myles.ViaVersion.protocols.protocol1_9to1_8.packets;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.StringTag;
import io.netty.buffer.ByteBuf;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.minecraft.Position;
import us.myles.ViaVersion.api.minecraft.chunks.Chunk1_8;
import us.myles.ViaVersion.api.minecraft.item.Item;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketHandler;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.remapper.ValueCreator;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.protocols.protocol1_8.ClientboundPackets1_8;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.ItemRewriter;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.Protocol1_9To1_8;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.ServerboundPackets1_9;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.providers.BulkChunkTranslatorProvider;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.providers.CommandBlockProvider;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.sounds.Effect;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.sounds.SoundEffect;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.storage.ClientChunks;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.storage.EntityTracker1_9;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.storage.PlaceBlockTracker;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.types.Chunk1_9to1_8Type;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class WorldPackets {
    public static void register(Protocol protocol) {
        protocol.registerOutgoing(ClientboundPackets1_8.UPDATE_SIGN, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.POSITION); // 0 - Sign Position
                map(Type.STRING, Protocol1_9To1_8.FIX_JSON); // 1 - Sign Line (json)
                map(Type.STRING, Protocol1_9To1_8.FIX_JSON); // 2 - Sign Line (json)
                map(Type.STRING, Protocol1_9To1_8.FIX_JSON); // 3 - Sign Line (json)
                map(Type.STRING, Protocol1_9To1_8.FIX_JSON); // 4 - Sign Line (json)
            }
        });

        protocol.registerOutgoing(ClientboundPackets1_8.EFFECT, new PacketRemapper() {
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

        protocol.registerOutgoing(ClientboundPackets1_8.NAMED_SOUND, new PacketRemapper() {
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
                            EntityTracker1_9 tracker = wrapper.user().get(EntityTracker1_9.class);
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

        protocol.registerOutgoing(ClientboundPackets1_8.CHUNK_DATA, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        ClientChunks clientChunks = wrapper.user().get(ClientChunks.class);
                        Chunk1_9to1_8Type type = new Chunk1_9to1_8Type(clientChunks);
                        Chunk1_8 chunk = (Chunk1_8) wrapper.read(type);
                        if (chunk.isUnloadPacket()) {
                            wrapper.setId(0x1D);

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

        protocol.registerOutgoing(ClientboundPackets1_8.MAP_BULK_CHUNK, null, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        wrapper.cancel(); // Cancel the packet from being sent
                        BulkChunkTranslatorProvider provider = Via.getManager().getProviders().get(BulkChunkTranslatorProvider.class);

                        // Don't read the packet
                        if (!provider.isPacketLevel())
                            return;

                        List<Object> list = provider.transformMapChunkBulk(wrapper, wrapper.user().get(ClientChunks.class));
                        for (Object obj : list) {
                            if (!(obj instanceof PacketWrapper))
                                throw new IOException("transformMapChunkBulk returned the wrong object type");

                            PacketWrapper output = (PacketWrapper) obj;
                            ByteBuf buffer = wrapper.user().getChannel().alloc().buffer();
                            try {
                                output.setId(-1); // -1 for no writing of id
                                output.writeToBuffer(buffer);
                                PacketWrapper chunkPacket = new PacketWrapper(0x21, buffer, wrapper.user());
                                chunkPacket.send(Protocol1_9To1_8.class, false, true);
                            } finally {
                                buffer.release();
                            }
                        }
                    }
                });
            }
        });

        protocol.registerOutgoing(ClientboundPackets1_8.BLOCK_ENTITY_DATA, new PacketRemapper() {
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

        protocol.registerOutgoing(ClientboundPackets1_8.BLOCK_CHANGE, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.POSITION);
                map(Type.VAR_INT);
            }
        });


        /* Incoming Packets */
        protocol.registerIncoming(ServerboundPackets1_9.UPDATE_SIGN, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.POSITION); // 0 - Sign Position
                map(Type.STRING, Protocol1_9To1_8.FIX_JSON); // 1 - Sign Line (json)
                map(Type.STRING, Protocol1_9To1_8.FIX_JSON); // 2 - Sign Line (json)
                map(Type.STRING, Protocol1_9To1_8.FIX_JSON); // 3 - Sign Line (json)
                map(Type.STRING, Protocol1_9To1_8.FIX_JSON); // 4 - Sign Line (json)
            }
        });

        protocol.registerIncoming(ServerboundPackets1_9.PLAYER_DIGGING, new PacketRemapper() {
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
                            EntityTracker1_9 entityTracker = wrapper.user().get(EntityTracker1_9.class);
                            if (entityTracker.isBlocking()) {
                                entityTracker.setBlocking(false);
                                entityTracker.setSecondHand(null);
                            }
                        }
                    }
                });
            }
        });

        protocol.registerIncoming(ServerboundPackets1_9.USE_ITEM, null, new PacketRemapper() {
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
                            EntityTracker1_9 tracker = wrapper.user().get(EntityTracker1_9.class);

                            if (item != null && Protocol1_9To1_8.isSword(item.getIdentifier())) {
                                if (hand == 0) {
                                    if (!tracker.isBlocking()) {
                                        tracker.setBlocking(true);
                                        Item shield = new Item(442, (byte) 1, (short) 0, null);
                                        tracker.setSecondHand(shield);
                                    }
                                    wrapper.cancel();
                                }
                            } else {
                                tracker.setSecondHand(null);
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

        protocol.registerIncoming(ServerboundPackets1_9.PLAYER_BLOCK_PLACEMENT, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.POSITION); // 0 - Position
                map(Type.VAR_INT, Type.UNSIGNED_BYTE); // 1 - Block Face
                map(Type.VAR_INT, Type.NOTHING); // 2 - Hand
                create(new ValueCreator() {
                    @Override
                    public void write(PacketWrapper wrapper) throws Exception {
                        Item item = Protocol1_9To1_8.getHandItem(wrapper.user());
                        wrapper.write(Type.ITEM, item); // 3 - Item
                    }
                });
                map(Type.UNSIGNED_BYTE); // 4 - X
                map(Type.UNSIGNED_BYTE); // 5 - Y
                map(Type.UNSIGNED_BYTE); // 6 - Z

                // Cancel if item as 1.9 uses Use_Item packet
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        Position position = wrapper.get(Type.POSITION, 0);
                        PlaceBlockTracker tracker = wrapper.user().get(PlaceBlockTracker.class);
                        if (tracker.getLastPlacedPosition() != null && tracker.getLastPlacedPosition().equals(position) && !tracker.isExpired(50))
                            wrapper.cancel();
                        tracker.updateTime();
                        tracker.setLastPlacedPosition(position);
                    }
                });

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
                        EntityTracker1_9 tracker = wrapper.user().get(EntityTracker1_9.class);
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
                            PacketWrapper updateBlockEntity = new PacketWrapper(0x09, null, wrapper.user());

                            updateBlockEntity.write(Type.POSITION, pos);
                            updateBlockEntity.write(Type.UNSIGNED_BYTE, (short) 2);
                            updateBlockEntity.write(Type.NBT, tag.get());

                            updateBlockEntity.send(Protocol1_9To1_8.class);
                        }
                    }
                });

            }
        });
    }
}

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
package com.viaversion.viaversion.protocols.protocol1_12to1_11_1;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.IntTag;
import com.github.steveice10.opennbt.tag.builtin.StringTag;
import com.google.gson.JsonElement;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.chunks.Chunk;
import com.viaversion.viaversion.api.minecraft.chunks.ChunkSection;
import com.viaversion.viaversion.api.minecraft.entities.Entity1_12Types;
import com.viaversion.viaversion.api.platform.providers.ViaProviders;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.protocol.remapper.PacketRemapper;
import com.viaversion.viaversion.api.rewriter.ItemRewriter;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.version.Types1_12;
import com.viaversion.viaversion.data.entity.EntityTrackerBase;
import com.viaversion.viaversion.protocols.protocol1_12to1_11_1.metadata.MetadataRewriter1_12To1_11_1;
import com.viaversion.viaversion.protocols.protocol1_12to1_11_1.packets.InventoryPackets;
import com.viaversion.viaversion.protocols.protocol1_12to1_11_1.providers.InventoryQuickMoveProvider;
import com.viaversion.viaversion.protocols.protocol1_9_1_2to1_9_3_4.types.Chunk1_9_3_4Type;
import com.viaversion.viaversion.protocols.protocol1_9_3to1_9_1_2.ClientboundPackets1_9_3;
import com.viaversion.viaversion.protocols.protocol1_9_3to1_9_1_2.ServerboundPackets1_9_3;
import com.viaversion.viaversion.protocols.protocol1_9_3to1_9_1_2.storage.ClientWorld;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.Protocol1_9To1_8;
import com.viaversion.viaversion.rewriter.EntityRewriter;
import com.viaversion.viaversion.rewriter.SoundRewriter;

public class Protocol1_12To1_11_1 extends AbstractProtocol<ClientboundPackets1_9_3, ClientboundPackets1_12, ServerboundPackets1_9_3, ServerboundPackets1_12> {

    private final EntityRewriter metadataRewriter = new MetadataRewriter1_12To1_11_1(this);
    private final ItemRewriter itemRewriter = new InventoryPackets(this);

    public Protocol1_12To1_11_1() {
        super(ClientboundPackets1_9_3.class, ClientboundPackets1_12.class, ServerboundPackets1_9_3.class, ServerboundPackets1_12.class);
    }

    @Override
    protected void registerPackets() {
        metadataRewriter.register();
        itemRewriter.register();

        registerClientbound(ClientboundPackets1_9_3.SPAWN_ENTITY, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // 0 - Entity id
                map(Type.UUID); // 1 - UUID
                map(Type.BYTE); // 2 - Type

                // Track Entity
                handler(metadataRewriter.objectTrackerHandler());
            }
        });

        registerClientbound(ClientboundPackets1_9_3.SPAWN_MOB, new PacketRemapper() {
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
                handler(metadataRewriter.trackerAndRewriterHandler(Types1_12.METADATA_LIST));
            }
        });

        registerClientbound(ClientboundPackets1_9_3.CHAT_MESSAGE, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        if (!Via.getConfig().is1_12NBTArrayFix()) return;
                        try {
                            JsonElement obj = Protocol1_9To1_8.FIX_JSON.transform(null, wrapper.passthrough(Type.COMPONENT).toString());
                            TranslateRewriter.toClient(obj, wrapper.user());
                            ChatItemRewriter.toClient(obj, wrapper.user());
                            wrapper.set(Type.COMPONENT, 0, obj);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });

        registerClientbound(ClientboundPackets1_9_3.CHUNK_DATA, new PacketRemapper() {
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
                                        int block = section.getBlockWithoutData(x, y, z);
                                        // Is this a bed?
                                        if (block == 26) {
                                            //  NBT -> { color:14, x:132, y:64, z:222, id:"minecraft:bed" } (Debug output)
                                            CompoundTag tag = new CompoundTag();
                                            tag.put("color", new IntTag(14)); // Set color to red (Default in previous versions)
                                            tag.put("x", new IntTag(x + (chunk.getX() << 4)));
                                            tag.put("y", new IntTag(y + (i << 4)));
                                            tag.put("z", new IntTag(z + (chunk.getZ() << 4)));
                                            tag.put("id", new StringTag("minecraft:bed"));

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

        metadataRewriter.registerRemoveEntities(ClientboundPackets1_9_3.DESTROY_ENTITIES);
        metadataRewriter.registerMetadataRewriter(ClientboundPackets1_9_3.ENTITY_METADATA, Types1_12.METADATA_LIST);

        registerClientbound(ClientboundPackets1_9_3.JOIN_GAME, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.INT);
                map(Type.UNSIGNED_BYTE);
                map(Type.INT);
                handler(wrapper -> {
                    ClientWorld clientChunks = wrapper.user().get(ClientWorld.class);
                    int dimensionId = wrapper.get(Type.INT, 1);
                    clientChunks.setEnvironment(dimensionId);
                });
            }
        });
        registerClientbound(ClientboundPackets1_9_3.RESPAWN, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.INT);
                handler(wrapper -> {
                    ClientWorld clientWorld = wrapper.user().get(ClientWorld.class);
                    int dimensionId = wrapper.get(Type.INT, 0);
                    clientWorld.setEnvironment(dimensionId);
                });
            }
        });

        new SoundRewriter(this, this::getNewSoundId).registerSound(ClientboundPackets1_9_3.SOUND);


        // New packet at 0x01
        cancelServerbound(ServerboundPackets1_12.PREPARE_CRAFTING_GRID);

        // Client Settings (max length changed)
        registerServerbound(ServerboundPackets1_12.CLIENT_SETTINGS, new PacketRemapper() {
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

        // New packet at 0x17
        cancelServerbound(ServerboundPackets1_12.RECIPE_BOOK_DATA);

        // New packet 0x19
        cancelServerbound(ServerboundPackets1_12.ADVANCEMENT_TAB);
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
    public void register(ViaProviders providers) {
        providers.register(InventoryQuickMoveProvider.class, new InventoryQuickMoveProvider());
    }

    @Override
    public void init(UserConnection userConnection) {
        userConnection.addEntityTracker(this.getClass(), new EntityTrackerBase(userConnection, Entity1_12Types.EntityType.PLAYER));
        if (!userConnection.has(ClientWorld.class)) {
            userConnection.put(new ClientWorld(userConnection));
        }
    }

    @Override
    public EntityRewriter getEntityRewriter() {
        return metadataRewriter;
    }

    @Override
    public ItemRewriter getItemRewriter() {
        return itemRewriter;
    }
}

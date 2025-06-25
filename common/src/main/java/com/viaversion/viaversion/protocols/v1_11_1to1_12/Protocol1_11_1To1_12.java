/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2025 ViaVersion and contributors
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
package com.viaversion.viaversion.protocols.v1_11_1to1_12;

import com.google.gson.JsonElement;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.IntTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.ClientWorld;
import com.viaversion.viaversion.api.minecraft.chunks.Chunk;
import com.viaversion.viaversion.api.minecraft.chunks.ChunkSection;
import com.viaversion.viaversion.api.minecraft.chunks.DataPalette;
import com.viaversion.viaversion.api.minecraft.chunks.PaletteType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_12;
import com.viaversion.viaversion.api.platform.providers.ViaProviders;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.chunk.ChunkType1_9_3;
import com.viaversion.viaversion.data.entity.EntityTrackerBase;
import com.viaversion.viaversion.protocols.v1_11_1to1_12.data.ChatItemRewriter;
import com.viaversion.viaversion.protocols.v1_11_1to1_12.data.TranslateRewriter;
import com.viaversion.viaversion.protocols.v1_11_1to1_12.packet.ClientboundPackets1_12;
import com.viaversion.viaversion.protocols.v1_11_1to1_12.packet.ServerboundPackets1_12;
import com.viaversion.viaversion.protocols.v1_11_1to1_12.provider.InventoryQuickMoveProvider;
import com.viaversion.viaversion.protocols.v1_11_1to1_12.rewriter.EntityPacketRewriter1_12;
import com.viaversion.viaversion.protocols.v1_11_1to1_12.rewriter.ItemPacketRewriter1_12;
import com.viaversion.viaversion.protocols.v1_9_1to1_9_3.packet.ClientboundPackets1_9_3;
import com.viaversion.viaversion.protocols.v1_9_1to1_9_3.packet.ServerboundPackets1_9_3;
import com.viaversion.viaversion.rewriter.SoundRewriter;

public class Protocol1_11_1To1_12 extends AbstractProtocol<ClientboundPackets1_9_3, ClientboundPackets1_12, ServerboundPackets1_9_3, ServerboundPackets1_12> {

    private final EntityPacketRewriter1_12 entityRewriter = new EntityPacketRewriter1_12(this);
    private final ItemPacketRewriter1_12 itemRewriter = new ItemPacketRewriter1_12(this);

    public Protocol1_11_1To1_12() {
        super(ClientboundPackets1_9_3.class, ClientboundPackets1_12.class, ServerboundPackets1_9_3.class, ServerboundPackets1_12.class);
    }

    @Override
    protected void registerPackets() {
        super.registerPackets();

        registerClientbound(ClientboundPackets1_9_3.CHAT, wrapper -> {
            final JsonElement element = wrapper.passthrough(Types.COMPONENT);
            TranslateRewriter.toClient(wrapper.user(), element);
            ChatItemRewriter.toClient(element);

            wrapper.set(Types.COMPONENT, 0, element);
        });

        registerClientbound(ClientboundPackets1_9_3.LEVEL_CHUNK, wrapper -> {
            ClientWorld clientWorld = wrapper.user().getClientWorld(Protocol1_11_1To1_12.class);

            ChunkType1_9_3 type = ChunkType1_9_3.forEnvironment(clientWorld.getEnvironment());
            Chunk chunk = wrapper.passthrough(type);

            for (int s = 0; s < chunk.getSections().length; s++) {
                ChunkSection section = chunk.getSections()[s];
                if (section == null) continue;
                DataPalette blocks = section.palette(PaletteType.BLOCKS);

                for (int idx = 0; idx < ChunkSection.SIZE; idx++) {
                    int id = blocks.idAt(idx) >> 4;
                    // Is this a bed?
                    if (id != 26) continue;

                    //  NBT -> { color:14, x:132, y:64, z:222, id:"minecraft:bed" } (Debug output)
                    CompoundTag tag = new CompoundTag();
                    tag.put("color", new IntTag(14)); // Set color to red (Default in previous versions)
                    tag.put("x", new IntTag(ChunkSection.xFromIndex(idx) + (chunk.getX() << 4)));
                    tag.put("y", new IntTag(ChunkSection.yFromIndex(idx) + (s << 4)));
                    tag.put("z", new IntTag(ChunkSection.zFromIndex(idx) + (chunk.getZ() << 4)));
                    tag.put("id", new StringTag("minecraft:bed"));

                    // Add a fake block entity
                    chunk.getBlockEntities().add(tag);
                }
            }
        });

        new SoundRewriter<>(this, this::getNewSoundId).registerSound(ClientboundPackets1_9_3.SOUND);


        // New packet at 0x01
        cancelServerbound(ServerboundPackets1_12.CRAFTING_RECIPE_PLACEMENT);

        // Client Settings (max length changed)
        registerServerbound(ServerboundPackets1_12.CLIENT_INFORMATION, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.STRING); // 0 - Locale
                map(Types.BYTE); // 1 - view distance
                map(Types.VAR_INT); // 2 - chat mode
                map(Types.BOOLEAN); // 3 - chat colors
                map(Types.UNSIGNED_BYTE); // 4 - chat flags
                map(Types.VAR_INT); // 5 - main hand
                handler(wrapper -> {
                    // As part of the fix for MC-111054, the max length of
                    // the locale was raised to 16 (from 7), and the client
                    // now makes sure that resource packs have names in that
                    // length.  However, for older servers, it is still 7,
                    // and thus the server will reject it (and the client
                    // won't know that the pack's invalid).
                    // The fix is to just silently lower the length.  The
                    // server doesn't actually use the locale anywhere, so
                    // this is fine.
                    String locale = wrapper.get(Types.STRING, 0);
                    if (locale.length() > 7) {
                        wrapper.set(Types.STRING, 0, locale.substring(0, 7));
                    }
                });
            }
        });

        // New packet at 0x17
        cancelServerbound(ServerboundPackets1_12.RECIPE_BOOK_UPDATE);

        // New packet 0x19
        cancelServerbound(ServerboundPackets1_12.SEEN_ADVANCEMENTS);
    }

    private int getNewSoundId(int id) {
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
        userConnection.addEntityTracker(this.getClass(), new EntityTrackerBase(userConnection, EntityTypes1_12.EntityType.PLAYER));
        userConnection.addClientWorld(this.getClass(), new ClientWorld());
    }

    @Override
    public EntityPacketRewriter1_12 getEntityRewriter() {
        return entityRewriter;
    }

    @Override
    public ItemPacketRewriter1_12 getItemRewriter() {
        return itemRewriter;
    }
}

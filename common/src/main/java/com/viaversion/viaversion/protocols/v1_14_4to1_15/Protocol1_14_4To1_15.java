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
package com.viaversion.viaversion.protocols.v1_14_4to1_15;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.MappingData;
import com.viaversion.viaversion.api.data.MappingDataBase;
import com.viaversion.viaversion.api.minecraft.ClientWorld;
import com.viaversion.viaversion.api.minecraft.RegistryType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_15;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.data.entity.EntityTrackerBase;
import com.viaversion.viaversion.protocols.v1_13_2to1_14.packet.ServerboundPackets1_14;
import com.viaversion.viaversion.protocols.v1_14_3to1_14_4.packet.ClientboundPackets1_14_4;
import com.viaversion.viaversion.protocols.v1_14_4to1_15.packet.ClientboundPackets1_15;
import com.viaversion.viaversion.protocols.v1_14_4to1_15.rewriter.EntityPacketRewriter1_15;
import com.viaversion.viaversion.protocols.v1_14_4to1_15.rewriter.ItemPacketRewriter1_15;
import com.viaversion.viaversion.protocols.v1_14_4to1_15.rewriter.WorldPacketRewriter1_15;
import com.viaversion.viaversion.rewriter.ParticleRewriter;
import com.viaversion.viaversion.rewriter.SoundRewriter;
import com.viaversion.viaversion.rewriter.StatisticsRewriter;
import com.viaversion.viaversion.rewriter.TagRewriter;

public class Protocol1_14_4To1_15 extends AbstractProtocol<ClientboundPackets1_14_4, ClientboundPackets1_15, ServerboundPackets1_14, ServerboundPackets1_14> {

    public static final MappingData MAPPINGS = new MappingDataBase("1.14", "1.15");
    private final EntityPacketRewriter1_15 entityRewriter = new EntityPacketRewriter1_15(this);
    private final ItemPacketRewriter1_15 itemRewriter = new ItemPacketRewriter1_15(this);
    private final ParticleRewriter<ClientboundPackets1_14_4> particleRewriter = new ParticleRewriter<>(this);
    private final TagRewriter<ClientboundPackets1_14_4> tagRewriter = new TagRewriter<>(this);

    public Protocol1_14_4To1_15() {
        super(ClientboundPackets1_14_4.class, ClientboundPackets1_15.class, ServerboundPackets1_14.class, ServerboundPackets1_14.class);
    }

    @Override
    protected void registerPackets() {
        super.registerPackets();

        WorldPacketRewriter1_15.register(this);

        SoundRewriter<ClientboundPackets1_14_4> soundRewriter = new SoundRewriter<>(this);
        soundRewriter.registerSound(ClientboundPackets1_14_4.SOUND_ENTITY); // Entity Sound Effect (added somewhere in 1.14)
        soundRewriter.registerSound(ClientboundPackets1_14_4.SOUND);

        new StatisticsRewriter<>(this).register(ClientboundPackets1_14_4.AWARD_STATS);

        registerServerbound(ServerboundPackets1_14.EDIT_BOOK, wrapper -> itemRewriter.handleItemToServer(wrapper.user(), wrapper.passthrough(Types.ITEM1_13_2)));

        tagRewriter.register(ClientboundPackets1_14_4.UPDATE_TAGS, RegistryType.ENTITY);
    }

    @Override
    protected void onMappingDataLoaded() {
        EntityTypes1_15.initialize(this);

        tagRewriter.removeTag(RegistryType.BLOCK, "minecraft:dirt_like");
        tagRewriter.addEmptyTag(RegistryType.ITEM, "minecraft:lectern_books");
        tagRewriter.addEmptyTags(RegistryType.BLOCK, "minecraft:bee_growables", "minecraft:beehives");
        tagRewriter.addEmptyTag(RegistryType.ENTITY, "minecraft:beehive_inhabitors");

        super.onMappingDataLoaded();
    }

    @Override
    public void init(UserConnection connection) {
        addEntityTracker(connection, new EntityTrackerBase(connection, EntityTypes1_15.PLAYER));
        connection.addClientWorld(this.getClass(), new ClientWorld());
    }

    @Override
    public MappingData getMappingData() {
        return MAPPINGS;
    }

    @Override
    public EntityPacketRewriter1_15 getEntityRewriter() {
        return entityRewriter;
    }

    @Override
    public ItemPacketRewriter1_15 getItemRewriter() {
        return itemRewriter;
    }

    @Override
    public ParticleRewriter<ClientboundPackets1_14_4> getParticleRewriter() {
        return particleRewriter;
    }

    @Override
    public TagRewriter<ClientboundPackets1_14_4> getTagRewriter() {
        return tagRewriter;
    }
}

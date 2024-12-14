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
package com.viaversion.viaversion.protocols.v1_16_1to1_16_2;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.RegistryType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_16_2;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.data.entity.EntityTrackerBase;
import com.viaversion.viaversion.protocols.v1_15_2to1_16.packet.ClientboundPackets1_16;
import com.viaversion.viaversion.protocols.v1_15_2to1_16.packet.ServerboundPackets1_16;
import com.viaversion.viaversion.protocols.v1_16_1to1_16_2.data.MappingData1_16_2;
import com.viaversion.viaversion.protocols.v1_16_1to1_16_2.packet.ClientboundPackets1_16_2;
import com.viaversion.viaversion.protocols.v1_16_1to1_16_2.packet.ServerboundPackets1_16_2;
import com.viaversion.viaversion.protocols.v1_16_1to1_16_2.rewriter.EntityPacketRewriter1_16_2;
import com.viaversion.viaversion.protocols.v1_16_1to1_16_2.rewriter.ItemPacketRewriter1_16_2;
import com.viaversion.viaversion.protocols.v1_16_1to1_16_2.rewriter.WorldPacketRewriter1_16_2;
import com.viaversion.viaversion.rewriter.ParticleRewriter;
import com.viaversion.viaversion.rewriter.SoundRewriter;
import com.viaversion.viaversion.rewriter.StatisticsRewriter;
import com.viaversion.viaversion.rewriter.TagRewriter;

public class Protocol1_16_1To1_16_2 extends AbstractProtocol<ClientboundPackets1_16, ClientboundPackets1_16_2, ServerboundPackets1_16, ServerboundPackets1_16_2> {

    public static final MappingData1_16_2 MAPPINGS = new MappingData1_16_2();
    private final EntityPacketRewriter1_16_2 entityRewriter = new EntityPacketRewriter1_16_2(this);
    private final ItemPacketRewriter1_16_2 itemRewriter = new ItemPacketRewriter1_16_2(this);
    private final ParticleRewriter<ClientboundPackets1_16> particleRewriter = new ParticleRewriter<>(this);
    private final TagRewriter<ClientboundPackets1_16> tagRewriter = new TagRewriter<>(this);

    public Protocol1_16_1To1_16_2() {
        super(ClientboundPackets1_16.class, ClientboundPackets1_16_2.class, ServerboundPackets1_16.class, ServerboundPackets1_16_2.class);
    }

    @Override
    protected void registerPackets() {
        super.registerPackets();

        WorldPacketRewriter1_16_2.register(this);

        tagRewriter.register(ClientboundPackets1_16.UPDATE_TAGS, RegistryType.ENTITY);
        particleRewriter.registerLevelParticles1_13(ClientboundPackets1_16.LEVEL_PARTICLES, Types.DOUBLE);

        new StatisticsRewriter<>(this).register(ClientboundPackets1_16.AWARD_STATS);

        SoundRewriter<ClientboundPackets1_16> soundRewriter = new SoundRewriter<>(this);
        soundRewriter.registerSound(ClientboundPackets1_16.SOUND);
        soundRewriter.registerSound(ClientboundPackets1_16.SOUND_ENTITY);

        // Recipe book data has been split into 2 separate packets
        registerServerbound(ServerboundPackets1_16_2.RECIPE_BOOK_CHANGE_SETTINGS, ServerboundPackets1_16.RECIPE_BOOK_UPDATE, wrapper -> {
            int recipeType = wrapper.read(Types.VAR_INT);
            boolean open = wrapper.read(Types.BOOLEAN);
            boolean filter = wrapper.read(Types.BOOLEAN);
            wrapper.write(Types.VAR_INT, 1); // Settings
            wrapper.write(Types.BOOLEAN, recipeType == 0 && open); // Crafting
            wrapper.write(Types.BOOLEAN, filter);
            wrapper.write(Types.BOOLEAN, recipeType == 1 && open); // Furnace
            wrapper.write(Types.BOOLEAN, filter);
            wrapper.write(Types.BOOLEAN, recipeType == 2 && open); // Blast Furnace
            wrapper.write(Types.BOOLEAN, filter);
            wrapper.write(Types.BOOLEAN, recipeType == 3 && open); // Smoker
            wrapper.write(Types.BOOLEAN, filter);
        });
        registerServerbound(ServerboundPackets1_16_2.RECIPE_BOOK_SEEN_RECIPE, ServerboundPackets1_16.RECIPE_BOOK_UPDATE, wrapper -> {
            String recipe = wrapper.read(Types.STRING);
            wrapper.write(Types.VAR_INT, 0); // Shown
            wrapper.write(Types.STRING, recipe);
        });
    }

    @Override
    protected void onMappingDataLoaded() {
        EntityTypes1_16_2.initialize(this);

        tagRewriter.removeTag(RegistryType.ITEM, "minecraft:furnace_materials");

        super.onMappingDataLoaded();
    }

    @Override
    public void init(UserConnection userConnection) {
        userConnection.addEntityTracker(this.getClass(), new EntityTrackerBase(userConnection, EntityTypes1_16_2.PLAYER));
    }

    @Override
    public MappingData1_16_2 getMappingData() {
        return MAPPINGS;
    }

    @Override
    public EntityPacketRewriter1_16_2 getEntityRewriter() {
        return entityRewriter;
    }

    @Override
    public ItemPacketRewriter1_16_2 getItemRewriter() {
        return itemRewriter;
    }

    @Override
    public ParticleRewriter<ClientboundPackets1_16> getParticleRewriter() {
        return particleRewriter;
    }

    @Override
    public TagRewriter<ClientboundPackets1_16> getTagRewriter() {
        return tagRewriter;
    }
}

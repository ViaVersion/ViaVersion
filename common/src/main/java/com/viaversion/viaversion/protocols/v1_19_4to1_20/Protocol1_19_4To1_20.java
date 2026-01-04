/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2026 ViaVersion and contributors
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
package com.viaversion.viaversion.protocols.v1_19_4to1_20;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.MappingData;
import com.viaversion.viaversion.api.data.MappingDataBase;
import com.viaversion.viaversion.api.minecraft.RegistryType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_19_4;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.misc.ParticleType;
import com.viaversion.viaversion.api.type.types.version.Types1_20;
import com.viaversion.viaversion.data.entity.EntityTrackerBase;
import com.viaversion.viaversion.protocols.v1_19_3to1_19_4.packet.ClientboundPackets1_19_4;
import com.viaversion.viaversion.protocols.v1_19_3to1_19_4.packet.ServerboundPackets1_19_4;
import com.viaversion.viaversion.protocols.v1_19_4to1_20.rewriter.EntityPacketRewriter1_20;
import com.viaversion.viaversion.protocols.v1_19_4to1_20.rewriter.ItemPacketRewriter1_20;
import com.viaversion.viaversion.rewriter.ParticleRewriter;
import com.viaversion.viaversion.rewriter.SoundRewriter;
import com.viaversion.viaversion.rewriter.StatisticsRewriter;
import com.viaversion.viaversion.rewriter.TagRewriter;

public final class Protocol1_19_4To1_20 extends AbstractProtocol<ClientboundPackets1_19_4, ClientboundPackets1_19_4, ServerboundPackets1_19_4, ServerboundPackets1_19_4> {

    public static final MappingData MAPPINGS = new MappingDataBase("1.19.4", "1.20");
    private final EntityPacketRewriter1_20 entityRewriter = new EntityPacketRewriter1_20(this);
    private final ItemPacketRewriter1_20 itemRewriter = new ItemPacketRewriter1_20(this);
    private final ParticleRewriter<ClientboundPackets1_19_4> particleRewriter = new ParticleRewriter<>(this);
    private final TagRewriter<ClientboundPackets1_19_4> tagRewriter = new TagRewriter<>(this);

    public Protocol1_19_4To1_20() {
        super(ClientboundPackets1_19_4.class, ClientboundPackets1_19_4.class, ServerboundPackets1_19_4.class, ServerboundPackets1_19_4.class);
    }

    @Override
    protected void registerPackets() {
        super.registerPackets();

        tagRewriter.registerGeneric(ClientboundPackets1_19_4.UPDATE_TAGS);
        particleRewriter.registerLevelParticles1_19(ClientboundPackets1_19_4.LEVEL_PARTICLES);

        final SoundRewriter<ClientboundPackets1_19_4> soundRewriter = new SoundRewriter<>(this);
        soundRewriter.registerSound1_19_3(ClientboundPackets1_19_4.SOUND);
        soundRewriter.registerSound1_19_3(ClientboundPackets1_19_4.SOUND_ENTITY);

        new StatisticsRewriter<>(this).register(ClientboundPackets1_19_4.AWARD_STATS);

        registerClientbound(ClientboundPackets1_19_4.PLAYER_COMBAT_END, wrapper -> {
            wrapper.passthrough(Types.VAR_INT); // Duration
            wrapper.read(Types.INT); // Killer ID
        });
        registerClientbound(ClientboundPackets1_19_4.PLAYER_COMBAT_KILL, wrapper -> {
            wrapper.passthrough(Types.VAR_INT); // Player ID
            wrapper.read(Types.INT); // Killer ID
        });
    }

    @Override
    protected void onMappingDataLoaded() {
        Types1_20.PARTICLE.filler(this)
            .reader("block", ParticleType.Readers.BLOCK)
            .reader("block_marker", ParticleType.Readers.BLOCK)
            .reader("dust", ParticleType.Readers.DUST)
            .reader("falling_dust", ParticleType.Readers.BLOCK)
            .reader("dust_color_transition", ParticleType.Readers.DUST_TRANSITION)
            .reader("item", ParticleType.Readers.ITEM1_13_2)
            .reader("vibration", ParticleType.Readers.VIBRATION1_19)
            .reader("sculk_charge", ParticleType.Readers.SCULK_CHARGE)
            .reader("shriek", ParticleType.Readers.SHRIEK);

        tagRewriter.removeTag(RegistryType.BLOCK, "minecraft:replaceable_plants");
        tagRewriter.addEmptyTags(RegistryType.ITEM, "minecraft:decorated_pot_ingredients", "minecraft:decorated_pot_sherds");
        tagRewriter.addEmptyTag(RegistryType.BLOCK, "minecraft:trail_ruins_replaceable");

        super.onMappingDataLoaded();
    }

    @Override
    public void init(final UserConnection user) {
        addEntityTracker(user, new EntityTrackerBase(user, EntityTypes1_19_4.PLAYER));
    }

    @Override
    public MappingData getMappingData() {
        return MAPPINGS;
    }

    @Override
    public EntityPacketRewriter1_20 getEntityRewriter() {
        return entityRewriter;
    }

    @Override
    public ItemPacketRewriter1_20 getItemRewriter() {
        return itemRewriter;
    }

    @Override
    public ParticleRewriter<ClientboundPackets1_19_4> getParticleRewriter() {
        return particleRewriter;
    }

    @Override
    public TagRewriter<ClientboundPackets1_19_4> getTagRewriter() {
        return tagRewriter;
    }
}

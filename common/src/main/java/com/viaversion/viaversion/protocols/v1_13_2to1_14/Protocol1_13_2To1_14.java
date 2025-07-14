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
package com.viaversion.viaversion.protocols.v1_13_2to1_14;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.ClientWorld;
import com.viaversion.viaversion.api.minecraft.RegistryType;
import com.viaversion.viaversion.api.minecraft.entities.EntityTypes1_14;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.misc.ParticleType;
import com.viaversion.viaversion.api.type.types.version.Types1_13_2;
import com.viaversion.viaversion.api.type.types.version.Types1_14;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.packet.ClientboundPackets1_13;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.packet.ServerboundPackets1_13;
import com.viaversion.viaversion.protocols.v1_13_2to1_14.data.MappingData1_14;
import com.viaversion.viaversion.protocols.v1_13_2to1_14.packet.ClientboundPackets1_14;
import com.viaversion.viaversion.protocols.v1_13_2to1_14.packet.ServerboundPackets1_14;
import com.viaversion.viaversion.protocols.v1_13_2to1_14.rewriter.ComponentRewriter1_14;
import com.viaversion.viaversion.protocols.v1_13_2to1_14.rewriter.EntityPacketRewriter1_14;
import com.viaversion.viaversion.protocols.v1_13_2to1_14.rewriter.ItemPacketRewriter1_14;
import com.viaversion.viaversion.protocols.v1_13_2to1_14.rewriter.PlayerPacketRewriter1_14;
import com.viaversion.viaversion.protocols.v1_13_2to1_14.rewriter.WorldPacketRewriter1_14;
import com.viaversion.viaversion.protocols.v1_13_2to1_14.storage.EntityTracker1_14;
import com.viaversion.viaversion.rewriter.CommandRewriter;
import com.viaversion.viaversion.rewriter.ParticleRewriter;
import com.viaversion.viaversion.rewriter.SoundRewriter;
import com.viaversion.viaversion.rewriter.StatisticsRewriter;
import com.viaversion.viaversion.rewriter.TagRewriter;
import com.viaversion.viaversion.rewriter.text.JsonNBTComponentRewriter;
import org.checkerframework.checker.nullness.qual.Nullable;

public class Protocol1_13_2To1_14 extends AbstractProtocol<ClientboundPackets1_13, ClientboundPackets1_14, ServerboundPackets1_13, ServerboundPackets1_14> {

    public static final MappingData1_14 MAPPINGS = new MappingData1_14();
    private final EntityPacketRewriter1_14 entityRewriter = new EntityPacketRewriter1_14(this);
    private final ItemPacketRewriter1_14 itemRewriter = new ItemPacketRewriter1_14(this);
    private final ParticleRewriter<ClientboundPackets1_13> particleRewriter = new ParticleRewriter<>(this);
    private final TagRewriter<ClientboundPackets1_13> tagRewriter = new TagRewriter<>(this);

    public Protocol1_13_2To1_14() {
        super(ClientboundPackets1_13.class, ClientboundPackets1_14.class, ServerboundPackets1_13.class, ServerboundPackets1_14.class);
    }

    @Override
    protected void registerPackets() {
        super.registerPackets();

        WorldPacketRewriter1_14.register(this);
        PlayerPacketRewriter1_14.register(this);

        new SoundRewriter<>(this).registerSound(ClientboundPackets1_13.SOUND);
        new StatisticsRewriter<>(this).register(ClientboundPackets1_13.AWARD_STATS);

        particleRewriter.registerLevelParticles1_13(ClientboundPackets1_13.LEVEL_PARTICLES, Types.FLOAT);

        JsonNBTComponentRewriter<ClientboundPackets1_13> componentRewriter = new ComponentRewriter1_14<>(this);
        componentRewriter.registerComponentPacket(ClientboundPackets1_13.CHAT);

        CommandRewriter<ClientboundPackets1_13> commandRewriter = new CommandRewriter<>(this) {
            @Override
            public @Nullable String handleArgumentType(String argumentType) {
                if (argumentType.equals("minecraft:nbt")) {
                    return "minecraft:nbt_compound_tag";
                }
                return super.handleArgumentType(argumentType);
            }
        };
        commandRewriter.registerDeclareCommands(ClientboundPackets1_13.COMMANDS);

        registerClientbound(ClientboundPackets1_13.UPDATE_TAGS, new PacketHandlers() {
            @Override
            protected void register() {
                handler(tagRewriter.getHandler(RegistryType.FLUID));
                handler(wrapper -> tagRewriter.appendNewTags(wrapper, RegistryType.ENTITY));
            }
        });

        // Set Difficulty packet added in 19w11a
        cancelServerbound(ServerboundPackets1_14.CHANGE_DIFFICULTY);
        // Lock Difficulty packet added in 19w11a
        cancelServerbound(ServerboundPackets1_14.LOCK_DIFFICULTY);
        // Unknown packet added in 19w13a
        cancelServerbound(ServerboundPackets1_14.SET_JIGSAW_BLOCK);
    }

    @Override
    protected void onMappingDataLoaded() {
        WorldPacketRewriter1_14.air = MAPPINGS.getBlockStateMappings().getNewId(0);
        WorldPacketRewriter1_14.voidAir = MAPPINGS.getBlockStateMappings().getNewId(8591);
        WorldPacketRewriter1_14.caveAir = MAPPINGS.getBlockStateMappings().getNewId(8592);

        EntityTypes1_14.initialize(this);
        Types1_13_2.PARTICLE.filler(this, false)
            .reader("block", ParticleType.Readers.BLOCK)
            .reader("dust", ParticleType.Readers.DUST)
            .reader("falling_dust", ParticleType.Readers.BLOCK)
            .reader("item", ParticleType.Readers.ITEM1_13_2);
        Types1_14.PARTICLE.filler(this)
            .reader("block", ParticleType.Readers.BLOCK)
            .reader("dust", ParticleType.Readers.DUST)
            .reader("falling_dust", ParticleType.Readers.BLOCK)
            .reader("item", ParticleType.Readers.ITEM1_13_2);

        tagRewriter.addEmptyTag(RegistryType.BLOCK, "bamboo_plantable_on");

        super.onMappingDataLoaded();
    }

    @Override
    public void init(UserConnection userConnection) {
        userConnection.addEntityTracker(this.getClass(), new EntityTracker1_14(userConnection));
        userConnection.addClientWorld(this.getClass(), new ClientWorld());
    }

    @Override
    public MappingData1_14 getMappingData() {
        return MAPPINGS;
    }

    @Override
    public EntityPacketRewriter1_14 getEntityRewriter() {
        return entityRewriter;
    }

    @Override
    public ItemPacketRewriter1_14 getItemRewriter() {
        return itemRewriter;
    }

    @Override
    public ParticleRewriter<ClientboundPackets1_13> getParticleRewriter() {
        return particleRewriter;
    }

    @Override
    public TagRewriter<ClientboundPackets1_13> getTagRewriter() {
        return tagRewriter;
    }
}

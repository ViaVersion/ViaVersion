/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2023 ViaVersion and contributors
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
package com.viaversion.viaversion.protocols.protocol1_20_2to1_20;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.entities.Entity1_19_4Types;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.rewriter.EntityRewriter;
import com.viaversion.viaversion.api.rewriter.ItemRewriter;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.data.entity.EntityTrackerBase;
import com.viaversion.viaversion.protocols.base.ClientboundLoginPackets;
import com.viaversion.viaversion.protocols.protocol1_19_4to1_19_3.ClientboundPackets1_19_4;
import com.viaversion.viaversion.protocols.protocol1_19_4to1_19_3.ServerboundPackets1_19_4;
import com.viaversion.viaversion.protocols.protocol1_20_2to1_20.handler.BlockItemPacketHandler1_20_2;
import com.viaversion.viaversion.protocols.protocol1_20_2to1_20.handler.EntityPacketHandler1_20_2;
import com.viaversion.viaversion.protocols.protocol1_20_2to1_20.packet.ClientboundPackets1_20_2;
import com.viaversion.viaversion.protocols.protocol1_20_2to1_20.packet.ServerboundPackets1_20_2;
import com.viaversion.viaversion.rewriter.SoundRewriter;
import com.viaversion.viaversion.rewriter.StatisticsRewriter;
import com.viaversion.viaversion.rewriter.TagRewriter;

import java.util.UUID;

public final class Protocol1_20_2To1_20 extends AbstractProtocol<ClientboundPackets1_19_4, ClientboundPackets1_20_2, ServerboundPackets1_19_4, ServerboundPackets1_20_2> {

    private final EntityPacketHandler1_20_2 entityPacketHandler = new EntityPacketHandler1_20_2(this);
    private final BlockItemPacketHandler1_20_2 itemPacketHandler = new BlockItemPacketHandler1_20_2(this);

    public Protocol1_20_2To1_20() {
        // Passing the class types into the super constructor is needed for automatic packet type id remapping, but can otherwise be omitted
        super(ClientboundPackets1_19_4.class, ClientboundPackets1_20_2.class, ServerboundPackets1_19_4.class, ServerboundPackets1_20_2.class);
    }

    @Override
    protected void registerPackets() {
        // TODO Stopped at ForgetLevelChunk
        // TODO New login packets (custom query answer, login ack)
        // TODO Handle move from and to configuration state
        //      Game profile/serverbound ack -> configuration
        //      Client/serverbound config finish -> play
        // TODO New helper methods for: join game, respawn,
        // TODO Make sure Paper/Velocity handle a 0,0 uuid fine during login

        // Lesser:
        // TODO Player info, replace profile with missing name with null?
        // TODO Scoreboard objective probably okay, but there are refactors to the id
        super.registerPackets();

        final TagRewriter<ClientboundPackets1_19_4> tagRewriter = new TagRewriter<>(this);
        tagRewriter.registerGeneric(ClientboundPackets1_19_4.TAGS);

        final SoundRewriter<ClientboundPackets1_19_4> soundRewriter = new SoundRewriter<>(this);
        soundRewriter.register1_19_3Sound(ClientboundPackets1_19_4.SOUND);
        soundRewriter.registerSound(ClientboundPackets1_19_4.ENTITY_SOUND);

        new StatisticsRewriter<>(this).register(ClientboundPackets1_19_4.STATISTICS);

        registerClientbound(ClientboundPackets1_19_4.SCOREBOARD_OBJECTIVE, wrapper -> {
            final byte slot = wrapper.read(Type.BYTE);
            wrapper.write(Type.VAR_INT, (int) slot);
        });

        registerClientbound(State.LOGIN, ClientboundLoginPackets.HELLO.getId(), ClientboundLoginPackets.HELLO.getId(), wrapper -> {
            wrapper.passthrough(Type.STRING); // Name
            final UUID uuid = wrapper.read(Type.OPTIONAL_UUID);
            wrapper.write(Type.UUID, uuid != null ? uuid : new UUID(0, 0));
        });
    }

    @Override
    public void init(final UserConnection user) {
        addEntityTracker(user, new EntityTrackerBase(user, Entity1_19_4Types.PLAYER));
    }

    @Override
    public EntityRewriter<Protocol1_20_2To1_20> getEntityRewriter() {
        return entityPacketHandler;
    }

    @Override
    public ItemRewriter<Protocol1_20_2To1_20> getItemRewriter() {
        return itemPacketHandler;
    }
}
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
package com.viaversion.viaversion.rewriter;

import com.viaversion.viaversion.api.minecraft.Holder;
import com.viaversion.viaversion.api.minecraft.SoundEvent;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.type.Type;

public class SoundRewriter<C extends ClientboundPacketType> {
    protected final Protocol<C, ?, ?, ?> protocol;
    protected final IdRewriteFunction idRewriter;

    public SoundRewriter(Protocol<C, ?, ?, ?> protocol) {
        this.protocol = protocol;
        this.idRewriter = id -> protocol.getMappingData().getSoundMappings().getNewId(id);
    }

    public SoundRewriter(Protocol<C, ?, ?, ?> protocol, IdRewriteFunction idRewriter) {
        this.protocol = protocol;
        this.idRewriter = idRewriter;
    }

    public void registerSound(C packetType) {
        protocol.registerClientbound(packetType, new PacketHandlers() {
            @Override
            public void register() {
                map(Type.VAR_INT); // Sound id
                handler(getSoundHandler());
            }
        });
    }

    public void register1_19_3Sound(C packetType) {
        protocol.registerClientbound(packetType, soundHolderHandler());
    }

    public PacketHandler soundHolderHandler() {
        return wrapper -> {
            Holder<SoundEvent> soundEventHolder = wrapper.read(Type.SOUND_EVENT);
            if (soundEventHolder.isDirect()) {
                // Is followed by the resource loation
                wrapper.write(Type.SOUND_EVENT, soundEventHolder);
                return;
            }

            final int mappedId = idRewriter.rewrite(soundEventHolder.id());
            if (mappedId == -1) {
                wrapper.cancel();
                return;
            }

            if (mappedId != soundEventHolder.id()) {
                soundEventHolder = Holder.of(mappedId);
            }

            wrapper.write(Type.SOUND_EVENT, soundEventHolder);
        };
    }

    public PacketHandler getSoundHandler() {
        return wrapper -> {
            int soundId = wrapper.get(Type.VAR_INT, 0);
            int mappedId = idRewriter.rewrite(soundId);
            if (mappedId == -1) {
                wrapper.cancel();
            } else if (soundId != mappedId) {
                wrapper.set(Type.VAR_INT, 0, mappedId);
            }
        };
    }
}

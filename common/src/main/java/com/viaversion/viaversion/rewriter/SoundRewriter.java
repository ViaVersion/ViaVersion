/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2022 ViaVersion and contributors
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

import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.protocol.remapper.PacketRemapper;
import com.viaversion.viaversion.api.type.Type;

public class SoundRewriter {
    protected final Protocol protocol;
    protected final IdRewriteFunction idRewriter;

    public SoundRewriter(Protocol protocol) {
        this.protocol = protocol;
        this.idRewriter = id -> protocol.getMappingData().getSoundMappings().getNewId(id);
    }

    public SoundRewriter(Protocol protocol, IdRewriteFunction idRewriter) {
        this.protocol = protocol;
        this.idRewriter = idRewriter;
    }

    // The same for entity sound
    public void registerSound(ClientboundPacketType packetType) {
        protocol.registerClientbound(packetType, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // Sound Id
                handler(getSoundHandler());
            }
        });
    }

    // Different to entity sounds now
    public void register1_19_3Sound(ClientboundPacketType packetType) {
        protocol.registerClientbound(packetType, new PacketRemapper() {
            @Override
            public void registerMap() {
                handler(wrapper -> {
                    int soundId = wrapper.read(Type.VAR_INT);
                    if (soundId == 0) {
                        // Is followed by the resource loation
                        wrapper.write(Type.VAR_INT, 0);
                        return;
                    }

                    // The id needs to be normalized
                    soundId--;
                    final int mappedId = idRewriter.rewrite(soundId);
                    if (mappedId == -1) {
                        wrapper.cancel();
                        return;
                    }

                    wrapper.write(Type.VAR_INT, mappedId + 1);
                });
            }
        });
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

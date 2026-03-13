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
package com.viaversion.viaversion.rewriter;

import com.viaversion.viaversion.api.data.Mappings;
import com.viaversion.viaversion.api.minecraft.Holder;
import com.viaversion.viaversion.api.minecraft.SoundEvent;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.type.Types;

public class SoundRewriter<C extends ClientboundPacketType> {
    protected final Protocol<C, ?, ?, ?> protocol;

    public SoundRewriter(Protocol<C, ?, ?, ?> protocol) {
        this.protocol = protocol;
    }

    public void registerSound(C packetType) {
        if (protocol.getMappingData() == null || Mappings.isIntIdIdentity(protocol.getMappingData().getSoundMappings())) {
            return;
        }
        protocol.registerClientbound(packetType, wrapper -> {
            wrapper.passthrough(Types.VAR_INT); // Sound id
            getSoundHandler().handle(wrapper);
        });
    }

    public void registerSound1_19_3(C packetType) {
        if (protocol.getMappingData() != null && !Mappings.isFullIdentity(protocol.getMappingData().getFullSoundMappings())) {
            protocol.registerClientbound(packetType, soundHolderHandler());
        }
    }

    public PacketHandler soundHolderHandler() {
        return wrapper -> {
            Holder<SoundEvent> soundEventHolder = wrapper.read(Types.SOUND_EVENT);
            if (soundEventHolder.isDirect()) {
                // Is followed by the resource loation
                wrapper.write(Types.SOUND_EVENT, rewriteSoundEvent(wrapper, soundEventHolder));
                return;
            }

            final int mappedId = protocol.getMappingData().getSoundMappings().getNewId(soundEventHolder.id());
            if (mappedId == -1) {
                wrapper.cancel();
                return;
            }

            if (mappedId != soundEventHolder.id()) {
                soundEventHolder = Holder.of(mappedId);
            }

            wrapper.write(Types.SOUND_EVENT, soundEventHolder);
        };
    }

    public Holder<SoundEvent> rewriteSoundEvent(final PacketWrapper wrapper, final Holder<SoundEvent> soundEventHolder) {
        final SoundEvent soundEvent = soundEventHolder.value();
        final String mappedIdentifier = protocol.getMappingData().getFullSoundMappings().mappedIdentifier(soundEvent.identifier());
        if (mappedIdentifier != null) {
            if (!mappedIdentifier.isEmpty()) {
                return Holder.of(soundEvent.withIdentifier(mappedIdentifier));
            }
            wrapper.cancel();
        }
        return soundEventHolder;
    }

    public PacketHandler getSoundHandler() {
        return wrapper -> {
            int soundId = wrapper.get(Types.VAR_INT, 0);
            int mappedId = protocol.getMappingData().getSoundMappings().getNewId(soundId);
            if (mappedId == -1) {
                wrapper.cancel();
            } else if (soundId != mappedId) {
                wrapper.set(Types.VAR_INT, 0, mappedId);
            }
        };
    }
}

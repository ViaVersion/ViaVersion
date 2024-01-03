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
package com.viaversion.viaversion.protocols.protocol1_9to1_8.providers;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.Position;
import com.viaversion.viaversion.api.platform.providers.Provider;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.ClientboundPackets1_9;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.Protocol1_9To1_8;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.storage.CommandBlockStorage;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.storage.EntityTracker1_9;
import java.util.Optional;

public class CommandBlockProvider implements Provider {

    public void addOrUpdateBlock(UserConnection user, Position position, CompoundTag tag) throws Exception {
        checkPermission(user);
        getStorage(user).addOrUpdateBlock(position, tag);
    }

    public Optional<CompoundTag> get(UserConnection user, Position position) throws Exception {
        checkPermission(user);
        return getStorage(user).getCommandBlock(position);
    }

    public void unloadChunk(UserConnection user, int x, int z) throws Exception {
        checkPermission(user);
        getStorage(user).unloadChunk(x, z);
    }

    private CommandBlockStorage getStorage(UserConnection connection) {
        return connection.get(CommandBlockStorage.class);
    }

    public void sendPermission(UserConnection user) throws Exception {
        PacketWrapper wrapper = PacketWrapper.create(ClientboundPackets1_9.ENTITY_STATUS, null, user); // Entity status

        EntityTracker1_9 tracker = user.getEntityTracker(Protocol1_9To1_8.class);
        wrapper.write(Type.INT, tracker.getProvidedEntityId()); // Entity ID
        wrapper.write(Type.BYTE, (byte) 26); // Hardcoded op permission level

        wrapper.scheduleSend(Protocol1_9To1_8.class);

        user.get(CommandBlockStorage.class).setPermissions(true);
    }

    // Fix for Bungee since the join game is not sent after the first one
    private void checkPermission(UserConnection user) throws Exception {
        CommandBlockStorage storage = getStorage(user);
        if (!storage.isPermissions()) {
            sendPermission(user);
        }
    }

    public void unloadChunks(UserConnection userConnection) {
        getStorage(userConnection).unloadChunks();
    }
}

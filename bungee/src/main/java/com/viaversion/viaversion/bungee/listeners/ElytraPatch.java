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
package com.viaversion.viaversion.bungee.listeners;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.metadata.Metadata;
import com.viaversion.viaversion.api.minecraft.metadata.types.MetaType1_9;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.version.Types1_9;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.ClientboundPackets1_9;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.Protocol1_9To1_8;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.storage.EntityTracker1_9;
import java.util.Collections;
import java.util.logging.Level;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

/*
 * This patches https://github.com/ViaVersion/ViaVersion/issues/555
 */
public class ElytraPatch implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onServerConnected(ServerConnectedEvent event) {
        UserConnection user = Via.getManager().getConnectionManager().getConnectedClient(event.getPlayer().getUniqueId());
        if (user == null) return;

        try {
            if (user.getProtocolInfo().getPipeline().contains(Protocol1_9To1_8.class)) {
                EntityTracker1_9 tracker = user.getEntityTracker(Protocol1_9To1_8.class);
                int entityId = tracker.getProvidedEntityId();

                PacketWrapper wrapper = PacketWrapper.create(ClientboundPackets1_9.ENTITY_METADATA, null, user);

                wrapper.write(Type.VAR_INT, entityId);
                wrapper.write(Types1_9.METADATA_LIST, Collections.singletonList(new Metadata(0, MetaType1_9.Byte, (byte) 0)));

                wrapper.scheduleSend(Protocol1_9To1_8.class);
            }
        } catch (Exception e) {
            Via.getPlatform().getLogger().log(Level.WARNING, "Failed to send elytra patch metadata packet!", e);
        }
    }
}

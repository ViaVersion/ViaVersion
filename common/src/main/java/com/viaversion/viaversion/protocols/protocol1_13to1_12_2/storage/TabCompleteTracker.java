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
package com.viaversion.viaversion.protocols.protocol1_13to1_12_2.storage;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.StorableObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.Position;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_12_1to1_12.ServerboundPackets1_12_1;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.Protocol1_13To1_12_2;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.providers.PlayerLookTargetProvider;
import java.util.logging.Level;

public class TabCompleteTracker implements StorableObject {
    private int transactionId;
    private String input;
    private String lastTabComplete;
    private long timeToSend;

    public void sendPacketToServer(UserConnection connection) {
        if (lastTabComplete == null || timeToSend > System.currentTimeMillis()) return;
        PacketWrapper wrapper = PacketWrapper.create(ServerboundPackets1_12_1.TAB_COMPLETE, null, connection);
        wrapper.write(Type.STRING, lastTabComplete);
        wrapper.write(Type.BOOLEAN, false);
        final Position playerLookTarget = Via.getManager().getProviders().get(PlayerLookTargetProvider.class).getPlayerLookTarget(connection);
        wrapper.write(Type.OPTIONAL_POSITION1_8, playerLookTarget);
        try {
            wrapper.scheduleSendToServer(Protocol1_13To1_12_2.class);
        } catch (Exception e) {
            Via.getPlatform().getLogger().log(Level.WARNING, "Failed to send tab complete packet", e);
        }
        lastTabComplete = null;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public String getInput() {
        return input;
    }

    public void setInput(final String input) {
        this.input = input;
    }

    public String getLastTabComplete() {
        return lastTabComplete;
    }

    public void setLastTabComplete(String lastTabComplete) {
        this.lastTabComplete = lastTabComplete;
    }

    public long getTimeToSend() {
        return timeToSend;
    }

    public void setTimeToSend(long timeToSend) {
        this.timeToSend = timeToSend;
    }
}

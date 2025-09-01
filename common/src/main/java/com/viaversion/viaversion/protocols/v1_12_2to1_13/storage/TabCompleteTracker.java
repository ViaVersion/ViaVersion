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
package com.viaversion.viaversion.protocols.v1_12_2to1_13.storage;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.StorableObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.Protocol1_12_2To1_13;
import com.viaversion.viaversion.protocols.v1_12_2to1_13.provider.PlayerLookTargetProvider;
import com.viaversion.viaversion.protocols.v1_12to1_12_1.packet.ServerboundPackets1_12_1;

public class TabCompleteTracker implements StorableObject {
    private int transactionId;
    private String input;
    private String lastTabComplete;
    private long timeToSend;

    public void sendPacketToServer(UserConnection connection) {
        if (lastTabComplete == null || timeToSend > System.currentTimeMillis()) return;
        PacketWrapper wrapper = PacketWrapper.create(ServerboundPackets1_12_1.COMMAND_SUGGESTION, null, connection);
        wrapper.write(Types.STRING, lastTabComplete);
        wrapper.write(Types.BOOLEAN, false);
        final BlockPosition playerLookTarget = Via.getManager().getProviders().get(PlayerLookTargetProvider.class).getPlayerLookTarget(connection);
        wrapper.write(Types.OPTIONAL_POSITION1_8, playerLookTarget);
        wrapper.sendToServer(Protocol1_12_2To1_13.class);
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

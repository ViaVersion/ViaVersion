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
package com.viaversion.viaversion.debug;

import com.viaversion.viaversion.api.debug.DebugHandler;
import com.viaversion.viaversion.api.protocol.packet.Direction;
import com.viaversion.viaversion.api.protocol.packet.PacketType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.HashSet;
import java.util.Set;

public final class DebugHandlerImpl implements DebugHandler {

    private final Set<String> packetTypesToLog = new HashSet<>();
    private final IntSet clientboundPacketIdsToLog = new IntOpenHashSet();
    private final IntSet serverboundPacketIdsToLog = new IntOpenHashSet();
    private boolean logPostPacketTransform;
    private boolean enabled;

    @Override
    public boolean enabled() {
        return enabled;
    }

    @Override
    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void addPacketTypeNameToLog(final String packetTypeName) {
        packetTypesToLog.add(packetTypeName);
    }

    @Override
    public void addPacketTypeToLog(PacketType packetType) {
        (packetType.direction() == Direction.SERVERBOUND ? serverboundPacketIdsToLog : clientboundPacketIdsToLog).add(packetType.getId());
    }

    @Override
    public boolean removePacketTypeNameToLog(final String packetTypeName) {
        return packetTypesToLog.remove(packetTypeName);
    }

    @Override
    public void clearPacketTypesToLog() {
        packetTypesToLog.clear();
    }

    @Override
    public boolean logPostPacketTransform() {
        return logPostPacketTransform;
    }

    @Override
    public void setLogPostPacketTransform(final boolean logPostPacketTransform) {
        this.logPostPacketTransform = logPostPacketTransform;
    }

    @Override
    public boolean shouldLog(final PacketWrapper wrapper, final Direction direction) {
        return packetTypesToLog.isEmpty() && serverboundPacketIdsToLog.isEmpty() && clientboundPacketIdsToLog.isEmpty()
                || (wrapper.getPacketType() != null && packetTypesToLog.contains(wrapper.getPacketType().getName()))
                || (direction == Direction.SERVERBOUND ? serverboundPacketIdsToLog : clientboundPacketIdsToLog).contains(wrapper.getId());
    }
}

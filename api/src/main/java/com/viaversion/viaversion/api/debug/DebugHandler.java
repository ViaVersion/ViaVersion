/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2024 ViaVersion and contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.viaversion.viaversion.api.debug;

import com.google.common.annotations.Beta;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.protocol.packet.Direction;
import com.viaversion.viaversion.api.protocol.packet.PacketType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import java.util.logging.Level;

@Beta
public interface DebugHandler {

    /**
     * Returns whether debug mode is enabled.
     *
     * @return whether debug mode is enabled
     */
    boolean enabled();

    /**
     * Sets debug mode.
     *
     * @param enabled whether debug should be enabled
     */
    void setEnabled(boolean enabled);

    /**
     * Adds a packet type name to the list of packet types to log.
     *
     * @param packetTypeName packet type name
     */
    void addPacketTypeNameToLog(String packetTypeName);

    /**
     * Adds a packet id to the list of packet types to log.
     * Packets will be checked on each protocol transformer, so this is best used on single protocol pipes.
     *
     * @param packetType packet type
     */
    void addPacketTypeToLog(PacketType packetType);

    /**
     * Removes a packet type name from the list of packet types to log.
     *
     * @param packetTypeName packet type name
     */
    boolean removePacketTypeNameToLog(String packetTypeName);

    /**
     * Resets packet type filters.
     */
    void clearPacketTypesToLog();

    /**
     * Returns whether packets should be logged after being transformed.
     * Set to true by default.
     *
     * @return whether packets should be logged after being transformed
     */
    boolean logPostPacketTransform();

    /**
     * Sets whether packets should be logged after being transformed.
     *
     * @param logPostPacketTransform whether packets should be logged after being transformed
     */
    void setLogPostPacketTransform(boolean logPostPacketTransform);

    /**
     * Returns whether the given packet should be logged.
     * If no specific packet type has been added, all packet types will be logged.
     *
     * @param wrapper   packet wrapper
     * @param direction packet direction
     * @return whether the packet should be logged
     */
    boolean shouldLog(PacketWrapper wrapper, Direction direction);

    default void enableAndLogIds(final PacketType... packetTypes) {
        setEnabled(true);
        for (final PacketType packetType : packetTypes) {
            addPacketTypeToLog(packetType);
        }
    }

    /**
     * Logs an error if debug mode is enabled or error suppression is disabled.
     *
     * @param error error message
     * @param t     thrown exception
     */
    default void error(final String error, final Throwable t) {
        if (!Via.getConfig().isSuppressConversionWarnings() || enabled()) {
            Via.getPlatform().getLogger().log(Level.SEVERE, error, t);
        }
    }
}

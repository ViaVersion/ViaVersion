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
package com.viaversion.viaversion.api.protocol.packet;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.configuration.ViaVersionConfig;
import com.viaversion.viaversion.api.connection.UserConnection;

public class PacketTracker {
    private final UserConnection connection;
    private long sentPackets;
    private long receivedPackets;
    // Used for tracking pps
    private long startTime;
    private long intervalPackets;
    private long packetsPerSecond = -1L;
    // Used for handling warnings (over time)
    private int secondsObserved;
    private int warnings;

    public PacketTracker(UserConnection connection) {
        this.connection = connection;
    }

    /**
     * Used for incrementing the number of packets sent to the client.
     */
    public void incrementSent() {
        this.sentPackets++;
    }

    /**
     * Used for incrementing the number of packets received from the client.
     *
     * @return true if the interval has reset and can now be checked for the packets sent
     */
    public boolean incrementReceived() {
        // handle stats
        long diff = System.currentTimeMillis() - startTime;
        if (diff >= 1000) {
            packetsPerSecond = intervalPackets;
            startTime = System.currentTimeMillis();
            intervalPackets = 1;
            return true;
        } else {
            intervalPackets++;
        }
        // increase total
        this.receivedPackets++;
        return false;
    }

    /**
     * Checks for packet flood with the packets sent in the last second.
     * ALWAYS check for {@link #incrementReceived()} before using this method.
     *
     * @return true if the packet should be cancelled
     * @see #incrementReceived()
     */
    public boolean exceedsMaxPPS() {
        if (connection.isClientSide()) return false; // Don't apply PPS limiting for client-side
        ViaVersionConfig conf = Via.getConfig();
        // Max PPS Checker
        if (conf.getMaxPPS() > 0 && packetsPerSecond >= conf.getMaxPPS()) {
            connection.disconnect(conf.getMaxPPSKickMessage().replace("%pps", Long.toString(packetsPerSecond)));
            return true; // don't send current packet
        }

        // Tracking PPS Checker
        if (conf.getMaxWarnings() > 0 && conf.getTrackingPeriod() > 0) {
            if (secondsObserved > conf.getTrackingPeriod()) {
                // Reset
                warnings = 0;
                secondsObserved = 1;
            } else {
                secondsObserved++;
                if (packetsPerSecond >= conf.getWarningPPS()) {
                    warnings++;
                }

                if (warnings >= conf.getMaxWarnings()) {
                    connection.disconnect(conf.getMaxWarningsKickMessage().replace("%pps", Long.toString(packetsPerSecond)));
                    return true; // don't send current packet
                }
            }
        }
        return false;
    }

    public long getSentPackets() {
        return sentPackets;
    }

    public void setSentPackets(long sentPackets) {
        this.sentPackets = sentPackets;
    }

    public long getReceivedPackets() {
        return receivedPackets;
    }

    public void setReceivedPackets(long receivedPackets) {
        this.receivedPackets = receivedPackets;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getIntervalPackets() {
        return intervalPackets;
    }

    public void setIntervalPackets(long intervalPackets) {
        this.intervalPackets = intervalPackets;
    }

    public long getPacketsPerSecond() {
        return packetsPerSecond;
    }

    public void setPacketsPerSecond(long packetsPerSecond) {
        this.packetsPerSecond = packetsPerSecond;
    }

    public int getSecondsObserved() {
        return secondsObserved;
    }

    public void setSecondsObserved(int secondsObserved) {
        this.secondsObserved = secondsObserved;
    }

    public int getWarnings() {
        return warnings;
    }

    public void setWarnings(int warnings) {
        this.warnings = warnings;
    }
}

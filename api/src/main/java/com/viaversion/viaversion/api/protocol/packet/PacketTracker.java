/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2025 ViaVersion and contributors
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
import com.viaversion.viaversion.api.configuration.RateLimitConfig;
import com.viaversion.viaversion.api.configuration.ViaVersionConfig;
import com.viaversion.viaversion.api.connection.UserConnection;
import java.util.Arrays;

public class PacketTracker {
    private static final long SECOND_NANOS = 1_000_000_000L;

    private final RateTracker packetTracker = new RateTracker(5);
    private final RateTracker packetSizeTracker = new RateTracker(3);
    private long startTime = System.nanoTime();
    private boolean packetLimiterEnabled = true;
    private final UserConnection connection;
    private long sentPacketsTotal;
    private long receivedPacketsTotal;

    public PacketTracker(UserConnection connection) {
        this.connection = connection;
    }

    /**
     * Increments the number of packets sent to the client.
     */
    public void incrementSent() {
        this.sentPacketsTotal++;
    }

    @Deprecated(forRemoval = true)
    public boolean incrementReceived() {
        return incrementReceived(0);
    }

    /**
     * Increments the number of packets received from the client.
     *
     * @return true if the interval has reset
     */
    public boolean incrementReceived(int packetSize) {
        receivedPacketsTotal++;

        ViaVersionConfig config = Via.getConfig();
        long currentTime = System.nanoTime();
        long elapsed = currentTime - startTime;
        if (elapsed < SECOND_NANOS) {
            // Add to current interval
            if (config.getPacketTrackerConfig().enabled()) {
                packetTracker.addIntervalValue(1);
            }
            if (config.getPacketSizeTrackerConfig().enabled()) {
                packetSizeTracker.addIntervalValue(packetSize);
            }
            return false;
        }

        // Reset interval tracking
        if (config.getPacketTrackerConfig().enabled()) {
            packetTracker.updateRate(elapsed);
            packetTracker.addIntervalValue(1);
        }
        if (config.getPacketSizeTrackerConfig().enabled()) {
            packetSizeTracker.updateRate(elapsed);
            packetSizeTracker.addIntervalValue(packetSize);
        }
        startTime = currentTime;
        return true;
    }

    public boolean exceedsLimits() {
        if (connection.isClientSide()) {
            return false;
        }

        ViaVersionConfig config = Via.getConfig();
        return packetTracker.exceedsLimit(connection, config.getPacketTrackerConfig())
            || packetSizeTracker.exceedsLimit(connection, config.getPacketSizeTrackerConfig());
    }

    public long getSentPackets() {
        return sentPacketsTotal;
    }

    @Deprecated(forRemoval = true)
    public void setSentPackets(long sentPackets) {
        this.sentPacketsTotal = sentPackets;
    }

    public long getReceivedPackets() {
        return receivedPacketsTotal;
    }

    @Deprecated(forRemoval = true)
    public void setReceivedPackets(long receivedPackets) {
        this.receivedPacketsTotal = receivedPackets;
    }

    @Deprecated(forRemoval = true)
    public long getIntervalPackets() {
        return this.packetTracker.intervalValue;
    }

    @Deprecated(forRemoval = true)
    public void setIntervalPackets(long intervalPackets) {
        this.packetTracker.intervalValue = Math.toIntExact(intervalPackets);
    }

    public int getPacketsPerSecond() {
        return packetTracker.currentRate;
    }

    public boolean isPacketLimiterEnabled() {
        ViaVersionConfig config = Via.getConfig();
        return packetLimiterEnabled && config.getPacketTrackerConfig().enabled() && config.getPacketSizeTrackerConfig().enabled();
    }

    public void setPacketLimiterEnabled(boolean packetLimiterEnabled) {
        this.packetLimiterEnabled = packetLimiterEnabled;
    }

    @Deprecated(forRemoval = true)
    public void setWarnings(int warnings) {
        this.packetTracker.warnings = warnings;
    }

    private static final class RateTracker {
        private final int[] history;
        private int historyIndex;
        private int historyCount;
        private int currentRate = -1;

        // Interval tracking
        int intervalValue;

        // Warning system
        private long warningPeriodStart = System.nanoTime();
        private int warnings;

        public RateTracker(int windowSize) {
            this.history = new int[windowSize];
        }

        public void addIntervalValue(int value) {
            intervalValue += value;
        }

        public void updateRate(long elapsedNanos) {
            // Update sliding window
            history[historyIndex] = (int) ((intervalValue * SECOND_NANOS) / elapsedNanos);
            historyIndex = (historyIndex + 1) % history.length;
            if (historyCount < history.length) {
                historyCount++;
            }

            // Calculate smoothed average
            int sum = 0;
            for (int i = 0; i < historyCount; i++) {
                sum += history[i];
            }

            currentRate = sum / historyCount;
            intervalValue = 0; // Reset
        }

        public boolean exceedsLimit(UserConnection connection, RateLimitConfig limitConfig) {
            if (!limitConfig.enabled() || currentRate < 0) {
                return false;
            }

            // Immediate limit check
            if (limitConfig.maxRate() > 0 && currentRate >= limitConfig.maxRate()) {
                connection.disconnect(limitConfig.maxRateKickMessage().replace(limitConfig.ratePlaceholder(), Integer.toString(currentRate)));
                return true;
            }

            // Interval warnings
            if (limitConfig.maxWarnings() > 0 && limitConfig.trackingPeriodNanos() > 0 && limitConfig.warningRate() > 0) {
                return checkTrackedInterval(connection, limitConfig);
            }

            return false;
        }

        private boolean checkTrackedInterval(UserConnection connection, RateLimitConfig config) {
            long currentTime = System.nanoTime();
            if (currentTime - warningPeriodStart >= config.trackingPeriodNanos()) {
                // Reset
                warnings = 0;
                warningPeriodStart = currentTime;
                return false;
            }

            if (currentRate >= config.warningRate() && ++warnings >= config.maxWarnings()) {
                connection.disconnect(config.warningKickMessage().replace(config.ratePlaceholder(), Integer.toString(currentRate)));
                return true;
            }
            return false;
        }

        public void reset() {
            intervalValue = 0;
            currentRate = -1;
            warnings = 0;
            warningPeriodStart = System.nanoTime();
            historyIndex = 0;
            historyCount = 0;
            Arrays.fill(history, 0);
        }
    }

    public void reset() {
        sentPacketsTotal = 0;
        receivedPacketsTotal = 0;
        startTime = System.nanoTime();
        packetTracker.reset();
        packetSizeTracker.reset();
    }
}

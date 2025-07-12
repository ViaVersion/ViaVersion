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
package com.viaversion.viaversion.protocols.v1_20_3to1_20_5.storage;

import com.viaversion.viaversion.api.connection.StorableObject;
import com.viaversion.viaversion.api.minecraft.ProfileKey;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_20_2to1_20_3.packet.ServerboundPackets1_20_3;
import com.viaversion.viaversion.protocols.v1_20_3to1_20_5.Protocol1_20_3To1_20_5;
import java.util.BitSet;
import java.util.UUID;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Tracks the last Secure Chat state that we received from the client. This is important to always have a valid 'last
 * seen' state that is consistent with future and past updates from the client (which may be signed). This state is
 * used to construct signed command packets to the outdated server from unsigned ones from the modern client.
 * <ul>
 *     <li>If we last forwarded a chat or command packet from the client, we have a known 'last seen' that we can
 *     reuse.</li>
 *     <li>If we last forwarded a chat acknowledgement packet, the previous 'last seen' cannot be reused. We
 *     cannot predict an up-to-date 'last seen', as we do not know which messages the client actually saw.</li>
 *     <li>Therefore, we need to hold back any acknowledgement packets so that we can continue to reuse the last valid
 *     'last seen' state.</li>
 *     <li>However, there is a limit to the number of messages that can remain unacknowledged on the server.</li>
 *     <li>To address this, we know that if the client has moved its 'last seen' window far enough, we can fill in the
 *     gap with dummy 'last seen', and it will never be checked.</li>
 * </ul>
 */
public final class AcknowledgedMessagesStorage implements StorableObject {
    private static final int MAX_HISTORY = 20;
    private static final int MINIMUM_DELAYED_ACK_COUNT = MAX_HISTORY;
    private static final BitSet DUMMY_LAST_SEEN_MESSAGES = new BitSet();

    private Boolean secureChatEnforced;
    private ChatSession chatSession;

    private BitSet lastSeenMessages = new BitSet();
    private int delayedAckCount;

    public int updateFromMessage(int ackCount, BitSet lastSeenMessages) {
        // We held back some acknowledged messages, so flush that out now that we have a known 'last seen' state again
        int delayedAckCount = this.delayedAckCount;
        this.delayedAckCount = 0;
        this.lastSeenMessages = lastSeenMessages;
        return ackCount + delayedAckCount;
    }

    public int accumulateAckCount(int ackCount) {
        delayedAckCount += ackCount;
        int ackCountToForward = delayedAckCount - MINIMUM_DELAYED_ACK_COUNT;
        if (ackCountToForward >= MAX_HISTORY) {
            // Because we only forward acknowledgements above the window size, we don't have to shift the previous 'last seen' state
            lastSeenMessages = DUMMY_LAST_SEEN_MESSAGES;
            delayedAckCount = MINIMUM_DELAYED_ACK_COUNT;
            return ackCountToForward;
        }
        return 0;
    }

    public BitSet createSpoofedAck() {
        return lastSeenMessages;
    }

    public void setSecureChatEnforced(final boolean secureChatEnforced) {
        this.secureChatEnforced = secureChatEnforced;
    }

    public @Nullable Boolean secureChatEnforced() {
        return this.secureChatEnforced;
    }

    public boolean isSecureChatEnforced() {
        // Assume it is enforced by default
        return this.secureChatEnforced == null || this.secureChatEnforced;
    }

    public void queueChatSession(final UUID sessionId, final ProfileKey profileKey) {
        this.chatSession = new ChatSession(sessionId, profileKey);
    }

    public void sendQueuedChatSession(final PacketWrapper wrapper) {
        if (chatSession == null) {
            return;
        }

        final PacketWrapper chatSessionUpdate = wrapper.create(ServerboundPackets1_20_3.CHAT_SESSION_UPDATE);
        chatSessionUpdate.write(Types.UUID, chatSession.sessionId());
        chatSessionUpdate.write(Types.PROFILE_KEY, chatSession.profileKey());
        chatSessionUpdate.sendToServer(Protocol1_20_3To1_20_5.class);
        chatSession = null;
    }

    public record ChatSession(UUID sessionId, ProfileKey profileKey) {
    }

    public void clear() {
        lastSeenMessages = new BitSet();
        delayedAckCount = 0;
    }
}

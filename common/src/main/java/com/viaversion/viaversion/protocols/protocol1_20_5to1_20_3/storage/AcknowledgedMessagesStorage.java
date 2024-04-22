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
package com.viaversion.viaversion.protocols.protocol1_20_5to1_20_3.storage;

import com.viaversion.viaversion.api.connection.StorableObject;
import com.viaversion.viaversion.api.minecraft.ProfileKey;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_20_3to1_20_2.packet.ServerboundPackets1_20_3;
import com.viaversion.viaversion.protocols.protocol1_20_5to1_20_3.Protocol1_20_5To1_20_3;
import java.util.Arrays;
import java.util.BitSet;
import java.util.UUID;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class AcknowledgedMessagesStorage implements StorableObject {
    private static final int MAX_HISTORY = 20;
    private final boolean[] trackedMessages = new boolean[MAX_HISTORY];
    private Boolean secureChatEnforced;
    private ChatSession chatSession;
    private int offset;
    private int tail;
    private byte[] lastMessage;

    public boolean add(final byte[] message) {
        if (Arrays.equals(message, lastMessage)) {
            return false;
        }
        this.lastMessage = message;
        this.offset++;
        this.trackedMessages[this.tail] = true;
        this.tail = (this.tail + 1) % MAX_HISTORY;
        return true;
    }

    public BitSet toAck() {
        final BitSet acks = new BitSet(MAX_HISTORY);
        for (int i = 0; i < MAX_HISTORY; i++) {
            final int messageIndex = (this.tail + i) % MAX_HISTORY;
            acks.set(i, this.trackedMessages[messageIndex]);
        }
        return acks;
    }

    public int offset() {
        return this.offset;
    }

    public void clearOffset() {
        this.offset = 0;
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

    public void sendQueuedChatSession(final PacketWrapper wrapper) throws Exception {
        if (chatSession == null) {
            return;
        }

        final PacketWrapper chatSessionUpdate = wrapper.create(ServerboundPackets1_20_3.CHAT_SESSION_UPDATE);
        chatSessionUpdate.write(Type.UUID, chatSession.sessionId());
        chatSessionUpdate.write(Type.PROFILE_KEY, chatSession.profileKey());
        chatSessionUpdate.sendToServer(Protocol1_20_5To1_20_3.class);
        chatSession = null;
    }

    public static final class ChatSession {
        private final UUID sessionId;
        private final ProfileKey profileKey;

        public ChatSession(final UUID sessionId, final ProfileKey profileKey) {
            this.sessionId = sessionId;
            this.profileKey = profileKey;
        }

        public UUID sessionId() {
            return sessionId;
        }

        public ProfileKey profileKey() {
            return profileKey;
        }
    }

    public void clear() {
        this.offset = 0;
        this.tail = 0;
        this.lastMessage = null;
        Arrays.fill(this.trackedMessages, false);
    }

    @Override
    public boolean clearOnServerSwitch() {
        return false;
    }
}

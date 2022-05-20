/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2022 ViaVersion and contributors
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
package com.viaversion.viaversion.protocols.protocol1_19to1_18_2.storage;

import com.google.gson.JsonElement;
import com.viaversion.viaversion.api.connection.StorableObject;

import java.util.ArrayDeque;
import java.util.Queue;

public final class QueuedMessagesStorage implements StorableObject {

    private final Queue<Message> messages = new ArrayDeque<>();

    public Queue<Message> messages() {
        return messages;
    }

    @Override
    public boolean clearOnServerSwitch() {
        return false;
    }

    public static final class Message {
        private final JsonElement content;
        private final int chatType;

        public Message(final JsonElement content, final int chatType) {
            this.content = content;
            this.chatType = chatType;
        }

        public JsonElement content() {
            return content;
        }

        public int chatType() {
            return chatType;
        }
    }
}

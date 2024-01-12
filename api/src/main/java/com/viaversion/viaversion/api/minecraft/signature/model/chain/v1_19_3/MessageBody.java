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
package com.viaversion.viaversion.api.minecraft.signature.model.chain.v1_19_3;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.viaversion.viaversion.api.minecraft.PlayerMessageSignature;
import com.viaversion.viaversion.api.minecraft.signature.util.DataConsumer;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

public class MessageBody {

    private final String content;
    private final Instant timestamp;
    private final long salt;
    private final PlayerMessageSignature[] lastSeenMessages;

    public MessageBody(final String content, final Instant timestamp, final long salt, final PlayerMessageSignature[] lastSeenMessages) {
        this.content = content;
        this.timestamp = timestamp;
        this.salt = salt;
        this.lastSeenMessages = lastSeenMessages;
    }

    public void update(final DataConsumer dataConsumer) {
        dataConsumer.accept(Longs.toByteArray(this.salt));
        dataConsumer.accept(Longs.toByteArray(this.timestamp.getEpochSecond()));
        final byte[] contentData = this.content.getBytes(StandardCharsets.UTF_8);
        dataConsumer.accept(Ints.toByteArray(contentData.length));
        dataConsumer.accept(contentData);

        dataConsumer.accept(Ints.toByteArray(this.lastSeenMessages.length));
        for (PlayerMessageSignature messageSignatureData : this.lastSeenMessages) {
            dataConsumer.accept(messageSignatureData.signatureBytes());
        }
    }

}

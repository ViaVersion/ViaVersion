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
package com.viaversion.viaversion.api.minecraft.signature.storage;

import com.viaversion.viaversion.api.minecraft.PlayerMessageSignature;
import com.viaversion.viaversion.api.minecraft.ProfileKey;
import com.viaversion.viaversion.api.minecraft.signature.model.DecoratableMessage;
import com.viaversion.viaversion.api.minecraft.signature.model.MessageMetadata;
import com.viaversion.viaversion.api.minecraft.signature.model.chain.v1_19_1.MessageBody;
import com.viaversion.viaversion.api.minecraft.signature.model.chain.v1_19_1.MessageHeader;

import java.security.PrivateKey;
import java.security.SignatureException;
import java.util.UUID;

public class ChatSession1_19_1 extends ChatSession {

    private byte[] precedingSignature;

    public ChatSession1_19_1(UUID uuid, PrivateKey privateKey, ProfileKey profileKey) {
        super(uuid, privateKey, profileKey);
    }

    public byte[] signChatMessage(final MessageMetadata metadata, final DecoratableMessage content, final PlayerMessageSignature[] lastSeenMessages) throws SignatureException {
        final byte[] signature = this.sign(signer -> {
            final MessageHeader messageHeader = new MessageHeader(this.precedingSignature, metadata.sender());
            final MessageBody messageBody = new MessageBody(content, metadata.timestamp(), metadata.salt(), lastSeenMessages);
            messageHeader.update(signer);
            messageBody.update(signer);
        });
        this.precedingSignature = signature;
        return signature;
    }

}

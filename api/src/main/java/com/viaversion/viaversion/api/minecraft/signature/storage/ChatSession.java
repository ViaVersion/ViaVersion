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

import com.viaversion.viaversion.api.connection.StorableObject;
import com.viaversion.viaversion.api.minecraft.ProfileKey;
import com.viaversion.viaversion.api.minecraft.signature.util.DataConsumer;

import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

public class ChatSession implements StorableObject {

    private final UUID uuid;
    private final PrivateKey privateKey;
    private final ProfileKey profileKey;
    private final Signature signer;

    public ChatSession(final UUID uuid, final PrivateKey privateKey, final ProfileKey profileKey) {
        Objects.requireNonNull(uuid, "uuid");
        Objects.requireNonNull(privateKey, "privateKey");
        Objects.requireNonNull(profileKey, "profileKey");
        this.uuid = uuid;
        this.privateKey = privateKey;
        this.profileKey = profileKey;

        try {
            this.signer = Signature.getInstance("SHA256withRSA");
            this.signer.initSign(this.privateKey);
        } catch (Throwable e) {
            throw new RuntimeException("Failed to initialize signature", e);
        }
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public ProfileKey getProfileKey() {
        return this.profileKey;
    }

    public byte[] sign(final Consumer<DataConsumer> dataConsumer) throws SignatureException {
        dataConsumer.accept(bytes -> {
            try {
                this.signer.update(bytes);
            } catch (SignatureException e) {
                throw new RuntimeException(e);
            }
        });
        return this.signer.sign();
    }

}

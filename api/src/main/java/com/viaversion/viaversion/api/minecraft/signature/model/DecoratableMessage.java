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
package com.viaversion.viaversion.api.minecraft.signature.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class DecoratableMessage {

    private final String plain;
    private final JsonElement decorated;

    public DecoratableMessage(final String plain) {
        this(plain, createLiteralText(plain));
    }

    public DecoratableMessage(final String plain, final JsonElement decorated) {
        this.plain = plain;
        this.decorated = decorated;
    }

    public String plain() {
        return this.plain;
    }

    public JsonElement decorated() {
        return this.decorated;
    }

    public boolean isDecorated() {
        return !this.decorated.equals(createLiteralText(plain));
    }

    private static JsonElement createLiteralText(final String text) {
        final JsonObject object = new JsonObject();
        object.addProperty("text", text);
        return object;
    }

}

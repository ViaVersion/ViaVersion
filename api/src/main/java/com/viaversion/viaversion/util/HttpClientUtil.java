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
package com.viaversion.viaversion.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

public final class HttpClientUtil {

    private static final Cache<Executor, HttpClient> clients = CacheBuilder.newBuilder()
        .concurrencyLevel(1)
        .expireAfterAccess(30L, TimeUnit.MINUTES)
        .maximumSize(16)
        .build();

    private HttpClientUtil() {}

    public static HttpClient get(Executor executor) {
        try {
            return clients.get(executor, () -> HttpClient.newBuilder().executor(executor).build());
        } catch (ExecutionException e) {
            throw new RuntimeException("Failed to get HttpClient for " + executor, e);
        }
    }

    public static HttpResponse.BodyHandler<JsonElement> jsonResponse(Gson gson) {
        return jsonResponse(gson, JsonElement.class);
    }

    public static <T> HttpResponse.BodyHandler<T> jsonResponse(Gson gson, Class<T> type) {
        return jsonResponse(gson, type, StandardCharsets.UTF_8);
    }

    public static <T> HttpResponse.BodyHandler<T> jsonResponse(Gson gson, Class<T> type, Charset charset) {
        return responseInfo -> HttpResponse.BodySubscribers.mapping(
            HttpResponse.BodySubscribers.ofInputStream(),
            inputStream -> {
                try (InputStreamReader reader = new InputStreamReader(inputStream, charset)) {
                    return gson.fromJson(reader, type);
                } catch (IOException e) {
                    throw new UncheckedIOException("Error parsing JSON response", e);
                }
            }
        );
    }

}

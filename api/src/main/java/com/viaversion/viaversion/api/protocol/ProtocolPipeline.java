/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2023 ViaVersion and contributors
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
package com.viaversion.viaversion.api.protocol;

import com.viaversion.viaversion.api.connection.UserConnection;
import java.util.Collection;
import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface ProtocolPipeline extends SimpleProtocol {

    /**
     * Adds a protocol to the current pipeline.
     * This will call the {@link Protocol#init(UserConnection)} method.
     *
     * @param protocol protocol to add to the end
     */
    void add(Protocol protocol);

    /**
     * Adds a collection of protocols to the current pipeline.
     * This will call the {@link Protocol#init(UserConnection)} method.
     * <p>
     * Callers of this method should make sure the collection is correctly sorted.
     *
     * @param protocols correctly sorted protocols to add to the end
     */
    void add(Collection<Protocol> protocols);

    /**
     * Returns whether the protocol is in this pipeline.
     *
     * @param protocolClass protocol class
     * @return whether the protocol class is in this pipeline
     */
    boolean contains(Class<? extends Protocol> protocolClass);

    /**
     * Returns the protocol from the given class if present in the pipeline.
     *
     * @param pipeClass protocol class
     * @param <P>       protocol
     * @return protocol from class
     * @see #contains(Class)
     * @see ProtocolManager#getProtocol(Class) for a faster implementation
     */
    @Nullable <P extends Protocol> P getProtocol(Class<P> pipeClass);

    /**
     * Returns the list of protocols this pipeline contains.
     *
     * @return list of protocols in this pipe
     */
    List<Protocol> pipes();

    /**
     * Returns whether this pipe has protocols that are not base protocols, as given by {@link Protocol#isBaseProtocol()}.
     *
     * @return whether this pipe has protocols that are not base protocols
     */
    boolean hasNonBaseProtocols();

    /**
     * Cleans the pipe and adds the base protocol.
     * /!\ WARNING - It doesn't add version-specific base Protocol.
     */
    void cleanPipes();
}

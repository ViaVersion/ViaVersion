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
package com.viaversion.viaversion.api.minecraft.codec;

import com.viaversion.viaversion.api.data.MappingData;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.util.Key;

public interface CodecContext {

    RegistryAccess registryAccess();

    boolean mapped();

    boolean isSupported(StructuredDataKey<?> key);

    interface RegistryAccess {

        Key item(int id);

        Key attributeModifier(int id);

        Key dataComponentType(int id);

        Key entity(int id);

        Key blockEntity(int id);

        Key sound(int id);

        /**
         * Returns the key for a stored mapping type and its numeric id.
         *
         * @param mappingType mapping type
         * @param id numeric id
         * @return the key
         */
        Key key(MappingData.MappingType mappingType, int id);

        /**
         * Returns the numeric id for a stored mapping type and its identifier.
         *
         * @param mappingType mapping type
         * @param identifier identifier
         * @return the numeric id, or -1 if not found
         */
        int id(MappingData.MappingType mappingType, String identifier);

        static RegistryAccess of(final Protocol<?, ?, ?, ?> protocol) {
            return new RegistryAccessImpl(protocol);
        }

        /**
         * Returns the key for a client-synchronized registry element.
         *
         * @param registry registry key
         * @param id numeric id
         * @return the key
         */
        Key registryKey(String registry, int id);

        RegistryAccess withMapped(boolean mapped);
    }
}

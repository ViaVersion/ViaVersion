/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2021 ViaVersion and contributors
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
package com.viaversion.viaversion.protocol.packet;

import com.google.common.base.Preconditions;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.Direction;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.TypeConverter;
import com.viaversion.viaversion.exception.CancelException;
import com.viaversion.viaversion.exception.InformativeException;
import com.viaversion.viaversion.util.Pair;
import com.viaversion.viaversion.util.PipelineUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.NoSuchElementException;

public class PacketWrapperImpl implements PacketWrapper {
    private static final Protocol[] PROTOCOL_ARRAY = new Protocol[0];

    private final ByteBuf inputBuffer;
    private final UserConnection userConnection;
    private boolean send = true;
    private int id = -1;
    private final Deque<Pair<Type, Object>> readableObjects = new ArrayDeque<>();
    private final List<Pair<Type, Object>> packetValues = new ArrayList<>();

    public PacketWrapperImpl(int packetId, @Nullable ByteBuf inputBuffer, UserConnection userConnection) {
        this.id = packetId;
        this.inputBuffer = inputBuffer;
        this.userConnection = userConnection;
    }

    @Override
    public <T> T get(Type<T> type, int index) throws Exception {
        int currentIndex = 0;
        for (Pair<Type, Object> packetValue : packetValues) {
            if (packetValue.getKey() != type) continue;
            if (currentIndex == index) {
                return (T) packetValue.getValue();
            }
            currentIndex++;
        }

        Exception e = new ArrayIndexOutOfBoundsException("Could not find type " + type.getTypeName() + " at " + index);
        throw new InformativeException(e).set("Type", type.getTypeName()).set("Index", index).set("Packet ID", getId()).set("Data", packetValues);
    }

    @Override
    public boolean is(Type type, int index) {
        int currentIndex = 0;
        for (Pair<Type, Object> packetValue : packetValues) {
            if (packetValue.getKey() != type) continue;
            if (currentIndex == index) {
                return true;
            }
            currentIndex++;
        }
        return false;
    }

    @Override
    public boolean isReadable(Type type, int index) {
        int currentIndex = 0;
        for (Pair<Type, Object> packetValue : readableObjects) {
            if (packetValue.getKey().getBaseClass() != type.getBaseClass()) continue;
            if (currentIndex == index) {
                return true;
            }
            currentIndex++;
        }
        return false;
    }


    @Override
    public <T> void set(Type<T> type, int index, T value) throws Exception {
        int currentIndex = 0;
        for (Pair<Type, Object> packetValue : packetValues) {
            if (packetValue.getKey() != type) continue;
            if (currentIndex == index) {
                packetValue.setValue(attemptTransform(type, value));
                return;
            }
            currentIndex++;
        }
        Exception e = new ArrayIndexOutOfBoundsException("Could not find type " + type.getTypeName() + " at " + index);
        throw new InformativeException(e).set("Type", type.getTypeName()).set("Index", index).set("Packet ID", getId());
    }

    @Override
    public <T> T read(Type<T> type) throws Exception {
        if (type == Type.NOTHING) return null;
        if (readableObjects.isEmpty()) {
            Preconditions.checkNotNull(inputBuffer, "This packet does not have an input buffer.");
            // We could in the future log input read values, but honestly for things like bulk maps, mem waste D:
            try {
                return type.read(inputBuffer);
            } catch (Exception e) {
                throw new InformativeException(e).set("Type", type.getTypeName()).set("Packet ID", getId()).set("Data", packetValues);
            }
        }

        Pair<Type, Object> read = readableObjects.poll();
        Type rtype = read.getKey();
        if (rtype == type
                || (type.getBaseClass() == rtype.getBaseClass()
                && type.getOutputClass() == rtype.getOutputClass())) {
            return (T) read.getValue();
        } else if (rtype == Type.NOTHING) {
            return read(type); // retry
        } else {
            Exception e = new IOException("Unable to read type " + type.getTypeName() + ", found " + read.getKey().getTypeName());
            throw new InformativeException(e).set("Type", type.getTypeName()).set("Packet ID", getId()).set("Data", packetValues);
        }
    }

    @Override
    public <T> void write(Type<T> type, T value) {
        packetValues.add(new Pair<>(type, attemptTransform(type, value)));
    }

    /**
     * Returns the value if already matching, else the converted value or possibly unmatched value.
     *
     * @param expectedType expected type
     * @param value        value
     * @return value if already matching, else the converted value or possibly unmatched value
     */
    private @Nullable Object attemptTransform(Type<?> expectedType, @Nullable Object value) {
        if (value != null && !expectedType.getOutputClass().isAssignableFrom(value.getClass())) {
            // Attempt conversion
            if (expectedType instanceof TypeConverter) {
                return ((TypeConverter) expectedType).from(value);
            }

            Via.getPlatform().getLogger().warning("Possible type mismatch: " + value.getClass().getName() + " -> " + expectedType.getOutputClass());
        }
        return value;
    }

    @Override
    public <T> T passthrough(Type<T> type) throws Exception {
        T value = read(type);
        write(type, value);
        return value;
    }

    @Override
    public void passthroughAll() throws Exception {
        // Copy previous objects
        packetValues.addAll(readableObjects);
        readableObjects.clear();
        // If the buffer has readable bytes, copy them.
        if (inputBuffer.isReadable()) {
            passthrough(Type.REMAINING_BYTES);
        }
    }

    @Override
    public void writeToBuffer(ByteBuf buffer) throws Exception {
        if (id != -1) {
            Type.VAR_INT.writePrimitive(buffer, id);
        }
        if (!readableObjects.isEmpty()) {
            packetValues.addAll(readableObjects);
            readableObjects.clear();
        }

        int index = 0;
        for (Pair<Type, Object> packetValue : packetValues) {
            try {
                packetValue.getKey().write(buffer, packetValue.getValue());
            } catch (Exception e) {
                throw new InformativeException(e).set("Index", index).set("Type", packetValue.getKey().getTypeName()).set("Packet ID", getId()).set("Data", packetValues);
            }
            index++;
        }
        writeRemaining(buffer);
    }

    @Override
    public void clearInputBuffer() {
        if (inputBuffer != null) {
            inputBuffer.clear();
        }
        readableObjects.clear(); // :(
    }

    @Override
    public void clearPacket() {
        clearInputBuffer();
        packetValues.clear();
    }

    private void writeRemaining(ByteBuf output) {
        if (inputBuffer != null) {
            output.writeBytes(inputBuffer);
        }
    }

    @Override
    public void send(Class<? extends Protocol> protocol, boolean skipCurrentPipeline) throws Exception {
        send0(protocol, skipCurrentPipeline, true);
    }

    @Override
    public void scheduleSend(Class<? extends Protocol> protocol, boolean skipCurrentPipeline) throws Exception {
        send0(protocol, skipCurrentPipeline, false);
    }

    private void send0(Class<? extends Protocol> protocol, boolean skipCurrentPipeline, boolean currentThread) throws Exception {
        if (isCancelled()) return;

        try {
            ByteBuf output = constructPacket(protocol, skipCurrentPipeline, Direction.CLIENTBOUND);
            if (currentThread) {
                user().sendRawPacket(output);
            } else {
                user().scheduleSendRawPacket(output);
            }
        } catch (Exception e) {
            if (!PipelineUtil.containsCause(e, CancelException.class)) {
                throw e;
            }
        }
    }

    /**
     * Let the packet go through the protocol pipes and write it to ByteBuf
     *
     * @param packetProtocol      The protocol version of the packet.
     * @param skipCurrentPipeline Skip the current pipeline
     * @return Packet buffer
     * @throws Exception if it fails to write
     */
    private ByteBuf constructPacket(Class<? extends Protocol> packetProtocol, boolean skipCurrentPipeline, Direction direction) throws Exception {
        // Apply current pipeline - for outgoing protocol, the collection will be reversed in the apply method
        Protocol[] protocols = user().getProtocolInfo().getPipeline().pipes().toArray(PROTOCOL_ARRAY);
        boolean reverse = direction == Direction.CLIENTBOUND;
        int index = -1;
        for (int i = 0; i < protocols.length; i++) {
            if (protocols[i].getClass() == packetProtocol) {
                index = i;
                break;
            }
        }

        if (index == -1) {
            // The given protocol is not in the pipeline
            throw new NoSuchElementException(packetProtocol.getCanonicalName());
        }

        if (skipCurrentPipeline) {
            index = reverse ? index - 1 : index + 1;
        }

        // Reset reader before we start
        resetReader();

        // Apply other protocols
        apply(direction, user().getProtocolInfo().getState(), index, protocols, reverse);
        ByteBuf output = inputBuffer == null ? user().getChannel().alloc().buffer() : inputBuffer.alloc().buffer();
        try {
            writeToBuffer(output);
            return output.retain();
        } finally {
            output.release();
        }
    }

    @Override
    public ChannelFuture sendFuture(Class<? extends Protocol> packetProtocol) throws Exception {
        if (!isCancelled()) {
            ByteBuf output = constructPacket(packetProtocol, true, Direction.CLIENTBOUND);
            return user().sendRawPacketFuture(output);
        }
        return user().getChannel().newFailedFuture(new Exception("Cancelled packet"));
    }

    @Override
    @Deprecated
    public void send() throws Exception {
        if (isCancelled()) return;

        // Send
        ByteBuf output = inputBuffer == null ? user().getChannel().alloc().buffer() : inputBuffer.alloc().buffer();
        try {
            writeToBuffer(output);
            user().sendRawPacket(output.retain());
        } finally {
            output.release();
        }
    }

    @Override
    public PacketWrapperImpl create(int packetId) {
        return new PacketWrapperImpl(packetId, null, user());
    }

    @Override
    public PacketWrapperImpl create(int packetId, PacketHandler handler) throws Exception {
        PacketWrapperImpl wrapper = create(packetId);
        handler.handle(wrapper);
        return wrapper;
    }

    @Override
    public PacketWrapperImpl apply(Direction direction, State state, int index, List<Protocol> pipeline, boolean reverse) throws Exception {
        Protocol[] array = pipeline.toArray(PROTOCOL_ARRAY);
        return apply(direction, state, reverse ? array.length - 1 : index, array, reverse); // Copy to prevent from removal
    }

    @Override
    public PacketWrapperImpl apply(Direction direction, State state, int index, List<Protocol> pipeline) throws Exception {
        return apply(direction, state, index, pipeline.toArray(PROTOCOL_ARRAY), false);
    }

    private PacketWrapperImpl apply(Direction direction, State state, int index, Protocol[] pipeline, boolean reverse) throws Exception {
        // Reset the reader after every transformation for the packetWrapper, so it can be recycled across packets
        if (reverse) {
            for (int i = index; i >= 0; i--) {
                pipeline[i].transform(direction, state, this);
                resetReader();
            }
        } else {
            for (int i = index; i < pipeline.length; i++) {
                pipeline[i].transform(direction, state, this);
                resetReader();
            }
        }
        return this;
    }

    @Override
    public void cancel() {
        this.send = false;
    }

    @Override
    public boolean isCancelled() {
        return !this.send;
    }

    @Override
    public UserConnection user() {
        return this.userConnection;
    }

    @Override
    public void resetReader() {
        // Move all packet values to the readable for next packet.
        for (int i = packetValues.size() - 1; i >= 0; i--) {
            this.readableObjects.addFirst(this.packetValues.get(i));
        }
        this.packetValues.clear();
    }

    @Override
    @Deprecated
    public void sendToServer() throws Exception {
        if (isCancelled()) return;

        ByteBuf output = inputBuffer == null ? user().getChannel().alloc().buffer() : inputBuffer.alloc().buffer();
        try {
            writeToBuffer(output);
            user().sendRawPacketToServer(output.retain());
        } finally {
            output.release();
        }
    }

    @Override
    public void sendToServer(Class<? extends Protocol> protocol, boolean skipCurrentPipeline) throws Exception {
        sendToServer0(protocol, skipCurrentPipeline, true);
    }

    @Override
    public void scheduleSendToServer(Class<? extends Protocol> protocol, boolean skipCurrentPipeline) throws Exception {
        sendToServer0(protocol, skipCurrentPipeline, false);
    }

    private void sendToServer0(Class<? extends Protocol> protocol, boolean skipCurrentPipeline, boolean currentThread) throws Exception {
        if (isCancelled()) return;

        try {
            ByteBuf output = constructPacket(protocol, skipCurrentPipeline, Direction.SERVERBOUND);
            if (currentThread) {
                user().sendRawPacketToServer(output);
            } else {
                user().scheduleSendRawPacketToServer(output);
            }
        } catch (Exception e) {
            if (!PipelineUtil.containsCause(e, CancelException.class)) {
                throw e;
            }
        }
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    public @Nullable ByteBuf getInputBuffer() {
        return inputBuffer;
    }

    @Override
    public String toString() {
        return "PacketWrapper{" +
                "packetValues=" + packetValues +
                ", readableObjects=" + readableObjects +
                ", id=" + id +
                '}';
    }
}

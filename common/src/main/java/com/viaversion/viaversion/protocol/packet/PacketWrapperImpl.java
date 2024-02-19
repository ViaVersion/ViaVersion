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
package com.viaversion.viaversion.protocol.packet;

import com.google.common.base.Preconditions;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.ProtocolInfo;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.Direction;
import com.viaversion.viaversion.api.protocol.packet.PacketType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.TypeConverter;
import com.viaversion.viaversion.exception.CancelException;
import com.viaversion.viaversion.exception.InformativeException;
import com.viaversion.viaversion.util.PipelineUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.Nullable;

public class PacketWrapperImpl implements PacketWrapper {
    private final Deque<PacketValue<?>> readableObjects = new ArrayDeque<>();
    private final List<PacketValue<?>> packetValues = new ArrayList<>();
    private final ByteBuf inputBuffer;
    private final UserConnection userConnection;
    private boolean send = true;
    /**
     * Only non-null if specifically set and gotten before packet transformation
     */
    private PacketType packetType;
    private int id;

    public PacketWrapperImpl(int packetId, @Nullable ByteBuf inputBuffer, UserConnection userConnection) {
        this.id = packetId;
        this.inputBuffer = inputBuffer;
        this.userConnection = userConnection;
    }

    public PacketWrapperImpl(@Nullable PacketType packetType, @Nullable ByteBuf inputBuffer, UserConnection userConnection) {
        this.packetType = packetType;
        this.id = packetType != null ? packetType.getId() : -1;
        this.inputBuffer = inputBuffer;
        this.userConnection = userConnection;
    }

    @Override
    public <T> T get(Type<T> type, int index) throws Exception {
        int currentIndex = 0;
        for (PacketValue<?> packetValue : packetValues) {
            if (packetValue.type() != type) {
                continue;
            }
            if (currentIndex == index) {
                //noinspection unchecked
                return (T) packetValue.value();
            }
            currentIndex++;
        }
        throw createInformativeException(new ArrayIndexOutOfBoundsException("Could not find type " + type.getTypeName() + " at " + index), type, index);
    }

    @Override
    public boolean is(Type type, int index) {
        int currentIndex = 0;
        for (PacketValue<?> packetValue : packetValues) {
            if (packetValue.type() != type) {
                continue;
            }
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
        for (PacketValue<?> packetValue : readableObjects) {
            if (packetValue.type().getBaseClass() != type.getBaseClass()) {
                continue;
            }
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
        for (PacketValue packetValue : packetValues) {
            if (packetValue.type() != type) {
                continue;
            }
            if (currentIndex == index) {
                packetValue.setValue(attemptTransform(type, value));
                return;
            }
            currentIndex++;
        }
        throw createInformativeException(new ArrayIndexOutOfBoundsException("Could not find type " + type.getTypeName() + " at " + index), type, index);
    }

    @Override
    public <T> T read(Type<T> type) throws Exception {
        if (readableObjects.isEmpty()) {
            Preconditions.checkNotNull(inputBuffer, "This packet does not have an input buffer.");
            // We could in the future log input read values, but honestly for things like bulk maps, mem waste D:
            try {
                return type.read(inputBuffer);
            } catch (Exception e) {
                throw createInformativeException(e, type, packetValues.size() + 1);
            }
        }

        PacketValue readValue = readableObjects.poll();
        Type<?> readType = readValue.type();
        if (readType == type
            || (type.getBaseClass() == readType.getBaseClass()
            && type.getOutputClass() == readType.getOutputClass())) {
            //noinspection unchecked
            return (T) readValue.value();
        } else {
            throw createInformativeException(new IOException("Unable to read type " + type.getTypeName() + ", found " + readValue.type().getTypeName()), type, readableObjects.size());
        }
    }

    @Override
    public <T> void write(Type<T> type, T value) {
        packetValues.add(new PacketValue<>(type, attemptTransform(type, value)));
    }

    /**
     * Returns the value if already matching, else the converted value or possibly unmatched value.
     *
     * @param expectedType expected type
     * @param value        value
     * @return value if already matching, else the converted value or possibly unmatched value
     */
    private <T> @Nullable T attemptTransform(Type<T> expectedType, @Nullable T value) {
        if (value != null && !expectedType.getOutputClass().isAssignableFrom(value.getClass())) {
            // Attempt conversion
            if (expectedType instanceof TypeConverter<?>) {
                //noinspection unchecked
                return ((TypeConverter<T>) expectedType).from(value);
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

        for (int i = 0; i < packetValues.size(); i++) {
            PacketValue<?> packetValue = packetValues.get(i);
            try {
                packetValue.write(buffer);
            } catch (final Exception e) {
                throw createInformativeException(e, packetValue.type(), i);
            }
        }
        writeRemaining(buffer);
    }

    private InformativeException createInformativeException(final Exception cause, final Type<?> type, final int index) {
        return new InformativeException(cause)
            .set("Index", index)
            .set("Type", type.getTypeName())
            .set("Packet ID", this.id)
            .set("Packet Type", this.packetType)
            .set("Data", this.packetValues);
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
        if (isCancelled()) {
            return;
        }

        final UserConnection connection = user();
        if (currentThread) {
            try {
                final ByteBuf output = constructPacket(protocol, skipCurrentPipeline, Direction.CLIENTBOUND);
                connection.sendRawPacket(output);
            } catch (final Exception e) {
                if (!PipelineUtil.containsCause(e, CancelException.class)) {
                    throw e;
                }
            }
            return;
        }

        connection.getChannel().eventLoop().submit(() -> {
            try {
                final ByteBuf output = constructPacket(protocol, skipCurrentPipeline, Direction.CLIENTBOUND);
                connection.sendRawPacket(output);
            } catch (final RuntimeException e) {
                if (!PipelineUtil.containsCause(e, CancelException.class)) {
                    throw e;
                }
            } catch (final Exception e) {
                if (!PipelineUtil.containsCause(e, CancelException.class)) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    /**
     * Let the packet go through the protocol pipes and write it to ByteBuf
     *
     * @param protocolClass       protocol class to send the packet from, or null to go through the full pipeline
     * @param skipCurrentPipeline whether to start from the next protocol in the pipeline, or the provided one
     * @return created packet buffer
     * @throws Exception if it fails to write
     */
    private ByteBuf constructPacket(@Nullable Class<? extends Protocol> protocolClass, boolean skipCurrentPipeline, Direction direction) throws Exception {
        resetReader(); // Reset reader before we start

        final ProtocolInfo protocolInfo = user().getProtocolInfo();
        final List<Protocol> protocols = protocolInfo.getPipeline().pipes(protocolClass, skipCurrentPipeline, direction);
        apply(direction, protocolInfo.getState(direction), protocols);
        final ByteBuf output = inputBuffer == null ? user().getChannel().alloc().buffer() : inputBuffer.alloc().buffer();
        try {
            writeToBuffer(output);
            return output.retain();
        } finally {
            output.release();
        }
    }

    @Override
    public ChannelFuture sendFuture(Class<? extends Protocol> protocolClass) throws Exception {
        if (!isCancelled()) {
            ByteBuf output = constructPacket(protocolClass, true, Direction.CLIENTBOUND);
            return user().sendRawPacketFuture(output);
        }
        return user().getChannel().newFailedFuture(new Exception("Cancelled packet"));
    }

    @Override
    public void sendRaw() throws Exception {
        sendRaw(true);
    }

    @Override
    public void scheduleSendRaw() throws Exception {
        sendRaw(false);
    }

    private void sendRaw(boolean currentThread) throws Exception {
        if (isCancelled()) {
            return;
        }

        ByteBuf output = inputBuffer == null ? user().getChannel().alloc().buffer() : inputBuffer.alloc().buffer();
        try {
            writeToBuffer(output);
            if (currentThread) {
                user().sendRawPacket(output.retain());
            } else {
                user().scheduleSendRawPacket(output.retain());
            }
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
    public void apply(Direction direction, State state, List<Protocol> pipeline) throws Exception {
        // Indexed loop to allow additions to the tail
        for (int i = 0, size = pipeline.size(); i < size; i++) {
            Protocol<?, ?, ?, ?> protocol = pipeline.get(i);
            protocol.transform(direction, state, this);
            resetReader();
            if (this.packetType != null) {
                state = this.packetType.state();
            }
        }
    }

    @Override
    @Deprecated
    public PacketWrapperImpl apply(Direction direction, State state, int index, List<Protocol> pipeline, boolean reverse) throws Exception {
        // Reset the reader after every transformation for the packetWrapper, so it can be recycled across packets
        if (reverse) {
            for (int i = index; i >= 0; i--) {
                pipeline.get(i).transform(direction, state, this);
                resetReader();
                if (this.packetType != null) {
                    state = this.packetType.state();
                }
            }
        } else {
            for (int i = index; i < pipeline.size(); i++) {
                pipeline.get(i).transform(direction, state, this);
                resetReader();
                if (this.packetType != null) {
                    state = this.packetType.state();
                }
            }
        }
        return this;
    }

    @Override
    public boolean isCancelled() {
        return !this.send;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.send = !cancel;
    }

    @Override
    public UserConnection user() {
        return this.userConnection;
    }

    @Override
    public void resetReader() {
        // Move all packet values to the readable for next Protocol
        for (int i = packetValues.size() - 1; i >= 0; i--) {
            this.readableObjects.addFirst(this.packetValues.get(i));
        }
        this.packetValues.clear();
    }

    @Override
    public void sendToServerRaw() throws Exception {
        sendToServerRaw(true);
    }

    @Override
    public void scheduleSendToServerRaw() throws Exception {
        sendToServerRaw(false);
    }

    private void sendToServerRaw(boolean currentThread) throws Exception {
        if (isCancelled()) {
            return;
        }

        ByteBuf output = inputBuffer == null ? user().getChannel().alloc().buffer() : inputBuffer.alloc().buffer();
        try {
            writeToBuffer(output);
            if (currentThread) {
                user().sendRawPacketToServer(output.retain());
            } else {
                user().scheduleSendRawPacketToServer(output.retain());
            }
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
        if (isCancelled()) {
            return;
        }

        final UserConnection connection = user();
        if (currentThread) {
            try {
                final ByteBuf output = constructPacket(protocol, skipCurrentPipeline, Direction.SERVERBOUND);
                connection.sendRawPacketToServer(output);
            } catch (final Exception e) {
                if (!PipelineUtil.containsCause(e, CancelException.class)) {
                    throw e;
                }
            }
            return;
        }

        connection.getChannel().eventLoop().submit(() -> {
            try {
                final ByteBuf output = constructPacket(protocol, skipCurrentPipeline, Direction.SERVERBOUND);
                connection.sendRawPacketToServer(output);
            } catch (final RuntimeException e) {
                if (!PipelineUtil.containsCause(e, CancelException.class)) {
                    throw e;
                }
            } catch (final Exception e) {
                if (!PipelineUtil.containsCause(e, CancelException.class)) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @Override
    public @Nullable PacketType getPacketType() {
        return packetType;
    }

    @Override
    public void setPacketType(PacketType packetType) {
        this.packetType = packetType;
        this.id = packetType != null ? packetType.getId() : -1;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    @Deprecated
    public void setId(int id) {
        // Loses packet type info
        this.packetType = null;
        this.id = id;
    }

    public @Nullable ByteBuf getInputBuffer() {
        return inputBuffer;
    }

    @Override
    public String toString() {
        return "PacketWrapper{" +
            "type=" + packetType +
            ", id=" + id +
            ", values=" + packetValues +
            ", readable=" + readableObjects +
            '}';
    }

    public static final class PacketValue<T> {
        private final Type<T> type;
        private T value;

        private PacketValue(final Type<T> type, @Nullable final T value) {
            this.type = type;
            this.value = value;
        }

        public Type<T> type() {
            return type;
        }

        public @Nullable Object value() {
            return value;
        }

        public void write(final ByteBuf buffer) throws Exception {
            type.write(buffer, value);
        }

        public void setValue(@Nullable final T value) {
            this.value = value;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final PacketValue<?> that = (PacketValue<?>) o;
            if (!type.equals(that.type)) return false;
            return Objects.equals(value, that.value);
        }

        @Override
        public int hashCode() {
            int result = type.hashCode();
            result = 31 * result + (value != null ? value.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "{" + type + ": " + value + "}";
        }
    }
}

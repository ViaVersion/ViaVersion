/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2021 ViaVersion and contributors
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
package us.myles.ViaVersion.api;

import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.protocol.PacketType;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.ValueCreator;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.api.type.TypeConverter;
import us.myles.ViaVersion.exception.CancelException;
import us.myles.ViaVersion.exception.InformativeException;
import us.myles.ViaVersion.packets.Direction;
import us.myles.ViaVersion.packets.State;
import us.myles.ViaVersion.util.PipelineUtil;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.NoSuchElementException;

public class PacketWrapper {
    public static final int PASSTHROUGH_ID = 1000;
    private static final Protocol[] PROTOCOL_ARRAY = new Protocol[0];

    private final ByteBuf inputBuffer;
    private final UserConnection userConnection;
    private boolean send = true;
    private int id = -1;
    private final Deque<Pair<Type, Object>> readableObjects = new ArrayDeque<>();
    private final List<Pair<Type, Object>> packetValues = new ArrayList<>();

    public PacketWrapper(int packetID, ByteBuf inputBuffer, UserConnection userConnection) {
        this.id = packetID;
        this.inputBuffer = inputBuffer;
        this.userConnection = userConnection;
    }

    /**
     * Get a part from the output
     *
     * @param type  The type of the part you wish to get.
     * @param <T>   The return type of the type you wish to get.
     * @param index The index of the part (relative to the type)
     * @return The requested type or throws ArrayIndexOutOfBounds
     * @throws InformativeException If it fails to find it, an exception will be thrown.
     */
    public <T> T get(Type<T> type, int index) throws Exception {
        int currentIndex = 0;
        for (Pair<Type, Object> packetValue : packetValues) {
            if (packetValue.getKey() == type) { // Ref check
                if (currentIndex == index) {
                    return (T) packetValue.getValue();
                }
                currentIndex++;
            }
        }

        Exception e = new ArrayIndexOutOfBoundsException("Could not find type " + type.getTypeName() + " at " + index);
        throw new InformativeException(e).set("Type", type.getTypeName()).set("Index", index).set("Packet ID", getId()).set("Data", packetValues);
    }

    /**
     * Check if a type is at an index
     *
     * @param type  The type of the part you wish to get.
     * @param index The index of the part (relative to the type)
     * @return True if the type is at the index
     */
    public boolean is(Type type, int index) {
        int currentIndex = 0;
        for (Pair<Type, Object> packetValue : packetValues) {
            if (packetValue.getKey() == type) { // Ref check
                if (currentIndex == index) {
                    return true;
                }
                currentIndex++;
            }
        }
        return false;
    }

    /**
     * Check if a type is at an index
     *
     * @param type  The type of the part you wish to get.
     * @param index The index of the part (relative to the type)
     * @return True if the type is at the index
     */
    public boolean isReadable(Type type, int index) {
        int currentIndex = 0;
        for (Pair<Type, Object> packetValue : readableObjects) {
            if (packetValue.getKey().getBaseClass() == type.getBaseClass()) { // Ref check
                if (currentIndex == index) {
                    return true;
                }
                currentIndex++;
            }
        }
        return false;
    }


    /**
     * Set a currently existing part in the output
     *
     * @param type  The type of the part you wish to set.
     * @param <T>   The return type of the type you wish to set.
     * @param index The index of the part (relative to the type)
     * @param value The value of the part you wish to set it to.
     * @throws InformativeException If it fails to set it, an exception will be thrown.
     */
    public <T> void set(Type<T> type, int index, T value) throws Exception {
        int currentIndex = 0;
        for (Pair<Type, Object> packetValue : packetValues) {
            if (packetValue.getKey() == type) { // Ref check
                if (currentIndex == index) {
                    packetValue.setValue(value);
                    return;
                }
                currentIndex++;
            }
        }
        Exception e = new ArrayIndexOutOfBoundsException("Could not find type " + type.getTypeName() + " at " + index);
        throw new InformativeException(e).set("Type", type.getTypeName()).set("Index", index).set("Packet ID", getId());
    }

    /**
     * Read a type from the input.
     *
     * @param type The type you wish to read
     * @param <T>  The return type of the type you wish to read.
     * @return The requested type
     * @throws InformativeException If it fails to read
     */
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
        } else {
            Pair<Type, Object> read = readableObjects.poll();
            Type rtype = read.getKey();
            if (rtype.equals(type) || (type.getBaseClass().equals(rtype.getBaseClass()) && type.getOutputClass().equals(rtype.getOutputClass()))) {
                return (T) read.getValue();
            } else {
                if (rtype == Type.NOTHING) {
                    return read(type); // retry
                } else {
                    Exception e = new IOException("Unable to read type " + type.getTypeName() + ", found " + read.getKey().getTypeName());
                    throw new InformativeException(e).set("Type", type.getTypeName()).set("Packet ID", getId()).set("Data", packetValues);
                }
            }
        }
    }

    /**
     * Write a type to the output.
     *
     * @param type  The type to write.
     * @param <T>   The return type of the type you wish to write.
     * @param value The value of the type to write.
     */
    public <T> void write(Type<T> type, T value) {
        if (value != null) {
            if (!type.getOutputClass().isAssignableFrom(value.getClass())) {
                // attempt conversion
                if (type instanceof TypeConverter) {
                    value = (T) ((TypeConverter) type).from(value);
                } else {
                    Via.getPlatform().getLogger().warning("Possible type mismatch: " + value.getClass().getName() + " -> " + type.getOutputClass());
                }
            }
        }
        packetValues.add(new Pair<Type, Object>(type, value));
    }

    /**
     * Take a value from the input and write to the output.
     *
     * @param type The type to read and write.
     * @param <T>  The return type of the type you wish to pass through.
     * @return The type which was read/written.
     * @throws Exception If it failed to read or write
     */
    public <T> T passthrough(Type<T> type) throws Exception {
        T value = read(type);
        write(type, value);
        return value;
    }

    /**
     * Take all the inputs and write them to the output.
     *
     * @throws Exception If it failed to read or write
     */
    public void passthroughAll() throws Exception {
        // Copy previous objects
        packetValues.addAll(readableObjects);
        readableObjects.clear();
        // If the buffer has readable bytes, copy them.
        if (inputBuffer.readableBytes() > 0) {
            passthrough(Type.REMAINING_BYTES);
        }
    }

    /**
     * Write the current output to a buffer.
     *
     * @param buffer The buffer to write to.
     * @throws InformativeException Throws an exception if it fails to write a value.
     */
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
                Object value = packetValue.getValue();
                if (value != null) {
                    if (!packetValue.getKey().getOutputClass().isAssignableFrom(value.getClass())) {
                        // attempt conversion
                        if (packetValue.getKey() instanceof TypeConverter) {
                            value = ((TypeConverter) packetValue.getKey()).from(value);
                        } else {
                            Via.getPlatform().getLogger().warning("Possible type mismatch: " + value.getClass().getName() + " -> " + packetValue.getKey().getOutputClass());
                        }
                    }
                }
                packetValue.getKey().write(buffer, value);
            } catch (Exception e) {
                throw new InformativeException(e).set("Index", index).set("Type", packetValue.getKey().getTypeName()).set("Packet ID", getId()).set("Data", packetValues);
            }
            index++;
        }
        writeRemaining(buffer);
    }

    /**
     * Clear the input buffer / readable objects
     */
    public void clearInputBuffer() {
        if (inputBuffer != null) {
            inputBuffer.clear();
        }
        readableObjects.clear(); // :(
    }

    /**
     * Clear the packet, used if you have to change the packet completely
     */
    public void clearPacket() {
        clearInputBuffer();
        packetValues.clear();
    }

    private void writeRemaining(ByteBuf output) {
        if (inputBuffer != null) {
            output.writeBytes(inputBuffer, inputBuffer.readableBytes());
        }
    }

    /**
     * Send this packet to the associated user.
     * Be careful not to send packets twice.
     * (Sends it after current)
     *
     * @param packetProtocol      The protocol version of the packet.
     * @param skipCurrentPipeline Skip the current pipeline
     * @throws Exception if it fails to write
     */
    public void send(Class<? extends Protocol> packetProtocol, boolean skipCurrentPipeline) throws Exception {
        send(packetProtocol, skipCurrentPipeline, false);
    }

    /**
     * Send this packet to the associated user.
     * Be careful not to send packets twice.
     * (Sends it after current)
     *
     * @param packetProtocol      The protocol version of the packet.
     * @param skipCurrentPipeline Skip the current pipeline
     * @param currentThread       Run in the same thread
     * @throws Exception if it fails to write
     */
    public void send(Class<? extends Protocol> packetProtocol, boolean skipCurrentPipeline, boolean currentThread) throws Exception {
        if (!isCancelled()) {
            try {
                ByteBuf output = constructPacket(packetProtocol, skipCurrentPipeline, Direction.OUTGOING);
                user().sendRawPacket(output, currentThread);
            } catch (Exception e) {
                if (!PipelineUtil.containsCause(e, CancelException.class)) {
                    throw e;
                }
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
        boolean reverse = direction == Direction.OUTGOING;
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
        writeToBuffer(output);
        return output;
    }

    /**
     * Send this packet to the associated user.
     * Be careful not to send packets twice.
     * (Sends it after current)
     *
     * @param packetProtocol The protocol version of the packet.
     * @throws Exception if it fails to write
     */
    public void send(Class<? extends Protocol> packetProtocol) throws Exception {
        send(packetProtocol, true);
    }

    /**
     * Send this packet to the associated user.
     * Be careful not to send packets twice.
     * (Sends it after current)
     * Also returns the packets ChannelFuture
     *
     * @param packetProtocol The protocol version of the packet.
     * @return The packets ChannelFuture
     * @throws Exception if it fails to write
     */
    public ChannelFuture sendFuture(Class<? extends Protocol> packetProtocol) throws Exception {
        if (!isCancelled()) {
            ByteBuf output = constructPacket(packetProtocol, true, Direction.OUTGOING);
            return user().sendRawPacketFuture(output);
        }
        return user().getChannel().newFailedFuture(new Exception("Cancelled packet"));
    }

    /**
     * Send this packet to the associated user.
     * Be careful not to send packets twice.
     * (Sends it after current)
     * <b>This method is no longer used, it's favoured to use {@link #send(Class)} as it will handle the pipeline properly.</b>
     *
     * @throws Exception if it fails to write
     */
    @Deprecated
    public void send() throws Exception {
        if (!isCancelled()) {
            // Send
            ByteBuf output = inputBuffer == null ? user().getChannel().alloc().buffer() : inputBuffer.alloc().buffer();
            writeToBuffer(output);
            user().sendRawPacket(output);
        }
    }

    /**
     * Create a new packet for the target of this packet.
     *
     * @param packetType packet type of the new packedt
     * @return The newly created packet wrapper
     */
    public PacketWrapper create(PacketType packetType) {
        return new PacketWrapper(packetType.ordinal(), null, user());
    }

    /**
     * Create a new packet for the target of this packet.
     *
     * @param packetID The ID of the new packet
     * @return The newly created packet wrapper
     */
    public PacketWrapper create(int packetID) {
        return new PacketWrapper(packetID, null, user());
    }

    /**
     * Create a new packet with values.
     *
     * @param packetID The ID of the new packet
     * @param init     A ValueCreator to write to the packet.
     * @return The newly created packet wrapper
     * @throws Exception If it failed to write the values from the ValueCreator.
     */
    public PacketWrapper create(int packetID, ValueCreator init) throws Exception {
        PacketWrapper wrapper = create(packetID);
        init.write(wrapper);
        return wrapper;
    }

    /**
     * Applies a pipeline from an index to the wrapper.
     *
     * @param direction protocol direction
     * @param state     protocol state
     * @param index     index to start from, will be reversed depending on the reverse parameter
     * @param pipeline  protocol pipeline
     * @param reverse   whether the array should be looped in reverse, will also reverse the given index
     * @return The current packetwrapper
     * @throws Exception If it fails to transform a packet, exception will be thrown
     */
    public PacketWrapper apply(Direction direction, State state, int index, List<Protocol> pipeline, boolean reverse) throws Exception {
        Protocol[] array = pipeline.toArray(PROTOCOL_ARRAY);
        return apply(direction, state, reverse ? array.length - 1 : index, array, reverse); // Copy to prevent from removal
    }

    /**
     * @see #apply(Direction, State, int, List, boolean)
     */
    public PacketWrapper apply(Direction direction, State state, int index, List<Protocol> pipeline) throws Exception {
        return apply(direction, state, index, pipeline.toArray(PROTOCOL_ARRAY), false);
    }

    private PacketWrapper apply(Direction direction, State state, int index, Protocol[] pipeline, boolean reverse) throws Exception {
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

    /**
     * Cancel this packet from sending
     */
    public void cancel() {
        this.send = false;
    }

    /**
     * Check if this packet is cancelled.
     *
     * @return True if the packet won't be sent.
     */
    public boolean isCancelled() {
        return !this.send;
    }

    /**
     * Get the user associated with this Packet
     *
     * @return The user
     */
    public UserConnection user() {
        return this.userConnection;
    }

    /**
     * Reset the reader, so that it can be read again.
     */
    public void resetReader() {
        // Move all packet values to the readable for next packet.
        for (int i = packetValues.size() - 1; i >= 0; i--) {
            this.readableObjects.addFirst(this.packetValues.get(i));
        }
        this.packetValues.clear();
    }

    /**
     * Send the current packet to the server.
     * (Ensure the ID is suitable for viaversion)
     *
     * @throws Exception If it failed to write
     */
    @Deprecated
    public void sendToServer() throws Exception {
        if (!isCancelled()) {
            ByteBuf output = inputBuffer == null ? user().getChannel().alloc().buffer() : inputBuffer.alloc().buffer();
            writeToBuffer(output);

            user().sendRawPacketToServer(output, true);
        }
    }

    /**
     * Send this packet to the server.
     *
     * @param packetProtocol      The protocol version of the packet.
     * @param skipCurrentPipeline Skip the current pipeline
     * @param currentThread       Run in the same thread
     * @throws Exception if it fails to write
     */
    public void sendToServer(Class<? extends Protocol> packetProtocol, boolean skipCurrentPipeline, boolean currentThread) throws Exception {
        if (!isCancelled()) {
            try {
                ByteBuf output = constructPacket(packetProtocol, skipCurrentPipeline, Direction.INCOMING);
                user().sendRawPacketToServer(output, currentThread);
            } catch (Exception e) {
                if (!PipelineUtil.containsCause(e, CancelException.class)) {
                    throw e;
                }
            }
        }
    }

    public void sendToServer(Class<? extends Protocol> packetProtocol, boolean skipCurrentPipeline) throws Exception {
        sendToServer(packetProtocol, skipCurrentPipeline, false);
    }

    public void sendToServer(Class<? extends Protocol> packetProtocol) throws Exception {
        sendToServer(packetProtocol, true);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

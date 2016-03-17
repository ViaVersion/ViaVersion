package us.myles.ViaVersion2.api;

import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Getter;
import lombok.Setter;
import us.myles.ViaVersion2.api.data.UserConnection;
import us.myles.ViaVersion2.api.remapper.ValueCreator;
import us.myles.ViaVersion2.api.type.Type;
import us.myles.ViaVersion2.api.type.TypeConverter;
import us.myles.ViaVersion2.api.util.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class PacketWrapper {
    private final ByteBuf inputBuffer;
    private final UserConnection userConnection;
    private boolean send = true;
    @Setter
    @Getter
    private int id = -1;
    private LinkedList<Pair<Type, Object>> readableObjects = new LinkedList<>();
    private List<Pair<Type, Object>> packetValues = new ArrayList<>();

    public PacketWrapper(int packetID, ByteBuf inputBuffer, UserConnection userConnection) {
        this.id = packetID;
        this.inputBuffer = inputBuffer;
        this.userConnection = userConnection;
    }

    public <T> T get(Type<T> type, int index) {
        int currentIndex = 0;
        for (Pair<Type, Object> packetValue : packetValues) {
            if (packetValue.getKey() == type) { // Ref check
                if (currentIndex == index) {
                    return (T) packetValue.getValue();
                }
                currentIndex++;
            }
        }
        throw new ArrayIndexOutOfBoundsException("Could not find type " + type.getTypeName() + " at " + index);
    }

    public <T> void set(Type<T> type, int index, T value) {
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
        throw new ArrayIndexOutOfBoundsException("Could not find type " + type.getTypeName() + " at " + index);
    }

    public <T> T read(Type<T> type) throws Exception {
        if (readableObjects.isEmpty()) {
            Preconditions.checkNotNull(inputBuffer, "This packet does not have an input buffer.");
            // We could in the future log input read values, but honestly for things like bulk maps, mem waste D:
            return type.read(inputBuffer);
        } else {
            Pair<Type, Object> read = readableObjects.poll();
            if (read.getKey().equals(type)) {
                return (T) read.getValue();
            } else {
                throw new IOException("Unable to read type " + type.getTypeName() + ", found " + type.getTypeName());
            }
        }
    }


    public <T> void write(Type<T> type, T value) {
        packetValues.add(new Pair<Type, Object>(type, value));
    }

    public <T> T passthrough(Type<T> type) throws Exception {
        T value = read(type);
        write(type, value);
        return value;
    }

    public void writeToBuffer(ByteBuf buffer) throws Exception {
        if (id != -1) {
            Type.VAR_INT.write(buffer, id);
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
                            System.out.println("Possible type mismatch: " + value.getClass().getName() + " -> " + packetValue.getKey().getOutputClass());
                        }
                    }
                }
                packetValue.getKey().write(buffer, value);
            } catch (Exception e) {
                System.out.println(getId() + " Index: " + index + " Type: " + packetValue.getKey().getTypeName());
                throw e;
            }
            index++;
        }
        writeRemaining(buffer);
    }

    public void clearInputBuffer() {
        if (inputBuffer != null)
            inputBuffer.clear();
    }

    private void writeRemaining(ByteBuf output) {
        if (inputBuffer != null) {
            output.writeBytes(inputBuffer);
        }
    }

    public void send() throws Exception {
        ByteBuf output = inputBuffer == null ? Unpooled.buffer() : inputBuffer.alloc().buffer();
        writeToBuffer(output);
        user().sendRawPacket(output);
    }

    public PacketWrapper create(int packetID) throws Exception {
        return new PacketWrapper(packetID, null, user());
    }

    public PacketWrapper create(int packetID, ValueCreator init) throws Exception {
        PacketWrapper wrapper = create(packetID);
        init.write(wrapper);
        return wrapper;
    }

    public void cancel() {
        this.send = false;
    }

    public boolean isCancelled() {
        return !this.send;
    }

    public UserConnection user() {
        return this.userConnection;
    }

    public void resetReader() {
        this.readableObjects.addAll(packetValues);
    }
}

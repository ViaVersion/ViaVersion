package us.myles.ViaVersion2.api;

import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;
import us.myles.ViaVersion2.api.data.UserConnection;
import us.myles.ViaVersion2.api.remapper.ValueCreator;
import us.myles.ViaVersion2.api.type.Type;
import us.myles.ViaVersion2.api.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class PacketWrapper {
    private final ByteBuf inputBuffer;
    private final UserConnection userConnection;
    private boolean send = true;
    private List<Pair<Type, Object>> packetValues = new ArrayList<>();

    public PacketWrapper(ByteBuf inputBuffer, UserConnection userConnection) {
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
        Preconditions.checkNotNull(inputBuffer, "This packet does not have an input buffer.");
        System.out.println("Reading: " + type.getTypeName());
        // We could in the future log input read values, but honestly for things like bulk maps, mem waste D:
        return type.read(inputBuffer);
    }


    public <T> void write(Type<T> type, T value) {
        System.out.println("Writing " + type.getTypeName() + " - " + value);
        packetValues.add(new Pair<Type, Object>(type, value));
    }

    public <T> T passthrough(Type<T> type) throws Exception {
        T value = read(type);
        write(type, value);
        return value;
    }

    public void writeToBuffer(ByteBuf buffer) throws Exception {
        for (Pair<Type, Object> packetValue : packetValues) {
            packetValue.getKey().write(buffer, packetValue.getValue());
        }
    }

    public void writeRemaining(ByteBuf output) {
        if (inputBuffer != null) {
            System.out.println("Writing remaining: " + output.readableBytes());
            output.writeBytes(inputBuffer);
        }
    }

    public void send() throws Exception {
        ByteBuf output = inputBuffer.alloc().buffer();
        writeToBuffer(output);
        writeRemaining(output);

        user().sendRawPacket(output);
    }

    public PacketWrapper create() throws Exception {
        return new PacketWrapper(null, user());
    }

    public PacketWrapper create(ValueCreator init) throws Exception {
        PacketWrapper wrapper = create();
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
}

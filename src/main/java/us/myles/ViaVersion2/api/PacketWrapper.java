package us.myles.ViaVersion2.api;

import io.netty.buffer.ByteBuf;
import us.myles.ViaVersion2.api.data.UserConnection;
import us.myles.ViaVersion2.api.type.Type;
import us.myles.ViaVersion2.api.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class PacketWrapper {
    private final ByteBuf inputBuffer;
    private final UserConnection userConnection;
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

    public <T> T read(Type<T> type) {
        System.out.println("Reading: " + type.getTypeName());
        // We could in the future log input read values, but honestly for things like bulk maps, mem waste D:
        return type.read(inputBuffer);
    }


    public <T> void write(Type<T> type, T value) {
        System.out.println("Writing " + type.getTypeName() + " - " + value);
        packetValues.add(new Pair<Type, Object>(type, value));
    }

    public void writeToBuffer(ByteBuf buffer) {
        for (Pair<Type, Object> packetValue : packetValues) {
            packetValue.getKey().write(buffer, packetValue.getValue());
        }
    }

    public void writeRemaining(ByteBuf output) {
        output.writeBytes(inputBuffer);
    }

    public UserConnection user() {
        return this.userConnection;
    }
}

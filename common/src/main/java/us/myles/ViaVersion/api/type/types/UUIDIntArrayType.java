package us.myles.ViaVersion.api.type.types;

import io.netty.buffer.ByteBuf;
import us.myles.ViaVersion.api.type.Type;

import java.util.UUID;

public class UUIDIntArrayType extends Type<UUID> {

    public UUIDIntArrayType() {
        super(UUID.class);
    }

    @Override
    public UUID read(ByteBuf buffer) {
        int[] ints = new int[4];
        for (int i = 0; i < 4; ++i) {
            ints[i] = buffer.readInt();
        }
        return uuidFromIntArray(ints);
    }

    @Override
    public void write(ByteBuf buffer, UUID object) {
        int[] ints = uuidToIntArray(object);
        for (int i = 0; i < 4; i++) {
            buffer.writeInt(ints[i]);
        }
    }

    public static UUID uuidFromIntArray(int[] ints) {
        return new UUID((long) ints[0] << 32 | ((long) ints[1] & 0xFFFFFFFFL), (long) ints[2] << 32 | ((long) ints[3] & 0xFFFFFFFFL));
    }

    public static int[] uuidToIntArray(UUID uuid) {
        return bitsToIntArray(uuid.getMostSignificantBits(), uuid.getLeastSignificantBits());
    }

    public static int[] bitsToIntArray(long long1, long long2) {
        return new int[]{(int) (long1 >> 32), (int) long1, (int) (long2 >> 32), (int) long2};
    }
}

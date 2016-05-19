package us.myles.ViaVersion.protocols.protocol1_9to1_8.metadata;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import us.myles.ViaVersion.api.type.Type;

@RequiredArgsConstructor
@Getter
public enum NewType {
    Byte(0, Type.BYTE),
    VarInt(1, Type.VAR_INT),
    Float(2, Type.FLOAT),
    String(3, Type.STRING),
    Chat(4, Type.STRING),
    Slot(5, Type.ITEM),
    Boolean(6, Type.BOOLEAN),
    Vector3F(7, Type.ROTATION),
    Position(8, Type.POSITION),
    OptPosition(9, Type.OPTIONAL_POSITION),
    Direction(10, Type.VAR_INT),
    OptUUID(11, Type.OPTIONAL_UUID),
    BlockID(12, Type.VAR_INT),
    Discontinued(99, null);

    private final int typeID;
    private final Type type;

    public static NewType byId(int id) {
        return values()[id];
    }

}

package us.myles.ViaVersion.protocols.protocol1_9to1_8.metadata;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import us.myles.ViaVersion.api.type.Type;

@RequiredArgsConstructor
@Getter
public enum MetadataTypes {
    Byte(0, Type.BYTE),
    Short(1, Type.SHORT),
    Int(2, Type.INT),
    Float(3, Type.FLOAT),
    String(4, Type.STRING),
    Slot(5, Type.ITEM),
    Position(6, Type.VECTOR),
    Rotation(7, Type.ROTATION),
    NonExistent(-1, Type.NOTHING);

    private final int typeID;
    private final Type type;

    public static MetadataTypes byId(int id) {
        return values()[id];
    }
}

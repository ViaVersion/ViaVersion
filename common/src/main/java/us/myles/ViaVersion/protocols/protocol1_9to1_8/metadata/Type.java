package us.myles.ViaVersion.protocols.protocol1_9to1_8.metadata;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Type {
    Byte(0),
    Short(1),
    Int(2),
    Float(3),
    String(4),
    Slot(5),
    Position(6),
    Rotation(7),
    NonExistent(-1);
    private final int typeID;

    public static Type byId(int id) {
        return values()[id];
    }
}

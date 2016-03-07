package us.myles.ViaVersion.metadata;

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
    Rotation(7);
    private final int typeID;

    public static Type byId(int id) {
        return values()[id];
    }
}

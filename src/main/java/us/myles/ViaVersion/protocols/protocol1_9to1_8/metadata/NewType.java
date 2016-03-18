package us.myles.ViaVersion.protocols.protocol1_9to1_8.metadata;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum NewType {
    Byte(0),
    VarInt(1),
    Float(2),
    String(3),
    Chat(4),
    Slot(5),
    Boolean(6),
    Vector3F(7),
    Position(8),
    OptPosition(9),
    Direction(10),
    OptUUID(11),
    BlockID(12),
    Discontinued(99);

    private final int typeID;

}

package us.myles.ViaVersion.api.minecraft.metadata.types;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import us.myles.ViaVersion.api.minecraft.metadata.MetaType;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.api.type.types.version.Types1_13;

@RequiredArgsConstructor
@Getter
public enum MetaType1_13 implements MetaType {
    Byte(0, Type.BYTE),
    VarInt(1, Type.VAR_INT),
    Float(2, Type.FLOAT),
    String(3, Type.STRING),
    Chat(4, Type.STRING),
    OptChat(5, Type.OPTIONAL_CHAT),
    Slot(6, Type.FLAT_ITEM),
    Boolean(7, Type.BOOLEAN),
    Vector3F(8, Type.ROTATION),
    Position(9, Type.POSITION),
    OptPosition(10, Type.OPTIONAL_POSITION),
    Direction(11, Type.VAR_INT),
    OptUUID(12, Type.OPTIONAL_UUID),
    BlockID(13, Type.VAR_INT),
    NBTTag(14, Type.NBT),
    PARTICLE(15, Types1_13.PARTICLE),
    Discontinued(99, null);

    private final int typeID;
    private final Type type;

    public static MetaType1_13 byId(int id) {
        return values()[id];
    }

}

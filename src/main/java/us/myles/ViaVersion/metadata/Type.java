package us.myles.ViaVersion.metadata;

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

    Type(int typeID){
        this.typeID = typeID;
    }

    public int getTypeID() {
        return typeID;
    }
}

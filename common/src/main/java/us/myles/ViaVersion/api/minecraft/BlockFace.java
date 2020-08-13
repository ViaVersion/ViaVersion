package us.myles.ViaVersion.api.minecraft;

import java.util.HashMap;
import java.util.Map;

public enum BlockFace {
    NORTH((byte) 0, (byte) 0, (byte) -1, EnumAxis.Z),
    SOUTH((byte) 0, (byte) 0, (byte) 1, EnumAxis.Z),
    EAST((byte) 1, (byte) 0, (byte) 0, EnumAxis.X),
    WEST((byte) -1, (byte) 0, (byte) 0, EnumAxis.X),
    TOP((byte) 0, (byte) 1, (byte) 0, EnumAxis.Y),
    BOTTOM((byte) 0, (byte) -1, (byte) 0, EnumAxis.Y);

    private static final Map<BlockFace, BlockFace> opposites = new HashMap<>();

    static {
        opposites.put(BlockFace.NORTH, BlockFace.SOUTH);
        opposites.put(BlockFace.SOUTH, BlockFace.NORTH);
        opposites.put(BlockFace.EAST, BlockFace.WEST);
        opposites.put(BlockFace.WEST, BlockFace.EAST);
        opposites.put(BlockFace.TOP, BlockFace.BOTTOM);
        opposites.put(BlockFace.BOTTOM, BlockFace.TOP);
    }

    private final byte modX;
    private final byte modY;
    private final byte modZ;
    private final EnumAxis axis;

    BlockFace(byte modX, byte modY, byte modZ, EnumAxis axis) {
        this.modX = modX;
        this.modY = modY;
        this.modZ = modZ;
        this.axis = axis;
    }

    public BlockFace opposite() {
        return opposites.get(this);
    }

    public byte getModX() {
        return modX;
    }

    public byte getModY() {
        return modY;
    }

    public byte getModZ() {
        return modZ;
    }

    public EnumAxis getAxis() {
        return axis;
    }

    public enum EnumAxis {
        X, Y, Z
    }
}

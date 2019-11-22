package us.myles.ViaVersion.api.minecraft;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
@AllArgsConstructor
public enum BlockFace {
    NORTH((byte) 0, (byte) 0, (byte) -1, EnumAxis.Z),
    SOUTH((byte) 0, (byte) 0, (byte) 1, EnumAxis.Z),
    EAST((byte) 1, (byte) 0, (byte) 0, EnumAxis.X),
    WEST((byte) -1, (byte) 0, (byte) 0, EnumAxis.X),
    TOP((byte) 0, (byte) 1, (byte) 0, EnumAxis.Y),
    BOTTOM((byte) 0, (byte) -1, (byte) 0, EnumAxis.Y);

    private static Map<BlockFace, BlockFace> opposites = new HashMap<>();

    static {
        opposites.put(BlockFace.NORTH, BlockFace.SOUTH);
        opposites.put(BlockFace.SOUTH, BlockFace.NORTH);
        opposites.put(BlockFace.EAST, BlockFace.WEST);
        opposites.put(BlockFace.WEST, BlockFace.EAST);
        opposites.put(BlockFace.TOP, BlockFace.BOTTOM);
        opposites.put(BlockFace.BOTTOM, BlockFace.TOP);
    }

    private byte modX, modY, modZ;
    private EnumAxis axis;

    public BlockFace opposite() {
        return opposites.get(this);
    }

    public enum EnumAxis {
        X, Y, Z
    }
}

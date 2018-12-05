package us.myles.ViaVersion.api.minecraft;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
@AllArgsConstructor
public enum BlockFace {
    NORTH(0, 0, -1, EnumAxis.Z), SOUTH(0, 0, 1, EnumAxis.Z), EAST(1, 0, 0, EnumAxis.X), WEST(-1, 0, 0, EnumAxis.X), TOP(0, 1, 0, EnumAxis.Y), BOTTOM(0, -1, 0, EnumAxis.Y);

    private static Map<BlockFace, BlockFace> opposites = new HashMap<>();

    static {
        opposites.put(BlockFace.NORTH, BlockFace.SOUTH);
        opposites.put(BlockFace.SOUTH, BlockFace.NORTH);
        opposites.put(BlockFace.EAST, BlockFace.WEST);
        opposites.put(BlockFace.WEST, BlockFace.EAST);
        opposites.put(BlockFace.TOP, BlockFace.BOTTOM);
        opposites.put(BlockFace.BOTTOM, BlockFace.TOP);
    }

    private int modX, modY, modZ;
    private EnumAxis axis;

    public BlockFace opposite() {
        return opposites.get(this);
    }

    public enum EnumAxis {
        X, Y, Z;
    }
}

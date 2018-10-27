package us.myles.ViaVersion.api.minecraft;

import java.util.HashMap;
import java.util.Map;

public enum BlockFace {
	NORTH(0, 0, -1), SOUTH(0, 0, 1), EAST(1, 0, 0), WEST(-1, 0, 0), TOP(0, 1, 0), BOTTOM(0, -1, 0);

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

	BlockFace(int modX, int modY, int modZ) {
		this.modX = modX;
		this.modY = modY;
		this.modZ = modZ;
	}

	public int getModX() {
		return modX;
	}

	public int getModY() {
		return modY;
	}

	public int getModZ() {
		return modZ;
	}

	public BlockFace opposite() {
		return opposites.get(this);
	}
}

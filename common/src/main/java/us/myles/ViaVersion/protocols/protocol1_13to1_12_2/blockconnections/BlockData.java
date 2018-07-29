package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.blockconnections;

import us.myles.ViaVersion.api.minecraft.BlockFace;

public class BlockData {
	//TODO store in a map?
	private boolean[] connectsToFences = new boolean[6];
	private boolean[] connectsToNetherFences = new boolean[6];
	private boolean[] connectsToPanes = new boolean[6];
	private boolean[] connectsToWalls = new boolean[6];

	public boolean connectsToFences(BlockFace face) {
		return connectsToFences[face.ordinal()];
	}
	
	public void setConnectsToFences(BlockFace face, boolean connects) {
		connectsToFences[face.ordinal()] = connects;
	}
	
	public boolean connectsToPanes(BlockFace face) {
		return connectsToPanes[face.ordinal()];
	}
	
	public void setConnectsToPanes(BlockFace face, boolean connects) {
		connectsToPanes[face.ordinal()] = connects;
	}
}

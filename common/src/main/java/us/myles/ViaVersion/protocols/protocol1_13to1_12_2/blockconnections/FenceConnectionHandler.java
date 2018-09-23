package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.blockconnections;

import us.myles.ViaVersion.api.minecraft.BlockFace;
import us.myles.ViaVersion.api.minecraft.Position;

import java.util.*;

class FenceConnectionHandler implements ConnectionHandler {
	private static HashSet<String> baseFences = new HashSet<>();
	private static Map<Integer, String> fences = new HashMap<>();
	
	private static final List<BlockFace> blockFaceList = Arrays.asList(BlockFace.EAST, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.WEST);

	static void init() {
		baseFences.add("minecraft:oak_fence");
		baseFences.add("minecraft:birch_fence");
		baseFences.add("minecraft:jungle_fence");
		baseFences.add("minecraft:dark_oak_fence");
		baseFences.add("minecraft:acacia_fence");
		baseFences.add("minecraft:spruce_fence");
		baseFences.add("minecraft:cobblestone_wall");
		baseFences.add("minecraft:mossy_cobblestone_wall");

		FenceConnectionHandler connectionHandler = new FenceConnectionHandler();
		for (Map.Entry<String, Integer> blockState : ConnectionData.keyToId.entrySet()) {
			String key = blockState.getKey().split("\\[")[0];
			if (baseFences.contains(key)) {
				fences.put(blockState.getValue(), key);
				ConnectionData.connectionHandlerMap.put(blockState.getValue(), connectionHandler);
			}
		}
	}

	@Override
	public int connect(Position position, int blockState, ConnectionData connectionData) {
	    String key = fences.get(blockState);
	    String extraData = "";
	    if(key.endsWith("_wall")){
	        extraData = "up=" + up(position, connectionData) + ",";
        }
		String data = key + '[' +
				             "east=" + connects(BlockFace.EAST, connectionData.get(position.getRelative(BlockFace.EAST))) + ',' +
				             "north=" + connects(BlockFace.NORTH, connectionData.get(position.getRelative(BlockFace.NORTH))) + ',' +
				             "south=" + connects(BlockFace.SOUTH, connectionData.get(position.getRelative(BlockFace.SOUTH))) + ',' +
                             extraData +
				             "waterlogged=false," +
				             "west=" + connects(BlockFace.WEST, connectionData.get(position.getRelative(BlockFace.WEST))) +
				             ']';
		return ConnectionData.getId(data);
	}

	private boolean connects(BlockFace side, int blockState) {
		return fences.containsKey(blockState) || ConnectionData.blockConnectionData.containsKey(blockState) && ConnectionData.blockConnectionData.get(blockState).connectsToFences(side.opposite());
	}

	public boolean up(Position position, ConnectionData data){
	    if(isWall(data.get(position.getRelative(BlockFace.BOTTOM))) || isWall(data.get(position.getRelative(BlockFace.TOP))))return true;
	    List<BlockFace> list = getBlockFaces(position, data);
	    if(list.isEmpty()) return true;
	    if(list.size() >= 4) return true;
        for (BlockFace blockFace : list) {
            if(!list.contains(blockFace.opposite())){
                return true;
            }
        }
        return false;
    }

    private List<BlockFace> getBlockFaces(Position position, ConnectionData data){
	    List<BlockFace> blockFaces = new ArrayList<>();
        for (BlockFace face : blockFaceList) {
            if(isWall(data.get(position.getRelative(face)))){
                blockFaces.add(face);
            }
        }
        return blockFaces;
    }

    private boolean isWall(int id){
	    if(fences.containsKey(id)) return fences.get(id).endsWith("_wall");
	    return false;
    }

}

package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.blockconnections;

import us.myles.ViaVersion.api.minecraft.BlockFace;
import us.myles.ViaVersion.api.minecraft.Position;

import java.util.ArrayList;
import java.util.List;

public class WallConnectionHandler extends AbstractFenceConnectionHandler{

    private static final BlockFace[] blockFaceList = { BlockFace.EAST, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.WEST };

    static void init() {
        List<String> baseFences = new ArrayList<>();
        baseFences.add("minecraft:cobblestone_wall");
        baseFences.add("minecraft:mossy_cobblestone_wall");

        new WallConnectionHandler("cobbleWallConnections", baseFences);
    }


    public WallConnectionHandler(String blockConnections, List<String> keyList) {
        super(blockConnections, keyList);
    }

    @Override
    public void onConnect(Position position, int blockState, ConnectionData connectionData, WrappedBlockdata blockdata) {
        blockdata.set("up", up(position, connectionData));
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
        return getBlockStates().contains(id);
    }
}

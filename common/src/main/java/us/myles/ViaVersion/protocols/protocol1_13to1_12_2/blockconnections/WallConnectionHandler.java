package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.blockconnections;

import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.minecraft.BlockFace;
import us.myles.ViaVersion.api.minecraft.Position;

import java.util.ArrayList;
import java.util.List;

public class WallConnectionHandler extends AbstractFenceConnectionHandler{

    private static final BlockFace[] blockFaceList = { BlockFace.EAST, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.WEST };

    static void init() {
        new WallConnectionHandler("cobbleWallConnections", "minecraft:cobblestone_wall");
        new WallConnectionHandler("cobbleWallConnections", "minecraft:mossy_cobblestone_wall");
    }


    public WallConnectionHandler(String blockConnections, String key) {
        super(blockConnections, key);
    }

    @Override
    protected Byte getStates(WrappedBlockData blockData) {
        byte states = super.getStates(blockData);
        if (blockData.getValue("up").equals("true")) states |= 32;
        return states;
    }

    protected Byte getStates(UserConnection user, Position position, int blockState) {
        byte states = super.getStates(user, position, blockState);
        if (up(user, position)) states |= 32;
        return states;
    }

    public boolean up(UserConnection user, Position position){
        if(isWall(getBlockData(user, position.getRelative(BlockFace.BOTTOM))) || isWall(getBlockData(user, position.getRelative(BlockFace.TOP))))return true;
        List<BlockFace> list = getBlockFaces(user, position);
        if(list.isEmpty()) return true;
        if(list.size() >= 4) return true;
        for (BlockFace blockFace : list) {
            if(!list.contains(blockFace.opposite())){
                return true;
            }
        }
        return false;
    }

    private List<BlockFace> getBlockFaces(UserConnection user, Position position){
        List<BlockFace> blockFaces = new ArrayList<>();
        for (BlockFace face : blockFaceList) {
            if(isWall(getBlockData(user, position.getRelative(face)))){
                blockFaces.add(face);
            }
        }
        return blockFaces;
    }

    private boolean isWall(int id){
        return getBlockStates().contains(id);
    }
}

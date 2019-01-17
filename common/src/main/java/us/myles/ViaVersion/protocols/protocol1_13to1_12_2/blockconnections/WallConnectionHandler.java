package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.blockconnections;

import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.minecraft.BlockFace;
import us.myles.ViaVersion.api.minecraft.Position;

public class WallConnectionHandler extends AbstractFenceConnectionHandler {
    private static final BlockFace[] BLOCK_FACES = {BlockFace.EAST, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.WEST};
    private static final int[] OPPOSITES = {3, 2, 1, 0};

    static void init() {
        new WallConnectionHandler("cobbleWallConnections", "minecraft:cobblestone_wall");
        new WallConnectionHandler("cobbleWallConnections", "minecraft:mossy_cobblestone_wall");
    }


    public WallConnectionHandler(String blockConnections, String key) {
        super(blockConnections, key);
    }

    @Override
    protected byte getStates(WrappedBlockData blockData) {
        byte states = super.getStates(blockData);
        if (blockData.getValue("up").equals("true")) states |= 16;
        return states;
    }

    protected byte getStates(UserConnection user, Position position, int blockState) {
        byte states = super.getStates(user, position, blockState);
        if (up(user, position)) states |= 16;
        return states;
    }

    public boolean up(UserConnection user, Position position) {
        if (isWall(getBlockData(user, position.getRelative(BlockFace.BOTTOM))) || isWall(getBlockData(user, position.getRelative(BlockFace.TOP))))
            return true;
        int blockFaces = getBlockFaces(user, position);
        if (blockFaces == 0 || blockFaces == 0xF) return true;
        for (int i = 0; i < BLOCK_FACES.length; i++) {
            if ((blockFaces & (1 << i)) != 0 && (blockFaces & (1 << OPPOSITES[i])) == 0) return true;
        }
        return false;
    }

    private int getBlockFaces(UserConnection user, Position position) {
        int blockFaces = 0;
        for (int i = 0; i < BLOCK_FACES.length; i++) {
            if (isWall(getBlockData(user, position.getRelative(BLOCK_FACES[i])))) {
                blockFaces |= 1 << i;
            }
        }
        return blockFaces;
    }

    private boolean isWall(int id) {
        return getBlockStates().contains(id);
    }
}

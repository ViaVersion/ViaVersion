package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.blockconnections;

import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.minecraft.BlockFace;
import us.myles.ViaVersion.api.minecraft.Position;

import java.util.ArrayList;
import java.util.List;

public class WallConnectionHandler extends AbstractFenceConnectionHandler {
    private static final BlockFace[] BLOCK_FACES = {BlockFace.EAST, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.WEST};
    private static final int[] OPPOSITES = {3, 2, 1, 0};

    static List<ConnectionData.ConnectorInitAction> init() {
        List<ConnectionData.ConnectorInitAction> actions = new ArrayList<>(2);
        actions.add(new WallConnectionHandler("cobbleWallConnections").getInitAction("minecraft:cobblestone_wall"));
        actions.add(new WallConnectionHandler("cobbleWallConnections").getInitAction("minecraft:mossy_cobblestone_wall"));
        return actions;
    }


    public WallConnectionHandler(String blockConnections) {
        super(blockConnections);
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

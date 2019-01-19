package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.blockconnections;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.minecraft.BlockFace;
import us.myles.ViaVersion.api.minecraft.Position;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DoorConnectionHandler extends ConnectionHandler {
    private static Map<Integer, DoorData> doorDataMap = new HashMap<>();
    private static Map<Short, Integer> connectedStates = new HashMap<>();

    static ConnectionData.ConnectorInitAction init() {
        final List<String> baseDoors = new LinkedList<>();
        baseDoors.add("minecraft:oak_door");
        baseDoors.add("minecraft:birch_door");
        baseDoors.add("minecraft:jungle_door");
        baseDoors.add("minecraft:dark_oak_door");
        baseDoors.add("minecraft:acacia_door");
        baseDoors.add("minecraft:spruce_door");
        baseDoors.add("minecraft:iron_door");

        final DoorConnectionHandler connectionHandler = new DoorConnectionHandler();
        return new ConnectionData.ConnectorInitAction() {
            @Override
            public void check(WrappedBlockData blockData) {
                int type = baseDoors.indexOf(blockData.getMinecraftKey());
                if (type == -1) return;

                int id = blockData.getSavedBlockStateId();

                DoorData doorData = new DoorData(
                        blockData.getValue("half").equals("lower"),
                        blockData.getValue("hinge").equals("right"),
                        blockData.getValue("powered").equals("true"),
                        blockData.getValue("open").equals("true"),
                        BlockFace.valueOf(blockData.getValue("facing").toUpperCase()),
                        type
                );

                doorDataMap.put(id, doorData);

                connectedStates.put(getStates(doorData), id);

                ConnectionData.connectionHandlerMap.put(id, connectionHandler);
            }
        };
    }

    private static short getStates(DoorData doorData) {
        short s = 0;
        if (doorData.isLower()) s |= 1;
        if (doorData.isOpen()) s |= 2;
        if (doorData.isPowered()) s |= 4;
        if (doorData.isRightHinge()) s |= 8;
        s |= doorData.getFacing().ordinal() << 4;
        s |= (doorData.getType() & 0x7) << 6;
        return s;
    }

    @Override
    public int connect(UserConnection user, Position position, int blockState) {
        DoorData doorData = doorDataMap.get(blockState);
        if (doorData == null) return blockState;
        short s = 0;
        s |= (doorData.getType() & 0x7) << 6;
        if (doorData.isLower()) {
            DoorData upperHalf = doorDataMap.get(getBlockData(user, position.getRelative(BlockFace.TOP)));
            if (upperHalf == null) return blockState;
            s |= 1;
            if (doorData.isOpen()) s |= 2;
            if (upperHalf.isPowered()) s |= 4;
            if (upperHalf.isRightHinge()) s |= 8;
            s |= doorData.getFacing().ordinal() << 4;
        } else {
            DoorData lowerHalf = doorDataMap.get(getBlockData(user, position.getRelative(BlockFace.BOTTOM)));
            if (lowerHalf == null) return blockState;
            if (lowerHalf.isOpen()) s |= 2;
            if (doorData.isPowered()) s |= 4;
            if (doorData.isRightHinge()) s |= 8;
            s |= lowerHalf.getFacing().ordinal() << 4;
        }
        Integer newBlockState = connectedStates.get(s);
        return newBlockState == null ? blockState : newBlockState;
    }

    @AllArgsConstructor
    @Getter
    @ToString
    private static class DoorData {
        private final boolean lower, rightHinge, powered, open;
        private final BlockFace facing;
        private int type;
    }
}

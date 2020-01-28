package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.blockconnections.providers;

import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.platform.providers.Provider;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.data.MappingData;

public class BlockConnectionProvider implements Provider {

    public int getBlockData(UserConnection connection, int x, int y, int z) {
        int oldId = getWorldBlockData(connection, x, y, z);
        return MappingData.blockMappings.getNewId(oldId);
    }

    public int getWorldBlockData(UserConnection connection, int x, int y, int z) {
        return -1;
    }

    public void storeBlock(UserConnection connection, int x, int y, int z, int blockState) {

    }

    public void removeBlock(UserConnection connection, int x, int y, int z) {

    }

    public void clearStorage(UserConnection connection) {

    }

    public void unloadChunk(UserConnection connection, int x, int z) {

    }

    public boolean storesBlocks() {
        return false;
    }
}

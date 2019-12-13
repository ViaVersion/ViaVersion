package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.blockconnections.providers;

import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.storage.BlockConnectionStorage;

public class PacketBlockConnectionProvider extends BlockConnectionProvider {

    @Override
    public void storeBlock(UserConnection connection, int x, int y, int z, int blockState) {
        connection.get(BlockConnectionStorage.class).store(x, y, z, blockState);
    }

    @Override
    public void removeBlock(UserConnection connection, int x, int y, int z) {
        connection.get(BlockConnectionStorage.class).remove(x, y, z);
    }

    @Override
    public int getBlockData(UserConnection connection, int x, int y, int z) {
        return connection.get(BlockConnectionStorage.class).get(x, y, z);
    }

    @Override
    public void clearStorage(UserConnection connection) {
        connection.get(BlockConnectionStorage.class).clear();
    }

    @Override
    public void unloadChunk(UserConnection connection, int x, int z) {
        connection.get(BlockConnectionStorage.class).unloadChunk(x, z);
    }

    @Override
    public boolean storesBlocks() {
        return true;
    }
}

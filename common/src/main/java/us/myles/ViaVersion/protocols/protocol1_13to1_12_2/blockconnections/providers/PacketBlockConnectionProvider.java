package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.blockconnections.providers;

import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.minecraft.Position;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.storage.BlockConnectionStorage;

public class PacketBlockConnectionProvider extends BlockConnectionProvider {

    @Override
    public void storeBlock(UserConnection connection, Position position, int blockState) {
        connection.get(BlockConnectionStorage.class).store(position, blockState);
    }

    @Override
    public void removeBlock(UserConnection connection, Position position) {
        connection.get(BlockConnectionStorage.class).remove(position);
    }

    @Override
    public int getBlockdata(UserConnection connection, Position position) {
        return connection.get(BlockConnectionStorage.class).get(position);
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

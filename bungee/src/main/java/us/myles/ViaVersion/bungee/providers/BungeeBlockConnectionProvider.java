package us.myles.ViaVersion.bungee.providers;

import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.minecraft.Position;
import us.myles.ViaVersion.bungee.storage.BungeeBlockConnectionData;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.blockconnections.providers.BlockConnectionProvider;

public class BungeeBlockConnectionProvider extends BlockConnectionProvider {

    @Override
    public void storeBlock(UserConnection connection, Position position, int blockState) {
        connection.get(BungeeBlockConnectionData.class).store(position, blockState);
    }

    @Override
    public void removeBlock(UserConnection connection, Position position) {
        connection.get(BungeeBlockConnectionData.class).remove(position);
    }

    @Override
    public int getBlockdata(UserConnection connection, Position position) {
        return connection.get(BungeeBlockConnectionData.class).get(position);
    }

    @Override
    public boolean needBlockStore() {
        return true;
    }
}

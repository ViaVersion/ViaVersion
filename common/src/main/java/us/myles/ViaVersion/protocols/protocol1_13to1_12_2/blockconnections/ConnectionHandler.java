package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.blockconnections;

import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.minecraft.Position;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.blockconnections.providers.BlockConnectionProvider;

public abstract class ConnectionHandler {
    public abstract int connect(UserConnection user, Position position, int blockState);

    public int getBlockData(UserConnection user, Position position) {
        return Via.getManager().getProviders().get(BlockConnectionProvider.class).getBlockdata(user, position);
    }

    public boolean canConnect(int id) {
        ConnectionHandler handler = ConnectionData.connectionHandlerMap.get(id);
        return handler != null && handler == this;
    }
}

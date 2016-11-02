package us.myles.ViaVersion.bungee.providers;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.bungee.storage.BungeeStorage;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.providers.EntityIdProvider;

import java.lang.reflect.Method;

public class BungeeEntityIdProvider extends EntityIdProvider {
    private static Method getClientEntityId;

    static {
        try {
            getClientEntityId = Class.forName("net.md_5.bungee.UserConnection").getDeclaredMethod("getClientEntityId");
        } catch (NoSuchMethodException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getEntityId(UserConnection user) throws Exception {
        BungeeStorage storage = user.get(BungeeStorage.class);
        ProxiedPlayer player = storage.getPlayer();

        return (int) getClientEntityId.invoke(player);
    }
}

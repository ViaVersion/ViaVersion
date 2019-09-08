package us.myles.ViaVersion.bungee.providers;

import lombok.SneakyThrows;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.bungee.storage.BungeeStorage;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.providers.EntityIdProvider;
import us.myles.ViaVersion.util.InvokeUtil;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

public class BungeeEntityIdProvider extends EntityIdProvider {
    private static final MethodHandle getClientEntityId;

    static {
        try {
            getClientEntityId = InvokeUtil.lookup()
                    .findVirtual(Class.forName("net.md_5.bungee.UserConnection"), "getClientEntityId", MethodType.methodType(Integer.TYPE));
        } catch (NoSuchMethodException | IllegalAccessException | ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    @Override
    @SneakyThrows
    public int getEntityId(UserConnection user) throws Exception {
        BungeeStorage storage = user.get(BungeeStorage.class);
        ProxiedPlayer player = storage.getPlayer();

        return (int) getClientEntityId.invoke(player);
    }
}

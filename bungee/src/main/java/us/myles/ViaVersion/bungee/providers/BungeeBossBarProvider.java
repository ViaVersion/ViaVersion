package us.myles.ViaVersion.bungee.providers;

import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.bungee.storage.BungeeStorage;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.providers.BossBarProvider;

import java.util.UUID;

public class BungeeBossBarProvider extends BossBarProvider {
    @Override
    public void handleAdd(UserConnection user, UUID barUUID) {
        if (user.has(BungeeStorage.class)) {
            BungeeStorage storage = user.get(BungeeStorage.class);
            // Check if bossbars are supported by bungee, static maybe
            if (storage.getBossbar() != null) {
                storage.getBossbar().add(barUUID);
            }
        }
    }

    @Override
    public void handleRemove(UserConnection user, UUID barUUID) {
        if (user.has(BungeeStorage.class)) {
            BungeeStorage storage = user.get(BungeeStorage.class);
            if (storage.getBossbar() != null) {
                storage.getBossbar().remove(barUUID);
            }
        }
    }
}

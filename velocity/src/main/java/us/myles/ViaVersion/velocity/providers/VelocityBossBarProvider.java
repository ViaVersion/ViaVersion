package us.myles.ViaVersion.velocity.providers;

import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.providers.BossBarProvider;
import us.myles.ViaVersion.velocity.storage.VelocityStorage;

import java.util.UUID;

public class VelocityBossBarProvider extends BossBarProvider {
    @Override
    public void handleAdd(UserConnection user, UUID barUUID) {
        if (user.has(VelocityStorage.class)) {
            VelocityStorage storage = user.get(VelocityStorage.class);
            // Check if bossbars are supported by bungee, static maybe
            if (storage.getBossbar() != null) {
                storage.getBossbar().add(barUUID);
            }
        }
    }

    @Override
    public void handleRemove(UserConnection user, UUID barUUID) {
        if (user.has(VelocityStorage.class)) {
            VelocityStorage storage = user.get(VelocityStorage.class);
            if (storage.getBossbar() != null) {
                storage.getBossbar().remove(barUUID);
            }
        }
    }
}

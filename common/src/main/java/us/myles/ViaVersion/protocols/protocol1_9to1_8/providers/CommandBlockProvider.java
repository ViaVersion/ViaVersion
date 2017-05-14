package us.myles.ViaVersion.protocols.protocol1_9to1_8.providers;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.google.common.base.Optional;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.minecraft.Position;
import us.myles.ViaVersion.api.platform.providers.Provider;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.Protocol1_9TO1_8;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.storage.CommandBlockStorage;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.storage.EntityTracker;

public class CommandBlockProvider implements Provider {

    public void addOrUpdateBlock(UserConnection user, Position position, CompoundTag tag) throws Exception {
        checkPermission(user);
        if (isEnabled())
            getStorage(user).addOrUpdateBlock(position, tag);
    }

    public Optional<CompoundTag> get(UserConnection user, Position position) throws Exception {
        checkPermission(user);
        if (isEnabled())
            return getStorage(user).getCommandBlock(position);
        return Optional.absent();
    }

    public void unloadChunk(UserConnection user, int x, int z) throws Exception {
        checkPermission(user);
        if (isEnabled())
            getStorage(user).unloadChunk(x, z);
    }

    private CommandBlockStorage getStorage(UserConnection connection) {
        return connection.get(CommandBlockStorage.class);
    }

    public void sendPermission(UserConnection user) throws Exception {
        if (!isEnabled())
            return;
        PacketWrapper wrapper = new PacketWrapper(0x1B, null, user); // Entity status

        wrapper.write(Type.INT, user.get(EntityTracker.class).getProvidedEntityId()); // Entity ID
        wrapper.write(Type.BYTE, (byte) 26); // Hardcoded op permission level

        wrapper.send(Protocol1_9TO1_8.class);

        user.get(CommandBlockStorage.class).setPermissions(true);
    }

    // Fix for Bungee since the join game is not sent after the first one
    private void checkPermission(UserConnection user) throws Exception {
        if (!isEnabled())
            return;
        CommandBlockStorage storage = getStorage(user);
        if (!storage.isPermissions()) {
            sendPermission(user);
        }
    }

    public boolean isEnabled() {
        return true;
    }

    public void unloadChunks(UserConnection userConnection) {
        if (isEnabled())
            getStorage(userConnection).unloadChunks();
    }
}

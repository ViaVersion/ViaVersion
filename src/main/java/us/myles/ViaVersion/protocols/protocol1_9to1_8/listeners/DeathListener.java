package us.myles.ViaVersion.protocols.protocol1_9to1_8.listeners;

import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import us.myles.ViaVersion.ViaVersionPlugin;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.ViaVersion;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.protocols.base.ProtocolInfo;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.Protocol1_9TO1_8;

@RequiredArgsConstructor
public class DeathListener implements Listener {
    private final ViaVersionPlugin plugin;

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent e) {
        Player p = e.getEntity();
        if (ViaVersion.getConfig().isShowNewDeathMessages() && checkGamerule(p.getWorld()) && e.getDeathMessage() != null && checkPipeline(p)) {
            sendPacket(p, e.getDeathMessage());
        }
    }

    public boolean checkPipeline(Player p) {
        UserConnection userConnection = plugin.getConnection(p);
        return userConnection != null && userConnection.get(ProtocolInfo.class).getPipeline().contains(Protocol1_9TO1_8.class);
    }

    private UserConnection getUserConnection(Player p) {
        return plugin.getConnection(p);
    }

    public boolean checkGamerule(World w) {
        try {
            return Boolean.parseBoolean(w.getGameRuleValue("showDeathMessages"));
        } catch (Exception e) {
            return false;
        }
    }

    private void sendPacket(final Player p, final String msg) {
        Bukkit.getScheduler().runTask(plugin, new Runnable() {
            @Override
            public void run() {
                PacketWrapper wrapper = new PacketWrapper(0x2C, null, getUserConnection(p));
                try {
                    wrapper.write(Type.VAR_INT, 2);
                    wrapper.write(Type.VAR_INT, p.getEntityId());
                    wrapper.write(Type.INT, p.getEntityId());
                    Protocol1_9TO1_8.FIX_JSON.write(wrapper, msg);
                    wrapper.send(Protocol1_9TO1_8.class);
                } catch (Exception e) {
                    e.printStackTrace();
                    wrapper.clearInputBuffer();
                }
            }
        });
    }
}

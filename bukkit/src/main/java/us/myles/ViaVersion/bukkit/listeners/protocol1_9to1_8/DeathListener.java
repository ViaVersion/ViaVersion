package us.myles.ViaVersion.bukkit.listeners.protocol1_9to1_8;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import us.myles.ViaVersion.ViaVersionPlugin;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.bukkit.listeners.ViaBukkitListener;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.Protocol1_9To1_8;

public class DeathListener extends ViaBukkitListener {
    public DeathListener(ViaVersionPlugin plugin) {
        super(plugin, Protocol1_9To1_8.class);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent e) {
        Player p = e.getEntity();
        if (isOnPipe(p) && Via.getConfig().isShowNewDeathMessages() && checkGamerule(p.getWorld()) && e.getDeathMessage() != null)
            sendPacket(p, e.getDeathMessage());
    }

    public boolean checkGamerule(World w) {
        try {
            return Boolean.parseBoolean(w.getGameRuleValue("showDeathMessages"));
        } catch (Exception e) {
            return false;
        }
    }

    private void sendPacket(final Player p, final String msg) {
        Via.getPlatform().runSync(new Runnable() {
            @Override
            public void run() {
                // If online
                UserConnection userConnection = getUserConnection(p);
                if (userConnection != null) {
                    PacketWrapper wrapper = new PacketWrapper(0x2C, null, userConnection);
                    try {
                        wrapper.write(Type.VAR_INT, 2); // Event - Entity dead
                        wrapper.write(Type.VAR_INT, p.getEntityId()); // Player ID
                        wrapper.write(Type.INT, p.getEntityId()); // Entity ID
                        Protocol1_9To1_8.FIX_JSON.write(wrapper, msg); // Message

                        wrapper.send(Protocol1_9To1_8.class);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}

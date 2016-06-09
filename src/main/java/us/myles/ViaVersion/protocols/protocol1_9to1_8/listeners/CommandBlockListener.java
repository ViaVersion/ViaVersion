package us.myles.ViaVersion.protocols.protocol1_9to1_8.listeners;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.CommandBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.spacehq.opennbt.tag.builtin.ByteTag;
import org.spacehq.opennbt.tag.builtin.CompoundTag;
import us.myles.ViaVersion.ViaVersionPlugin;
import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.ViaVersion;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.minecraft.Position;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.protocols.base.ProtocolInfo;
import us.myles.ViaVersion.protocols.protocol1_9to1_8.Protocol1_9TO1_8;
import us.myles.ViaVersion.util.ReflectionUtil;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.lang.reflect.Method;

@RequiredArgsConstructor
public class CommandBlockListener implements Listener {

    private final ViaVersionPlugin plugin;

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent e) {
        sendOp(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRespawn(final PlayerRespawnEvent e) {
        Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
            @Override
            public void run() {
                sendOp(e.getPlayer());
            }
        }, 1L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldChange(PlayerChangedWorldEvent e) {
        sendOp(e.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && plugin.isPorted(e.getPlayer()) && e.getPlayer().isOp()) {
            // Ensure that the player is on our pipe
            UserConnection userConnection = ((ViaVersionPlugin) ViaVersion.getInstance()).getConnection(e.getPlayer());
            if (userConnection == null) return;
            if (!userConnection.get(ProtocolInfo.class).getPipeline().contains(Protocol1_9TO1_8.class)) return;

            try {
                sendCommandBlockPacket(e.getClickedBlock(), e.getPlayer());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void sendOp(Player p) {
        if (p.isOp() && plugin.isPorted(p)) {
            // Ensure that the player is on our pipe
            UserConnection userConnection = ((ViaVersionPlugin) ViaVersion.getInstance()).getConnection(p);
            if (userConnection == null) return;
            if (!userConnection.get(ProtocolInfo.class).getPipeline().contains(Protocol1_9TO1_8.class)) return;

            try {
                PacketWrapper wrapper = new PacketWrapper(0x1B, null, userConnection); // Entity status
                wrapper.write(Type.INT, p.getEntityId());
                wrapper.write(Type.BYTE, (byte) 26); //Hardcoded op permission level
                wrapper.send(Protocol1_9TO1_8.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void sendCommandBlockPacket(Block b, Player player) throws Exception {
        if (!(b.getState() instanceof CommandBlock))
            return;
        CommandBlock cmd = (CommandBlock) b.getState();

        Object tileEntityCommand = ReflectionUtil.get(cmd, "commandBlock", ReflectionUtil.nms("TileEntityCommand"));
        Object updatePacket = ReflectionUtil.invoke(tileEntityCommand, "getUpdatePacket");

        UserConnection userConnection = ((ViaVersionPlugin) ViaVersion.getInstance()).getConnection(player);

        PacketWrapper wrapper = generatePacket(updatePacket, userConnection);
        wrapper.send(Protocol1_9TO1_8.class);
    }

    private PacketWrapper generatePacket(Object updatePacket, UserConnection usr) throws Exception {
        PacketWrapper wrapper = new PacketWrapper(0x09, null, usr); // Update block entity

        long[] pos = getPosition(ReflectionUtil.get(updatePacket, "a", ReflectionUtil.nms("BlockPosition")));

        wrapper.write(Type.POSITION, new Position(pos[0], pos[1], pos[2])); //Block position
        wrapper.write(Type.BYTE, (byte) 2); // Action id always 2

        CompoundTag nbt = getNBT(ReflectionUtil.get(updatePacket, "c", ReflectionUtil.nms("NBTTagCompound")));
        if (nbt == null) {
            wrapper.write(Type.BYTE, (byte) 0); //If nbt is null. Use 0 as nbt
            return wrapper;
        }
        nbt.put(new ByteTag("powered", (byte) 0));
        nbt.put(new ByteTag("auto", (byte) 0));
        nbt.put(new ByteTag("conditionMet", (byte) 0));

        wrapper.write(Type.NBT, nbt); // NBT TAG
        return wrapper;
    }

    private long[] getPosition(Object obj) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        return new long[]{
                (long) ReflectionUtil.getSuper(obj, "a", int.class), //X
                (long) ReflectionUtil.getSuper(obj, isR1() ? "b" : "c", int.class), //Y
                (long) ReflectionUtil.getSuper(obj, isR1() ? "c" : "d", int.class)  //Z
        };
    }

    private boolean isR1() {
        return ReflectionUtil.getVersion().equals("v1_8_R1");
    }

    private CompoundTag getNBT(Object obj) throws Exception {
        ByteBuf buf = Unpooled.buffer();
        Method m = ReflectionUtil.nms("NBTCompressedStreamTools").getMethod("a", ReflectionUtil.nms("NBTTagCompound"), DataOutput.class);
        m.invoke(null, obj, new DataOutputStream(new ByteBufOutputStream(buf)));
        try {
            return Type.NBT.read(buf);
        } finally {
            buf.release();
        }
    }
}


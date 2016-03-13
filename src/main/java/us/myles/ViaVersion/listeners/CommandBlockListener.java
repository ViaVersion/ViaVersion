package us.myles.ViaVersion.listeners;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import lombok.RequiredArgsConstructor;
import org.bukkit.block.Block;
import org.bukkit.block.CommandBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.spacehq.opennbt.tag.builtin.ByteTag;
import org.spacehq.opennbt.tag.builtin.CompoundTag;
import us.myles.ViaVersion.ViaVersionPlugin;
import us.myles.ViaVersion.packets.PacketType;
import us.myles.ViaVersion.util.PacketUtil;
import us.myles.ViaVersion.util.ReflectionUtil;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.lang.reflect.Method;

@RequiredArgsConstructor
public class CommandBlockListener implements Listener {

    private final ViaVersionPlugin plugin;

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onJoin(final PlayerJoinEvent e) {
        if (e.getPlayer().isOp() && plugin.isPorted(e.getPlayer())) {
            ByteBuf buf = Unpooled.buffer();
            PacketUtil.writeVarInt(PacketType.PLAY_ENTITY_STATUS.getNewPacketID(), buf);
            buf.writeInt(e.getPlayer().getEntityId());
            buf.writeByte(26);
            plugin.sendRawPacket(e.getPlayer(), buf);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && plugin.isPorted(e.getPlayer()) && e.getPlayer().isOp()) {
            try {
                sendCommandBlockPacket(e.getClickedBlock(), e.getPlayer());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void sendCommandBlockPacket(Block b, Player player) throws Exception {
        if (!(b.getState() instanceof CommandBlock))
            return;
        CommandBlock cmd = (CommandBlock) b.getState();

        Object tileEntityCommand = ReflectionUtil.get(cmd, "commandBlock", ReflectionUtil.nms("TileEntityCommand"));
        Object updatePacket = ReflectionUtil.invoke(tileEntityCommand, "getUpdatePacket");
        ByteBuf buf = packetToByteBuf(updatePacket);
        plugin.sendRawPacket(player, buf);
    }

    private ByteBuf packetToByteBuf(Object updatePacket) throws Exception {
        ByteBuf buf = Unpooled.buffer();
        PacketUtil.writeVarInt(PacketType.PLAY_UPDATE_BLOCK_ENTITY.getNewPacketID(), buf); //Packet ID
        long[] pos = getPosition(ReflectionUtil.get(updatePacket, "a", ReflectionUtil.nms("BlockPosition")));
        PacketUtil.writeBlockPosition(buf, pos[0], pos[1], pos[2]); //Block position
        buf.writeByte(2); //Action id always 2
        CompoundTag nbt = getNBT(ReflectionUtil.get(updatePacket, "c", ReflectionUtil.nms("NBTTagCompound")));
        if (nbt == null) {
            buf.writeByte(0); //If nbt is null. Use 0 as nbt
            return buf;
        }
        nbt.put(new ByteTag("powered", (byte) 0));
        nbt.put(new ByteTag("auto", (byte) 0));
        nbt.put(new ByteTag("conditionMet", (byte) 0));
        PacketUtil.writeNBT(buf, nbt); //NBT tag
        return buf;
    }

    private long[] getPosition(Object obj) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        return new long[]{
                (long) ReflectionUtil.getSuper(obj, "a", int.class), //X
                (long) ReflectionUtil.getSuper(obj, "c", int.class), //Y
                (long) ReflectionUtil.getSuper(obj, "d", int.class)  //Z
        };
    }

    private CompoundTag getNBT(Object obj) throws Exception {
        ByteBuf buf = Unpooled.buffer();
        Method m = ReflectionUtil.nms("NBTCompressedStreamTools").getMethod("a", ReflectionUtil.nms("NBTTagCompound"), DataOutput.class);
        m.invoke(null, obj, new DataOutputStream(new ByteBufOutputStream(buf)));
        try {
            return PacketUtil.readNBT(buf);
        } finally {
            buf.release();
        }
    }
}


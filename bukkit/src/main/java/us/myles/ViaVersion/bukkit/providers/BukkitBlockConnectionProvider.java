package us.myles.ViaVersion.bukkit.providers;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.minecraft.Position;
import us.myles.ViaVersion.protocols.base.ProtocolInfo;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.blockconnections.providers.BlockConnectionProvider;

import java.util.UUID;

public class BukkitBlockConnectionProvider extends BlockConnectionProvider {
    private Chunk lastChunk;

    @Override
    public int getWorldBlockData(UserConnection user, Position position) {
        UUID uuid = user.get(ProtocolInfo.class).getUuid();
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            World world = player.getWorld();
            int x = (int) (position.getX() >> 4);
            int z = (int) (position.getZ() >> 4);
            if (world.isChunkLoaded(x, z)) {
                Chunk c = getChunk(world, x, z);
                Block b = c.getBlock(position.getX().intValue(), position.getY().intValue(), position.getZ().intValue());
                return b.getTypeId() << 4 | b.getData();
            }
        }
        return 0;
    }

    public Chunk getChunk(World world, int x, int z) {
        if (lastChunk != null && lastChunk.getX() == x && lastChunk.getZ() == z) {
            return lastChunk;
        }
        return lastChunk = world.getChunkAt(x, z);
    }
}

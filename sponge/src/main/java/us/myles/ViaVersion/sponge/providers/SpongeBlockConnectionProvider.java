package us.myles.ViaVersion.sponge.providers;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.World;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.minecraft.Position;
import us.myles.ViaVersion.protocols.base.ProtocolInfo;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.blockconnections.providers.BlockConnectionProvider;
import us.myles.ViaVersion.util.ReflectionUtil;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class SpongeBlockConnectionProvider extends BlockConnectionProvider {
    private static Class block;
    private static Map<Object, Integer> blockStateIds;

    static {
        try {
            block = Class.forName("net.minecraft.block.Block");
            blockStateIds = ReflectionUtil.get(
                    ReflectionUtil.getStatic(block, "field_176229_d", Object.class),
                    "field_148749_a", Map.class);
        } catch (ClassNotFoundException e) {
            Via.getPlatform().getLogger().warning("net.minecraft.block.Block not found! Are you using Lantern?");
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getWorldBlockData(UserConnection user, Position position) {
        if (blockStateIds != null) {
            UUID uuid = user.get(ProtocolInfo.class).getUuid();
            Optional<Player> player = Sponge.getServer().getPlayer(uuid);
            if (player.isPresent()) {
                World world = player.get().getWorld();
                Optional<Chunk> chunk = world.getChunkAtBlock(position.getX().intValue(), position.getY().intValue(), position.getZ().intValue());
                if (chunk.isPresent()) {
                    BlockState b = chunk.get().getBlock(position.getX().intValue(), position.getY().intValue(), position.getZ().intValue());
                    Integer id = blockStateIds.get(b);
                    if (id == null) {
                        System.out.println("id not found");
                    } else {
                        return id;
                    }
                }
            }
        }
        return 0;
    }
}

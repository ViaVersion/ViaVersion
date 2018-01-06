package us.myles.ViaVersion.protocols.protocolsnapshotto1_12_2.providers.blockentities;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.Tag;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.minecraft.Position;
import us.myles.ViaVersion.protocols.protocolsnapshotto1_12_2.providers.BlockEntityProvider;
import us.myles.ViaVersion.protocols.protocolsnapshotto1_12_2.storage.BlockStorage;

public class BannerHandler implements BlockEntityProvider.BlockEntityHandler {
    private final int WALL_BANNER_START = 5633; // 4 each
    private final int WALL_BANNER_STOP = 5696;

    private final int BANNER_START = 5377; // 16 each
    private final int BANNER_STOP = 5632;

    @Override
    public int transform(UserConnection user, CompoundTag tag) {
        BlockStorage storage = user.get(BlockStorage.class);
        Position position = new Position(getLong(tag.get("x")), getLong(tag.get("y")), getLong(tag.get("z")));

        if (!storage.contains(position)) {
            System.out.println("Received an banner color update packet, but there is no banner! O_o " + tag);
            return -1;
        }

        int blockId = storage.get(position).getOriginal();

        int color = (int) tag.get("Base").getValue();
        // Standing banner
        if (blockId >= BANNER_START && blockId <= BANNER_STOP)
            blockId += ((15 - color) * 16);
            // Wall banner
        else if (blockId >= WALL_BANNER_START && blockId <= WALL_BANNER_STOP)
            blockId += ((15 - color) * 4);
        else
            System.out.println("Why does this block have the banner block entity? :(" + tag);

        return blockId;
    }

    private long getLong(Tag tag) {
        return ((Integer) tag.getValue()).longValue();
    }
}
package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.providers.blockentities;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.NumberTag;
import com.github.steveice10.opennbt.tag.builtin.Tag;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.minecraft.Position;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.providers.BlockEntityProvider;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.storage.BlockStorage;

public class BedHandler implements BlockEntityProvider.BlockEntityHandler {

    @Override
    public int transform(UserConnection user, CompoundTag tag) {
        BlockStorage storage = user.get(BlockStorage.class);
        Position position = new Position((int) getLong(tag.get("x")), (short) getLong(tag.get("y")), (int) getLong(tag.get("z")));

        if (!storage.contains(position)) {
            Via.getPlatform().getLogger().warning("Received an bed color update packet, but there is no bed! O_o " + tag);
            return -1;
        }

        //                                              RED_BED + FIRST_BED
        int blockId = storage.get(position).getOriginal() - 972 + 748;

        Tag color = tag.get("color");
        if (color != null) {
            blockId += (((NumberTag) color).asInt() * 16);
        }

        return blockId;
    }

    private long getLong(NumberTag tag) {
        return tag.asLong();
    }
}

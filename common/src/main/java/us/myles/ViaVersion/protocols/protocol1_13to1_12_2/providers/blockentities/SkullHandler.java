package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.providers.blockentities;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.Tag;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.minecraft.Position;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.providers.BlockEntityProvider;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.storage.BlockStorage;

public class SkullHandler implements BlockEntityProvider.BlockEntityHandler {
    private final int SKULL_WALL_START = 5447;
    private final int SKULL_END = 5566;

    @Override
    public int transform(UserConnection user, CompoundTag tag) {
        BlockStorage storage = user.get(BlockStorage.class);
        Position position = new Position(getLong(tag.get("x")), getLong(tag.get("y")), getLong(tag.get("z")));

        if (!storage.contains(position)) {
            Via.getPlatform().getLogger().warning("Received an head update packet, but there is no head! O_o " + tag);
            return -1;
        }

        int id = storage.get(position).getOriginal();

        if (id >= SKULL_WALL_START && id <= SKULL_END) {
            id += (byte) tag.get("SkullType").getValue() * 20;
            if (tag.contains("Rot")) {
                id += (byte) tag.get("Rot").getValue();
            }
        } else {
            Via.getPlatform().getLogger().warning("Why does this block have the skull block entity? " + tag);
            return -1;
        }

        return id;
    }

    private long getLong(Tag tag) {
        return ((Integer) tag.getValue()).longValue();
    }
}

package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.providers.blockentities;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.StringTag;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.data.EntityNameRewriter;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.providers.BlockEntityProvider;

public class SpawnerHandler implements BlockEntityProvider.BlockEntityHandler {
    @Override
    public int transform(UserConnection user, CompoundTag tag) {
        if (tag.contains("SpawnData") && tag.get("SpawnData") instanceof CompoundTag) {
            CompoundTag data = tag.get("SpawnData");
            if (data.contains("id") && data.get("id") instanceof StringTag) {
                StringTag s = data.get("id");
                s.setValue(EntityNameRewriter.rewrite(s.getValue()));
            }

        }

        // Always return -1 because the block is still the same id
        return -1;
    }
}

package us.myles.ViaVersion.protocols.protocolsnapshotto1_12_2.providers.blockentities;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.github.steveice10.opennbt.tag.builtin.Tag;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.minecraft.Position;
import us.myles.ViaVersion.protocols.protocolsnapshotto1_12_2.providers.BlockEntityProvider;
import us.myles.ViaVersion.protocols.protocolsnapshotto1_12_2.storage.BlockStorage;

public class SkullHandler implements BlockEntityProvider.BlockEntityHandler {
    @Override
    public int transform(UserConnection user, CompoundTag tag) {
        BlockStorage storage = user.get(BlockStorage.class);
        Position position = new Position(getLong(tag.get("x")), getLong(tag.get("y")), getLong(tag.get("z")));

        if (!storage.contains(position)) {
            System.out.println("Received an head update packet, but there is no head! O_o " + tag);
            return -1;
        }

        int data = storage.get(position).getOriginal() & 0xF;

        byte type = (Byte) tag.get("SkullType").getValue();

        int add = 0;

        // wall head start
        int blockId = 4669;

        switch (data % 6) {
            case 1:
                add = (Byte) tag.get("Rot").getValue() + 4;
                break;
            case 2:
                add = 0;
                break;
            case 3:
                add = 2;
                break;
            case 4:
                add = 3;
                break;
            case 5:
                add = 1;
                break;
        }

        blockId += add + type * 20;

        return blockId;
    }
    private long getLong(Tag tag) {
        return ((Integer) tag.getValue()).longValue();
    }
}

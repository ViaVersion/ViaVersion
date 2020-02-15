package us.myles.ViaVersion.protocols.protocol20w07ato1_16;

import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.packets.State;
import us.myles.ViaVersion.protocols.protocol1_16to1_15_2.packets.InventoryPackets;

import static us.myles.ViaVersion.protocols.protocol1_16to1_15_2.Protocol1_16To1_15_2.getNewBlockId;

public class Protocol20w07aTo1_16 extends Protocol {

    @Override
    protected void registerPackets() {

        // Tags
        registerOutgoing(State.PLAY, 0x5C, 0x5C, new PacketRemapper() {
        @Override
        public void registerMap() {
            handler(wrapper -> {
                int blockTagsSize = wrapper.read(Type.VAR_INT);
                wrapper.write(Type.VAR_INT, blockTagsSize + 1); // new tag(s)

                for (int i = 0; i < blockTagsSize; i++) {
                    wrapper.passthrough(Type.STRING);
                    int[] blockIds = wrapper.passthrough(Type.VAR_INT_ARRAY_PRIMITIVE);
                    for (int j = 0; j < blockIds.length; j++) {
                        blockIds[j] = getNewBlockId(blockIds[j]);
                    }
                }

                // Only send the necessary new tags
                wrapper.write(Type.STRING, "minecraft:beacon_base_blocks");
                wrapper.write(Type.VAR_INT_ARRAY_PRIMITIVE, new int[]{getNewBlockId(133), getNewBlockId(134), getNewBlockId(148), getNewBlockId(265)});

                int itemTagsSize = wrapper.passthrough(Type.VAR_INT);
                for (int i = 0; i < itemTagsSize; i++) {
                    wrapper.passthrough(Type.STRING);
                    int[] itemIds = wrapper.passthrough(Type.VAR_INT_ARRAY_PRIMITIVE);
                    for (int j = 0; j < itemIds.length; j++) {
                        itemIds[j] = InventoryPackets.getNewItemId(itemIds[j]);
                    }
                }
            });
        }
        });
    }

    @Override
    public void init(UserConnection userConnection) {

    }
}

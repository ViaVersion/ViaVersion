package us.myles.ViaVersion.protocols.protocol1_12to1_11_1;

import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketHandler;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.packets.State;

public class Protocol1_12To1_12_Pre6 extends Protocol {
    @Override
    protected void registerPackets() {
        registerOutgoing(State.PLAY, 0x48, 0x48, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // 0 - Sound name
                map(Type.VAR_INT); // 1 - Sound Category
                map(Type.INT); // 2 - x
                map(Type.INT); // 3 - y
                map(Type.INT); // 4 - z
                map(Type.FLOAT); // 5 - Volume
                map(Type.FLOAT); // 6 - Pitch

                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        int id = wrapper.get(Type.VAR_INT, 0);
                        id = getNewSoundId(id);

                        if (id == -1) // Removed
                            wrapper.cancel();
                        wrapper.set(Type.VAR_INT, 0, id);
                    }
                });
            }
        });
    }

    @Override
    public void init(UserConnection userConnection) {

    }

    private int getNewSoundId(int id) { //TODO Make it better, suggestions are welcome. It's ugly and hardcoded now.
        int newId = id;
        if (id >= 544) // UI toast sound
            newId += 3;
        return newId;
    }
}

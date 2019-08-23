package us.myles.ViaVersion.protocols.protocol1_15to1_14_4;

import us.myles.ViaVersion.api.PacketWrapper;
import us.myles.ViaVersion.api.data.UserConnection;
import us.myles.ViaVersion.api.entities.Entity1_15Types;
import us.myles.ViaVersion.api.protocol.Protocol;
import us.myles.ViaVersion.api.remapper.PacketHandler;
import us.myles.ViaVersion.api.remapper.PacketRemapper;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.packets.State;
import us.myles.ViaVersion.protocols.protocol1_15to1_14_4.data.MappingData;
import us.myles.ViaVersion.protocols.protocol1_15to1_14_4.packets.EntityPackets;
import us.myles.ViaVersion.protocols.protocol1_15to1_14_4.storage.EntityTracker;

public class Protocol1_15To1_14_4 extends Protocol {

    @Override
    protected void registerPackets() {
        //TODO do the new tags have to be sent?
        //TODO sound, item, possibly block remaps (and blockstates should be the same with the new ones just being appended?)

        MappingData.init();
        EntityPackets.register(this);

        // Join Game
        registerOutgoing(State.PLAY, 0x25, 0x26, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.INT); // 0 - Entity ID
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        Entity1_15Types.EntityType entType = Entity1_15Types.EntityType.PLAYER;
                        EntityTracker tracker = wrapper.user().get(EntityTracker.class);
                        tracker.addEntity(wrapper.get(Type.INT, 0), entType);
                    }
                });
            }
        });

        // Sound Effect
        registerOutgoing(State.PLAY, 0x51, 0x52, new PacketRemapper() {
            @Override
            public void registerMap() {
                map(Type.VAR_INT); // Sound Id
                handler(new PacketHandler() {
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        wrapper.set(Type.VAR_INT, 0, MappingData.soundMappings.getNewSound(wrapper.get(Type.VAR_INT, 0)));
                    }
                });
            }
        });

        registerOutgoing(State.PLAY, 0x08, 0x09);
        registerOutgoing(State.PLAY, 0x09, 0x0A);
        registerOutgoing(State.PLAY, 0x0A, 0x0B);
        registerOutgoing(State.PLAY, 0x0B, 0x0C);
        registerOutgoing(State.PLAY, 0x0C, 0x0D);
        registerOutgoing(State.PLAY, 0x0D, 0x0E);
        registerOutgoing(State.PLAY, 0x0E, 0x0F);
        registerOutgoing(State.PLAY, 0x0F, 0x10);
        registerOutgoing(State.PLAY, 0x10, 0x11);
        registerOutgoing(State.PLAY, 0x11, 0x12);
        registerOutgoing(State.PLAY, 0x12, 0x13);
        registerOutgoing(State.PLAY, 0x13, 0x14);
        registerOutgoing(State.PLAY, 0x14, 0x15);
        registerOutgoing(State.PLAY, 0x15, 0x16);
        registerOutgoing(State.PLAY, 0x16, 0x17);
        registerOutgoing(State.PLAY, 0x17, 0x18);
        registerOutgoing(State.PLAY, 0x18, 0x19);
        registerOutgoing(State.PLAY, 0x19, 0x1A);
        registerOutgoing(State.PLAY, 0x1A, 0x1B);
        registerOutgoing(State.PLAY, 0x1B, 0x1C);
        registerOutgoing(State.PLAY, 0x1C, 0x1D);
        registerOutgoing(State.PLAY, 0x1D, 0x1E);
        registerOutgoing(State.PLAY, 0x1E, 0x1F);
        registerOutgoing(State.PLAY, 0x1F, 0x20);
        registerOutgoing(State.PLAY, 0x20, 0x21);
        registerOutgoing(State.PLAY, 0x21, 0x22);
        registerOutgoing(State.PLAY, 0x22, 0x23);
        registerOutgoing(State.PLAY, 0x23, 0x24);
        registerOutgoing(State.PLAY, 0x24, 0x25);

        registerOutgoing(State.PLAY, 0x26, 0x27);
        registerOutgoing(State.PLAY, 0x27, 0x28);
        registerOutgoing(State.PLAY, 0x28, 0x29);
        registerOutgoing(State.PLAY, 0x29, 0x2A);
        registerOutgoing(State.PLAY, 0x2A, 0x2B);
        registerOutgoing(State.PLAY, 0x2B, 0x2C);
        registerOutgoing(State.PLAY, 0x2C, 0x2D);
        registerOutgoing(State.PLAY, 0x2D, 0x2E);
        registerOutgoing(State.PLAY, 0x2E, 0x2F);
        registerOutgoing(State.PLAY, 0x2F, 0x30);
        registerOutgoing(State.PLAY, 0x30, 0x31);
        registerOutgoing(State.PLAY, 0x31, 0x32);
        registerOutgoing(State.PLAY, 0x32, 0x33);
        registerOutgoing(State.PLAY, 0x33, 0x34);
        registerOutgoing(State.PLAY, 0x34, 0x35);
        registerOutgoing(State.PLAY, 0x35, 0x36);
        registerOutgoing(State.PLAY, 0x36, 0x37);
        registerOutgoing(State.PLAY, 0x37, 0x38);
        registerOutgoing(State.PLAY, 0x38, 0x39);
        registerOutgoing(State.PLAY, 0x39, 0x3A);
        registerOutgoing(State.PLAY, 0x3A, 0x3B);
        registerOutgoing(State.PLAY, 0x3B, 0x3C);
        registerOutgoing(State.PLAY, 0x3C, 0x3D);
        registerOutgoing(State.PLAY, 0x3D, 0x3E);
        registerOutgoing(State.PLAY, 0x3E, 0x3F);
        registerOutgoing(State.PLAY, 0x3F, 0x40);
        registerOutgoing(State.PLAY, 0x40, 0x41);
        registerOutgoing(State.PLAY, 0x41, 0x42);
        registerOutgoing(State.PLAY, 0x42, 0x43);

        registerOutgoing(State.PLAY, 0x44, 0x45);
        registerOutgoing(State.PLAY, 0x45, 0x46);
        registerOutgoing(State.PLAY, 0x46, 0x47);
        registerOutgoing(State.PLAY, 0x47, 0x48);
        registerOutgoing(State.PLAY, 0x48, 0x49);
        registerOutgoing(State.PLAY, 0x49, 0x4A);
        registerOutgoing(State.PLAY, 0x4A, 0x4B);
        registerOutgoing(State.PLAY, 0x4B, 0x4C);
        registerOutgoing(State.PLAY, 0x4C, 0x4D);
        registerOutgoing(State.PLAY, 0x4D, 0x4E);
        registerOutgoing(State.PLAY, 0x4E, 0x4F);
        registerOutgoing(State.PLAY, 0x4F, 0x50);
        registerOutgoing(State.PLAY, 0x50, 0x51);

        registerOutgoing(State.PLAY, 0x52, 0x53);
        registerOutgoing(State.PLAY, 0x53, 0x54);
        registerOutgoing(State.PLAY, 0x54, 0x55);
        registerOutgoing(State.PLAY, 0x55, 0x56);
        registerOutgoing(State.PLAY, 0x56, 0x57);
        registerOutgoing(State.PLAY, 0x57, 0x58);
        registerOutgoing(State.PLAY, 0x58, 0x59);
        registerOutgoing(State.PLAY, 0x59, 0x5A);
        registerOutgoing(State.PLAY, 0x5A, 0x5B);
        registerOutgoing(State.PLAY, 0x5B, 0x5C);
        registerOutgoing(State.PLAY, 0x5C, 0x08);
    }

    @Override
    public void init(UserConnection userConnection) {
        userConnection.put(new EntityTracker(userConnection));
    }
}

package us.myles.ViaVersion.sponge.providers;

import us.myles.ViaVersion.protocols.protocol1_9to1_8.providers.MovementTransmitterProvider;

import java.lang.reflect.Field;

public class SpongeViaMovementTransmitter extends MovementTransmitterProvider {
    // Used for packet mode
    private Object idlePacket;
    private Object idlePacket2;

    public SpongeViaMovementTransmitter() {
        Class<?> idlePacketClass;
        try {
            idlePacketClass = Class.forName("net.minecraft.network.play.client.C03PacketPlayer");
        } catch (ClassNotFoundException e) {
            return; // We'll hope this is 1.9.4+
        }
        try {
            idlePacket = idlePacketClass.newInstance();
            idlePacket2 = idlePacketClass.newInstance();

            Field flying = idlePacketClass.getDeclaredField("field_149474_g");
            flying.setAccessible(true);

            flying.set(idlePacket2, true);
        } catch (NoSuchFieldException | InstantiationException | IllegalArgumentException | IllegalAccessException e) {
            throw new RuntimeException("Couldn't make player idle packet, help!", e);
        }
    }

    @Override
    public Object getFlyingPacket() {
        if (idlePacket == null)
            throw new NullPointerException("Could not locate flying packet");
        return idlePacket2;
    }

    @Override
    public Object getGroundPacket() {
        if (idlePacket == null)
            throw new NullPointerException("Could not locate flying packet");
        return idlePacket;
    }
}

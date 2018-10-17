package us.myles.ViaVersion.protocols.protocol1_13_2to1_13_1.types;

import io.netty.buffer.ByteBuf;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.protocols.protocol1_13to1_12_2.data.Particle;

public class Particle1_13_2Type extends Type<Particle> {
    public Particle1_13_2Type() {
        super("Particle", Particle.class);
    }

    @Override
    public void write(ByteBuf buffer, Particle object) throws Exception {
        Type.VAR_INT.write(buffer, object.getId());
        for (Particle.ParticleData data : object.getArguments())
            data.getType().write(buffer, data.getValue());
    }

    @Override
    public Particle read(ByteBuf buffer) throws Exception {
        int type = Type.VAR_INT.read(buffer);
        Particle particle = new Particle(type);

        switch (type) {
            // Block / Falling Dust /
            case 3:
            case 20:
                particle.getArguments().add(new Particle.ParticleData(Type.VAR_INT, Type.VAR_INT.read(buffer))); // Flat Block
                break;
            // Dust
            case 11:
                particle.getArguments().add(new Particle.ParticleData(Type.FLOAT, Type.FLOAT.read(buffer))); // Red 0 - 1
                particle.getArguments().add(new Particle.ParticleData(Type.FLOAT, Type.FLOAT.read(buffer))); // Green 0 - 1
                particle.getArguments().add(new Particle.ParticleData(Type.FLOAT, Type.FLOAT.read(buffer))); // Blue 0 - 1
                particle.getArguments().add(new Particle.ParticleData(Type.FLOAT, Type.FLOAT.read(buffer)));// Scale 0.01 - 4
                break;
            // Item
            case 27:
                particle.getArguments().add(new Particle.ParticleData(Type.FLAT_VAR_INT_ITEM, Type.FLAT_VAR_INT_ITEM.read(buffer))); // Flat item
                break;
        }
        return particle;
    }
}

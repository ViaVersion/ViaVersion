package us.myles.ViaVersion.api.type.types.minecraft;

import io.netty.buffer.ByteBuf;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.api.type.types.Particle;

public class Particle1_14Type extends Type<Particle> {

    public Particle1_14Type() {
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
            case 23:
                particle.getArguments().add(new Particle.ParticleData(Type.VAR_INT, Type.VAR_INT.read(buffer))); // Flat Block
                break;
            // Dust
            case 14:
                particle.getArguments().add(new Particle.ParticleData(Type.FLOAT, Type.FLOAT.read(buffer))); // Red 0 - 1
                particle.getArguments().add(new Particle.ParticleData(Type.FLOAT, Type.FLOAT.read(buffer))); // Green 0 - 1
                particle.getArguments().add(new Particle.ParticleData(Type.FLOAT, Type.FLOAT.read(buffer))); // Blue 0 - 1
                particle.getArguments().add(new Particle.ParticleData(Type.FLOAT, Type.FLOAT.read(buffer)));// Scale 0.01 - 4
                break;
            // Item
            case 32:
                particle.getArguments().add(new Particle.ParticleData(Type.FLAT_VAR_INT_ITEM, Type.FLAT_VAR_INT_ITEM.read(buffer))); // Flat item
                break;
        }
        return particle;
    }
}


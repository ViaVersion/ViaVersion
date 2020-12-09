package us.myles.ViaVersion.api.type.types.minecraft;

import io.netty.buffer.ByteBuf;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.type.Type;
import us.myles.ViaVersion.api.type.types.Particle;

public class Particle1_17Type extends Type<Particle> {

    public Particle1_17Type() {
        super("Particle", Particle.class);
    }

    @Override
    public void write(ByteBuf buffer, Particle object) throws Exception {
        Type.VAR_INT.writePrimitive(buffer, object.getId());
        for (Particle.ParticleData data : object.getArguments()) {
            data.getType().write(buffer, data.getValue());
        }
    }

    @Override
    public Particle read(ByteBuf buffer) throws Exception {
        int type = Type.VAR_INT.readPrimitive(buffer);
        Particle particle = new Particle(type);

        switch (type) {
            case 3: // Block
            case 24: // Falling dust
                particle.getArguments().add(new Particle.ParticleData(Type.VAR_INT, Type.VAR_INT.readPrimitive(buffer))); // Flat Block
                break;
            case 14: // Dust
            case 15: // Dust transition
                particle.getArguments().add(new Particle.ParticleData(Type.FLOAT, Type.FLOAT.readPrimitive(buffer))); // Red 0 - 1
                particle.getArguments().add(new Particle.ParticleData(Type.FLOAT, Type.FLOAT.readPrimitive(buffer))); // Green 0 - 1
                particle.getArguments().add(new Particle.ParticleData(Type.FLOAT, Type.FLOAT.readPrimitive(buffer))); // Blue 0 - 1
                particle.getArguments().add(new Particle.ParticleData(Type.FLOAT, Type.FLOAT.readPrimitive(buffer))); // Scale 0.01 - 4
                if (type == 15) {
                    // Transition to color
                    particle.getArguments().add(new Particle.ParticleData(Type.FLOAT, Type.FLOAT.readPrimitive(buffer))); // Red
                    particle.getArguments().add(new Particle.ParticleData(Type.FLOAT, Type.FLOAT.readPrimitive(buffer))); // Green
                    particle.getArguments().add(new Particle.ParticleData(Type.FLOAT, Type.FLOAT.readPrimitive(buffer))); // Blue
                }
                break;
            case 33: // Item
                particle.getArguments().add(new Particle.ParticleData(Type.FLAT_VAR_INT_ITEM, Type.FLAT_VAR_INT_ITEM.read(buffer))); // Flat item
                break;
            case 36: // Vibration path
                particle.getArguments().add(new Particle.ParticleData(Type.POSITION1_14, Type.POSITION1_14.read(buffer))); // From block pos
                String resourceLocation = Type.STRING.read(buffer);
                if (resourceLocation.equals("block")) {
                    particle.getArguments().add(new Particle.ParticleData(Type.POSITION1_14, Type.POSITION1_14.read(buffer))); // Target block pos
                } else if (resourceLocation.equals("entity")) {
                    particle.getArguments().add(new Particle.ParticleData(Type.VAR_INT, Type.VAR_INT.read(buffer))); // Target entity
                } else {
                    Via.getPlatform().getLogger().warning("Unknown vibration path position source type: " + resourceLocation);
                }
                particle.getArguments().add(new Particle.ParticleData(Type.VAR_INT, Type.VAR_INT.readPrimitive(buffer))); // Arrival in ticks
        }
        return particle;
    }
}


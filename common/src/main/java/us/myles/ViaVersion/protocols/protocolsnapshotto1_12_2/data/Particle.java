package us.myles.ViaVersion.protocols.protocolsnapshotto1_12_2.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import us.myles.ViaVersion.api.type.Type;

import java.util.Set;
import java.util.TreeSet;

@Data
public class Particle {
    private int id;
    private Set<ParticleData> arguments = new TreeSet<>();

    public Particle(int id) {
        this.id = id;
    }

    @lombok.Data
    @AllArgsConstructor
    public static class ParticleData {
        private Type type;
        private Object value;
    }
}

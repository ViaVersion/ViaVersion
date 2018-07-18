package us.myles.ViaVersion.protocols.protocol1_13to1_12_2.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import us.myles.ViaVersion.api.type.Type;

import java.util.LinkedList;
import java.util.List;

@Data
public class Particle {
    private int id;
    private List<ParticleData> arguments = new LinkedList<>();

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

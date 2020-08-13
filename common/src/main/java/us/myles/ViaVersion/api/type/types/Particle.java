package us.myles.ViaVersion.api.type.types;

import us.myles.ViaVersion.api.type.Type;

import java.util.LinkedList;
import java.util.List;

public class Particle {
    private int id;
    private List<ParticleData> arguments = new LinkedList<>();

    public Particle(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<ParticleData> getArguments() {
        return arguments;
    }

    public void setArguments(List<ParticleData> arguments) {
        this.arguments = arguments;
    }

    public static class ParticleData {
        private Type type;
        private Object value;

        public ParticleData(Type type, Object value) {
            this.type = type;
            this.value = value;
        }

        public Type getType() {
            return type;
        }

        public void setType(Type type) {
            this.type = type;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }
    }
}

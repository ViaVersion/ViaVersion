package us.myles.ViaVersion.api;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class Pair<X, Y> {
    private X key;
    private Y value;

    public Pair(X key, Y value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String toString() {
        return "Pair{" + key + ", " + value + '}';
    }
}

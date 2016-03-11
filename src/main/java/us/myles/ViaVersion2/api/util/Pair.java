package us.myles.ViaVersion2.api.util;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class Pair<X, Y> {
    private final X key;
    private final Y value;

    public Pair(X key, Y value){
        this.key = key;
        this.value = value;
    }
}

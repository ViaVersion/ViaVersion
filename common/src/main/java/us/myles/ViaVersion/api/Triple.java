package us.myles.ViaVersion.api;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Triple<A, B, C> {
    private A first;
    private B second;
    private C third;

    @Override
    public String toString() {
        return "Triple{" + first + ", " + second + ", " + third + '}';
    }
}

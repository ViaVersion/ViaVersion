package us.myles.ViaVersion.protocols.protocol1_9to1_8.chunk;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import us.myles.ViaVersion.api.minecraft.Position;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class Sign {
    private Position blockPosition;
    private String[] lines;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Sign sign = (Sign) o;

        return blockPosition.equals(sign.blockPosition);

    }

    @Override
    public int hashCode() {
        return blockPosition.hashCode();
    }
}

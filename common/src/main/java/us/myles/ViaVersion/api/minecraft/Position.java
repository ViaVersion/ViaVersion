package us.myles.ViaVersion.api.minecraft;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
@EqualsAndHashCode
public class Position {
    private Long x;
    private Long y;
    private Long z;

    public Position getRelative(BlockFace face) {
        return new Position(this.x + face.getModX(), this.y + face.getModY(), this.z + face.getModZ());
    }

    public Position shift(BlockFace face) {
        this.x += face.getModX();
        this.y += face.getModY();
        this.z += face.getModZ();
        return this;
    }
}

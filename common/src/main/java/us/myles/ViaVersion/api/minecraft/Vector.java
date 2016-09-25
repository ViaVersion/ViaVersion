package us.myles.ViaVersion.api.minecraft;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Vector {
    private int blockX;
    private int blockY;
    private int blockZ;
}

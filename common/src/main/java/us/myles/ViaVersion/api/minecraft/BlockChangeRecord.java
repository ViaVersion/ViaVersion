package us.myles.ViaVersion.api.minecraft;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BlockChangeRecord {
    private short horizontal;
    private short y;
    private int blockId;
}

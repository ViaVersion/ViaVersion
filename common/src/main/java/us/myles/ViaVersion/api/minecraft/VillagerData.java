package us.myles.ViaVersion.api.minecraft;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class VillagerData {
    private int type;
    private int profession;
    private int level;
}

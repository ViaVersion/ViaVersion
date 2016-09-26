package us.myles.ViaVersion.api.minecraft.metadata;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class Metadata {
    private int id;
    private MetaType metaType;
    private Object value;
}

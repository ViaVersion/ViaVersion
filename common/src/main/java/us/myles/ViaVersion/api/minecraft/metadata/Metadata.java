package us.myles.ViaVersion.api.minecraft.metadata;

import lombok.AllArgsConstructor;
import lombok.Data;
import us.myles.ViaVersion.api.type.Type;

@AllArgsConstructor
@Data
public class Metadata {
    private int id;
    private int typeID;
    private Type type;
    private Object value;
}

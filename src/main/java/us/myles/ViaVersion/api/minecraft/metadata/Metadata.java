package us.myles.ViaVersion.api.minecraft.metadata;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import us.myles.ViaVersion.api.type.Type;

@AllArgsConstructor
@Getter
@Setter
public class Metadata {
    private int id;
    private int typeID;
    private Type type;
    private Object value;

    @Override
    public String toString() {
        return "Metadata{" +
                "id=" + id +
                ", typeID=" + typeID +
                ", type=" + type +
                ", value=" + value +
                '}';
    }
}

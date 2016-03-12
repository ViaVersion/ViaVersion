package us.myles.ViaVersion2.api.metadata;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import us.myles.ViaVersion2.api.type.Type;

@AllArgsConstructor
@Getter
@Setter
public class Metadata {
    private final int id;
    private Type type;
    private Object value;
}

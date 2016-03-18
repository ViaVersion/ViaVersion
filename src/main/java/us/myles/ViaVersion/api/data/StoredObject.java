package us.myles.ViaVersion.api.data;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter(AccessLevel.PROTECTED)
@AllArgsConstructor
public class StoredObject {
    private UserConnection user;
}

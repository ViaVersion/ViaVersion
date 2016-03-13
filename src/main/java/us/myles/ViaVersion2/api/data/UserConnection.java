package us.myles.ViaVersion2.api.data;

import java.util.ArrayList;
import java.util.List;

public class UserConnection {
    List<StoredObject> storedObjects = new ArrayList<>();

    public <T extends StoredObject> T get(Class<T> objectClass) {
        for (StoredObject o : storedObjects) {
            if (o.getClass().equals(objectClass))
                return (T) o;
        }
        return null;
    }

    public <T extends StoredObject> boolean has(Class<T> objectClass) {
        for (StoredObject o : storedObjects) {
            if (o.getClass().equals(objectClass))
                return true;
        }
        return false;
    }

    public void put(StoredObject object) {
        storedObjects.add(object);
    }
}

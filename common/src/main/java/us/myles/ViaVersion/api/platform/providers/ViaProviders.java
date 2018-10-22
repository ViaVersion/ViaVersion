package us.myles.ViaVersion.api.platform.providers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViaProviders {
    private final Map<Class<? extends Provider>, Provider> providers = new HashMap<>();
    private final List<Class<? extends Provider>> lonelyProviders = new ArrayList<>();

    public void require(Class<? extends Provider> provider) {
        lonelyProviders.add(provider);
    }

    public <T extends Provider> void register(Class<T> provider, T value) {
        providers.put(provider, value);
    }

    public <T extends Provider> void use(Class<T> provider, T value) {
        lonelyProviders.remove(provider);
        providers.put(provider, value);
    }

    public <T extends Provider> T get(Class<T> provider) {
        Provider rawProvider = providers.get(provider);
        if (rawProvider != null) {
            return (T) rawProvider;
        } else {
            if (lonelyProviders.contains(provider)) {
                throw new IllegalStateException("There was no provider for " + provider + ", one is required!");
            }
            return null;
        }
    }
}

package us.myles.ViaVersion.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class GsonUtil {
    private static final Gson gson = getGsonBuilder().create();

    /**
     * Get google's Gson magic
     *
     * @return Gson instance
     */
    public static Gson getGson() {
        return gson;
    }

    /**
     * Get the GsonBuilder in case you want to add other stuff
     *
     * @return GsonBuilder instance
     */
    public static GsonBuilder getGsonBuilder() {
        return new GsonBuilder();
    }
}

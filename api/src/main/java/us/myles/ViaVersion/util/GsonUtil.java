package us.myles.ViaVersion.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;

public final class GsonUtil {
    private static final JsonParser JSON_PARSER = new JsonParser();
    private static final Gson GSON = getGsonBuilder().create();

    /**
     * Get google's Gson magic
     *
     * @return Gson instance
     */
    public static Gson getGson() {
        return GSON;
    }

    /**
     * Get the GsonBuilder in case you want to add other stuff
     *
     * @return GsonBuilder instance
     */
    public static GsonBuilder getGsonBuilder() {
        return new GsonBuilder();
    }

    public static JsonParser getJsonParser() {
        return JSON_PARSER;
    }
}

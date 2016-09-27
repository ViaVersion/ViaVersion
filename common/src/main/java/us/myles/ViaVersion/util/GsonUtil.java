package us.myles.ViaVersion.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import lombok.experimental.UtilityClass;

@UtilityClass
@Getter
public class GsonUtil {
    private final Gson gson = getGsonBuilder().create();

    /**
     * Get google's Gson magic
     *
     * @return Gson instance
     */
    public Gson getGson() {
        return gson;
    }

    /**
     * Get the GsonBuilder in case you want to add other stuff
     *
     * @return GsonBuilder instance
     */
    public GsonBuilder getGsonBuilder() {
        return new GsonBuilder();
    }

}

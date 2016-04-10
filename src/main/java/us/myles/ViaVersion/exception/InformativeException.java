package us.myles.ViaVersion.exception;

import java.util.HashMap;
import java.util.Map;

public class InformativeException extends Exception {
    Map<String, Object> info = new HashMap<>();
    int sources = 0;

    public InformativeException(Throwable cause) {
        super(cause);
    }

    public InformativeException set(String key, Object value) {
        info.put(key, value);
        return this;
    }

    public InformativeException addSource(Class<?> sourceClazz) {
        return set("Source #" + sources++, getSource(sourceClazz));
    }

    private String getSource(Class<?> sourceClazz) {
        if (sourceClazz.isAnonymousClass()) {
            return sourceClazz.getName() + " (Anonymous)";
        } else {
            return sourceClazz.getName();
        }
    }

    @Override
    public String getMessage() {
        StringBuilder builder = new StringBuilder();
        builder.append("Please post this error to http://github.com/MylesIsCool/ViaVersion/issues\n{");
        int i = 0;
        for (Map.Entry<String, Object> entry : info.entrySet()) {
            builder.append((i == 0 ? "" : ", ") + entry.getKey() + ": " + entry.getValue().toString());
            i++;
        }
        builder.append("}\nActual Error: ");

        return builder.toString();
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        // Don't record this stack
        return this;
    }
}

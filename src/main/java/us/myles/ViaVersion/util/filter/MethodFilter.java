package us.myles.ViaVersion.util.filter;

import java.lang.reflect.Method;

/**
 * Created by Florian on 08.04.16 in us.myles.ViaVersion.util
 */
public abstract class MethodFilter extends IFilter<Method> {
    @Override
    public abstract boolean filter(Method method, Object in) throws Exception;

    @Override
    public String type() {
        return "Method";
    }

}

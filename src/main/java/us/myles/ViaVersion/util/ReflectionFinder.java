package us.myles.ViaVersion.util;

import java.lang.annotation.ElementType;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Logger;

/**
 * Created by Florian on 03.04.16 in us.myles.ViaVersion.util
 */
public abstract class ReflectionFinder<T> {

    Logger logger = null;
    String nomatch = null;

    public <R> R find(Object in, Class<?> c, ElementType et) throws Exception {
        if (et != ElementType.METHOD && et != ElementType.FIELD) throw new IllegalArgumentException("ReflectionFinder can't search for "+et.name());
        R r = null;
        for (AccessibleObject ao : (et == ElementType.METHOD ? c.getDeclaredMethods() : c.getDeclaredFields())) {
            ao.setAccessible(true);
            if (!filter((T)ao)) continue;
            iterate((T)ao);
            r = (R) (et == ElementType.METHOD ? ((Method)ao).invoke(in) : ((Field)ao).get(in));
        }
        if (nomatch != null && r == null && logger != null) logger.warning(nomatch);
        return r;
    }

    public ReflectionFinder noMatch(Logger logger, String s) {
        this.nomatch = s;
        this.logger = logger;
        return this;
    }

    public abstract boolean filter(T obj) throws Exception;

    public abstract void iterate(T match) throws Exception;

}

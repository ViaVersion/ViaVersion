/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2024 ViaVersion and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.viaversion.viaversion.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ReflectionUtil {

    public static Object invokeStatic(Class<?> clazz, String method) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method m = clazz.getDeclaredMethod(method);
        return m.invoke(null);
    }

    public static Object invoke(Object o, String method) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method m = o.getClass().getDeclaredMethod(method);
        return m.invoke(o);
    }

    public static <T> T getStatic(Class<?> clazz, String f, Class<T> type) throws NoSuchFieldException, IllegalAccessException {
        Field field = clazz.getDeclaredField(f);
        field.setAccessible(true);
        return type.cast(field.get(null));
    }

    public static void setStatic(Class<?> clazz, String f, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field field = clazz.getDeclaredField(f);
        field.setAccessible(true);
        field.set(null, value);
    }

    public static <T> T getSuper(Object o, String f, Class<T> type) throws NoSuchFieldException, IllegalAccessException {
        Field field = o.getClass().getSuperclass().getDeclaredField(f);
        field.setAccessible(true);
        return type.cast(field.get(o));
    }

    public static <T> T get(Object instance, Class<?> clazz, String f, Class<T> type) throws NoSuchFieldException, IllegalAccessException {
        Field field = clazz.getDeclaredField(f);
        field.setAccessible(true);
        return type.cast(field.get(instance));
    }

    public static <T> T get(Object o, String f, Class<T> type) throws NoSuchFieldException, IllegalAccessException {
        Field field = o.getClass().getDeclaredField(f);
        field.setAccessible(true);
        return type.cast(field.get(o));
    }

    public static <T> T getPublic(Object o, String f, Class<T> type) throws NoSuchFieldException, IllegalAccessException {
        Field field = o.getClass().getField(f);
        field.setAccessible(true);
        return type.cast(field.get(o));
    }

    public static void set(Object o, String f, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field field = o.getClass().getDeclaredField(f);
        field.setAccessible(true);
        field.set(o, value);
    }

    public static final class ClassReflection {
        private final Class<?> handle;
        private final Map<String, Field> fields = new ConcurrentHashMap<>();
        private final Map<String, Method> methods = new ConcurrentHashMap<>();

        public ClassReflection(Class<?> handle) {
            this(handle, true);
        }

        public ClassReflection(Class<?> handle, boolean recursive) {
            this.handle = handle;
            scanFields(handle, recursive);
            scanMethods(handle, recursive);
        }

        private void scanFields(Class<?> host, boolean recursive) {
            if (recursive && host.getSuperclass() != null && host.getSuperclass() != Object.class) {
                scanFields(host.getSuperclass(), true);
            }

            for (Field field : host.getDeclaredFields()) {
                field.setAccessible(true);
                fields.put(field.getName(), field);
            }
        }

        private void scanMethods(Class<?> host, boolean recursive) {
            if (recursive && host.getSuperclass() != null && host.getSuperclass() != Object.class) {
                scanMethods(host.getSuperclass(), true);
            }

            for (Method method : host.getDeclaredMethods()) {
                method.setAccessible(true);
                methods.put(method.getName(), method);
            }
        }

        public Object newInstance() throws ReflectiveOperationException {
            return handle.getConstructor().newInstance();
        }

        public Field getField(String name) {
            return fields.get(name);
        }

        public void setFieldValue(String fieldName, Object instance, Object value) throws IllegalAccessException {
            getField(fieldName).set(instance, value);
        }

        public <T> T getFieldValue(String fieldName, Object instance, Class<T> type) throws IllegalAccessException {
            return type.cast(getField(fieldName).get(instance));
        }

        public <T> T invokeMethod(Class<T> type, String methodName, Object instance, Object... args) throws InvocationTargetException, IllegalAccessException {
            return type.cast(getMethod(methodName).invoke(instance, args));
        }

        public Method getMethod(String name) {
            return methods.get(name);
        }

        public Collection<Field> getFields() {
            return Collections.unmodifiableCollection(fields.values());
        }

        public Collection<Method> getMethods() {
            return Collections.unmodifiableCollection(methods.values());
        }
    }
}

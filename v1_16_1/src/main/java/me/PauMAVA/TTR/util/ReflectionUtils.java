/*
 * TheTowersRemastered (TTR)
 * Copyright (c) 2019-2021  Pau Machetti Vallverdú
 * [Licencia...]
 */

package me.PauMAVA.TTR.util;

import org.bukkit.Bukkit;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public class ReflectionUtils {
    private static String version = "";

    static {
        try {
            String pkg = Bukkit.getServer().getClass().getPackage().getName();
            String[] parts = pkg.split("\\.");
            if (parts.length > 3) {
                version = parts[3];
            }
        } catch (Exception e) {
        }
    }



    public static void setField(Class<?> clazz, Object instance, String fieldName, Object value) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            boolean accessible = field.canAccess(instance);
            field.setAccessible(true);
            field.set(instance, value);
            field.setAccessible(accessible);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T callMethod(Object instance, String name, List<Class<?>> paramTypes, List<Object> parameters, Class<T> returnValue) {
        try {
            Method method = instance.getClass().getDeclaredMethod(name, paramTypes.toArray(new Class[0]));
            method.setAccessible(true);
            return (T) method.invoke(instance, parameters.toArray());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public static Class<?> getNMSClass(String clazz) { return null; }
    public static Class<?> getCraftbukkitClass(String clazz) { return null; }

}
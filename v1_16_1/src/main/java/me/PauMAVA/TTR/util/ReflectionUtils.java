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

    // En versiones modernas (1.21), ya no usamos versiones en el nombre del paquete (v1_XX_R1).
    // Mantenemos la variable solo para evitar errores si otro archivo la llama.
    private static String version = "";

    static {
        try {
            String pkg = Bukkit.getServer().getClass().getPackage().getName();
            String[] parts = pkg.split("\\.");
            if (parts.length > 3) {
                version = parts[3];
            }
        } catch (Exception e) {
            // Ignoramos errores de versión ya que no usamos NMS
        }
    }

    /*
     * -------------------------------------------------------------------------
     * LIMPIEZA PARA MINECRAFT 1.21
     * Se han eliminado los métodos: getPlayerConnection, getPlayerChannel,
     * sendNMSPacketToPlayer y createNMSInstance.
     *
     * CAUSA: Usaban librerías (Netty) y nombres (NMS) que ya no existen.
     * SOLUCIÓN: Los archivos que usaban esto (CustomTab y PacketInterceptor)
     * ya fueron actualizados para usar la API oficial de Spigot.
     * -------------------------------------------------------------------------
     */

    // Mantenemos estos métodos genéricos por si alguna parte del plugin los usa
    // para cosas simples (no relacionadas con NMS).

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
package ovo.xsvf.izmk.injection;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Type;
import ovo.xsvf.izmk.misc.Constants;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.HashMap;

public class ReflectionUtil implements Constants {
    private static final HashMap<String, Field> cachedFields = new HashMap<>();
    private static final HashMap<String, Class<?>> cachedClasses = new HashMap<>();
    private static final @NotNull Unsafe unsafe;

    static {
        try {
            Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            unsafe = (Unsafe) unsafeField.get(null);
        } catch (Exception ex) {
            logger.error("Failed to obtain Unsafe instance: %s", ex);
            throw new RuntimeException("Failed to obtain Unsafe instance", ex);
        }
    }

    private static Field getCachedField(@NotNull Class<?> clazz, @NotNull String field) {
        String key = Type.getInternalName(clazz) + "/" + field;
        return cachedFields.get(key);
    }

    private static Field getField(@NotNull Class<?> clazz, @NotNull String field) throws NoSuchFieldException {
        String key = Type.getInternalName(clazz) + "/" + field;
        Field privField = getCachedField(clazz, field);
        if (privField != null) {
            return privField;
        }

        try {
            privField = clazz.getDeclaredField(field);
            privField.setAccessible(true);
            cachedFields.put(key, privField);
            logger.debug("Cached field: %s", key);
        } catch (NoSuchFieldException | SecurityException e) {
            logger.error("Error occurred while finding or accessing field %s in class %s: %s", e, field, clazz.getName());
            throw e;
        }

        return privField;
    }

    public static Object getField(@NotNull Object instance, @NotNull String field, @NotNull Class<?> clazz) {
        try {
            Field privField = getField(clazz, field);
            return privField.get(instance);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            logger.error("Error occurred while accessing field %s in class %s: %s", e, field, clazz.getName());
            throw new RuntimeException("Failed to access field " + field + " in class " + clazz.getName(), e);
        }
    }

    public static void setField(@NotNull Object instance, @NotNull Object value, @NotNull String field, @NotNull Class<?> clazz) {
        try {
            Field privField = getField(clazz, field);
            privField.set(instance, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            logger.error("Error occurred while setting field %s in class %s: %s", e, field, clazz.getName());
            throw new RuntimeException("Failed to set field " + field + " in class " + clazz.getName(), e);
        }
    }

    public static void setFieldStatic(@NotNull Object value, @NotNull String field, @NotNull Class<?> clazz) {
        try {
            Field privField = getField(clazz, field);
            privField.set(null, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            logger.error("Error occurred while setting static field %s in class %s: %s", e, field, clazz.getName());
            throw new RuntimeException("Failed to set static field " + field + " in class " + clazz.getName(), e);
        }
    }

    public static void setFieldFinal(@NotNull Object instance, @NotNull Object value, @NotNull String field) {
        Class<?> clazz = instance.getClass();
        try {
            Field privField = getField(clazz, field);
            unsafe.putObject(instance, unsafe.objectFieldOffset(privField), value);
        } catch (NoSuchFieldException e) {
            logger.error("Failed to map final field %s in class %s: %s", e, field, clazz.getName());
            throw new RuntimeException("Failed to map final field " + field + " in class " + clazz.getName(), e);
        }
    }

    public static void setFieldFinalStatic(@NotNull Object value, @NotNull Class<?> clazz, @NotNull String field) {
        try {
            Field privField = getField(clazz, field);
            unsafe.putObject(unsafe.staticFieldBase(privField), unsafe.staticFieldOffset(privField), value);
        } catch (NoSuchFieldException e) {
            logger.error("Failed to map final static field %s in class %s: %s", e, field, clazz.getName());
            throw new RuntimeException("Failed to map final static field " + field + " in class " + clazz.getName(), e);
        }
    }

    public static Class<?> forName(String className) {
        if (cachedClasses.containsKey(className)) {
            return cachedClasses.get(className);
        }

        try {
            Class<?> clazz = Class.forName(className.replace("/", "."));
            cachedClasses.put(className, clazz);
            logger.debug("Cached class: %s", className);
            return clazz;
        } catch (ClassNotFoundException e) {
            logger.error("Failed to obtain class %s: %s", e, className);
            throw new RuntimeException("Failed to obtain class " + className, e);
        }
    }

    public static void setClassModule(Class<?> clazz, Module module) throws NoSuchFieldException {
        unsafe.getAndSetObject(clazz, unsafe.objectFieldOffset(Class.class.getDeclaredField("module")), module);
    }
}

package ovo.xsvf.izmk.injection;

import org.objectweb.asm.Type;
import ovo.xsvf.izmk.IZMK;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.HashMap;

public class ReflectionUtil {
    private static final HashMap<String, Field> cachedFields = new HashMap<>();
    private static final HashMap<String, Class<?>> cachedClasses = new HashMap<>();
    private static final Unsafe unsafe;

    static {
        try {
            Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            unsafe = (Unsafe) unsafeField.get(null);
        } catch (Exception ex) {
            IZMK.INSTANCE.getLogger().error("Failed to obtain Unsafe INSTANCE: {}", ex);
            throw new RuntimeException("Failed to obtain Unsafe INSTANCE", ex);
        }
    }

    private static Field getCachedField(Class<?> clazz, String field) {
        String key = Type.getInternalName(clazz) + "/" + field;
        return cachedFields.get(key);
    }

    private static Field getField(Class<?> clazz, String field) throws NoSuchFieldException {
        String key = Type.getInternalName(clazz) + "/" + field;
        Field privField = getCachedField(clazz, field);
        if (privField != null) {
            return privField;
        }

        try {
            privField = clazz.getDeclaredField(field);
            privField.setAccessible(true);
            cachedFields.put(key, privField);
            IZMK.INSTANCE.getLogger().debug("Cached field: {}", key);
        } catch (NoSuchFieldException | SecurityException e) {
            IZMK.INSTANCE.getLogger().error("Error occurred while finding or accessing field {} in class {}: {}", e, field, clazz.getName());
            throw e;
        }

        return privField;
    }

    public static Object getField(Object instance, String field, String className) {
        try {
            Class<?> clazz = forName(className);
            Field privField = getField(clazz, field);
            return privField.get(instance);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            IZMK.INSTANCE.getLogger().error("Error occurred while accessing field {} in class {}: {}", e, field, className);
            throw new RuntimeException("Failed to access field " + field + " in class " + className, e);
        }
    }

    public static void setField(Object instance, Object value, String field, String className) {
        try {
            Class<?> clazz = forName(className);
            Field privField = getField(clazz, field);
            privField.set(instance, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            IZMK.INSTANCE.getLogger().error("Error occurred while setting field {} in class {}: {}", e, field, className);
            throw new RuntimeException("Failed to set field " + field + " in class " + className, e);
        }
    }

    public static void setFieldStatic(Object value, String field, String className) {
        try {
            Class<?> clazz = forName(className);
            Field privField = getField(clazz, field);
            privField.set(null, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            IZMK.INSTANCE.getLogger().error("Error occurred while setting static field {} in class {}: {}", e, field, className);
            throw new RuntimeException("Failed to set static field " + field + " in class " + className, e);
        }
    }

    public static void setFieldFinal(Object instance, Object value, String field) {
        Class<?> clazz = instance.getClass();
        try {
            Field privField = getField(clazz, field);
            unsafe.putObject(instance, unsafe.objectFieldOffset(privField), value);
        } catch (NoSuchFieldException e) {
            IZMK.INSTANCE.getLogger().error("Failed to map final field {} in class {}: {}", e, field, clazz.getName());
            throw new RuntimeException("Failed to map final field " + field + " in class " + clazz.getName(), e);
        }
    }

    public static void setFieldFinalStatic(Object value, String field, String className) {
        Class<?> clazz = forName(className);
        try {
            Field privField = getField(clazz, field);
            unsafe.putObject(unsafe.staticFieldBase(privField), unsafe.staticFieldOffset(privField), value);
        } catch (NoSuchFieldException e) {
            IZMK.INSTANCE.getLogger().error("Failed to map final static field {} in class {}: {}", e, field, clazz.getName());
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
            IZMK.INSTANCE.getLogger().debug("Cached class: {}", className);
            return clazz;
        } catch (ClassNotFoundException e) {
            IZMK.INSTANCE.getLogger().error("Failed to find class {}: {}", e, className);
            throw new RuntimeException("Failed to find class " + className, e);
        }
    }

    public static void setClassModule(Class<?> clazz, Module module) throws NoSuchFieldException {
        unsafe.getAndSetObject(clazz, unsafe.objectFieldOffset(Class.class.getDeclaredField("module")), module);
    }
}

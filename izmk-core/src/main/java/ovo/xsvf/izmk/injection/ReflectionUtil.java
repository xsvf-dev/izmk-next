package ovo.xsvf.izmk.injection;

import org.objectweb.asm.Type;
import ovo.xsvf.izmk.IZMK;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.util.HashMap;

public class ReflectionUtil {
    private static final HashMap<String, Field> cachedFields = new HashMap<>();
    private static final HashMap<String, Class<?>> cachedClasses = new HashMap<>();

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
            IZMK.INSTANCE.getLogger().error("Error occurred while finding or accessing field {} in class {}: {}", field, clazz.getName(), e);
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
            IZMK.INSTANCE.getLogger().error("Error occurred while accessing field {} in class {}: {}", field, className, e);
            throw new RuntimeException("Failed to access field " + field + " in class " + className, e);
        }
    }

    public static void setField(Object instance, Object value, String field, String className) {
        try {
            Class<?> clazz = forName(className);
            Field privField = getField(clazz, field);
            privField.set(instance, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            IZMK.INSTANCE.getLogger().error("Error occurred while setting field {} in class {}: {}", field, className, e);
            throw new RuntimeException("Failed to set field " + field + " in class " + className, e);
        }
    }

    public static void setFieldStatic(Object value, String field, String className) {
        try {
            Class<?> clazz = forName(className);
            Field privField = getField(clazz, field);

            VarHandle varHandle = MethodHandles.privateLookupIn(clazz, MethodHandles.lookup())
                    .unreflectVarHandle(privField);
            varHandle.set(value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            IZMK.INSTANCE.getLogger().error("Error occurred while setting static field {} in class {}: {}", field, className, e);
            throw new RuntimeException("Failed to set static field " + field + " in class " + className, e);
        }
    }

    public static void setFieldFinal(Object instance, Object value, String field) {
        Class<?> clazz = instance.getClass();
        try {
            Field privField = getField(clazz, field);

            VarHandle varHandle = MethodHandles.privateLookupIn(clazz, MethodHandles.lookup())
                    .unreflectVarHandle(privField);
            varHandle.set(instance, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            IZMK.INSTANCE.getLogger().error("Failed to map final field {} in class {}: {}", field, clazz.getName(), e);
            throw new RuntimeException("Failed to map final field " + field + " in class " + clazz.getName(), e);
        }
    }

    public static void setFieldFinalStatic(Object value, String field, String className) {
        Class<?> clazz = forName(className);
        try {
            Field privField = getField(clazz, field);

            VarHandle varHandle = MethodHandles.privateLookupIn(clazz, MethodHandles.lookup())
                    .unreflectVarHandle(privField);
            varHandle.set(value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            IZMK.INSTANCE.getLogger().error("Failed to map final static field {} in class {}: {}", field, clazz.getName(), e);
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
            IZMK.INSTANCE.getLogger().error("Failed to find class {}: {}", className, e);
            throw new RuntimeException("Failed to find class " + className, e);
        }
    }

    public static void setClassModule(Class<?> clazz, Module module) throws NoSuchFieldException, IllegalAccessException {
        Field moduleField = Class.class.getDeclaredField("module");
        moduleField.setAccessible(true);

        VarHandle varHandle = MethodHandles.privateLookupIn(Class.class, MethodHandles.lookup())
                .unreflectVarHandle(moduleField);
        varHandle.set(clazz, module);
    }
}

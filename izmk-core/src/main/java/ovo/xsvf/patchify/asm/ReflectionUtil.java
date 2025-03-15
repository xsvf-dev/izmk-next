package ovo.xsvf.patchify.asm;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.client.Minecraft;
import sun.misc.Unsafe;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;

@SuppressWarnings("deprecation")
public final class ReflectionUtil {
    private static final MethodHandles.Lookup publicLookup = MethodHandles.lookup();

    private static final Object2ObjectMap<String, MethodHandles.Lookup> cachedLookups = new Object2ObjectOpenHashMap<>(500);
    private static final Object2ObjectMap<String, Class<?>> cachedClasses = new Object2ObjectOpenHashMap<>(500);
    private static final Object2ObjectMap<String, VarHandle> cachedVarHandles = new Object2ObjectOpenHashMap<>(1000);

    private static final Object2LongMap<String> cachedFieldOffsets = new Object2LongRBTreeMap<>();
    private static final Object2ObjectMap<String, ObjectLongPair<Object>> cachedStaticFieldOffsets = new Object2ObjectOpenHashMap<>();

    private static final Unsafe unsafe;
    static {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            unsafe = (Unsafe) field.get(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static long getFieldOffset(Class<?> clazz, String name) {
        String key = clazz.getName()+ "/" + name;
        Long l = cachedFieldOffsets.get(key);
        if (l != null) return l;

        try {
            long offset;
            offset = unsafe.objectFieldOffset(clazz.getDeclaredField(name));

            cachedFieldOffsets.put(key, offset);
            return offset;
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    private static ObjectLongPair<Object> getStaticFieldOffset(Class<?> clazz, String name) {
        String key = clazz.getName()+ "/" + name;
        ObjectLongPair<Object> pair = cachedStaticFieldOffsets.get(key);
        if (pair != null) return pair;

        try {
            Field field = clazz.getDeclaredField(name);

            ObjectLongPair<Object> value = new ObjectLongImmutablePair<>(unsafe.staticFieldBase(field), unsafe.staticFieldOffset(field));
            cachedStaticFieldOffsets.put(key, value);
            return value;
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    private static MethodHandles.Lookup lookup(Class<?> clazz, String className) {
        MethodHandles.Lookup cache = cachedLookups.get(className);
        if (cache != null) return cache;
        try {
            MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(clazz, publicLookup);
            cachedLookups.put(className, lookup);
            return lookup;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static VarHandle getVarHandle(Class<?> clazz, String name, String className) {
        String key = className + "/" + name;
        VarHandle varHandle = cachedVarHandles.get(key);
        if (varHandle != null) return varHandle;
        try {
            VarHandle handle = lookup(clazz, className)
                    .unreflectVarHandle(clazz.getDeclaredField(name));
            handle.accessModeType(VarHandle.AccessMode.GET_AND_SET);
            cachedVarHandles.put(key, handle);
            return handle;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object getField(Object instance, String field, String className) {
        Class<?> clazz = forName(className);
        if (instance == null) {
            ObjectLongPair<Object> pair = getStaticFieldOffset(clazz, field);
            return unsafe.getObject(pair.first(), pair.second());
        } else {
            return getVarHandle(clazz, field, className).get(instance);
        }
    }

    public static void setField(Object instance, Object value, String field, String className) {
        Class<?> clazz = forName(className);
        unsafe.putObject(instance, getFieldOffset(clazz, field), value);
    }

    public static void setFieldStatic(Object value, String field, String className) {
        Class<?> clazz = forName(className);
        Pair<Object, Long> pair = getStaticFieldOffset(clazz, field);
        unsafe.putObject(pair.first(), pair.second(), value);
    }

    public static void setFieldFinal(Object instance, Object value, String field) {
        Class<?> clazz = instance.getClass();
        unsafe.putObject(instance, getFieldOffset(clazz, field), value);
    }

    public static void setFieldFinalStatic(Object value, String field, String className) {
        Class<?> clazz = forName(className);
        Pair<Object, Long> pair = getStaticFieldOffset(clazz, field);
        unsafe.putObject(pair.first(), pair.second(), value);
    }

    public static Class<?> forName(String className) {
        Class<?> cache = cachedClasses.get(className);
        if (cache != null) return cache;
        try {
            Class<?> clazz = Class.forName(className.replace("/", "."));
            cachedClasses.put(className, clazz);
            return clazz;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
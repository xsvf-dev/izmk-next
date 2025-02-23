package ovo.xsvf.izmk.misc;

import it.unimi.dsi.fastutil.Pair;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import ovo.xsvf.izmk.IZMK;

import java.lang.instrument.ClassDefinition;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import java.util.HashMap;

public class ClassUtil implements Opcodes {
    private static final HashMap<Class<?>, byte[]> classMap = new HashMap<>();
    private static final HashMap<Class<?>, byte[]> modifiedClasses = new HashMap<>();
    private static final HashMap<Pair<String,String>, Method> cachedMethods = new HashMap<>();
    private static Instrumentation instrumentation;

    public static Instrumentation getInstrumentation() {
        return instrumentation;
    }

    public static byte[] getClassBytes(Class<?> clazz) {
        retransformClass(clazz);
        return classMap.get(clazz);
    }

    public static void retransformClass(Class<?> clazz) {
        try {
            instrumentation.retransformClasses(clazz);
        } catch (Exception e) {
            IZMK.INSTANCE.getLogger().error(e);
            throw new RuntimeException(e);
        }
    }

    public static void redefineClass(@NotNull Class<?> clazz, byte @NotNull [] bytes, boolean cache) {
        try {
            if (cache) {
                modifiedClasses.put(clazz, getClassBytes(clazz));
            }
            instrumentation.redefineClasses(new ClassDefinition(clazz, bytes));
        } catch (Exception e) {
            IZMK.INSTANCE.getLogger().error(e);
            throw new RuntimeException(e);
        }
    }

    public static void redefineClass(@NotNull Class<?> clazz, byte @NotNull [] bytes) {
        redefineClass(clazz, bytes, true);
    }

    public static void selfDestruct() {
        modifiedClasses.forEach((clazz, bytes) -> {
            try {
                instrumentation.redefineClasses(new ClassDefinition(clazz, bytes));
                IZMK.INSTANCE.getLogger().info("Restored class {}", clazz);
            } catch (ClassNotFoundException | UnmodifiableClassException ignored) {}
        });
        System.gc();
    }

    public static void init(@NotNull Instrumentation inst) {
        instrumentation = inst;
        inst.addTransformer(new Transformer(), true);
    }

    private static class Transformer implements ClassFileTransformer {
        @Override
        public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                                ProtectionDomain protectionDomain, byte[] classfileBuffer) {
            classMap.put(classBeingRedefined, classfileBuffer);
            return null;
        }
    }
}

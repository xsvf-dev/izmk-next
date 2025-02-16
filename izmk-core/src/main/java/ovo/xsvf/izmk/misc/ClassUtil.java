package ovo.xsvf.izmk.misc;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import ovo.xsvf.izmk.IZMK;
import ovo.xsvf.Pair;

import java.lang.instrument.ClassDefinition;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import java.util.HashMap;

public class ClassUtil implements Opcodes, Constants {
    private static final HashMap<Class<?>, byte[]> classMap = new HashMap<>();
    private static final HashMap<Class<?>, byte[]> modifiedClasses = new HashMap<>();
    private static final HashMap<Pair<String,String>, Method> cachedMethods = new HashMap<>();
    @Getter private static Instrumentation ins;

    public static byte[] getClassBytes(Class<?> clazz) {
        retransformClass(clazz);
        return classMap.get(clazz);
    }

    public static void retransformClass(Class<?> clazz) {
        try {
            ins.retransformClasses(clazz);
        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException(e);
        }
    }

    public static void redefineClass(@NotNull Class<?> clazz, byte @NotNull [] bytes, boolean cache) {
        try {
            if (cache) {
                modifiedClasses.put(clazz, getClassBytes(clazz));
            }
            ins.redefineClasses(new ClassDefinition(clazz, bytes));
        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException(e);
        }
    }

    public static void redefineClass(@NotNull Class<?> clazz, byte @NotNull [] bytes) {
        redefineClass(clazz, bytes, true);
    }

    public static @NotNull ClassNode node(byte @NotNull [] bytes) {
        ClassReader cr = new ClassReader(bytes);
        ClassNode node = new ClassNode();
        cr.accept(node, ClassReader.EXPAND_FRAMES | ClassReader.SKIP_DEBUG);
        return node;
    }

    public static byte[] rewriteClass(@NotNull ClassNode node) {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES) {
            @Override
            protected ClassLoader getClassLoader() {
                return Thread.currentThread().getContextClassLoader();
            }
        };
        node.accept(cw);
        return cw.toByteArray();
    }

    public static void selfDestruct() {
        modifiedClasses.forEach((clazz, bytes) -> {
            try {
                ins.redefineClasses(new ClassDefinition(clazz, bytes));
                IZMK.logger.info("Restored class %s", clazz);
            } catch (ClassNotFoundException | UnmodifiableClassException ignored) {}
        });
        System.gc();
    }

    public static void init(@NotNull Instrumentation inst) {
        ins = inst;
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

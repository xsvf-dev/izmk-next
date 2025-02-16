package ovo.xsvf;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import ovo.xsvf.logging.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;

public class ClassAnalyzer {
    private final HashMap<String, byte[]> classMap = new HashMap<>();
    private final HashMap<ClassNode, List<String>> depCache = new HashMap<>();
    private final BiFunction<String, byte[], Class<?>> defineMethod;
    private final Logger logger;

    public ClassAnalyzer(BiFunction<String, byte[], Class<?>> callback, Logger logger) {
        this.defineMethod = callback;
        this.logger = logger;
    }

    public List<Class<?>> loadClasses(List<byte[]> classBytes) {
        LinkedList<String> previous = new LinkedList<>();
        LinkedList<String> classes = new LinkedList<>();
        classBytes.forEach(it -> {
            ClassNode node = ASMUtil.node(it);
            if ((node.access & Opcodes.ACC_MODULE) != 0) {
                return;
            }
            String name = node.name;
            classMap.put(name, it);
            classes.add(name);
        });

        do {
            previous.clear();
            previous.addAll(classes);
            for (int i = 0; i < classes.size(); i++) {
                List<String> deps = resolveDeps(classes.get(i));
                final int finalI = i; AtomicReference<Integer> size = new AtomicReference<>(0);
                deps.forEach(dep -> {
                    if (classMap.containsKey(dep)) {
                        classes.add(finalI, dep);
                        size.getAndSet(size.get() + 1);
                    }
                });
                i += size.get();
            }
            List<String> distinctList = distinct(classes);
            classes.clear();
            classes.addAll(distinctList);
        } while (!previous.equals(classes));

        final List<Class<?>> newClasses = new ArrayList<>();
        classes.forEach(className -> {
            try {
                try {
                    Class.forName(className.replace("/", "."), false,
                            Thread.currentThread().getContextClassLoader());
                } catch (ClassNotFoundException e) {
                    newClasses.add(defineMethod.apply(className, classMap.get(className)));
                }
            } catch (Exception e) {
                logger.error(e);
            }
        });
        return newClasses;
    }

    private List<String> resolveDeps(ClassNode node) {
        if (!depCache.containsKey(node)) {
            List<String> deps = new ArrayList<>();
            if (node.interfaces != null) deps.addAll(node.interfaces);
            if (node.superName != null) deps.add(node.superName);
            if (node.visibleAnnotations != null) {
                node.visibleAnnotations.forEach(it -> deps.add(ASMUtil.fromDesc(it.desc)));
            }
            if (node.invisibleAnnotations != null) {
                node.invisibleAnnotations.forEach(it -> deps.add(ASMUtil.fromDesc(it.desc)));
            }
            depCache.put(node, deps);
            return deps;
        } else return depCache.get(node);
    }

    private List<String> resolveDeps(String clz) {
        return resolveDeps(ASMUtil.node(classMap.get(clz)));
    }

    private <T> List<T> distinct(List<T> list) {
        List<T> list2 = new ArrayList<>();
        list.forEach(it -> {if (!list2.contains(it)) list2.add(it);});
        return list2;
    }
}
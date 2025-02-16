package ovo.xsvf;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import ovo.xsvf.logging.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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
        // Step 1: Populate classMap with filtered classes
        classBytes.forEach(it -> {
            ClassNode node = ASMUtil.node(it);
            if ((node.access & Opcodes.ACC_MODULE) != 0 || node.name.startsWith("kotlin/")) {
                return;
            }
            String name = node.name;
            classMap.put(name, it);
        });

        // Step 2: Build dependency graph and perform topological sort
        Map<String, List<String>> adjacency = new HashMap<>();
        Map<String, Integer> inDegree = new ConcurrentHashMap<>();

        // Initialize adjacency list and in-degree map
        classMap.keySet().forEach(className -> {
            adjacency.put(className, new ArrayList<>());
            inDegree.put(className, 0);
        });

        // Populate adjacency and in-degree based on dependencies
        classMap.forEach((className, bytes) -> {
            ClassNode node = ASMUtil.node(bytes);
            List<String> deps = resolveDeps(node);
            deps.forEach(dep -> {
                if (classMap.containsKey(dep)) {
                    adjacency.get(dep).add(className); // Edge from dep to className
                    inDegree.put(className, inDegree.get(className) + 1);
                }
            });
        });

        // Kahn's algorithm for topological sorting
        Queue<String> queue = new LinkedList<>();
        inDegree.forEach((className, degree) -> {
            if (degree == 0) {
                queue.offer(className);
            }
        });

        List<String> sortedClasses = new ArrayList<>();
        while (!queue.isEmpty()) {
            String current = queue.poll();
            sortedClasses.add(current);
            for (String dependent : adjacency.get(current)) {
                int updatedDegree = inDegree.get(dependent) - 1;
                inDegree.put(dependent, updatedDegree);
                if (updatedDegree == 0) {
                    queue.offer(dependent);
                }
            }
        }

        // Check for cycles
        if (sortedClasses.size() != classMap.size()) {
            logger.error("Circular dependency detected among classes.");
        }

        // Step 3: Load classes in topological order
        List<Class<?>> newClasses = new ArrayList<>();
        sortedClasses.forEach(className -> {
            try {
                String dottedName = className.replace("/", ".");
                try {
                    Class.forName(dottedName, false, Thread.currentThread().getContextClassLoader());
                } catch (ClassNotFoundException e) {
                    Class<?> clazz = defineMethod.apply(className, classMap.get(className));
                    newClasses.add(clazz);
                }
            } catch (Exception e) {
                logger.error("Failed to load class: " + className, e);
            }
        });

        return newClasses;
    }

    private List<String> resolveDeps(ClassNode node) {
        return depCache.computeIfAbsent(node, k -> {
            List<String> deps = new ArrayList<>();
            if (node.superName != null) deps.add(node.superName);
            if (node.interfaces != null) deps.addAll(node.interfaces);
            if (node.visibleAnnotations != null) {
                node.visibleAnnotations.forEach(ann -> deps.add(ASMUtil.fromDesc(ann.desc)));
            }
            if (node.invisibleAnnotations != null) {
                node.invisibleAnnotations.forEach(ann -> deps.add(ASMUtil.fromDesc(ann.desc)));
            }
            return deps;
        });
    }
}
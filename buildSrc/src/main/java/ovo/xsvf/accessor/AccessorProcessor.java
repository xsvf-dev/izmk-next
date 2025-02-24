package ovo.xsvf.accessor;

import lombok.*;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import ovo.xsvf.ASMUtil;
import ovo.xsvf.JarClassLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;

@Builder
public class AccessorProcessor implements Opcodes {
    private final static String ANNOTATION_PACKAGE = "ovo/xsvf/patchify/annotation";
    private final static Type ACCESSOR_ANNOTATION = Type.getType(ANNOTATION_PACKAGE + "/Accessor");
    private final static Type FIELD_ACCESSOR_ANNOTATION = Type.getType(ANNOTATION_PACKAGE + "/FieldAccessor");
    private final static Type METHOD_ACCESSOR_ANNOTATION = Type.getType(ANNOTATION_PACKAGE + "/MethodAccessor");
    private final static Type FINAL_ANNOTATION = Type.getType(ANNOTATION_PACKAGE + "/Final");

    private final @NotNull File inputJarFile;
    private final @NotNull File outputFile;
    private final @NotNull List<Path> libraryJars;
    private final @NotNull Consumer<String> log;
    private final @NotNull Integer readFlags;
    private final @NotNull Integer writeFlags;

    private JarClassLoader classLoader;

    private final Map<String, ClassNode> classNodes = new HashMap<>();
    private final Map<String, byte[]> resourceList = new HashMap<>();

    public void load() throws IOException {
        try (JarInputStream jarInputStream = new JarInputStream(inputJarFile.toURI().toURL().openStream())) {
            JarEntry entry;
            while ((entry = jarInputStream.getNextJarEntry()) != null) {
                if (entry.getName().endsWith(".class")) {
                    classNodes.put(entry.getName(), ASMUtil.node(jarInputStream.readAllBytes()));
                } else {
                    resourceList.put(entry.getName(), jarInputStream.readAllBytes());
                }
            }
        }
        classLoader = new JarClassLoader(libraryJars, this.getClass().getClassLoader(), log);
    }

    public void preProcess() {
//        for (ClassNode node : classNodes.values()) {
//            if (node.visibleAnnotations == null || node.visibleAnnotations.stream()
//                    .noneMatch(a -> a.desc.equals(ACCESSOR_ANNOTATION.getDescriptor())))
//                continue;
//            log.accept("find class: " + node.name);
//        }
    }

    public void postProcess() {

    }

    public void write() throws IOException {
        try (JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(outputFile))) {
            for (Map.Entry<String, byte[]> entry : resourceList.entrySet()) {
                JarEntry jarEntry = new JarEntry(entry.getKey());
                jarOutputStream.putNextEntry(jarEntry);
                jarOutputStream.write(entry.getValue());
                jarOutputStream.closeEntry();
            }
            for (Map.Entry<String, ClassNode> entry : classNodes.entrySet()) {
                ClassWriter classWriter = new ClassWriter(writeFlags) {
                    @Override
                    protected ClassLoader getClassLoader() {
                        return classLoader;
                    }
                };
                entry.getValue().accept(classWriter);
                JarEntry jarEntry = new JarEntry(entry.getKey());
                jarOutputStream.putNextEntry(jarEntry);
                jarOutputStream.write(classWriter.toByteArray());
                jarOutputStream.closeEntry();
            }
        } catch (IOException e) {
            log.accept("Error writing to the output JAR file: " + e.getMessage());
            throw e;
        }
    }

    @Value
    private static class Method {
        String accessor;
        String owner;
        String name;
        String desc;
    }
}

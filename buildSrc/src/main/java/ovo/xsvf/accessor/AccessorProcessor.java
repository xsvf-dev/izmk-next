package ovo.xsvf.accessor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import ovo.xsvf.JarClassLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;

@Builder
public class AccessorProcessor implements Opcodes {
    private final static String ACCESSOR_ANNOTATION_DESC = "Lovo/xsvf/izmk/injection/accessor/annotation/Accessor;";
    private final static String FINAL_ANNOTATION_DESC = "Lovo/xsvf/izmk/injection/accessor/annotation/Final;";
    private final static String RELOCATE_ANNOTATION_DESC = "Lovo/xsvf/izmk/injection/accessor/annotation/Relocate;";
    private final static String FIELD_ACCESSOR_DESC = "Lovo/xsvf/izmk/injection/accessor/annotation/FieldAccessor;";
    private final static String METHOD_ACCESSOR_DESC = "Lovo/xsvf/izmk/injection/accessor/annotation/MethodAccessor;";

    private final static String UTIL_CLASS = "ovo/xsvf/izmk/injection/ReflectionUtil";
    private final static String METHOD_HELPER_CLASS = "ovo/xsvf/izmk/injection/MethodHelper";
    private final static String METHOD_HELPER_CLASS_DESC = "L" + METHOD_HELPER_CLASS + ";";

    private final @NotNull File inputJarFile;
    private final @NotNull File outputFile;
    private final @NotNull List<Path> libraryJars;
    private final @NotNull Consumer<String> log;
    private final @NotNull Integer readFlags;
    private final @NotNull Integer writeFlags;

    private final HashMap<String, ClassNode> classNodes = new HashMap<>();
    private final HashMap<String, byte[]> resourceList = new HashMap<>();

    private final List<String> shadowClasses = new ArrayList<>();
    private final HashMap<String, List<Field>> shadowFields = new HashMap<>();
    private final HashMap<String, List<Method>> shadowMethods = new HashMap<>();

    private JarClassLoader classLoader;

    private static InsnList checkcastFromObject(Type type) {
        InsnList insnList = new InsnList();

        if (type.equals(Type.INT_TYPE)) {
            insnList.add(new TypeInsnNode(CHECKCAST, "java/lang/Integer"));
            insnList.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false));
        } else if (type.equals(Type.BOOLEAN_TYPE)) {
            insnList.add(new TypeInsnNode(CHECKCAST, "java/lang/Boolean"));
            insnList.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z", false));
        } else if (type.equals(Type.CHAR_TYPE)) {
            insnList.add(new TypeInsnNode(CHECKCAST, "java/lang/Character"));
            insnList.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Character", "charValue", "()C", false));
        } else if (type.equals(Type.BYTE_TYPE)) {
            insnList.add(new TypeInsnNode(CHECKCAST, "java/lang/Byte"));
            insnList.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Byte", "byteValue", "()B", false));
        } else if (type.equals(Type.SHORT_TYPE)) {
            insnList.add(new TypeInsnNode(CHECKCAST, "java/lang/Short"));
            insnList.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Short", "shortValue", "()S", false));
        } else if (type.equals(Type.LONG_TYPE)) {
            insnList.add(new TypeInsnNode(CHECKCAST, "java/lang/Long"));
            insnList.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Long", "longValue", "()J", false));
        } else if (type.equals(Type.FLOAT_TYPE)) {
            insnList.add(new TypeInsnNode(CHECKCAST, "java/lang/Float"));
            insnList.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Float", "floatValue", "()F", false));
        } else if (type.equals(Type.DOUBLE_TYPE)) {
            insnList.add(new TypeInsnNode(CHECKCAST, "java/lang/Double"));
            insnList.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Double", "doubleValue", "()D", false));
        } else {
            insnList.add(new TypeInsnNode(CHECKCAST, type.getInternalName()));
        }

        return insnList;
    }

    private static InsnList checkcastFromObject(String desc) {
        InsnList insnList = new InsnList();
        Type type = Type.getType(desc);

        if (type.equals(Type.INT_TYPE)) {
            insnList.add(new TypeInsnNode(CHECKCAST, "java/lang/Integer"));
            insnList.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false));
        } else if (type.equals(Type.BOOLEAN_TYPE)) {
            insnList.add(new TypeInsnNode(CHECKCAST, "java/lang/Boolean"));
            insnList.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z", false));
        } else if (type.equals(Type.CHAR_TYPE)) {
            insnList.add(new TypeInsnNode(CHECKCAST, "java/lang/Character"));
            insnList.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Character", "charValue", "()C", false));
        } else if (type.equals(Type.BYTE_TYPE)) {
            insnList.add(new TypeInsnNode(CHECKCAST, "java/lang/Byte"));
            insnList.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Byte", "byteValue", "()B", false));
        } else if (type.equals(Type.SHORT_TYPE)) {
            insnList.add(new TypeInsnNode(CHECKCAST, "java/lang/Short"));
            insnList.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Short", "shortValue", "()S", false));
        } else if (type.equals(Type.LONG_TYPE)) {
            insnList.add(new TypeInsnNode(CHECKCAST, "java/lang/Long"));
            insnList.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Long", "longValue", "()J", false));
        } else if (type.equals(Type.FLOAT_TYPE)) {
            insnList.add(new TypeInsnNode(CHECKCAST, "java/lang/Float"));
            insnList.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Float", "floatValue", "()F", false));
        } else if (type.equals(Type.DOUBLE_TYPE)) {
            insnList.add(new TypeInsnNode(CHECKCAST, "java/lang/Double"));
            insnList.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Double", "doubleValue", "()D", false));
        } else {
            insnList.add(new TypeInsnNode(CHECKCAST, type.getInternalName()));
        }

        return insnList;
    }

    private static InsnList checkcastToObject(String desc) {
        InsnList insnList = new InsnList();
        Type type = Type.getType(desc);

        if (type.getSort() == Type.INT) {
            insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false));
        } else if (type.getSort() == Type.BOOLEAN) {
            insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false));
        } else if (type.getSort() == Type.CHAR) {
            insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;", false));
        } else if (type.getSort() == Type.BYTE) {
            insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;", false));
        } else if (type.getSort() == Type.SHORT) {
            insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;", false));
        } else if (type.getSort() == Type.LONG) {
            insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", false));
        } else if (type.getSort() == Type.FLOAT) {
            insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;", false));
        } else if (type.getSort() == Type.DOUBLE) {
            insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", false));
        } else {
            insnList.add(new TypeInsnNode(Opcodes.CHECKCAST, "java/lang/Object"));
        }

        return insnList;
    }

    private static InsnList checkcastToObject(Type type) {
        InsnList insnList = new InsnList();

        if (type.getSort() == Type.INT) {
            insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false));
        } else if (type.getSort() == Type.BOOLEAN) {
            insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false));
        } else if (type.getSort() == Type.CHAR) {
            insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;", false));
        } else if (type.getSort() == Type.BYTE) {
            insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;", false));
        } else if (type.getSort() == Type.SHORT) {
            insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;", false));
        } else if (type.getSort() == Type.LONG) {
            insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", false));
        } else if (type.getSort() == Type.FLOAT) {
            insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;", false));
        } else if (type.getSort() == Type.DOUBLE) {
            insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", false));
        } else {
            insnList.add(new TypeInsnNode(Opcodes.CHECKCAST, "java/lang/Object"));
        }

        return insnList;
    }

    public void load() throws IOException {
        try (JarInputStream jarInputStream = new JarInputStream(inputJarFile.toURI().toURL().openStream())) {
            JarEntry entry;
            while ((entry = jarInputStream.getNextJarEntry()) != null) {
                if (entry.getName().endsWith(".class")) {
                    classNodes.put(entry.getName(), node(jarInputStream.readAllBytes()));
                } else {
                    resourceList.put(entry.getName(), jarInputStream.readAllBytes());
                }
            }
        }
        classLoader = new JarClassLoader(libraryJars, this.getClass().getClassLoader(), log);
    }

    private ClassNode node(byte[] bytes) {
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, 0);
        return node;
    }

    public void preProcess() {
        for (ClassNode node : classNodes.values()) {
            if (node.invisibleAnnotations == null || node.invisibleAnnotations.stream().noneMatch(it -> it.desc.equals(ACCESSOR_ANNOTATION_DESC)))
                continue;
            shadowClasses.add(node.name);
            String accessor = node.invisibleAnnotations.stream()
                    .filter(it -> it.desc.equals(ACCESSOR_ANNOTATION_DESC))
                    .map(it -> ((Type) it.values.get(1)).getInternalName())
                    .findFirst().orElseThrow();

            for (MethodNode method : node.methods) {
                for (AnnotationNode anno : method.invisibleAnnotations) {
                    String owner = method.invisibleAnnotations.stream()
                            .filter(it -> it.desc.equals(RELOCATE_ANNOTATION_DESC))
                            .map(it -> ((Type) it.values.get(1)).getInternalName())
                            .findFirst().orElse(accessor);
                    if (anno.desc.equals(FIELD_ACCESSOR_DESC)) {
                        String name = (String) anno.values.get(1);
                        String desc = Type.getReturnType(method.desc).getDescriptor();
                        boolean finalAccess = method.invisibleAnnotations.stream()
                                .anyMatch(it -> it.desc.equals(FINAL_ANNOTATION_DESC));
                        boolean isGetter = anno.values.size() < 3 || (boolean) anno.values.get(3);
                        shadowFields.computeIfAbsent(node.name, k -> new ArrayList<>()).add(new Field(name, desc, method.name, finalAccess, isGetter, owner));
                        break;
                    } else if (anno.desc.equals(METHOD_ACCESSOR_DESC)) {
                        shadowMethods.computeIfAbsent(node.name, k -> new ArrayList<>()).add(new Method(method.name, method.desc, owner));
                        break;
                    }
                }
            }
        }
    }

    public void postProcess() {
        classNodes.values().forEach(it -> it.methods.forEach(method -> {
            for (var insnNode : method.instructions) {
                if (insnNode instanceof TypeInsnNode && insnNode.getOpcode() == CHECKCAST &&
                        shadowClasses.contains(((TypeInsnNode) insnNode).desc)) {
                    method.instructions.remove(insnNode);
                } else if (insnNode instanceof MethodInsnNode) {
                    String owner = ((MethodInsnNode) insnNode).owner;
                    if (!shadowClasses.contains(owner)) continue;

                    InsnList insnList = new InsnList();
                    if (shadowFields.getOrDefault(owner, List.of()).stream().anyMatch(field -> field.accessMethod.equals(((MethodInsnNode) insnNode).name))) {
                        Field field = shadowFields.get(owner).stream()
                               .filter(field1 -> field1.accessMethod.equals(((MethodInsnNode) insnNode).name))
                               .findFirst().orElseThrow();
                        if (field.isGetter) {
                            if (insnNode.getOpcode() == INVOKESTATIC) {
                                insnList.add(new InsnNode(ACONST_NULL));
                            } else if (insnNode.getOpcode() != INVOKEINTERFACE) {
                                throw new IllegalStateException("Unexpected opcode: " + insnNode.getOpcode());
                            }
                            insnList.add(new LdcInsnNode(field.name));
                            insnList.add(new LdcInsnNode(field.owner));
                            insnList.add(new MethodInsnNode(INVOKESTATIC, UTIL_CLASS, "getField", "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/Object;", false));
                            insnList.add(checkcastFromObject(field.desc));
                        } else {
                            if (insnNode.getOpcode() == INVOKESTATIC) {
                                // static : [value]
                                insnList.add(new LdcInsnNode(field.name));
                                insnList.add(new LdcInsnNode(field.owner));
                                insnList.add(new MethodInsnNode(INVOKESTATIC, UTIL_CLASS, field.finalAccess ? "setFieldFinalStatic" : "setFieldStatic",
                                        "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", false));
                            } else if (insnNode.getOpcode() == INVOKEINTERFACE) {
                                // interface : [instance, value]
                                insnList.add(new LdcInsnNode(field.name));
                                if (field.finalAccess) {
                                    insnList.add(new MethodInsnNode(INVOKESTATIC, UTIL_CLASS, "setFieldFinal", "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/String;)V", false));
                                } else {
                                    insnList.add(new LdcInsnNode(field.owner));
                                    insnList.add(new MethodInsnNode(INVOKESTATIC, UTIL_CLASS, "setField", "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;)V", false));
                                }
                            }
                        }
                    } else if (shadowMethods.getOrDefault(owner, List.of()).stream().anyMatch(m -> m.name.equals(((MethodInsnNode) insnNode).name) && m.desc.equals(((MethodInsnNode) insnNode).desc))) {
                        Method method1 = shadowMethods.get(owner).stream()
                               .filter(m -> m.name.equals(((MethodInsnNode) insnNode).name) && m.desc.equals(((MethodInsnNode) insnNode).desc))
                               .findFirst().orElseThrow();
                        int methodHelperIndex = method.maxLocals; method.maxLocals += 1;

                        // stack: [<instance>, args...]
                        insnList.add(new MethodInsnNode(INVOKESTATIC, METHOD_HELPER_CLASS, "getInstance", "()L" + METHOD_HELPER_CLASS + ";", false));
                        insnList.add(new VarInsnNode(ASTORE, methodHelperIndex));
                        // stack: [<instance>, args...]
                        Type[] argTypes = Type.getArgumentTypes(method1.desc);
                        for (int i = argTypes.length - 1; i >= 0; i--) {
                            insnList.add(checkcastToObject(argTypes[i]));
                            insnList.add(new VarInsnNode(ALOAD, methodHelperIndex));
                            insnList.add(new InsnNode(SWAP));
                            insnList.add(new MethodInsnNode(INVOKEVIRTUAL, METHOD_HELPER_CLASS, "addParam", "(Ljava/lang/Object;)L" + METHOD_HELPER_CLASS + ";", false));
                            insnList.add(new InsnNode(POP));
                            // stack: [<instance>, args...]
                        }
                        insnList.add(new VarInsnNode(ALOAD, methodHelperIndex));
                        if (insnNode.getOpcode() != INVOKESTATIC) {
                            insnList.add(new InsnNode(SWAP));
                        }
                        insnList.add(new LdcInsnNode(method1.owner));
                        insnList.add(new LdcInsnNode(method1.name));
                        insnList.add(new LdcInsnNode(method1.desc));
                        if (insnNode.getOpcode() == INVOKESTATIC) {
                            insnList.add(new MethodInsnNode(INVOKEVIRTUAL, METHOD_HELPER_CLASS, "callStatic", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/Object;", false));
                        } else if (insnNode.getOpcode() == INVOKEINTERFACE) {
                            insnList.add(new MethodInsnNode(INVOKEVIRTUAL, METHOD_HELPER_CLASS, "call", "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/Object;", false));
                        } else {
                            throw new IllegalStateException("Unexpected opcode: " + insnNode.getOpcode());
                        }

                        if (Type.getReturnType(method1.desc) == Type.VOID_TYPE) {
                            insnList.add(new InsnNode(POP));
                        } else {
                            insnList.add(checkcastFromObject(Type.getReturnType(method1.desc)));
                        }
                    } else {
                        throw new IllegalStateException("Unexpected method: " + owner + "." + ((MethodInsnNode) insnNode).name + " " + ((MethodInsnNode) insnNode).desc);
                    }

                    if (insnList.size() > 0) {
                        method.instructions.insert(insnNode, insnList);
                        method.instructions.remove(insnNode);
                    }
                }
            }
        }));
        shadowClasses.forEach(classNodes::remove);
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

    @ToString
    @EqualsAndHashCode
    @AllArgsConstructor
    private static class Field {
        private final String name;
        private final String desc;
        private final String accessMethod;
        private final boolean finalAccess;
        private final boolean isGetter;
        private final String owner;
    }

    @ToString
    @EqualsAndHashCode
    @AllArgsConstructor
    private static class Method {
        private final String name;
        private final String desc;
        private final String owner;
    }
}

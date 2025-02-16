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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;

@Builder
public class AccessorProcessor implements Opcodes {
    private final static String UTIL_CLASS = "ovo/xsvf/izmk/injection/ReflectionUtil";
    private final static String METHOD_HELPER_CLASS = "ovo/xsvf/izmk/injection/MethodHelper";

    private final static String ACCESSOR_ANNOTATION_DESC = "Lovo/xsvf/izmk/injection/accessor/annotation/Accessor;";
    private final static String FINAL_ANNOTATION_DESC = "Lovo/xsvf/izmk/injection/accessor/annotation/Final;";
    private final static String AT_ANNOTATION_DESC = "Lovo/xsvf/izmk/injection/accessor/annotation/At;";
    private final static String NAME_ANNOTATION_DESC = "Lovo/xsvf/izmk/injection/accessor/annotation/Name;";
    private final static String METHOD_HELPER_CLASS_DESC = "L"+METHOD_HELPER_CLASS+";";

    private final @NotNull File inputJarFile;
    private final @NotNull File outputFile;
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
        classLoader = new JarClassLoader(inputJarFile.toPath(), this.getClass().getClassLoader(), log);
    }

    private ClassNode node(byte[] bytes) {
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, 0);
        return node;
    }

    public void preProcess() {
        classNodes.values().forEach(node -> {
            if (node.invisibleAnnotations != null) {
                node.invisibleAnnotations.forEach(annotation -> {
                    if (annotation.desc.equals(ACCESSOR_ANNOTATION_DESC)) {
                        String shadowName = node.superName;
                        List<Field> fields = new ArrayList<>();
                        List<Method> methods = new ArrayList<>();
                        node.fields.forEach(field -> {
                            AtomicBoolean isFinal = new AtomicBoolean(false);
                            AtomicReference<String> shadowOwner = new AtomicReference<>(shadowName);
                            AtomicReference<String> realName = new AtomicReference<>(field.name);
                            if (field.invisibleAnnotations != null) {
                                field.invisibleAnnotations.forEach(fieldAnnotation -> {
                                    if (fieldAnnotation.desc.equals(FINAL_ANNOTATION_DESC)) {
                                        isFinal.set(true);
                                    }
                                    if (fieldAnnotation.desc.equals(AT_ANNOTATION_DESC)) {
                                        shadowOwner.set(((Type) fieldAnnotation.values.get(1)).getInternalName());
                                    }
                                    if (fieldAnnotation.desc.equals(NAME_ANNOTATION_DESC)) {
                                        realName.set(fieldAnnotation.values.get(1).toString());
                                    }
                                });
                            }
                            fields.add(new Field(field.name,realName.get(), isFinal.get(), shadowOwner.get()));
                        });
                        node.methods.forEach(method -> {
                            AtomicReference<String> shadowOwner = new AtomicReference<>(shadowName);
                            AtomicReference<String> realName = new AtomicReference<>(method.name);
                            if (method.invisibleAnnotations != null) {
                                method.invisibleAnnotations.forEach(methodAnnotation -> {
                                    if (methodAnnotation.desc.equals(AT_ANNOTATION_DESC)) {
                                        shadowOwner.set(((Type) methodAnnotation.values.get(1)).getInternalName());
                                    }
                                    if (methodAnnotation.desc.equals(NAME_ANNOTATION_DESC)) {
                                        realName.set(methodAnnotation.values.get(1).toString());
                                    }
                                });
                            }
                            methods.add(new Method(method.name, realName.get(), method.desc, shadowOwner.get()));
                        });
                        shadowClasses.add(node.name);
                        shadowFields.put(node.name, fields);
                        shadowMethods.put(node.name, methods);
                    }
                });
            }
        });
    }

    public void postProcess() {
        classNodes.values().forEach(it -> it.methods.forEach(method -> method.instructions.forEach(insnNode -> {
            if (insnNode instanceof TypeInsnNode && insnNode.getOpcode() == CHECKCAST &&
                    shadowClasses.contains(((TypeInsnNode) insnNode).desc)) {
                method.instructions.remove(insnNode);
            } else if (insnNode instanceof FieldInsnNode) {
                String owner = ((FieldInsnNode) insnNode).owner;
                String name = ((FieldInsnNode) insnNode).name;
                String desc = ((FieldInsnNode) insnNode).desc;

                if (shadowClasses.contains(owner)) {
                    InsnList insnList = new InsnList();
                    Field field = shadowFields.get(owner).stream()
                            .filter(f -> f.name.equals(name))
                            .findFirst().orElseThrow();
                    switch (insnNode.getOpcode()) {
                        case GETFIELD -> {
                            insnList.add(new LdcInsnNode(field.realName));
                            insnList.add(new LdcInsnNode(field.classOwner));
                            insnList.add(new MethodInsnNode(INVOKESTATIC, UTIL_CLASS, "forName",
                                    "(Ljava/lang/String;)Ljava/lang/Class;"));
                            insnList.add(new MethodInsnNode(INVOKESTATIC, UTIL_CLASS, "getField",
                                    "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/Object;"));
                            insnList.add(checkcastFromObject(desc));
                        }
                        case GETSTATIC -> {
                            insnList.add(new InsnNode(ACONST_NULL));
                            insnList.add(new LdcInsnNode(field.realName));
                            insnList.add(new LdcInsnNode(field.classOwner));
                            insnList.add(new MethodInsnNode(INVOKESTATIC, UTIL_CLASS, "forName",
                                    "(Ljava/lang/String;)Ljava/lang/Class;"));
                            insnList.add(new MethodInsnNode(INVOKESTATIC, UTIL_CLASS, "getField",
                                    "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/Object;"));
                            insnList.add(checkcastFromObject(desc));
                        }
                        case PUTFIELD -> {
                            insnList.add(checkcastToObject(desc));
                            if (field.finalAccess) {
                                insnList.add(new LdcInsnNode(field.realName));
                                insnList.add(new MethodInsnNode(INVOKESTATIC, UTIL_CLASS, "setFieldFinal",
                                        "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/String;)V"));
                            } else {
                                insnList.add(new LdcInsnNode(field.realName));
                                insnList.add(new LdcInsnNode(field.classOwner));
                                insnList.add(new MethodInsnNode(INVOKESTATIC, UTIL_CLASS, "forName",
                                        "(Ljava/lang/String;)Ljava/lang/Class;"));
                                insnList.add(new MethodInsnNode(INVOKESTATIC, UTIL_CLASS, "setField",
                                        "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/String;Ljava/lang/Class;)V"));
                            }
                        }
                        case PUTSTATIC -> {
                            insnList.add(checkcastToObject(desc));
                            if (field.finalAccess) {
                                insnList.add(new LdcInsnNode(field.realName));
                                insnList.add(new LdcInsnNode(field.classOwner));
                                insnList.add(new MethodInsnNode(INVOKESTATIC, UTIL_CLASS, "forName",
                                         "(Ljava/lang/String;)Ljava/lang/Class;"));
                                insnList.add(new MethodInsnNode(INVOKESTATIC, UTIL_CLASS, "setFieldFinalStatic",
                                        "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/Class;)V"));
                            } else {
                                insnList.add(new LdcInsnNode(field.realName));
                                insnList.add(new LdcInsnNode(field.classOwner));
                                insnList.add(new MethodInsnNode(INVOKESTATIC, UTIL_CLASS, "forName",
                                        "(Ljava/lang/String;)Ljava/lang/Class;"));
                                insnList.add(new MethodInsnNode(INVOKESTATIC, UTIL_CLASS, "setFieldStatic",
                                        "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/Class;)V"));
                            }
                        }
                    }
                    if (insnList.size() > 0) {
                        method.instructions.insert(insnNode, insnList);
                        method.instructions.remove(insnNode);
                    }
                }
            } else if (insnNode instanceof MethodInsnNode) {
                String owner = ((MethodInsnNode) insnNode).owner;
                String name = ((MethodInsnNode) insnNode).name;
                String desc = ((MethodInsnNode) insnNode).desc;


                if (shadowClasses.contains(owner)) {

                    InsnList insnList = new InsnList();
                    Method methodInfo = shadowMethods.get(owner).stream()
                            .filter(m -> m.name.equals(name) && m.desc.equals(desc))
                            .findFirst().orElseThrow();
                    Type[] args = Type.getArgumentTypes(desc);
                    Type returnType = Type.getReturnType(desc);
                    // print all method datas
                    log.accept(methodInfo.toString());
                    switch (insnNode.getOpcode()) {
                        case INVOKEVIRTUAL, INVOKESTATIC -> {
                            // first, get a MethodHelper instance
                            insnList.add(new MethodInsnNode(INVOKESTATIC, METHOD_HELPER_CLASS,
                                    "getInstance", "()" + METHOD_HELPER_CLASS_DESC));
                            // next, for every param, do swap, and invoke addParam in MethodHelper
                            for (int i = args.length - 1; i >= 0; i--) {
                                Type type = args[i];
                                // stack: param, instance
                                if (type == Type.LONG_TYPE || type == Type.DOUBLE_TYPE) {
                                    // long and double take two slots in stack
                                    int temp = method.maxLocals - 1; // use temp to store the original value
                                    int temp2 = method.maxLocals;
                                    insnList.add(new VarInsnNode(ASTORE, temp));
                                    insnList.add(new VarInsnNode(type == Type.LONG_TYPE ? LSTORE : DSTORE, temp2));
                                    insnList.add(new VarInsnNode(ALOAD, temp));
                                    insnList.add(new VarInsnNode(type == Type.LONG_TYPE ? LLOAD : DLOAD, temp2));
                                } else {
                                    // swap directly
                                    insnList.add(new InsnNode(SWAP));
                                }
                                insnList.add(checkcastToObject(type));
                                insnList.add(new MethodInsnNode(INVOKEVIRTUAL, METHOD_HELPER_CLASS, "addParam", "(Ljava/lang/Object;)" + METHOD_HELPER_CLASS_DESC));
                            }
                            // stack: (if virtual) oinstance instance
                            // we need to swap the instance and oinstance again,
                            // because the instance is the first param in the method signature
                            if (insnNode.getOpcode() == INVOKEVIRTUAL) {
                                insnList.add(new InsnNode(SWAP));
                            }
                            // stack: instance (if virtual) oinstance
                            // finally, put the classowner, name and desc into stack,
                            // and do the virtual method call
                            insnList.add(new LdcInsnNode(methodInfo.classOwner));
                            insnList.add(new LdcInsnNode(methodInfo.realName));
                            insnList.add(new LdcInsnNode(methodInfo.desc));
                            if (insnNode.getOpcode() == INVOKEVIRTUAL) {
                                insnList.add(new MethodInsnNode(INVOKEVIRTUAL, METHOD_HELPER_CLASS, "call", "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/Object;"));
                            } else {
                                insnList.add(new MethodInsnNode(INVOKEVIRTUAL, METHOD_HELPER_CLASS, "callStatic", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/Object;"));
                            }
                            if (returnType == Type.VOID_TYPE) {
                                insnList.add(new InsnNode(POP));
                            } else {
                                insnList.add(checkcastFromObject(returnType));
                            }
                        }
                        case INVOKESPECIAL -> {
                            // should not process this
                        }
                        case INVOKEINTERFACE -> {
                            // TODO: invoke interface method
                        }
                    }
                    if (insnList.size() > 0) {
                        method.instructions.insert(insnNode, insnList);
                        method.instructions.remove(insnNode);
                    }
                }
            }
        })));
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
                log.accept("Writing class: " + entry.getKey());
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
        private final String realName;
        private final boolean finalAccess;
        private final String classOwner;
    }

    @ToString
    @EqualsAndHashCode
    @AllArgsConstructor
    private static class Method {
        private final String name;
        private final String realName;
        private final String desc;
        private final String classOwner;
    }
}

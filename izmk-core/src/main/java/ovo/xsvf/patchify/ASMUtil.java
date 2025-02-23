package ovo.xsvf.patchify;

import it.unimi.dsi.fastutil.Pair;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ASMUtil implements Opcodes {
    private static final Map<Integer, String> OPCODE_NAMES = new HashMap<>();
    static {
        try {
            for (Field field : Opcodes.class.getFields()) {
                if (field.getType() == int.class) {
                    OPCODE_NAMES.put(field.getInt(null), field.getName());
                }
            }
        } catch (Exception ignored) {
        }
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

    public static void printOpcodesWithParameters(MethodNode methodNode, Consumer<String> log) {
        log.accept("Method: " + methodNode.name);
        int i = 0;

        // 遍历所有字节码指令
        for (AbstractInsnNode abstractInsn : methodNode.instructions) {
            // 跳过 LineNumberNode 和 LabelNode
            if (abstractInsn instanceof LineNumberNode || abstractInsn instanceof LabelNode) {
                continue;
            }

            // 获取操作码名称
            int opcode = abstractInsn.getOpcode();
            String opcodeName = getOpcodeName(opcode);

            // 打印操作码及指令类型
            StringBuilder sb = new StringBuilder("[ " + ++i + " ]  " + abstractInsn.getClass().getSimpleName() + " (Opcode: " + opcodeName + ")");

            // 解析并打印参数
            String parameters = parseParameters(abstractInsn);
            if (!parameters.isEmpty()) {
                sb.append(" Parameters: " + parameters);
            }

            log.accept(sb.toString());
        }
    }

    // 辅助函数：将操作码值转换为名称
    private static String getOpcodeName(int opcode) {
        try {
            // 使用反射获取 Opcodes 类中的操作码名称
            return OPCODE_NAMES.getOrDefault(opcode, "UNKNOWN_" + opcode);
        } catch (Exception e) {
            return "UNKNOWN_" + opcode;
        }
    }

    // 辅助函数：解析指令的参数
    private static String parseParameters(AbstractInsnNode abstractInsn) {
        if (abstractInsn instanceof VarInsnNode varInsn) {
            return "var: " + varInsn.var;
        } else if (abstractInsn instanceof IntInsnNode intInsn) {
            return "value: " + intInsn.operand;
        } else if (abstractInsn instanceof FieldInsnNode fieldInsn) {
            return "owner: " + fieldInsn.owner + ", name: " + fieldInsn.name + ", desc: " + fieldInsn.desc;
        } else if (abstractInsn instanceof MethodInsnNode methodInsn) {
            return "owner: " + methodInsn.owner + ", name: " + methodInsn.name + ", desc: " + methodInsn.desc;
        } else if (abstractInsn instanceof LdcInsnNode ldcInsn) {
            return "value: " + ldcInsn.cst;
        } else if (abstractInsn instanceof TypeInsnNode typeInsn) {
            return "desc: " + typeInsn.desc;
        } else if (abstractInsn instanceof JumpInsnNode jumpInsn) {
            return "label: " + jumpInsn.label.toString();
        } else if (abstractInsn instanceof MultiANewArrayInsnNode multiANewArrayInsn) {
            return "desc: " + multiANewArrayInsn.desc + ", dims: " + multiANewArrayInsn.dims;
        } else if (abstractInsn instanceof IincInsnNode iincInsn) {
            return "var: " + iincInsn.var + ", incr: " + iincInsn.incr;
        } else {
            return "";
        }
    }

    public static InsnList checkcastFromObject(Type type) {
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

    public static InsnList checkcastFromObject(String desc) {
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

    public static InsnList checkcastToObject(String desc) {
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

    public static InsnList checkcastToObject(Type type) {
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


    // owner, name
    public static @NotNull Pair<String, String> splitDesc(@NotNull String desc) {
        String[] descs = desc.split("/");
        StringBuilder name = new StringBuilder();
        for (int i = 0; i < descs.length - 1; ++i) name.append(descs[i]).append("/");
        return Pair.of(name.substring(0, name.length() - 1), descs[descs.length - 1]);
    }
}

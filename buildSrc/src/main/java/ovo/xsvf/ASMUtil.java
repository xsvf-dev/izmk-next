package ovo.xsvf;

import org.objectweb.asm.ClassReader;
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

    public static ClassNode node(byte[] bytes) {
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, 0);
        return node;
    }

    public static void printMethod(MethodNode methodNode, Consumer<String> log) {
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
                sb.append(" Parameters: ").append(parameters);
            }

            log.accept(sb.toString());
        }
    }

    private static String getOpcodeName(int opcode) {
        return OPCODE_NAMES.getOrDefault(opcode, "UNKNOWN_" + opcode);
    }

    private static String parseParameters(AbstractInsnNode abstractInsn) {
        return switch (abstractInsn) {
            case VarInsnNode varInsn -> "var: " + varInsn.var;
            case IntInsnNode intInsn -> "value: " + intInsn.operand;
            case FieldInsnNode fieldInsn ->
                    "owner: " + fieldInsn.owner + ", name: " + fieldInsn.name + ", desc: " + fieldInsn.desc;
            case MethodInsnNode methodInsn ->
                    "owner: " + methodInsn.owner + ", name: " + methodInsn.name + ", desc: " + methodInsn.desc;
            case LdcInsnNode ldcInsn -> "value: " + ldcInsn.cst;
            case TypeInsnNode typeInsn -> "desc: " + typeInsn.desc;
            case JumpInsnNode jumpInsn -> "label: " + jumpInsn.label.toString();
            case MultiANewArrayInsnNode multiANewArrayInsn ->
                    "desc: " + multiANewArrayInsn.desc + ", dims: " + multiANewArrayInsn.dims;
            case IincInsnNode iincInsn -> "var: " + iincInsn.var + ", incr: " + iincInsn.incr;
            case null, default -> "";
        };
    }

    public static InsnList checkcastFromObject(String desc) {
        return checkcastFromObject(Type.getType(desc));
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

    public static InsnList checkcastToObject(String desc) {
        return checkcastToObject(Type.getType(desc));
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
}

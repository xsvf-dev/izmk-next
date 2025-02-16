package ovo.xsvf;

import org.objectweb.asm.Opcodes;
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
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                sb.append(" Parameters: ").append(parameters);
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
}

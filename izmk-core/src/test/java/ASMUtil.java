import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import static ovo.xsvf.patchify.ASMUtil.*;

public class ASMUtil implements Opcodes {
    public static ClassNode createClassNode(String pkg) {
        ClassNode node = new ClassNode();
        node.visit(65, ACC_PUBLIC | ACC_SUPER,
                pkg + (pkg.isEmpty() ? "" : "/") + "宝马" + System.currentTimeMillis(),
                null, "java/lang/Object", null);
        return node;
    }

    /* static & non-final field getter */
    public static ClassNode createStaticFieldGetter(String invokeMethodName, String owner, String name, String desc) {
        Type type = Type.getType(desc);
        ClassNode node = createClassNode(classToPackage(owner));

        MethodNode method = new MethodNode(ACC_PUBLIC | ACC_STATIC, invokeMethodName, "()" + desc, null, null);
        method.instructions.add(new FieldInsnNode(GETSTATIC, owner, name, desc));
        method.instructions.add(checkcastFromObject(type));
        method.instructions.add(new InsnNode(type.getOpcode(IRETURN)));
        node.methods.add(method);

        return node;
    }

    /* non-static & non-final field getter */
    public static ClassNode createFieldGetter(String invokeMethodName, String owner, String name, String desc) {
        Type type = Type.getType(desc);
        ClassNode node = createClassNode(classToPackage(owner));

        MethodNode method = new MethodNode(ACC_PUBLIC | ACC_STATIC, invokeMethodName, "(L" + owner + ";)" + desc, null, null);
        method.instructions.add(new VarInsnNode(ALOAD, 0));
        method.instructions.add(new FieldInsnNode(GETFIELD, owner, name, desc));
        method.instructions.add(checkcastFromObject(type));
        method.instructions.add(new InsnNode(type.getOpcode(IRETURN)));
        node.methods.add(method);

        return node;
    }

    /* static & non-final field setter */
    public static ClassNode createStaticFieldSetter(String invokeMethodName, String owner, String name, String desc) {
        Type type = Type.getType(desc);
        ClassNode node = createClassNode(classToPackage(owner));

        MethodNode method = new MethodNode(ACC_PUBLIC | ACC_STATIC, invokeMethodName, "(" + desc + ")V", null, null);
        method.instructions.add(new FieldInsnNode(PUTSTATIC, owner, name, desc));
        method.instructions.add(new InsnNode(RETURN));
        node.methods.add(method);

        return node;
    }

    /* non-static & non-final field setter */
    public static ClassNode createFieldSetter(String invokeMethodName, String owner, String name, String desc) {
        Type type = Type.getType(desc);
        ClassNode node = createClassNode(classToPackage(owner));

        MethodNode method = new MethodNode(ACC_PUBLIC | ACC_STATIC, invokeMethodName, "(L" + owner + ";" + desc + ")V", null, null);
        method.instructions.add(new VarInsnNode(ALOAD, 0));
        method.instructions.add(new VarInsnNode(type.getOpcode(ILOAD), 1));
        method.instructions.add(new FieldInsnNode(PUTFIELD, owner, name, desc));
        method.instructions.add(new InsnNode(RETURN));
        node.methods.add(method);

        return node;
    }

    public static String classToPackage(String name) {
        int idx = name.lastIndexOf(46);
        return idx != -1 && idx != name.length() - 1 ? name.substring(0, idx) : "";
    }
}

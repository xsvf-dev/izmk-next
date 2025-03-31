package ovo.xsvf;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

public final class ASMUtil implements Opcodes {
    public static ClassNode node(byte[] bytes) {
        ClassReader cr = new ClassReader(bytes);
        ClassNode node = new ClassNode();
        cr.accept(node, ClassReader.EXPAND_FRAMES | ClassReader.SKIP_DEBUG);
        return node;
    }

    public static byte[] rewriteClass(ClassNode node) {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES) {
            @Override
            protected ClassLoader getClassLoader() {
                return Thread.currentThread().getContextClassLoader();
            }
        };
        node.accept(cw);
        return cw.toByteArray();
    }
}

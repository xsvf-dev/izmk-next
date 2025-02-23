package ovo.xsvf.util;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import ovo.xsvf.Pair;

public class ASMUtil {
    // owner, name
    public static Pair<String, String> spiltDesc(String desc) {
        String[] descs = desc.split("/");
        StringBuilder name = new StringBuilder();
        for (int i = 0; i < descs.length - 1; ++i) name.append(descs[i]).append("/");
        return new Pair<>(name.substring(0, name.length() - 1), descs[descs.length - 1]);
    }

    // La/b/c; -> a/b/c
    public static String fromDesc(String desc) {
        StringBuilder name = new StringBuilder();
        boolean finding = false;
        for (char c : desc.toCharArray()) {
            if (c == 'L' && !finding) finding = true;
            else if (c == ';' && finding) return name.toString();
            else if (finding) name.append(c);
        }
        return desc; // impossible
    }

    public static ClassNode node(byte [] bytes) {
        ClassReader classReader = new ClassReader(bytes);
        ClassNode node = new ClassNode();
        classReader.accept(node, ClassReader.EXPAND_FRAMES);
        return node;
    }
}

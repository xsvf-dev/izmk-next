package ovo.xsvf.izmk.injection.patch;

import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.FastColor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import ovo.xsvf.izmk.graphics.color.ColorRGB;
import ovo.xsvf.izmk.module.impl.HitColor;
import ovo.xsvf.patchify.annotation.Patch;
import ovo.xsvf.patchify.annotation.Transform;

@Patch(OverlayTexture.class)
public class OverlayTexturePatch {
    private static final Logger log = LogManager.getLogger(OverlayTexturePatch.class);

    @Transform(method = "<init>", desc = "()V")
    public static void transformConstructor(MethodNode node) {
        InsnList insnList = new InsnList();
        insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, Type.getInternalName(OverlayTexturePatch.class),
                "modifyColor", "(I)I"));

        node.instructions.forEach(insnNode -> {
            if (insnNode instanceof LdcInsnNode ldcInsnNode && ldcInsnNode.cst.equals(-1308622593)) {
                node.instructions.insert(insnNode, insnList);
            }
        });
    }

    public static int modifyColor(int i) {
        ColorRGB color = HitColor.INSTANCE.getColor();
        return HitColor.INSTANCE.getEnabled() ?
                FastColor.ARGB32.color(255 - color.getA(), color.getB(), color.getG(), color.getR())
        : i;
    }
}

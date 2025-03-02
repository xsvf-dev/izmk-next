package ovo.xsvf.izmk.injection.patch;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.world.entity.item.ItemEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import ovo.xsvf.izmk.injection.accessor.ItemEntityRendererAccessor;
import ovo.xsvf.izmk.module.impl.ItemPhysics;
import ovo.xsvf.patchify.ASMUtil;
import ovo.xsvf.patchify.annotation.Patch;
import ovo.xsvf.patchify.annotation.Transform;

@Patch(ItemEntityRenderer.class)
public class ItemEntityRendererPatch {
    private static final Logger log = LogManager.getLogger(ItemEntityRendererPatch.class);

    public static boolean onItemRender(ItemEntity itemEntity, float f, float g, PoseStack poseStack,
                                       MultiBufferSource multiBufferSource, int i, ItemEntityRenderer itemEntityRenderer) {
        if (ItemPhysics.INSTANCE.getEnabled()) {
            ItemPhysics.INSTANCE.render(itemEntity, poseStack, multiBufferSource, i,
                    ((ItemEntityRendererAccessor) itemEntityRenderer).getItemRenderer());
            return true;
        }
        return false;
    }

    @Transform(method = "render", desc = "(Lnet/minecraft/world/entity/item/ItemEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V")
    public static void injectItemRender(@NotNull MethodNode node) {
        InsnList insnList = new InsnList();
        LabelNode labelNode = new LabelNode();
        Pair<String, String> superRenderMethod = Pair.of(Type.getInternalName(EntityRenderer.class) + "/render", "(Lnet/minecraft/world/entity/Entity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V");
        Pair<String, String> data = ASMUtil.splitDesc(superRenderMethod.first());

        insnList.add(new VarInsnNode(Opcodes.ALOAD, 1));
        insnList.add(new VarInsnNode(Opcodes.FLOAD, 2));
        insnList.add(new VarInsnNode(Opcodes.FLOAD, 3));
        insnList.add(new VarInsnNode(Opcodes.ALOAD, 4));
        insnList.add(new VarInsnNode(Opcodes.ALOAD, 5));
        insnList.add(new VarInsnNode(Opcodes.ILOAD, 6));
        insnList.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this
        insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                Type.getInternalName(ItemEntityRendererPatch.class), "onItemRender",
                "(Lnet/minecraft/world/entity/item/ItemEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/renderer/entity/ItemEntityRenderer;)Z"));
        insnList.add(new JumpInsnNode(Opcodes.IFEQ, labelNode));

        insnList.add(new VarInsnNode(Opcodes.ALOAD, 0));
        insnList.add(new VarInsnNode(Opcodes.ALOAD, 1));
        insnList.add(new VarInsnNode(Opcodes.FLOAD, 2));
        insnList.add(new VarInsnNode(Opcodes.FLOAD, 3));
        insnList.add(new VarInsnNode(Opcodes.ALOAD, 4));
        insnList.add(new VarInsnNode(Opcodes.ALOAD, 5));
        insnList.add(new VarInsnNode(Opcodes.ILOAD, 6));
        insnList.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, data.first(), data.second(), superRenderMethod.second()));

        insnList.add(new InsnNode(Opcodes.RETURN));
        insnList.add(labelNode);
        node.instructions.insert(insnList);
    }
}

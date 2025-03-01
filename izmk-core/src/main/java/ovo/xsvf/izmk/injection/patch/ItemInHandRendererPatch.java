package ovo.xsvf.izmk.injection.patch;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.UseAnim;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ovo.xsvf.izmk.IZMK;
import ovo.xsvf.izmk.module.impl.OldAnimations;
import ovo.xsvf.patchify.CallbackInfo;
import ovo.xsvf.patchify.annotation.*;
import ovo.xsvf.patchify.api.Invocation;

@Patch(ItemInHandRenderer.class)
public class ItemInHandRendererPatch {
    private static final Logger log = LogManager.getLogger(ItemInHandRendererPatch.class);

    @WrapInvoke(method = "renderArmWithItem", desc = "(Lnet/minecraft/client/player/AbstractClientPlayer;FFLnet/minecraft/world/InteractionHand;FLnet/minecraft/world/item/ItemStack;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
                target = "net/minecraft/world/item/ItemStack/getUseAnimation", targetDesc = "()Lnet/minecraft/world/item/UseAnim;")
    public static UseAnim getUseAnimation(ItemInHandRenderer self, AbstractClientPlayer pPlayer,
                                          float pPartialTicks, float pPitch, InteractionHand pHand,
                                          float pSwingProgress, ItemStack pStack, float pEquippedProgress,
                                          PoseStack pPoseStack, MultiBufferSource pBuffer, int pCombinedLight,
                                          Invocation invocation) throws Exception {
        if (OldAnimations.INSTANCE.getEnabled() && OldAnimations.INSTANCE.getSwordBlocking() &&
                ((ItemStack) invocation.instance()).getItem() instanceof SwordItem) {
           return UseAnim.BLOCK;
        }
        return (UseAnim) invocation.call();
    }

    @Inject(method = "renderArmWithItem", desc = "(Lnet/minecraft/client/player/AbstractClientPlayer;FFLnet/minecraft/world/InteractionHand;FLnet/minecraft/world/item/ItemStack;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
             slice = @Slice(startIndex = 5, endIndex = 5),
             at = @At(value = At.Type.AFTER_INVOKE, method = "net/minecraft/client/renderer/ItemInHandRenderer/applyItemArmTransform", desc = "(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/entity/HumanoidArm;F)V"))
    public static void transformBlocking(ItemInHandRenderer self, AbstractClientPlayer pPlayer,
                                         float pPartialTicks, float pPitch, InteractionHand pHand,
                                         float pSwingProgress, ItemStack pStack, float pEquippedProgress,
                                         PoseStack pPoseStack, MultiBufferSource pBuffer,
                                         int pCombinedLight, CallbackInfo ci) {
        if (IZMK.mc.options.keyUse.isDown() && OldAnimations.INSTANCE.getEnabled() && OldAnimations.INSTANCE.getSwordBlocking() &&
                pStack.getItem() instanceof SwordItem) {
            OldAnimations.INSTANCE.transform(
                    pPoseStack,
                    pHand == InteractionHand.MAIN_HAND ? HumanoidArm.RIGHT : HumanoidArm.LEFT,
                    pEquippedProgress,
                    pSwingProgress
            );
        }
    }

    @WrapInvoke(method = "renderArmWithItem", desc = "(Lnet/minecraft/client/player/AbstractClientPlayer;FFLnet/minecraft/world/InteractionHand;FLnet/minecraft/world/item/ItemStack;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            slice = @Slice(startIndex = 5, endIndex = 5),
            target = "net/minecraft/client/renderer/ItemInHandRenderer/applyItemArmTransform", targetDesc = "(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/entity/HumanoidArm;F)V")
    public static void translate(ItemInHandRenderer self, AbstractClientPlayer pPlayer,
                                 float pPartialTicks, float pPitch, InteractionHand pHand,
                                 float pSwingProgress, ItemStack pStack, float pEquippedProgress,
                                 PoseStack pPoseStack, MultiBufferSource pBuffer,
                                 int pCombinedLight, Invocation invocation) throws Exception {
        if (IZMK.mc.player != null && IZMK.mc.options.keyUse.isDown() && OldAnimations.INSTANCE.getEnabled() &&
                OldAnimations.INSTANCE.getSwordBlocking() && pStack.getItem() instanceof SwordItem) {
            invocation.args().set(3, -0.52f);
        }
        invocation.call();
    }
}

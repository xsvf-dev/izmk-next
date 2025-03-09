package ovo.xsvf.izmk.injection.patch;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import ovo.xsvf.izmk.module.impl.OldAnimations;
import ovo.xsvf.patchify.CallbackInfo;
import ovo.xsvf.patchify.annotation.At;
import ovo.xsvf.patchify.annotation.Inject;
import ovo.xsvf.patchify.annotation.Patch;
import ovo.xsvf.patchify.annotation.Slice;

@Patch(ItemInHandRenderer.class)
public class ItemInHandRendererPatch {
    private static final Minecraft mc = Minecraft.getInstance();

    @Inject(method = "renderArmWithItem",
            desc = "(Lnet/minecraft/client/player/AbstractClientPlayer;FFLnet/minecraft/world/InteractionHand;FLnet/minecraft/world/item/ItemStack;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(value = At.Type.AFTER_INVOKE, method = "net/minecraft/client/renderer/ItemInHandRenderer/applyItemArmTransform", desc = "(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/entity/HumanoidArm;F)V"),
            slice = @Slice(startIndex = 2, endIndex = 6))
    public static void applySwingOffset(ItemInHandRenderer instance, AbstractClientPlayer pPlayer, float pPartialTicks, float pPitch, InteractionHand pHand, float pSwingProgress, ItemStack pStack, float pEquippedProgress, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pCombinedLight, CallbackInfo callbackInfo) {
        if (OldAnimations.INSTANCE.getSwing()) {
            OldAnimations.INSTANCE.applySwingOffset(pMatrixStack,  pHand == InteractionHand.MAIN_HAND ? HumanoidArm.RIGHT : HumanoidArm.LEFT, pSwingProgress);
        }
    }

    @Inject(method = "renderArmWithItem",
            desc = "(Lnet/minecraft/client/player/AbstractClientPlayer;FFLnet/minecraft/world/InteractionHand;FLnet/minecraft/world/item/ItemStack;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(value = At.Type.AFTER_INVOKE, method = "net/minecraft/client/renderer/ItemInHandRenderer/applyItemArmTransform", desc = "(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/entity/HumanoidArm;F)V"),
            slice = @Slice(startIndex = 5, endIndex = 5))
    public static void onBlocking0(ItemInHandRenderer instance, AbstractClientPlayer pPlayer, float pPartialTicks, float pPitch, InteractionHand pHand, float pSwingProgress, ItemStack pStack, float pEquippedProgress, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pCombinedLight, CallbackInfo callbackInfo) {
        onBlocking(pHand, pMatrixStack, pStack);
    }

    @Inject(method = "renderArmWithItem",
            desc = "(Lnet/minecraft/client/player/AbstractClientPlayer;FFLnet/minecraft/world/InteractionHand;FLnet/minecraft/world/item/ItemStack;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(value = At.Type.AFTER_INVOKE, method = "net/minecraft/client/renderer/ItemInHandRenderer/applyItemArmTransform", desc = "(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/entity/HumanoidArm;F)V"),
            slice = @Slice(startIndex = 9, endIndex = 9))
    public static void onBlocking1(ItemInHandRenderer instance, AbstractClientPlayer pPlayer, float pPartialTicks, float pPitch, InteractionHand pHand, float pSwingProgress, ItemStack pStack, float pEquippedProgress, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pCombinedLight, CallbackInfo callbackInfo) {
        onBlocking(pHand, pMatrixStack, pStack);
    }

    private static void onBlocking(InteractionHand hand, PoseStack poseStack, ItemStack itemStack) {
        if (OldAnimations.INSTANCE.getSwordBlocking() && itemStack.getItem() instanceof SwordItem &&
                mc.options.keyUse.isDown()) {
            OldAnimations.INSTANCE.transformBlockFirstPerson(poseStack, hand == InteractionHand.MAIN_HAND ? HumanoidArm.RIGHT : HumanoidArm.LEFT);
        }
    }
}

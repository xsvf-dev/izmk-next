package ovo.xsvf.izmk.injection.patch;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ovo.xsvf.izmk.injection.accessor.ItemInHandRendererAccessor;
import ovo.xsvf.izmk.module.impl.OldAnimations;
import ovo.xsvf.patchify.CallbackInfo;
import ovo.xsvf.patchify.annotation.At;
import ovo.xsvf.patchify.annotation.Inject;
import ovo.xsvf.patchify.annotation.Patch;
import ovo.xsvf.patchify.annotation.Slice;

@Patch(ItemInHandRenderer.class)
public class ItemInHandRendererPatch {
    private static final Minecraft mc = Minecraft.getInstance();
    private static final Logger log = LogManager.getLogger(ItemInHandRendererPatch.class);

    @Inject(method = "renderArmWithItem", desc = "(Lnet/minecraft/client/player/AbstractClientPlayer;FFLnet/minecraft/world/InteractionHand;FLnet/minecraft/world/item/ItemStack;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V")
    public static void itemRendererHook(ItemInHandRenderer instance, AbstractClientPlayer player,
                                        float partialTicks, float pitch, InteractionHand hand,
                                        float swingProgress, ItemStack itemStack, float equipProgress,
                                        PoseStack matrices, MultiBufferSource buffer, int combinedLight,
                                        CallbackInfo callbackInfo) {
        if (!OldAnimations.INSTANCE.shouldBlock()) return;
        if (hand == InteractionHand.MAIN_HAND) {
            callbackInfo.cancel();

            matrices.pushPose();

            HumanoidArm arm = player.getMainArm();
            float equip = 0f;

            if (OldAnimations.INSTANCE.getSwordBlocking()) equip = equipProgress;

            matrices.translate(-0.05f, 0f, 0f);

            float n = -0.2f * Mth.sin(Mth.sqrt(swingProgress) * 3.1415927F);
            float f = -0.1f * Mth.sin(swingProgress * 3.1415927F);

            matrices.translate(n, 0f, f);

            ((ItemInHandRendererAccessor) instance).applyItemArmTransform(matrices, arm, equip);
            ((ItemInHandRendererAccessor) instance).applyItemArmAttackTransform(matrices, arm, swingProgress);

            matrices.mulPose(Axis.YP.rotationDegrees(77f));
            matrices.mulPose(Axis.ZP.rotationDegrees(-10f));
            matrices.mulPose(Axis.XP.rotationDegrees(-80f));

            if (player.isUsingItem()) {
                matrices.translate(0.05f, -0.05f, -0.1f);
                matrices.mulPose(Axis.XP.rotationDegrees(20f));
                matrices.scale(1.1f, 1.1f, 1.1f);
            }

            ((ItemInHandRendererAccessor) instance).renderItem(player, itemStack,
                    ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, false, matrices,
                    buffer, combinedLight);

            matrices.popPose();
        } else if (hand == InteractionHand.OFF_HAND && mc.player != null &&
                mc.player.getOffhandItem().getItem() instanceof ShieldItem) {
            callbackInfo.cancel();
        }
    }

    @Inject(method = "renderArmWithItem", desc = "(Lnet/minecraft/client/player/AbstractClientPlayer;FFLnet/minecraft/world/InteractionHand;FLnet/minecraft/world/item/ItemStack;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(value = At.Type.BEFORE_INVOKE,
                    method = "net/minecraft/client/renderer/ItemInHandRenderer/renderItem",
                    desc = "(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"),
            slice = @Slice(startIndex = 2, endIndex = 2))
    public static void itemTransform(ItemInHandRenderer instance, AbstractClientPlayer player,
                                     float partialTicks, float pitch, InteractionHand hand,
                                     float swingProgress, ItemStack itemStack, float equippedProgress,
                                     PoseStack poseStack, MultiBufferSource buffer, int combinedLight,
                                     CallbackInfo callbackInfo) {
        if (itemStack.getItem() instanceof BlockItem) return;

        final float scale = 0.7585F / 0.86F;
        poseStack.scale(scale, scale, scale);
        poseStack.translate(getHandMultiplier(player, hand) * -0.084F, 0.059F, 0.08F);
        poseStack.mulPose(Axis.YP.rotationDegrees(getHandMultiplier(player, hand) * 5.0F));
    }

    @Inject(method = "tick", desc = "()V")
    public static void tick(ItemInHandRenderer instance, CallbackInfo ci) {
        if (mc.player == null) return;
        if (!OldAnimations.INSTANCE.shouldBlock() || !OldAnimations.INSTANCE.getSwing()) return;
        if (OldAnimations.INSTANCE.getSwing()) {
            if (!mc.player.isUsingItem()) return;

            if (mc.player.getUseItem().getUseAnimation() != UseAnim.EAT &&
                    mc.player.getUseItem().getUseAnimation() != UseAnim.DRINK &&
                    mc.player.getUseItem().getUseAnimation() != UseAnim.BOW) return;
        }

        ci.cancel();

        float mainHandHeight = ((ItemInHandRendererAccessor) instance).getMainHandHeight();
        ((ItemInHandRendererAccessor) instance).setOMainHandHeight(mainHandHeight);
        AbstractClientPlayer clientPlayerEntity = mc.player;
        if (clientPlayerEntity == null) return;
        ItemStack itemStack = clientPlayerEntity.getMainHandItem();

        if (ItemStack.isSameItem(((ItemInHandRendererAccessor) instance).getMainHandItem(), itemStack))
            ((ItemInHandRendererAccessor) instance).setMainHandItem(itemStack);
        ((ItemInHandRendererAccessor) instance).setMainHandHeight(mainHandHeight +
                Mth.clamp((((ItemInHandRendererAccessor) instance).getMainHandItem() == itemStack ? 1f : 0.0F) -
                        mainHandHeight, -0.4F, 0.4F)
        );

        if (mainHandHeight < 0.1F)
            ((ItemInHandRendererAccessor) instance).setMainHandItem(itemStack);
    }

    private static int getHandMultiplier(Player player, InteractionHand hand) {
        HumanoidArm arm = hand == InteractionHand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
        return arm == HumanoidArm.RIGHT ? 1 : -1;
    }
}

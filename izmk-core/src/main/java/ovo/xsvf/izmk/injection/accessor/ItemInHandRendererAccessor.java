package ovo.xsvf.izmk.injection.accessor;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import ovo.xsvf.patchify.annotation.Accessor;
import ovo.xsvf.patchify.annotation.FieldAccessor;
import ovo.xsvf.patchify.annotation.MethodAccessor;

@Accessor(ItemInHandRenderer.class)
public interface ItemInHandRendererAccessor {
    @MethodAccessor
    void applyItemArmTransform(PoseStack pPoseStack, HumanoidArm pHand, float pEquippedProg);

    @MethodAccessor
    void applyItemArmAttackTransform(PoseStack pPoseStack, HumanoidArm pHand, float pSwingProgress);

    @MethodAccessor
    void renderItem(LivingEntity pEntity, ItemStack pItemStack, ItemDisplayContext pDisplayContext,
                    boolean pLeftHand, PoseStack pPoseStack, MultiBufferSource pBuffer, int pSeed);

    @FieldAccessor("mainHandItem")
    ItemStack getMainHandItem();

    @FieldAccessor(value = "mainHandItem", getter = false)
    void setMainHandItem(ItemStack pMainHandItem);

    @FieldAccessor("offHandItem")
    ItemStack getOffHandItem();

    @FieldAccessor("mainHandHeight")
    float getMainHandHeight();

    @FieldAccessor(value = "mainHandHeight", getter = false)
    void setMainHandHeight(float pMainHandHeight);

    @FieldAccessor("oMainHandHeight")
    float getOldMainHandHeight();

    @FieldAccessor(value = "oMainHandHeight", getter = false)
    void setOMainHandHeight(float pOldMainHandHeight);

    @FieldAccessor("offHandHeight")
    float getOffHandHeight();

    @FieldAccessor("oOffHandHeight")
    float getOldOffHandHeight();
}

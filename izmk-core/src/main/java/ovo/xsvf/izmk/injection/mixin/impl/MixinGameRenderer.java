package ovo.xsvf.izmk.injection.mixin.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.GameRenderer;
import ovo.xsvf.izmk.injection.mixin.CallbackInfo;
import ovo.xsvf.izmk.injection.mixin.annotation.Inject;
import ovo.xsvf.izmk.injection.mixin.annotation.Mixin;
import ovo.xsvf.izmk.module.impl.NoHurtcam;

@Mixin(GameRenderer.class)
public class MixinGameRenderer {
    @Inject(method = "bobHurt", desc = "(Lcom/mojang/blaze3d/vertex/PoseStack;F)V")
    public static void bobHurt(GameRenderer instance, PoseStack poseStack, float f, CallbackInfo callbackInfo) {
        callbackInfo.cancelled = NoHurtcam.INSTANCE.getEnabled();
    }
}

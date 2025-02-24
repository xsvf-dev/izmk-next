package ovo.xsvf.izmk.injection.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.GameRenderer;
import ovo.xsvf.izmk.module.impl.NoHurtcam;
import ovo.xsvf.patchify.CallbackInfo;
import ovo.xsvf.patchify.annotation.Inject;
import ovo.xsvf.patchify.annotation.Patch;

@Patch(GameRenderer.class)
public class MixinGameRenderer {
    @Inject(method = "bobHurt", desc = "(Lcom/mojang/blaze3d/vertex/PoseStack;F)V")
    public static void bobHurt(GameRenderer instance, PoseStack poseStack, float f, CallbackInfo callbackInfo) {
        callbackInfo.cancelled = NoHurtcam.INSTANCE.getEnabled();
    }
}

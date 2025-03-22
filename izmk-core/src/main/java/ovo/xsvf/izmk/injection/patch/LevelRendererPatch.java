package ovo.xsvf.izmk.injection.patch;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import net.optifine.CustomSky;
import org.joml.Matrix4f;
import ovo.xsvf.izmk.injection.accessor.LevelRendererAccessor;
import ovo.xsvf.patchify.CallbackInfo;
import ovo.xsvf.patchify.annotation.At;
import ovo.xsvf.patchify.annotation.Inject;
import ovo.xsvf.patchify.annotation.Patch;
import ovo.xsvf.patchify.annotation.Slice;

@Patch(LevelRenderer.class)
public class LevelRendererPatch {
    @Inject(method = "renderEndSky", desc = "(Lcom/mojang/blaze3d/vertex/PoseStack;F)V",
            at = @At(value = At.Type.BEFORE_INVOKE, method = "com/mojang/blaze3d/systems/RenderSystem/depthMask", desc = "(Z)V"))
    public static void renderEndSky(LevelRenderer instance, PoseStack pPoseStack, CallbackInfo ci) {
        CustomSky.renderSky(((LevelRendererAccessor) instance).level(), pPoseStack, 0.0F);
    }

    @Inject(method = "renderSky", desc = "(Lcom/mojang/blaze3d/vertex/PoseStack;Lorg/joml/Matrix4f;FLnet/minecraft/client/Camera;ZLjava/lang/Runnable;)V",
            at = @At(value = At.Type.BEFORE_INVOKE,
                    method = "net/minecraft/client/multiplayer/ClientLevel/getTimeOfDay",
                    remapped = "net/minecraft/client/multiplayer/ClientLevel/m_46942_", desc = "(F)F"),
            slice = @Slice(startIndex = 2, endIndex = 2))
    public static void renderSky(LevelRenderer instance, PoseStack pPoseStack, Matrix4f pProjectionMatrix,
                                 float pPartialTick, Camera pCamera, boolean pIsFoggy, Runnable pSkyFogSetup, CallbackInfo ci) {
        CustomSky.renderSky(((LevelRendererAccessor) instance).level(), pPoseStack, pPartialTick);
    }
}